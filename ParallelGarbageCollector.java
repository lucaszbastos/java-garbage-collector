import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class ParallelGarbageCollector implements GarbageCollector {
    private final GCObject[] youngHeap;
    private final GCObject[] oldHeap;
    private volatile int youngPointer = 0;
    private volatile int oldPointer = 0;
    private final Random random;
    private final int promotionThreshold;
    private final Set<String> actualDeletedIds = ConcurrentHashMap.newKeySet();
    private final List<GCObject> rootObjects = new CopyOnWriteArrayList<>();
    private final ExecutorService executor;
    private final int numThreads;

    public ParallelGarbageCollector(int youngSize, int oldSize, int rootObjectsQuantity, int numThreads, int promotionThreshold) {
        this.youngHeap = new GCObject[youngSize];
        this.oldHeap = new GCObject[oldSize];
        this.random = new Random();
        this.executor = Executors.newFixedThreadPool(numThreads);
        this.numThreads = numThreads;
        this.promotionThreshold = promotionThreshold;
        setRootObjects(rootObjectsQuantity);
    }

    public void setRootObjects(int rootObjectsQuantity) {
        for (int i = 0; i < rootObjectsQuantity; i++) {
            GCObject gcObject = new GCObject("Root");
            allocateYoung(gcObject);
            rootObjects.add(gcObject);
        }
    }

    public synchronized void allocateYoung(GCObject object) {
        System.out.println("Allocating in Young Heap: "+object.id);
        System.out.println("Print Heap before allocation: ");
        printHeap();
        if(youngPointer < youngHeap.length){
            randomReference(object);
            youngHeap[youngPointer++] = object;
            promoteYoungObjects();
            System.out.println("Print Heap After allocation: ");
            printHeap();
            return;
        }
        minorGC();
        randomReference(object);
        youngHeap[youngPointer++] = object;
        promoteYoungObjects();
        System.out.println("Print Heap After allocation: ");
        printHeap();
    }

    public synchronized void allocateOld(GCObject object) {
        System.out.println("Allocating in Old Heap: "+object.id);
        if(oldPointer < oldHeap.length){
            oldHeap[oldPointer++] = object;
            object.marked = false;
            System.out.println("Allocated Old: " + object);
            System.out.println("Old Pointer: " + oldPointer);
            return;
        }
        fullGC();
        if(actualDeletedIds.contains(object.id)){
            System.out.println("Object was deleted during full GC, skipping allocation");
            return;
        }
        oldHeap[oldPointer++] = object;
        object.marked = false;
        System.out.println("Allocated Old: " + object);
        System.out.println("Old Pointer: " + oldPointer);
    }

    public void randomReference(GCObject newObject) {
        if ("Root".equals(newObject.id)) return;

        GCObject target;
        int randomIndex;
        do {
            randomIndex = random.nextInt(youngHeap.length + oldHeap.length);
            if (randomIndex < youngHeap.length) {
                target = youngHeap[randomIndex];
            } else {
                target = oldHeap[randomIndex - youngHeap.length];
            }
        } while (target == null);

        synchronized (target) {
            target.addReference(newObject);
        }

        System.out.println("Added Ref " + newObject.id + " as reference to " + target.id);
    }

    public void mark(GCObject obj) {
        mark(obj, null);
    }


    private void mark(GCObject obj, GCObject parent) {

        if (actualDeletedIds.contains(obj.id)) {
            if (parent != null) {

                parent.references.removeIf(r -> actualDeletedIds.contains(r.id));
            }
            actualDeletedIds.remove(obj.id);
            return;
        }

        if (obj.marked) return;

        obj.marked = true;

        for (GCObject child : obj.references) {
            mark(child, obj);
        }
    }

    public void sweep(GCObject[] heap) throws InterruptedException {
        int chunkSize = (int) Math.ceil((double) heap.length / numThreads);
        List<Callable<Void>> tasks = new CopyOnWriteArrayList<>();

        for (int i = 0; i < heap.length; i += chunkSize) {
            final int start = i;

            final int end = Math.min(i + chunkSize, heap.length);
            tasks.add(() -> {
                for (int j = start; j < end; j++) {

                    if (heap[j] != null && !heap[j].marked) {
                        actualDeletedIds.add(heap[j].id);
                        heap[j] = null;
                    }
                }
                return null;
            });
        }

        executor.invokeAll(tasks);
    }

    public void unmarkAll() {
        for (GCObject obj : youngHeap) {
            if (obj != null) obj.marked = false;
        }
        for (GCObject obj : oldHeap) {
            if (obj != null) obj.marked = false;
        }
    }

    public synchronized void promoteYoungObjects() {
        System.out.println("Promoting Young Objects");
        for (int i = 0; i < youngPointer; i++) {
            if (youngHeap[i] != null && youngHeap[i].age >= promotionThreshold) {
                GCObject obj = youngHeap[i];
                youngHeap[i] = null;
                allocateOld(obj);
            } else if (youngHeap[i] != null) {
                youngHeap[i].age++;
            }
        }
        youngPointer = compact(youngHeap);
    }

    public int compact(GCObject[] heap) {
        int ptr = 0;
        for (int i = 0; i < heap.length; i++) {
            if (heap[i] != null) {
                heap[ptr++] = heap[i];
                if (i != ptr - 1) heap[i] = null;
            }
        }
        return ptr;
    }

    private void randomDeletion(GCObject[] heap) {
        int pos;
        do {
            pos = random.nextInt(heap.length);
        } while (heap[pos] == null || "Root".equals(heap[pos].id));

        System.out.println("Random Deletion: " + heap[pos].id );
        heap[pos].printReferences( heap[pos]);
        System.out.println();
        actualDeletedIds.add(heap[pos].id);
        heap[pos] = null;
    }

    public void markAndSweep(GCObject[] heap, String heapName) throws InterruptedException {
        List<Callable<Void>> markTasks = new CopyOnWriteArrayList<>();
        for (GCObject root : rootObjects) {
            markTasks.add(() -> {
                synchronized (root) {
                    mark(root);
                }
                return null;
            });
        }
        System.out.println(heapName+" Marking and Sweeping");

        executor.invokeAll(markTasks);
        System.out.println(heapName+" after parallel Marking");
        printHeap();

        sweep(heap);
        unmarkAll();
        System.out.println(heapName+" after parallel sweep");
        printHeap();
    }

    public synchronized void minorGC() {
        System.out.println("Starting Minor GC:");
        randomDeletion(youngHeap);
        try {
            markAndSweep(youngHeap,"Young Heap");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        youngPointer = compact(youngHeap);
    }

    public synchronized void fullGC() {
        System.out.println("Starting Full GC:");
        randomDeletion(oldHeap);
        try {
            markAndSweep(oldHeap, "Old Heap");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        oldPointer = compact(oldHeap);
        minorGC();
    }

    public void printHeap() {
        System.out.println("Young Heap:");
        for (GCObject gcObject : youngHeap) {
            System.out.print(gcObject != null ? gcObject : "[ ]");
        }
        System.out.println();

        System.out.println("Old Heap:");
        for (GCObject gcObject : oldHeap) {
            System.out.print(gcObject != null ? gcObject : "[ ]");
        }
        System.out.println("\n");
    }

    @Override
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

