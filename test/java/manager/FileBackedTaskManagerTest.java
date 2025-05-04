package manager;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends InMemoryTaskManagerTest {

    private Path tempFile;

    @BeforeEach
    @Override
    void setUp() throws IOException {
        tempFile = Files.createTempFile("testTasks", ".csv");
        taskManager = new FileBackedTaskManager(tempFile.toFile());
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(tempFile);
    }

    @Test
    void saveAndLoadEmptyFile() throws ManagerSaveException, IOException {
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile.toFile());

        assertTrue(loadedManager.getTasks().isEmpty());
        assertTrue(loadedManager.getEpics().isEmpty());
        assertTrue(loadedManager.getSubtasks().isEmpty());
        assertTrue(loadedManager.getHistory().isEmpty());
    }

    @Test
    void saveAndLoadTasks() throws ManagerSaveException, IOException {
        Task task1 = new Task("Задача 1", "Описание 1");
        taskManager.createTask(task1);
        Task task2 = new Task("Задача 2", "Описание 2");
        taskManager.createTask(task2);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile.toFile());

        List<Task> loadedTasks = loadedManager.getTasks();
        assertEquals(2, loadedTasks.size());

        Task retrievedTask1 = loadedManager.getTask(task1.getId());
        Task retrievedTask2 = loadedManager.getTask(task2.getId());

        assertNotNull(retrievedTask1);
        assertNotNull(retrievedTask2);
        assertEquals(task1.getName(), retrievedTask1.getName());
        assertEquals(task1.getDescription(), retrievedTask1.getDescription());
        assertEquals(task1.getStatus(), retrievedTask1.getStatus());
        assertEquals(task1.getId(), retrievedTask1.getId());

        assertEquals(task2.getName(), retrievedTask2.getName());
        assertEquals(task2.getDescription(), retrievedTask2.getDescription());
        assertEquals(task2.getStatus(), retrievedTask2.getStatus());
        assertEquals(task2.getId(), retrievedTask2.getId());

        Task newTask = new Task("Новая задача", "Новое описание");
        int newTaskId = loadedManager.createTask(newTask);
        assertTrue(newTaskId > task2.getId());
    }

    @Test
    void saveAndLoadEpicsAndSubtasks() throws ManagerSaveException, IOException {
        Epic epic1 = new Epic("Эпик 1", "Описание эпика 1");
        int epicId1 = taskManager.createEpic(epic1);

        Subtask subtask1 = new Subtask("Подзадача 1", "Описание подзадачи 1", epicId1);
        int subtaskId1 = taskManager.createSubtask(subtask1);

        Subtask subtask2 = new Subtask("Подзадача 2", "Описание подзадачи 2", epicId1);
        int subtaskId2 = taskManager.createSubtask(subtask2);

        subtask1.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(subtask1);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile.toFile());

        List<Epic> loadedEpics = loadedManager.getEpics();
        assertEquals(1, loadedEpics.size());
        Epic loadedEpic = loadedEpics.get(0);
        assertEquals(epic1.getName(), loadedEpic.getName());
        assertEquals(epic1.getDescription(), loadedEpic.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, loadedEpic.getStatus());

        List<Subtask> loadedSubtasks = loadedManager.getSubtasks();
        assertEquals(2, loadedSubtasks.size());

        Subtask retrievedSubtask1 = loadedManager.getSubtask(subtaskId1);
        assertNotNull(retrievedSubtask1);
        assertEquals(subtask1.getName(), retrievedSubtask1.getName());
        assertEquals(subtask1.getDescription(), retrievedSubtask1.getDescription());
        assertEquals(subtask1.getStatus(), retrievedSubtask1.getStatus());
        assertEquals(subtask1.getId(), retrievedSubtask1.getId());
        assertEquals(subtask1.getEpicId(), retrievedSubtask1.getEpicId());

        Subtask retrievedSubtask2 = loadedManager.getSubtask(subtaskId2);
        assertNotNull(retrievedSubtask2);
        assertEquals(subtask2.getName(), retrievedSubtask2.getName());
        assertEquals(subtask2.getDescription(), retrievedSubtask2.getDescription());
        assertEquals(subtask2.getStatus(), retrievedSubtask2.getStatus());
        assertEquals(subtask2.getId(), retrievedSubtask2.getId());
        assertEquals(subtask2.getEpicId(), retrievedSubtask2.getEpicId());

        List<Integer> epicSubtaskIds = loadedEpic.getSubtaskIds();
        assertEquals(2, epicSubtaskIds.size());
        assertTrue(epicSubtaskIds.contains(subtaskId1));
        assertTrue(epicSubtaskIds.contains(subtaskId2));

        Task newTask = new Task("Новая задача", "Новое описание");
        int newTaskId = loadedManager.createTask(newTask);
        assertTrue(newTaskId > Math.max(epicId1, Math.max(subtaskId1, subtaskId2)));
    }

    @Test
    void saveAndLoadMixedTaskTypes() throws ManagerSaveException, IOException {
        Task task = new Task("Простая задача", "Описание");
        taskManager.createTask(task);

        Epic epic = new Epic("Большой эпик", "Описание эпика");
        int epicId = taskManager.createEpic(epic);

        Subtask subtask = new Subtask("Часть эпика", "Описание подзадачи", epicId);
        taskManager.createSubtask(subtask);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile.toFile());

        assertEquals(1, loadedManager.getTasks().size());
        assertEquals(1, loadedManager.getEpics().size());
        assertEquals(1, loadedManager.getSubtasks().size());

        Task loadedTask = loadedManager.getTask(task.getId());
        assertNotNull(loadedTask);
        assertEquals(task.getName(), loadedTask.getName());

        Epic loadedEpic = loadedManager.getEpic(epic.getId());
        assertNotNull(loadedEpic);
        assertEquals(epic.getName(), loadedEpic.getName());

        Subtask loadedSubtask = loadedManager.getSubtask(subtask.getId());
        assertNotNull(loadedSubtask);
        assertEquals(subtask.getName(), loadedSubtask.getName());
        assertEquals(subtask.getEpicId(), loadedSubtask.getEpicId());
    }


    @Test
    void loadFromFile_shouldHandleCorruptedFile() throws IOException {
        Files.writeString(tempFile, "id,type,name,status,description,epic\nнекорректная_строка\n1,TASK,Корректная задача,NEW,Описание,");

        assertDoesNotThrow(() -> {
            FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile.toFile());
            assertEquals(1, loadedManager.getTasks().size());
            assertTrue(loadedManager.getEpics().isEmpty());
            assertTrue(loadedManager.getSubtasks().isEmpty());
        });
    }

    @Test
    void loadFromFile_shouldHandleFileWithOnlyHeader() throws IOException {
        Files.writeString(tempFile, "id,type,name,status,description,epic\n");

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile.toFile());

        assertTrue(loadedManager.getTasks().isEmpty());
        assertTrue(loadedManager.getEpics().isEmpty());
        assertTrue(loadedManager.getSubtasks().isEmpty());
    }

    @Test
    void deleteTask_shouldSaveState() throws IOException {
        Task task = new Task("Задача для удаления", "Описание");
        int taskId = taskManager.createTask(task);

        taskManager.deleteTask(taskId);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile.toFile());
        assertTrue(loadedManager.getTasks().isEmpty());
    }

    @Test
    void deleteEpic_shouldSaveState() throws IOException {
        Epic epic = new Epic("Эпик для удаления", "Описание");
        int epicId = taskManager.createEpic(epic);
        Subtask subtask = new Subtask("Подзадача эпика для удаления", "Описание", epicId);
        taskManager.createSubtask(subtask);

        taskManager.deleteEpic(epicId);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile.toFile());
        assertTrue(loadedManager.getEpics().isEmpty());
        assertTrue(loadedManager.getSubtasks().isEmpty());
    }

    @Test
    void deleteSubtask_shouldSaveState() throws IOException {
        Epic epic = new Epic("Эпик с подзадачей для удаления", "Описание");
        int epicId = taskManager.createEpic(epic);
        Subtask subtask = new Subtask("Подзадача для удаления", "Описание", epicId);
        int subtaskId = taskManager.createSubtask(subtask);

        taskManager.deleteSubtask(subtaskId);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile.toFile());
        assertEquals(1, loadedManager.getEpics().size());
        assertTrue(loadedManager.getSubtasks().isEmpty());
        assertFalse(loadedManager.getEpic(epicId).getSubtaskIds().contains(subtaskId));
    }
}