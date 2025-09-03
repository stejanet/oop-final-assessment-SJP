package factories;
import entities.*;
import enums.*;

// Factory
public class WorkItemFactory {
    public static WorkItem createWorkItem(WorkItemType type) {
        if (type == WorkItemType.EPIC) {
            return new Epic();
        } 
        else if (type == WorkItemType.PHASE) {
            return new Phase();
        }
        else if (type == WorkItemType.FEATURE) {
            return new Feature();
        }
        else if (type == WorkItemType.MILESTONE) {
            return new Milestone();
        }
        else if (type == WorkItemType.TASK) {
            return new Task();
        }
        else {
            return null;
        }
    }

    public static WorkItem createWorkItem(String line) {
        String type = WorkItem.getClassFromLine(line);
        
        switch (type.toLowerCase()) {
            case "epic": return Epic.parse(line);
            case "phase": return Phase.parse(line);
            case "feature": return Feature.parse(line);
            case "milestone": return Milestone.parse(line);
            case "task": return Task.parse(line);
            default: throw new IllegalArgumentException("Unknown type: " + type);
        }
    }
}
