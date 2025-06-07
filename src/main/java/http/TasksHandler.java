package http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import manager.ManagerSaveException;
import manager.TaskManager;
import model.Task;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import http.utils.GsonUtils;
import java.util.List;
import java.util.Optional;

public class TasksHandler extends BaseHttpHandler {

    private final TaskManager taskManager;
    private final Gson gson;

    public TasksHandler(TaskManager taskManager) {
        this.taskManager = taskManager;

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new GsonUtils.LocalDateTimeAdapter());
        gsonBuilder.registerTypeAdapter(Duration.class, new GsonUtils.DurationAdapter());
        gsonBuilder.setPrettyPrinting();
        this.gson = gsonBuilder.create();
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
                    break;
                case "POST":
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
                        Optional<Integer> idFromQuery = parseId(query);
                        if (idFromQuery.isPresent() && idFromQuery.get() != task.getId() && task.getId() != 0) {
                            System.out.println("Предупреждение: ID в URL (" + idFromQuery.get() + ") не совпадает с ID в теле (" + task.getId() + ").");
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
                    break;
                case "DELETE":
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
                        // Если ID не указан, то удалятся все задачи
                        taskManager.removeAllTasks();
                        sendNoContent(exchange);
                        System.out.println("Все задачи удалены.");
                    }
                    break;
                default:
                    sendText(exchange, "Метод " + requestMethod + " не поддерживается.");
            }
        } catch (Exception e) {
            System.err.println("Ошибка при обработке запроса: " + e.getMessage());
            e.printStackTrace();
            sendBadRequest(exchange, "Ошибка сервера: " + e.getMessage());
        }
    }
}