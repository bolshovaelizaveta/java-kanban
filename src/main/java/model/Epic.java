package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends model.Task {

    private final List<Integer> subtaskIds = new ArrayList<>();
    private Duration duration;
    private LocalDateTime startTime;
    private LocalDateTime endTime;


    // Конструктор для создания Epic
    public Epic(String name, String description) {
        super(name, description);
        this.duration = Duration.ZERO;
        this.startTime = null;
        this.endTime = null;
        this.status = TaskStatus.NEW;
    }

    // Конструктор для загрузки Epic
    public Epic(String name, String description, int id, TaskStatus status) {
        super(name, description, id, status);
        this.duration = Duration.ZERO;
        this.startTime = null;
        this.endTime = null;
        this.status = status;
    }


    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void addSubtaskId(int subtaskId) {
        this.subtaskIds.add(subtaskId);
    }

    public void removeSubtaskId(Integer subtaskId) {
        this.subtaskIds.remove(subtaskId);
    }

    @Override
    public Duration getDuration() {
        return duration;
    }

    @Override
    public LocalDateTime getStartTime() {
        return startTime;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public void setStatus(TaskStatus status) {
        this.status = status;
    }


    @Override
    public String toString() {
        return "Epic{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", id=" + id +
                ", status=" + status +
                ", subtaskIds=" + subtaskIds +
                ", duration=" + duration +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}