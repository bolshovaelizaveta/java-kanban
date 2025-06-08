package http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public abstract class BaseHttpHandler implements HttpHandler {

    // Вспомогательный класс для указания, что ID был передан, но некорректно
    public static class InvalidIdFormatException extends IllegalArgumentException {
        public InvalidIdFormatException(String message) {
            super(message);
        }
    }

    // Новый вспомогательный метод для отправки HTTP-ответов
    protected void sendResponse(HttpExchange h, int statusCode, String message) throws IOException {
        byte[] resp = message.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(statusCode, resp.length);
        try (OutputStream os = h.getResponseBody()) {
            os.write(resp);
        }
    }

    // Метод sendText из примера ТЗ для статуса 200
    protected void sendText(HttpExchange h, String text) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(200, resp.length);
        try (OutputStream os = h.getResponseBody()) {
            os.write(resp);
        }
    }

    // Метод sendNotFound для статуса 404
    protected void sendNotFound(HttpExchange h, String message) throws IOException {
        byte[] resp = message.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(404, resp.length);
        try (OutputStream os = h.getResponseBody()) {
            os.write(resp);
        }
    }

    // Метод sendHasInteractions для статуса 406
    protected void sendHasInteractions(HttpExchange h, String message) throws IOException {
        byte[] resp = message.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(406, resp.length);
        try (OutputStream os = h.getResponseBody()) {
            os.write(resp);
        }
    }

    // Метод sendNoContent для статуса 204
    protected void sendNoContent(HttpExchange h) throws IOException {
        h.sendResponseHeaders(204, -1); // -1 означает, что нет тела ответа
    }

    // Метод sendBadRequest для статуса 400. Для ошибок, связанных с клиентским запросом (пустое тело, некорректный JSON)
    protected void sendBadRequest(HttpExchange h, String message) throws IOException {
        byte[] resp = message.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(400, resp.length);
        try (OutputStream os = h.getResponseBody()) {
            os.write(resp);
        }
    }

    // Метод sendInternalServerError для статуса 500
    protected void sendInternalServerError(HttpExchange h, String message) throws IOException {
        byte[] resp = message.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(500, resp.length);
        try (OutputStream os = h.getResponseBody()) {
            os.write(resp);
        }
    }

    // Метод sendMethodNotAllowed для статуса 405
    protected void sendMethodNotAllowed(HttpExchange exchange, String message) throws IOException {
        byte[] response = message.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(405, response.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }

    // Измененный метод parseId для парсинга по id
    protected static Optional<Integer> parseId(String query) {
        if (query == null || query.isBlank()) {
            return Optional.empty();
        }
        String[] params = query.split("&");
        for (String param : params) {
            if (param.startsWith("id=")) {
                String idString = param.substring(3);
                if (idString.isEmpty()) {
                    throw new InvalidIdFormatException("ID параметр пуст.");
                }
                try {
                    return Optional.of(Integer.parseInt(idString));
                } catch (NumberFormatException e) {
                    throw new InvalidIdFormatException("Некорректный формат ID: '" + idString + "' не является числом.");
                }
            }
        }
        return Optional.empty();
    }
}