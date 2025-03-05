package model;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task { // У меня почему-то idea начала ругаться, начала чушью заниматься, а потом забыла вернуть наследование...
    // Всё вернула, всё работает :)
    private final List<Integer> subtaskIds = new ArrayList<>();

    public Epic(String name, String description) {
        super(name, description, TaskStatus.NEW);
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

}