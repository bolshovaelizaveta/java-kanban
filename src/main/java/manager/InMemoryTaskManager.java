package manager;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;


public class InMemoryTaskManager implements TaskManager {

    protected final Map<Integer, Task> tasks;
    protected final Map<Integer, Epic> epics;
    protected final Map<Integer, Subtask> subtasks;
    protected int idCounter;
    protected final HistoryManager historyManager;
    protected final Set<Task> prioritizedTasks;


    public HistoryManager getHistoryManager() {
        return historyManager;
    }

    public InMemoryTaskManager() {
        this.tasks = new HashMap<>();
        this.epics = new HashMap<>();
        this.subtasks = new HashMap<>();
        this.idCounter = 0;
        this.historyManager = Managers.getDefaultHistory();
        this.prioritizedTasks = new TreeSet<>(
                Comparator.comparing(Task::getStartTime, Comparator.nullsLast(LocalDateTime::compareTo))
                        .thenComparingInt(Task::getId));
    }


    @Override
    public int createTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Ошибка: Нельзя создать null задачу.");
        }
        if (task.getStartTime() != null && task.getDuration() != null) {
            if (hasIntersections(task)) {
                throw new ManagerSaveException("Создание задачи приведет к пересечению интервалов выполнения.");
            }
        }

        int id = generateId();
        task.setId(id);
        tasks.put(id, task);
        if (task.getStartTime() != null && task.getDuration() != null) {
            prioritizedTasks.add(task);
        }
        return id;
    }

    @Override
    public int createEpic(Epic epic) {
        if (epic == null) {
            throw new IllegalArgumentException("Ошибка: Нельзя создать null эпик.");
        }
        int id = generateId();
        epic.setId(id);
        epics.put(id, epic);
        return id;
    }

    @Override
    public int createSubtask(Subtask subtask) {
        if (subtask == null) {
            throw new IllegalArgumentException("Ошибка: Нельзя создать null подзадачу.");
        }
        if (!epics.containsKey(subtask.getEpicId())) {
            throw new IllegalArgumentException("Невозможно создать подзадачу без существующего Эпика.");
        }

        if (subtask.getStartTime() != null && subtask.getDuration() != null) {
            if (hasIntersections(subtask)) {
                throw new ManagerSaveException("Создание подзадачи приведет к пересечению интервалов выполнения.");
            }
        }

        int id = generateId();
        subtask.setId(id);
        subtasks.put(id, subtask);

        Epic epic = epics.get(subtask.getEpicId());
        epic.addSubtaskId(id);

        calculateEpicTimesAndStatus(epic);

        if (subtask.getStartTime() != null && subtask.getDuration() != null) {
            prioritizedTasks.add(subtask);
        }

        return id;
    }

    @Override
    public void updateTask(Task task) {
        if (task == null || !tasks.containsKey(task.getId())) {
            throw new IllegalArgumentException("Ошибка: Невозможно обновить несуществующую задачу или задача null.");
        }
        Task existingTask = tasks.get(task.getId());

        boolean wasInPrioritized = false;
        if (existingTask.getStartTime() != null && existingTask.getDuration() != null) {
            wasInPrioritized = prioritizedTasks.remove(existingTask);
        }

        if (task.getStartTime() != null && task.getDuration() != null) {
            if (hasIntersections(task)) {
                if (wasInPrioritized) {
                    prioritizedTasks.add(existingTask);
                }
                throw new ManagerSaveException("Обновление задачи приведет к пересечению интервалов выполнения.");
            }
        }

        if (task.getStartTime() != null && task.getDuration() != null) {
            prioritizedTasks.add(task);
        }

        tasks.put(task.getId(), task);
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epic == null || !epics.containsKey(epic.getId())) {
            throw new IllegalArgumentException("Ошибка: Невозможно обновить несуществующий эпик или эпик null.");
        }
        Epic existingEpic = epics.get(epic.getId());
        existingEpic.setName(epic.getName());
        existingEpic.setDescription(epic.getDescription());
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtask == null || !subtasks.containsKey(subtask.getId())) {
            throw new IllegalArgumentException("Ошибка: Невозможно обновить несуществующую подзадачу или подзадача null.");
        }
        if (!epics.containsKey(subtask.getEpicId())) {
            throw new IllegalArgumentException("Ошибка: Невозможно обновить подзадачу, у которой нет существующего эпика: " + subtask.getId());
        }

        Subtask existingSubtask = subtasks.get(subtask.getId());

        boolean wasInPrioritized = false;
        if (existingSubtask.getStartTime() != null && existingSubtask.getDuration() != null) {
            wasInPrioritized = prioritizedTasks.remove(existingSubtask);
        }

        if (subtask.getStartTime() != null && subtask.getDuration() != null) {
            if (hasIntersections(subtask)) {
                if (wasInPrioritized) {
                    prioritizedTasks.add(existingSubtask);
                }
                throw new ManagerSaveException("Обновление подзадачи приведет к пересечению интервалов выполнения.");
            }
        }

        if (subtask.getStartTime() != null && subtask.getDuration() != null) {
            prioritizedTasks.add(subtask);
        }

        subtasks.put(subtask.getId(), subtask);

        Epic epic = epics.get(subtask.getEpicId());
        calculateEpicTimesAndStatus(epic);
    }


    @Override
    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return subtask;
    }

    @Override
    public void deleteTask(int id) {
        Task task = tasks.remove(id);
        if (task != null) {
            if (task.getStartTime() != null && task.getDuration() != null) {
                prioritizedTasks.remove(task);
            }
            historyManager.remove(id);
        }
    }

    @Override
    public void deleteEpic(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            for (int subtaskId : new ArrayList<>(epic.getSubtaskIds())) {
                deleteSubtask(subtaskId);
            }
            if (epic.getStartTime() != null && epic.getDuration() != null) {
                prioritizedTasks.remove(epic);
            }
            historyManager.remove(id);
        }
    }

    @Override
    public void deleteSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            if (subtask.getStartTime() != null && subtask.getDuration() != null) {
                prioritizedTasks.remove(subtask);
            }
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtaskId(id);
                calculateEpicTimesAndStatus(epic);
                prioritizedTasks.remove(epic);
                if (epic.getStartTime() != null && epic.getDuration() != null) {
                    prioritizedTasks.add(epic);
                }
            }
            historyManager.remove(id);
        }
    }


    @Override
    public void removeAllTasks() {
        for (Integer taskId : new ArrayList<>(tasks.keySet())) {
            historyManager.remove(taskId);
        }
        tasks.clear();
        prioritizedTasks.removeIf(task -> task instanceof Task && !(task instanceof Epic) && !(task instanceof Subtask));
    }

    @Override
    public void removeAllEpics() {
        Set<Integer> idsToRemoveFromHistory = new HashSet<>();
        for (Epic epic : epics.values()) {
            idsToRemoveFromHistory.add(epic.getId());
            for (int subtaskId : new ArrayList<>(epic.getSubtaskIds())) {
                Subtask subtask = subtasks.get(subtaskId);
                if (subtask != null) {
                    if (subtask.getStartTime() != null && subtask.getDuration() != null) {
                        prioritizedTasks.remove(subtask);
                    }
                    historyManager.remove(subtaskId);
                    subtasks.remove(subtaskId);
                }
            }
            if (epic.getStartTime() != null && epic.getDuration() != null) {
                prioritizedTasks.remove(epic);
            }
        }

        for (Integer id : idsToRemoveFromHistory) {
            historyManager.remove(id);
        }
        epics.clear();
        prioritizedTasks.removeIf(task -> task instanceof Subtask || task instanceof Epic);
        subtasks.clear();
    }

    @Override
    public void removeAllSubtasks() {
        Set<Integer> epicIdsToUpdate = new HashSet<>();
        for (Subtask subtask : subtasks.values()) {
            epicIdsToUpdate.add(subtask.getEpicId());
        }

        Set<Integer> subtaskIdsToRemoveFromHistory = new HashSet<>(subtasks.keySet());

        subtasks.clear();
        prioritizedTasks.removeIf(task -> task instanceof Subtask);

        for (int epicId : epicIdsToUpdate) {
            Epic epic = epics.get(epicId);
            if (epic != null) {
                epic.getSubtaskIds().clear();
                calculateEpicTimesAndStatus(epic);
                prioritizedTasks.remove(epic);
                if (epic.getStartTime() != null && epic.getDuration() != null) {
                    prioritizedTasks.add(epic);
                }
            }
        }
        for (Integer subtaskId : subtaskIdsToRemoveFromHistory) {
            historyManager.remove(subtaskId);
        }
    }


    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    protected void calculateEpicTimesAndStatus(Epic epic) {
        if (epic == null) {
            return;
        }

        List<Integer> subtaskIds = epic.getSubtaskIds();
        if (subtaskIds.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            epic.setCalculatedDuration(Duration.ZERO);
            epic.setCalculatedStartTime(null);
            epic.setEndTime(null);
            prioritizedTasks.remove(epic);
            return;
        }

        boolean allNew = true;
        boolean allDone = true;
        LocalDateTime earliestStartTime = null;
        LocalDateTime latestEndTime = null;
        Duration totalDuration = Duration.ZERO;


        for (int subtaskId : subtaskIds) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask != null) {
                if (subtask.getStatus() != TaskStatus.NEW) {
                    allNew = false;
                }
                if (subtask.getStatus() != TaskStatus.DONE) {
                    allDone = false;
                }

                if (subtask.getStartTime() != null && subtask.getDuration() != null) {
                    if (earliestStartTime == null || subtask.getStartTime().isBefore(earliestStartTime)) {
                        earliestStartTime = subtask.getStartTime();
                    }
                    LocalDateTime subtaskEndTime = subtask.getEndTime();
                    if (latestEndTime == null || subtaskEndTime.isAfter(latestEndTime)) {
                        latestEndTime = subtaskEndTime;
                    }
                    totalDuration = totalDuration.plus(subtask.getDuration());
                } else if (subtask.getDuration() != null) {
                    totalDuration = totalDuration.plus(subtask.getDuration());
                }
            }
        }

        if (allNew) {
            epic.setStatus(TaskStatus.NEW);
        } else if (allDone) {
            epic.setStatus(TaskStatus.DONE);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }

        epic.setCalculatedDuration(totalDuration);
        epic.setCalculatedStartTime(earliestStartTime);
        epic.setEndTime(latestEndTime);


        prioritizedTasks.remove(epic);
        if (epic.getStartTime() != null && epic.getDuration() != null) {
            prioritizedTasks.add(epic);
        }
    }


    protected int generateId() {
        return ++idCounter;
    }

    protected boolean hasIntersections(Task newTask) {

        if (newTask.getStartTime() == null || newTask.getDuration() == null) {
            return false;
        }

        LocalDateTime newStart = newTask.getStartTime();
        LocalDateTime newEnd = newTask.getEndTime();

        for (Task existingTask : prioritizedTasks) {

            if (newTask.getId() != 0 && newTask.getId() == existingTask.getId()) {
                continue;
            }

            if (existingTask.getStartTime() == null || existingTask.getDuration() == null) {
                continue;
            }

            LocalDateTime existingStart = existingTask.getStartTime();
            LocalDateTime existingEnd = existingTask.getEndTime();

            if (newStart.isBefore(existingEnd) && existingStart.isBefore(newEnd)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    @Override
    public List<Subtask> getEpicSubtasks(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return new ArrayList<>();
        }

        List<Subtask> epicSubtasks = new ArrayList<>();
        for (int subtaskId : epic.getSubtaskIds()) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask != null) {
                epicSubtasks.add(subtask);
            }
        }
        return epicSubtasks;
    }
}