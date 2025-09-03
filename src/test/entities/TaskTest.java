package test.entities;

import enums.WorkItemStatus;
import org.junit.Test;

import entities.Task;

import java.time.LocalDate;

import static org.junit.Assert.*;

public class TaskTest {
    @Test
    public void testTaskBuilderRequiredFields() {
        Task task = Task.builder()
                .setName("Task 1")
                .setStatus(WorkItemStatus.NOT_STARTED)
                .build();
        assertEquals("Task 1", task.getName());
        assertEquals(WorkItemStatus.NOT_STARTED, task.getStatus());
    }

    @Test
    public void testTaskBuilderOptionalFields() {
        LocalDate due = LocalDate.of(2025, 6, 1);
        Task task = Task.builder()
                .setName("Task 2")
                .setStatus(WorkItemStatus.IN_PROGRESS)
                .setDescription("Task description")
                .setDueDate(due)
                .setEstimatedHours(8.0)
                .build();
        assertEquals("Task description", task.getDescription());
        assertEquals(due, task.getDueDate());
        assertEquals(8.0, task.getEstimatedHours(), 0.0001);
    }

    @Test
    public void testTaskBuilderOptionalFieldsBlank() {
        Task task = Task.builder()
                .setName("Task 3")
                .setStatus(WorkItemStatus.COMPLETED)
                .setDescription(null)
                .setDueDate(null)
                .setEstimatedHours(null)
                .build();
        assertNull(task.getDescription());
        assertNull(task.getDueDate());
        assertNull(task.getEstimatedHours());
    }

    @Test
    public void testTaskSerializationAndParsing() {
        Task original = Task.builder()
                .setName("Task Serialization")
                .setStatus(WorkItemStatus.IN_PROGRESS)
                .setDescription("Serialize/Parse test")
                .setDueDate(LocalDate.of(2025, 6, 1))
                .setEstimatedHours(8.0)
                .build();
        original.setID(51);

        String serialized = original.serialize(null);
        Task parsed = Task.parse(serialized);

        assertNotNull(parsed);
        assertEquals(original.getID(), parsed.getID());
        assertEquals(original.getName(), parsed.getName());
        assertEquals(original.getStatus(), parsed.getStatus());
        assertEquals(original.getDescription(), parsed.getDescription());
        assertEquals(original.getDueDate(), parsed.getDueDate());
        assertEquals(original.getEstimatedHours(), parsed.getEstimatedHours(), 0.0001);
    }

    @Test
    public void testTaskParseHandlesMissingOptionalFields() {
        String line = "Type=Task;ID=52;Name=No Optionals;Status=NOT_STARTED";
        Task task = Task.parse(line);

        assertNotNull(task);
        assertEquals(52, task.getID(), 0.1);
        assertEquals("No Optionals", task.getName());
        assertEquals(WorkItemStatus.NOT_STARTED, task.getStatus());
        assertNull(task.getDescription());
        assertNull(task.getDueDate());
        assertNull(task.getEstimatedHours());
    }
}