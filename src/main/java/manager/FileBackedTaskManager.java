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
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;


public class FileBackedTaskManager extends InMemoryTaskManager {

    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    private void save() throws ManagerSaveException {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("id,type,name,status,description,startTime,duration,epic\n");

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

    // Метод для преобразования задачи в строку для файла
    private String taskToString(Task task) {
        StringBuilder sb = new StringBuilder();
        sb.append(task.getId()).append(",");
        if (task instanceof Epic) {
            sb.append(TaskType.EPIC);
        } else if (task instanceof Subtask) {
            sb.append(TaskType.SUBTASK);
        } else {
            sb.append(TaskType.TASK);
        }
        sb.append(",").append(task.getName());
        sb.append(",").append(task.getStatus());
        sb.append(",").append(task.getDescription());

        sb.append(",");
        if (task.getStartTime() != null) {
            sb.append(task.getStartTime());
        }
        sb.append(",");
        if (task.getDuration() != null) {
            sb.append(task.getDuration().toMinutes());
        }

        if (task instanceof Subtask) {
            sb.append(",").append(((Subtask) task).getEpicId());
        } else {
            sb.append(",");
        }
        return sb.toString();
    }

    public static FileBackedTaskManager loadFromFile(File file) throws ManagerSaveException {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        int maxId = 0;

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

                    Optional<Task> optionalTask = fromString(line);
                    if (optionalTask.isEmpty()) continue;
                    Task task = optionalTask.get();

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
                    System.err.println("Предупреждение при загрузке: Подзадача с ID " + subtask.getId() +
                            " ссылается на несуществующий эпик с ID " + subtask.getEpicId() +
                            ". Подзадача может быть некорректно связана.");
                }
            }

            for (Epic epic : manager.epics.values()) {
                manager.calculateEpicTimesAndStatus(epic);
            }

            for (Task task : manager.tasks.values()) {
                if (task.getStartTime() != null) {
                    manager.prioritizedTasks.add(task);
                }
            }
            for (Subtask subtask : manager.subtasks.values()) {
                if (subtask.getStartTime() != null) {
                    manager.prioritizedTasks.add(subtask);
                }
            }

            manager.idCounter = maxId;

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка загрузки задач из файла: " + file.getName(), e);
        }

        return manager;
    }

    // Метод для преобразования строки из файла в объект задачи
    private static Optional<Task> fromString(String value) {
        String[] parts = value.split(",");
        if (parts.length < 5) {
            System.err.println("Некорректный формат строки: недостаточно основных полей: " + value);
            return Optional.empty();
        }


        try {
            int id = Integer.parseInt(parts[0]);
            TaskType type = TaskType.valueOf(parts[1]);
            String name = parts[2];
            TaskStatus status = TaskStatus.valueOf(parts[3]);
            String description = parts[4];

            LocalDateTime startTime = null;
            if (parts.length > 5 && !parts[5].isEmpty()) {
                try {
                    startTime = LocalDateTime.parse(parts[5]);
                } catch (DateTimeParseException e) {
                    System.err.println("Ошибка парсинга времени старта в строке: " + value + " - " + e.getMessage());
                }
            }

            Duration duration = null;
            if (parts.length > 6 && !parts[6].isEmpty()) {
                try {
                    long minutes = Long.parseLong(parts[6]);
                    duration = Duration.ofMinutes(minutes);
                } catch (NumberFormatException e) {
                    System.err.println("Ошибка парсинга длительности в строке: " + value + " - " + e.getMessage());
                }
            }


            switch (type) {
                case TASK:
                    Task task = new Task(name, description, id, status, duration, startTime);
                    return Optional.of(task);
                case EPIC:
                    Epic epic = new Epic(name, description, id, status);
                    return Optional.of(epic);
                case SUBTASK:
                    if (parts.length < 8) {
                        System.err.println("Некорректный формат строки для Subtask: отсутствует epicId: " + value);
                        return Optional.empty();
                    }
                    int epicId = Integer.parseInt(parts[7]);
                    Subtask subtask = new Subtask(name, description, id, status, epicId, duration, startTime);
                    return Optional.of(subtask);
                default:
                    System.err.println("Неизвестный тип задачи: " + type + " в строке: " + value);
                    return Optional.empty();
            }
        } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
            System.err.println("Ошибка парсинга строки: " + value + " - " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public int createTask(Task task) {
        int id = super.createTask(task);
        if (id != -1) {
            save();
        }
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