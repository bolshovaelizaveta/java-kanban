package http;

import com.google.gson.Gson;
import manager.InMemoryTaskManager;
import manager.TaskManager;
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

public class HttpTaskServerTasksTest {

    TaskManager manager;
    HttpTaskServer taskServer;
    Gson gson;

    private Task testTask;

    @BeforeEach
    public void setUp() throws IOException {
        manager = new InMemoryTaskManager();
        taskServer = new HttpTaskServer(manager);
        taskServer.start();
        gson = HttpTaskServer.getGson();

        manager.removeAllTasks();
        manager.removeAllEpics();
        manager.removeAllSubtasks();

        testTask = new Task("Test Task", "Description",
                Duration.ofMinutes(30), LocalDateTime.now().plusHours(1));
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }


    // Тесты для GET /tasks
    @Test
    void testGetAllTasksEmpty() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Expected 200 OK for getting empty tasks list");

        List<Task> tasks = gson.fromJson(response.body(), new com.google.gson.reflect.TypeToken<List<Task>>() {}.getType());
        assertNotNull(tasks, "Tasks list should not be null");
        assertTrue(tasks.isEmpty(), "Tasks list should be empty");
    }

    @Test
    void testGetAllTasks() throws IOException, InterruptedException {
        manager.createTask(testTask);
        Task anotherTask = new Task("Another Task", "Another Desc",
                Duration.ofMinutes(60), LocalDateTime.now().plusHours(2));
        manager.createTask(anotherTask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Expected 200 OK for getting all tasks");

        List<Task> tasks = gson.fromJson(response.body(), new com.google.gson.reflect.TypeToken<List<Task>>() {}.getType());
        assertNotNull(tasks, "Tasks list should not be null");
        assertEquals(2, tasks.size(), "Expected 2 tasks");
        assertTrue(tasks.contains(testTask), "List should contain testTask");
        assertTrue(tasks.contains(anotherTask), "List should contain anotherTask");
    }

    @Test
    void testGetTaskById() throws IOException, InterruptedException {
        int taskId = manager.createTask(testTask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks?id=" + taskId);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Expected 200 OK for getting task by ID");

        Task receivedTask = gson.fromJson(response.body(), Task.class);
        assertNotNull(receivedTask, "Received task should not be null");
        assertEquals(testTask, receivedTask, "Received task should match original");
    }

    @Test
    void testGetTaskByIdNotFound() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks?id=999");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode(), "Expected 404 Not Found for non-existent task");
        assertTrue(response.body().contains("не найдена"), "Response body should indicate task not found");
    }

    @Test
    void testGetTaskByIdInvalidFormat() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks?id=abc");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response.statusCode(), "Expected 400 Bad Request for invalid ID format");
    }

    // Тесты для POST /tasks
    @Test
    void testCreateTask() throws IOException, InterruptedException {
        Task newTask = new Task("New Task", "New Description",
                Duration.ofMinutes(45), LocalDateTime.now().plusDays(1));
        String taskJson = gson.toJson(newTask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Expected 200 OK for task creation");
        assertTrue(response.body().contains("Задача создана с ID:"), "Response should indicate creation with ID");

        List<Task> tasksFromManager = manager.getTasks();
        assertNotNull(tasksFromManager, "Tasks list should not be null");
        assertEquals(1, tasksFromManager.size(), "Expected 1 task in manager");
        assertEquals(newTask.getName(), tasksFromManager.get(0).getName(), "Task name should match");
        assertNotEquals(0, tasksFromManager.get(0).getId(), "Task ID should be assigned");
    }

    @Test
    void testUpdateTask() throws IOException, InterruptedException {
        int taskId = manager.createTask(testTask);
        Task updatedTask = new Task("Updated Task", "Updated Description", taskId, TaskStatus.DONE,
                Duration.ofMinutes(60), LocalDateTime.now().plusHours(3));
        String taskJson = gson.toJson(updatedTask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Expected 200 OK for task update");
        assertTrue(response.body().contains("обновлена"), "Response should indicate update");

        Task taskFromManager = manager.getTask(taskId);
        assertNotNull(taskFromManager, "Task should exist in manager");
        assertEquals("Updated Task", taskFromManager.getName(), "Task name should be updated");
        assertEquals(TaskStatus.DONE, taskFromManager.getStatus(), "Task status should be updated");
    }

    @Test
    void testPostTaskEmptyBody() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString("")).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response.statusCode(), "Expected 400 Bad Request for empty body");
        assertTrue(response.body().contains("Тело запроса пустое"), "Response should indicate empty body");
    }

    @Test
    void testPostTaskInvalidJson() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString("{invalid json")).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response.statusCode(), "Expected 400 Bad Request for invalid JSON");
        assertTrue(response.body().contains("Некорректный формат JSON"), "Response should indicate invalid JSON");
    }

    @Test
    void testPostTaskTimeIntersection() throws IOException, InterruptedException {
        Task initialTask = new Task("Initial Task", "Desc",
                Duration.ofMinutes(60), LocalDateTime.of(2025, 1, 1, 10, 0));
        manager.createTask(initialTask);

        Task intersectingTask = new Task("Intersecting Task", "Desc",
                Duration.ofMinutes(60), LocalDateTime.of(2025, 1, 1, 10, 30));
        String taskJson = gson.toJson(intersectingTask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode(), "Expected 406 Not Acceptable for time intersection");
        assertTrue(response.body().contains("пересечению интервалов выполнения"), "Response should indicate intersection");
    }

    // Тесты для DELETE /tasks
    @Test
    void testDeleteAllTasks() throws IOException, InterruptedException {
        manager.createTask(testTask);
        manager.createTask(new Task("Another Task", "Desc", Duration.ofMinutes(10), LocalDateTime.now().plusHours(5)));

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(204, response.statusCode(), "Expected 204 No Content for deleting all tasks");

        assertTrue(manager.getTasks().isEmpty(), "All tasks should be deleted from manager");
    }

    @Test
    void testDeleteTaskById() throws IOException, InterruptedException {
        int taskId = manager.createTask(testTask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks?id=" + taskId);
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(204, response.statusCode(), "Expected 204 No Content for deleting task by ID");

        assertNull(manager.getTask(taskId), "Task should be deleted from manager");
    }

    @Test
    void testDeleteTaskByIdNotFound() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks?id=999");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode(), "Expected 404 Not Found for deleting non-existent task");
        assertTrue(response.body().contains("не найдена для удаления"), "Response body should indicate not found");
    }

    @Test
    void testDeleteTaskByIdInvalidFormat() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks?id=xyz");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response.statusCode(), "Expected 400 Bad Request for invalid ID format");
    }
}
