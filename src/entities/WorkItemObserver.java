package entities;

public interface WorkItemObserver {
    void onWorkItemAdded(WorkItem item);
    void onWorkItemDeleted(WorkItem item);
}