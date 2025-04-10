package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SubtaskTest {
    @Test
    void equals_shouldReturnTrue_whenSubtasksHaveSameId() {
        Subtask subtask1 = new Subtask("Subtask1", "Description1", 1, 1);
        Subtask subtask2 = new Subtask("Subtask2", "Description2", 1, 1);

        assertEquals(subtask1, subtask2, "Подзадачи с одинаковым ID должны быть равны.");
    }

    @Test
    void equals_shouldReturnFalse_whenSubtasksHaveDifferentIds() {
        Subtask subtask1 = new Subtask("Subtask1", "Description1", 1, 1);
        Subtask subtask2 = new Subtask("Subtask2", "Description2", 2, 2);

        assertNotEquals(subtask1, subtask2, "Подзадачи с разными ID не должны быть равны.");
    }

    @Test
    void subtaskCannotBeEpicOfItself() {
        Epic epic = new Epic("Test Epic", "Test Description", 1);
        Subtask subtask = new Subtask("Test Subtask", "Test Description", epic.getId(), 2);

        assertNotEquals(subtask.getEpicId(), subtask.getId(), "Подзадача не должна быть эпиком для самой себя");
    }
}