package manager;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.Collections;


import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    private Path tempFile;

    @BeforeEach
    @Override
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

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile.toFile());
        assertEquals(0, loadedManager.idCounter, "idCounter должен быть 0 при загрузке пустого файла");
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
        assertEquals(task1.getName(), retrievedTask1.getName());
        assertEquals(task1.getDescription(), retrievedTask1.getDescription());
        assertEquals(task1.getStatus(), retrievedTask1.getStatus());
        assertEquals(task1.getId(), retrievedTask1.getId());
        assertEquals(task1.getDuration(), retrievedTask1.getDuration());
        assertEquals(task1.getStartTime(), retrievedTask1.getStartTime());
        assertEquals(task1.getEndTime(), retrievedTask1.getEndTime());


        assertNotNull(retrievedTask2);
        assertEquals(task2.getName(), retrievedTask2.getName());
        assertEquals(task2.getDescription(), retrievedTask2.getDescription());
        assertEquals(task2.getStatus(), retrievedTask2.getStatus());
        assertEquals(task2.getId(), retrievedTask2.getId());
        assertNull(retrievedTask2.getDuration());
        assertNull(retrievedTask2.getStartTime());
        assertNull(retrievedTask2.getEndTime());


        int maxId = Stream.of(task1.getId(), task2.getId()).max(Integer::compareTo).orElse(0);
        assertEquals(maxId, loadedManager.idCounter, "idCounter должен быть равен максимальному ID в файле");

        Task newTask = new Task("Новая задача", "Новое описание", Duration.ofMinutes(10), LocalDateTime.now().plusHours(1));
        int newTaskId = loadedManager.createTask(newTask); // ID 3 (maxId + 1)
        assertEquals(loadedManager.idCounter, newTaskId, "Новый ID должен быть на 1 больше idCounter после загрузки");
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
        assertNotNull(originalEpic.getStartTime());


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

        int maxId = Stream.of(epicId1, subtaskId1, subtaskId2).max(Integer::compareTo).orElse(0);
        assertEquals(maxId, loadedManager.idCounter, "idCounter должен быть равен максимальному ID в файле");

        Task newTask = new Task("Новая задача", "Новое описание", Duration.ofMinutes(10), LocalDateTime.now().plusDays(3));
        int newTaskId = loadedManager.createTask(newTask);
        assertEquals(loadedManager.idCounter, newTaskId, "Новый ID должен быть на 1 больше максимального после загрузки");
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
        assertEquals(4, originalPrioritized.size());

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile.toFile());

        assertEquals(1, loadedManager.getTasks().size());
        assertEquals(1, loadedManager.getEpics().size());
        assertEquals(2, loadedManager.getSubtasks().size());

        List<Task> loadedPrioritized = loadedManager.getPrioritizedTasks();
        assertEquals(4, loadedPrioritized.size(), "Должны загрузиться 4 приоритезированные задачи (Задача, Эпик, 2 Подзадачи)");

        List<Task> expectedPrioritized = new ArrayList<>();
        expectedPrioritized.add(loadedManager.getTask(task.getId()));
        expectedPrioritized.add(loadedManager.getEpic(epicId));
        expectedPrioritized.add(loadedManager.getSubtask(subtask1.getId()));
        expectedPrioritized.add(loadedManager.getSubtask(subtask2.getId()));

        expectedPrioritized.sort(Comparator.comparing(Task::getStartTime, Comparator.nullsLast(LocalDateTime::compareTo)).thenComparing(Task::getId));


        assertEquals(expectedPrioritized.size(), loadedPrioritized.size());

        for (int i = 0; i < expectedPrioritized.size(); i++) {
            assertEquals(expectedPrioritized.get(i).getId(), loadedPrioritized.get(i).getId(), "Элемент " + i + " в приоритезированных списках не совпадает по ID");
        }


        int maxId = Stream.of(task.getId(), epicId, subtask1.getId(), subtask2.getId()).max(Integer::compareTo).orElse(0);
        assertEquals(maxId, loadedManager.idCounter, "idCounter должен быть равен максимальному ID в файле");

    }


    @Test
    void loadFromFile_shouldHandleCorruptedFile() {
        assertDoesNotThrow(() -> {
            Files.writeString(tempFile, "id,type,name,status,description,startTime,duration,epic\n" +
                    "некорректная_строка_1\n" +
                    "1,TASK,Корректная задача,NEW,Описание,2024-01-01T10:00,30,\n" +
                    "2,EPIC,Корректный эпик,NEW,Описание_эпика,,,\n" +
                    "3,SUBTASK,Подзадача без эпика,NEW,Описание_подзадачи,2024-01-01T11:00,15,999\n" +
                    "4,TASK,Задача с неверной длительностью,NEW,Описание,2024-01-01T12:00,неверно,\n" +
                    "5,TASK,Задача с неверным временем,NEW,Описание,неверное_время,30,\n" +
                    "6,SUBTASK,Подзадача с неверным epicId,NEW,Описание,2024-01-01T13:00,30,неверно\n" +
                    "некорректная_строка_2\n");


            FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile.toFile());

            assertEquals(1, loadedManager.getTasks().size(), "Должна загрузиться 1 корректная задача");
            assertEquals(1, loadedManager.getEpics().size(), "Должен загрузиться 1 корректный эпик");
            assertEquals(1, loadedManager.getSubtasks().size(), "Должна загрузиться 1 подзадача с несуществующим эпиком");


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
            assertTrue(correctEpic.getSubtaskIds().isEmpty(), "Эпик не должен содержать подзадачи с несуществующим epicId");


            Subtask unlinkedSubtask = loadedManager.getSubtask(3);
            assertNotNull(unlinkedSubtask, "Подзадача с несуществующим эпиком должна быть загружена");
            assertEquals(999, unlinkedSubtask.getEpicId(), "Подзадача должна иметь epicId из файла");


            List<Task> prioritized = loadedManager.getPrioritizedTasks();
            assertEquals(2, prioritized.size(), "Корректная задача и подзадача с временем должны быть в приоритезированных");
            assertTrue(prioritized.contains(correctTask));
            assertTrue(prioritized.contains(unlinkedSubtask));


            assertEquals(3, loadedManager.idCounter, "idCounter должен быть равен максимальному загруженному ID");


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
        assertTrue(loadedManager.getTasks().isEmpty(), "Задач не должно быть после удаления и загрузки");
        assertTrue(loadedManager.getPrioritizedTasks().isEmpty(), "Приоритезированных задач не должно быть после удаления и загрузки");
        assertEquals(0, loadedManager.idCounter, "idCounter должен быть 0 после удаления последней задачи и загрузки");
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
        assertTrue(loadedManager.getEpics().isEmpty(), "Эпиков не должно быть после удаления и загрузки");
        assertTrue(loadedManager.getSubtasks().isEmpty(), "Подзадач не должно быть после удаления эпика и загрузки");
        assertTrue(loadedManager.getPrioritizedTasks().isEmpty(), "Приоритезированных задач не должно быть после удаления эпика и загрузки");
        assertEquals(0, loadedManager.idCounter, "idCounter должен быть 0 после удаления эпика и подзадач и загрузки");
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
        assertEquals(1, loadedManager.getEpics().size(), "Эпик должен остаться после удаления подзадачи");
        assertTrue(loadedManager.getSubtasks().isEmpty(), "Подзадач не должно быть после удаления");
        assertFalse(loadedManager.getEpic(epicId).getSubtaskIds().contains(subtaskId), "Список подзадач эпика должен быть пуст");

        List<Task> prioritized = loadedManager.getPrioritizedTasks();
        assertEquals(0, prioritized.size(), "После удаления последней подзадачи со временем, эпик должен потерять время");

        Epic loadedEpic = loadedManager.getEpic(epicId);
        assertEquals(Duration.ZERO, loadedEpic.getDuration());
        assertNull(loadedEpic.getStartTime());
        assertNull(loadedEpic.getEndTime());
        assertEquals(TaskStatus.NEW, loadedEpic.getStatus());

        assertEquals(epicId, loadedManager.idCounter, "idCounter должен быть равен ID эпика после удаления подзадачи и загрузки");
    }

}