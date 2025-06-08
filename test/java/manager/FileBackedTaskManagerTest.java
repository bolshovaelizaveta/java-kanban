package manager;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    private File tempFile;

    protected TaskManager createTaskManager() {
        try {
            return FileBackedTaskManager.loadFromFile(this.tempFile);
        } catch (ManagerSaveException var2) {
            throw new RuntimeException(var2);
        }
    }

    @BeforeEach
    protected void setUp() {
        try {
            this.tempFile = File.createTempFile("test", ".csv");
        } catch (IOException var2) {
            throw new RuntimeException(var2);
        }

        super.setUp();
    }

    @AfterEach
    protected void tearDown() {
        this.tempFile.delete();
    }

    @Test
    void loadEmptyFile() {
        Assertions.assertTrue(this.taskManager.getTasks().isEmpty(), "Должен загружаться пустой список задач.");
        Assertions.assertTrue(this.taskManager.getEpics().isEmpty(), "Должен загружаться пустой список эпиков.");
        Assertions.assertTrue(this.taskManager.getSubtasks().isEmpty(), "Должен загружаться пустой список подзадач.");
        Assertions.assertTrue(this.taskManager.getHistory().isEmpty(), "История должна быть пустой.");
    }

    @Test
    void saveAndLoadSingleTask() {
        Task task = new Task("Test Task", "Test Description", Duration.ofMinutes(60L), LocalDateTime.of(2023, 1, 1, 10, 0));
        this.taskManager.createTask(task);
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(this.tempFile);
        List<Task> tasks = loadedManager.getTasks();
        Assertions.assertFalse(tasks.isEmpty());
        Assertions.assertEquals(1, tasks.size());
        Assertions.assertEquals(task, tasks.get(0));
        Assertions.assertEquals("Test Task", tasks.get(0).getName());
    }

    @Test
    void saveAndLoadMultipleTasksAndEpicsAndSubtasks() {
        Task task1 = new Task("Task 1", "Description 1", Duration.ofMinutes(30L), LocalDateTime.of(2023, 1, 1, 10, 0));
        this.taskManager.createTask(task1);
        Epic epic1 = new Epic("Epic 1", "Description 1");
        int epicId1 = this.taskManager.createEpic(epic1);
        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", TaskStatus.NEW, epicId1, Duration.ofMinutes(15L), LocalDateTime.of(2023, 1, 1, 10, 30));
        this.taskManager.createSubtask(subtask1);
        Task task2 = new Task("Task 2", "Description 2");
        this.taskManager.createTask(task2);
        this.taskManager.getTask(task1.getId());
        this.taskManager.getEpic(epicId1);
        this.taskManager.getSubtask(subtask1.getId());
        this.taskManager.getTask(task2.getId());
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(this.tempFile);
        Assertions.assertEquals(2, loadedManager.getTasks().size(), "Должно быть 2 задачи.");
        Assertions.assertEquals(1, loadedManager.getEpics().size(), "Должен быть 1 эпик.");
        Assertions.assertEquals(1, loadedManager.getSubtasks().size(), "Должна быть 1 подзадача.");
        Assertions.assertFalse(loadedManager.getHistory().isEmpty(), "История не должна быть пустой.");
        Assertions.assertEquals(4, loadedManager.getHistory().size(), "В истории должно быть 4 элемента.");
    }

    @Test
    void saveAndLoadWithEmptyHistory() {
        Task task = new Task("Task with no history", "Description", Duration.ofMinutes(30L), LocalDateTime.of(2023, 1, 1, 10, 0));
        this.taskManager.createTask(task);
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(this.tempFile);
        Assertions.assertTrue(loadedManager.getHistory().isEmpty(), "История должна быть пустой, если не было просмотров.");
        Assertions.assertFalse(loadedManager.getTasks().isEmpty());
    }

    @Test
    void saveAndLoadWithTasksWithNoTimeAndDuration() {
        Task task1 = new Task("Task 1", "Desc 1");
        this.taskManager.createTask(task1);
        Epic epic1 = new Epic("Epic 1", "Desc 1");
        int epicId1 = this.taskManager.createEpic(epic1);
        Subtask subtask1 = new Subtask("Subtask 1", "Desc 1", TaskStatus.NEW, epicId1, null, null);
        this.taskManager.createSubtask(subtask1);
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(this.tempFile);
        Assertions.assertEquals(1, loadedManager.getTasks().size());
        Assertions.assertNull(((Task)loadedManager.getTasks().get(0)).getStartTime());
        Assertions.assertEquals(1, loadedManager.getEpics().size());
        Assertions.assertNull(((Epic)loadedManager.getEpics().get(0)).getStartTime());
        Assertions.assertEquals(1, loadedManager.getSubtasks().size());
        Assertions.assertNull(((Subtask)loadedManager.getSubtasks().get(0)).getStartTime());
    }
}