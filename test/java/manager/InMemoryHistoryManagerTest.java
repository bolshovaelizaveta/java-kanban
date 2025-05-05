package manager;

import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class InMemoryHistoryManagerTest implements HistoryManagerTest<InMemoryHistoryManager> {

    @Override
    public InMemoryHistoryManager createHistoryManager() {
        return new InMemoryHistoryManager();
    }


    @Test
    public void add_shouldAddTasksToHistory() {
        HistoryManager historyManager = createHistoryManager();
        Task task1 = new Task("Task 1", "Description 1", 1, TaskStatus.NEW);
        Task task2 = new Task("Task 2", "Description 2", 2, TaskStatus.NEW);

        historyManager.add(task1);
        historyManager.add(task2);

        List<Task> history = historyManager.getHistory();
        assertNotNull(history);
        assertEquals(2, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task2, history.get(1));
    }

    @Test
    public void add_shouldNotAddDuplicates_onlyKeepLatest() {
        HistoryManager historyManager = createHistoryManager();
        Task task1 = new Task("Task 1", "Description 1", 1, TaskStatus.NEW);
        Task task2 = new Task("Task 2", "Description 2", 2, TaskStatus.NEW);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task1);

        List<Task> history = historyManager.getHistory();
        assertNotNull(history);
        assertEquals(2, history.size());
        assertEquals(task2, history.get(0));
        assertEquals(task1, history.get(1));
    }

    @Test
    public void remove_shouldRemoveTaskFromHistory() {
        HistoryManager historyManager = createHistoryManager();
        Task task1 = new Task("Task 1", "Description 1", 1, TaskStatus.NEW);
        Task task2 = new Task("Task 2", "Description 2", 2, TaskStatus.NEW);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.remove(1);

        List<Task> history = historyManager.getHistory();
        assertNotNull(history);
        assertEquals(1, history.size());
        assertEquals(task2, history.get(0));
    }

    @Test
    public void remove_shouldHandleRemovingNonExistingTask() {
        HistoryManager historyManager = createHistoryManager();
        Task task1 = new Task("Task 1", "Description 1", 1, TaskStatus.NEW);
        historyManager.add(task1);
        historyManager.remove(2);

        List<Task> history = historyManager.getHistory();
        assertNotNull(history);
        assertEquals(1, history.size());
        assertEquals(task1, history.get(0));
    }

    @Test
    public void getHistory_shouldReturnEmptyListWhenHistoryIsEmpty() {
        HistoryManager historyManager = createHistoryManager();
        List<Task> history = historyManager.getHistory();
        assertNotNull(history);
        assertTrue(history.isEmpty());
    }

    @Test
    public void remove_shouldRemoveFromBeginning() {
        HistoryManager historyManager = createHistoryManager();
        Task task1 = new Task("Task 1", "Desc 1", 1, TaskStatus.NEW);
        Task task2 = new Task("Task 2", "Desc 2", 2, TaskStatus.NEW);
        Task task3 = new Task("Task 3", "Desc 3", 3, TaskStatus.NEW);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(1);

        List<Task> history = historyManager.getHistory();
        assertNotNull(history);
        assertEquals(2, history.size());
        assertEquals(task2, history.get(0));
        assertEquals(task3, history.get(1));
    }

    @Test
    public void remove_shouldRemoveFromMiddle() {
        HistoryManager historyManager = createHistoryManager();
        Task task1 = new Task("Task 1", "Desc 1", 1, TaskStatus.NEW);
        Task task2 = new Task("Task 2", "Desc 2", 2, TaskStatus.NEW);
        Task task3 = new Task("Task 3", "Desc 3", 3, TaskStatus.NEW);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(2);

        List<Task> history = historyManager.getHistory();
        assertNotNull(history);
        assertEquals(2, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task3, history.get(1));
    }

    @Test
    public void remove_shouldRemoveFromEnd() {
        HistoryManager historyManager = createHistoryManager();
        Task task1 = new Task("Task 1", "Desc 1", 1, TaskStatus.NEW);
        Task task2 = new Task("Task 2", "Desc 2", 2, TaskStatus.NEW);
        Task task3 = new Task("Task 3", "Desc 3", 3, TaskStatus.NEW);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(3);

        List<Task> history = historyManager.getHistory();
        assertNotNull(history);
        assertEquals(2, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task2, history.get(1));
    }

    @Test
    public void removeAll_shouldClearHistory() {
        HistoryManager historyManager = createHistoryManager();
        Task task1 = new Task("Task 1", "Desc 1", 1, TaskStatus.NEW);
        Task task2 = new Task("Task 2", "Desc 2", 2, TaskStatus.NEW);

        historyManager.add(task1);
        historyManager.add(task2);

        historyManager.removeAll();

        List<Task> history = historyManager.getHistory();
        assertNotNull(history);
        assertTrue(history.isEmpty());
    }
}