package enums;

public enum WorkItemType {
    EPIC("Epic"),
    PHASE("Phase"),
    FEATURE("Feature"),
    MILESTONE("Milestone"),
    TASK("Task");

    private final String displayName;

    WorkItemType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
