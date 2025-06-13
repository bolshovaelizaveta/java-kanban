package http;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import manager.ManagerSaveException;
import manager.TaskManager;
import model.Task;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import http.utils.GsonUtils;
import java.util.List;
import java.util.Optional;

public class TasksHandler extends BaseHttpHandler {

    private final TaskManager taskManager;
    private final Gson gson;

    public TasksHandler(TaskManager taskManager) {
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
            switch (requestMethod) {
                case "GET":
                    handleGetTasksRequest(exchange, query);
                    break;
                case "POST":
                    handlePostTaskRequest(exchange);
                    break;
                case "DELETE":
                    handleDeleteTaskRequest(exchange, query);
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

    private void handleGetTasksRequest(HttpExchange exchange, String query) throws IOException {
        Optional<Integer> taskIdOptional = parseId(query);
        if (taskIdOptional.isPresent()) {
            int taskId = taskIdOptional.get();
            Task task = taskManager.getTask(taskId);
            if (task != null) {
                String response = gson.toJson(task);
                sendText(exchange, response);
            } else {
                sendNotFound(exchange, "Задача с ID " + taskId + " не найдена.");
            }
        } else {
            List<Task> tasks = taskManager.getTasks();
            String response = gson.toJson(tasks);
            sendText(exchange, response);
        }
    }

    private void handlePostTaskRequest(HttpExchange exchange) throws IOException {
        InputStream requestBody = exchange.getRequestBody();
        String body = new String(requestBody.readAllBytes(), StandardCharsets.UTF_8);

        if (body.isEmpty()) {
            sendBadRequest(exchange, "Тело запроса пустое.");
            return;
        }

        try {
            Task task = gson.fromJson(body, Task.class);
            if (task == null) {
                sendBadRequest(exchange, "Не удалось десериализовать задачу.");
                return;
            }

            if (task.getId() == 0) {
                int newId = taskManager.createTask(task);
                sendText(exchange, "Задача создана с ID: " + newId);
            } else {
                taskManager.updateTask(task);
                sendText(exchange, "Задача с ID " + task.getId() + " обновлена.");
            }

        } catch (JsonSyntaxException e) {
            sendBadRequest(exchange, "Некорректный формат JSON: " + e.getMessage());
        } catch (ManagerSaveException e) {
            sendHasInteractions(exchange, e.getMessage());
        }
    }

    private void handleDeleteTaskRequest(HttpExchange exchange, String query) throws IOException {
        Optional<Integer> deleteIdOptional = parseId(query);
        if (deleteIdOptional.isPresent()) {
            int taskIdToDelete = deleteIdOptional.get();
            Task taskToDelete = taskManager.getTask(taskIdToDelete);
            if (taskToDelete != null) {
                taskManager.deleteTask(taskIdToDelete);
                sendNoContent(exchange);
            } else {
                sendNotFound(exchange, "Задача с ID " + taskIdToDelete + " не найдена для удаления.");
            }
        } else {
            taskManager.removeAllTasks();
            sendNoContent(exchange);
            System.out.println("Все задачи удалены.");
        }
    }
}