package view;

import controller.ReminderController;
import controller.TaskController;
import controller.CategoryController;
import controller.PriorityController;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.util.Pair;
import model.*;
import model.Priority;
import org.example.Main;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class TaskManagementView {

    public static Tab createTaskManagementTab(TaskController taskController,
                                              CategoryController categoryController,
                                              PriorityController priorityController,
                                              ReminderController reminderController,
                                              TableView<Object> table,
                                              Runnable refreshAll,
                                              Main.SearchQuery searchQuery) {
        Tab tab = new Tab("Task Management");
        tab.setContent(createTaskManagementPane(taskController, categoryController, priorityController,
                                                reminderController, table, refreshAll, searchQuery));
        return tab;
    }

    public static Pane createTaskManagementPane(TaskController taskController,
                                                CategoryController categoryController,
                                                PriorityController priorityController,
                                                ReminderController reminderController,
                                                TableView<Object> table,
                                                Runnable refreshAll,
                                                Main.SearchQuery searchQuery) {


        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // ---------------------------
        // HEADER TEXT
        // ---------------------------
        Text infoText = new Text("This is the management panel for Tasks. "
                + "You can create, edit, and remove tasks. "
                + "Click More to view the details of a specific task, edit the task and handle its reminders.");

        infoText.setWrappingWidth(650);
        infoText.setTextAlignment(TextAlignment.CENTER);
        infoText.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-fill: #2C3E50; -fx-font-family: 'Arial';");

        TextFlow infoTextFlow = new TextFlow(infoText);
        infoTextFlow.setMaxWidth(650);
        infoTextFlow.setTextAlignment(TextAlignment.CENTER);

        VBox headerContainer = new VBox(infoTextFlow);
        headerContainer.setAlignment(Pos.CENTER);
        headerContainer.setPadding(new Insets(10, 0, 20, 0)); // Add spacing


        // ---------------------------
        // SEARCH BOX
        // ---------------------------
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER);
        searchBox.setPadding(new Insets(5, 0, 10, 0));

        TextField titleSearchField = new TextField();
        titleSearchField.setPromptText("Search by Task Title");

        TextField categorySearchField = new TextField();
        categorySearchField.setPromptText("Search by Category");

        TextField prioritySearchField = new TextField();
        prioritySearchField.setPromptText("Search by Priority");

        Button searchButton = new Button("Search");
        Button clearFiltersButton = new Button("Clear Filters");

        titleSearchField.setPrefWidth(200);
        categorySearchField.setPrefWidth(200);
        prioritySearchField.setPrefWidth(200);
        searchButton.setPrefWidth(100);
        clearFiltersButton.setPrefWidth(100);

        searchBox.getChildren().addAll(titleSearchField, categorySearchField, prioritySearchField, searchButton, clearFiltersButton);

        // ---------------------------
        // TASK TABLE
        // ---------------------------
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setSelectionModel(null);
        table.setMinWidth(900);
        table.setPrefWidth(Region.USE_COMPUTED_SIZE);

        VBox.setVgrow(table, javafx.scene.layout.Priority.ALWAYS);

        // ---------------------------
        // SEARCH FUNCTIONALITY
        // ---------------------------
        searchButton.setOnAction(event -> {
            searchQuery.titleQuery = titleSearchField.getText().trim();
            searchQuery.categoryQuery = categorySearchField.getText().trim();
            searchQuery.priorityQuery = prioritySearchField.getText().trim();

            var filteredTasks = taskController.searchTasks(
                    searchQuery.titleQuery, searchQuery.categoryQuery, searchQuery.priorityQuery
            );
            table.setItems(FXCollections.observableArrayList(filteredTasks));
            table.refresh();
        });

        clearFiltersButton.setOnAction(event -> {
            searchQuery.titleQuery = "";
            searchQuery.categoryQuery = "";
            searchQuery.priorityQuery = "";

            titleSearchField.clear();
            categorySearchField.clear();
            prioritySearchField.clear();

            table.setItems(FXCollections.observableArrayList(taskController.getTasks()));
            table.refresh();
        });

        // ---------------------------
        // TABLE COLUMNS
        // ---------------------------
        List<Main.AttributeColumnSpec> attributeSpecs = new ArrayList<>();
        attributeSpecs.add(new Main.AttributeColumnSpec("Task", item -> {
            Task t = (Task) item;
            return t.getTitle() + "\n(" + t.getId() + ")";
        }));
        attributeSpecs.add(new Main.AttributeColumnSpec("Category", item -> ((Task)item).getCategory().getName()));
        attributeSpecs.add(new Main.AttributeColumnSpec("Priority", item -> ((Task)item).getPriority().getName()));
        attributeSpecs.add(new Main.AttributeColumnSpec("Deadline / Status", item -> {
            Task t = (Task) item;
            return t.getDeadline().toString() + " / " + getFriendlyStatus(t.getStatus());
        }));

        final double actionsColumnWidth = 180;
        final double attrColMinWidth = 180;

        List<TableColumn<Object, String>> attributeColumns = new ArrayList<>();
        for (Main.AttributeColumnSpec spec : attributeSpecs) {
            TableColumn<Object, String> col = new TableColumn<>(spec.getHeader());
            col.setCellValueFactory(data -> new SimpleStringProperty(spec.getExtractor().apply(data.getValue())));
            col.setMinWidth(attrColMinWidth);
            col.setCellFactory(tc -> new TableCell<Object, String>() {
                private final Text text = new Text();
                {
                    text.wrappingWidthProperty().bind(this.widthProperty().subtract(10));
                    setGraphic(text);
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    text.setText(empty ? null : item);
                }
            });
            attributeColumns.add(col);
        }

        TableColumn<Object, Void> actionsColumn = new TableColumn<>("Actions");
        actionsColumn.setCellFactory(col -> new TableCell<Object, Void>() {
            private final Button moreButton = new Button("More");
            private final Button removeButton = new Button("Remove");
            private final HBox actionButtons = new HBox(5);

            {
                actionButtons.setAlignment(Pos.CENTER);
                moreButton.setPrefWidth(80);
                removeButton.setPrefWidth(80);
                actionButtons.getChildren().addAll(moreButton, removeButton);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Object currentItem = getTableView().getItems().get(getIndex());
                    moreButton.setOnAction(event -> showTaskDetailsDialog(currentItem, taskController, categoryController, priorityController, reminderController, table, refreshAll));
                    removeButton.setOnAction(event -> showRemoveTaskDialog(currentItem, taskController, reminderController, table, refreshAll));
                    setGraphic(actionButtons);
                }
            }
        });
        actionsColumn.setPrefWidth(actionsColumnWidth);
        actionsColumn.setMinWidth(actionsColumnWidth);
        actionsColumn.setMaxWidth(actionsColumnWidth);

        for (TableColumn<Object, String> col : attributeColumns) {
            table.getColumns().add(col);
        }
        table.getColumns().add(actionsColumn);
        table.setItems(FXCollections.observableArrayList(taskController.getTasks()));

        // ---------------------------
        // SCROLL PANE FOR TAB
        // ---------------------------
        ScrollPane tabScrollPane = new ScrollPane();
        tabScrollPane.setFitToWidth(true);
        tabScrollPane.setFitToHeight(true);
        tabScrollPane.setPannable(true);

        VBox tableContainer = new VBox(table);
        tableContainer.setAlignment(Pos.CENTER);
        tableContainer.setPadding(new Insets(5));
        tableContainer.setMinHeight(200);
        tableContainer.setPrefHeight(Region.USE_COMPUTED_SIZE);
        VBox.setVgrow(tableContainer, javafx.scene.layout.Priority.ALWAYS);

        // ---------------------------
        // ADD BUTTON
        // ---------------------------
        Button addButton = new Button("Add New Task");
        addButton.setOnAction(event -> showAddTaskDialog(taskController, categoryController, priorityController, table, refreshAll));

        // ---------------------------
        // LAYOUT MANAGEMENT
        // ---------------------------
        HBox addButtonContainer = new HBox(addButton);
        addButtonContainer.setAlignment(Pos.CENTER);

        VBox scrollableContent = new VBox(10, headerContainer, searchBox, tableContainer, addButtonContainer);
        scrollableContent.setPadding(new Insets(10));
        scrollableContent.setAlignment(Pos.CENTER);
        VBox.setVgrow(tableContainer, javafx.scene.layout.Priority.ALWAYS);

        tabScrollPane.setContent(scrollableContent);
        root.setCenter(tabScrollPane);

        return root;
    }


    private static Dialog<Void> moreDialogReference = null;

    private static void refreshMoreDialog(Object item, TaskController taskController,
                                          CategoryController categoryController,
                                          PriorityController priorityController,
                                          ReminderController reminderController,
                                          TableView<Object> table,
                                          Runnable refreshAll) {
        if (moreDialogReference == null) return;

        if (!(item instanceof Task task)) {
            return;
        }

        VBox content = buildMoreDialogContent(task, taskController, categoryController,
                priorityController, reminderController, table, refreshAll);
        moreDialogReference.getDialogPane().setContent(content);
    }

    private static void showTaskDetailsDialog(Object item, TaskController taskController,
                                              CategoryController categoryController,
                                              PriorityController priorityController,
                                              ReminderController reminderController,
                                              TableView<Object> table,
                                              Runnable refreshAll) {
        if (!(item instanceof Task task)) {
            return;
        }

        moreDialogReference = new Dialog<>();
        moreDialogReference.getDialogPane().getStylesheets().add(Main.class.getResource("/style.css").toExternalForm());
        moreDialogReference.getDialogPane().getStyleClass().add("custom-dialog");
        moreDialogReference.setTitle("Task Details");
        moreDialogReference.setHeaderText("Task Information");

        VBox content = buildMoreDialogContent(task, taskController, categoryController,
                priorityController, reminderController, table, refreshAll);

        moreDialogReference.getDialogPane().setPrefWidth(500);

        moreDialogReference.getDialogPane().setContent(content);
        moreDialogReference.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        moreDialogReference.showAndWait();

        moreDialogReference = null;
    }


    private static VBox buildMoreDialogContent(Task task, TaskController taskController, CategoryController categoryController,
                                               PriorityController priorityController, ReminderController reminderController, TableView<Object> table, Runnable refreshAll) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 50, 10, 10));

        Label idTextLabel = new Label("ID:");
        idTextLabel.setMinWidth(100);
        grid.add(idTextLabel, 0, 0);
        Label idLabel = new Label(task.getId());
        idLabel.setWrapText(true);
        idLabel.setMaxWidth(350);
        grid.add(idLabel, 1, 0);

        Label titleTextLabel = new Label("Title:");
        titleTextLabel.setMinWidth(100);
        grid.add(titleTextLabel, 0, 1);
        Label titleLabel = new Label(task.getTitle());
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(350);
        grid.add(titleLabel, 1, 1);

        Label descriptionTextLabel = new Label("Description:");
        descriptionTextLabel.setMinWidth(100);
        grid.add(descriptionTextLabel, 0, 2);
        Label descriptionLabel = new Label(task.getDescription());
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxWidth(350);
        grid.add(descriptionLabel, 1, 2);

        Label categoryTextLabel = new Label("Category:");
        categoryTextLabel.setMinWidth(100);
        grid.add(categoryTextLabel, 0, 3);
        Label categoryLabel = new Label(task.getCategory().getName());
        categoryLabel.setWrapText(true);
        categoryLabel.setMaxWidth(350);
        grid.add(categoryLabel, 1, 3);

        Label priorityTextLabel = new Label("Priority:");
        priorityTextLabel.setMinWidth(100);
        grid.add(priorityTextLabel, 0, 4);
        Label priorityLabel = new Label(task.getPriority().getName());
        priorityLabel.setWrapText(true);
        priorityLabel.setMaxWidth(350);
        grid.add(priorityLabel, 1, 4);

        Label deadlineTextLabel = new Label("Deadline:");
        deadlineTextLabel.setMinWidth(100);
        grid.add(deadlineTextLabel, 0, 5);
        Label deadlineLabel = new Label(task.getDeadline().toString());
        deadlineLabel.setWrapText(true);
        deadlineLabel.setMaxWidth(350);
        grid.add(deadlineLabel, 1, 5);

        Label statusTextLabel = new Label("Status:");
        statusTextLabel.setMinWidth(100);
        grid.add(statusTextLabel, 0, 6);
        Label statusLabel = new Label(getFriendlyStatus(task.getStatus()));
        statusLabel.setWrapText(true);
        statusLabel.setMaxWidth(350);
        grid.add(statusLabel, 1, 6);

        Label remindersLabel = new Label("Reminders:");
        TableView<Object> reminderTable = new TableView<>();
        reminderTable.setPrefHeight(150);
        reminderTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        reminderTable.setItems(FXCollections.observableArrayList(task.getReminders()));
        reminderTable.refresh();

        reminderTable.setFocusTraversable(true);
        reminderTable.setMouseTransparent(false);

        reminderTable.setSelectionModel(null);

        TableColumn<Object, String> reminderInfoColumn = new TableColumn<>("Reminder");
        reminderInfoColumn.setCellValueFactory(data -> {
            Reminder reminder = (Reminder) data.getValue();
            return new SimpleStringProperty(reminder.getType() + " - " + reminder.getReminderDate().toString());
        });

        TableColumn<Object, Void> actionsColumn = new TableColumn<>("Actions");
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button removeButton = new Button("Remove");
            private final HBox actionButtons = new HBox(5);

            {
                actionButtons.setAlignment(Pos.CENTER);
                editButton.setPrefWidth(80);
                removeButton.setPrefWidth(80);
                actionButtons.getChildren().addAll(editButton, removeButton);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Object reminder = getTableView().getItems().get(getIndex());

                    editButton.setOnAction(event -> showEditReminderDialog(reminder, reminderController, reminderTable, refreshAll));
                    removeButton.setOnAction(event -> showRemoveReminderDialog(reminder, reminderController, reminderTable, refreshAll));

                    setGraphic(actionButtons);
                }
            }
        });

        reminderTable.getColumns().addAll(reminderInfoColumn, actionsColumn);

        Button editButton = new Button("Edit Task");
        Button addReminderButton = new Button("Add Reminder");

        double buttonWidth = 120;
        editButton.setPrefWidth(buttonWidth);
        addReminderButton.setPrefWidth(buttonWidth);

        editButton.setOnAction(event -> showEditTaskDialog(task, taskController, categoryController, priorityController, reminderController, reminderTable, refreshAll));
        addReminderButton.setOnAction(event -> showAddReminderDialog(task, reminderController, reminderTable, refreshAll));

        HBox topButton = new HBox(10, editButton);
        topButton.setAlignment(Pos.CENTER);

        HBox downButton = new HBox(10, addReminderButton);
        downButton.setAlignment(Pos.CENTER);

        VBox content = new VBox(10, grid, topButton, remindersLabel, reminderTable, downButton);
        return content;
    }

    private static void showEditTaskDialog(Object item, TaskController taskController,
                                           CategoryController categoryController,
                                           PriorityController priorityController,
                                           ReminderController reminderController,
                                           TableView<Object> table,
                                           Runnable refreshAll) {
        if (!(item instanceof Task task)) {
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.getDialogPane().getStylesheets().add(Main.class.getResource("/style.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("custom-dialog");
        dialog.setTitle("Edit Task");
        dialog.setHeaderText("Modify Task Information");

        TaskStatus initialStatus = task.getStatus();
        LocalDate initialDeadline = task.getDeadline();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("ID:"), 0, 0);
        grid.add(new Label(task.getId()), 1, 0);

        grid.add(new Label("Title:"), 0, 1);
        TextField titleField = new TextField(task.getTitle());
        grid.add(titleField, 1, 1);

        grid.add(new Label("Description:"), 0, 2);
        TextArea descriptionArea = new TextArea(task.getDescription());
        descriptionArea.setPrefRowCount(4);
        descriptionArea.setWrapText(true);
        descriptionArea.setPrefWidth(250);
        grid.add(descriptionArea, 1, 2);

        grid.add(new Label("Category:"), 0, 3);
        ComboBox<Category> categoryCombo = new ComboBox<>(FXCollections.observableArrayList(categoryController.getCategories()));
        categoryCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        categoryCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        categoryCombo.setValue(task.getCategory());
        grid.add(categoryCombo, 1, 3);

        grid.add(new Label("Priority:"), 0, 4);
        ComboBox<Priority> priorityCombo = new ComboBox<>(FXCollections.observableArrayList(priorityController.getPriorities()));
        priorityCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Priority item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        priorityCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Priority item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        priorityCombo.setValue(task.getPriority());
        grid.add(priorityCombo, 1, 4);

        grid.add(new Label("Deadline:"), 0, 5);
        DatePicker deadlinePicker = new DatePicker(task.getDeadline());
        grid.add(deadlinePicker, 1, 5);

        grid.add(new Label("Status:"), 0, 6);
        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.setItems(FXCollections.observableArrayList("Open", "In Progress", "Postponed", "Completed", "Delayed"));
        statusCombo.setValue(getFriendlyStatus(task.getStatus()));
        grid.add(statusCombo, 1, 6);

        Button updateButton = new Button("Update");
        Button cancelButton = new Button("Cancel");

        double buttonWidth = 120;
        updateButton.setPrefWidth(buttonWidth);
        cancelButton.setPrefWidth(buttonWidth);

        HBox buttons = new HBox(10, updateButton, cancelButton);
        buttons.setAlignment(Pos.CENTER);

        VBox content = new VBox(10, grid, buttons);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        updateButton.setOnAction(event -> {
            boolean statusChangedToCompleted = (getTaskStatusFromFriendlyName(statusCombo.getValue()) == TaskStatus.COMPLETED && initialStatus != TaskStatus.COMPLETED);
            boolean deadlineChanged = !initialDeadline.equals(deadlinePicker.getValue());
            boolean wasDelayed = (initialStatus == TaskStatus.DELAYED);
            boolean newDeadlineInFuture = deadlinePicker.getValue().isAfter(LocalDate.now());

            if (wasDelayed && deadlineChanged && newDeadlineInFuture && getTaskStatusFromFriendlyName(statusCombo.getValue()) == TaskStatus.DELAYED) {
                Alert warningAlert = new Alert(Alert.AlertType.WARNING);
                warningAlert.getDialogPane().getStylesheets().add(Main.class.getResource("/style.css").toExternalForm());
                warningAlert.getDialogPane().getStyleClass().add("alert");
                warningAlert.setTitle("Status Update Notice");
                warningAlert.setHeaderText("Task Status Will Change");
                warningAlert.setContentText("Since the deadline has been moved to a future date, the task status will automatically be changed from DELAYED to OPEN.");
                warningAlert.showAndWait();
            }

            if (statusChangedToCompleted && !task.getReminders().isEmpty()) {
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.getDialogPane().getStylesheets().add(Main.class.getResource("/style.css").toExternalForm());
                confirmAlert.getDialogPane().getStyleClass().add("alert");
                confirmAlert.setTitle("Confirm Completion");
                confirmAlert.setHeaderText("Warning: Completing Task");
                confirmAlert.setContentText("Setting this task as COMPLETED will remove all associated reminders. Are you sure you want to continue?");
                Optional<ButtonType> result = confirmAlert.showAndWait();
                if (result.isEmpty() || result.get() != ButtonType.OK) {
                    return;
                }
            }

            if (deadlineChanged && !task.getReminders().isEmpty()) {
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.getDialogPane().getStylesheets().add(Main.class.getResource("/style.css").toExternalForm());
                confirmAlert.getDialogPane().getStyleClass().add("alert");
                confirmAlert.setTitle("Confirm Deadline Change");
                confirmAlert.setHeaderText("Warning: Changing Deadline");
                confirmAlert.setContentText("Changing the task deadline may affect associated reminders. Some reminders may be removed if they are no longer valid.");
                Optional<ButtonType> result = confirmAlert.showAndWait();
                if (result.isEmpty() || result.get() != ButtonType.OK) {
                    return;
                }
            }
            TaskStatus selectedStatus = getTaskStatusFromFriendlyName(statusCombo.getValue());
            try {
                taskController.updateTask(task, titleField.getText(), descriptionArea.getText(),
                        categoryCombo.getValue(), priorityCombo.getValue(),
                        deadlinePicker.getValue(), selectedStatus, reminderController);
                // table.setItems(FXCollections.observableArrayList(task.getReminders()));
                table.refresh();
                refreshAll.run();

                if (moreDialogReference != null) {
                    refreshMoreDialog(task, taskController, categoryController,
                            priorityController, reminderController, table, refreshAll);
                    table.refresh();
                }

                dialog.close();
            } catch (IllegalArgumentException e) {
                showError("Error Updating Task", e.getMessage());
                return;
            }
        });

        cancelButton.setOnAction(event -> dialog.close());
        dialog.showAndWait();
    }

    private static void showRemoveTaskDialog(Object item, TaskController taskController,
                                             ReminderController reminderController, TableView<Object> table, Runnable refreshAll) {

        if (!(item instanceof Task task)) {
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.getDialogPane().getStylesheets().add(Main.class.getResource("/style.css").toExternalForm());
        alert.getDialogPane().getStyleClass().add("alert");
        alert.setTitle("Confirm Removal");
        alert.setHeaderText("Remove Task");
        alert.setContentText("Are you sure you want to remove this task?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    taskController.removeTask(task, reminderController);
                    table.setItems(FXCollections.observableArrayList(taskController.getTasks()));
                    refreshAll.run();
                } catch (IllegalArgumentException e) {
                    showError("Error Removing Task", e.getMessage());
                    return;
                }
            }
        });
    }

    private static void showAddTaskDialog(TaskController taskController,
                                          CategoryController categoryController,
                                          PriorityController priorityController,
                                          TableView<Object> table,
                                          Runnable refreshAll) {
        Dialog<Task> dialog = new Dialog<>();
        dialog.getDialogPane().getStylesheets().add(Main.class.getResource("/style.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("custom-dialog");
        dialog.setTitle("Add Task");
        dialog.setHeaderText("Create a New Task");
        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField titleField = new TextField();
        titleField.setPromptText("Title");

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Description");
        descriptionArea.setPrefRowCount(4);
        descriptionArea.setWrapText(true);
        descriptionArea.setPrefWidth(250);

        ComboBox<Category> categoryCombo = new ComboBox<>(FXCollections.observableArrayList(categoryController.getCategories()));
        categoryCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        categoryCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        if (!categoryCombo.getItems().isEmpty()) {
            categoryCombo.setValue(categoryCombo.getItems().get(0));
        }

        ComboBox<Priority> priorityCombo = new ComboBox<>(FXCollections.observableArrayList(priorityController.getPriorities()));
        priorityCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Priority item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        priorityCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Priority item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        priorityCombo.setValue(priorityController.getDefaultPriority());


        DatePicker deadlinePicker = new DatePicker(LocalDate.now().plusDays(1));

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionArea, 1, 1);
        grid.add(new Label("Category:"), 0, 2);
        grid.add(categoryCombo, 1, 2);
        grid.add(new Label("Priority:"), 0, 3);
        grid.add(priorityCombo, 1, 3);
        grid.add(new Label("Deadline:"), 0, 4);
        grid.add(deadlinePicker, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    Task newTask = new Task(
                            titleField.getText(),
                            descriptionArea.getText(),
                            categoryCombo.getValue(),
                            priorityCombo.getValue(),
                            deadlinePicker.getValue()
                    );
                    return newTask;
                } catch (IllegalArgumentException e) {
                    showError("Error Creating Task", e.getMessage());
                    return null;
                }
            }
            return null;
        });

        Optional<Task> result = dialog.showAndWait();
        result.ifPresent(newTask -> {
            try {
                taskController.addTask(newTask, priorityController);
                table.setItems(FXCollections.observableArrayList(taskController.getTasks()));
                refreshAll.run();
            } catch (IllegalArgumentException e) {
                showError("Error Adding Task", e.getMessage());
                return;
            }
        });
    }

    private static void showAddReminderDialog(Object item, ReminderController reminderController, TableView<Object> table, Runnable refreshAll) {
        if (!(item instanceof Task task)) {
            return;
        }

        Dialog<Reminder> dialog = new Dialog<>();
        dialog.getDialogPane().getStylesheets().add(Main.class.getResource("/style.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("custom-dialog");
        dialog.setTitle("Add Reminder");
        dialog.setHeaderText("Create a New Reminder");
        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<ReminderType> typeComboBox = new ComboBox<>();
        typeComboBox.getItems().addAll(
                ReminderType.ONE_DAY_BEFORE,
                ReminderType.ONE_WEEK_BEFORE,
                ReminderType.ONE_MONTH_BEFORE,
                ReminderType.CUSTOM_DATE
        );
        typeComboBox.setValue(ReminderType.ONE_DAY_BEFORE);

        DatePicker datePicker = new DatePicker();
        datePicker.setDisable(typeComboBox.getValue() != ReminderType.CUSTOM_DATE);

        typeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            datePicker.setDisable(newVal != ReminderType.CUSTOM_DATE);
        });

        Label taskLabel = new Label(task.getTitle() + " (" + task.getId() + ")");

        Label deadlineLabel = new Label("Deadline: " + task.getDeadline().toString());

        grid.add(new Label("Reminder Type:"), 0, 0);
        grid.add(typeComboBox, 1, 0);
        grid.add(new Label("Reminder Date:"), 0, 1);
        grid.add(datePicker, 1, 1);
        grid.add(new Label("Task:"), 0, 2);
        grid.add(taskLabel, 1, 2);
        grid.add(new Label("Task Deadline:"), 0, 3);
        grid.add(deadlineLabel, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    Reminder newReminder = new Reminder(
                            typeComboBox.getValue(),
                            task,
                            typeComboBox.getValue() == ReminderType.CUSTOM_DATE ? datePicker.getValue() : null
                    );
                    return newReminder;
                } catch (IllegalArgumentException e) {
                    showError("Error Creating Reminder", e.getMessage());
                    return null;
                }
            }
            return null;
        });

        Optional<Reminder> result = dialog.showAndWait();
        result.ifPresent(newReminder -> {
            try {
                reminderController.addReminder(newReminder);
                table.setItems(FXCollections.observableArrayList(task.getReminders()));
                refreshAll.run();
            } catch (IllegalArgumentException e) {
                showError("Error Adding Reminder", e.getMessage());
                return;
            }
        });
    }


    private static void showEditReminderDialog(Object item, ReminderController reminderController, TableView<Object> table, Runnable refreshAll) {
        if (!(item instanceof Reminder currentReminder)) {
            return;
        }

        Dialog<Pair<ReminderType, LocalDate>> dialog = new Dialog<>();
        dialog.getDialogPane().getStylesheets().add(Main.class.getResource("/style.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("custom-dialog");
        dialog.setTitle("Edit Reminder");
        dialog.setHeaderText("Edit Reminder");
        ButtonType updateButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

        ComboBox<ReminderType> typeComboBox = new ComboBox<>();
        typeComboBox.getItems().addAll(
                ReminderType.ONE_DAY_BEFORE,
                ReminderType.ONE_WEEK_BEFORE,
                ReminderType.ONE_MONTH_BEFORE,
                ReminderType.CUSTOM_DATE
        );
        typeComboBox.setValue(currentReminder.getType());

        DatePicker datePicker = new DatePicker();
        datePicker.setValue(currentReminder.getReminderDate());
        datePicker.setDisable(currentReminder.getType() != ReminderType.CUSTOM_DATE);

        typeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            datePicker.setDisable(newVal != ReminderType.CUSTOM_DATE);
        });

        Label taskLabel = new Label(currentReminder.getTask().getTitle() + " (" + currentReminder.getTask().getId() + ")");
        Label deadlineLabel = new Label(currentReminder.getTask().getDeadline().toString());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        grid.add(new Label("Reminder Type:"), 0, 0);
        grid.add(typeComboBox, 1, 0);
        grid.add(new Label("Reminder Date:"), 0, 1);
        grid.add(datePicker, 1, 1);
        grid.add(new Label("Task:"), 0, 2);
        grid.add(taskLabel, 1, 2);
        grid.add(new Label("Task Deadline:"), 0, 3);
        grid.add(deadlineLabel, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == updateButtonType) {
                return new Pair<>(typeComboBox.getValue(), datePicker.getValue());
            }
            return null;
        });

        Optional<Pair<ReminderType, LocalDate>> result = dialog.showAndWait();
        result.ifPresent(pair -> {
            try {
                Task currentTask = currentReminder.getTask();
                reminderController.updateReminder(currentReminder, pair.getKey(), pair.getValue());
                table.setItems(FXCollections.observableArrayList(currentTask.getReminders()));
                table.refresh();
                refreshAll.run();
            } catch (IllegalArgumentException e) {
                showError("Error Editing Reminder", e.getMessage());
                return;
            }
        });
    }

    public static void showRemoveReminderDialog(Object item, ReminderController reminderController, TableView<Object> table, Runnable refreshAll) {
        if (!(item instanceof Reminder currentReminder)) {
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.getDialogPane().getStylesheets().add(Main.class.getResource("/style.css").toExternalForm());
        alert.getDialogPane().getStyleClass().add("alert");
        alert.setTitle("Confirm Removal");
        alert.setHeaderText("Remove Reminder");
        alert.setContentText("Are you sure you want to remove this reminder?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    Task currentTask = currentReminder.getTask();
                    reminderController.removeReminder(currentReminder);
                    table.setItems(FXCollections.observableArrayList(currentTask.getReminders()));
                    table.refresh();
                    refreshAll.run();
                } catch (IllegalArgumentException e) {
                    showError("Error Removing Reminder", e.getMessage());
                    return;
                }
            }
        });
    }

    private static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.getDialogPane().getStylesheets().add(Main.class.getResource("/style.css").toExternalForm());
        alert.getDialogPane().getStyleClass().add("alert");
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static String getFriendlyStatus(TaskStatus status) {
        return switch (status) {
            case OPEN -> "Open";
            case IN_PROGRESS -> "In Progress";
            case POSTPONED -> "Postponed";
            case COMPLETED -> "Completed";
            case DELAYED -> "Delayed";
        };
    }

    private static TaskStatus getTaskStatusFromFriendlyName(String status) {
        return switch (status) {
            case "Open" -> TaskStatus.OPEN;
            case "In Progress" -> TaskStatus.IN_PROGRESS;
            case "Postponed" -> TaskStatus.POSTPONED;
            case "Completed" -> TaskStatus.COMPLETED;
            case "Delayed" -> TaskStatus.DELAYED;
            default -> throw new IllegalArgumentException("Invalid status selected.");
        };
    }


}
