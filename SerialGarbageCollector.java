import java.util.*;

public class SerialGarbageCollector implements GarbageCollector {
    private final GCObject[] youngHeap;
    private final GCObject[] oldHeap;
    private int youngPointer = 0;
    private int oldPointer = 0;
    private final Random random;
    private final int promotionThreshold;
    private final Set<String> actualDeletedIds = new HashSet<>();
    private final List<GCObject> rootObjects = new ArrayList<>();

    public SerialGarbageCollector(int youngSize, int oldSize, int rootObjectsQuantity, int promotionThreshold ) {
        youngHeap = new GCObject[youngSize];
        oldHeap = new GCObject[oldSize];
        random = new Random();
        this.promotionThreshold = promotionThreshold;
        setRootObjects(rootObjectsQuantity);
    }

    public Random getRandom(){
        return this.random;
    }

    public void setRootObjects(int rootObjectsQuantity){
        for(int i = 0; i < rootObjectsQuantity; i++){
            GCObject gcObject = new GCObject("Root");
            this.allocateYoung(gcObject);
            rootObjects.add(gcObject);

        }

    }

    public void allocateYoung(GCObject object){
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

    public void allocateOld(GCObject object){
        System.out.println("Allocating in Old Heap: "+object.id);
        if(oldPointer < oldHeap.length){
            oldHeap[oldPointer++] = object;
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
        System.out.println("Allocated Old: " + object);
        System.out.println("Old Pointer: " + oldPointer);

    }
    public void randomReference(GCObject newObject){
        if(newObject.id.equals("Root")){
            return;
        }
        GCObject target;
        int randomIndex;
        do{
             randomIndex = this.getRandom().nextInt(youngHeap.length + oldHeap.length);
            if(randomIndex < youngHeap.length){
                target = youngHeap[randomIndex];
            }else{
                target = oldHeap[randomIndex - youngHeap.length];
            }
        }while (target == null);
        target.addReference(newObject);
        System.out.println("Added Ref " + newObject.id + " as reference to " + target.id);

    }
    public void mark(GCObject obj) {
        mark(obj, null); // overload para manter a API atual
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
    public void sweep(GCObject[] heapRegion){
        for(int i = 0; i < heapRegion.length; i++){
           // System.out.println("Analyzing: " + heapRegion[i].id + "Status: "+heapRegion[i].marked);
            if(heapRegion[i]!= null && !heapRegion[i].marked){
                System.out.println("Removing: " + heapRegion[i].id);
                actualDeletedIds.add(heapRegion[i].id);
                heapRegion[i] = null;
            }
        }
    }
    public void unmkark(){
        for (GCObject object : youngHeap) {
            if (object != null) {
                object.marked = false;
            }
        }
        for (GCObject object : oldHeap) {
            if (object != null) {
                object.marked = false;
            }
        }
    }
    public void promoteYoungObjects() {
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
        int compactPointer = 0;
        for (int i = 0; i < heap.length; i++) {
            if (heap[i] != null) {
                heap[compactPointer++] = heap[i];
                if (i != compactPointer - 1) heap[i] = null;
            }
        }

        return compactPointer;
    }


    private void randomDeletion(GCObject[] heap) {
        int position;
        do {
            position = this.getRandom().nextInt(heap.length);
        } while (heap[position] == null || "Root".equals(heap[position].id));
        System.out.println("Random Deletion: " + heap[position].id);
    //    System.out.println(heap[position].id +" references: "+ heap[position].references);
        heap[position].printReferences( heap[position]);
        System.out.println();
        this.actualDeletedIds.add(heap[position].id);
        heap[position] = null;
    }

    public void markAndSweep(GCObject[] heap, String heapName)  {
        System.out.println(heapName+" Marking and Sweeping");
        for(GCObject obj : rootObjects){
            mark(obj);
        }
        System.out.println(heapName+" after Marking");
        printHeap();
        sweep(heap);
        unmkark();
        System.out.println(heapName+" after sweep");
        printHeap();
    }

    public void minorGC() {
        System.out.println("Starting Minor GC:");
        randomDeletion(youngHeap);
        markAndSweep(youngHeap, "Young Heap");
        youngPointer = compact(youngHeap);

    }

    public void fullGC() {
        System.out.println("Starting Full GC:");
        randomDeletion(oldHeap);
        markAndSweep(oldHeap, "Old Heap");
        oldPointer = compact(oldHeap);
        minorGC();
    }

    public void printHeap(){
        System.out.println("Young Heap");
        for (GCObject gcObject : youngHeap) {
            System.out.print(gcObject != null ? gcObject : "[ ]");
        }
        System.out.println();
        System.out.println("Old Heap: ");
        for (GCObject object : oldHeap) {
            System.out.print(object != null ? object : "[ ]");
        }
        System.out.println();
        System.out.println();
    }



}
