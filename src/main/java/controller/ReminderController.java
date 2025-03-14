package controller;

import model.Reminder;
import model.ReminderType;
import model.Task;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReminderController {
    private final List<Reminder> reminders;

    public ReminderController() {
        this.reminders = new ArrayList<>();
    }

    public void addReminder(Reminder reminder) {
        if (reminder == null) {
            throw new IllegalArgumentException("Reminder cannot be null.");
        }
        if (reminder.getTask() == null) {
            throw new IllegalArgumentException("Reminder's task cannot be null.");
        }
        if (!reminders.contains(reminder)) {
            reminder.getTask().addReminderToTask(reminder);
            reminders.add(reminder);
        } else {
            if (!reminder.getTask().getReminders().contains(reminder)) {
                reminder.getTask().addReminderToTask(reminder);
                throw new IllegalArgumentException("Reminder already exists but was missing from the task reminders list.");
            } else {
                throw new IllegalArgumentException("Reminder already exists in the system for this task.");
            }
        }
    }

    public void updateReminder(Reminder reminder, ReminderType newType, LocalDate newReminderDate) {
        if (reminder == null) {
            throw new IllegalArgumentException("Reminder cannot be null.");
        }
        if (!reminders.contains(reminder)) {
            throw new IllegalArgumentException("Reminder not managed by this controller.");
        }
        if (newType == null) {
            throw new IllegalArgumentException("Reminder type cannot be null.");
        }
        if (newType == ReminderType.CUSTOM_DATE) {
            if (newReminderDate == null) {
                throw new IllegalArgumentException("Custom reminders require a specific date.");
            }
            if (reminder.getType() == ReminderType.CUSTOM_DATE) {
                reminder.setReminderDate(newReminderDate);
            } else {
                removeReminder(reminder);
                Reminder newReminder = new Reminder(newType, reminder.getTask(), newReminderDate);
                addReminder(newReminder);
            }
        } else {
            reminder.setType(newType);
        }
    }

    public void removeReminder(Reminder reminder) {
        if (reminder == null) {
            throw new IllegalArgumentException("Reminder cannot be null.");
        }
        if (!reminders.contains(reminder)) {
            throw new IllegalArgumentException("Reminder does not exist in the list.");
        }
        reminder.getTask().removeReminderFromTask(reminder);
        reminders.remove(reminder);
    }

    public List<Reminder> getAllReminders() {
        return new ArrayList<>(reminders);
    }

    public void reevaluateRemindersForTask(Task task, ReminderController reminderController) {
        if (task == null || reminderController == null) {
            return;
        }

        List<Reminder> updatedReminders = new ArrayList<>();
        List<Reminder> customReminders = new ArrayList<>();

        for (Reminder oldReminder : new ArrayList<>(task.getReminders())) {
            try {
                Reminder newReminder;
                if (oldReminder.getType() != ReminderType.CUSTOM_DATE) {
                    LocalDate newReminderDate = oldReminder.getTask().getDeadline().minusDays(1);

                    if (!newReminderDate.isBefore(LocalDate.now())) {
                        newReminder = new Reminder(oldReminder.getType(), task, null);
                        updatedReminders.add(newReminder);
                    }
                } else {
                    customReminders.add(oldReminder);
                }
            } catch (IllegalArgumentException ignored) {
                // If a new reminder is invalid, we do not add it
            }
        }
        for (Reminder reminder : new ArrayList<>(task.getReminders())) {
            reminderController.removeReminder(reminder);
        }
        for (Reminder oldCustomReminder : customReminders) {
            try {
                Reminder newCustomReminder = new Reminder(ReminderType.CUSTOM_DATE, task, oldCustomReminder.getReminderDate());
                updatedReminders.add(newCustomReminder);
            } catch (IllegalArgumentException ignored) {
            }
        }
        for (Reminder newReminder : updatedReminders) {
            reminderController.addReminder(newReminder);
        }
    }

    @Override
    public String toString() {
        return "ReminderController{" +
                "reminders=" + reminders +
                '}';
    }
}
