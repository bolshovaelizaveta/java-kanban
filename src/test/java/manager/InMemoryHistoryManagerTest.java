package manager;

import manager.HistoryManager;
import manager.InMemoryTaskManager;
import manager.Managers;
import model.Task;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class InMemoryHistoryManagerTest {

    @Test
    void historyManagerPreservesTaskVersion() {
        //  Проверяем, что задачи в HistoryManager, сохраняют предыдущую версию задачи
        HistoryManager historyManager = Managers.getDefaultHistory();
        InMemoryTaskManager taskManager = new InMemoryTaskManager();
        Task task = new Task("Task", "Description");
        int taskId = taskManager.createTask(task);
        historyManager.add(taskManager.getTask(taskId));

        Task modifiedTask = taskManager.getTask(taskId);
        modifiedTask.setName("New Name");
        modifiedTask.setDescription("New Description");
        historyManager.add(modifiedTask);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "История должна содержать 2 задачи.");
        assertEquals("Task", history.get(0).getName(), "Имя первой задачи должно быть оригинальным.");
        assertEquals("New Name", history.get(1).getName(), "Имя второй задачи должно быть новым.");
    }

    @Test
    void add() {
        //Тест добавления в историю
        HistoryManager historyManager = Managers.getDefaultHistory();
        Task task = new Task("Test Task", "Test Description");
        historyManager.add(task);
        final List<Task> history = historyManager.getHistory();
        assertNotNull(history, "История не пустая.");
        assertEquals(1, history.size(), "История не пустая.");
    }
}
