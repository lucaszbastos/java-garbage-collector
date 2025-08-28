public interface GarbageCollector {

    void mark(GCObject obj);

    void sweep(GCObject[] heap) throws InterruptedException;
    void minorGC();
    void fullGC();
    void markAndSweep(GCObject[] heap, String heapName) throws InterruptedException;
    int compact(GCObject[] heap);
    void promoteYoungObjects();
    void allocateYoung(GCObject object);
    void allocateOld(GCObject object);
}
