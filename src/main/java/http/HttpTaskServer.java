package http;

import com.sun.net.httpserver.HttpServer;
import manager.Managers;
import manager.TaskManager;
import model.Subtask;
import model.Task;
import model.Epic;
import model.TaskStatus;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;

public class HttpTaskServer {

    private static final int PORT = 8080;
    private final TaskManager taskManager;
    private HttpServer server;

    public HttpTaskServer() throws IOException {
        this.taskManager = Managers.getDefault();
        this.server = HttpServer.create(new InetSocketAddress(PORT), 0);


        // Временные таски для тестирования
        int taskId1 = taskManager.createTask(new Task("Тестовая задача 1", "Описание 1", Duration.ofMinutes(60), LocalDateTime.of(2025, 7, 7, 10, 0)));
        int taskId2 = taskManager.createTask(new Task("Тестовая задача 2", "Описание 2", Duration.ofMinutes(45), LocalDateTime.of(2025, 7, 8, 10, 0)));

        // Временные эпики для тестирования
        int epicId1 = taskManager.createEpic(new Epic("Тестовый Эпик 1", "Описание эпика 1"));
        int epicId2 = taskManager.createEpic(new Epic("Тестовый Эпик 2", "Описание эпика 2"));

        // Временные сабтаски для тестирования
        int subtaskId1 = taskManager.createSubtask(new Subtask("Подзадача 1 Эпика 1", "Описание подзадачи 1", TaskStatus.NEW, epicId1, Duration.ofMinutes(30), LocalDateTime.of(2025, 7, 7, 12, 0)));
        int subtaskId2 = taskManager.createSubtask(new Subtask("Подзадача 2 Эпика 1", "Описание подзадачи 2", TaskStatus.IN_PROGRESS, epicId1, Duration.ofMinutes(15), LocalDateTime.of(2025, 7, 7, 12, 30)));
        int subtaskId3 = taskManager.createSubtask(new Subtask("Подзадача 1 Эпика 2", "Описание подзадачи 3", TaskStatus.DONE, epicId2, Duration.ofMinutes(20), LocalDateTime.of(2025, 7, 9, 9, 0)));

        System.out.println("Тестовые данные добавлены.");

        // Привязываем обработчики
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