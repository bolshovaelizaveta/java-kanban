package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EpicTest {

    @Test
    void equals_shouldReturnTrue_whenEpicsHaveSameId() {
        Epic epic1 = new Epic("Epic1", "Description1", 1, TaskStatus.NEW);
        Epic epic2 = new Epic("Epic2", "Description2", 1, TaskStatus.NEW);

        assertEquals(epic1, epic2, "Эпики с одинаковым ID должны быть равны.");
    }

    @Test
    void equals_shouldReturnFalse_whenEpicsHaveDifferentId() {
        Epic epic1 = new Epic("Epic1", "Description1", 1, TaskStatus.NEW);
        Epic epic2 = new Epic("Epic2", "Description2", 2, TaskStatus.NEW);

        assertNotEquals(epic1, epic2, "Эпики с разными ID не должны быть равны.");
    }

}