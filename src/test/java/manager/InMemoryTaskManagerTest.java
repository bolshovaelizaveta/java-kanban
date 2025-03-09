package manager;

import model.Epic;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskManagerTest {

    @Test
    void canCreateAndRetrieveTasks() {
        InMemoryTaskManager taskManager = new InMemoryTaskManager();

        Task task = new Task("Task", "Description");
        int taskId = taskManager.createTask(task);
        Task retrievedTask = taskManager.getTask(taskId);
        assertEquals(task, retrievedTask, "Задача должна быть успешно создана и получена.");
    }

    @Test
    void subtaskCannotHaveItselfAsEpic() {
        InMemoryTaskManager taskManager = new InMemoryTaskManager();
        Subtask subtask = new Subtask("Подзадача", "Описание", 10);

        taskManager.createSubtask(subtask);
        assertNull(taskManager.getSubtask(10), "Подзадача не должна быть сохранена, если Epic не существует.");
    } // Исправила: подзадача не сохранится, если epicId не был найден

    @Test
    void inMemoryTaskManager_addDifferentTaskTypesAndFindById() {
        InMemoryTaskManager taskManager = new InMemoryTaskManager();

        //  Проверяем, что InMemoryTaskManager добавляет задачи разного типа и находит их по id
        Task task = new Task("Test Task", "Test Description");
        int taskId = taskManager.createTask(task);
        Epic epic = new Epic("Test Epic", "Test Description");
        int epicId = taskManager.createEpic(epic);
        Subtask subtask = new Subtask("Test Subtask", "Test Description", epicId);
        int subtaskId = taskManager.createSubtask(subtask);
        Task foundTask = taskManager.getTask(taskId);
        Epic foundEpic = taskManager.getEpic(epicId);
        Subtask foundSubtask = taskManager.getSubtask(subtaskId);
        assertEquals(task, foundTask, "Задача не найдена по ID.");
        assertEquals(epic, foundEpic, "Эпик не найден по ID.");
        assertEquals(subtask, foundSubtask, "Подзадача не найдена по ID.");
    }

    @Test
    void tasksWithGivenIdAndGeneratedIdDoNotConflict() {
        //  Проверяем, что задачи с заданным id и сгенерированным id не конфликтуют внутри менеджера
        InMemoryTaskManager taskManager = new InMemoryTaskManager();
        Task task1 = new Task("Задача 1", "Описание 1");
        Task task2 = new Task("Задача 2", "Описание 2");
        int id1 = taskManager.createTask(task1);
        int id2 = taskManager.createTask(task2);

        taskManager.getTask(id1);
        taskManager.getTask(id2);
        assertNotNull(taskManager.getTask(id1), "Задача с заданным ID должна существовать.");
        assertNotNull(taskManager.getTask(id2), "Задача с заданным ID должна существовать.");

        assertEquals(2, taskManager.getHistory().size(), "В истории должно быть 2 задачи.");
    } // Вроде исправила

    // Тогда удаляю этот тест с:

    @Test
    void addNewTask() {
        InMemoryTaskManager taskManager = new InMemoryTaskManager();

        //Проверяем добавление новой задачи
        Task task = new Task("Test addNewTask", "Test addNewTask description");
        final int taskId = taskManager.createTask(task);
        final Task savedTask = taskManager.getTask(taskId);
        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");
        final List<Task> tasks = taskManager.getTasks();
        assertNotNull(tasks, "Задачи не возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(task, tasks.getFirst(), "Задачи не совпадают.");
    }
}