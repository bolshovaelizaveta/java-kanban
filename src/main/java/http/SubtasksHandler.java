package http;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import manager.ManagerSaveException;
import manager.TaskManager;
import model.Epic;
import model.Subtask;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import http.utils.GsonUtils;

public class SubtasksHandler extends BaseHttpHandler {

    private final TaskManager taskManager;
    private final Gson gson;

    public SubtasksHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
        this.gson = GsonUtils.getGson();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestMethod = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String query = exchange.getRequestURI().getQuery();

        System.out.println("Началась обработка запроса: " + requestMethod + " " + path + " с параметрами: " + query);

        try {
            if (path.equals("/subtasks/epic")) {
                handleEpicSubtasksRequest(exchange, requestMethod, query);
                return;
            }

            switch (requestMethod) {
                case "GET":
                    handleGetSubtasksRequest(exchange, query);
                    break;
                case "POST":
                    handlePostSubtaskRequest(exchange);
                    break;
                case "DELETE":
                    handleDeleteSubtaskRequest(exchange, query);
                    break;
                default:
                    sendMethodNotAllowed(exchange, "Метод " + requestMethod + " не поддерживается.");
            }
        } catch (InvalidIdFormatException e) {
            sendBadRequest(exchange, e.getMessage());
        } catch (Exception e) {
            System.err.println("Ошибка при обработке запроса: " + e.getMessage());
            e.printStackTrace();
            sendInternalServerError(exchange, "Ошибка сервера: " + e.getMessage());
        }
    }

    private void handleEpicSubtasksRequest(HttpExchange exchange, String requestMethod, String query) throws IOException {
        if ("GET".equals(requestMethod)) {
            Optional<Integer> epicIdOptional = parseId(query);
            if (epicIdOptional.isPresent()) {
                int epicId = epicIdOptional.get();
                Epic epic = taskManager.getEpic(epicId);
                if (epic != null) {
                    List<Subtask> epicSubtasks = taskManager.getEpicSubtasks(epicId);
                    String response = gson.toJson(epicSubtasks);
                    sendText(exchange, response);
                } else {
                    sendNotFound(exchange, "Эпик с ID " + epicId + " не найден.");
                }
            } else {
                sendBadRequest(exchange, "Требуется ID эпика для запроса подзадач.");
            }
        } else {
            sendBadRequest(exchange, "Метод " + requestMethod + " не поддерживается для /subtasks/epic.");
        }
    }

    private void handleGetSubtasksRequest(HttpExchange exchange, String query) throws IOException {
        Optional<Integer> subtaskIdOptional = parseId(query);
        if (subtaskIdOptional.isPresent()) {
            int subtaskId = subtaskIdOptional.get();
            Subtask subtask = taskManager.getSubtask(subtaskId);
            if (subtask != null) {
                String response = gson.toJson(subtask);
                sendText(exchange, response);
            } else {
                sendNotFound(exchange, "Подзадача с ID " + subtaskId + " не найдена.");
            }
        } else {
            List<Subtask> subtasks = taskManager.getSubtasks();
            String response = gson.toJson(subtasks);
            sendText(exchange, response);
        }
    }

    private void handlePostSubtaskRequest(HttpExchange exchange) throws IOException {
        InputStream requestBody = exchange.getRequestBody();
        String body = new String(requestBody.readAllBytes(), StandardCharsets.UTF_8);

        if (body.isEmpty()) {
            sendBadRequest(exchange, "Тело запроса пустое.");
            return;
        }

        try {
            Subtask subtask = gson.fromJson(body, Subtask.class);

            if (subtask == null) {
                sendBadRequest(exchange, "Не удалось десериализовать подзадачу из JSON.");
                return;
            }

            if (!taskManager.getEpics().stream().anyMatch(epic -> epic.getId() == subtask.getEpicId())) {
                sendBadRequest(exchange, "Указанный Epic с ID " + subtask.getEpicId() + " не существует.");
                return;
            }

            if (subtask.getId() == 0) {
                int newId = taskManager.createSubtask(subtask);
                sendText(exchange, "Подзадача создана с ID: " + newId);
            } else {
                Subtask existingSubtask = taskManager.getSubtask(subtask.getId());
                if (existingSubtask != null && existingSubtask.getEpicId() != subtask.getEpicId()) {
                    sendBadRequest(exchange, "Нельзя изменить EpicId у существующей подзадачи.");
                    return;
                }
                taskManager.updateSubtask(subtask);
                sendText(exchange, "Подзадача с ID " + subtask.getId() + " обновлена.");
            }

        } catch (JsonSyntaxException e) {
            sendBadRequest(exchange, "Некорректный формат JSON для подзадачи: " + e.getMessage());
        } catch (ManagerSaveException e) {
            sendHasInteractions(exchange, e.getMessage());
        }
    }

    private void handleDeleteSubtaskRequest(HttpExchange exchange, String query) throws IOException {
        Optional<Integer> deleteIdOptional = parseId(query);
        if (deleteIdOptional.isPresent()) {
            int subtaskIdToDelete = deleteIdOptional.get();
            Subtask subtaskToDelete = taskManager.getSubtask(subtaskIdToDelete);
            if (subtaskToDelete != null) {
                taskManager.deleteSubtask(subtaskIdToDelete);
                sendNoContent(exchange);
            } else {
                sendNotFound(exchange, "Подзадача с ID " + subtaskIdToDelete + " не найдена для удаления.");
            }
        } else {
            taskManager.removeAllSubtasks();
            sendNoContent(exchange);
            System.out.println("Все подзадачи удалены.");
        }
    }
}