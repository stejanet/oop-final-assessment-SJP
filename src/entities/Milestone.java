package entities;

import java.time.LocalDate;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import enums.WorkItemStatus;
import utils.ParseUtils;

public class Milestone extends WorkItem {
    private static final Logger logger = Logger.getLogger(Milestone.class.getName());

    private LocalDate dueDate;

    public LocalDate getDueDate() { return this.dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public static class Builder implements WorkItem.Builder<Milestone> {
        private String name;
        private WorkItemStatus status = WorkItemStatus.NOT_STARTED;
        private LocalDate dueDate;

        public Builder setName(String name) { this.name = name; return this; }
        public Builder setStatus(WorkItemStatus status) { this.status = status; return this; }
        public Builder setDueDate(LocalDate dueDate) { this.dueDate = dueDate; return this; }

        @Override
        public Milestone build() {
            Milestone milestone = new Milestone();
            milestone.setName(name);
            milestone.setStatus(status);
            milestone.setDueDate(dueDate);
            return milestone;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Milestone parse(String line) {
        Milestone milestone = new Milestone();
        Map<String, String> map;

        try {
            map = ParseUtils.parseKeyValuePairs(line);
            if (map.containsKey("id")) milestone.id = Integer.parseInt(map.get("id"));
            if (map.containsKey("name")) milestone.name = map.get("name");
            if (map.containsKey("status")) milestone.status = WorkItemStatus.valueOf(map.get("status").toUpperCase());
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to parse Milestone from line: " + line, e);
            return null;
        }
        
        if (map.containsKey("duedate")) {
            try { milestone.dueDate = LocalDate.parse(map.get("duedate")); } catch (Exception e) { milestone.dueDate = null; }
        }

        return milestone;
    }
    
    @Override
    public String serialize(Integer parentId) {
        return 
            "Type=Milestone;ID=" + this.id + (parentId != null ? ";ParentID=" + parentId : "") + ";Name=" + this.name + ";Status=" + this.status + 
            ";DueDate=" + (this.dueDate != null ? this.dueDate : "");
    }
    
    @Override
    public void print(Integer depth, boolean printDetails) {
        System.out.println(getPrefixByDepth(depth, '-') + "[" + this.id + "] MILESTONE: " + this.name + " (" + this.status.getDisplayName() + ")");

        if (printDetails) {
            System.out.println(getPrefixByDepth(depth, ' ') + "Due date: " + (this.dueDate != null ? this.dueDate : "--"));
        }
    }
}
