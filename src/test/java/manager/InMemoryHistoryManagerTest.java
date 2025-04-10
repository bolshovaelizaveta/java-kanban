package manager;

import model.Task;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

public class InMemoryHistoryManagerTest {

    @Test
    void add_shouldAddTasksToHistory() {
        HistoryManager historyManager = new InMemoryHistoryManager();
        Task task1 = new Task("Task 1", "Description 1", 1);
        Task task2 = new Task("Task 2", "Description 2", 2);

        historyManager.add(task1);
        historyManager.add(task2);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "История должна содержать 2 задачи.");
        assertEquals(task1, history.get(0), "Первая задача в истории должна быть task1.");
        assertEquals(task2, history.get(1), "Вторая задача в истории должна быть task2.");
    }

    @Test
    void add_shouldNotAddDuplicates_onlyKeepLatest() {
        HistoryManager historyManager = new InMemoryHistoryManager();
        Task task1 = new Task("Task 1", "Description 1", 1);
        Task task2 = new Task("Task 2", "Description 2", 2);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task1);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "История должна содержать 2 задачи.");
        assertEquals(task2, history.get(0), "Первая задача в истории должна быть task2.");
        assertEquals(task1, history.get(1), "Вторая задача в истории должна быть task1 (последний просмотр).");
    }

    @Test
    void remove_shouldRemoveTaskFromHistory() {
        HistoryManager historyManager = new InMemoryHistoryManager();
        Task task1 = new Task("Task 1", "Description 1", 1);
        Task task2 = new Task("Task 2", "Description 2", 2);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.remove(1);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "История должна содержать 1 задачу после удаления.");
        assertEquals(task2, history.get(0), "Единственная задача в истории должна быть task2.");
    }

    @Test
    void remove_shouldHandleRemovingNonExistingTask() {
        HistoryManager historyManager = new InMemoryHistoryManager();
        Task task1 = new Task("Task 1", "Description 1", 1);
        historyManager.add(task1);
        historyManager.remove(2); // Removing a non-existing task

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "История должна содержать 1 задачу.");
        assertEquals(task1, history.get(0), "Задача в истории должна быть task1.");
    }

    @Test
    void getHistory_shouldReturnEmptyListWhenHistoryIsEmpty() {
        HistoryManager historyManager = new InMemoryHistoryManager();
        List<Task> history = historyManager.getHistory();
        assertTrue(history.isEmpty(), "История должна быть пустой.");
    }
}