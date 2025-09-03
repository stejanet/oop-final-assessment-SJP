package validators;

import entities.WorkItem;
import java.util.List;

public class ExistingWorkItemValidator extends InputValidator {
    private final List<WorkItem> workItems;

    public ExistingWorkItemValidator(List<WorkItem> workItems) {
        this.workItems = workItems;
    }

    @Override
    public boolean validate(String input) {
        int id = Integer.parseInt(input.trim());
        WorkItem found = WorkItem.findById(workItems, id);
        if (found == null) {
            System.out.println("No work item found with ID " + id + ".");
            return false;
        }
        return validateNext(input);
    }
}