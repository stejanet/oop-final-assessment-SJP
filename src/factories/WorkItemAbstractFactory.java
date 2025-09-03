package factories;

import java.time.LocalDate;

import entities.*;
import enums.WorkItemStatus;

public interface WorkItemAbstractFactory {
    Epic createEpic();
    Phase createPhase();
    Feature createFeature();
    Milestone createMilestone();
    Task createTask();
    
    WorkItemStatus getDefaultWorkItemStatus();
    LocalDate getDefaultStartDate();
}