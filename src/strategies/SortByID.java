package strategies;

import entities.WorkItem;
import java.util.List;
import java.util.Comparator;

public class SortByID implements SortStrategy {
    public void sort(List<WorkItem> items) {
        items.sort(Comparator.comparingInt(WorkItem::getID));
    }
}