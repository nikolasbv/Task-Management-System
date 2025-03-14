package view;

import controller.ReminderController;
import controller.TaskController;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.util.Pair;
import model.*;
import org.example.Main;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReminderManagementView {

    public static Tab createReminderManagementTab(TaskController taskController,
                                                  ReminderController reminderController,
                                                  TableView<Object> table,
                                                  Runnable refreshAll
                                                  ) {
        Tab tab = new Tab("Reminder Management");
        tab.setContent(createReminderManagementPane(taskController, reminderController, table, refreshAll));
        return tab;
    }

    public static Pane createReminderManagementPane(
            TaskController taskController,
            ReminderController reminderController,
            TableView<Object> table,
            Runnable refreshAll) {

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // ---------------------------
        // HEADER TEXT
        // ---------------------------
        Label infoLabel = new Label("This is the management panel for Reminders. "
                + "You can create, edit, and remove reminders.");

        VBox headerContainer = new VBox(infoLabel);
        headerContainer.setAlignment(Pos.CENTER);
        headerContainer.setPadding(new Insets(10, 0, 10, 0));

        // ---------------------------
        // REMINDER TABLE
        // ---------------------------
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setSelectionModel(null);
        table.setMinWidth(900);
        table.setPrefWidth(Region.USE_COMPUTED_SIZE);

        VBox.setVgrow(table, javafx.scene.layout.Priority.ALWAYS);

        // ---------------------------
        // TABLE COLUMNS
        // ---------------------------
        List<Main.AttributeColumnSpec> attributeSpecs = new ArrayList<>();
        attributeSpecs.add(new Main.AttributeColumnSpec("Task", item -> {
            Task t = ((Reminder) item).getTask();
            return t.getTitle() + "\n(" + t.getId() + ")";
        }));
        attributeSpecs.add(new Main.AttributeColumnSpec("Deadline", item -> ((Reminder) item).getTask().getDeadline().toString()));
        attributeSpecs.add(new Main.AttributeColumnSpec("Type", item -> ((Reminder) item).getType().toString()));
        attributeSpecs.add(new Main.AttributeColumnSpec("Date", item -> ((Reminder) item).getReminderDate().toString()));

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
                    Object currentItem = getTableView().getItems().get(getIndex());
                    editButton.setOnAction(event -> showEditReminderDialog(currentItem, reminderController, table, refreshAll));
                    removeButton.setOnAction(event -> showRemoveReminderDialog(currentItem, reminderController, table, refreshAll));
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
        table.setItems(FXCollections.observableArrayList(reminderController.getAllReminders()));

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
        Button addButton = new Button("Add New Reminder");
        addButton.setOnAction(event -> showAddReminderDialog(reminderController, taskController, table, refreshAll));

        // ---------------------------
        // LAYOUT MANAGEMENT
        // ---------------------------
        HBox addButtonContainer = new HBox(addButton);
        addButtonContainer.setAlignment(Pos.CENTER);

        VBox scrollableContent = new VBox(10, headerContainer, tableContainer, addButtonContainer);
        scrollableContent.setPadding(new Insets(10));
        scrollableContent.setAlignment(Pos.CENTER);
        VBox.setVgrow(tableContainer, javafx.scene.layout.Priority.ALWAYS);

        tabScrollPane.setContent(scrollableContent);
        root.setCenter(tabScrollPane);

        // ---------------------------
        // DYNAMIC HEIGHT & WIDTH ADJUSTMENT WITH SCROLLING
        // ---------------------------
        root.heightProperty().addListener((obs, oldHeight, newHeight) -> {
            double availableHeight = newHeight.doubleValue() - infoLabel.getHeight() - addButton.getHeight() - 40;
            if (availableHeight < 200) {
                tabScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            } else {
                tabScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            }
            table.setPrefHeight(Math.max(200, availableHeight));
        });

        root.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            if (newWidth.doubleValue() < 900) {
                tabScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            } else {
                tabScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            }
        });

        return root;
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
                reminderController.updateReminder(currentReminder, pair.getKey(), pair.getValue());
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
                    reminderController.removeReminder(currentReminder);
                    table.setItems(FXCollections.observableArrayList(reminderController.getAllReminders()));
                    refreshAll.run();
                } catch (IllegalArgumentException e) {
                    showError("Error Removing Reminder", e.getMessage());
                    return;
                }
            }
        });
    }

    private static void showAddReminderDialog(ReminderController reminderController,
                                              TaskController taskController,
                                              TableView<Object> table,
                                              Runnable refreshAll) {
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

        ComboBox<Task> taskComboBox = new ComboBox<>(FXCollections.observableArrayList(taskController.getTasks()));

        taskComboBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Task item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getTitle() + " (" + item.getId() + ")");  // Display Task title and ID
            }
        });

        taskComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Task item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getTitle() + " (" + item.getId() + ")");  // Display Task title and ID in dropdown
            }
        });

        if (!taskComboBox.getItems().isEmpty()) {
            taskComboBox.setValue(taskComboBox.getItems().get(0));
        }

        Label deadlineLabel = new Label();
        if (!taskComboBox.getItems().isEmpty()) {
            deadlineLabel.setText("Deadline: " + taskComboBox.getItems().get(0).getDeadline());
        } else {
            deadlineLabel.setText("Deadline: N/A");
        }

        taskComboBox.valueProperty().addListener((obs, oldTask, newTask) -> {
            if (newTask != null) {
                deadlineLabel.setText("Deadline: " + newTask.getDeadline());
            } else {
                deadlineLabel.setText("Deadline: N/A");
            }
        });

        grid.add(new Label("Reminder Type:"), 0, 0);
        grid.add(typeComboBox, 1, 0);
        grid.add(new Label("Reminder Date:"), 0, 1);
        grid.add(datePicker, 1, 1);
        grid.add(new Label("Select Task:"), 0, 2);
        grid.add(taskComboBox, 1, 2);
        grid.add(deadlineLabel, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    Reminder newReminder = new Reminder(
                            typeComboBox.getValue(),
                            taskComboBox.getValue(),
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
                table.setItems(FXCollections.observableArrayList(reminderController.getAllReminders()));
                refreshAll.run();
            } catch (IllegalArgumentException e) {
                showError("Error Adding Reminder", e.getMessage());
                return;
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

}
