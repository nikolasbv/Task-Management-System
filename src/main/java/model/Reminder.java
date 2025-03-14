package model;

import java.time.LocalDate;
import java.util.Objects;

public class Reminder {
    private LocalDate reminderDate;
    private ReminderType type;
    private final Task task;

    public Reminder(ReminderType type, Task task, LocalDate customDate) {
        if (type == null) {
            throw new IllegalArgumentException("Reminder type cannot be null.");
        }
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null.");
        }

        this.type = type;
        this.task = task;

        if (type == ReminderType.CUSTOM_DATE) {
            if (customDate == null) {
                throw new IllegalArgumentException("Custom reminders must have a specific date.");
            }
            this.reminderDate = customDate;
        } else {
            this.reminderDate = calculateReminderDate(type);
        }

        validateProposedValues(this.type, this.reminderDate);
    }

    public LocalDate getReminderDate() {
        return reminderDate;
    }

    public ReminderType getType() {
        return type;
    }

    public Task getTask() {
        return task;
    }

    // Setters
    public void setType(ReminderType newType) {
        if (newType == null) {
            throw new IllegalArgumentException("Reminder type cannot be null.");
        }
        if (newType == ReminderType.CUSTOM_DATE) {
            throw new IllegalArgumentException("Cannot set type to CUSTOM_DATE using setType. Use setReminderDate to set a custom date.");
        }

        LocalDate newReminderDate = calculateReminderDate(newType);

        validateProposedValues(newType, newReminderDate);

        this.type = newType;
        this.reminderDate = newReminderDate;
    }

    public void setReminderDate(LocalDate newCustomDate) {
        if (this.type != ReminderType.CUSTOM_DATE) {
            throw new IllegalArgumentException("Only custom reminders can have their date manually set.");
        }
        if (newCustomDate == null) {
            throw new IllegalArgumentException("Custom reminder date cannot be null.");
        }

        validateProposedValues(this.type, newCustomDate);

        this.reminderDate = newCustomDate;
    }

    private LocalDate calculateReminderDate(ReminderType type) {
        switch (type) {
            case ONE_DAY_BEFORE:
                return task.getDeadline().minusDays(1);
            case ONE_WEEK_BEFORE:
                return task.getDeadline().minusWeeks(1);
            case ONE_MONTH_BEFORE:
                return task.getDeadline().minusMonths(1);
            default:
                throw new IllegalArgumentException("Invalid reminder type.");
        }
    }

    private void validateProposedValues(ReminderType proposedType, LocalDate proposedReminderDate) {
        if (task.getStatus() == TaskStatus.COMPLETED) {
            throw new IllegalArgumentException("Cannot add reminders to a completed task.");
        }
        if (proposedReminderDate == null) {
            throw new IllegalArgumentException("Reminder date cannot be null.");
        }
        if (proposedReminderDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Reminder date cannot be in the past.");
        }

        if (!(proposedReminderDate.isBefore(task.getDeadline()) || proposedReminderDate.isEqual(task.getDeadline()))) {
            throw new IllegalArgumentException(
                    String.format("Reminder date %s for type %s is invalid for task deadline %s.",
                            proposedReminderDate, proposedType, task.getDeadline()));
        }

        if (task.getReminders().stream()
                .filter(r -> r != this)
                .anyMatch(r -> r.getReminderDate().equals(proposedReminderDate))) {
            throw new IllegalArgumentException(
                    String.format("A reminder with the date %s already exists for this task.", proposedReminderDate));
        }
    }

    @Override
    public String toString() {
        return "Reminder{" +
                "reminderDate=" + reminderDate +
                ", type=" + type +
                ", taskTitle=" + task.getTitle() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reminder reminder = (Reminder) o;
        return Objects.equals(reminderDate, reminder.reminderDate) &&
                Objects.equals(type, reminder.type) &&
                Objects.equals(task, reminder.task);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reminderDate, type, task);
    }
}
