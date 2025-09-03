package entities;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import enums.WorkItemStatus;

public abstract class WorkItem implements Iterable<WorkItem> {
    private static final Logger logger = Logger.getLogger(WorkItem.class.getName());

    protected Integer id;
    protected String name;
    protected WorkItemStatus status;
    protected final List<WorkItem> children = new ArrayList<>();

    public WorkItem() {
        this.id = 0;
        this.status = WorkItemStatus.NOT_STARTED;
    }

    // ==== Abstract methods to implement in subclasses ====
    public abstract String serialize(Integer parentId);
    // public abstract void parse(String line);

    public abstract void print(Integer depth, boolean printDetails);
    // =====================================================

    public Integer getID() { return this.id; }
    public void setID(Integer id) { this.id = id; }

    public String getName() { return this.name; }
    public void setName(String name) { this.name = name; }

    public WorkItemStatus getStatus() { return this.status; }
    public void setStatus(WorkItemStatus status) { this.status = status; }

    public List<WorkItem> getChildren() { return this.children; }

    public void addChild(WorkItem child) {
        children.add(child);
    }

    public interface Builder<T extends WorkItem> {
        T build();
    }

    private static final List<WorkItemObserver> observers = new ArrayList<>();

    public static void addObserver(WorkItemObserver observer) {
        observers.add(observer);
    }

    public static void removeObserver(WorkItemObserver observer) {
        observers.remove(observer);
    }

    public static void notifyWorkItemAdded(WorkItem item) {
        for (WorkItemObserver observer : observers) {
            observer.onWorkItemAdded(item);
        }
    }

    public static void notifyWorkItemDeleted(WorkItem item) {
        for (WorkItemObserver observer : observers) {
            observer.onWorkItemDeleted(item);
        }
    }

    public static boolean wouldCreateCycle(WorkItem parent, WorkItem child) {
        if (parent == null || child == null) return false;
        for (WorkItem descendant : child) {
            if (descendant.equals(parent)) return true;
        }
        return false;
    }

    public static Integer getNextID(List<? extends WorkItem> items) {
        return getNextIDInternal(items, new HashSet<>()) + 1;
    }

    @Override
    public Iterator<WorkItem> iterator() {
        return new WorkItemIterator(this);
    }

    // Depth-first iterator for WorkItem hierarchy
    private static class WorkItemIterator implements Iterator<WorkItem> {
        private final Stack<WorkItem> stack = new Stack<>();

        public WorkItemIterator(WorkItem root) {
            if (root != null) stack.push(root);
        }

        @Override
        public boolean hasNext() {
            return !stack.isEmpty();
        }

        @Override
        public WorkItem next() {
            WorkItem current = stack.pop();
            // Add children in reverse order to maintain left-to-right traversal
            List<WorkItem> children = current.getChildren();
            for (int i = children.size() - 1; i >= 0; i--) {
                stack.push(children.get(i));
            }
            return current;
        }
    }

    private static Integer getNextIDInternal(List<? extends WorkItem> items, Set<Integer> visited) {
        int maxId = 0;
        for (WorkItem root : items) {
            for (WorkItem wi : root) { // use iterator
                if (!visited.add(wi.getID())) continue;
                if (wi.getID() > maxId) maxId = wi.getID();
            }
        }
        return maxId;
    }

    public static WorkItem findById(List<WorkItem> items, int id) {
        for (WorkItem root : items) {
            for (WorkItem wi : root) {
                if (wi.getID() == id) return wi;
            }
        }
        return null;
    }

    public void printHierarchy(Integer depth, boolean printDetails) {
        printHierarchy(depth, new HashSet<>(), printDetails);
    }

    private void printHierarchy(Integer depth, Set<Integer> visited, boolean printDetails) {
        if (!visited.add(this.getID())) {
            System.out.println(getPrefixByDepth(depth + 1, '-') + "[Cycle detected: ID " + this.getID() + "]");
            return;
        }
        this.print(depth, printDetails);

        if (printDetails) {
            System.out.println(); // Empty line
        }
        
        if (this.children != null) {
            // Sort children by ID and print hierarchy
            this.children.stream()
                .sorted(Comparator.comparingInt(WorkItem::getID))
                .forEach(wi -> wi.printHierarchy(depth + 1, visited, printDetails));
        }
        visited.remove(this.getID());
    }

    protected String getPrefixByDepth(Integer depth, Character character) {
        if (depth == 0)
            return "";

        depth *= 4;
        String prefix = "";

        for (int i = 0; i < depth; i++) {
            prefix += character;
        }

        prefix += " ";

        return prefix;
    }

    public static String getClassFromLine(String line) {
        if (line == null || line.isEmpty()) return null;

        String[] parts = line.split(";");
        for (String part : parts) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2) {
                String key = kv[0].trim();
                String value = kv[1].trim();
                if (key.equalsIgnoreCase("Type")) {
                    if (value.equalsIgnoreCase("epic") || value.equalsIgnoreCase("phase") ||  
                    value.equalsIgnoreCase("feature") || value.equalsIgnoreCase("milestone") || 
                    value.equalsIgnoreCase("task"))
                    return value; // Here returning "Epic", "Phase", "Feature", etc.
                }
            }
        }

        logger.log(Level.WARNING, "Type key not found in line " + line);
        return null;
    }

    public static Integer getParentIdFromLine(String line) {
        if (line == null || line.isEmpty()) return null;

        String[] parts = line.split(";");
        for (String part : parts) {
            String[] kv = part.split("=");
            if (kv.length == 2 && kv[0].trim().equalsIgnoreCase("ParentID")) {
                try {
                    return Integer.parseInt(kv[1].trim());
                } 
                catch (Exception e) {
                    return null;
                }
            }
        }
        return null;
    }
}
