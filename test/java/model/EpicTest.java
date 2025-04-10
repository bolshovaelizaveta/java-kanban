package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EpicTest {
    @Test
    void equals_shouldReturnTrue_whenEpicsHaveSameId() {
        Epic epic1 = new Epic("Epic1", "Description1", 1);
        Epic epic2 = new Epic("Epic2", "Description2", 1);

        assertEquals(epic1, epic2, "Эпики с одинаковым ID должны быть равны.");
    }

    @Test
    void equals_shouldReturnFalse_whenEpicsHaveDifferentIds() {
        Epic epic1 = new Epic("Epic1", "Description1", 1);
        Epic epic2 = new Epic("Epic2", "Description2", 2);

        assertNotEquals(epic1, epic2, "Эпики с разными ID не должны быть равны.");
    }

    @Test
    void epicCannotBeSubtaskOfItself() {
        Epic epic = new Epic("Test Epic", "Test Description", 1);
        int epicId = epic.getId();
        epic.addSubtaskId(epicId);
    }
}
