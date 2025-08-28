public class Main {
    private static GarbageCollector getGarbageCollector(String[] args) {
        int mode = Integer.parseInt(args[1]);
        int youngSize = Integer.parseInt(args[2]);
        int oldSize = Integer.parseInt(args[3]);
        int rootObjectsQuantity = Integer.parseInt(args[4]);
        int promotionThreshold = Integer.parseInt(args[5]);
        GarbageCollector collector;


        if(rootObjectsQuantity == 0 || rootObjectsQuantity> oldSize){
            throw new IllegalArgumentException("Invalid root objects quantity");
        }

        if(mode == 1){
            collector = new SerialGarbageCollector(youngSize, oldSize,
                    rootObjectsQuantity,promotionThreshold);
        }else{
            int numThreads = Integer.parseInt(args[6]);
            collector = new ParallelGarbageCollector(youngSize, oldSize,
                    rootObjectsQuantity, numThreads,promotionThreshold);
        }
        return collector;
    }

    public static void main(String[] args) {




        GarbageCollector collector = getGarbageCollector(args);
        int objectsQuantity = Integer.parseInt(args[0]);
        for(int i = 1; i<=objectsQuantity; i++){
            collector.allocateYoung(new GCObject("Obj"+i));
        }
        
        
        }
      
            
    }
        

