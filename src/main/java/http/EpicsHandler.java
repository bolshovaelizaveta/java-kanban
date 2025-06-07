package http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import manager.ManagerSaveException;
import manager.TaskManager;
import model.Epic;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import http.utils.GsonUtils;
import java.util.List;
import java.util.Optional;

public class EpicsHandler extends BaseHttpHandler {

    private final TaskManager taskManager;
    private final Gson gson;

    public EpicsHandler(TaskManager taskManager) {
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
                    Optional<Integer> epicIdOptional = parseId(query);
                    if (epicIdOptional.isPresent()) {
                        int epicId = epicIdOptional.get();
                        Epic epic = taskManager.getEpic(epicId);
                        if (epic != null) {
                            String response = gson.toJson(epic);
                            sendText(exchange, response);
                        } else {
                            sendNotFound(exchange, "Эпик с ID " + epicId + " не найден.");
                        }
                    } else {
                        List<Epic> epics = taskManager.getEpics();
                        String response = gson.toJson(epics);
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
                        Epic epic = gson.fromJson(body, Epic.class);

                        if (epic == null) {
                            sendBadRequest(exchange, "Не удалось десериализовать эпик из JSON.");
                            return;
                        }

                        Optional<Integer> idFromQuery = parseId(query);

                        if (epic.getId() == 0) {
                            int newId = taskManager.createEpic(epic);
                            sendText(exchange, "Эпик создан с ID: " + newId);
                        } else {
                            taskManager.updateEpic(epic);
                            sendText(exchange, "Эпик с ID " + epic.getId() + " обновлен.");
                        }

                    } catch (JsonSyntaxException e) {
                        sendBadRequest(exchange, "Некорректный формат JSON для эпика: " + e.getMessage());
                    } catch (ManagerSaveException e) {
                        sendHasInteractions(exchange, e.getMessage());
                    }
                    break;
                case "DELETE":
                    Optional<Integer> deleteIdOptional = parseId(query);
                    if (deleteIdOptional.isPresent()) {
                        int epicIdToDelete = deleteIdOptional.get();
                        Epic epicToDelete = taskManager.getEpic(epicIdToDelete);
                        if (epicToDelete != null) {
                            taskManager.deleteEpic(epicIdToDelete);
                            sendNoContent(exchange);
                        } else {
                            sendNotFound(exchange, "Эпик с ID " + epicIdToDelete + " не найден для удаления.");
                        }
                    } else {
                        taskManager.removeAllEpics();
                        sendNoContent(exchange);
                        System.out.println("Все эпики удалены.");
                    }
                    break;
                default:
                    sendText(exchange, "Метод " + requestMethod + " не поддерживается.");
            }
        } catch (Exception e) {
            System.err.println("Ошибка при обработке запроса: " + e.getMessage());
            e.printStackTrace();
            sendInternalServerError(exchange, "Ошибка сервера: " + e.getMessage());
        }
    }
}
