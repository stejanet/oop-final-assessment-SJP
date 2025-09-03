package test.entities;

import enums.WorkItemStatus;
import org.junit.Test;

import entities.Milestone;

import java.time.LocalDate;

import static org.junit.Assert.*;

public class MilestoneTest {
    @Test
    public void testMilestoneBuilderRequiredFields() {
        Milestone milestone = Milestone.builder()
                .setName("Milestone 1")
                .setStatus(WorkItemStatus.NOT_STARTED)
                .build();
        assertEquals("Milestone 1", milestone.getName());
        assertEquals(WorkItemStatus.NOT_STARTED, milestone.getStatus());
    }

    @Test
    public void testMilestoneBuilderOptionalFields() {
        LocalDate due = LocalDate.of(2025, 5, 1);
        Milestone milestone = Milestone.builder()
                .setName("Milestone 2")
                .setStatus(WorkItemStatus.IN_PROGRESS)
                .setDueDate(due)
                .build();
        assertEquals(due, milestone.getDueDate());
    }

    @Test
    public void testMilestoneBuilderOptionalFieldsBlank() {
        Milestone milestone = Milestone.builder()
                .setName("Milestone 3")
                .setStatus(WorkItemStatus.COMPLETED)
                .setDueDate(null)
                .build();
        assertNull(milestone.getDueDate());
    }

    @Test
    public void testMilestoneSerializationAndParsing() {
        Milestone original = Milestone.builder()
                .setName("Milestone Serialization")
                .setStatus(WorkItemStatus.IN_PROGRESS)
                .setDueDate(LocalDate.of(2025, 5, 1))
                .build();
        original.setID(41);

        String serialized = original.serialize(null);
        Milestone parsed = Milestone.parse(serialized);

        assertNotNull(parsed);
        assertEquals(original.getID(), parsed.getID());
        assertEquals(original.getName(), parsed.getName());
        assertEquals(original.getStatus(), parsed.getStatus());
        assertEquals(original.getDueDate(), parsed.getDueDate());
    }

    @Test
    public void testMilestoneParseHandlesMissingOptionalFields() {
        String line = "Type=Milestone;ID=42;Name=No Optionals;Status=NOT_STARTED";
        Milestone milestone = Milestone.parse(line);

        assertNotNull(milestone);
        assertEquals(42, milestone.getID(), 0.1);
        assertEquals("No Optionals", milestone.getName());
        assertEquals(WorkItemStatus.NOT_STARTED, milestone.getStatus());
        assertNull(milestone.getDueDate());
    }
}