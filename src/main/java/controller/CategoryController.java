package controller;

import model.Category;
import model.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CategoryController {
    private List<Category> categories;

    public CategoryController() {
        this.categories = new ArrayList<>();
    }

    public void addCategory(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null.");
        }
        if (categories.stream().anyMatch(c -> c.getName().equalsIgnoreCase(category.getName()))) {
            throw new IllegalArgumentException("Category name must be unique.");
        }

        categories.add(category);
    }

    public void removeCategory(Category category, TaskController taskController, ReminderController reminderController) {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null.");
        }
        if (!categories.contains(category)) {
            throw new IllegalArgumentException("Category does not exist in the system.");
        }

        List<Task> tasksToRemove = taskController.getTasks().stream()
                .filter(task -> task.getCategory() == category)
                .toList();

        for (Task task : tasksToRemove) {
            taskController.removeTask(task, reminderController);
        }
        categories.remove(category);
    }

    public void updateCategory(Category category, String newName) {
        if (category == null || newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("Category and new name cannot be null or empty.");
        }
        if (!categories.contains(category)) {
            throw new IllegalArgumentException("Category does not exist in the system.");
        }
        if (categories.stream().anyMatch(c -> c != category && c.getName().equalsIgnoreCase(newName))) {
            throw new IllegalArgumentException("Category name must be unique.");
        }
        category.setName(newName);
    }

    public List<Category> getCategories() {
        return new ArrayList<>(categories);
    }

    @Override
    public String toString() {
        return "CategoryController{" +
               "categories=" + categories +
               '}';
    }
}
