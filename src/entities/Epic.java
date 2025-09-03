package entities;

import java.time.LocalDate;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import enums.WorkItemStatus;
import utils.ParseUtils;

public class Epic extends WorkItem {
    private static final Logger logger = Logger.getLogger(Epic.class.getName());

    private String description;
    private LocalDate startDate;
    private LocalDate endDate;

    public String getDescription() { return this.description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getStartDate() { return this.startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return this.endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public static class Builder implements WorkItem.Builder<Epic> {
        private String name;
        private WorkItemStatus status = WorkItemStatus.NOT_STARTED;
        private String description;
        private LocalDate startDate;
        private LocalDate endDate;

        public Builder setName(String name) { this.name = name; return this; }
        public Builder setStatus(WorkItemStatus status) { this.status = status; return this; }
        public Builder setDescription(String description) { this.description = description; return this; }
        public Builder setStartDate(LocalDate startDate) { this.startDate = startDate; return this; }
        public Builder setEndDate(LocalDate endDate) { this.endDate = endDate; return this; }

        @Override
        public Epic build() {
            Epic epic = new Epic();
            epic.setName(name);
            epic.setStatus(status);
            epic.setDescription(description);
            epic.setStartDate(startDate);
            epic.setEndDate(endDate);
            return epic;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Epic parse(String line) {
        Epic epic = new Epic();
        Map<String, String> map;

        try {
            map = ParseUtils.parseKeyValuePairs(line);

            if (map.containsKey("id")) epic.id = Integer.parseInt(map.get("id"));
            if (map.containsKey("name")) epic.name = map.get("name");
            if (map.containsKey("status")) epic.status = WorkItemStatus.valueOf(map.get("status").toUpperCase());
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to parse Epic from line: " + line, e);
            return null;
        }

        if (map.containsKey("description")) {
            try { epic.description = map.get("description"); } catch (Exception e) { epic.description = null; }
        }
        if (map.containsKey("startdate")) {
            try { epic.startDate = LocalDate.parse(map.get("startdate")); } catch (Exception e) { epic.startDate = null; }
        }
        if (map.containsKey("enddate")) {
            try { epic.endDate = LocalDate.parse(map.get("enddate")); } catch (Exception e) { epic.endDate = null; }
        }

        return epic;
    }
    
    @Override
    public String serialize(Integer parentId) {
        return 
            "Type=Epic;ID=" + this.id + ";Name=" + this.name + ";Status=" + this.status + 
            ";Description=" + (this.description != null ? this.description : "") + ";StartDate=" + (this.startDate != null ? this.startDate : "") + 
            ";EndDate=" + (this.endDate != null ? this.endDate : "");
    }
    
    @Override
    public void print(Integer depth, boolean printDetails) {
        System.out.println(getPrefixByDepth(depth, '-') + "[" + this.id + "] EPIC: " + this.name + " (" + this.status.getDisplayName() + ")");
        
        if (printDetails) {
            System.out.println(getPrefixByDepth(depth, ' ') + "Start date: " + (this.startDate != null ? this.startDate : "--") + ", End date: " + (this.endDate != null ? this.endDate : "--"));
            System.out.println(getPrefixByDepth(depth, ' ') + "Description: " + (this.description != null && this.description.length() > 0 ? this.description : "--"));
        }
    }
}
