package manager;

import model.Task;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

public class InMemoryHistoryManagerTest {

    @Test
    void historyManagerPreservesTaskVersion() {
        //  Проверяем, что задачи в HistoryManager, сохраняют предыдущую версию задачи
        InMemoryTaskManager taskManager = new InMemoryTaskManager(); //  Передаем historyManager в taskManager
        // И убрала лишнее добавление напрямую в historyManager
        Task task = new Task("Task", "Description");
        int taskId = taskManager.createTask(task);

        // Получаем задачу через taskManager, чтобы она попала в историю
        taskManager.getTask(taskId);
        Task modifiedTask = new Task(task.getName(), task.getDescription());
        modifiedTask.setName("New Name");
        modifiedTask.setDescription("New Description");

        taskManager.updateTask(modifiedTask); // Обновляем задачу через taskManager
        taskManager.getTask(taskId);

        List<Task> history = taskManager.getHistory();
        assertEquals(2, history.size(), "История должна содержать 2 задачи.");
        assertEquals("Task", history.get(0).getName(), "Имя первой задачи должно быть оригинальным.");
        assertEquals("New Name", history.get(1).getName(), "Имя второй задачи должно быть новым.");
    }

    @Test
    void add() {
        //Тест добавления в историю
        InMemoryTaskManager taskManager = new InMemoryTaskManager();
        Task task = new Task("Test Task", "Test Description");
        int taskId = taskManager.createTask(task);

        taskManager.getTask(taskId); // Добавляем задачу в историю через getTask()

        List<Task> history = taskManager.getHistory();
        assertNotNull(history, "История не пустая.");
        assertEquals(1, history.size(), "История должна содержать одну задачу.");
        assertEquals(task, history.getFirst(), "Задача в истории должна совпадать с добавленной.");
    }
}
