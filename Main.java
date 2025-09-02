public class Main {
    private static GarbageCollector getGarbageCollector(String[] args) {

        if (args == null || args.length < 6) {
            throw new IllegalArgumentException(
                    "Usage: java Main <objectsQuantity> <mode> <youngSize> <oldSize> <rootCount> <promotionThreshold> [numThreads]\n" +
                            "  mode: 1=Serial, 2=Parallel"
            );
        }

        int mode               = parseIntOrThrow(args[1], "mode");
        int youngSize          = parsePositiveInt(args[2], "youngSize");
        int oldSize            = parsePositiveInt(args[3], "oldSize");
        int rootCount          = parsePositiveInt(args[4], "rootCount"); // deve ser >0
        int promotionThreshold = parseNonNegativeInt(args[5], "promotionThreshold");


        if (mode != 1 && mode != 2) {
            throw new IllegalArgumentException("Invalid mode: " + mode + " (valid: 1=Serial, 2=Parallel)");
        }

        if (rootCount > oldSize) {
            throw new IllegalArgumentException("Invalid rootCount: must be <= oldSize (" + oldSize + ")");
        }

        if (youngSize < 1) throw new IllegalArgumentException("youngSize must be >= 1");
        if (oldSize   < 1) throw new IllegalArgumentException("oldSize must be >= 1");
        GarbageCollector collector;

        if (mode == 1) {
            collector = new SerialGarbageCollector(youngSize, oldSize, rootCount, promotionThreshold);
        } else {
            if (args.length < 7) {
                throw new IllegalArgumentException("Missing numThreads for Parallel mode. Usage: ... [numThreads]");
            }
            int numThreads = parsePositiveInt(args[6], "numThreads"); // >=1
            collector = new ParallelGarbageCollector(youngSize, oldSize, rootCount, numThreads, promotionThreshold);
        }

        return collector;
    }

    private static int parseIntOrThrow(String s, String name) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid integer for " + name + ": '" + s + "'", e);
        }
    }

    private static int parsePositiveInt(String s, String name) {
        int v = parseIntOrThrow(s, name);
        if (v <= 0) throw new IllegalArgumentException(name + " must be > 0 (got " + v + ")");
        return v;
    }

    private static int parseNonNegativeInt(String s, String name) {
        int v = parseIntOrThrow(s, name);
        if (v < 0) throw new IllegalArgumentException(name + " must be >= 0 (got " + v + ")");
        return v;
    }

    public static void main(String[] args) {

        try (GarbageCollector collector = getGarbageCollector(args)) {
            int objectsQuantity = Integer.parseInt(args[0]);
            for (int i = 1; i <= objectsQuantity; i++) {
                collector.allocateYoung(new GCObject("Obj" + i));
            }
        }
        
        
        }
      
            
    }
        

