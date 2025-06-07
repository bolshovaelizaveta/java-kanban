package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private List<Integer> subtaskIds = new ArrayList<>();
    private LocalDateTime endTime = null;

    public Epic(String name, String description) {
        super(name, description);
        this.status = TaskStatus.NEW;
        this.duration = Duration.ZERO;
        this.startTime = null;
    }

    public Epic(String name, String description, int id, TaskStatus status) {
        super(name, description, id, status);
        this.duration = Duration.ZERO;
        this.startTime = null;
    }

    // Это для Gson, чтобы не было больше null. Иначе NullPointerException заставляет дёргаться мой глаз
    public Epic() {
        super(null, null); // Вызов конструктора "родителя"
        this.status = TaskStatus.NEW;
        this.duration = Duration.ZERO;
        this.startTime = null;
    }


    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void addSubtaskId(int subtaskId) {
        subtaskIds.add(subtaskId);
    }

    public void removeSubtaskId(int subtaskId) {
        subtaskIds.remove((Integer) subtaskId);
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public Duration getDuration() {
        return duration;
    }

    @Override
    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setCalculatedDuration(Duration duration) {
        this.duration = duration;
    }

    public void setCalculatedStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", subtaskIds=" + subtaskIds +
                ", duration=" + duration +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}