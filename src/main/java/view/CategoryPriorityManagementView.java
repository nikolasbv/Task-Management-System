package view;

import controller.PriorityController;
import controller.ReminderController;
import controller.TaskController;
import controller.CategoryController;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import model.*;
import model.Priority;
import org.example.Main;

import java.util.ArrayList;
import java.util.List;

public class CategoryPriorityManagementView {

    public static Tab createCategoryPriorityManagementTab(TaskController taskController,
                                                          CategoryController categoryController,
                                                          PriorityController priorityController,
                                                          ReminderController reminderController,
                                                          TableView<Object> categoryTable,
                                                          TableView<Object> priorityTable,
                                                          Runnable refreshAll) {
        Tab tab = new Tab("Category and Priority Management");
        tab.setContent(createCategoryPriorityManagementPane(taskController, categoryController, priorityController,
                                                            reminderController, categoryTable, priorityTable, refreshAll));
        return tab;
    }

    public static Pane createCategoryPriorityManagementPane(
            TaskController taskController,
            CategoryController categoryController,
            PriorityController priorityController,
            ReminderController reminderController,
            TableView<Object> categoryTable,
            TableView<Object> priorityTable,
            Runnable refreshAll) {

        Pane categoryPane = createManagementPane(true, taskController, categoryController, priorityController,
                                                    reminderController, categoryTable, refreshAll);
        Pane priorityPane = createManagementPane(false, taskController, categoryController, priorityController,
                                                    reminderController, priorityTable, refreshAll);

        Text text1 = new Text("This is the management panel for Categories and Priorities. "
                + "You can create, edit, and remove categories and priorities.");
        Text text2 = new Text("Note: The default priority cannot be edited or removed.");

        text1.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-fill: #343A40; -fx-font-family: 'Arial';");
        text2.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-fill: #343A40; -fx-font-family: 'Arial';");

        TextFlow headerTextFlow1 = new TextFlow(text1);
        TextFlow headerTextFlow2 = new TextFlow(text2);

        headerTextFlow1.setMaxWidth(750);
        headerTextFlow2.setMaxWidth(750);
        headerTextFlow1.setTextAlignment(TextAlignment.CENTER);
        headerTextFlow2.setTextAlignment(TextAlignment.CENTER);

        headerTextFlow1.getStyleClass().add("header-text");
        headerTextFlow2.getStyleClass().add("header-text");

        VBox headerContainer = new VBox(5, headerTextFlow1, headerTextFlow2);
        headerContainer.setAlignment(Pos.CENTER);
        headerContainer.setPadding(new Insets(15, 10, 5, 10));

        HBox container = new HBox(50, categoryPane, priorityPane);
        container.setPadding(new Insets(10));
        container.setAlignment(Pos.CENTER);

        VBox content = new VBox(10, headerContainer, container);
        content.setPadding(new Insets(5));
        content.setAlignment(Pos.CENTER);
        VBox.setVgrow(container, javafx.scene.layout.Priority.ALWAYS);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setPannable(true);

        // ---------------------------
        // DYNAMIC HEIGHT & WIDTH ADJUSTMENT WITH SCROLLING
        // ---------------------------
        scrollPane.heightProperty().addListener((obs, oldHeight, newHeight) -> {
            if (newHeight.doubleValue() < 500) {  // Adjust minimum height before scrolling
                scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            } else {
                scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            }
        });

        scrollPane.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            if (newWidth.doubleValue() < 900) {  // Adjust minimum width before scrolling
                scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            } else {
                scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            }
        });

        BorderPane root = new BorderPane();
        root.setCenter(scrollPane);
        return root;
    }

    private static Pane createManagementPane(boolean isCategory,
                                             TaskController taskController,
                                             CategoryController categoryController,
                                             PriorityController priorityController,
                                             ReminderController reminderController,
                                             TableView<Object> table,
                                             Runnable refreshAll) {

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // ---------------------------
        // MANAGEMENT TABLE
        // ---------------------------
        // TableView<Object> table = new TableView<>();
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        table.setSelectionModel(null);

        VBox.setVgrow(table, javafx.scene.layout.Priority.ALWAYS);

        // ---------------------------
        // TABLE COLUMNS
        // ---------------------------
        List<Main.AttributeColumnSpec> attributeSpecs = new ArrayList<>();
        attributeSpecs.add(new Main.AttributeColumnSpec(
                isCategory ? "Category Name" : "Priority Name",
                item -> isCategory ? ((Category) item).getName() : ((Priority) item).getName()));

        final double actionsColumnWidth = 180;
        final double attrColMinWidth = 180;
        table.setMinWidth(385);
        table.setPrefWidth(385);
        table.setMaxWidth(385);

        List<TableColumn<Object, String>> attributeColumns = new ArrayList<>();
        for (Main.AttributeColumnSpec spec : attributeSpecs) {
            TableColumn<Object, String> col = new TableColumn<>(spec.getHeader());
            col.setCellValueFactory(data -> new SimpleStringProperty(spec.getExtractor().apply(data.getValue())));
            col.setMinWidth(attrColMinWidth);
            col.setPrefWidth(attrColMinWidth);
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

                    if (isCategory) {
                        editButton.setDisable(false);
                        removeButton.setDisable(false);
                        editButton.setOnAction(event -> showEditCategoryDialog(currentItem, categoryController, table, refreshAll));
                        removeButton.setOnAction(event -> showRemoveCategoryDialog(currentItem, categoryController, taskController, reminderController, table, refreshAll));
                    } else {
                        Priority currentPriority = (Priority) currentItem;
                        boolean isDefaultPriority = currentPriority.equals(priorityController.getDefaultPriority());

                        editButton.setDisable(isDefaultPriority);
                        removeButton.setDisable(isDefaultPriority);

                        editButton.setOnAction(event -> {
                            if (!isDefaultPriority) {
                                showEditPriorityDialog(currentItem, priorityController, table, refreshAll);
                            }
                        });

                        removeButton.setOnAction(event -> {
                            if (!isDefaultPriority) {
                                showRemovePriorityDialog(currentItem, priorityController, taskController, table, refreshAll);
                            }
                        });
                    }

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
        table.setItems(FXCollections.observableArrayList(
                isCategory ? categoryController.getCategories() : priorityController.getPriorities()));

        VBox tableContainer = new VBox(table);
        tableContainer.setAlignment(Pos.CENTER);
        tableContainer.setPadding(new Insets(5));
        tableContainer.setMinHeight(200);
        tableContainer.setPrefHeight(Region.USE_COMPUTED_SIZE);
        VBox.setVgrow(tableContainer, javafx.scene.layout.Priority.ALWAYS);

        // ---------------------------
        // ADD BUTTON
        // ---------------------------
        String addButtonText = isCategory ? "Add New Category" : "Add New Priority";
        Button addButton = new Button(addButtonText);
        addButton.setOnAction(event -> {
            if (isCategory) {
                showAddCategoryDialog(categoryController, table, refreshAll);
            } else {
                showAddPriorityDialog(priorityController, table, refreshAll);
            }
        });

        // ---------------------------
        // LAYOUT MANAGEMENT
        // ---------------------------
        HBox addButtonContainer = new HBox(addButton);
        addButtonContainer.setAlignment(Pos.CENTER);

        VBox centerContent = new VBox(10, tableContainer, addButtonContainer);
        centerContent.setPadding(new Insets(10));
        VBox.setVgrow(tableContainer, javafx.scene.layout.Priority.ALWAYS);

        root.setCenter(centerContent);

        return root;
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

    // -------------------------
    // Category Methods
    // -------------------------
    private static void showEditCategoryDialog(Object item, CategoryController categoryController,
                                               TableView<Object> table, Runnable refreshAll) {
        if (!(item instanceof Category currentCategory)) {
            return;
        }

        TextInputDialog dialog = new TextInputDialog(currentCategory.getName());
        dialog.setTitle("Edit Category");
        dialog.setHeaderText("Edit Category");
        dialog.setContentText("Enter new category name:");

        dialog.showAndWait().ifPresent(newVal -> {
            try {
                categoryController.updateCategory(currentCategory, newVal.trim());
                refreshAll.run();
            } catch (IllegalArgumentException e) {
                showError("Error Editing Category", e.getMessage());
                return;
            }
        });
    }

    private static void showRemoveCategoryDialog(Object item, CategoryController categoryController,
                                                 TaskController taskController, ReminderController reminderController,
                                                 TableView<Object> table, Runnable refreshAll) {
        if (!(item instanceof Category currentCategory)) {
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.getDialogPane().getStylesheets().add(Main.class.getResource("/style.css").toExternalForm());
        alert.getDialogPane().getStyleClass().add("alert");
        alert.setTitle("Confirm Removal");
        alert.setHeaderText("Remove Category");
        alert.setContentText("Are you sure you want to remove this category?\nThis will also remove all associated tasks.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    categoryController.removeCategory(currentCategory, taskController, reminderController);
                    table.setItems(FXCollections.observableArrayList(categoryController.getCategories()));
                    refreshAll.run();
                } catch (IllegalArgumentException e) {
                    showError("Error Removing Category", e.getMessage());
                    return;
                }
            }
        });
    }

    private static void showAddCategoryDialog(CategoryController categoryController, TableView<Object> table,
                                              Runnable refreshAll) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Category");
        dialog.setHeaderText("Create a New Category");
        dialog.setContentText("Enter category name:");

        dialog.showAndWait().ifPresent(val -> {
            try {
                Category newCategory = new Category(val.trim());
                categoryController.addCategory(newCategory);
                table.setItems(FXCollections.observableArrayList(categoryController.getCategories()));
                refreshAll.run();
            } catch (IllegalArgumentException e) {
                showError("Error Adding Category", e.getMessage());
                return;
            }
        });
    }

    // -------------------------
    // Priority Methods
    // -------------------------
    private static void showEditPriorityDialog(Object item, PriorityController priorityController,
                                               TableView<Object> table, Runnable refreshAll) {
        if (!(item instanceof Priority currentPriority)) {
            return;
        }

        if (currentPriority.isDefault()) {
            showError("Edit Priority", "The default priority cannot be edited.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(currentPriority.getName());
        dialog.setTitle("Edit Priority");
        dialog.setHeaderText("Edit Priority");
        dialog.setContentText("Enter new priority name:");

        dialog.showAndWait().ifPresent(newVal -> {
            try {
                priorityController.updatePriority(currentPriority, newVal.trim());
                // table.refresh();
                refreshAll.run();
            } catch (IllegalArgumentException e) {
                showError("Error Editing Priority", e.getMessage());
                return;
            }
        });
    }


    private static void showRemovePriorityDialog(Object item, PriorityController priorityController,
                                                 TaskController taskController, TableView<Object> table,
                                                 Runnable refreshAll) {
        if (!(item instanceof Priority currentPriority)) {
            return;
        }

        if (currentPriority.isDefault()) {
            showError("Remove Priority", "The default priority cannot be removed.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.getDialogPane().getStylesheets().add(Main.class.getResource("/style.css").toExternalForm());
        alert.getDialogPane().getStyleClass().add("alert");
        alert.setTitle("Confirm Removal");
        alert.setHeaderText("Remove Priority");
        alert.setContentText("Are you sure you want to remove this priority?\nThis will also set the default priority to all associated tasks.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    priorityController.removePriority(currentPriority, taskController);
                    table.setItems(FXCollections.observableArrayList(priorityController.getPriorities()));
                    refreshAll.run();
                } catch (IllegalArgumentException e) {
                    showError("Error Removing Priority", e.getMessage());
                    return;
                }
            }
        });
    }

    private static void showAddPriorityDialog(PriorityController priorityController, TableView<Object> table,
                                              Runnable refreshAll) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Priority");
        dialog.setHeaderText("Create a New Priority");
        dialog.setContentText("Enter priority name:");

        dialog.showAndWait().ifPresent(val -> {
            try {
                Priority newPriority = new Priority(val.trim());
                priorityController.addPriority(newPriority);
                table.setItems(FXCollections.observableArrayList(priorityController.getPriorities()));
                refreshAll.run();
            } catch (IllegalArgumentException e) {
                showError("Error Adding Priority", e.getMessage());
                return;
            }
        });
    }


}
