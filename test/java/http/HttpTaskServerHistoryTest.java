package http;

import com.google.gson.Gson;
import manager.InMemoryTaskManager;
import manager.TaskManager;
import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskServerHistoryTest {

    private TaskManager taskManager;
    private HttpTaskServer taskServer;
    private Gson gson;
    private HttpClient client;

    @BeforeEach
    public void setUp() throws IOException {
        taskManager = new InMemoryTaskManager();
        taskServer = new HttpTaskServer(taskManager);
        gson = HttpTaskServer.getGson();
        client = HttpClient.newHttpClient();
        taskServer.start();

        taskManager.removeAllTasks();
        taskManager.removeAllEpics();
        taskManager.removeAllSubtasks();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    void testGetEmptyHistory() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Expected 200 OK for history endpoint.");
        List<Task> history = gson.fromJson(response.body(), new com.google.gson.reflect.TypeToken<List<Task>>(){}.getType());

        assertNotNull(history, "History list should not be null.");
        assertTrue(history.isEmpty(), "History should be empty when no tasks have been viewed.");
    }

    @Test
    void testGetHistoryAfterViewingTasks() throws IOException, InterruptedException {
        Task task1 = new Task("Task 1", "Desc 1", Duration.ofMinutes(30), LocalDateTime.now());
        Task task2 = new Task("Task 2", "Desc 2", Duration.ofMinutes(60), LocalDateTime.now().plusHours(1));
        task2.setStatus(TaskStatus.IN_PROGRESS);

        Epic epic1 = new Epic("Epic 1", "Epic Desc 1");
        int taskId1 = taskManager.createTask(task1);
        int taskId2 = taskManager.createTask(task2);
        int epicId1 = taskManager.createEpic(epic1);

        taskManager.getTask(taskId1);
        taskManager.getEpic(epicId1);
        taskManager.getTask(taskId2);

        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Expected 200 OK for history endpoint.");
        List<Task> history = gson.fromJson(response.body(), new com.google.gson.reflect.TypeToken<List<Task>>(){}.getType());

        assertNotNull(history, "History list should not be null.");
        assertEquals(3, history.size(), "Expected 3 items in history.");

        assertEquals(taskId1, history.get(0).getId(), "First viewed task should be first in history.");
        assertEquals(epicId1, history.get(1).getId(), "Second viewed epic should be second in history.");
        assertEquals(taskId2, history.get(2).getId(), "Third viewed task should be third in history.");
    }

    @Test
    void testHistoryOrderOnRevisit() throws IOException, InterruptedException {
        Task task1 = new Task("Task 1", "Desc 1", Duration.ofMinutes(30), LocalDateTime.now());
        Task task2 = new Task("Task 2", "Desc 2", Duration.ofMinutes(60), LocalDateTime.now().plusHours(1));
        task2.setStatus(TaskStatus.IN_PROGRESS); // Если статус не NEW по умолчанию
        int taskId1 = taskManager.createTask(task1);
        int taskId2 = taskManager.createTask(task2);

        taskManager.getTask(taskId1);
        taskManager.getTask(taskId2);
        taskManager.getTask(taskId1);

        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Expected 200 OK for history endpoint.");

        List<Task> history = gson.fromJson(response.body(), new com.google.gson.reflect.TypeToken<List<Task>>(){}.getType());

        assertNotNull(history, "History list should not be null.");
        assertEquals(2, history.size(), "Expected 2 items in history (T1, T2).");
        assertEquals(taskId2, history.get(0).getId(), "Task 2 should be first in history.");
        assertEquals(taskId1, history.get(1).getId(), "Task 1 should be last in history after re-view.");
    }

    @Test
    void testHistoryAfterDeletingTask() throws IOException, InterruptedException {
        Task task1 = new Task("Task 1", "Desc 1", Duration.ofMinutes(30), LocalDateTime.now());
        Task task2 = new Task("Task 2", "Desc 2", Duration.ofMinutes(60), LocalDateTime.now().plusHours(1));
        task2.setStatus(TaskStatus.IN_PROGRESS);
        int taskId1 = taskManager.createTask(task1);
        int taskId2 = taskManager.createTask(task2);

        taskManager.getTask(taskId1);
        taskManager.getTask(taskId2);

        taskManager.deleteTask(taskId1);

        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Expected 200 OK for history endpoint.");

        List<Task> history = gson.fromJson(response.body(), new com.google.gson.reflect.TypeToken<List<Task>>(){}.getType());

        assertNotNull(history, "History list should not be null.");
        assertEquals(1, history.size(), "Expected 1 item in history after deletion.");
        assertEquals(taskId2, history.get(0).getId(), "Only Task 2 should remain in history.");
    }

    @Test
    void testHistoryAfterDeletingEpicAndSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Description");
        int createdEpicId = taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Desc 1", TaskStatus.NEW, createdEpicId, Duration.ofMinutes(10), LocalDateTime.now().plusDays(1));
        Subtask subtask2 = new Subtask("Subtask 2", "Desc 2", TaskStatus.NEW, createdEpicId, Duration.ofMinutes(15), LocalDateTime.now().plusDays(2));
        int subtaskId1 = taskManager.createSubtask(subtask1);
        int subtaskId2 = taskManager.createSubtask(subtask2);

        taskManager.getEpic(createdEpicId);
        taskManager.getSubtask(subtaskId1);
        taskManager.getSubtask(subtaskId2);

        taskManager.deleteEpic(createdEpicId);

        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Expected 200 OK for history endpoint.");

        List<Task> history = gson.fromJson(response.body(), new com.google.gson.reflect.TypeToken<List<Task>>(){}.getType());

        assertNotNull(history, "History list should not be null.");
        assertTrue(history.isEmpty(), "History should be empty after deleting epic and its subtasks.");
    }
}