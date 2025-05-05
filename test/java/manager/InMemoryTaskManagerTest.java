package manager;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;


import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @Override
    protected TaskManager createTaskManager() {
        return new InMemoryTaskManager();
    }

    @Test
    void shouldCalculateEpicStatusWhenAllSubtasksNew() {
        Epic epic = new Epic("Epic with New Subtasks", "Description");
        int epicId = taskManager.createEpic(epic);
        Subtask subtask1 = new Subtask("Subtask 1 NEW", "Desc", epicId, Duration.ofMinutes(10), LocalDateTime.now());
        taskManager.createSubtask(subtask1);
        Subtask subtask2 = new Subtask("Subtask 2 NEW", "Desc", epicId, Duration.ofMinutes(10), LocalDateTime.now().plusMinutes(15));
        taskManager.createSubtask(subtask2);

        assertEquals(TaskStatus.NEW, epic.getStatus());
        assertNotNull(epic.getStartTime());
        assertNotNull(epic.getEndTime());
        assertNotNull(epic.getDuration());
        assertEquals(Duration.ofMinutes(20), epic.getDuration());
    }

    @Test
    void shouldCalculateEpicStatusWhenAllSubtasksDone() {
        Epic epic = new Epic("Epic with Done Subtasks", "Description");
        int epicId = taskManager.createEpic(epic);
        Subtask subtask1 = new Subtask("Subtask 1 DONE", "Desc", epicId, Duration.ofMinutes(10), LocalDateTime.now());
        subtask1.setStatus(TaskStatus.DONE);
        taskManager.createSubtask(subtask1);
        Subtask subtask2 = new Subtask("Subtask 2 DONE", "Desc", epicId, Duration.ofMinutes(10), LocalDateTime.now().plusMinutes(15));
        subtask2.setStatus(TaskStatus.DONE);
        taskManager.createSubtask(subtask2);

        assertEquals(TaskStatus.DONE, epic.getStatus());
        assertNotNull(epic.getStartTime());
        assertNotNull(epic.getEndTime());
        assertNotNull(epic.getDuration());
        assertEquals(Duration.ofMinutes(20), epic.getDuration());
    }

    @Test
    void shouldCalculateEpicStatusWhenSubtasksMixedNewAndDone() {
        Epic epic = new Epic("Epic with Mixed Subtasks", "Description");
        int epicId = taskManager.createEpic(epic);
        Subtask subtask1 = new Subtask("Subtask 1 NEW", "Desc", epicId, Duration.ofMinutes(10), LocalDateTime.now());
        taskManager.createSubtask(subtask1);
        Subtask subtask2 = new Subtask("Subtask 2 DONE", "Desc", epicId, Duration.ofMinutes(10), LocalDateTime.now().plusMinutes(15));
        subtask2.setStatus(TaskStatus.DONE);
        taskManager.createSubtask(subtask2);

        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus());
        assertNotNull(epic.getStartTime());
        assertNotNull(epic.getEndTime());
        assertNotNull(epic.getDuration());
        assertEquals(Duration.ofMinutes(20), epic.getDuration());
    }

    @Test
    void shouldCalculateEpicStatusWhenAnySubtaskInProgress() {
        Epic epic = new Epic("Epic with IN_PROGRESS Subtask", "Description");
        int epicId = taskManager.createEpic(epic);
        Subtask subtask1 = new Subtask("Subtask 1 NEW", "Desc", epicId, Duration.ofMinutes(10), LocalDateTime.now());
        taskManager.createSubtask(subtask1);
        Subtask subtask2 = new Subtask("Subtask 2 IN_PROGRESS", "Desc", epicId, Duration.ofMinutes(10), LocalDateTime.now().plusMinutes(15));
        subtask2.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.createSubtask(subtask2);
        Subtask subtask3 = new Subtask("Subtask 3 DONE", "Desc", epicId, Duration.ofMinutes(10), LocalDateTime.now().plusMinutes(30));
        subtask3.setStatus(TaskStatus.DONE);
        taskManager.createSubtask(subtask3);


        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus());
        assertNotNull(epic.getStartTime());
        assertNotNull(epic.getEndTime());
        assertNotNull(epic.getDuration());
        assertEquals(Duration.ofMinutes(30), epic.getDuration());
    }

    @Test
    void shouldCalculateEpicStatusWhenNoSubtasks() {
        Epic epic = new Epic("Epic without Subtasks", "Description");
        int epicId = taskManager.createEpic(epic);

        assertEquals(TaskStatus.NEW, epic.getStatus());
        assertTrue(epic.getSubtaskIds().isEmpty());
        assertEquals(Duration.ZERO, epic.getDuration());
        assertNull(epic.getStartTime());
        assertNull(epic.getEndTime());
    }

    @Test
    void shouldRecalculateEpicTimesOnSubtaskUpdate() {
        Epic epic = new Epic("Epic for Recalculation", "Description");
        int epicId = taskManager.createEpic(epic);
        Subtask subtask1 = new Subtask("Subtask 1", "Desc", epicId, Duration.ofMinutes(10), LocalDateTime.of(2023, 1, 1, 10, 0));
        int subtaskId1 = taskManager.createSubtask(subtask1);
        Subtask subtask2 = new Subtask("Subtask 2", "Desc", epicId, Duration.ofMinutes(20), LocalDateTime.of(2023, 1, 1, 11, 0));
        int subtaskId2 = taskManager.createSubtask(subtask2);

        assertEquals(Duration.ofMinutes(30), epic.getDuration());
        assertEquals(LocalDateTime.of(2023, 1, 1, 10, 0), epic.getStartTime());
        assertEquals(LocalDateTime.of(2023, 1, 1, 11, 20), epic.getEndTime());

        Subtask updatedSubtask1 = new Subtask("Subtask 1 Updated", "Desc", subtaskId1, TaskStatus.NEW, epicId, Duration.ofMinutes(25), LocalDateTime.of(2023, 1, 1, 9, 0));
        taskManager.updateSubtask(updatedSubtask1);

        assertEquals(Duration.ofMinutes(45), epic.getDuration());
        assertEquals(LocalDateTime.of(2023, 1, 1, 9, 0), epic.getStartTime());
        assertEquals(LocalDateTime.of(2023, 1, 1, 11, 20), epic.getEndTime());

        Subtask updatedSubtask2 = new Subtask("Subtask 2 Updated", "Desc", subtaskId2, TaskStatus.NEW, epicId, Duration.ofMinutes(30), LocalDateTime.of(2023, 1, 1, 12, 0));
        taskManager.updateSubtask(updatedSubtask2);

        assertEquals(Duration.ofMinutes(55), epic.getDuration());
        assertEquals(LocalDateTime.of(2023, 1, 1, 9, 0), epic.getStartTime());
        assertEquals(LocalDateTime.of(2023, 1, 1, 12, 30), epic.getEndTime());

    }

    @Test
    void shouldRecalculateEpicTimesOnSubtaskDeletion() {
        Epic epic = new Epic("Epic for Recalculation on Delete", "Description");
        int epicId = taskManager.createEpic(epic);
        Subtask subtask1 = new Subtask("Subtask 1", "Desc", epicId, Duration.ofMinutes(10), LocalDateTime.of(2023, 1, 1, 10, 0));
        int subtaskId1 = taskManager.createSubtask(subtask1);
        Subtask subtask2 = new Subtask("Subtask 2", "Desc", epicId, Duration.ofMinutes(20), LocalDateTime.of(2023, 1, 1, 11, 0));
        int subtaskId2 = taskManager.createSubtask(subtask2);
        Subtask subtask3 = new Subtask("Subtask 3", "Desc", epicId, Duration.ofMinutes(15), LocalDateTime.of(2023, 1, 1, 9, 30));

        int subtaskId3 = taskManager.createSubtask(subtask3);


        assertEquals(Duration.ofMinutes(45), epic.getDuration());
        assertEquals(LocalDateTime.of(2023, 1, 1, 9, 30), epic.getStartTime());
        assertEquals(LocalDateTime.of(2023, 1, 1, 11, 20), epic.getEndTime());


        taskManager.deleteSubtask(subtaskId3);

        assertEquals(Duration.ofMinutes(30), epic.getDuration());
        assertEquals(LocalDateTime.of(2023, 1, 1, 10, 0), epic.getStartTime());
        assertEquals(LocalDateTime.of(2023, 1, 1, 11, 20), epic.getEndTime());


        taskManager.deleteSubtask(subtaskId2);

        assertEquals(Duration.ofMinutes(10), epic.getDuration());
        assertEquals(LocalDateTime.of(2023, 1, 1, 10, 0), epic.getStartTime());
        assertEquals(LocalDateTime.of(2023, 1, 1, 10, 10), epic.getEndTime());


        taskManager.deleteSubtask(subtaskId1);

        assertEquals(Duration.ZERO, epic.getDuration());
        assertNull(epic.getStartTime());
        assertNull(epic.getEndTime());
        assertEquals(TaskStatus.NEW, epic.getStatus());
        assertTrue(epic.getSubtaskIds().isEmpty());
    }
}