package manager;

import manager.InMemoryTaskManager;
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

        Subtask subtask = new Subtask("Subtask", "Description", 1);
        assertThrows(IllegalArgumentException.class, () -> {
            subtask.setEpicId(subtask.getId());
        }, "Подзадача не может быть своим же эпиком");
    }

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
        InMemoryTaskManager taskManager = new InMemoryTaskManager();

        //  Проверяем, что задачи с заданным id и сгенерированным id не конфликтуют внутри менеджера
        Task taskWithGivenId = new Task("Task1", "Description1", 10);
        taskManager.createTask(taskWithGivenId);

        Task taskWithGeneratedId = new Task("Task2", "Description2");
        int generatedId = taskManager.createTask(taskWithGeneratedId);

        assertNotEquals(10, generatedId, "ID сгенерированной задачи не должен совпадать с заданным.");
        assertNotNull(taskManager.getTask(10), "Задача с заданным ID должна существовать.");
        assertNotNull(taskManager.getTask(generatedId), "Задача со сгенерированным ID должна существовать.");
    }

    @Test
    void taskImmutabilityAfterAddingToManager() {
        InMemoryTaskManager taskManager = new InMemoryTaskManager();

        //  Проверяем неизменность задачи при добавлении задачи в менеджер
        Task task = new Task("Task", "Description");
        int taskId = taskManager.createTask(task);
        Task retrievedTask = taskManager.getTask(taskId);

        retrievedTask.setName("New Name");
        retrievedTask.setDescription("New Description");

        Task originalTask = taskManager.getTask(taskId);
        assertEquals("Task", originalTask.getName(), "Имя исходной задачи не должно измениться.");
        assertEquals("Description", originalTask.getDescription(), "Описание исходной задачи не должно измениться.");
    }

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
        assertEquals(task, tasks.get(0), "Задачи не совпадают.");
    }
}