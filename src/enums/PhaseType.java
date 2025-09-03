package enums;

public enum PhaseType {
    R_AND_D("Research & Development"),
    PROJECT_MANAGEMENT("Project Management"),
    DEVELOPMENT("Development");

    private final String displayName;

    PhaseType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}