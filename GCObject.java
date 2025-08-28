import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class GCObject {
    public volatile boolean marked = false;
    int age = 0;
    String id;
    List<GCObject> references = new CopyOnWriteArrayList<>();

    public GCObject(String id) {
        this.id = id;
    }

    public void addReference(GCObject obj) {
        references.add(obj);
    }

    @Override
    public String toString() {
        return marked ? "[" + id + "*, age: "+ age+"]" : "[" + id + ", age: "+ age+"]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GCObject)) return false;
        GCObject that = (GCObject) o;
        return Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public void printReferences(GCObject object) {
        printReferences(object, new HashSet<>(), 0);
    }

    private void printReferences(GCObject object, Set<GCObject> visited, int depth) {

        String indent = "  ".repeat(depth);
        System.out.print(indent + object + " references: [");
        for (int i = 0; i < object.references.size(); i++) {
            System.out.print(object.references.get(i).id);
            if (i < object.references.size() - 1) System.out.print(", ");
        }
        System.out.println("]");


        if (!visited.add(object)) return;


        for (GCObject ref : object.references) {
            printReferences(ref, visited, depth + 1);
        }
    }
}
