import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
class TaskManager {
    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private final HashMap<Integer, Subtask> subtasks = new HashMap<>();
    private int idCounter = 0;

    private int generateId() {
        return ++idCounter;
    }

    // Методы для Task
    public Task createTask(String name, String description, TaskStatus status) {
        int id = generateId();
        Task task = new Task(id, name, description, status);
        tasks.put(id, task);
        return task;
    }

    public Task getTask(int id) {
        return tasks.get(id);
    }

    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public void updateTask(Task task) {
        tasks.put(task.getId(), task);
    }

    public void deleteTask(int id) {
        tasks.remove(id);
    }

    public void deleteAllTasks() {
        tasks.clear();
    }

    // Методы для Epic
    public Epic createEpic(String name, String description) {
        int id = generateId();
        Epic epic = new Epic(id, name, description, TaskStatus.NEW);
        epics.put(id, epic);
        return epic;
    }

    public Epic getEpic(int id) {
        return epics.get(id);
    }

    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    public void updateEpic(Epic epic) {
        epics.put(epic.getId(), epic);
        updateEpicStatus(epic.getId());
    }

    public void deleteEpic(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            List<Integer> subtaskIds = new ArrayList<>(epic.getSubtaskIds());
            for (int subtaskId : subtaskIds) {
                deleteSubtask(subtaskId);
            }
            epics.remove(id);
        }
    }

    public void deleteAllEpics() {
        for (Epic epic : epics.values()) {
            List<Integer> subtaskIds = new ArrayList<>(epic.getSubtaskIds());
            for (int subtaskId : subtaskIds) {
                deleteSubtask(subtaskId);
            }
        }
        epics.clear();
    }


    // Методы для Subtask
    public Subtask createSubtask(String name, String description, TaskStatus status, int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            throw new IllegalArgumentException("Эпик с id " + epicId + " не найден.");
        }

        int id = generateId();
        Subtask subtask = new Subtask(id, name, description, status, epicId);
        subtasks.put(id, subtask);
        epic.addSubtaskId(id);
        updateEpicStatus(epicId);
        return subtask;
    }


    public Subtask getSubtask(int id) {
        return subtasks.get(id);
    }

    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    public void updateSubtask(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
        updateEpicStatus(subtask.getEpicId());
    }

    public void deleteSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtaskId(id);
                updateEpicStatus(subtask.getEpicId());
            }
            subtasks.remove(id);
        }
    }

    public void deleteAllSubtasks() {
        // Нужно пройтись по всем эпикам и удалить subtaskID из списка
        for (Epic epic : epics.values()) {
            epic.getSubtaskIds().clear();
            updateEpicStatus(epic.getId());
        }
        subtasks.clear();
    }

    public List<Subtask> getSubtasksForEpic(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return new ArrayList<>();
        }
        List<Subtask> result = new ArrayList<>();
        for (int subtaskId : epic.getSubtaskIds()) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask != null) {
                result.add(subtask);
            }
        }
        return result;
    }


    private void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return;
        }

        List<Subtask> subtasksForEpic = getSubtasksForEpic(epicId);

        if (subtasksForEpic.isEmpty()) {
            if (epic.getStatus() != TaskStatus.NEW) {
                epic = new Epic(epic.getId(), epic.getName(), epic.getDescription(), TaskStatus.NEW);
                epics.put(epic.getId(), epic);
            }
            return;
        }

        boolean allNew = true;
        boolean allDone = true;

        for (Subtask subtask : subtasksForEpic) {
            if (subtask.getStatus() != TaskStatus.NEW) {
                allNew = false;
            }
            if (subtask.getStatus() != TaskStatus.DONE) {
                allDone = false;
            }
        }

        TaskStatus newStatus;
        if (allDone) {
            newStatus = TaskStatus.DONE;
        } else if (allNew) {
            newStatus = TaskStatus.NEW;
        } else {
            newStatus = TaskStatus.IN_PROGRESS;
        }

        if (epic.getStatus() != newStatus) {
            epic = new Epic(epic.getId(), epic.getName(), epic.getDescription(), newStatus);
            epics.put(epic.getId(), epic);
        }
    }

    public HashMap<Integer, Task> getTasks() {
        return tasks;
    }

    public HashMap<Integer, Epic> getEpics() {
        return epics;
    }

    public HashMap<Integer, Subtask> getSubtasks() {
        return subtasks;
    }
}