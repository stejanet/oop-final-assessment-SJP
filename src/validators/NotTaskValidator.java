package validators;

import entities.WorkItem;
import entities.Task;
import java.util.List;

public class NotTaskValidator extends InputValidator {
    private final List<WorkItem> workItems;

    public NotTaskValidator(List<WorkItem> workItems) {
        this.workItems = workItems;
    }

    @Override
    public boolean validate(String input) {
        int id = Integer.parseInt(input.trim());
        WorkItem found = WorkItem.findById(workItems, id);
        if (found instanceof Task) {
            System.out.println("Parent cannot be a Task.");
            return false;
        }
        return validateNext(input);
    }
}