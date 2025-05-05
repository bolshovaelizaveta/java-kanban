package manager;

public class Managers {

    public static manager.TaskManager getDefault() {
        return new manager.InMemoryTaskManager();
    }

    public static manager.HistoryManager getDefaultHistory() {
        return new manager.InMemoryHistoryManager();
    }
}