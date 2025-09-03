package test.entities;

import enums.WorkItemStatus;
import org.junit.Test;

import entities.Epic;
import entities.Feature;

import static org.junit.Assert.*;

import java.time.LocalDate;

public class FeatureTest {
    @Test
    public void testFeatureBuilderRequiredFields() {
        Feature feature = Feature.builder()
                .setName("Feature 1")
                .setStatus(WorkItemStatus.NOT_STARTED)
                .build();
        assertEquals("Feature 1", feature.getName());
        assertEquals(WorkItemStatus.NOT_STARTED, feature.getStatus());
    }

    @Test
    public void testFeatureBuilderOptionalFields() {
        Feature feature = Feature.builder()
                .setName("Feature 2")
                .setStatus(WorkItemStatus.IN_PROGRESS)
                .setDescription("Feature description")
                .setEstimatedHours(12.5)
                .build();
        assertEquals("Feature description", feature.getDescription());
        assertEquals(12.5, feature.getEstimatedHours(), 0.0001);
    }

    @Test
    public void testFeatureBuilderOptionalFieldsBlank() {
        Feature feature = Feature.builder()
                .setName("Feature 3")
                .setStatus(WorkItemStatus.COMPLETED)
                .setDescription(null)
                .setEstimatedHours(null)
                .build();
        assertNull(feature.getDescription());
        assertNull(feature.getEstimatedHours());
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
        assertEquals(99, epic.getID(), 0.1);
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
        assertEquals(100, epic.getID(), 0.1);
        assertEquals("Bad Dates", epic.getName());
        assertEquals(WorkItemStatus.IN_PROGRESS, epic.getStatus());
        assertNull(epic.getStartDate());
    assertNull(epic.getEndDate());
}
}