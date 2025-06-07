package model;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {
    private int epicId;

    public Subtask(String name, String description, TaskStatus status, int epicId, Duration duration, LocalDateTime startTime) {
        super(name, description, duration, startTime);
        this.status = status;
        this.epicId = epicId;
    }

    public Subtask(String name, String description, int id, TaskStatus status, int epicId, Duration duration, LocalDateTime startTime) {
        super(name, description, id, status, duration, startTime); // Вызов конструктора Task с id, временем и длительностью
        this.epicId = epicId;
    }

    // Исправила конструктор для Gson
    public Subtask() {
        super(null, null);
        this.status = TaskStatus.NEW;
        this.epicId = 0;
        this.duration = Duration.ZERO;
        this.startTime = null;
    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", epicId=" + epicId +
                ", duration=" + duration +
                ", startTime=" + startTime +
                ", endTime=" + getEndTime() +
                '}';
    }
}