package strategies;

import entities.WorkItem;
import java.util.List;

public interface SortStrategy {
    void sort(List<WorkItem> items);
}