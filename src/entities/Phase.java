package entities;

import java.time.LocalDate;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import enums.PhaseType;
import enums.WorkItemStatus;
import utils.ParseUtils;

public class Phase extends WorkItem {
    private static final Logger logger = Logger.getLogger(Phase.class.getName());

    private PhaseType phaseType;
    private LocalDate startDate;
    private LocalDate endDate;
    
    public PhaseType getPhaseType() { return phaseType; }
    public void setPhaseType(PhaseType phaseType) { this.phaseType = phaseType; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public static class Builder implements WorkItem.Builder<Phase> {
        private String name;
        private WorkItemStatus status = WorkItemStatus.NOT_STARTED;
        private PhaseType phaseType;
        private LocalDate startDate;
        private LocalDate endDate;

        public Builder setName(String name) { this.name = name; return this; }
        public Builder setStatus(WorkItemStatus status) { this.status = status; return this; }
        public Builder setPhaseType(PhaseType phaseType) { this.phaseType = phaseType; return this; }
        public Builder setStartDate(LocalDate startDate) { this.startDate = startDate; return this; }
        public Builder setEndDate(LocalDate endDate) { this.endDate = endDate; return this; }

        @Override
        public Phase build() {
            Phase phase = new Phase();
            phase.setName(name);
            phase.setStatus(status);
            phase.setPhaseType(phaseType);
            phase.setStartDate(startDate);
            phase.setEndDate(endDate);
            return phase;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Phase parse(String line) {
        Phase phase = new Phase();
        Map<String, String> map;

        try {
            map = ParseUtils.parseKeyValuePairs(line);
            if (map.containsKey("id")) phase.id = Integer.parseInt(map.get("id"));
            if (map.containsKey("name")) phase.name = map.get("name");
            if (map.containsKey("status")) phase.status = WorkItemStatus.valueOf(map.get("status").toUpperCase());
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to parse Phase from line: " + line, e);
            return null;
        }

        if (map.containsKey("phasetype")) {
            try { phase.phaseType = PhaseType.valueOf(map.get("phasetype").toUpperCase()); } catch (Exception e) { phase.phaseType = null; }
        }
        if (map.containsKey("startdate")) {
            try { phase.startDate = LocalDate.parse(map.get("startdate")); } catch (Exception e) { phase.startDate = null; }
        }
        if (map.containsKey("enddate")) {
            try { phase.endDate = LocalDate.parse(map.get("enddate")); } catch (Exception e) { phase.endDate = null; }
        }

        return phase;
    }
    
    @Override
    public String serialize(Integer parentId) {
        return 
            "Type=Phase;ID=" + this.id + (parentId != null ? ";ParentID=" + parentId : "") + ";Name=" + this.name + ";Status=" + this.status + 
            ";PhaseType=" + this.phaseType + ";StartDate=" + (this.startDate != null ? this.startDate : "") + 
            ";EndDate=" + (this.endDate != null ? this.endDate : "");
    }

    @Override
    public void print(Integer depth, boolean printDetails) {
        System.out.println(getPrefixByDepth(depth, '-') + "[" + this.id + "] PHASE (" + this.phaseType.getDisplayName() + "): " + this.name + " (" + this.status.getDisplayName() + ")");

        if (printDetails) {
            System.out.println(getPrefixByDepth(depth, ' ') + "Start date: " + (this.startDate != null ? this.startDate : "--") + ", End date: " + (this.endDate != null ? this.endDate : "--"));
            System.out.println(getPrefixByDepth(depth, ' ') + "Phase Type: " + (this.phaseType != null ? this.phaseType.getDisplayName() : "--"));
        }
    }
}
