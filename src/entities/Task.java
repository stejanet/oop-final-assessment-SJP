package entities;

import java.time.LocalDate;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import enums.WorkItemStatus;
import utils.ParseUtils;

public class Task extends WorkItem {
    private static final Logger logger = Logger.getLogger(Task.class.getName());

    private String description;
    private LocalDate dueDate;
    private Double estimatedHours;

    public String getDescription() { return this.description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getDueDate() { return this.dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public Double getEstimatedHours() { return this.estimatedHours; }
    public void setEstimatedHours(Double estimatedHours) { this.estimatedHours = estimatedHours; }
    
    @Override
    public void addChild(WorkItem child) {
        throw new UnsupportedOperationException("Tasks cannot have children.");
    }

    public static class Builder implements WorkItem.Builder<Task> {
        private String name;
        private WorkItemStatus status = WorkItemStatus.NOT_STARTED;
        private String description;
        private LocalDate dueDate;
        private Double estimatedHours;

        public Builder setName(String name) { this.name = name; return this; }
        public Builder setStatus(WorkItemStatus status) { this.status = status; return this; }
        public Builder setDescription(String description) { this.description = description; return this; }
        public Builder setDueDate(LocalDate dueDate) { this.dueDate = dueDate; return this; }
        public Builder setEstimatedHours(Double estimatedHours) { this.estimatedHours = estimatedHours; return this; }

        @Override
        public Task build() {
            Task task = new Task();
            task.setName(name);
            task.setStatus(status);
            task.setDescription(description);
            task.setDueDate(dueDate);
            task.setEstimatedHours(estimatedHours);
            return task;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Task parse(String line) {
        Task task = new Task();
        Map<String, String> map;

        try {
            map = ParseUtils.parseKeyValuePairs(line);
            if (map.containsKey("id")) task.id = Integer.parseInt(map.get("id"));
            if (map.containsKey("name")) task.name = map.get("name");
            if (map.containsKey("status")) task.status = WorkItemStatus.valueOf(map.get("status").toUpperCase());
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to parse Task from line: " + line, e);
            return null;
        }

        if (map.containsKey("description")) {
            try { task.description = map.get("description"); } catch (Exception e) { task.description = null; }
        }
        if (map.containsKey("duedate")) {
            try { task.dueDate = LocalDate.parse(map.get("duedate")); } catch (Exception e) { task.dueDate = null; }
        }
        if (map.containsKey("estimatedhours")) {
            try { task.estimatedHours = Double.parseDouble(map.get("estimatedhours")); } catch (Exception e) { task.estimatedHours = null; }
        }

        return task;
    }

    @Override
    public String serialize(Integer parentId) {
        return 
            "Type=Task;ID=" + this.id + (parentId != null ? ";ParentID=" + parentId : "") + ";Name=" + this.name + ";Status=" + this.status + 
            ";Description=" + (this.description != null ? this.description : "") + ";DueDate=" + (this.dueDate != null ? this.dueDate : "") + 
            ";EstimatedHours=" + (this.estimatedHours != null ? this.estimatedHours : "");
    }
    
    @Override
    public void print(Integer depth, boolean printDetails) {
        System.out.println(getPrefixByDepth(depth, '-') + "[" + this.id + "] TASK: " + this.name + " (" + this.status.getDisplayName() + ")");

        if (printDetails) {
            System.out.println(getPrefixByDepth(depth, ' ') + "Due date: " + (this.dueDate != null ? this.dueDate : "--") + ", Estimated hours: " + (this.estimatedHours != null ? this.estimatedHours : "--"));
            System.out.println(getPrefixByDepth(depth, ' ') + "Description: " + (this.description != null && this.description.length() > 0 ? this.description : "--"));
        }
    }
}
