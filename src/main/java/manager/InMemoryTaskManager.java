package manager;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Comparator;
import java.util.TreeSet;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.HashSet;


public class InMemoryTaskManager implements TaskManager {
    protected final HashMap<Integer, Task> tasks = new HashMap<>();
    protected final HashMap<Integer, Epic> epics = new HashMap<>();
    protected final HashMap<Integer, Subtask> subtasks = new HashMap<>();
    protected int idCounter = 0;
    private final HistoryManager historyManager = Managers.getDefaultHistory();

    protected final TreeSet<Task> prioritizedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime));


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
            historyManager.remove(id);
            if (task.getStartTime() != null) {
                prioritizedTasks.remove(task);
            }
        }
    }

    @Override
    public void deleteEpic(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            historyManager.remove(id);
            if (epic.getStartTime() != null) {
                prioritizedTasks.remove(epic);
            }
            for (int subtaskId : epic.getSubtaskIds()) {
                Subtask subtask = subtasks.remove(subtaskId);
                if (subtask != null) {
                    historyManager.remove(subtaskId);
                    if (subtask.getStartTime() != null) {
                        prioritizedTasks.remove(subtask);
                    }
                }
            }
        }
    }

    @Override
    public void deleteSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            historyManager.remove(id);
            if (subtask.getStartTime() != null) {
                prioritizedTasks.remove(subtask);
            }
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtaskId((Integer) subtask.getId());
                calculateEpicTimesAndStatus(epic);
            }
        }
    }

    @Override
    public void updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            Task oldTask = tasks.get(task.getId());

            boolean wasInPrioritized = false;
            if (oldTask != null && oldTask.getStartTime() != null) {
                wasInPrioritized = prioritizedTasks.remove(oldTask);
            }

            boolean hasOverlap = hasIntersections(task);

            if (hasOverlap) {
                System.out.println("Ошибка: Обновление задачи " + task.getId() + " приведет к пересечению интервалов выполнения.");
                if (wasInPrioritized) {
                    prioritizedTasks.add(oldTask);
                }
                return;
            }

            tasks.put(task.getId(), task);
            if (task.getStartTime() != null) {
                prioritizedTasks.add(task);
            } else if (oldTask != null && oldTask.getStartTime() != null) {
                prioritizedTasks.remove(task);
            }


        } else {
            System.out.println("Задача с ID " + task.getId() + " не найдена или не является Task.");
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            Epic existingEpic = epics.get(epic.getId());
            existingEpic.setName(epic.getName());
            existingEpic.setDescription(epic.getDescription());
            // existingEpic.setStatus(epic.getStatus()); // Статус эпика рассчитывается, не устанавливается напрямую

            calculateEpicTimesAndStatus(existingEpic); // Пересчет статуса и времени

            if (existingEpic.getStartTime() != null) {
                prioritizedTasks.add(existingEpic);
            } else {
                prioritizedTasks.remove(existingEpic);
            }


        } else {
            System.out.println("Epic с ID " + epic.getId() + " не найден.");
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtasks.containsKey(subtask.getId()) && epics.containsKey(subtask.getEpicId())) {
            Subtask oldSubtask = subtasks.get(subtask.getId());

            boolean wasInPrioritized = false;
            if (oldSubtask != null && oldSubtask.getStartTime() != null) {
                wasInPrioritized = prioritizedTasks.remove(oldSubtask);
            }

            boolean hasOverlap = hasIntersections(subtask);

            if (hasOverlap) {
                System.out.println("Ошибка: Обновление подзадачи " + subtask.getId() + " приведет к пересечению интервалов выполнения.");
                if (wasInPrioritized) {
                    prioritizedTasks.add(oldSubtask);
                }
                calculateEpicTimesAndStatus(epics.get(subtask.getEpicId()));
                return;
            }


            subtasks.put(subtask.getId(), subtask);
            if (subtask.getStartTime() != null) {
                prioritizedTasks.add(subtask);
            } else if (oldSubtask != null && oldSubtask.getStartTime() != null) {
                prioritizedTasks.remove(subtask);
            }


            Epic epic = epics.get(subtask.getEpicId());
            calculateEpicTimesAndStatus(epic);
        } else {
            System.out.println("Подзадача с ID " + subtask.getId() + " не найдена или Epic с ID " + subtask.getEpicId() + " не существует.");
        }
    }


    @Override
    public int createTask(Task task) {
        if (task.getStartTime() != null && task.getDuration() != null && hasIntersections(task)) {
            System.out.println("Ошибка: Создание задачи приведет к пересечению интервалов выполнения.");
            return -1;
        }

        task.setId(generateId());
        tasks.put(task.getId(), task);
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
        return task.getId();
    }

    @Override
    public int createEpic(Epic epic) {
        epic.setId(generateId());
        epics.put(epic.getId(), epic);
        return epic.getId();
    }

    @Override
    public int createSubtask(Subtask subtask) {
        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) {
            System.out.println("Невозможно создать подзадачу без Эпика.");
            return -1;
        }

        if (subtask.getStartTime() != null && subtask.getDuration() != null && hasIntersections(subtask)) {
            System.out.println("Ошибка: Создание подзадачи приведет к пересечению интервалов выполнения.");
            return -1;
        }


        subtask.setId(generateId());
        subtasks.put(subtask.getId(), subtask);
        epic.addSubtaskId(subtask.getId());
        if (subtask.getStartTime() != null) {
            prioritizedTasks.add(subtask);
        }
        calculateEpicTimesAndStatus(epic);
        return subtask.getId();
    }

    @Override
    public List<Subtask> getEpicSubtasks(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return new ArrayList<>();
        }
        return epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public void removeAllTasks() {
        tasks.clear();
        epics.clear();
        subtasks.clear();
        prioritizedTasks.clear();
        historyManager.removeAll();
    }


    @Override
    public void removeAllEpics() {
        epics.clear();
        subtasks.clear(); // Подзадачи эпиков тоже удаляются
        prioritizedTasks.clear(); // Очищаем prioritizedTasks полностью
        historyManager.removeAll();
    }

    @Override
    public void removeAllSubtasks() {
        // 1. Собираем ID эпиков, у которых есть подзадачи
        Set<Integer> affectedEpicIds = new HashSet<>();
        for (Subtask subtask : subtasks.values()) {
            affectedEpicIds.add(subtask.getEpicId());
        }

        // 2. Собираем ID подзадач для удаления из истории
        Set<Integer> subtaskIdsToRemove = new HashSet<>(subtasks.keySet());

        // 3. Очищаем карту подзадач
        subtasks.clear();

        // 4. Удаляем только подзадачи из приоритезированного списка
        prioritizedTasks.removeIf(task -> task instanceof Subtask);

        // 5. Удаляем подзадачи из истории
        for (Integer subtaskId : subtaskIdsToRemove) {
            historyManager.remove(subtaskId);
        }


        // 6. Для каждого затронутого эпика очищаем его список подзадач и пересчитываем
        for (Integer epicId : affectedEpicIds) {
            Epic epic = epics.get(epicId);
            if (epic != null) {
                epic.clearSubtaskIds(); // Очищаем список ID подзадач у эпика
                calculateEpicTimesAndStatus(epic); // Пересчет обновит эпик в prioritizedTasks
            }
        }
    }


    protected int generateId() {
        return ++idCounter;
    }

    protected void calculateEpicTimesAndStatus(Epic epic) {
        List<Integer> subtaskIds = epic.getSubtaskIds();
        List<Subtask> epicSubtasks = subtaskIds.stream()
                .map(subtasks::get)
                .filter(java.util.Objects::nonNull) // Фильтруем null, если подзадача уже удалена из карты
                .collect(Collectors.toList());


        if (epicSubtasks.isEmpty()) {
            epic.setStartTime(null);
            epic.setEndTime(null);
            epic.setDuration(Duration.ZERO);
            epic.setStatus(TaskStatus.NEW);
            prioritizedTasks.remove(epic); // Удаляем эпик из prioritizedTasks, если у него нет времени
            return;
        }

        LocalDateTime earliestStartTime = null;
        LocalDateTime latestEndTime = null;
        Duration totalDuration = Duration.ZERO;

        boolean allNew = true;
        boolean allDone = true;
        boolean inProgress = false;

        // Рассчитываем время только по подзадачам с заданным временем
        List<Subtask> subtasksWithTime = epicSubtasks.stream()
                .filter(sub -> sub.getStartTime() != null && sub.getDuration() != null)
                .collect(Collectors.toList());

        if (!subtasksWithTime.isEmpty()) {
            earliestStartTime = subtasksWithTime.stream()
                    .map(Task::getStartTime)
                    .min(LocalDateTime::compareTo)
                    .orElse(null);


            latestEndTime = subtasksWithTime.stream()
                    .map(Task::getEndTime)
                    .max(LocalDateTime::compareTo)
                    .orElse(null);


            totalDuration = subtasksWithTime.stream()
                    .map(Task::getDuration)
                    .reduce(Duration.ZERO, Duration::plus);

        } else {
            // Если подзадач с временем нет, сбрасываем время и длительность эпика
            epic.setStartTime(null);
            epic.setEndTime(null);
            epic.setDuration(Duration.ZERO);
        }


        for (Subtask subtask : epicSubtasks) {
            TaskStatus status = subtask.getStatus();
            if (status != TaskStatus.DONE) {
                allDone = false;
            }
            if (status != TaskStatus.NEW) {
                allNew = false;
            }
            if (status == TaskStatus.IN_PROGRESS) {
                inProgress = true;
            }
        }


        epic.setStartTime(earliestStartTime);
        epic.setEndTime(latestEndTime);
        epic.setDuration(totalDuration);


        if (allNew) {
            epic.setStatus(TaskStatus.NEW);
        } else if (allDone) {
            epic.setStatus(TaskStatus.DONE);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }

        // Обновляем эпик в prioritizedTasks
        prioritizedTasks.remove(epic); // Удаляем старую версию (по ID)
        if (epic.getStartTime() != null) {
            prioritizedTasks.add(epic); // Добавляем, если есть время
        }
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    protected boolean isIntersecting(Task task1, Task task2) {
        if (task1.getStartTime() == null || task1.getDuration() == null || task2.getStartTime() == null || task2.getDuration() == null) {
            return false;
        }

        LocalDateTime start1 = task1.getStartTime();
        LocalDateTime end1 = task1.getEndTime();

        LocalDateTime start2 = task2.getStartTime();
        LocalDateTime end2 = task2.getEndTime();

        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    protected boolean hasIntersections(Task task) {
        if (task.getStartTime() == null || task.getDuration() == null) {
            return false;
        }

        return prioritizedTasks.stream()
                .filter(existingTask -> existingTask.getId() != task.getId())
                .anyMatch(existingTask -> isIntersecting(task, existingTask));
    }
}