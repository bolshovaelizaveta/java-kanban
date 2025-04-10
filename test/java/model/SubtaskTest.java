package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SubtaskTest {
    @Test
    void equals_shouldReturnTrue_whenSubtasksHaveSameId() {
        // Проверяем, что объекты Subtask считаются равными, если у них одинаковый ID
        Subtask subtask1 = new Subtask("Subtask1", "Description1", 1, 1);
        Subtask subtask2 = new Subtask("Subtask2", "Description2", 1, 2);

        assertEquals(subtask1, subtask2, "Подзадачи с одинаковым ID должны быть равны.");
    }

    @Test
    void equals_shouldReturnFalse_whenSubtasksHaveDifferentIds() {
        // Проверяем, что объекты Subtask считаются неравными, если у них разные ID
        Subtask subtask1 = new Subtask("Subtask1", "Description1", 1, 1);
        Subtask subtask2 = new Subtask("Subtask2", "Description2", 2, 2);

        assertNotEquals(subtask1, subtask2, "Подзадачи с разными ID не должны быть равны.");
    }

    @Test
    void subtaskCannotBeEpicOfItself() {
        // Проверяет, что Subtask нельзя сделать своим же эпиком
        Subtask subtask = new Subtask("Test Subtask", "Test Description",1, 1);
        assertNotEquals(subtask.getEpicId(), subtask.getId(), "Подзадача не должна быть эпиком для самой себя");
    }
}