package model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Task {
    private final String id;
    private String title;
    private String description;
    private Category category;
    private Priority priority;
    private LocalDate deadline;
    private TaskStatus status;
    private final List<Reminder> reminders;

    public Task(String title, String description, Category category, Priority priority, LocalDate deadline) {
        this(title, description, category, priority, deadline, false);
    }

    public Task(String title, String description, Category category, Priority priority, LocalDate deadline, boolean loaded) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Task title cannot be null or empty.");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Task description cannot be null or empty.");
        }
        if (category == null) {
            throw new IllegalArgumentException("Task category cannot be null.");
        }
        if (deadline == null) {
            throw new IllegalArgumentException("Task deadline cannot be null.");
        }

        if (!loaded && deadline.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Task deadline cannot be in the past.");
        }

        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.deadline = deadline;
        this.status = TaskStatus.OPEN;
        this.reminders = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Category getCategory() {
        return category;
    }

    public Priority getPriority() {
        return priority;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public List<Reminder> getReminders() {
        return reminders;
    }

    public void setTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Task title cannot be null or empty.");
        }
        this.title = title;
    }

    public void setDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Task description cannot be null or empty.");
        }
        this.description = description;
    }

    public void setCategory(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Task category cannot be null.");
        }
        this.category = category;
    }

    public void setPriority(Priority priority) {
        if (priority == null) {
            throw new IllegalArgumentException("Task priority cannot be null.");
        }
        this.priority = priority;
    }

    public void setDeadline(LocalDate deadline) {
        if (deadline == null) {
            throw new IllegalArgumentException("Task deadline cannot be null.");
        }

        if (this.status == TaskStatus.DELAYED) {
            if (deadline.isBefore(LocalDate.now()) && !deadline.equals(this.deadline)) {
                throw new IllegalArgumentException("A delayed task can only keep its current past deadline or be set to a future date.");
            }
        } else {
            if (deadline.isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("Task deadline cannot be in the past.");
            }
        }
        this.deadline = deadline;
    }

    public void setStatus(TaskStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Task status cannot be null.");
        }
        this.status = status;
    }

    public void addReminderToTask(Reminder reminder) {
        if (reminder == null) {
            throw new IllegalArgumentException("Reminder cannot be null.");
        }

        if (!reminder.getTask().equals(this)) {
            throw new IllegalArgumentException("Reminder does not belong to this task.");
        }

        if (reminders.contains(reminder)) {
            throw new IllegalArgumentException("Reminder already exists in this task.");
        }

        for (Reminder r : reminders) {
            if (r.getReminderDate().equals(reminder.getReminderDate())) {
                throw new IllegalArgumentException(
                        String.format("A reminder with the date %s already exists for this task.", reminder.getReminderDate()));
            }
        }
        reminders.add(reminder);
    }

    public void removeReminderFromTask(Reminder reminder) {
        if (reminder == null) {
            throw new IllegalArgumentException("Reminder cannot be null.");
        }

        if (!reminder.getTask().equals(this)) {
            throw new IllegalArgumentException("Reminder does not belong to this task.");
        }

        if (!reminders.contains(reminder)) {
            throw new IllegalArgumentException("Reminder not found in this task.");
        }
        reminders.remove(reminder);
    }


    @Override
    public String toString() {
        return "Task{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", category=" + category +
                ", priority=" + priority +
                ", deadline=" + deadline +
                ", status=" + status +
                ", reminders=" + reminders +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(id, task.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
