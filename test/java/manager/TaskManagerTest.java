package manager;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Comparator;
import java.util.Collections;


import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {

    protected TaskManager taskManager;

    protected abstract TaskManager createTaskManager();

    @BeforeEach
    protected void setUp() {
        taskManager = createTaskManager();
    }


    @Test
    void taskManagerCreationAndInitialization() {
        assertNotNull(taskManager);
        assertTrue(taskManager.getTasks().isEmpty());
        assertTrue(taskManager.getEpics().isEmpty());
        assertTrue(taskManager.getSubtasks().isEmpty());
        assertTrue(taskManager.getHistory().isEmpty());
        assertTrue(taskManager.getPrioritizedTasks().isEmpty());
    }


    @Test
    void createTaskAndGetById() {
        Task task = new Task("Test Task", "Test Description", Duration.ofMinutes(30), LocalDateTime.now());
        int taskId = taskManager.createTask(task);
        Task retrievedTask = taskManager.getTask(taskId);

        assertNotNull(retrievedTask);
        assertEquals(task, retrievedTask);
        assertEquals("Test Task", retrievedTask.getName());
        assertEquals("Test Description", retrievedTask.getDescription());
        assertEquals(TaskStatus.NEW, retrievedTask.getStatus());
        assertEquals(Duration.ofMinutes(30), retrievedTask.getDuration());
        assertEquals(task.getStartTime(), retrievedTask.getStartTime());
        assertNotNull(retrievedTask.getEndTime());
    }

    @Test
    void createTaskWithoutTimeAndDuration() {
        Task task = new Task("Task Without Time", "Description");
        int taskId = taskManager.createTask(task);
        Task retrievedTask = taskManager.getTask(taskId);

        assertNotNull(retrievedTask);
        assertEquals(task, retrievedTask);
        assertNull(retrievedTask.getDuration());
        assertNull(retrievedTask.getStartTime());
        assertNull(retrievedTask.getEndTime());
    }


    @Test
    void createEpicAndGetById() {
        Epic epic = new Epic("Test Epic", "Test Description");
        int epicId = taskManager.createEpic(epic);
        Epic retrievedEpic = taskManager.getEpic(epicId);

        assertNotNull(retrievedEpic);
        assertEquals(epic, retrievedEpic);
        assertEquals("Test Epic", retrievedEpic.getName());
        assertEquals("Test Description", retrievedEpic.getDescription());
        assertEquals(TaskStatus.NEW, retrievedEpic.getStatus());
        assertTrue(retrievedEpic.getSubtaskIds().isEmpty());
        assertEquals(Duration.ZERO, retrievedEpic.getDuration());
        assertNull(retrievedEpic.getStartTime());
        assertNull(retrievedEpic.getEndTime());
    }

    @Test
    void createSubtaskAndGetById() {
        Epic epic = new Epic("Parent Epic", "Description");
        int epicId = taskManager.createEpic(epic);

        Subtask subtask = new Subtask("Test Subtask", "Test Description", epicId, Duration.ofHours(1), LocalDateTime.now().plusDays(1));
        int subtaskId = taskManager.createSubtask(subtask);
        Subtask retrievedSubtask = taskManager.getSubtask(subtaskId);

        assertNotNull(retrievedSubtask);
        assertEquals(subtask, retrievedSubtask);
        assertEquals("Test Subtask", retrievedSubtask.getName());
        assertEquals("Test Description", retrievedSubtask.getDescription());
        assertEquals(TaskStatus.NEW, retrievedSubtask.getStatus());
        assertEquals(epicId, retrievedSubtask.getEpicId());
        assertEquals(Duration.ofHours(1), retrievedSubtask.getDuration());
        assertEquals(subtask.getStartTime(), retrievedSubtask.getStartTime());
        assertNotNull(retrievedSubtask.getEndTime());

        Epic parentEpic = taskManager.getEpic(epicId);
        assertTrue(parentEpic.getSubtaskIds().contains(subtaskId));
    }

    @Test
    void createSubtaskWithoutTimeAndDuration() {
        Epic epic = new Epic("Parent Epic", "Description");
        int epicId = taskManager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask Without Time", "Description", epicId);
        int subtaskId = taskManager.createSubtask(subtask);
        Subtask retrievedSubtask = taskManager.getSubtask(subtaskId);

        assertNotNull(retrievedSubtask);
        assertEquals(subtask, retrievedSubtask);
        assertNull(retrievedSubtask.getDuration());
        assertNull(retrievedSubtask.getStartTime());
        assertNull(retrievedSubtask.getEndTime());

        Epic parentEpic = taskManager.getEpic(epicId);
        assertTrue(parentEpic.getSubtaskIds().contains(subtaskId));
    }


    @Test
    void cannotCreateSubtaskWithoutExistingEpic() {
        Subtask subtask = new Subtask("Подзадача", "Описание", 10, Duration.ofMinutes(30), LocalDateTime.now());
        int subtaskId = taskManager.createSubtask(subtask);
        assertEquals(-1, subtaskId);
        assertNull(taskManager.getSubtask(10));
    }


    @Test
    void updateTask() {
        Task task = new Task("Original Task", "Original Description", Duration.ofMinutes(30), LocalDateTime.now());
        int taskId = taskManager.createTask(task);

        Task updatedTask = new Task("Updated Task", "Updated Description", taskId, TaskStatus.DONE, Duration.ofHours(1), LocalDateTime.now().plusHours(2));
        taskManager.updateTask(updatedTask);
        Task retrievedTask = taskManager.getTask(taskId);

        assertNotNull(retrievedTask);
        assertEquals(updatedTask, retrievedTask);
        assertEquals("Updated Task", retrievedTask.getName());
        assertEquals(TaskStatus.DONE, retrievedTask.getStatus());
        assertEquals(Duration.ofHours(1), retrievedTask.getDuration());
        assertEquals(updatedTask.getStartTime(), retrievedTask.getStartTime());
    }

    @Test
    void updateTaskTimeAndDurationToNull() {
        Task task = new Task("Original Task", "Original Description", Duration.ofMinutes(30), LocalDateTime.now());
        int taskId = taskManager.createTask(task);

        Task updatedTask = new Task("Updated Task", "Updated Description", taskId, TaskStatus.DONE, null, null);
        taskManager.updateTask(updatedTask);
        Task retrievedTask = taskManager.getTask(taskId);

        assertNotNull(retrievedTask);
        assertEquals(updatedTask, retrievedTask);
        assertNull(retrievedTask.getDuration());
        assertNull(retrievedTask.getStartTime());
        assertNull(retrievedTask.getEndTime());
    }


    @Test
    void updateEpic() {
        Epic epic = new Epic("Original Epic", "Original Description");
        int epicId = taskManager.createEpic(epic);

        Epic updatedEpic = new Epic("Updated Epic", "Updated Description", epicId, TaskStatus.IN_PROGRESS);
        taskManager.updateEpic(updatedEpic);
        Epic retrievedEpic = taskManager.getEpic(epicId);

        assertNotNull(retrievedEpic);
        assertEquals(updatedEpic.getName(), retrievedEpic.getName());
        assertEquals(updatedEpic.getDescription(), retrievedEpic.getDescription());

        assertEquals(TaskStatus.NEW, retrievedEpic.getStatus());

        assertEquals(Duration.ZERO, retrievedEpic.getDuration());
        assertNull(retrievedEpic.getStartTime());
        assertNull(retrievedEpic.getEndTime());

    }

    @Test
    void updateSubtask() {
        Epic epic = new Epic("Parent Epic", "Description");
        int epicId = taskManager.createEpic(epic);
        Subtask subtask = new Subtask("Original Subtask", "Original Description", epicId, Duration.ofMinutes(30), LocalDateTime.now());
        int subtaskId = taskManager.createSubtask(subtask);

        Subtask updatedSubtask = new Subtask("Updated Subtask", "Updated Description", subtaskId, TaskStatus.DONE, epicId, Duration.ofMinutes(45), LocalDateTime.now().plusMinutes(30));
        taskManager.updateSubtask(updatedSubtask);
        Subtask retrievedSubtask = taskManager.getSubtask(subtaskId);

        assertNotNull(retrievedSubtask);
        assertEquals(updatedSubtask, retrievedSubtask);
        assertEquals("Updated Subtask", retrievedSubtask.getName());
        assertEquals(TaskStatus.DONE, retrievedSubtask.getStatus());
        assertEquals(epicId, retrievedSubtask.getEpicId());
        assertEquals(Duration.ofMinutes(45), retrievedSubtask.getDuration());
        assertEquals(updatedSubtask.getStartTime(), retrievedSubtask.getStartTime());

        Epic parentEpic = taskManager.getEpic(epicId);
        assertNotNull(parentEpic.getStartTime());
        assertNotNull(parentEpic.getDuration());
    }

    @Test
    void updateSubtaskTimeAndDurationToNull() {
        Epic epic = new Epic("Parent Epic", "Description");
        int epicId = taskManager.createEpic(epic);
        Subtask subtask = new Subtask("Original Subtask", "Original Description", epicId, Duration.ofMinutes(30), LocalDateTime.now());
        int subtaskId = taskManager.createSubtask(subtask);

        Subtask updatedSubtask = new Subtask("Updated Subtask", "Updated Description", subtaskId, TaskStatus.DONE, epicId, null, null);
        taskManager.updateSubtask(updatedSubtask);
        Subtask retrievedSubtask = taskManager.getSubtask(subtaskId);

        assertNotNull(retrievedSubtask);
        assertEquals(updatedSubtask, retrievedSubtask);
        assertNull(retrievedSubtask.getDuration());
        assertNull(retrievedSubtask.getStartTime());
        assertNull(retrievedSubtask.getEndTime());

        Epic parentEpic = taskManager.getEpic(epicId);
        assertNull(parentEpic.getStartTime());
        assertEquals(Duration.ZERO, parentEpic.getDuration());
        assertNull(parentEpic.getEndTime());
        assertEquals(TaskStatus.DONE, parentEpic.getStatus());
    }


    @Test
    void deleteTaskById() {
        Task task = new Task("Test Task", "Test Description", Duration.ofMinutes(30), LocalDateTime.now());
        int taskId = taskManager.createTask(task);
        taskManager.deleteTask(taskId);

        assertNull(taskManager.getTask(taskId));
        assertTrue(taskManager.getTasks().isEmpty());
        assertTrue(taskManager.getPrioritizedTasks().isEmpty());
    }

    @Test
    void deleteEpicById() {
        Epic epic = new Epic("Test Epic", "Test Description");
        int epicId = taskManager.createEpic(epic);
        Subtask subtask1 = new Subtask("Subtask 1", "Description", epicId, Duration.ofMinutes(15), LocalDateTime.now());
        int subtaskId1 = taskManager.createSubtask(subtask1);
        Subtask subtask2 = new Subtask("Subtask 2", "Description", epicId, Duration.ofMinutes(15), LocalDateTime.now().plusMinutes(20));
        int subtaskId2 = taskManager.createSubtask(subtask2);

        taskManager.deleteEpic(epicId);

        assertNull(taskManager.getEpic(epicId));
        assertTrue(taskManager.getEpics().isEmpty());
        assertNull(taskManager.getSubtask(subtaskId1));
        assertNull(taskManager.getSubtask(subtaskId2));
        assertTrue(taskManager.getSubtasks().isEmpty());
        assertTrue(taskManager.getPrioritizedTasks().isEmpty());
    }

    @Test
    void deleteSubtaskById() {
        Epic epic = new Epic("Test Epic", "Test Description");
        int epicId = taskManager.createEpic(epic);
        Subtask subtask1 = new Subtask("Subtask 1", "Description", epicId, Duration.ofMinutes(15), LocalDateTime.now());
        int subtaskId1 = taskManager.createSubtask(subtask1);
        Subtask subtask2 = new Subtask("Subtask 2", "Description", epicId, Duration.ofMinutes(15), LocalDateTime.now().plusMinutes(20));
        int subtaskId2 = taskManager.createSubtask(subtask2);

        taskManager.deleteSubtask(subtaskId1);

        assertNull(taskManager.getSubtask(subtaskId1));
        assertNotNull(taskManager.getSubtask(subtaskId2));
        assertEquals(1, taskManager.getSubtasks().size());

        Epic parentEpic = taskManager.getEpic(epicId);
        assertNotNull(parentEpic);
        assertFalse(parentEpic.getSubtaskIds().contains(subtaskId1));
        assertTrue(parentEpic.getSubtaskIds().contains(subtaskId2));

        assertEquals(Duration.ofMinutes(15), parentEpic.getDuration());
        assertEquals(subtask2.getStartTime(), parentEpic.getStartTime());
        assertEquals(subtask2.getEndTime(), parentEpic.getEndTime());

        List<Task> prioritized = taskManager.getPrioritizedTasks();
        assertEquals(2, prioritized.size());
        assertTrue(prioritized.contains(parentEpic));
        assertTrue(prioritized.contains(taskManager.getSubtask(subtaskId2)));
    }


    @Test
    void getAllTasks() {
        Task task1 = new Task("Task 1", "Description 1", Duration.ofMinutes(30), LocalDateTime.now());
        int taskId1 = taskManager.createTask(task1);
        Task task2 = new Task("Task 2", "Description 2");
        int taskId2 = taskManager.createTask(task2);

        List<Task> tasks = taskManager.getTasks();

        assertNotNull(tasks);
        assertEquals(2, tasks.size());
        assertTrue(tasks.contains(taskManager.getTask(taskId1)));
        assertTrue(tasks.contains(taskManager.getTask(taskId2)));
    }

    @Test
    void getAllEpics() {
        Epic epic1 = new Epic("Epic 1", "Description 1");
        int epicId1 = taskManager.createEpic(epic1);
        Epic epic2 = new Epic("Epic 2", "Description 2");
        int epicId2 = taskManager.createEpic(epic2);

        List<Epic> epics = taskManager.getEpics();

        assertNotNull(epics);
        assertEquals(2, epics.size());
        assertTrue(epics.contains(taskManager.getEpic(epicId1)));
        assertTrue(epics.contains(taskManager.getEpic(epicId2)));
    }

    @Test
    void getAllSubtasks() {
        Epic epic = new Epic("Parent Epic", "Description");
        int epicId = taskManager.createEpic(epic);
        Subtask subtask1 = new Subtask("Subtask 1", "Description", epicId, Duration.ofMinutes(15), LocalDateTime.now());
        int subtaskId1 = taskManager.createSubtask(subtask1);
        Subtask subtask2 = new Subtask("Subtask 2", "Description", epicId);
        int subtaskId2 = taskManager.createSubtask(subtask2);

        List<Subtask> subtasks = taskManager.getSubtasks();

        assertNotNull(subtasks);
        assertEquals(2, subtasks.size());
        assertTrue(subtasks.contains(taskManager.getSubtask(subtaskId1)));
        assertTrue(subtasks.contains(taskManager.getSubtask(subtaskId2)));
    }

    @Test
    void getEpicSubtasks() {
        Epic epic1 = new Epic("Epic 1", "Description 1");
        int epicId1 = taskManager.createEpic(epic1);
        Epic epic2 = new Epic("Epic 2", "Description 2");
        int epicId2 = taskManager.createEpic(epic2);

        Subtask subtask1 = new Subtask("Subtask 1", "Description", epicId1);
        int subtaskId1 = taskManager.createSubtask(subtask1);
        Subtask subtask2 = new Subtask("Subtask 2", "Description", epicId1);
        int subtaskId2 = taskManager.createSubtask(subtask2);
        Subtask subtask3 = new Subtask("Subtask 3", "Description", epicId2);
        int subtaskId3 = taskManager.createSubtask(subtask3);

        List<Subtask> epic1Subtasks = taskManager.getEpicSubtasks(epicId1);
        List<Subtask> epic2Subtasks = taskManager.getEpicSubtasks(epicId2);
        List<Subtask> nonExistingEpicSubtasks = taskManager.getEpicSubtasks(99);

        assertNotNull(epic1Subtasks);
        assertEquals(2, epic1Subtasks.size());
        assertTrue(epic1Subtasks.contains(taskManager.getSubtask(subtaskId1)));
        assertTrue(epic1Subtasks.contains(taskManager.getSubtask(subtaskId2)));

        assertNotNull(epic2Subtasks);
        assertEquals(1, epic2Subtasks.size());
        assertTrue(epic2Subtasks.contains(taskManager.getSubtask(subtaskId3)));

        assertNotNull(nonExistingEpicSubtasks);
        assertTrue(nonExistingEpicSubtasks.isEmpty());
    }

    @Test
    void removeAllEpics() {
        Epic epic1 = new Epic("Epic 1", "Description 1");
        int epicId1 = taskManager.createEpic(epic1);
        Subtask subtask1 = new Subtask("Subtask 1", "Description", epicId1, Duration.ofMinutes(15), LocalDateTime.now());
        int subtaskId1 = taskManager.createSubtask(subtask1);
        Subtask subtask2 = new Subtask("Subtask 2", "Description", epicId1, Duration.ofMinutes(15), LocalDateTime.now().plusMinutes(20));
        int subtaskId2 = taskManager.createSubtask(subtask2);
        Epic epic2 = new Epic("Epic 2", "Description 2");
        int epicId2 = taskManager.createEpic(epic2);
        Subtask subtask3 = new Subtask("Subtask 3", "Description", epicId2, Duration.ofMinutes(15), LocalDateTime.now().plusMinutes(40));
        int subtaskId3 = taskManager.createSubtask(subtask3);

        taskManager.getEpic(epicId1);
        taskManager.getEpic(epicId2);
        taskManager.getSubtask(subtaskId1);
        taskManager.getSubtask(subtaskId2);
        taskManager.getSubtask(subtaskId3);


        taskManager.removeAllEpics();

        assertTrue(taskManager.getEpics().isEmpty());
        assertTrue(taskManager.getSubtasks().isEmpty());
        assertTrue(taskManager.getHistory().isEmpty());
        assertTrue(taskManager.getPrioritizedTasks().isEmpty());
    }

    @Test
    void historyShouldKeepLastViewedTasks() {
        Task task1 = new Task("Task 1", "Description 1", 1, TaskStatus.NEW);
        Task task2 = new Task("Task 2", "Description 2", 2, TaskStatus.NEW);
        Epic epic1 = new Epic("Epic 1", "Description 1", 3, TaskStatus.NEW);
        Subtask subtask1 = new Subtask("Subtask 1", "Description", 4, TaskStatus.NEW, 3);

        HistoryManager historyManager = Managers.getDefaultHistory();


        historyManager.add(task1);
        historyManager.add(epic1);
        historyManager.add(subtask1);
        historyManager.add(task2);
        historyManager.add(task1);

        List<Task> history = historyManager.getHistory();
        assertNotNull(history);
        assertEquals(4, history.size());

        assertEquals(epic1, history.get(0));
        assertEquals(subtask1, history.get(1));
        assertEquals(task2, history.get(2));
        assertEquals(task1, history.get(3));
    }

    @Test
    void historyShouldRemoveDeletedTasks() {
        Task task1 = new Task("Task 1", "Desc 1", 1, TaskStatus.NEW);
        Task task2 = new Task("Task 2", "Desc 2", 2, TaskStatus.NEW);
        taskManager.createTask(task1);
        taskManager.createTask(task2);
        taskManager.getTask(1);
        taskManager.getTask(2);
        assertEquals(2, taskManager.getHistory().size());
        taskManager.deleteTask(1);
        assertEquals(1, taskManager.getHistory().size());
        assertEquals(2, taskManager.getHistory().get(0).getId());

        Epic epic = new Epic("Epic for history delete", "desc");
        int epicId = taskManager.createEpic(epic);
        Subtask subtask = new Subtask("Subtask for history delete", "desc", epicId);
        int subtaskId = taskManager.createSubtask(subtask);
        taskManager.getEpic(epicId);
        taskManager.getSubtask(subtaskId);
        assertEquals(3, taskManager.getHistory().size());

        taskManager.deleteEpic(epicId);
        assertEquals(1, taskManager.getHistory().size());
        assertEquals(2, taskManager.getHistory().get(0).getId());

    }


    @Test
    void getPrioritizedTasks_shouldReturnEmptyListWhenNoTasksWithStartTime() {
        Task task1 = new Task("Task 1", "Desc");
        taskManager.createTask(task1);
        Epic epic1 = new Epic("Epic 1", "Desc");
        taskManager.createEpic(epic1);
        Subtask subtask1 = new Subtask("Subtask 1", "Desc", epic1.getId());
        taskManager.createSubtask(subtask1);

        List<Task> prioritized = taskManager.getPrioritizedTasks();
        assertNotNull(prioritized);
        assertTrue(prioritized.isEmpty());
    }

    @Test
    void getPrioritizedTasks_shouldReturnTasksAndSubtasksSortedByStartTime() {
        Epic epic1 = new Epic("Epic 1", "Desc");
        int epicId1 = taskManager.createEpic(epic1);

        Task task1 = new Task("Task 1", "Desc", Duration.ofMinutes(30), LocalDateTime.of(2023, 1, 1, 11, 0));
        int taskId1 = taskManager.createTask(task1);

        Subtask subtask1 = new Subtask("Subtask 1", "Desc", epicId1, Duration.ofMinutes(15), LocalDateTime.of(2023, 1, 1, 10, 0));
        int subtaskId1 = taskManager.createSubtask(subtask1);

        Task task2 = new Task("Task 2", "Desc", Duration.ofMinutes(45), LocalDateTime.of(2023, 1, 1, 12, 0));
        int taskId2 = taskManager.createTask(task2);

        Subtask subtask2 = new Subtask("Subtask 2", "Desc", epicId1, Duration.ofMinutes(20), LocalDateTime.of(2023, 1, 1, 10, 30));
        int subtaskId2 = taskManager.createSubtask(subtask2);


        List<Task> prioritized = taskManager.getPrioritizedTasks();
        assertNotNull(prioritized);


        Epic epicAfterSubtasks = taskManager.getEpic(epicId1);
        assertNotNull(epicAfterSubtasks.getStartTime());


        assertEquals(5, prioritized.size());

        assertEquals(epicAfterSubtasks, prioritized.get(0));
        assertEquals(taskManager.getSubtask(subtaskId1), prioritized.get(1));
        assertEquals(taskManager.getSubtask(subtaskId2), prioritized.get(2));
        assertEquals(taskManager.getTask(taskId1), prioritized.get(3));
        assertEquals(taskManager.getTask(taskId2), prioritized.get(4));


    }

}