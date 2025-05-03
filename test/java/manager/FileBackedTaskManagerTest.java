package manager;

import model.Epic;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {

    @Test
    void shouldSaveAndLoadEmptyTaskManager() throws IOException {
        File tempFile = File.createTempFile("empty_tasks", ".csv");
        tempFile.deleteOnExit();

        FileBackedTaskManager taskManager1 = new FileBackedTaskManager(tempFile);
        taskManager1.save();

        FileBackedTaskManager taskManager2 = new FileBackedTaskManager(tempFile);

        assertTrue(taskManager2.getTasks().isEmpty(), "Задачи должны быть пустыми.");
        assertTrue(taskManager2.getEpics().isEmpty(), "Эпики должны быть пустыми.");
        assertTrue(taskManager2.getSubtasks().isEmpty(), "Подзадачи должны быть пустыми.");
        assertTrue(taskManager2.getHistory().isEmpty(), "История должна быть пустой.");
    }

    @Test
    void shouldSaveAndLoadSingleTask() throws IOException {
        File tempFile = File.createTempFile("single_task", ".csv");
        tempFile.deleteOnExit();

        FileBackedTaskManager taskManager1 = new FileBackedTaskManager(tempFile);
        Task task = new Task("Test Task", "Test Description");
        taskManager1.createTask(task);
        taskManager1.save();

        FileBackedTaskManager taskManager2 = new FileBackedTaskManager(tempFile);
        List<Task> tasks2 = taskManager2.getTasks();

        assertEquals(1, tasks2.size(), "Должна быть одна задача.");
        assertEquals(task, tasks2.get(0), "Задачи не совпадают.");
    }

    @Test
    void shouldSaveAndLoadSingleEpicWithSubtask() throws IOException {
        File tempFile = File.createTempFile("epic_with_subtask", ".csv");
        tempFile.deleteOnExit();

        FileBackedTaskManager taskManager1 = new FileBackedTaskManager(tempFile);
        Epic epic = new Epic("Test Epic", "Test Description");
        int epicId = taskManager1.createEpic(epic);
        Subtask subtask = new Subtask("Test Subtask", "Sub Description", epicId);
        taskManager1.createSubtask(subtask);
        taskManager1.save();

        FileBackedTaskManager taskManager2 = new FileBackedTaskManager(tempFile);
        assertEquals(1, taskManager2.getEpics().size(), "Должен быть один эпик.");
        assertEquals(epic, taskManager2.getEpics().getFirst(), "Эпики не совпадают.");
        assertEquals(1, taskManager2.getSubtasks().size(), "Должна быть одна подзадача.");
        assertEquals(subtask, taskManager2.getSubtasks().getFirst(), "Подзадачи не совпадают.");
        assertEquals(epicId, taskManager2.getSubtasks().getFirst().getEpicId(), "ID эпика подзадачи не совпадает.");
        assertEquals(1, taskManager2.getEpics().getFirst().getSubtaskIds().size(), "Эпик должен содержать ID подзадачи.");
        assertEquals(epicId, taskManager2.getEpics().getFirst().getSubtaskIds().getFirst(), "ID подзадачи в эпике не совпадает.");
    }

    @Test
    void shouldSaveAndLoadMultipleTasks() throws IOException {
        File tempFile = File.createTempFile("multiple_tasks", ".csv");
        tempFile.deleteOnExit();

        FileBackedTaskManager taskManager1 = new FileBackedTaskManager(tempFile);
        Task task1 = new Task("Task 1", "Description 1");
        taskManager1.createTask(task1);
        Epic epic1 = new Epic("Epic 1", "Description 1");
        taskManager1.createEpic(epic1);
        int epicId1 = epic1.getId();
        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", epicId1);
        taskManager1.createSubtask(subtask1);
        Task task2 = new Task("Task 2", "Description 2");
        taskManager1.createTask(task2);
        taskManager1.save();

        FileBackedTaskManager taskManager2 = new FileBackedTaskManager(tempFile);
        assertEquals(2, taskManager2.getTasks().size(), "Должно быть две задачи.");
        assertEquals(1, taskManager2.getEpics().size(), "Должен быть один эпик.");
        assertEquals(1, taskManager2.getSubtasks().size(), "Должна быть одна подзадача.");

        assertTrue(taskManager2.getTasks().contains(task1), "Задача 1 должна присутствовать.");
        assertTrue(taskManager2.getTasks().contains(task2), "Задача 2 должна присутствовать.");
        assertTrue(taskManager2.getEpics().contains(epic1), "Эпик 1 должен присутствовать.");
        assertTrue(taskManager2.getSubtasks().contains(subtask1), "Подзадача 1 должна присутствовать.");
    }
}