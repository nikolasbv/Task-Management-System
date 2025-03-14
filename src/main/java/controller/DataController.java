package controller;

import model.*;

import javax.json.*;
import javax.json.stream.JsonGenerator;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;

public class DataController {
    private static final String DIRECTORY = "medialab";
    private static final String TASKS_FILE = DIRECTORY + "/tasks.json";
    private static final String CATEGORIES_FILE = DIRECTORY + "/categories.json";
    private static final String PRIORITIES_FILE = DIRECTORY + "/priorities.json";

    public DataController() {
        ensureDirectoryExists();
    }

    private void ensureDirectoryExists() {
        File directory = new File(DIRECTORY);
        if (!directory.exists()) {
            directory.mkdir();
        }
    }

    public void saveTasksAndReminders(List<Task> tasks) {
        JsonArrayBuilder tasksArrayBuilder = Json.createArrayBuilder();
        for (Task task : tasks) {
            JsonObjectBuilder taskObject = Json.createObjectBuilder()
                    .add("id", task.getId())
                    .add("title", task.getTitle())
                    .add("description", task.getDescription())
                    .add("category", task.getCategory().getName())
                    .add("priority", task.getPriority().getName())
                    .add("deadline", task.getDeadline().toString())
                    .add("status", task.getStatus().name());

            JsonArrayBuilder remindersArrayBuilder = Json.createArrayBuilder();
            for (Reminder reminder : task.getReminders()) {
                remindersArrayBuilder.add(Json.createObjectBuilder()
                        .add("type", reminder.getType().name())
                        .add("date", reminder.getReminderDate().toString()));
            }

            taskObject.add("reminders", remindersArrayBuilder);
            tasksArrayBuilder.add(taskObject);
        }
        JsonObject root = Json.createObjectBuilder().add("tasks", tasksArrayBuilder).build();
        saveJsonToFile(TASKS_FILE, root);
    }

    public void loadTasksAndReminders(List<Category> categories, List<Priority> priorities,
                                      List<Task> tasks, List<Reminder> reminders) {
        tasks.clear();
        reminders.clear();

        if (!Files.exists(Paths.get(TASKS_FILE))) {
            return;
        }

        try (JsonReader reader = Json.createReader(new FileInputStream(TASKS_FILE))) {
            JsonObject jsonObject = reader.readObject();
            JsonArray tasksArray = jsonObject.getJsonArray("tasks");
            for (JsonObject taskObject : tasksArray.getValuesAs(JsonObject.class)) {
                String title = taskObject.getString("title");
                String description = taskObject.getString("description");
                String categoryName = taskObject.getString("category");
                String priorityName = taskObject.getString("priority");
                LocalDate deadline = LocalDate.parse(taskObject.getString("deadline"));
                TaskStatus status = TaskStatus.valueOf(taskObject.getString("status"));

                Category category = categories.stream()
                        .filter(c -> c.getName().equals(categoryName))
                        .findFirst()
                        .orElse(null);
                Priority priority = priorities.stream()
                        .filter(p -> p.getName().equals(priorityName))
                        .findFirst()
                        .orElse(null);

                Task task = new Task(title, description, category, priority, deadline, true);
                task.setStatus(status);

                JsonArray remindersArray = taskObject.getJsonArray("reminders");
                for (JsonObject reminderObject : remindersArray.getValuesAs(JsonObject.class)) {
                    ReminderType type = ReminderType.valueOf(reminderObject.getString("type"));
                    LocalDate reminderDate = LocalDate.parse(reminderObject.getString("date"));
                    if (reminderDate.isBefore(LocalDate.now())) {
                        continue;
                    }
                    Reminder reminder = new Reminder(type, task, reminderDate);
                    reminders.add(reminder);
                }
                tasks.add(task);
            }
        } catch (IOException e) {
            System.err.println("Error loading tasks: " + e.getMessage());
        }
    }

    public void saveCategories(List<Category> categories) {
        JsonArrayBuilder categoriesArrayBuilder = Json.createArrayBuilder();
        for (Category category : categories) {
            categoriesArrayBuilder.add(Json.createObjectBuilder()
                    .add("name", category.getName()));
        }
        JsonObject root = Json.createObjectBuilder().add("categories", categoriesArrayBuilder).build();
        saveJsonToFile(CATEGORIES_FILE, root);
    }

    public List<Category> loadCategories() {
        List<Category> categories = new ArrayList<>();
        if (!Files.exists(Paths.get(CATEGORIES_FILE))) {
            return categories;
        }
        try (JsonReader reader = Json.createReader(new FileInputStream(CATEGORIES_FILE))) {
            JsonObject jsonObject = reader.readObject();
            JsonArray categoriesArray = jsonObject.getJsonArray("categories");
            for (JsonObject categoryObject : categoriesArray.getValuesAs(JsonObject.class)) {
                categories.add(new Category(categoryObject.getString("name")));
            }
        } catch (IOException e) {
            System.err.println("Error loading categories: " + e.getMessage());
        }
        return categories;
    }

    public void savePriorities(List<Priority> priorities) {
        JsonArrayBuilder prioritiesArrayBuilder = Json.createArrayBuilder();
        for (Priority priority : priorities) {
            prioritiesArrayBuilder.add(Json.createObjectBuilder()
                    .add("name", priority.getName()));
        }
        JsonObject root = Json.createObjectBuilder().add("priorities", prioritiesArrayBuilder).build();
        saveJsonToFile(PRIORITIES_FILE, root);
    }

    public List<Priority> loadPriorities() {
        List<Priority> priorities = new ArrayList<>();
        if (!Files.exists(Paths.get(PRIORITIES_FILE))) {
            return priorities;
        }

        try (JsonReader reader = Json.createReader(new FileInputStream(PRIORITIES_FILE))) {
            JsonObject jsonObject = reader.readObject();
            JsonArray prioritiesArray = jsonObject.getJsonArray("priorities");
            for (JsonObject priorityObject : prioritiesArray.getValuesAs(JsonObject.class)) {
                priorities.add(new Priority(priorityObject.getString("name")));
            }
        } catch (IOException e) {
            System.err.println("Error loading priorities: " + e.getMessage());
        }
        return priorities;
    }

    private void saveJsonToFile(String filePath, JsonObject jsonObject) {
        Map<String, Object> config = Collections.singletonMap(JsonGenerator.PRETTY_PRINTING, true);
        JsonWriterFactory writerFactory = Json.createWriterFactory(config);

        try (OutputStream os = new FileOutputStream(filePath);
             JsonWriter writer = writerFactory.createWriter(os)) {
            writer.write(jsonObject);
        } catch (IOException e) {
            System.err.println("Error saving JSON to " + filePath + ": " + e.getMessage());
        }
    }
}
