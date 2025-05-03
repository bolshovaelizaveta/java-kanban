package manager;

import model.Epic;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    private File tempFile;

    @Override
    @BeforeEach
    public void createManagerForTest() throws IOException {
        tempFile = File.createTempFile("test_tasks", ".csv");
        taskManager = new FileBackedTaskManager(tempFile.toPath());
        createTestTasks();
        createTestHistory();
    }

    @Override
    @AfterEach
    public void clear() {
        taskManager.removeAllTasks();
        taskManager.removeAllEpics();
        taskManager.removeAllSubtasks();
        tempFile.delete();
    }

    @Test
    void testSaveAndLoadEmptyFile() throws IOException {
        taskManager.save();
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile.toPath());
        assertTrue(loadedManager.getTasks().isEmpty(), "Задачи не пустые");
        assertTrue(loadedManager.getEpics().isEmpty(), "Эпики не пустые");
        assertTrue(loadedManager.getSubtasks().isEmpty(), "Подзадачи не пустые");
        assertTrue(loadedManager.getHistory().isEmpty(), "История не пустая");
    }

    @Test
    void testSaveMultipleTasks() throws IOException {
        Task task1 = new Task("Task 1", "Description 1");
        Epic epic1 = new Epic("Epic 1", "Description Epic 1");
        Subtask subtask1 = new Subtask("Subtask 1", "Description Subtask 1", epic1.getId());

        taskManager.createTask(task1);
        taskManager.createEpic(epic1);
        taskManager.createSubtask(subtask1);
        taskManager.save();

        List<String> lines = Files.readAllLines(tempFile.toPath());
        assertTrue(lines.size() > 1, "Файл должен содержать данные");
        assertTrue(lines.get(0).contains("id,type,name,status,description,epic"), "Заголовок неверный");
        assertTrue(lines.stream().anyMatch(line -> line.contains("TASK,Task 1")), "Задача не найдена");
        assertTrue(lines.stream().anyMatch(line -> line.contains("EPIC,Epic 1")), "Эпик не найден");
        assertTrue(lines.stream().anyMatch(line -> line.contains("SUBTASK,Subtask 1")), "Подзадача не найдена");
    }

    @Test
    void testLoadMultipleTasks() throws IOException {
        Task task1 = new Task("Task 1", "Description 1");
        Epic epic1 = new Epic("Epic 1", "Description Epic 1");
        Subtask subtask1 = new Subtask("Subtask 1", "Description Subtask 1", 1); // Предполагаем ID

        taskManager.createTask(task1);
        taskManager.createEpic(epic1);
        taskManager.createSubtask(subtask1);
        taskManager.save();

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile.toPath());
        assertEquals(taskManager.getTasks().size(), loadedManager.getTasks().size(), "Количество задач не совпадает");
        assertEquals(taskManager.getEpics().size(), loadedManager.getEpics().size(), "Количество эпиков не совпадает");
        assertEquals(taskManager.getSubtasks().size(), loadedManager.getSubtasks().size(), "Количество подзадач не совпадает");
        // Добавьте более детальное сравнение содержимого задач (по equals() после его реализации)
    }

    // Переопределенные тесты из TaskManagerTest с вызовом saveLoadTest()
    @Override
    @Test
    protected void addTaskTest() {
        super.addTaskTest();
        saveLoadTest();
    }

    @Override
    @Test
    protected void getTaskByIdTest() {
        super.getTaskByIdTest();
        saveLoadTest();
    }

    // ... и другие переопределенные тесты ...

    protected void saveLoadTest() {
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile.toPath());
        assertEquals(taskManager.getTasks().size(), loadedManager.getTasks().size(), "Количество задач после сохранения/загрузки не совпадает");
        assertEquals(taskManager.getEpics().size(), loadedManager.getEpics().size(), "Количество эпиков после сохранения/загрузки не совпадает");
        assertEquals(taskManager.getSubtasks().size(), loadedManager.getSubtasks().size(), "Количество подзадач после сохранения/загрузки не совпадает");
        assertEquals(taskManager.getHistory().size(), loadedManager.getHistory().size(), "Размер истории после сохранения/загрузки не совпадает");
        // Добавьте более детальное сравнение содержимого коллекций (возможно, итерация и сравнение equals())
    }

    // Методы createTestTasks() и createTestHistory() (реализуйте их в этом классе)
}