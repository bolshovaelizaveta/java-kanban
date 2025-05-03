package manager;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import model.TaskType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
        loadFromFile(file);
    }

    protected void save() {
        List<String> lines = new ArrayList<>();
        lines.add("id,type,name,status,description,epic");

        for (Task task : getTasksMap().values()) {
            lines.add(taskToString(task));
        }
        for (Epic epic : getEpicsMap().values()) {
            lines.add(epicToString(epic));
        }
        for (Subtask subtask : getSubtasksMap().values()) {
            lines.add(subtaskToString(subtask));
        }

        lines.add("");

        if (getHistoryManager() != null) {
            lines.add(getHistoryManager().getHistory().stream()
                    .map(Task::getId)
                    .map(String::valueOf)
                    .collect(Collectors.joining(",")));
        }

        try {
            Files.write(file.toPath(), lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении данных в файл: " + file.getAbsolutePath(), e);
        }
    }

    private String taskToString(Task task) {
        return String.format("%d,%s,%s,%s,%s,",
                task.getId(),
                TaskType.TASK,
                task.getName(),
                task.getStatus(),
                task.getDescription());
    }

    private String epicToString(Epic epic) {
        return String.format("%d,%s,%s,%s,%s,",
                epic.getId(),
                TaskType.EPIC,
                epic.getName(),
                epic.getStatus(),
                epic.getDescription());
    }

    private String subtaskToString(Subtask subtask) {
        return String.format("%d,%s,%s,%s,%s,%d",
                subtask.getId(),
                TaskType.SUBTASK,
                subtask.getName(),
                subtask.getStatus(),
                subtask.getDescription(),
                subtask.getEpicId());
    }

    private Task fromString(String value) {
        String[] parts = value.split(",");
        int id = Integer.parseInt(parts[0]);
        TaskType type = TaskType.valueOf(parts[1]);
        String name = parts[2];
        TaskStatus status = TaskStatus.valueOf(parts[3]);
        String description = parts[4];

        switch (type) {
            case TASK:
                Task task = new Task(name, description);
                task.setId(id);
                task.setStatus(status);
                return task;
            case EPIC:
                Epic epic = new Epic(name, description);
                epic.setId(id);
                epic.setStatus(status);
                return epic;
            case SUBTASK:
                int epicId = Integer.parseInt(parts[5]);
                Subtask subtask = new Subtask(name, description, epicId);
                subtask.setId(id);
                subtask.setStatus(status);
                return subtask;
            default:
                throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
        }
    }

    protected void loadFromFile(File file) {
        if (!file.exists()) {
            return;
        }
        try {
            List<String> lines = Files.readAllLines(file.toPath());
            if (lines.size() <= 1) {
                return;
            }

            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.isEmpty()) {
                    break;
                }
                Task task = fromString(line);
                if (task != null) {
                    if (task instanceof Epic) {
                        super.createEpic((Epic) task);
                    } else if (task instanceof Subtask) {
                        super.createSubtask((Subtask) task);
                    } else {
                        super.createTask(task);
                    }
                }
            }

            for (Subtask subtask : getSubtasksMap().values()) {
                Epic epic = getEpicsMap().get(subtask.getEpicId());
                if (epic != null) {
                    epic.addSubtaskId(subtask.getId());
                    super.updateEpic(epic);
                }
            }

            int historyStartIndex = lines.indexOf("");
            if (historyStartIndex != -1 && historyStartIndex < lines.size() - 1) {
                String historyLine = lines.get(historyStartIndex + 1);
                if (!historyLine.isEmpty() && getHistoryManager() != null) {
                    String[] historyIds = historyLine.split(",");
                    for (String idStr : historyIds) {
                        try {
                            int id = Integer.parseInt(idStr.trim());
                            getTask(id);
                        } catch (NumberFormatException e) {
                            System.err.println("Ошибка при чтении ID задачи из истории: " + idStr);
                        }
                    }
                }
            }

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при загрузке данных из файла: " + file.getAbsolutePath(), e);
        }
    }

    @Override
    public int createTask(Task task) {
        int id = super.createTask(task);
        save();
        return id;
    }

    @Override
    public int createEpic(Epic epic) {
        int id = super.createEpic(epic);
        save();
        return id;
    }

    @Override
    public int createSubtask(Subtask subtask) {
        int id = super.createSubtask(subtask);
        save();
        return id;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
        save();
    }

    @Override
    public void removeAllTasks() {
        super.removeAllTasks();
        save();
    }

    @Override
    public void removeAllEpics() {
        super.removeAllEpics();
        save();
    }

    @Override
    public void removeAllSubtasks() {
        super.removeAllSubtasks();
        save();
    }
}