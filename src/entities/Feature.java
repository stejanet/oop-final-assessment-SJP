package entities;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import enums.WorkItemStatus;
import utils.ParseUtils;

public class Feature extends WorkItem {
    private static final Logger logger = Logger.getLogger(Feature.class.getName());

    private String description;
    private Double estimatedHours;

    public String getDescription() { return this.description; }
    public void setDescription(String description) { this.description = description; }

    public Double getEstimatedHours() { return this.estimatedHours; }
    public void setEstimatedHours(Double estimatedHours) { this.estimatedHours = estimatedHours; }

    public static class Builder implements WorkItem.Builder<Feature> {
        private String name;
        private WorkItemStatus status = WorkItemStatus.NOT_STARTED;
        private String description;
        private Double estimatedHours;

        public Builder setName(String name) { this.name = name; return this; }
        public Builder setStatus(WorkItemStatus status) { this.status = status; return this; }
        public Builder setDescription(String description) { this.description = description; return this; }
        public Builder setEstimatedHours(Double estimatedHours) { this.estimatedHours = estimatedHours; return this; }

        @Override
        public Feature build() {
            Feature feature = new Feature();
            feature.setName(name);
            feature.setStatus(status);
            feature.setDescription(description);
            feature.setEstimatedHours(estimatedHours);
            return feature;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Feature parse(String line) {
        Feature feature = new Feature();
        Map<String, String> map;

        try {
            map = ParseUtils.parseKeyValuePairs(line);
            if (map.containsKey("id")) feature.id = Integer.parseInt(map.get("id"));
            if (map.containsKey("name")) feature.name = map.get("name");
            if (map.containsKey("status")) feature.status = WorkItemStatus.valueOf(map.get("status").toUpperCase());
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to parse Feature from line: " + line, e);
            return null;
        }

        if (map.containsKey("description")) {
            try { feature.description = map.get("description"); } catch (Exception e) { feature.description = null; }
        }
        if (map.containsKey("estimatedhours")) {
            try { feature.estimatedHours = Double.parseDouble(map.get("estimatedhours")); } catch (Exception e) { feature.estimatedHours = null; }
        }

        return feature;
    }

    @Override
    public String serialize(Integer parentId) {
        return 
            "Type=Feature;ID=" + this.id + (parentId != null ? ";ParentID=" + parentId : "") + ";Name=" + this.name + ";Status=" + this.status + 
            ";Description=" + (this.description != null ? this.description : "") + 
            ";EstimatedHours=" + (this.estimatedHours != null ? this.estimatedHours : "");
    }

    @Override
    public void print(Integer depth, boolean printDetails) {
        System.out.println(getPrefixByDepth(depth, '-') + "[" + this.id + "] FEATURE: " + this.name + " (" + this.status.getDisplayName() + ")");
        
        if (printDetails) {
            System.out.println(getPrefixByDepth(depth, ' ') + "Estimated hours: " + (this.estimatedHours != null ? this.estimatedHours : "--"));
            System.out.println(getPrefixByDepth(depth, ' ') + "Description: " + (this.description != null && this.description.length() > 0 ? this.description : "--"));
        }
    }
}
