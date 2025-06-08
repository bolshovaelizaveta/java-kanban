package manager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected TaskManager taskManager;

    public TaskManagerTest() {
    }

    protected abstract TaskManager createTaskManager();

    @BeforeEach
    protected void setUp() {
        this.taskManager = this.createTaskManager();
    }

    @Test
    void taskManagerCreationAndInitialization() {
        Assertions.assertNotNull(this.taskManager);
        Assertions.assertTrue(this.taskManager.getTasks().isEmpty());
        Assertions.assertTrue(this.taskManager.getEpics().isEmpty());
        Assertions.assertTrue(this.taskManager.getSubtasks().isEmpty());
        Assertions.assertTrue(this.taskManager.getHistory().isEmpty());
        Assertions.assertTrue(this.taskManager.getPrioritizedTasks().isEmpty());
    }

    @Test
    void createTaskAndGetById() {
        Task task = new Task("Test Task", "Test Description", Duration.ofMinutes(30L), LocalDateTime.now());
        int taskId = this.taskManager.createTask(task);
        Task retrievedTask = this.taskManager.getTask(taskId);
        Assertions.assertNotNull(retrievedTask);
        Assertions.assertEquals(task, retrievedTask);
        Assertions.assertEquals("Test Task", retrievedTask.getName());
        Assertions.assertEquals("Test Description", retrievedTask.getDescription());
        Assertions.assertEquals(TaskStatus.NEW, retrievedTask.getStatus());
        Assertions.assertEquals(Duration.ofMinutes(30L), retrievedTask.getDuration());
        Assertions.assertEquals(task.getStartTime(), retrievedTask.getStartTime());
        Assertions.assertNotNull(retrievedTask.getEndTime());
    }

    @Test
    void createTaskWithoutTimeAndDuration() {
        Task task = new Task("Task Without Time", "Description");
        int taskId = this.taskManager.createTask(task);
        Task retrievedTask = this.taskManager.getTask(taskId);
        Assertions.assertNotNull(retrievedTask);
        Assertions.assertEquals(task, retrievedTask);
        Assertions.assertNull(retrievedTask.getDuration());
        Assertions.assertNull(retrievedTask.getStartTime());
        Assertions.assertNull(retrievedTask.getEndTime());
    }

    @Test
    void createEpicAndGetById() {
        Epic epic = new Epic("Test Epic", "Test Description");
        int epicId = this.taskManager.createEpic(epic);
        Epic retrievedEpic = this.taskManager.getEpic(epicId);
        Assertions.assertNotNull(retrievedEpic);
        Assertions.assertEquals(epic, retrievedEpic);
        Assertions.assertEquals("Test Epic", retrievedEpic.getName());
        Assertions.assertEquals("Test Description", retrievedEpic.getDescription());
        Assertions.assertEquals(TaskStatus.NEW, retrievedEpic.getStatus());
        Assertions.assertTrue(retrievedEpic.getSubtaskIds().isEmpty());
        Assertions.assertEquals(Duration.ZERO, retrievedEpic.getDuration());
        Assertions.assertNull(retrievedEpic.getStartTime());
        Assertions.assertNull(retrievedEpic.getEndTime());
    }

    @Test
    void createSubtaskAndGetById() {
        Epic epic = new Epic("Parent Epic", "Description");
        int epicId = this.taskManager.createEpic(epic);
        Subtask subtask = new Subtask("Test Subtask", "Test Description", TaskStatus.NEW, epicId, Duration.ofHours(1L), LocalDateTime.now().plusDays(1L));
        int subtaskId = this.taskManager.createSubtask(subtask);
        Subtask retrievedSubtask = this.taskManager.getSubtask(subtaskId);
        Assertions.assertNotNull(retrievedSubtask);
        Assertions.assertEquals(subtask, retrievedSubtask);
        Assertions.assertEquals("Test Subtask", retrievedSubtask.getName());
        Assertions.assertEquals("Test Description", retrievedSubtask.getDescription());
        Assertions.assertEquals(TaskStatus.NEW, retrievedSubtask.getStatus());
        Assertions.assertEquals(epicId, retrievedSubtask.getEpicId());
        Assertions.assertEquals(Duration.ofHours(1L), retrievedSubtask.getDuration());
        Assertions.assertEquals(subtask.getStartTime(), retrievedSubtask.getStartTime());
        Assertions.assertNotNull(retrievedSubtask.getEndTime());
        Epic parentEpic = this.taskManager.getEpic(epicId);
        Assertions.assertTrue(parentEpic.getSubtaskIds().contains(subtaskId));
    }

    @Test
    void createSubtaskWithoutTimeAndDuration() {
        Epic epic = new Epic("Parent Epic", "Description");
        int epicId = this.taskManager.createEpic(epic);
        Subtask subtask = new Subtask("Subtask Without Time", "Description", TaskStatus.NEW, epicId, null, null);
        int subtaskId = this.taskManager.createSubtask(subtask);
        Subtask retrievedSubtask = this.taskManager.getSubtask(subtaskId);
        Assertions.assertNotNull(retrievedSubtask);
        Assertions.assertEquals(subtask, retrievedSubtask);
        Assertions.assertNull(retrievedSubtask.getDuration());
        Assertions.assertNull(retrievedSubtask.getStartTime());
        Assertions.assertNull(retrievedSubtask.getEndTime());
        Epic parentEpic = this.taskManager.getEpic(epicId);
        Assertions.assertTrue(parentEpic.getSubtaskIds().contains(subtaskId));
    }

    @Test
    void cannotCreateSubtaskWithoutExistingEpic() {
        Subtask subtask = new Subtask("Подзадача", "Описание", TaskStatus.NEW, 10, Duration.ofMinutes(30L), LocalDateTime.now());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                        this.taskManager.createSubtask(subtask),
                "Expected IllegalArgumentException when creating subtask with non-existent epic.");
        Assertions.assertEquals("Невозможно создать подзадачу без существующего Эпика.", exception.getMessage());
    }

    @Test
    void updateTask() {
        Task task = new Task("Original Task", "Original Description", Duration.ofMinutes(30L), LocalDateTime.now());
        int taskId = this.taskManager.createTask(task);
        Task updatedTask = new Task("Updated Task", "Updated Description", taskId, TaskStatus.DONE, Duration.ofHours(1L), LocalDateTime.now().plusHours(2L));
        this.taskManager.updateTask(updatedTask);
        Task retrievedTask = this.taskManager.getTask(taskId);
        Assertions.assertNotNull(retrievedTask);
        Assertions.assertEquals(updatedTask, retrievedTask);
        Assertions.assertEquals("Updated Task", retrievedTask.getName());
        Assertions.assertEquals(TaskStatus.DONE, retrievedTask.getStatus());
        Assertions.assertEquals(Duration.ofHours(1L), retrievedTask.getDuration());
        Assertions.assertEquals(updatedTask.getStartTime(), retrievedTask.getStartTime());
    }

    @Test
    void updateTaskTimeAndDurationToNull() {
        Task task = new Task("Original Task", "Original Description", Duration.ofMinutes(30L), LocalDateTime.now());
        int taskId = this.taskManager.createTask(task);
        Task updatedTask = new Task("Updated Task", "Updated Description", taskId, TaskStatus.DONE, (Duration)null, (LocalDateTime)null);
        this.taskManager.updateTask(updatedTask);
        Task retrievedTask = this.taskManager.getTask(taskId);
        Assertions.assertNotNull(retrievedTask);
        Assertions.assertEquals(updatedTask, retrievedTask);
        Assertions.assertNull(retrievedTask.getDuration());
        Assertions.assertNull(retrievedTask.getStartTime());
        Assertions.assertNull(retrievedTask.getEndTime());
    }

    @Test
    void updateEpic() {
        Epic epic = new Epic("Original Epic", "Original Description");
        int epicId = this.taskManager.createEpic(epic);
        Epic updatedEpic = new Epic("Updated Epic", "Updated Description", epicId, TaskStatus.IN_PROGRESS);
        this.taskManager.updateEpic(updatedEpic);
        Epic retrievedEpic = this.taskManager.getEpic(epicId);
        Assertions.assertNotNull(retrievedEpic);
        Assertions.assertEquals(updatedEpic.getName(), retrievedEpic.getName());
        Assertions.assertEquals(updatedEpic.getDescription(), retrievedEpic.getDescription());
        Assertions.assertEquals(TaskStatus.NEW, retrievedEpic.getStatus());
        Assertions.assertEquals(Duration.ZERO, retrievedEpic.getDuration());
        Assertions.assertNull(retrievedEpic.getStartTime());
        Assertions.assertNull(retrievedEpic.getEndTime());
    }

    @Test
    void updateSubtask() {
        Epic epic = new Epic("Parent Epic", "Description");
        int epicId = this.taskManager.createEpic(epic);
        Subtask subtask = new Subtask("Original Subtask", "Original Description", TaskStatus.NEW, epicId, Duration.ofMinutes(30L), LocalDateTime.now());
        int subtaskId = this.taskManager.createSubtask(subtask);
        Subtask updatedSubtask = new Subtask("Updated Subtask", "Updated Description", subtaskId, TaskStatus.DONE, epicId, Duration.ofMinutes(45L), LocalDateTime.now().plusMinutes(30L));
        this.taskManager.updateSubtask(updatedSubtask);
        Subtask retrievedSubtask = this.taskManager.getSubtask(subtaskId);
        Assertions.assertNotNull(retrievedSubtask);
        Assertions.assertEquals(updatedSubtask, retrievedSubtask);
        Assertions.assertEquals("Updated Subtask", retrievedSubtask.getName());
        Assertions.assertEquals(TaskStatus.DONE, retrievedSubtask.getStatus());
        Assertions.assertEquals(epicId, retrievedSubtask.getEpicId());
        Assertions.assertEquals(Duration.ofMinutes(45L), retrievedSubtask.getDuration());
        Assertions.assertEquals(updatedSubtask.getStartTime(), retrievedSubtask.getStartTime());
        Epic parentEpic = this.taskManager.getEpic(epicId);
        Assertions.assertNotNull(parentEpic.getStartTime());
        Assertions.assertNotNull(parentEpic.getDuration());
    }

    @Test
    void updateSubtaskTimeAndDurationToNull() {
        Epic epic = new Epic("Parent Epic", "Description");
        int epicId = this.taskManager.createEpic(epic);
        Subtask subtask = new Subtask("Original Subtask", "Original Description", TaskStatus.NEW, epicId, Duration.ofMinutes(30L), LocalDateTime.now());
        int subtaskId = this.taskManager.createSubtask(subtask);
        Subtask updatedSubtask = new Subtask("Updated Subtask", "Updated Description", subtaskId, TaskStatus.DONE, epicId, (Duration)null, (LocalDateTime)null);
        this.taskManager.updateSubtask(updatedSubtask);
        Subtask retrievedSubtask = this.taskManager.getSubtask(subtaskId);
        Assertions.assertNotNull(retrievedSubtask);
        Assertions.assertEquals(updatedSubtask, retrievedSubtask);
        Assertions.assertNull(retrievedSubtask.getDuration());
        Assertions.assertNull(retrievedSubtask.getStartTime());
        Assertions.assertNull(retrievedSubtask.getEndTime());
        Epic parentEpic = this.taskManager.getEpic(epicId);
        Assertions.assertNull(parentEpic.getStartTime());
        Assertions.assertEquals(Duration.ZERO, parentEpic.getDuration());
        Assertions.assertNull(parentEpic.getEndTime());
        Assertions.assertEquals(TaskStatus.DONE, parentEpic.getStatus());
    }

    @Test
    void deleteTaskById() {
        Task task = new Task("Test Task", "Test Description", Duration.ofMinutes(30L), LocalDateTime.now());
        int taskId = this.taskManager.createTask(task);
        this.taskManager.deleteTask(taskId);
        Assertions.assertNull(this.taskManager.getTask(taskId));
        Assertions.assertTrue(this.taskManager.getTasks().isEmpty());
        Assertions.assertTrue(this.taskManager.getPrioritizedTasks().isEmpty());
    }

    @Test
    void deleteEpicById() {
        Epic epic = new Epic("Test Epic", "Test Description");
        int epicId = this.taskManager.createEpic(epic);
        Subtask subtask1 = new Subtask("Subtask 1", "Description", TaskStatus.NEW, epicId, Duration.ofMinutes(15L), LocalDateTime.now());
        int subtaskId1 = this.taskManager.createSubtask(subtask1);
        Subtask subtask2 = new Subtask("Subtask 2", "Description", TaskStatus.NEW, epicId, Duration.ofMinutes(15L), LocalDateTime.now().plusMinutes(20L));
        int subtaskId2 = this.taskManager.createSubtask(subtask2);
        this.taskManager.deleteEpic(epicId);
        Assertions.assertNull(this.taskManager.getEpic(epicId));
        Assertions.assertTrue(this.taskManager.getEpics().isEmpty());
        Assertions.assertNull(this.taskManager.getSubtask(subtaskId1));
        Assertions.assertNull(this.taskManager.getSubtask(subtaskId2));
        Assertions.assertTrue(this.taskManager.getSubtasks().isEmpty());
        Assertions.assertTrue(this.taskManager.getPrioritizedTasks().isEmpty());
    }

    @Test
    void deleteSubtaskById() {
        Epic epic = new Epic("Test Epic", "Test Description");
        int epicId = this.taskManager.createEpic(epic);
        Subtask subtask1 = new Subtask("Subtask 1", "Description", TaskStatus.NEW, epicId, Duration.ofMinutes(15L), LocalDateTime.now());
        int subtaskId1 = this.taskManager.createSubtask(subtask1);
        Subtask subtask2 = new Subtask("Subtask 2", "Description", TaskStatus.NEW, epicId, Duration.ofMinutes(15L), LocalDateTime.now().plusMinutes(20L));
        int subtaskId2 = this.taskManager.createSubtask(subtask2);
        this.taskManager.deleteSubtask(subtaskId1);
        Assertions.assertNull(this.taskManager.getSubtask(subtaskId1));
        Assertions.assertNotNull(this.taskManager.getSubtask(subtaskId2));
        Assertions.assertEquals(1, this.taskManager.getSubtasks().size());
        Epic parentEpic = this.taskManager.getEpic(epicId);
        Assertions.assertNotNull(parentEpic);
        Assertions.assertFalse(parentEpic.getSubtaskIds().contains(subtaskId1));
        Assertions.assertTrue(parentEpic.getSubtaskIds().contains(subtaskId2));
        Assertions.assertEquals(Duration.ofMinutes(15L), parentEpic.getDuration());
        Assertions.assertEquals(subtask2.getStartTime(), parentEpic.getStartTime());
        Assertions.assertEquals(subtask2.getEndTime(), parentEpic.getEndTime());
        List<Task> prioritized = this.taskManager.getPrioritizedTasks();
        Assertions.assertEquals(2, prioritized.size());
        Assertions.assertTrue(prioritized.contains(parentEpic));
        Assertions.assertTrue(prioritized.contains(this.taskManager.getSubtask(subtaskId2)));
    }

    @Test
    void getAllTasks() {
        Task task1 = new Task("Task 1", "Description 1", Duration.ofMinutes(30L), LocalDateTime.now());
        int taskId1 = this.taskManager.createTask(task1);
        Task task2 = new Task("Task 2", "Description 2");
        int taskId2 = this.taskManager.createTask(task2);
        List<Task> tasks = this.taskManager.getTasks();
        Assertions.assertNotNull(tasks);
        Assertions.assertEquals(2, tasks.size());
        Assertions.assertTrue(tasks.contains(this.taskManager.getTask(taskId1)));
        Assertions.assertTrue(tasks.contains(this.taskManager.getTask(taskId2)));
    }

    @Test
    void getAllEpics() {
        Epic epic1 = new Epic("Epic 1", "Description 1");
        int epicId1 = this.taskManager.createEpic(epic1);
        Epic epic2 = new Epic("Epic 2", "Description 2");
        int epicId2 = this.taskManager.createEpic(epic2);
        List<Epic> epics = this.taskManager.getEpics();
        Assertions.assertNotNull(epics);
        Assertions.assertEquals(2, epics.size());
        Assertions.assertTrue(epics.contains(this.taskManager.getEpic(epicId1)));
        Assertions.assertTrue(epics.contains(this.taskManager.getEpic(epicId2)));
    }

    @Test
    void getAllSubtasks() {
        Epic epic = new Epic("Parent Epic", "Description");
        int epicId = this.taskManager.createEpic(epic);
        Subtask subtask1 = new Subtask("Subtask 1", "Description", TaskStatus.NEW, epicId, Duration.ofMinutes(15L), LocalDateTime.now());
        int subtaskId1 = this.taskManager.createSubtask(subtask1);
        Subtask subtask2 = new Subtask("Subtask 2", "Description", TaskStatus.NEW, epicId, null, null);
        int subtaskId2 = this.taskManager.createSubtask(subtask2);
        List<Subtask> subtasks = this.taskManager.getSubtasks();
        Assertions.assertNotNull(subtasks);
        Assertions.assertEquals(2, subtasks.size());
        Assertions.assertTrue(subtasks.contains(this.taskManager.getSubtask(subtaskId1)));
        Assertions.assertTrue(subtasks.contains(this.taskManager.getSubtask(subtaskId2)));
    }

    @Test
    void getEpicSubtasks() {
        Epic epic1 = new Epic("Epic 1", "Description 1");
        int epicId1 = this.taskManager.createEpic(epic1);
        Epic epic2 = new Epic("Epic 2", "Description 2");
        int epicId2 = this.taskManager.createEpic(epic2);
        Subtask subtask1 = new Subtask("Subtask 1", "Description", TaskStatus.NEW, epicId1, null, null);
        int subtaskId1 = this.taskManager.createSubtask(subtask1);
        Subtask subtask2 = new Subtask("Subtask 2", "Description", TaskStatus.NEW, epicId1, null, null);
        int subtaskId2 = this.taskManager.createSubtask(subtask2);
        Subtask subtask3 = new Subtask("Subtask 3", "Description", TaskStatus.NEW, epicId2, null, null);
        int subtaskId3 = this.taskManager.createSubtask(subtask3);
        List<Subtask> epic1Subtasks = this.taskManager.getEpicSubtasks(epicId1);
        List<Subtask> epic2Subtasks = this.taskManager.getEpicSubtasks(epicId2);
        List<Subtask> nonExistingEpicSubtasks = this.taskManager.getEpicSubtasks(99);
        Assertions.assertNotNull(epic1Subtasks);
        Assertions.assertEquals(2, epic1Subtasks.size());
        Assertions.assertTrue(epic1Subtasks.contains(this.taskManager.getSubtask(subtaskId1)));
        Assertions.assertTrue(epic1Subtasks.contains(this.taskManager.getSubtask(subtaskId2)));
        Assertions.assertNotNull(epic2Subtasks);
        Assertions.assertEquals(1, epic2Subtasks.size());
        Assertions.assertTrue(epic2Subtasks.contains(this.taskManager.getSubtask(subtaskId3)));
        Assertions.assertNotNull(nonExistingEpicSubtasks);
        Assertions.assertTrue(nonExistingEpicSubtasks.isEmpty());
    }

    @Test
    void removeAllEpics() {
        Epic epic1 = new Epic("Epic 1", "Description 1");
        int epicId1 = this.taskManager.createEpic(epic1);
        Subtask subtask1 = new Subtask("Subtask 1", "Description", TaskStatus.NEW, epicId1, Duration.ofMinutes(15L), LocalDateTime.now());
        int subtaskId1 = this.taskManager.createSubtask(subtask1);
        Subtask subtask2 = new Subtask("Subtask 2", "Description", TaskStatus.NEW, epicId1, Duration.ofMinutes(15L), LocalDateTime.now().plusMinutes(20L));
        int subtaskId2 = this.taskManager.createSubtask(subtask2);
        Epic epic2 = new Epic("Epic 2", "Description 2");
        int epicId2 = this.taskManager.createEpic(epic2);
        Subtask subtask3 = new Subtask("Subtask 3", "Description", TaskStatus.NEW, epicId2, Duration.ofMinutes(15L), LocalDateTime.now().plusMinutes(40L));
        int subtaskId3 = this.taskManager.createSubtask(subtask3);
        this.taskManager.getEpic(epicId1);
        this.taskManager.getEpic(epicId2);
        this.taskManager.getSubtask(subtaskId1);
        this.taskManager.getSubtask(subtaskId2);
        this.taskManager.getSubtask(subtaskId3);
        this.taskManager.removeAllEpics();
        Assertions.assertTrue(this.taskManager.getEpics().isEmpty());
        Assertions.assertTrue(this.taskManager.getSubtasks().isEmpty());
        Assertions.assertTrue(this.taskManager.getHistory().isEmpty());
        Assertions.assertTrue(this.taskManager.getPrioritizedTasks().isEmpty());
    }

    @Test
    void historyShouldKeepLastViewedTasks() {
        Task task1 = new Task("Task 1", "Description 1", 1, TaskStatus.NEW);
        Task task2 = new Task("Task 2", "Description 2", 2, TaskStatus.NEW);
        Epic epic1 = new Epic("Epic 1", "Description 1", 3, TaskStatus.NEW);
        Subtask subtask1 = new Subtask("Subtask 1", "Description", 4, TaskStatus.NEW, 3, null, null);
        HistoryManager historyManager = Managers.getDefaultHistory();
        historyManager.add(task1);
        historyManager.add(epic1);
        historyManager.add(subtask1);
        historyManager.add(task2);
        historyManager.add(task1);
        List<Task> history = historyManager.getHistory();
        Assertions.assertNotNull(history);
        Assertions.assertEquals(4, history.size());
        Assertions.assertEquals(epic1, history.get(0));
        Assertions.assertEquals(subtask1, history.get(1));
        Assertions.assertEquals(task2, history.get(2));
        Assertions.assertEquals(task1, history.get(3));
    }

    @Test
    void historyShouldRemoveDeletedTasks() {
        Task task1 = new Task("Task 1", "Desc 1", 1, TaskStatus.NEW);
        Task task2 = new Task("Task 2", "Desc 2", 2, TaskStatus.NEW);
        this.taskManager.createTask(task1);
        this.taskManager.createTask(task2);
        this.taskManager.getTask(1);
        this.taskManager.getTask(2);
        Assertions.assertEquals(2, this.taskManager.getHistory().size());
        this.taskManager.deleteTask(1);
        Assertions.assertEquals(1, this.taskManager.getHistory().size());
        Assertions.assertEquals(2, ((Task)this.taskManager.getHistory().get(0)).getId());
        Epic epic = new Epic("Epic for history delete", "desc");
        int epicId = this.taskManager.createEpic(epic);
        Subtask subtask = new Subtask("Subtask for history delete", "desc", TaskStatus.NEW, epicId, null, null);
        int subtaskId = this.taskManager.createSubtask(subtask);
        this.taskManager.getEpic(epicId);
        this.taskManager.getSubtask(subtaskId);
        Assertions.assertEquals(3, this.taskManager.getHistory().size());
        this.taskManager.deleteEpic(epicId);
        Assertions.assertEquals(1, this.taskManager.getHistory().size());
        Assertions.assertEquals(2, ((Task)this.taskManager.getHistory().get(0)).getId());
    }

    @Test
    void getPrioritizedTasks_shouldReturnEmptyListWhenNoTasksWithStartTime() {
        Task task1 = new Task("Task 1", "Desc");
        this.taskManager.createTask(task1);
        Epic epic1 = new Epic("Epic 1", "Desc");
        this.taskManager.createEpic(epic1);
        Subtask subtask1 = new Subtask("Subtask 1", "Desc", TaskStatus.NEW, epic1.getId(), null, null);
        this.taskManager.createSubtask(subtask1);
        List<Task> prioritized = this.taskManager.getPrioritizedTasks();
        Assertions.assertNotNull(prioritized);
        Assertions.assertTrue(prioritized.isEmpty());
    }

    @Test
    void getPrioritizedTasks_shouldReturnTasksAndSubtasksSortedByStartTime() {
        Epic epic1 = new Epic("Epic 1", "Desc");
        int epicId1 = this.taskManager.createEpic(epic1);
        Task task1 = new Task("Task 1", "Desc", Duration.ofMinutes(30L), LocalDateTime.of(2023, 1, 1, 11, 0));
        int taskId1 = this.taskManager.createTask(task1);
        Subtask subtask1 = new Subtask("Subtask 1", "Desc", TaskStatus.NEW, epicId1, Duration.ofMinutes(15L), LocalDateTime.of(2023, 1, 1, 10, 0));
        int subtaskId1 = this.taskManager.createSubtask(subtask1);
        Task task2 = new Task("Task 2", "Desc", Duration.ofMinutes(45L), LocalDateTime.of(2023, 1, 1, 12, 0));
        int taskId2 = this.taskManager.createTask(task2);
        Subtask subtask2 = new Subtask("Subtask 2", "Desc", TaskStatus.NEW, epicId1, Duration.ofMinutes(20L), LocalDateTime.of(2023, 1, 1, 10, 30));
        int subtaskId2 = this.taskManager.createSubtask(subtask2);
        List<Task> prioritized = this.taskManager.getPrioritizedTasks();
        Assertions.assertNotNull(prioritized);
        Epic epicAfterSubtasks = this.taskManager.getEpic(epicId1);
        Assertions.assertNotNull(epicAfterSubtasks.getStartTime());
        Assertions.assertEquals(5, prioritized.size());
        Assertions.assertEquals(epicAfterSubtasks, prioritized.get(0));
        Assertions.assertEquals(this.taskManager.getSubtask(subtaskId1), prioritized.get(1));
        Assertions.assertEquals(this.taskManager.getSubtask(subtaskId2), prioritized.get(2));
        Assertions.assertEquals(this.taskManager.getTask(taskId1), prioritized.get(3));
        Assertions.assertEquals(this.taskManager.getTask(taskId2), prioritized.get(4));
    }
}