package http;

import com.google.gson.Gson;
import manager.InMemoryTaskManager;
import manager.TaskManager;
import model.Epic;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskServerEpicsTest {

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
    void testCreateEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic 1", "Description 1");
        String epicJson = gson.toJson(epic);

        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Expected 200 OK status for epic creation (current handler returns 200).");
        assertTrue(response.body().contains("Эпик создан с ID:"), "Response body should indicate epic creation.");

        List<Epic> epicsFromManager = taskManager.getEpics();
        assertNotNull(epicsFromManager, "Epics list should not be null.");
        assertEquals(1, epicsFromManager.size(), "Expected 1 epic in manager after creation.");
        assertEquals("Test Epic 1", epicsFromManager.get(0).getName(), "Epic name mismatch.");
        assertEquals(epic.getDescription(), epicsFromManager.get(0).getDescription(), "Epic description mismatch.");
    }

    @Test
    void testUpdateEpic() throws IOException, InterruptedException {
        Epic originalEpic = new Epic("Original Epic", "Original Description");
        int epicId = taskManager.createEpic(originalEpic);

        Epic updatedEpic = new Epic("Updated Epic", "Updated Description");
        updatedEpic.setId(epicId);
        String updatedEpicJson = gson.toJson(updatedEpic);

        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(updatedEpicJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Expected 200 OK status for epic update (current handler returns 200).");
        assertTrue(response.body().contains("Эпик с ID " + epicId + " обновлен."), "Response body should indicate epic update.");

        Epic epicFromManager = taskManager.getEpic(epicId);
        assertNotNull(epicFromManager, "Updated epic should not be null.");
        assertEquals("Updated Epic", epicFromManager.getName(), "Epic name should be updated.");
        assertEquals("Updated Description", epicFromManager.getDescription(), "Epic description should be updated.");
    }

    @Test
    void testCreateEpicInvalidJson() throws IOException, InterruptedException {
        String invalidJson = "{invalid json";

        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(invalidJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response.statusCode(), "Expected 400 Bad Request for invalid JSON.");
        assertTrue(response.body().contains("Некорректный формат JSON для эпика"), "Response body should indicate JSON format error.");
    }

    @Test
    void testCreateEpicEmptyBody() throws IOException, InterruptedException {
        String emptyBody = "";

        URI url = URI.create("http://localhost:8080/epics");
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
    void testGetAllEpics() throws IOException, InterruptedException {
        taskManager.createEpic(new Epic("Epic 1", "Desc 1"));
        taskManager.createEpic(new Epic("Epic 2", "Desc 2"));

        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Expected 200 OK status for getting all epics.");

        List<Epic> epics = gson.fromJson(response.body(), new com.google.gson.reflect.TypeToken<List<Epic>>(){}.getType());

        assertNotNull(epics, "Epics list should not be null in response.");
        assertEquals(2, epics.size(), "Expected 2 epics in response.");
        assertEquals("Epic 1", epics.get(0).getName());
        assertEquals("Epic 2", epics.get(1).getName());
    }

    @Test
    void testGetEpicByIdValid() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic for ID", "Description for ID");
        int epicId = taskManager.createEpic(epic);

        URI url = URI.create("http://localhost:8080/epics?id=" + epicId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Expected 200 OK status for getting epic by ID.");

        Epic receivedEpic = gson.fromJson(response.body(), Epic.class);

        assertNotNull(receivedEpic, "Received epic should not be null.");
        assertEquals(epicId, receivedEpic.getId(), "Epic ID mismatch.");
        assertEquals("Epic for ID", receivedEpic.getName(), "Epic name mismatch.");
    }

    @Test
    void testGetEpicByIdNonExistent() throws IOException, InterruptedException {
        int nonExistentId = 999;
        URI url = URI.create("http://localhost:8080/epics?id=" + nonExistentId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Expected 404 Not Found for non-existent epic ID.");
        assertTrue(response.body().contains("Эпик с ID " + nonExistentId + " не найден."), "Response body should indicate epic not found.");
    }

    @Test
    void testGetEpicByIdInvalidFormat() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/epics?id=abc");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(), "Expected 400 Bad Request for invalid ID format.");
        assertTrue(response.body().contains("Некорректный формат ID: 'abc' не является числом."), "Response body should indicate invalid ID format error.");
    }

    @Test
    void testDeleteEpicByIdValid() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic to Delete", "Description");
        int epicId = taskManager.createEpic(epic);

        URI url = URI.create("http://localhost:8080/epics?id=" + epicId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(204, response.statusCode(), "Expected 204 No Content for successful deletion.");
        assertNull(taskManager.getEpic(epicId), "Epic should be deleted from manager.");
    }

    @Test
    void testDeleteEpicByIdNonExistent() throws IOException, InterruptedException {
        int nonExistentId = 999;
        URI url = URI.create("http://localhost:8080/epics?id=" + nonExistentId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Expected 404 Not Found for deleting non-existent epic.");
        assertTrue(response.body().contains("Эпик с ID " + nonExistentId + " не найден для удаления."), "Response body should indicate epic not found.");
    }

    @Test
    void testDeleteEpicByIdInvalidFormat() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/epics?id=xyz");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(), "Expected 400 Bad Request for invalid ID format.");
        assertTrue(response.body().contains("Некорректный формат ID: 'xyz' не является числом."), "Response body should indicate invalid ID format error.");
    }

    @Test
    void testDeleteAllEpics() throws IOException, InterruptedException {
        taskManager.createEpic(new Epic("Epic 1", "Desc 1"));
        taskManager.createEpic(new Epic("Epic 2", "Desc 2"));

        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(204, response.statusCode(), "Expected 204 No Content for deleting all epics.");
        assertTrue(taskManager.getEpics().isEmpty(), "All epics should be deleted from manager.");
    }
}