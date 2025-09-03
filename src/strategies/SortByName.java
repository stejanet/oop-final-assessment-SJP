package strategies;

import entities.WorkItem;
import java.util.List;
import java.util.Comparator;

public class SortByName implements SortStrategy {
    public void sort(List<WorkItem> items) {
        items.sort(Comparator.comparing(WorkItem::getName, String.CASE_INSENSITIVE_ORDER));
    }
}