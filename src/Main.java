public class Main {

    public static void main(String[] args) {

        System.out.println("Поехали!");
        TaskManager taskManager = new TaskManager();

        // Задачи
        Task task1 = taskManager.createTask("Сходить в магазин", "Купить: молоко, хлеб, сыр", TaskStatus.NEW);
        Task task2 = taskManager.createTask("Вынести мусор", "Мусоропровод через дорогу", TaskStatus.DONE);

        // Эпики
        Epic epic1 = taskManager.createEpic("Уборка", "Привести в порядок кухню и гостиную");
        Epic epic2 = taskManager.createEpic("Отдых на выходных", "Организовать досуг");

        // Подзадачи для эпика 1
        Subtask subtask1 = taskManager.createSubtask("Убраться на кухне", "Помыть посуду, полы, холодильник", TaskStatus.NEW, epic1.getId());
        Subtask subtask2 = taskManager.createSubtask("Убраться в комнате", "Погладить вещи, поменять постельное бельё", TaskStatus.NEW, epic1.getId());

        // Подзадачи для эпика 2
        Subtask subtask3 = taskManager.createSubtask("Купить билеты", "Купить билеты на фильм Анора с Юрой Борисовым", TaskStatus.NEW, epic2.getId());

        // Распечатка списков
        System.out.println("Задачи: " + taskManager.getAllTasks());
        System.out.println("Эпики: " + taskManager.getAllEpics());
        System.out.println("Подзадачи: " + taskManager.getAllSubtasks());

        // Изменение статусов
        task1.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateTask(task1);
        subtask1.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(subtask1);
        subtask2.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(subtask2);
        subtask3.setStatus(TaskStatus.DONE);
        taskManager.updateSubtask(subtask3);

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

