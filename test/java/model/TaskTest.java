package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TaskTest {

    @Test
    void equals_shouldReturnTrue_whenTasksHaveSameId() {
        Task task1 = new Task("Task1", "Description1", 1, TaskStatus.NEW);
        Task task2 = new Task("Task2", "Description2", 1, TaskStatus.NEW);

        assertEquals(task1, task2, "Задачи с одинаковым ID должны быть равны.");
    }

    @Test
    void equals_shouldReturnFalse_whenTasksHaveDifferentId() {
        Task task1 = new Task("Task1", "Description1", 1, TaskStatus.NEW);
        Task task2 = new Task("Task2", "Description2", 2, TaskStatus.NEW);

        assertNotEquals(task1, task2, "Задачи с разными ID не должны быть равны.");
    }
}