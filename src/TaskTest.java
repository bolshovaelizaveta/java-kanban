import model.Epic;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @Test
    void equals_shouldReturnTrue_whenTasksHaveSameId() {
        // Проверяем, что объекты Task считаются равными, если у них одинаковый ID
        Task task1 = new Task("Task1", "Description1", 1);
        Task task2 = new Task("Task2", "Description2", 1);

        assertEquals(task1, task2, "Tasks with the same ID should be equal.");
    }

    @Test
    void equals_shouldReturnFalse_whenTasksHaveDifferentIds() {
        // Проверяем, что объекты Task считаются неравными, если у них разные ID
        Task task1 = new Task("Task1", "Description1", 1);
        Task task2 = new Task("Task2", "Description2", 2);

        assertNotEquals(task1, task2, "Tasks with different IDs should not be equal.");
    }
}

class SubtaskTest {
    @Test
    void equals_shouldReturnTrue_whenSubtasksHaveSameId() {
        // Проверяем, что объекты Subtask считаются равными, если у них одинаковый ID
        Subtask subtask1 = new Subtask("Subtask1", "Description1", 1, 1);
        Subtask subtask2 = new Subtask("Subtask2", "Description2", 1, 2);

        assertEquals(subtask1, subtask2, "Subtasks with the same ID should be equal.");
    }

    @Test
    void equals_shouldReturnFalse_whenSubtasksHaveDifferentIds() {
        // Проверяем, что объекты Subtask считаются неравными, если у них разные ID
        Subtask subtask1 = new Subtask("Subtask1", "Description1", 1, 1);
        Subtask subtask2 = new Subtask("Subtask2", "Description2", 2, 2);

        assertNotEquals(subtask1, subtask2, "Subtasks with different IDs should not be equal.");
    }
}

class EpicTest {
    @Test
    void equals_shouldReturnTrue_whenEpicsHaveSameId() {
        // Проверяем, что объекты Epic считаются равными, если у них одинаковый ID
        Epic epic1 = new Epic("Epic1", "Description1", 1);
        Epic epic2 = new Epic("Epic2", "Description2", 1);

        assertEquals(epic1, epic2, "Epics with the same ID should be equal.");
    }

    @Test
    void equals_shouldReturnFalse_whenEpicsHaveDifferentIds() {
        // Проверяем, что объекты Epic считаются неравными, если у них разные ID
        Epic epic1 = new Epic("Epic1", "Description1", 1);
        Epic epic2 = new Epic("Epic2", "Description2", 2);

        assertNotEquals(epic1, epic2, "Epics with different IDs should not be equal.");
    }
}
