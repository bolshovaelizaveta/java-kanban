package http;

import com.google.gson.Gson;
import manager.InMemoryTaskManager;
import manager.TaskManager;
import model.Epic;
import model.Subtask;
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

public class HttpTaskServerSubtasksTest {

    private TaskManager taskManager;
    private HttpTaskServer taskServer;
    private Gson gson;
    private HttpClient client;
    private int epicId;

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

        Epic epic = new Epic("Parent Epic", "Description");
        epicId = taskManager.createEpic(epic);
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    void testCreateSubtask() throws IOException, InterruptedException {
        Subtask subtask = new Subtask("Test Subtask 1", "Desc 1", TaskStatus.NEW, epicId, Duration.ofMinutes(30), LocalDateTime.now());
        String subtaskJson = gson.toJson(subtask);

        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Expected 200 OK for subtask creation (current handler returns 200).");
        assertTrue(response.body().contains("Подзадача создана с ID:"), "Response body should indicate subtask creation.");

        List<Subtask> subtasksFromManager = taskManager.getSubtasks();
        assertNotNull(subtasksFromManager, "Subtasks list should not be null.");
        assertEquals(1, subtasksFromManager.size(), "Expected 1 subtask in manager after creation.");
        assertEquals("Test Subtask 1", subtasksFromManager.get(0).getName(), "Subtask name mismatch.");
        assertEquals(epicId, subtasksFromManager.get(0).getEpicId(), "Subtask epic ID mismatch.");
    }

    @Test
    void testUpdateSubtask() throws IOException, InterruptedException {
        Subtask originalSubtask = new Subtask("Original Subtask", "Original Desc", TaskStatus.NEW, epicId, Duration.ofMinutes(15), LocalDateTime.now().plusHours(1));
        int subtaskId = taskManager.createSubtask(originalSubtask);

        Subtask updatedSubtask = new Subtask("Updated Subtask", "Updated Desc", subtaskId, TaskStatus.DONE, epicId, Duration.ofMinutes(45), LocalDateTime.now().plusHours(2));
        String updatedSubtaskJson = gson.toJson(updatedSubtask);

        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(updatedSubtaskJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Expected 200 OK for subtask update (current handler returns 200).");
        assertTrue(response.body().contains("Подзадача с ID " + subtaskId + " обновлена."), "Response body should indicate subtask update.");

        Subtask subtaskFromManager = taskManager.getSubtask(subtaskId);
        assertNotNull(subtaskFromManager, "Updated subtask should not be null.");
        assertEquals("Updated Subtask", subtaskFromManager.getName(), "Subtask name should be updated.");
        assertEquals(TaskStatus.DONE, subtaskFromManager.getStatus(), "Subtask status should be updated.");
    }

    @Test
    void testCreateSubtaskInvalidJson() throws IOException, InterruptedException {
        String invalidJson = "{invalid json";

        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(invalidJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response.statusCode(), "Expected 400 Bad Request for invalid JSON.");
        assertTrue(response.body().contains("Некорректный формат JSON для подзадачи"), "Response body should indicate JSON format error.");
    }

    @Test
    void testCreateSubtaskEmptyBody() throws IOException, InterruptedException {
        String emptyBody = "";

        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(emptyBody))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response.statusCode(), "Expected 400 Bad Request for empty body.");
        assertTrue(response.body().contains("Тело запроса пустое."), "Response body should indicate empty body error.");
    }

    @Test
    void testCreateSubtaskForNonExistentEpic() throws IOException, InterruptedException {
        int nonExistentEpicId = 9999;
        Subtask subtask = new Subtask("Subtask for Non-Existent Epic", "Desc", TaskStatus.NEW, nonExistentEpicId, Duration.ofMinutes(10), LocalDateTime.now().plusDays(1));
        String subtaskJson = gson.toJson(subtask);

        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response.statusCode(), "Expected 400 Bad Request for non-existent epic ID.");
        assertTrue(response.body().contains("Указанный Epic с ID " + nonExistentEpicId + " не существует."), "Response body should indicate non-existent epic.");
    }

    @Test
    void testUpdateSubtaskChangeEpicId() throws IOException, InterruptedException {
        Epic anotherEpic = new Epic("Another Epic", "Desc");
        int anotherEpicId = taskManager.createEpic(anotherEpic);

        Subtask originalSubtask = new Subtask("Subtask to Change Epic", "Original Desc", TaskStatus.NEW, epicId, Duration.ofMinutes(15), LocalDateTime.now().plusHours(3));
        int subtaskId = taskManager.createSubtask(originalSubtask);

        // Используем конструктор, который принимает ID, и пытаемся изменить epicId
        Subtask updatedSubtask = new Subtask("Updated Subtask", "Updated Desc", subtaskId, TaskStatus.NEW, anotherEpicId, Duration.ofMinutes(15), LocalDateTime.now().plusHours(3));
        String updatedSubtaskJson = gson.toJson(updatedSubtask);

        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(updatedSubtaskJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response.statusCode(), "Expected 400 Bad Request when changing EpicId.");
        assertTrue(response.body().contains("Нельзя изменить EpicId у существующей подзадачи."), "Response body should indicate EpicId change is not allowed.");
    }


    @Test
    void testGetAllSubtasks() throws IOException, InterruptedException {
        taskManager.createSubtask(new Subtask("Subtask 1", "Desc 1", TaskStatus.NEW, epicId, Duration.ofMinutes(10), LocalDateTime.now().plusDays(1)));
        taskManager.createSubtask(new Subtask("Subtask 2", "Desc 2", TaskStatus.IN_PROGRESS, epicId, Duration.ofMinutes(20), LocalDateTime.now().plusDays(2)));

        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Expected 200 OK for getting all subtasks.");

        List<Subtask> subtasks = gson.fromJson(response.body(), new com.google.gson.reflect.TypeToken<List<Subtask>>(){}.getType());

        assertNotNull(subtasks, "Subtasks list should not be null in response.");
        assertEquals(2, subtasks.size(), "Expected 2 subtasks in response.");
        assertEquals("Subtask 1", subtasks.get(0).getName());
        assertEquals("Subtask 2", subtasks.get(1).getName());
    }

    @Test
    void testGetSubtaskByIdValid() throws IOException, InterruptedException {
        Subtask subtask = new Subtask("Subtask for ID", "Desc for ID", TaskStatus.NEW, epicId, Duration.ofMinutes(1), LocalDateTime.now().plusHours(3));
        int subtaskId = taskManager.createSubtask(subtask);

        URI url = URI.create("http://localhost:8080/subtasks?id=" + subtaskId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Expected 200 OK for getting subtask by ID.");

        Subtask receivedSubtask = gson.fromJson(response.body(), Subtask.class);

        assertNotNull(receivedSubtask, "Received subtask should not be null.");
        assertEquals(subtaskId, receivedSubtask.getId(), "Subtask ID mismatch.");
        assertEquals("Subtask for ID", receivedSubtask.getName(), "Subtask name mismatch.");
    }

    @Test
    void testGetSubtaskByIdNonExistent() throws IOException, InterruptedException {
        int nonExistentId = 999;
        URI url = URI.create("http://localhost:8080/subtasks?id=" + nonExistentId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Expected 404 Not Found for non-existent subtask ID.");
        assertTrue(response.body().contains("Подзадача с ID " + nonExistentId + " не найдена."), "Response body should indicate subtask not found.");
    }

    @Test
    void testGetSubtaskByIdInvalidFormat() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/subtasks?id=abc");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(500, response.statusCode(), "Expected 500 Internal Server Error for invalid ID format (current handler logic).");
        assertTrue(response.body().contains("Ошибка сервера: Некорректный формат ID"), "Response body should indicate server error related to ID format.");
    }

    @Test
    void testDeleteSubtaskByIdValid() throws IOException, InterruptedException {
        Subtask subtask = new Subtask("Subtask to Delete", "Desc", TaskStatus.NEW, epicId, Duration.ofMinutes(10), LocalDateTime.now().plusDays(4));
        int subtaskId = taskManager.createSubtask(subtask);

        URI url = URI.create("http://localhost:8080/subtasks?id=" + subtaskId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(204, response.statusCode(), "Expected 204 No Content for successful deletion.");
        assertNull(taskManager.getSubtask(subtaskId), "Subtask should be deleted from manager.");
    }

    @Test
    void testDeleteSubtaskByIdNonExistent() throws IOException, InterruptedException {
        int nonExistentId = 999;
        URI url = URI.create("http://localhost:8080/subtasks?id=" + nonExistentId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Expected 404 Not Found for deleting non-existent subtask.");
        assertTrue(response.body().contains("Подзадача с ID " + nonExistentId + " не найдена для удаления."), "Response body should indicate subtask not found.");
    }

    @Test
    void testDeleteSubtaskByIdInvalidFormat() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/subtasks?id=xyz");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(500, response.statusCode(), "Expected 500 Internal Server Error for invalid ID format (current handler logic).");
        assertTrue(response.body().contains("Ошибка сервера: Некорректный формат ID"), "Response body should indicate server error related to ID format.");
    }

    @Test
    void testDeleteAllSubtasks() throws IOException, InterruptedException {
        taskManager.createSubtask(new Subtask("Subtask 1", "Desc 1", TaskStatus.NEW, epicId, Duration.ofMinutes(10), LocalDateTime.now().plusDays(5)));
        taskManager.createSubtask(new Subtask("Subtask 2", "Desc 2", TaskStatus.IN_PROGRESS, epicId, Duration.ofMinutes(20), LocalDateTime.now().plusDays(6)));

        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(204, response.statusCode(), "Expected 204 No Content for deleting all subtasks.");
        assertTrue(taskManager.getSubtasks().isEmpty(), "All subtasks should be deleted from manager.");
    }

    @Test
    void testGetEpicSubtasksValidEpicId() throws IOException, InterruptedException {
        taskManager.createSubtask(new Subtask("Subtask for Epic 1", "Desc", TaskStatus.NEW, epicId, Duration.ofMinutes(5), LocalDateTime.now().plusDays(7)));
        taskManager.createSubtask(new Subtask("Subtask for Epic 2", "Desc", TaskStatus.IN_PROGRESS, epicId, Duration.ofMinutes(5), LocalDateTime.now().plusDays(8)));

        Epic anotherEpic = new Epic("Another Epic", "Desc");
        int anotherEpicId = taskManager.createEpic(anotherEpic);
        taskManager.createSubtask(new Subtask("Subtask for Another Epic", "Desc", TaskStatus.NEW, anotherEpicId, Duration.ofMinutes(5), LocalDateTime.now().plusDays(9)));

        URI url = URI.create("http://localhost:8080/subtasks/epic?id=" + epicId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Expected 200 OK for getting epic subtasks.");
        List<Subtask> epicSubtasks = gson.fromJson(response.body(), new com.google.gson.reflect.TypeToken<List<Subtask>>(){}.getType());

        assertNotNull(epicSubtasks, "Epic subtasks list should not be null.");
        assertEquals(2, epicSubtasks.size(), "Expected 2 subtasks for the epic.");
        assertEquals("Subtask for Epic 1", epicSubtasks.get(0).getName());
        assertEquals("Subtask for Epic 2", epicSubtasks.get(1).getName());
    }

    @Test
    void testGetEpicSubtasksNonExistentEpicId() throws IOException, InterruptedException {
        int nonExistentEpicId = 9999;
        URI url = URI.create("http://localhost:8080/subtasks/epic?id=" + nonExistentEpicId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode(), "Expected 404 Not Found for non-existent epic when requesting subtasks.");
        assertTrue(response.body().contains("Эпик с ID " + nonExistentEpicId + " не найден."), "Response body should indicate epic not found.");
    }

    @Test
    void testGetEpicSubtasksInvalidEpicIdFormat() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/subtasks/epic?id=invalid");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(500, response.statusCode(), "Expected 500 Internal Server Error for invalid epic ID format (current handler logic).");
        assertTrue(response.body().contains("Ошибка сервера: Некорректный формат ID"), "Response body should indicate server error related to ID format.");
    }

    @Test
    void testGetEpicSubtasksMissingEpicId() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/subtasks/epic");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response.statusCode(), "Expected 400 Bad Request for missing epic ID when requesting subtasks.");
        assertTrue(response.body().contains("Требуется ID эпика для запроса подзадач."), "Response body should indicate missing epic ID.");
    }
}