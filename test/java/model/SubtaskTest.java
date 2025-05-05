package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SubtaskTest {

    @Test
    void equals_shouldReturnTrue_whenSubtasksHaveSameId() {
        Subtask subtask1 = new Subtask("Subtask1", "Description1", 1, TaskStatus.NEW, 10);
        Subtask subtask2 = new Subtask("Subtask2", "Description2", 1, TaskStatus.NEW, 20);

        assertEquals(subtask1, subtask2, "Подзадачи с одинаковым ID должны быть равны.");
    }

    @Test
    void equals_shouldReturnFalse_whenSubtasksHaveDifferentId() {
        Subtask subtask1 = new Subtask("Subtask1", "Description1", 1, TaskStatus.NEW, 10);
        Subtask subtask2 = new Subtask("Subtask2", "Description2", 2, TaskStatus.NEW, 10);

        assertNotEquals(subtask1, subtask2, "Подзадачи с разными ID не должны быть равны.");
    }

}