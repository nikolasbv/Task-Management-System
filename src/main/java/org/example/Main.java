package org.example;

import controller.*;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import model.*;
import model.Priority;

import view.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Main extends Application {

    private TaskController taskController;
    private ReminderController reminderController;
    private CategoryController categoryController;
    private PriorityController priorityController;
    private DataController dataController;

    private TableView<Object> taskTable = new TableView<>();
    private TableView<Object> categoryTable = new TableView<>();
    private TableView<Object> priorityTable = new TableView<>();
    private TableView<Object> reminderTable = new TableView<>();

    private SearchQuery searchQuery = new SearchQuery();


    @Override
    public void start(Stage primaryStage) {
        dataController = new DataController();
        taskController = new TaskController();
        reminderController = new ReminderController();
        categoryController = new CategoryController();
        priorityController = new PriorityController();

        List<Category> loadedCategories = dataController.loadCategories();
        List<Priority> loadedPriorities = dataController.loadPriorities();
        List<Task> loadedTasks = new ArrayList<>();
        List<Reminder> loadedReminders = new ArrayList<>();
        dataController.loadTasksAndReminders(loadedCategories, loadedPriorities, loadedTasks, loadedReminders);

        for (Category category : loadedCategories) {
            categoryController.addCategory(category);
        }
        for (Priority priority : loadedPriorities) {
            try {
                priorityController.addPriority(priority);
            } catch (IllegalArgumentException e) {
                // For Default priority skip
            }
        }
        for (Task task : loadedTasks) {
            taskController.addTask(task, priorityController);
        }
        for (Reminder reminder : loadedReminders) {
            reminderController.addReminder(reminder);
        }

        taskController.updateDelayedTasks();

        // ***************************
        // UI Layout
        // ***************************
        VBox root = new VBox();
        root.getStyleClass().add("root");

        root.setPadding(new Insets(10));
        root.setSpacing(10);

        // ***************************
        // Top Section (Dashboard)
        // ***************************

        Label welcomeLabel = new Label("Welcome to MediaLab Assistant!");
        welcomeLabel.getStyleClass().add("dashboard-title");

        Label totalTasksLabel = new Label();
        Label completedTasksLabel = new Label();
        Label delayedTasksLabel = new Label();
        Label dueSoonTasksLabel = new Label();

        totalTasksLabel.getStyleClass().add("dashboard-stat");
        completedTasksLabel.getStyleClass().add("dashboard-stat");
        delayedTasksLabel.getStyleClass().add("dashboard-stat");
        dueSoonTasksLabel.getStyleClass().add("dashboard-stat");

        HBox dashboardContainer = new HBox();
        dashboardContainer.setSpacing(20);
        dashboardContainer.setPadding(new Insets(10, 20, 10, 20));
        dashboardContainer.setAlignment(Pos.CENTER_LEFT);
        dashboardContainer.getStyleClass().add("dashboard-container");

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        HBox dashboardValues = new HBox(20, totalTasksLabel, completedTasksLabel, delayedTasksLabel, dueSoonTasksLabel);
        dashboardValues.setAlignment(Pos.CENTER_RIGHT);

        dashboardContainer.getChildren().addAll(welcomeLabel, spacer, dashboardValues);

        primaryStage.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            if (newWidth.doubleValue() < 900) {
                if (dashboardContainer.getChildren().contains(welcomeLabel)) {
                    dashboardContainer.getChildren().remove(welcomeLabel);
                }
            } else {
                if (!dashboardContainer.getChildren().contains(welcomeLabel)) {
                    dashboardContainer.getChildren().add(0, welcomeLabel);
                }
            }
        });

        Runnable refreshDashboard = () -> {
            totalTasksLabel.setText("Total Tasks: " + taskController.getTasks().size());
            completedTasksLabel.setText("Completed: " +
                    taskController.getTasks().stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count());
            delayedTasksLabel.setText("Delayed: " +
                    taskController.getTasks().stream().filter(t -> t.getStatus() == TaskStatus.DELAYED).count());
            dueSoonTasksLabel.setText("Due in 7 Days: " +
                    taskController.getTasks().stream()
                            .filter(t -> !t.getDeadline().isBefore(LocalDate.now()) &&
                                    !t.getDeadline().isAfter(LocalDate.now().plusDays(7)) &&
                                    t.getStatus() != TaskStatus.COMPLETED)
                            .count());
        };
        refreshDashboard.run();

        Runnable refreshAll = () -> {
            refreshDashboard.run();

            taskTable.setItems(FXCollections.observableArrayList(taskController.getTasks()));
            categoryTable.setItems(FXCollections.observableArrayList(categoryController.getCategories()));
            priorityTable.setItems(FXCollections.observableArrayList(priorityController.getPriorities()));
            reminderTable.setItems(FXCollections.observableArrayList(reminderController.getAllReminders()));

            if (!searchQuery.titleQuery.isEmpty() || !searchQuery.categoryQuery.isEmpty() || !searchQuery.priorityQuery.isEmpty()) {
                var filteredTasks = taskController.searchTasks(searchQuery.titleQuery, searchQuery.categoryQuery, searchQuery.priorityQuery);
                taskTable.setItems(FXCollections.observableArrayList(filteredTasks));
            }

            taskTable.refresh();
            categoryTable.refresh();
            priorityTable.refresh();
            reminderTable.refresh();
        };


        // *****************************
        // Bottom Section (Operations Panel)
        // *****************************
        TabPane operationsPanel = new TabPane();
        operationsPanel.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab taskTab = TaskManagementView.createTaskManagementTab(
                taskController, categoryController, priorityController, reminderController,
                taskTable, refreshAll, searchQuery
        );
        Tab categoryPriorityTab = CategoryPriorityManagementView.createCategoryPriorityManagementTab(taskController, categoryController, priorityController, reminderController, categoryTable, priorityTable, refreshAll);
        Tab reminderTab = ReminderManagementView.createReminderManagementTab(taskController, reminderController, reminderTable, refreshAll);

        operationsPanel.getTabs().addAll(taskTab, categoryPriorityTab, reminderTab);

        VBox.setVgrow(operationsPanel, javafx.scene.layout.Priority.ALWAYS);

        root.getChildren().addAll(dashboardContainer, operationsPanel);

        Scene scene = new Scene(root, 1000, 600);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        primaryStage.setTitle("MediaLab Assistant");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(700);
        primaryStage.setMinHeight(400);

        primaryStage.show();

        long delayedCount = taskController.getTasks().stream()
                .filter(task -> task.getStatus() == TaskStatus.DELAYED)
                .count();
        if (delayedCount > 0) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.getDialogPane().getStylesheets().add(Main.class.getResource("/style.css").toExternalForm());
            alert.getDialogPane().getStyleClass().add("alert");
            alert.setTitle("Delayed Tasks Alert");
            alert.setHeaderText("There are Delayed Tasks");
            alert.setContentText("Number of Delayed Tasks: " + delayedCount);
            alert.showAndWait();
        }
    }


    @Override
    public void stop() {
        dataController.saveCategories(categoryController.getCategories());
        dataController.savePriorities(priorityController.getPriorities());
        dataController.saveTasksAndReminders(taskController.getTasks());
    }

    public static class AttributeColumnSpec {
        private final String header;
        private final Function<Object, String> extractor;

        public AttributeColumnSpec(String header, Function<Object, String> extractor) {
            this.header = header;
            this.extractor = extractor;
        }

        public String getHeader() {
            return header;
        }

        public Function<Object, String> getExtractor() {
            return extractor;
        }
    }

    public class SearchQuery {
        public String titleQuery = "";
        public String categoryQuery = "";
        public String priorityQuery = "";
    }

    public static void main(String[] args) {
        launch(args);
    }
}
