package http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import model.Task;

import java.io.IOException;
import java.util.List;
import http.utils.GsonUtils;

public class HistoryHandler extends BaseHttpHandler {

    private final TaskManager taskManager;
    private final Gson gson;

    public HistoryHandler(TaskManager taskManager) {
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
                    handleGetHistoryRequest(exchange);
                    break;
                default:
                    sendMethodNotAllowed(exchange, "Метод " + requestMethod + " не поддерживается для /history.");
                    break;
            }
        } catch (Exception e) {
            System.err.println("Ошибка при обработке запроса: " + e.getMessage());
            e.printStackTrace();
            sendInternalServerError(exchange, "Ошибка сервера: " + e.getMessage());
        }
    }

    private void handleGetHistoryRequest(HttpExchange exchange) throws IOException {
        List<Task> history = taskManager.getHistory();
        String response = gson.toJson(history);
        sendText(exchange, response);
    }
}