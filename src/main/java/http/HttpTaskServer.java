package http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpServer;
import http.utils.GsonUtils;
import manager.Managers;
import manager.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;


public class HttpTaskServer {

    private static final int PORT = 8080;
    private final TaskManager taskManager;
    private HttpServer server;

    public static Gson getGson() {
        return GsonUtils.getGson();
    }

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this.taskManager = taskManager;
        this.server = HttpServer.create(new InetSocketAddress(PORT), 0);
        initializeHandlers();
    }

    public HttpTaskServer() throws IOException {
        this.taskManager = Managers.getDefault();
        this.server = HttpServer.create(new InetSocketAddress(PORT), 0);
        initializeHandlers();
    }

    private void initializeHandlers() {
        TasksHandler tasksHandler = new TasksHandler(this.taskManager);
        this.server.createContext("/tasks", tasksHandler);

        EpicsHandler epicsHandler = new EpicsHandler(this.taskManager);
        this.server.createContext("/epics", epicsHandler);

        SubtasksHandler subtasksHandler = new SubtasksHandler(this.taskManager);
        this.server.createContext("/subtasks", subtasksHandler);
        this.server.createContext("/subtasks/epic", subtasksHandler);

        HistoryHandler historyHandler = new HistoryHandler(this.taskManager);
        this.server.createContext("/history", historyHandler);
    }

    public void start() {
        System.out.println("Запускаем HTTP-сервер на порту " + PORT);
        System.out.println("http://localhost:" + PORT + "/tasks");
        server.start();
    }

    public void stop() {
        server.stop(0);
        System.out.println("HTTP-сервер остановлен.");
    }

    public static void main(String[] args) {
        try {
            HttpTaskServer httpTaskServer = new HttpTaskServer();
            httpTaskServer.start();

        } catch (IOException e) {
            System.err.println("Ошибка при запуске HTTP-сервера: " + e.getMessage());
            e.printStackTrace();
        }
    }
}