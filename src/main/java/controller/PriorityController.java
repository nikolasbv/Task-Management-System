package controller;

import model.Priority;

import java.util.ArrayList;
import java.util.List;

public class PriorityController {
    private List<Priority> priorities;
    private Priority defaultPriority;

    // Constructor
    public PriorityController() {
        this.priorities = new ArrayList<>();
        this.defaultPriority = new Priority("Default");
        priorities.add(defaultPriority);
    }

    public void addPriority(Priority priority) {
        if (priority == null) {
            throw new IllegalArgumentException("Priority cannot be null.");
        }
        if (priorities.contains(priority)) {
            throw new IllegalArgumentException("Priority already exists in the system.");
        }
        if (priorities.stream().anyMatch(c -> c.getName().equalsIgnoreCase(priority.getName()))) {
            throw new IllegalArgumentException("Priority name must be unique.");
        }
        priorities.add(priority);
    }

    public void removePriority(Priority priority, TaskController taskController) {
        if (priority == null) {
            throw new IllegalArgumentException("Priority cannot be null.");
        }
        if (priority.isDefault()) {
            throw new IllegalArgumentException("Cannot delete the default priority.");
        }
        if (!priorities.contains(priority)) {
            throw new IllegalArgumentException("Priority does not exist in the system.");
        }

        taskController.getTasks().stream()
                .filter(task -> task.getPriority() == priority)
                .forEach(task -> task.setPriority(defaultPriority));

        priorities.remove(priority);
    }

    public void updatePriority(Priority priority, String newName) {
        if (priority == null || newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("Priority and new name cannot be null or empty.");
        }
        if (priority.isDefault()) {
            throw new IllegalArgumentException("Cannot rename the default priority.");
        }
        if (!priorities.contains(priority)) {
            throw new IllegalArgumentException("Priority does not exist in the system.");
        }
        if (priorities.stream().anyMatch(c -> c.getName().equalsIgnoreCase(priority.getName()))) {
            throw new IllegalArgumentException("Priority name must be unique.");
        }
        priority.setName(newName);
    }

    public List<Priority> getPriorities() {
        return new ArrayList<>(priorities);
    }

    public Priority getDefaultPriority() {
        return defaultPriority;
    }

    @Override
    public String toString() {
        return "PriorityController{" +
                "priorities=" + priorities +
                '}';
    }
}
