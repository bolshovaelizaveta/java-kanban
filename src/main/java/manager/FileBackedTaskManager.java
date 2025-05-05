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
import java.util.Set;
import java.util.Collection;
import java.util.stream.Stream;


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

        if (!file.exists()) {
            return manager; // Файл не существует, возвращаем пустой менеджер
        }

        try {
            String content = Files.readString(file.toPath());
            String[] lines = content.split("\n");

            if (lines.length > 1) {
                for (int i = 1; i < lines.length; i++) {
                    String line = lines[i];
                    if (line.isBlank()) continue;

                    Optional<Task> optionalTask = fromString(line);
                    if (optionalTask.isEmpty()) {
                        System.err.println("Пропущена некорректная строка при загрузке: " + line);
                        continue;
                    }
                    Task task = optionalTask.get();

                    if (task instanceof Epic) {
                        manager.epics.put(task.getId(), (Epic) task);
                    } else if (task instanceof Subtask) {
                        manager.subtasks.put(task.getId(), (Subtask) task);
                    } else {
                        manager.tasks.put(task.getId(), task);
                    }
                }
            }

            int maxId = Stream.of(manager.tasks.keySet(), manager.epics.keySet(), manager.subtasks.keySet())
                    .flatMap(Set::stream)
                    .max(Integer::compareTo)
                    .orElse(0);

            manager.idCounter = maxId;

            for (Subtask subtask : manager.subtasks.values()) {
                Epic epic = manager.epics.get(subtask.getEpicId());
                if (epic != null) {
                    epic.addSubtaskId(subtask.getId());
                } else {
                    System.err.println("Предупреждение при загрузке: Подзадача с ID " + subtask.getId() +
                            " ссылается на несуществующий эпик с ID " + subtask.getEpicId() +
                            ". Подзадача не будет привязана к эпику.");
                }
            }

            for (Epic epic : manager.epics.values()) {
                manager.calculateEpicTimesAndStatus(epic);
            }

            manager.prioritizedTasks.clear();
            Stream.of(manager.tasks.values(), manager.epics.values(), manager.subtasks.values())
                    .flatMap(Collection::stream)
                    .filter(task -> task != null && task.getStartTime() != null)
                    .forEach(manager.prioritizedTasks::add);


        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка загрузки задач из файла: " + file.getName(), e);
        }

        return manager;
    }

    private static Optional<Task> fromString(String value) {
        String[] parts = value.split(",");
        if (parts.length < 5) {
            return Optional.empty();
        }

        try {
            int id = Integer.parseInt(parts[0]);
            TaskType type = TaskType.valueOf(parts[1]);
            String name = parts[2];
            String statusString = parts[3];
            String description = parts[4];

            TaskStatus status;
            try {
                status = TaskStatus.valueOf(statusString);
            } catch (IllegalArgumentException e) {
                System.err.println("Ошибка парсинга статуса в строке: " + value + " - " + e.getMessage());
                return Optional.empty();
            }


            LocalDateTime startTime = null;
            if (parts.length > 5 && !parts[5].isEmpty()) {
                try {
                    startTime = LocalDateTime.parse(parts[5]);
                } catch (DateTimeParseException e) {
                    System.err.println("Ошибка парсинга времени старта в строке: " + value + " - " + e.getMessage());
                    return Optional.empty();
                }
            }

            Duration duration = null;
            if (parts.length > 6 && !parts[6].isEmpty()) {
                try {
                    long minutes = Long.parseLong(parts[6]);
                    duration = Duration.ofMinutes(minutes);
                } catch (NumberFormatException e) {
                    System.err.println("Ошибка парсинга длительности в строке: " + value + " - " + e.getMessage());
                    return Optional.empty();
                }
            }


            switch (type) {
                case TASK:
                    if (parts.length > 7 && !parts[7].isEmpty()) {
                        System.err.println("Некорректный формат строки для Task: лишние поля или epicId: " + value);
                        return Optional.empty();
                    }
                    return Optional.of(new Task(name, description, id, status, duration, startTime));
                case EPIC:
                    if (parts.length > 7 && !parts[7].isEmpty()) {
                        System.err.println("Некорректный формат строки для Epic: лишние поля или epicId: " + value);
                        return Optional.empty();
                    }
                    return Optional.of(new Epic(name, description, id, status));
                case SUBTASK:
                    if (parts.length < 8 || parts[7].isEmpty()) {
                        System.err.println("Некорректный формат строки для Subtask: отсутствует или пустой epicId: " + value);
                        return Optional.empty();
                    }
                    try {
                        int epicId = Integer.parseInt(parts[7]);
                        if (parts.length > 8) {
                            System.err.println("Некорректный формат строки для Subtask: лишние поля после epicId: " + value);
                            return Optional.empty();
                        }
                        return Optional.of(new Subtask(name, description, id, status, epicId, duration, startTime));
                    } catch (NumberFormatException e) {
                        System.err.println("Ошибка парсинга epicId в строке: " + value + " - " + e.getMessage());
                        return Optional.empty();
                    }

                default:
                    System.err.println("Неизвестный тип задачи: " + type + " в строке: " + value);
                    return Optional.empty();
            }
        } catch (NumberFormatException e) {
            System.err.println("Ошибка парсинга ID в строке: " + value + " - " + e.getMessage());
            return Optional.empty();
        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка парсинга типа задачи в строке: " + value + " - " + e.getMessage());
            return Optional.empty();
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Некорректный формат строки: пропущены поля: " + value + " - " + e.getMessage());
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