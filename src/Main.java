import manager.InMemoryTaskManager;
import manager.Managers;
import manager.TaskManager;
import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;

public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = Managers.getDefault();

        Task task1 = new Task("Помыть посуду", "Необходимо помыть всю грязную посуду");
        int taskId1 = taskManager.createTask(task1);

        Epic epic1 = new Epic("Переезд", "Собрать вещи и переехать в новую квартиру");
        int epicId1 = taskManager.createEpic(epic1);

        Subtask subtask1 = new Subtask("Собрать коробки", "Нужно найти и собрать все коробки для вещей", epicId1);
        int subtaskId1 = taskManager.createSubtask(subtask1);

        System.out.println("История:");
        for (Task task : taskManager.getHistory()) {
            System.out.println(task);
        }

        taskManager.getTask(taskId1);
        taskManager.getEpic(epicId1);
        taskManager.getSubtask(subtaskId1);

        System.out.println("История после просмотров:");
        for (Task task : taskManager.getHistory()) {
            System.out.println(task);
        }
    }
}