package manager;

import model.Epic;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskManagerTest {

    protected TaskManager taskManager;

    @BeforeEach
    void setUp() throws IOException {
        taskManager = new InMemoryTaskManager();
    }

    @Test
    void canCreateAndRetrieveTasks() {
        Task task = new Task("Task", "Description");
        int taskId = taskManager.createTask(task);
        Task retrievedTask = taskManager.getTask(taskId);
        assertEquals(task, retrievedTask);
    }

    @Test
    void cannotCreateSubtaskWithoutExistingEpic() {
        Subtask subtask = new Subtask("Подзадача", "Описание", 10);

        int subtaskId = taskManager.createSubtask(subtask);
        assertEquals(-1, subtaskId);
        assertNull(taskManager.getSubtask(10));
    }

    @Test
    void inMemoryTaskManager_addDifferentTaskTypesAndFindById() {

        Task task = new Task("Test Task", "Test Description");
        int taskId = taskManager.createTask(task);
        Epic epic = new Epic("Test Epic", "Test Description");
        int epicId = taskManager.createEpic(epic);
        Subtask subtask = new Subtask("Test Subtask", "Test Description", epicId);
        int subtaskId = taskManager.createSubtask(subtask);
        Task foundTask = taskManager.getTask(taskId);
        Epic foundEpic = taskManager.getEpic(epicId);
        Subtask foundSubtask = taskManager.getSubtask(subtaskId);
        assertEquals(task, foundTask);
        assertEquals(epic, foundEpic);
        assertEquals(subtask, foundSubtask);
    }

    @Test
    void tasksWithGivenIdAndGeneratedIdDoNotConflict() {
        Task task1 = new Task("Задача 1", "Описание 1");
        Task task2 = new Task("Задача 2", "Описание 2");
        int id1 = taskManager.createTask(task1);
        int id2 = taskManager.createTask(task2);

        taskManager.getTask(id1);
        taskManager.getTask(id2);
        assertNotNull(taskManager.getTask(id1));
        assertNotNull(taskManager.getTask(id2));

        assertEquals(2, taskManager.getHistory().size());
    }

    @Test
    void addNewTask() {

        Task task = new Task("Test addNewTask", "Test addNewTask description");
        final int taskId = taskManager.createTask(task);
        final Task savedTask = taskManager.getTask(taskId);
        assertNotNull(savedTask);
        assertEquals(task, savedTask);
        final List<Task> tasks = taskManager.getTasks();
        assertNotNull(tasks);
        assertEquals(1, tasks.size());
        assertEquals(task, tasks.getFirst());
    }

    @Test
    void deleteTask_shouldRemoveTaskFromHistory() {
        Task task = new Task("Test Task", "Test Description");
        int taskId = taskManager.createTask(task);
        taskManager.getTask(taskId);

        taskManager.deleteTask(taskId);
        assertTrue(taskManager.getHistory().isEmpty());
    }

    @Test
    void deleteEpic_shouldRemoveEpicAndSubtasksFromHistory() {
        Epic epic = new Epic("Test Epic", "Test Description");
        int epicId = taskManager.createEpic(epic);
        Subtask subtask1 = new Subtask("Subtask 1", "Description", epicId);
        int subtaskId1 = taskManager.createSubtask(subtask1);
        Subtask subtask2 = new Subtask("Subtask 2", "Description", epicId);
        int subtaskId2 = taskManager.createSubtask(subtask2);

        taskManager.getEpic(epicId);
        taskManager.getSubtask(subtaskId1);
        taskManager.getSubtask(subtaskId2);

        taskManager.deleteEpic(epicId);
        assertTrue(taskManager.getHistory().isEmpty());
    }

    @Test
    void deleteSubtask_shouldRemoveSubtaskFromHistory() {
        Epic epic = new Epic("Test Epic", "Test Description");
        int epicId = taskManager.createEpic(epic);
        Subtask subtask = new Subtask("Test Subtask", "Description", epicId);
        int subtaskId = taskManager.createSubtask(subtask);
        taskManager.getSubtask(subtaskId);

        taskManager.deleteSubtask(subtaskId);
        assertTrue(taskManager.getHistory().isEmpty());
    }
}