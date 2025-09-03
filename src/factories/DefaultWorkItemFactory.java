package factories;

import java.time.LocalDate;

import entities.*;
import enums.WorkItemStatus;

public class DefaultWorkItemFactory implements WorkItemAbstractFactory {
    public Epic createEpic() {
        Epic epic = new Epic();
        epic.setStatus(getDefaultWorkItemStatus());
        epic.setStartDate(getDefaultStartDate());
        return epic;
    }
    public Phase createPhase() {
        Phase phase = new Phase();
        phase.setStatus(getDefaultWorkItemStatus());
        phase.setStartDate(getDefaultStartDate());
        return phase;
    }
    public Feature createFeature() {
        Feature feature = new Feature();
        feature.setStatus(getDefaultWorkItemStatus());
        return feature;
    }
    public Milestone createMilestone() {
        Milestone milestone = new Milestone();
        milestone.setStatus(getDefaultWorkItemStatus());
        return milestone;
    }
    public Task createTask() {
        Task task = new Task();
        task.setStatus(getDefaultWorkItemStatus());
        return task;
    }
    
    public WorkItemStatus getDefaultWorkItemStatus() {
        return WorkItemStatus.NOT_STARTED;
    }
    
    public LocalDate getDefaultStartDate() {
        return null;
    }
}