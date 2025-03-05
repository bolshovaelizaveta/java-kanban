import manager.TaskManager;
import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;

// Здесь будет много комментариев, чтобы легче исправить все замечания...
// Как только проект пройдет все проверки, то я уберу за собой мусор и закину на гитхаб нормальный вариант :)
public class Main {

    public static void main(String[] args) {

        System.out.println("Поехали!");
        TaskManager taskManager = new TaskManager();

        // Задачи
        Task task1 = new Task("Сходить в магазин", "Купить: молоко, хлеб, сыр", TaskStatus.NEW);
        taskManager.createTask(task1);
        Task task2 = new Task("Вынести мусор", "Мусоропровод через дорогу", TaskStatus.DONE);
        taskManager.createTask(task2);

        // Эпики
        Epic epic1 = new Epic("Уборка", "Привести в порядок кухню и гостиную");
        taskManager.createEpic(epic1);
        Epic epic2 = new Epic("Отдых на выходных", "Организовать досуг");
        taskManager.createEpic(epic2);

        // Подзадачи для эпика 1
        Subtask subtask1 = new Subtask("Убраться на кухне", "Помыть посуду, полы, холодильник", TaskStatus.NEW, epic1.getId());
        taskManager.createSubtask(subtask1);
        Subtask subtask2 = new Subtask("Убраться в комнате", "Погладить вещи, поменять постельное бельё", TaskStatus.NEW, epic1.getId());
        taskManager.createSubtask(subtask2);

        // Подзадачи для эпика 2
        Subtask subtask3 = new Subtask("Купить билеты", "Купить билеты на фильм Анора с Юрой Борисовым", TaskStatus.NEW, epic2.getId());
        taskManager.createSubtask(subtask3);

        // Распечатка списков
        System.out.println("Задачи: " + taskManager.getAllTasks());
        System.out.println("Эпики: " + taskManager.getAllEpics());
        System.out.println("Подзадачи: " + taskManager.getAllSubtasks());

        // Изменение статусов (UPD)
        Task task1FromManager = taskManager.getTask(task1.getId());
        Task updatedTask = new Task(task1FromManager.getName(), task1FromManager.getDescription(), TaskStatus.IN_PROGRESS);
        updatedTask.setId(task1FromManager.getId());
        taskManager.updateTask(updatedTask);

        Subtask subtask1FromManager = taskManager.getSubtask(subtask1.getId());
        Subtask updatedSubtask1 = new Subtask(subtask1FromManager.getName(), subtask1FromManager.getDescription(), TaskStatus.DONE, subtask1FromManager.getEpicId());
        updatedSubtask1.setId(subtask1FromManager.getId());
        taskManager.updateSubtask(updatedSubtask1);

        Subtask subtask2FromManager = taskManager.getSubtask(subtask2.getId());
        Subtask updatedSubtask2 = new Subtask(subtask2FromManager.getName(), subtask2FromManager.getDescription(), TaskStatus.DONE, subtask2FromManager.getEpicId());
        updatedSubtask2.setId(subtask2FromManager.getId());
        taskManager.updateSubtask(updatedSubtask2);

        Subtask subtask3FromManager = taskManager.getSubtask(subtask3.getId());
        Subtask updatedSubtask3 = new Subtask(subtask3FromManager.getName(), subtask3FromManager.getDescription(), TaskStatus.DONE, subtask3FromManager.getEpicId());
        updatedSubtask3.setId(subtask3FromManager.getId());
        taskManager.updateSubtask(updatedSubtask3);

        // Списки после изменений
        System.out.println("\nЗадачи после изменений: " + taskManager.getAllTasks());
        System.out.println("Эпики после изменений: " + taskManager.getAllEpics());
        System.out.println("Подзадачи после изменений: " + taskManager.getAllSubtasks());

        // Удаление
        taskManager.deleteTask(task1.getId());
        taskManager.deleteEpic(epic2.getId());

        // Списки после удаления
        System.out.println("\nЗадачи после удаления: " + taskManager.getAllTasks());
        System.out.println("Эпики после удаления: " + taskManager.getAllEpics());
        System.out.println("Подзадачи после удаления: " + taskManager.getAllSubtasks());
    }
}

