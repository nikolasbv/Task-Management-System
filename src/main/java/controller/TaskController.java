package controller;

import model.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The TaskController class manages the lifecycle of tasks, including creation, deletion, updating,
 * and searching for tasks. It also handles the automatic status update for delayed tasks.
 */
public class TaskController {
    private List<Task> tasks;

    /**
     * Constructs a new TaskController with an empty task list.
     */
    public TaskController() {
        this.tasks = new ArrayList<>();
    }

    /**
     * Adds a new task to the system.
     *
     * @param task              The task to be added.
     * @param priorityController The priority controller to manage priorities.
     * @throws IllegalArgumentException if the task is null, already exists, or has no priority set.
     */
    public void addTask(Task task, PriorityController priorityController) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null.");
        }
        if (tasks.contains(task)) {
            throw new IllegalArgumentException("Task already exists in the system.");
        }
        if (task.getPriority() == null) {
            task.setPriority(priorityController.getDefaultPriority());
        }
        tasks.add(task);
    }

    /**
     * Removes an existing task from the system and also removes all associated reminders.
     *
     * @param task               The task to be removed.
     * @param reminderController The reminder controller to handle associated reminders.
     * @throws IllegalArgumentException if the task is null or does not exist in the system.
     */
    public void removeTask(Task task, ReminderController reminderController) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null.");
        }
        if (!tasks.contains(task)) {
            throw new IllegalArgumentException("Task does not exist in the system.");
        }

        for (Reminder reminder : new ArrayList<>(task.getReminders())) {
            reminderController.removeReminder(reminder);
        }
        tasks.remove(task);
    }

    /**
     * Updates an existing task's details, including title, description, category, priority,
     * deadline, and status. It also ensures that reminders are updated accordingly.
     *
     * @param task               The task to be updated.
     * @param title              The new title of the task.
     * @param description        The new description of the task.
     * @param category           The new category of the task.
     * @param priority           The new priority of the task.
     * @param deadline           The new deadline of the task.
     * @param status             The new status of the task.
     * @param reminderController The reminder controller to manage reminders.
     * @throws IllegalArgumentException if the task does not exist or deadline/status is invalid.
     */
    public void updateTask(Task task, String title, String description, Category category,
                           Priority priority, LocalDate deadline, TaskStatus status, ReminderController reminderController) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null.");
        }
        if (!tasks.contains(task)) {
            throw new IllegalArgumentException("Task does not exist in the system.");
        }

        boolean deadlineChanged = !task.getDeadline().equals(deadline);
        boolean statusChangedToCompleted = (status == TaskStatus.COMPLETED && task.getStatus() != TaskStatus.COMPLETED);
        boolean wasDelayed = (task.getStatus() == TaskStatus.DELAYED);

        if (wasDelayed) {
            if (deadline.isBefore(LocalDate.now()) && !deadline.equals(task.getDeadline())) {
                throw new IllegalArgumentException("A delayed task can only keep its current past deadline or be set to a future date.");
            }
        } else {
            if (deadline.isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("Task deadline cannot be in the past.");
            }
        }

        if (wasDelayed && status != TaskStatus.COMPLETED) {
            if (!deadlineChanged) {
                throw new IllegalArgumentException("A delayed task can only change status to COMPLETED unless its deadline is updated to a future date.");
            }
        }

        if (status == TaskStatus.DELAYED && !deadline.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("A task can only be marked as DELAYED if its deadline is in the past.");
        }

        if (wasDelayed && deadline.isAfter(LocalDate.now()) && status == TaskStatus.DELAYED) {
            status = TaskStatus.OPEN;
        }

        task.setTitle(title);
        task.setDescription(description);
        task.setCategory(category);
        task.setPriority(priority);
        task.setDeadline(deadline);
        task.setStatus(status);

        if (statusChangedToCompleted) {
            for (Reminder reminder : new ArrayList<>(task.getReminders())) {
                reminderController.removeReminder(reminder);
            }
        }
        if (deadlineChanged) {
            reminderController.reevaluateRemindersForTask(task, reminderController);
        }
    }

    /**
     * Updates the status of tasks that are past their deadline to "DELAYED".
     */
    public void updateDelayedTasks() {
        for (Task task : tasks) {
            if (task.getDeadline().isBefore(LocalDate.now()) && task.getStatus() != TaskStatus.COMPLETED) {
                task.setStatus(TaskStatus.DELAYED);
            }
        }
    }

    /**
     * Retrieves a list of all tasks in the system.
     *
     * @return A list containing all tasks.
     */
    public List<Task> getTasks() {
        return new ArrayList<>(tasks);
    }

    /**
     * Searches for tasks based on title, category, and priority.
     *
     * @param title        The title (or part of it) to search for.
     * @param categoryName The category name to filter by.
     * @param priorityName The priority name to filter by.
     * @return A list of tasks matching the search criteria.
     */
    public List<Task> searchTasks(String title, String categoryName, String priorityName) {
        return tasks.stream()
                .filter(task -> {
                    boolean matchesTitle = (title == null || title.isEmpty() ||
                            task.getTitle().toLowerCase().contains(title.toLowerCase()));

                    boolean matchesCategory = (categoryName == null || categoryName.isEmpty() ||
                            task.getCategory().getName().toLowerCase().contains(categoryName.toLowerCase()));

                    boolean matchesPriority = (priorityName == null || priorityName.isEmpty() ||
                            task.getPriority().getName().toLowerCase().contains(priorityName.toLowerCase()));

                    return matchesTitle && matchesCategory && matchesPriority;
                })
                .collect(Collectors.toList());
    }

    /**
     * Returns a string representation of the TaskController object.
     *
     * @return A string representation of the tasks managed by this controller.
     */
    @Override
    public String toString() {
        return "TaskController{" +
                "tasks=" + tasks +
                '}';
    }
}
