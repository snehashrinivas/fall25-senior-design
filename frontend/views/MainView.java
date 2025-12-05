/**
 * This class helps show the menu view of the application that can be expanded or collapsed.
 * It also manages the center content area where different views can be displayed.
 * We can access the menu from any of the other screens, so it is persistent throughout views of the application.
 * Written by Sneha Shrinivas
 */

package frontend.views;

import frontend.views.HomeView;
import frontend.views.Views;

import frontend.UploadStore;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;

public class MainView {

    private static BorderPane root;   // persistent shell
    private static VBox side;         // keep a reference so we can hide/show
    private static ToggleButton menuToggle;
    private static TitledPane importedPane;

    /**
     * Creates the main view with a header, collapsible sidebar, and center content area.
     * @return Parent node representing the main view.
     * Written by Sneha Shrinivas
     */
    public static Parent create() {
        if (root != null) return root;

        root = new BorderPane();
        root.setStyle("-fx-background-color: " + Views.APP_BG + ";");

        // Header with Menu toggle
        HBox header = new HBox(10);
        header.setPadding(new Insets(12, 16, 12, 16));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #e5e7eb;" +
                        "-fx-border-width: 0 0 1 0;"
        );

        menuToggle = new ToggleButton("☰ Menu");
        menuToggle.setSelected(true);
        menuToggle.setTooltip(new Tooltip("Show/Hide sidebar (Ctrl+M)"));
        menuToggle.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-border-color: #d1d5db;" +
                        "-fx-border-radius: 999;" +
                        "-fx-background-radius: 999;" +
                        "-fx-padding: 4 10 4 10;"
        );

        Label title = Views.title("Sentence Builder");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label subtitle = new Label("Sentence Generation");
        subtitle.setStyle("-fx-font-size: 13; -fx-text-fill: " + Views.TEXT_MUTED + ";");

        header.getChildren().addAll(menuToggle, title, spacer, subtitle);

        // Collapsible sidebar
        side = buildSidebar(subtitle);
        root.setLeft(side);

        // Initial center content
        setCenter(HomeView.create(), "Sentence Generation");

        //  Wire header toggle and shortcuts
        menuToggle.setOnAction(e -> {
            if (menuToggle.isSelected()) {
                root.setLeft(side);
            } else {
                root.setLeft(null); // hide the whole sidebar
            }
        });

        root.setOnKeyPressed(e -> {
            if (e.isControlDown() && e.getCode() == KeyCode.M) menuToggle.fire();
        });

        root.setTop(header);
        return root;
    }

    /**
     * Builds the left sidebar with file upload and action buttons.
     * @param subtitleLabel
     * @return VBox representing the sidebar.
     * Written by Sneha Shrinivas
     */

    private static VBox buildSidebar(Label subtitleLabel) {
        VBox sideBox = new VBox(12);
        sideBox.setPadding(new Insets(12));
        sideBox.setPrefWidth(280);

        //Wrap sidebar contents in a card
        VBox inner = new VBox(12);
        inner.setPadding(new Insets(8));
        inner.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 16;" +
                        "-fx-border-radius: 16;" +
                        "-fx-border-color: #e5e7eb;" +
                        "-fx-border-width: 1;"
        );

        // File Upload (collapsible)
        Button btnChoose = Views.secondaryButton("Choose Files…");
        Button btnUpload = Views.primaryButton("Upload");
        btnUpload.setDisable(true);
        HBox chooseRow = new HBox(8, btnChoose, btnUpload);
        chooseRow.setAlignment(Pos.CENTER_LEFT);

        // Staging area for selected files
        ObservableList<File> staging = FXCollections.observableArrayList();
        ListView<File> stagingList = new ListView<>(staging);
        stagingList.setPlaceholder(new Label("No files selected."));
        stagingList.setPrefHeight(120);
        stagingList.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(File f, boolean empty) {
                super.updateItem(f, empty);
                setText(empty || f == null ? null : f.getName());
            }
        });

        // Staging area actions (remove, clear)
        Button btnRemove = Views.secondaryButton("Remove");
        Button btnClear  = Views.secondaryButton("Clear");
        HBox stageActions = new HBox(8, btnRemove, btnClear);
        stageActions.setAlignment(Pos.CENTER_RIGHT);

        btnRemove.setOnAction(e -> {
            File sel = stagingList.getSelectionModel().getSelectedItem();
            if (sel != null) staging.remove(sel);
        });
        btnClear.setOnAction(e -> staging.clear());

        VBox stagingBox = new VBox(8,
                new Label("Selected (not uploaded):"),
                stagingList,
                stageActions
        );

        stagingBox.setPadding(new Insets(8));
        stagingBox.setStyle(
                "-fx-border-color:#e5e7eb;" +
                        "-fx-border-radius:10;" +
                        "-fx-background-radius:10;" +
                        "-fx-border-width:1;"
        );

        // Assemble upload pane
        VBox uploadContent = new VBox(10, chooseRow, stagingBox);
        TitledPane uploadPane = new TitledPane("File Upload", uploadContent);
        uploadPane.setExpanded(false);

        // Enable upload when something is staged
        staging.addListener((javafx.collections.ListChangeListener<? super File>) c ->
                btnUpload.setDisable(staging.isEmpty())
        );

        // Choose & upload
        btnChoose.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Choose Files");
            List<File> chosen = fc.showOpenMultipleDialog(root.getScene() == null ? null : root.getScene().getWindow());
            if (chosen != null && !chosen.isEmpty()) staging.addAll(chosen);
        });

        Label status = new Label();
        status.setStyle("-fx-text-fill:#16a34a; -fx-font-size:12;");

        // Imported Files
        ListView<File> importedList = new ListView<>(UploadStore.getImported());
        importedList.setPlaceholder(new Label("No files uploaded."));
        importedList.setPrefHeight(120);
        importedList.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(File f, boolean empty) {
                super.updateItem(f, empty);
                setText(empty || f == null ? null : f.getName());
            }
        });
        // Context menu to remove imported files
        importedList.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                File sel = importedList.getSelectionModel().getSelectedItem();
                if (sel != null) {
                    ContextMenu cm = new ContextMenu();
                    MenuItem rm = new MenuItem("Remove");
                    rm.setOnAction(a -> UploadStore.remove(sel));
                    cm.getItems().add(rm);
                    cm.show(importedList, e.getScreenX(), e.getScreenY());
                }
            }
        });

        importedPane = new TitledPane("Imported Files", importedList);
        importedPane.setExpanded(false);

        // Upload action
        btnUpload.setOnAction(e -> {
            if (!staging.isEmpty()) {
                int n = staging.size();
                UploadStore.addAll(staging);
                staging.clear();
                status.setText("Uploaded " + n + " file" + (n == 1 ? "" : "s") + ".");
                importedPane.setExpanded(true);
            }
        });

        Label actionsTitle = new Label("Actions");
        actionsTitle.setStyle("-fx-font-weight: bold; -fx-text-fill:" + Views.TEXT_DEFAULT + ";");

        // Sentence Generation button from sidebar
        Button btnSentence = Views.primaryButton("Sentence Generation");
        btnSentence.setMaxWidth(Double.MAX_VALUE);
        btnSentence.setOnAction(e -> {
            menuToggle.setSelected(false);
            root.setLeft(null);
            setCenter(HomeView.create(), "Sentence Builder - Home");
        });

        // Assemble sidebar
        inner.getChildren().addAll(
                uploadPane,
                importedPane,
                new Separator(),
                actionsTitle,
                btnSentence,
                status
        );

        sideBox.getChildren().add(inner);

        // Keyboard shortcuts for sidebar
        sideBox.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER && !btnUpload.isDisabled()) btnUpload.fire();
            if (e.getCode() == KeyCode.ESCAPE) menuToggle.fire();
        });

        return sideBox;
    }

    /**
     * Sets the center content area with the given content and subtitle.
     * @param content The content to display in the center area.
     * @param subtitle The subtitle to display in the header.
     * Written by Sneha Shrinivas
     */
    public static void setCenter(Parent content, String subtitle) {
        if (root == null) create();
        StackPane wrapper = new StackPane(content);
        wrapper.setPadding(new Insets(24));
        wrapper.setStyle("-fx-background-color: " + Views.APP_BG + ";");
        root.setCenter(wrapper);

        HBox header = (HBox) root.getTop();
        if (header != null && header.getChildren().size() >= 4) {
            Label sub = (Label) header.getChildren().get(3);
            sub.setText(subtitle == null ? "" : subtitle);
        }
    }
}
