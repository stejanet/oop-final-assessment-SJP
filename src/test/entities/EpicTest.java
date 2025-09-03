package test.entities;

import enums.WorkItemStatus;
import org.junit.Test;

import entities.Epic;

import java.time.LocalDate;

import static org.junit.Assert.*;

public class EpicTest {
    @Test
    public void testEpicBuilderRequiredFields() {
        Epic epic = Epic.builder()
                .setName("Epic 1")
                .setStatus(WorkItemStatus.NOT_STARTED)
                .build();
        assertEquals("Epic 1", epic.getName());
        assertEquals(WorkItemStatus.NOT_STARTED, epic.getStatus());
    }

    @Test
    public void testEpicBuilderOptionalFields() {
        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 2, 1);
        Epic epic = Epic.builder()
                .setName("Epic 2")
                .setStatus(WorkItemStatus.IN_PROGRESS)
                .setDescription("Optional description")
                .setStartDate(start)
                .setEndDate(end)
                .build();
        assertEquals("Optional description", epic.getDescription());
        assertEquals(start, epic.getStartDate());
        assertEquals(end, epic.getEndDate());
    }

    @Test
    public void testEpicBuilderOptionalFieldsBlank() {
        Epic epic = Epic.builder()
                .setName("Epic 3")
                .setStatus(WorkItemStatus.COMPLETED)
                .setDescription(null)
                .setStartDate(null)
                .setEndDate(null)
                .build();
        assertNull(epic.getDescription());
        assertNull(epic.getStartDate());
        assertNull(epic.getEndDate());
    }

    @Test
    public void testEpicSerializationAndParsing() {
        Epic original = Epic.builder()
                .setName("Epic Serialization")
                .setStatus(WorkItemStatus.IN_PROGRESS)
                .setDescription("Serialize/Parse test")
                .setStartDate(LocalDate.of(2025, 7, 1))
                .setEndDate(LocalDate.of(2025, 7, 31))
                .build();
        original.setID(42);

        // Serialize
        String serialized = original.serialize(null);

        // Parse back
        Epic parsed = Epic.parse(serialized);

        assertNotNull(parsed);
        assertEquals(original.getID(), parsed.getID());
        assertEquals(original.getName(), parsed.getName());
        assertEquals(original.getStatus(), parsed.getStatus());
        assertEquals(original.getDescription(), parsed.getDescription());
        assertEquals(original.getStartDate(), parsed.getStartDate());
        assertEquals(original.getEndDate(), parsed.getEndDate());
    }

    @Test
    public void testEpicParseHandlesMissingOptionalFields() {
        String line = "Type=Epic;ID=99;Name=No Optionals;Status=NOT_STARTED";
        Epic epic = Epic.parse(line);

        assertNotNull(epic);
        assertEquals(99, epic.getID(),0.1);
        assertEquals("No Optionals", epic.getName());
        assertEquals(WorkItemStatus.NOT_STARTED, epic.getStatus());
        assertNull(epic.getDescription());
        assertNull(epic.getStartDate());
        assertNull(epic.getEndDate());
    }

    @Test
    public void testEpicParseHandlesInvalidDatesGracefully() {
        String line = "Type=Epic;ID=100;Name=Bad Dates;Status=IN_PROGRESS;StartDate=notadate;EndDate=alsonotadate";
        Epic epic = Epic.parse(line);

        assertNotNull(epic);
        assertEquals(100, epic.getID(),0.1);
        assertEquals("Bad Dates", epic.getName());
        assertEquals(WorkItemStatus.IN_PROGRESS, epic.getStatus());
        assertNull(epic.getStartDate());
        assertNull(epic.getEndDate());
    }
}