package test.entities;

import enums.WorkItemStatus;
import enums.PhaseType;
import org.junit.Test;

import entities.Phase;

import java.time.LocalDate;

import static org.junit.Assert.*;

public class PhaseTest {
    @Test
    public void testPhaseBuilderRequiredFields() {
        Phase phase = Phase.builder()
                .setName("Phase 1")
                .setStatus(WorkItemStatus.NOT_STARTED)
                .setPhaseType(PhaseType.DEVELOPMENT)
                .build();
        assertEquals("Phase 1", phase.getName());
        assertEquals(WorkItemStatus.NOT_STARTED, phase.getStatus());
        assertEquals(PhaseType.DEVELOPMENT, phase.getPhaseType());
    }

    @Test
    public void testPhaseBuilderOptionalFields() {
        LocalDate start = LocalDate.of(2025, 3, 1);
        LocalDate end = LocalDate.of(2025, 4, 1);
        Phase phase = Phase.builder()
                .setName("Phase 2")
                .setStatus(WorkItemStatus.IN_PROGRESS)
                .setPhaseType(PhaseType.R_AND_D)
                .setStartDate(start)
                .setEndDate(end)
                .build();
        assertEquals(start, phase.getStartDate());
        assertEquals(end, phase.getEndDate());
    }

    @Test
    public void testPhaseBuilderOptionalFieldsBlank() {
        Phase phase = Phase.builder()
                .setName("Phase 3")
                .setStatus(WorkItemStatus.COMPLETED)
                .setPhaseType(PhaseType.PROJECT_MANAGEMENT)
                .setStartDate(null)
                .setEndDate(null)
                .build();
        assertNull(phase.getStartDate());
        assertNull(phase.getEndDate());
    }

    @Test
    public void testPhaseSerializationAndParsing() {
        Phase original = Phase.builder()
                .setName("Phase Serialization")
                .setStatus(WorkItemStatus.IN_PROGRESS)
                .setPhaseType(PhaseType.DEVELOPMENT)
                .setStartDate(LocalDate.of(2025, 3, 1))
                .setEndDate(LocalDate.of(2025, 4, 1))
                .build();
        original.setID(31);

        String serialized = original.serialize(null);
        Phase parsed = Phase.parse(serialized);

        assertNotNull(parsed);
        assertEquals(original.getID(), parsed.getID());
        assertEquals(original.getName(), parsed.getName());
        assertEquals(original.getStatus(), parsed.getStatus());
        assertEquals(original.getPhaseType(), parsed.getPhaseType());
        assertEquals(original.getStartDate(), parsed.getStartDate());
        assertEquals(original.getEndDate(), parsed.getEndDate());
    }

    @Test
    public void testPhaseParseHandlesMissingOptionalFields() {
        String line = "Type=Phase;ID=32;Name=No Optionals;Status=NOT_STARTED;PhaseType=DEVELOPMENT";
        Phase phase = Phase.parse(line);

        assertNotNull(phase);
        assertEquals(32, phase.getID(), 0.1);
        assertEquals("No Optionals", phase.getName());
        assertEquals(WorkItemStatus.NOT_STARTED, phase.getStatus());
        assertEquals(PhaseType.DEVELOPMENT, phase.getPhaseType());
        assertNull(phase.getStartDate());
        assertNull(phase.getEndDate());
    }
}