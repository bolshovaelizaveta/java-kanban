package manager;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import model.TaskType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    protected void save() {
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

            writer.write("\n");
            writer.write(historyToString(getHistory()));
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения задач в файл: " + file.getName(), e);
        }
    }

    private String taskToString(Task task) {
        StringBuilder csvLineBuilder = new StringBuilder();

        csvLineBuilder.append(task.getId()).append(",");

        if (task instanceof Epic) {
            csvLineBuilder.append(TaskType.EPIC);
        } else if (task instanceof Subtask) {
            csvLineBuilder.append(TaskType.SUBTASK);
        } else {
            csvLineBuilder.append(TaskType.TASK);
        }
        csvLineBuilder.append(",").append(task.getName());
        csvLineBuilder.append(",").append(task.getStatus());
        csvLineBuilder.append(",").append(task.getDescription());

        csvLineBuilder.append(",");
        if (task.getStartTime() != null) {
            csvLineBuilder.append(task.getStartTime());
        } else {
            csvLineBuilder.append("");
        }
        csvLineBuilder.append(",");
        if (task.getDuration() != null) {
            csvLineBuilder.append(task.getDuration().toMinutes());
        } else {
            csvLineBuilder.append("");
        }

        if (task instanceof Subtask) {
            csvLineBuilder.append(",").append(((Subtask) task).getEpicId());
        } else {
            csvLineBuilder.append(",");
        }
        return csvLineBuilder.toString();
    }

    public static FileBackedTaskManager loadFromFile(File file) throws ManagerSaveException {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        String historyLine = null;

        if (!file.exists()) {
            return manager;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine();

            String line;
            List<String> taskLines = new ArrayList<>();
            boolean readingHistory = false;

            while ((line = br.readLine()) != null) {
                if (line.isBlank()) {
                    readingHistory = true;
                    continue;
                }
                if (readingHistory) {
                    historyLine = line;
                } else {
                    taskLines.add(line);
                }
            }

            for (String taskLine : taskLines) {
                Optional<Task> optionalTask = fromString(taskLine);
                if (optionalTask.isEmpty()) {
                    System.err.println("Пропущена некорректная строка при загрузке: " + taskLine);
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
                if (task.getId() >= manager.idCounter) {
                    manager.idCounter = task.getId() + 1;
                }
            }

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

            if (historyLine != null && !historyLine.trim().isEmpty()) {
                List<Integer> idsInHistory = historyFromString(historyLine);
                for (Integer id : idsInHistory) {
                    if (manager.tasks.containsKey(id)) {
                        manager.getTask(id);
                    } else if (manager.epics.containsKey(id)) {
                        manager.getEpic(id);
                    } else if (manager.subtasks.containsKey(id)) {
                        manager.getSubtask(id);
                    }
                }
            }

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка загрузки задач из файла: " + file.getName(), e);
        }

        return manager;
    }

    private static Optional<Task> fromString(String value) {
        String[] parts = value.split(",");
        if (parts.length < 5) {
            System.err.println("Некорректная строка (слишком короткая): " + value);
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
            System.err.println("Ошибка парсинга числовых полей в строке: " + value + " - " + e.getMessage());
            return Optional.empty();
        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка парсинга типа или статуса задачи в строке: " + value + " - " + e.getMessage());
            return Optional.empty();
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Некорректный формат строки: пропущены поля: " + value + " - " + e.getMessage());
            return Optional.empty();
        }
    }

    private String historyToString(List<Task> history) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < history.size(); i++) {
            sb.append(history.get(i).getId());
            if (i < history.size() - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    private static List<Integer> historyFromString(String value) {
        List<Integer> ids = new ArrayList<>();
        if (value == null || value.trim().isEmpty()) {
            return ids;
        }
        String[] idStrings = value.split(",");
        for (String idStr : idStrings) {
            try {
                ids.add(Integer.parseInt(idStr.trim()));
            } catch (NumberFormatException e) {
                System.err.println("Ошибка парсинга ID в строке истории: " + idStr + " - " + e.getMessage());
            }
        }
        return ids;
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

    @Override
    public Task getTask(int id) {
        Task task = super.getTask(id);
        save();
        return task;
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = super.getEpic(id);
        save();
        return epic;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = super.getSubtask(id);
        save();
        return subtask;
    }
}