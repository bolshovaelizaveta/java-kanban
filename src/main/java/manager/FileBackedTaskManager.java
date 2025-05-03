package manager;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import model.TaskType;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
<<<<<<< HEAD
        load();
=======
>>>>>>> 707e19a (Борьба с тестами)
    }

    private void save() throws ManagerSaveException {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("id,type,name,status,description,epic\n");

            for (Task task : getTasks()) {
                writer.write(taskToString(task) + "\n");
            }

            for (Epic epic : getEpics()) {
                writer.write(taskToString(epic) + "\n");
            }

            for (Subtask subtask : getSubtasks()) {
                writer.write(taskToString(subtask) + "\n");
            }

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения задач в файл: " + file.getName(), e);
        }
    }

    private String taskToString(Task task) {
<<<<<<< HEAD
        return String.format("%d,%s,%s,%s,%s",
                task.getId(),
                TaskType.TASK,
                task.getName(),
                task.getStatus(),
                task.getDescription());
    }

    private String epicToString(Epic epic) {
        return String.format("%d,%s,%s,%s,%s",
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
=======
        StringBuilder sb = new StringBuilder();
        sb.append(task.getId()).append(",");
        if (task instanceof Epic) {
            sb.append(TaskType.EPIC);
        } else if (task instanceof Subtask) {
            sb.append(TaskType.SUBTASK);
        } else {
            sb.append(TaskType.TASK);
>>>>>>> 707e19a (Борьба с тестами)
        }
        sb.append(",").append(task.getName());
        sb.append(",").append(task.getStatus());
        sb.append(",").append(task.getDescription());
        if (task instanceof Subtask) {
            sb.append(",").append(((Subtask) task).getEpicId());
        } else {
            sb.append(",");
        }
        return sb.toString();
    }

<<<<<<< HEAD
    protected void load() {
=======
    public static FileBackedTaskManager loadFromFile(File file) throws ManagerSaveException {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        int maxId = 0;

>>>>>>> 707e19a (Борьба с тестами)
        if (!file.exists()) {
            return manager;
        }

        try {
            String content = Files.readString(file.toPath());
            String[] lines = content.split("\n");

            if (lines.length > 0) {
                for (int i = 1; i < lines.length; i++) {
                    String line = lines[i];
                    if (line.isBlank()) continue;

                    Task task = fromString(line);
                    if (task == null) continue;

                    int taskId = task.getId();
                    if (taskId > maxId) {
                        maxId = taskId;
                    }

                    if (task instanceof Epic) {
                        manager.epics.put(taskId, (Epic) task);
                    } else if (task instanceof Subtask) {
                        manager.subtasks.put(taskId, (Subtask) task);
                    } else {
                        manager.tasks.put(taskId, task);
                    }
                }
            }

            for (Subtask subtask : manager.subtasks.values()) {
                Epic epic = manager.epics.get(subtask.getEpicId());
                if (epic != null) {
                    epic.addSubtaskId(subtask.getId());
                } else {
                    System.err.println("Предупреждение: Подзадача с ID " + subtask.getId() +
                            " ссылается на несуществующий эпик с ID " + subtask.getEpicId() +
                            ". Подзадача будет проигнорирована.");
                }
            }

            for (Epic epic : manager.epics.values()) {
                manager.updateEpicStatus(epic);
            }

            manager.idCounter = maxId;

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка загрузки задач из файла: " + file.getName(), e);
        }

        return manager;
    }

    private static Task fromString(String value) {
        String[] parts = value.split(",");
        if (parts.length < 5) {
            System.err.println("Некорректный формат строки для задачи: " + value);
            return null;
        }

        try {
            int id = Integer.parseInt(parts[0]);
            TaskType type = TaskType.valueOf(parts[1]);
            String name = parts[2];
            TaskStatus status = TaskStatus.valueOf(parts[3]);
            String description = parts[4];

            switch (type) {
                case TASK:
                    Task task = new Task(name, description, id);
                    task.setStatus(status);
                    return task;
                case EPIC:
                    Epic epic = new Epic(name, description, id);
                    epic.setStatus(status);
                    return epic;
                case SUBTASK:
                    if (parts.length < 6) {
                        System.err.println("Некорректный формат строки для подзадачи (отсутствует epicId): " + value);
                        return null;
                    }
                    int epicId = Integer.parseInt(parts[5]);
                    Subtask subtask = new Subtask(name, description, id, epicId);
                    subtask.setStatus(status);
                    return subtask;
                default:
                    System.err.println("Неизвестный тип задачи: " + type + " в строке: " + value);
                    return null;
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка парсинга значений в строке: " + value + " - " + e.getMessage());
            return null;
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
        if (id != -1) {
            save();
        }
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