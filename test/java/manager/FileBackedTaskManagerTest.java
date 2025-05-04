package manager;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.time.Duration;
import java.time.LocalDateTime;


import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    private Path tempFile;

    @BeforeEach
    protected void setUp() {
        try {
            tempFile = Files.createTempFile("testTasks", ".csv");
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать временный файл для теста", e);
        }
        super.setUp();
    }

    @AfterEach
    void tearDown() {
        try {
            Files.deleteIfExists(tempFile);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось удалить временный файл после теста", e);
        }
    }

    @Override
    protected FileBackedTaskManager createTaskManager() {
        try {
            return FileBackedTaskManager.loadFromFile(tempFile.toFile());
        } catch (ManagerSaveException e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    void saveAndLoadEmptyFile() {
        assertTrue(taskManager.getTasks().isEmpty());
        assertTrue(taskManager.getEpics().isEmpty());
        assertTrue(taskManager.getSubtasks().isEmpty());
        assertTrue(taskManager.getHistory().isEmpty());
        assertTrue(taskManager.getPrioritizedTasks().isEmpty());
    }


    @Test
    void saveAndLoadTasks() {
        Task task1 = new Task("Задача 1", "Описание 1", Duration.ofMinutes(30), LocalDateTime.now());
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
        assertEquals(task1.getDuration(), retrievedTask1.getDuration());
        assertEquals(task1.getStartTime(), retrievedTask1.getStartTime());
        assertEquals(task1.getEndTime(), retrievedTask1.getEndTime());


        assertEquals(task2.getName(), retrievedTask2.getName());
        assertEquals(task2.getDescription(), retrievedTask2.getDescription());
        assertEquals(task2.getStatus(), retrievedTask2.getStatus());
        assertEquals(task2.getId(), retrievedTask2.getId());
        assertNull(retrievedTask2.getDuration());
        assertNull(retrievedTask2.getStartTime());
        assertNull(retrievedTask2.getEndTime());


        assertEquals(2, loadedManager.idCounter);
        Task newTask = new Task("Новая задача", "Новое описание", Duration.ofMinutes(10), LocalDateTime.now().plusHours(1));
        int newTaskId = loadedManager.createTask(newTask);
        assertEquals(3, newTaskId);
        assertEquals(3, loadedManager.getTasks().size());


        List<Task> prioritized = loadedManager.getPrioritizedTasks();
        assertEquals(2, prioritized.size());
        assertTrue(prioritized.contains(retrievedTask1));
        assertTrue(prioritized.contains(loadedManager.getTask(newTaskId)));
        assertFalse(prioritized.contains(retrievedTask2));


    }

    @Test
    void saveAndLoadEpicsAndSubtasks() {
        Epic epic1 = new Epic("Эпик 1", "Описание эпика 1");
        int epicId1 = taskManager.createEpic(epic1);

        Subtask subtask1 = new Subtask("Подзадача 1 NEW", "Описание 1", epicId1, Duration.ofMinutes(30), LocalDateTime.now().plusHours(1));
        int subtaskId1 = taskManager.createSubtask(subtask1);

        Subtask subtask2 = new Subtask("Подзадача 2 DONE", "Описание 2", epicId1, Duration.ofMinutes(45), LocalDateTime.now().plusHours(2));
        subtask2.setStatus(TaskStatus.DONE);
        int subtaskId2 = taskManager.createSubtask(subtask2);

        Epic originalEpic = taskManager.getEpic(epicId1);
        assertEquals(TaskStatus.IN_PROGRESS, originalEpic.getStatus());
        assertEquals(Duration.ofMinutes(75), originalEpic.getDuration());


        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile.toFile());

        List<Epic> loadedEpics = loadedManager.getEpics();
        assertEquals(1, loadedEpics.size());
        Epic loadedEpic = loadedEpics.get(0);

        assertEquals(epic1.getName(), loadedEpic.getName());
        assertEquals(epic1.getDescription(), loadedEpic.getDescription());
        assertEquals(epic1.getId(), loadedEpic.getId());

        assertEquals(TaskStatus.IN_PROGRESS, loadedEpic.getStatus());
        assertEquals(Duration.ofMinutes(75), loadedEpic.getDuration());
        assertNotNull(loadedEpic.getStartTime());
        assertNotNull(loadedEpic.getEndTime());
        assertTrue(loadedEpic.getSubtaskIds().contains(subtaskId1));
        assertTrue(loadedEpic.getSubtaskIds().contains(subtaskId2));

        List<Subtask> loadedSubtasks = loadedManager.getSubtasks();
        assertEquals(2, loadedSubtasks.size());

        Subtask retrievedSubtask1 = loadedManager.getSubtask(subtaskId1);
        assertNotNull(retrievedSubtask1);
        assertEquals(subtask1.getName(), retrievedSubtask1.getName());
        assertEquals(subtask1.getDescription(), retrievedSubtask1.getDescription());
        assertEquals(subtask1.getStatus(), retrievedSubtask1.getStatus());
        assertEquals(subtask1.getId(), retrievedSubtask1.getId());
        assertEquals(subtask1.getEpicId(), retrievedSubtask1.getEpicId());
        assertEquals(subtask1.getDuration(), retrievedSubtask1.getDuration());
        assertEquals(subtask1.getStartTime(), retrievedSubtask1.getStartTime());


        Subtask retrievedSubtask2 = loadedManager.getSubtask(subtaskId2);
        assertNotNull(retrievedSubtask2);
        assertEquals(subtask2.getName(), retrievedSubtask2.getName());
        assertEquals(subtask2.getDescription(), retrievedSubtask2.getDescription());
        assertEquals(subtask2.getStatus(), retrievedSubtask2.getStatus());
        assertEquals(subtask2.getId(), retrievedSubtask2.getId());
        assertEquals(subtask2.getEpicId(), retrievedSubtask2.getEpicId());
        assertEquals(subtask2.getDuration(), retrievedSubtask2.getDuration());
        assertEquals(subtask2.getStartTime(), retrievedSubtask2.getStartTime());

        List<Task> prioritized = loadedManager.getPrioritizedTasks();
        assertEquals(3, prioritized.size());
        assertTrue(prioritized.contains(loadedEpic));
        assertTrue(prioritized.contains(retrievedSubtask1));
        assertTrue(prioritized.contains(retrievedSubtask2));

        assertEquals(Math.max(epicId1, Math.max(subtaskId1, subtaskId2)), loadedManager.idCounter);
        Task newTask = new Task("Новая задача", "Новое описание", Duration.ofMinutes(10), LocalDateTime.now().plusDays(3));
        int newTaskId = loadedManager.createTask(newTask);
        assertEquals(loadedManager.idCounter, newTaskId);

    }

    @Test
    void saveAndLoadMixedTaskTypes() {
        Task task = new Task("Простая задача", "Описание", Duration.ofMinutes(60), LocalDateTime.now().plusHours(5));
        taskManager.createTask(task);

        Epic epic = new Epic("Большой эпик", "Описание эпика");
        int epicId = taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask("Часть эпика 1", "Описание 1", epicId, Duration.ofMinutes(30), LocalDateTime.now().plusHours(1));
        taskManager.createSubtask(subtask1);

        Subtask subtask2 = new Subtask("Часть эпика 2", "Описание 2", epicId, Duration.ofMinutes(45), LocalDateTime.now().plusHours(2));
        taskManager.createSubtask(subtask2);

        List<Task> originalPrioritized = taskManager.getPrioritizedTasks();
        assertEquals(3, originalPrioritized.size());

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile.toFile());

        assertEquals(1, loadedManager.getTasks().size());
        assertEquals(1, loadedManager.getEpics().size());
        assertEquals(2, loadedManager.getSubtasks().size());

        List<Task> loadedPrioritized = loadedManager.getPrioritizedTasks();
        assertEquals(3, loadedPrioritized.size());
        assertEquals(originalPrioritized.get(0).getStartTime(), loadedPrioritized.get(0).getStartTime());
        assertEquals(originalPrioritized.get(1).getStartTime(), loadedPrioritized.get(1).getStartTime());
        assertEquals(originalPrioritized.get(2).getStartTime(), loadedPrioritized.get(2).getStartTime());

        Epic loadedEpic = loadedManager.getEpic(epicId);
        assertEquals(Duration.ofMinutes(75), loadedEpic.getDuration());
        assertNotNull(loadedEpic.getStartTime());
        assertNotNull(loadedEpic.getEndTime());
        assertEquals(TaskStatus.NEW, loadedEpic.getStatus());

        assertEquals(Math.max(task.getId(), Math.max(epic.getId(), Math.max(subtask1.getId(), subtask2.getId()))), loadedManager.idCounter);
    }


    @Test
    void loadFromFile_shouldHandleCorruptedFile() {
        assertDoesNotThrow(() -> {
            Files.writeString(tempFile, "id,type,name,status,description,startTime,duration,epic\n" +
                    "некорректная_строка_1\n" +
                    "1,TASK,Корректная задача,NEW,Описание,2024-01-01T10:00,30,\n" +
                    "2,EPIC,Корректный эпик,NEW,Описание_эпика,,\n" +
                    "3,SUBTASK,Подзадача без эпика,NEW,Описание_подзадачи,2024-01-01T11:00,15,999\n" +
                    "4,TASK,Задача с неверной длительностью,NEW,Описание,2024-01-01T12:00,неверно,\n" +
                    "5,TASK,Задача с неверным временем,NEW,Описание,неверное_время,30,\n" +
                    "6,SUBTASK,Подзадача с неверным epicId,NEW,Описание,2024-01-01T13:00,30,неверно\n" +
                    "некорректная_строка_2\n");


            FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile.toFile());

            assertEquals(1, loadedManager.getTasks().size());
            assertEquals(1, loadedManager.getEpics().size());
            assertEquals(0, loadedManager.getSubtasks().size());


            Task correctTask = loadedManager.getTask(1);
            assertNotNull(correctTask);
            assertEquals("Корректная задача", correctTask.getName());
            assertEquals(TaskStatus.NEW, correctTask.getStatus());
            assertEquals(Duration.ofMinutes(30), correctTask.getDuration());
            assertNotNull(correctTask.getStartTime());

            Epic correctEpic = loadedManager.getEpic(2);
            assertNotNull(correctEpic);
            assertEquals("Корректный эпик", correctEpic.getName());
            assertEquals(TaskStatus.NEW, correctEpic.getStatus());


            List<Task> prioritized = loadedManager.getPrioritizedTasks();
            assertEquals(1, prioritized.size());
            assertTrue(prioritized.contains(correctTask));

            assertEquals(2, loadedManager.idCounter);


        });

    }

    @Test
    void loadFromFile_shouldHandleFileWithOnlyHeader() {
        assertDoesNotThrow(() -> {
            Files.writeString(tempFile, "id,type,name,status,description,startTime,duration,epic\n");

            FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile.toFile());

            assertTrue(loadedManager.getTasks().isEmpty());
            assertTrue(loadedManager.getEpics().isEmpty());
            assertTrue(loadedManager.getSubtasks().isEmpty());
            assertTrue(loadedManager.getHistory().isEmpty());
            assertTrue(loadedManager.getPrioritizedTasks().isEmpty());
            assertEquals(0, loadedManager.idCounter);
        });
    }

    @Test
    void deleteTask_shouldSaveState() {
        Task task = new Task("Задача для удаления", "Описание", Duration.ofMinutes(10), LocalDateTime.now());
        int taskId = taskManager.createTask(task);

        taskManager.deleteTask(taskId);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile.toFile());
        assertTrue(loadedManager.getTasks().isEmpty());
        assertTrue(loadedManager.getPrioritizedTasks().isEmpty());
        assertEquals(1, loadedManager.idCounter);
    }

    @Test
    void deleteEpic_shouldSaveState() {
        Epic epic = new Epic("Эпик для удаления", "Описание");
        int epicId = taskManager.createEpic(epic);
        Subtask subtask = new Subtask("Подзадача эпика для удаления", "Описание", epicId, Duration.ofMinutes(15), LocalDateTime.now());
        int subtaskId = taskManager.createSubtask(subtask);

        assertEquals(2, taskManager.getPrioritizedTasks().size());

        taskManager.deleteEpic(epicId);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile.toFile());
        assertTrue(loadedManager.getEpics().isEmpty());
        assertTrue(loadedManager.getSubtasks().isEmpty());
        assertTrue(loadedManager.getPrioritizedTasks().isEmpty());
        assertEquals(subtaskId, loadedManager.idCounter);
    }

    @Test
    void deleteSubtask_shouldSaveState() {
        Epic epic = new Epic("Эпик с подзадачей для удаления", "Описание");
        int epicId = taskManager.createEpic(epic);
        Subtask subtask = new Subtask("Подзадача для удаления", "Описание", epicId, Duration.ofMinutes(15), LocalDateTime.now());
        int subtaskId = taskManager.createSubtask(subtask);

        assertEquals(2, taskManager.getPrioritizedTasks().size());

        taskManager.deleteSubtask(subtaskId);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile.toFile());
        assertEquals(1, loadedManager.getEpics().size());
        assertTrue(loadedManager.getSubtasks().isEmpty());
        assertFalse(loadedManager.getEpic(epicId).getSubtaskIds().contains(subtaskId));

        List<Task> prioritized = loadedManager.getPrioritizedTasks();
        assertEquals(1, prioritized.size());
        assertTrue(prioritized.contains(loadedManager.getEpic(epicId)));

        Epic loadedEpic = loadedManager.getEpic(epicId);
        assertEquals(Duration.ZERO, loadedEpic.getDuration());
        assertNull(loadedEpic.getStartTime());
        assertNull(loadedEpic.getEndTime());
        assertEquals(TaskStatus.NEW, loadedEpic.getStatus());

        assertEquals(subtaskId, loadedManager.idCounter);
    }

    // Тесты на корректный перехват исключений при работе с файлами
    @Test
    void loadFromFile_shouldThrowManagerSaveExceptionOnIOError() {
        Path inaccessibleFile = null;
        try {
            inaccessibleFile = Files.createTempFile("inaccessible", ".csv");
            inaccessibleFile.toFile().setReadable(false, false);
            inaccessibleFile.toFile().setWritable(false, false);
        } catch (IOException e) {
            System.err.println("Не удалось создать или изменить атрибуты файла для теста исключения IO: " + e.getMessage());
            return;
        }

        Path finalInaccessibleFile = inaccessibleFile;
        ManagerSaveException exception = assertThrows(ManagerSaveException.class, () -> {
            FileBackedTaskManager.loadFromFile(finalInaccessibleFile.toFile());
        }, "Загрузка из недоступного файла должна выбрасывать ManagerSaveException.");

        assertNotNull(exception.getCause(), "Исключение ManagerSaveException должно содержать причину (IOException).");
        assertTrue(exception.getCause() instanceof IOException, "Причина исключения должна быть IOException.");

        try {
            Files.deleteIfExists(finalInaccessibleFile);
        } catch (IOException e) {
            System.err.println("Не удалось удалить недоступный файл после теста: " + e.getMessage());
        }
    }
}