package manager;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
public class TaskManager {
    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private final HashMap<Integer, Subtask> subtasks = new HashMap<>();
    private int idCounter = 0;

    private int generateId() {
        return ++idCounter;
    }

    // Методы для Task (UPD)
    public Task createTask(Task task) { // Убрала лишние параметры, принимает только объект Task
        task.setId(generateId());
        tasks.put(task.getId(), task);
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

    // Методы для Epic  (UPD)
    public Epic createEpic(Epic epic) { // Аналогично
        epic.setId(generateId());
        epics.put(epic.getId(), epic);
        return epic;
    }

    public Epic getEpic(int id) {
        return epics.get(id);
    }

    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    public void updateEpic(Epic epic) {
        epics.put(epic.getId(), epic); // удален вызов updateEpicStatus
    }

    public void deleteEpic(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            // удалила копирование коллекции
            for (int subtaskId : epic.getSubtaskIds()) {
                subtasks.remove(subtaskId); // Исправила на remove
            }
            epics.remove(id);
        }
    }

    public void deleteAllEpics() { // Исправила на вариант проще с:
        epics.clear();
        subtasks.clear();
    }


    // Методы для Subtask
    public Subtask createSubtask(Subtask subtask) { // Аналогично
        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) {
            // Исправила на return, вместо исключения
            System.out.println("Ошибка: Эпик с id " + subtask.getEpicId() + " не найден.");
            return null;
        }
        subtask.setId(generateId());
        subtasks.put(subtask.getId(), subtask);
        epic.getSubtaskIds().add(subtask.getId());
        updateEpicStatus(epic.getId());
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
        Subtask subtask = subtasks.remove(id); // Исправила на метод .remove(id);
        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.getSubtaskIds().remove((Integer) id);
                updateEpicStatus(subtask.getEpicId());
            }
        }
    }

    public void deleteAllSubtasks() {
        for (Epic epic : epics.values()) {
            epic.getSubtaskIds().clear();
            epic.setStatus(TaskStatus.NEW);
            // updateEpicStatus удалила и присваиваю статус NEW сеттером каждому эпику
        }
        subtasks.clear();
    }

    public List<Subtask> getSubtasksForEpic(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return Collections.emptyList(); // Исправлено на МОДНЫЙ вариант с:
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

        if (subtasksForEpic.isEmpty()) { // Исправлено, удалила лишнюю проверку
            epic.setStatus(TaskStatus.NEW);
            return;
        }

        boolean allNew = true;
        boolean allDone = true;

        for (Subtask subtask : subtasksForEpic) {
            // Добавила, что если хоть одна подзадача с IN_PROGRESS, то можно сразу эпику
            // ставить аналогичный статус и return
            if (subtask.getStatus() == TaskStatus.IN_PROGRESS) {
                epic.setStatus(TaskStatus.IN_PROGRESS);
                return;
            }
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

        // Исправила, использую сеттер, вместо создания нового Epic
        epic.setStatus(newStatus);
    }
        // Удалила лишнее
}