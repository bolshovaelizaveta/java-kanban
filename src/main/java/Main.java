import manager.TaskManager;
import manager.Managers;
import model.Epic;
import model.Subtask;
import model.Task;

import java.time.Duration;
import java.time.LocalDateTime;


public class Main {

    public static void main(String[] args) {
        System.out.println("Поехали!");

        TaskManager taskManager = Managers.getDefault();


        Task task1 = new Task("Помыть посуду", "Необходимо помыть все грязные посуду");
        int task1Id = taskManager.createTask(task1);

        Task task2 = new Task("Протереть пыль", "Протереть пыль на всех поверхностях", Duration.ofMinutes(40), LocalDateTime.now());
        int task2Id = taskManager.createTask(task2);


        Epic epic1 = new Epic("Собрать вещи", "Собрать вещи и переехать в новую квартиру");
        int epic1Id = taskManager.createEpic(epic1);

        Subtask subtask1 = new Subtask("Собрать коробки", "Нужно найти и собрать все коробки для вещей", epic1Id, Duration.ofMinutes(60), LocalDateTime.now().plusHours(2));
        int subtask1Id = taskManager.createSubtask(subtask1);

        Subtask subtask2 = new Subtask("Упаковать книги", "Собрать все книги в коробки", epic1Id, Duration.ofMinutes(90), LocalDateTime.now().plusHours(4));
        int subtask2Id = taskManager.createSubtask(subtask2);

        Epic epic2 = new Epic("Подготовка к отпуску", "Запланировать и подготовиться к отпуску");
        int epic2Id = taskManager.createEpic(epic2);

        Subtask subtask3 = new Subtask("Купить билеты", "На самолет или поезд", epic2Id, Duration.ofMinutes(30), LocalDateTime.now().plusDays(2));
        int subtask3Id = taskManager.createSubtask(subtask3);


        System.out.println("Все задачи:");
        for (Task task : taskManager.getTasks()) {
            System.out.println(task);
        }
        System.out.println("Все эпики:");
        for (Epic epic : taskManager.getEpics()) {
            System.out.println(epic);
        }
        System.out.println("Все подзадачи:");
        for (Subtask subtask : taskManager.getSubtasks()) {
            System.out.println(subtask);
        }


        System.out.println("История:");
        for (Task task : taskManager.getHistory()) {
            System.out.println(task);
        }

        taskManager.getTask(task1Id);
        taskManager.getEpic(epic1Id);
        taskManager.getSubtask(subtask1Id);
        taskManager.getTask(task2Id);
        taskManager.getSubtask(subtask3Id);
        taskManager.getEpic(epic2Id);
        taskManager.getTask(task1Id);


        System.out.println("История после просмотров:");
        for (Task task : taskManager.getHistory()) {
            System.out.println(task);
        }

        taskManager.deleteTask(task1Id);
        taskManager.deleteEpic(epic1Id);


        System.out.println("Задачи после удаления:");
        for (Task task : taskManager.getTasks()) {
            System.out.println(task);
        }
        System.out.println("Эпики после удаления:");
        for (Epic epic : taskManager.getEpics()) {
            System.out.println(epic);
        }
        System.out.println("Подзадачи после удаления:");
        for (Subtask subtask : taskManager.getSubtasks()) {
            System.out.println(subtask);
        }

        System.out.println("История после удаления:");
        for (Task task : taskManager.getHistory()) {
            System.out.println(task);
        }

        System.out.println("Приоритезированные задачи:");
        for (Task task : taskManager.getPrioritizedTasks()) {
            System.out.println(task);
        }

    }
}