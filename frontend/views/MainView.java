/*
 * This class helps show the manu view of the application that can be expanded or collapsed.
 * It also manages the center content area where different views can be displayed.
 * We can access the menu from any of the other screens, so it is persistent throughout views of the application.
 * Written by Sneha Shrinivas
 */


package frontend.views;

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

    public static Parent create() {
        if (root != null) return root;

        root = new BorderPane();

        // ===== Header with Menu toggle =====
        HBox header = new HBox(10);
        header.setPadding(new Insets(12, 16, 12, 16));
        header.setAlignment(Pos.CENTER_LEFT);

        menuToggle = new ToggleButton("☰ Menu");
        menuToggle.setSelected(true);
        menuToggle.setTooltip(new Tooltip("Show/Hide sidebar (Ctrl+M)"));

        Label title = Views.title("Sentence Builder");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label subtitle = new Label("Sentence Generation");
        subtitle.setStyle("-fx-font-size: 13; -fx-text-fill: #666;");

        header.getChildren().addAll(menuToggle, title, spacer, subtitle);

        // ===== Sidebar (collapsible) =====
        side = buildSidebar(subtitle);
        root.setLeft(side);

        // ===== Initial center content =====
        setCenter(HomeView.create(), "Sentence Generation");

        // ===== Wire header toggle and shortcuts =====
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

    /** Build the left sidebar with collapsible sections. */
    private static VBox buildSidebar(Label subtitleLabel) {
        VBox sideBox = new VBox(12);
        sideBox.setPadding(new Insets(12));
        sideBox.setPrefWidth(280);
        sideBox.setStyle("-fx-background-color: #fafafa; -fx-border-color: #ddd; -fx-border-width: 0 1 0 0;");

        // ===== File Upload (now inside a collapsible TitledPane) =====
        // Choose + Upload
        Button btnChoose = new Button("Choose Files…");
        Button btnUpload = new Button("Upload");
        btnUpload.setDisable(true);
        HBox chooseRow = new HBox(8, btnChoose, btnUpload);
        chooseRow.setAlignment(Pos.CENTER_LEFT);

        // Staging list
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

        Button btnRemove = new Button("Remove");
        Button btnClear  = new Button("Clear");
        btnRemove.setOnAction(e -> {
            File sel = stagingList.getSelectionModel().getSelectedItem();
            if (sel != null) staging.remove(sel);
        });
        btnClear.setOnAction(e -> staging.clear());
        HBox stageActions = new HBox(8, btnRemove, btnClear);
        stageActions.setAlignment(Pos.CENTER_RIGHT);

        VBox stagingBox = new VBox(8,
                new Label("Selected (not uploaded):"),
                stagingList,
                stageActions
        );
        stagingBox.setPadding(new Insets(8));
        stagingBox.setStyle("-fx-border-color:#ddd; -fx-border-radius:8; -fx-background-radius:8; -fx-border-width:1;");

        VBox uploadContent = new VBox(10, chooseRow, stagingBox);
        TitledPane uploadPane = new TitledPane("File Upload", uploadContent);
        uploadPane.setExpanded(false); // collapsed by default

        // Enable upload when something is staged
        staging.addListener((javafx.collections.ListChangeListener<? super File>) c ->
                btnUpload.setDisable(staging.isEmpty())
        );

        // Choose and Upload actions
        btnChoose.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Choose Files");
            List<File> chosen = fc.showOpenMultipleDialog(root.getScene() == null ? null : root.getScene().getWindow());
            if (chosen != null && !chosen.isEmpty()) staging.addAll(chosen);
        });

        Label status = new Label();
        status.setStyle("-fx-text-fill:#2e7d32; -fx-font-size:12;");

        btnUpload.setOnAction(e -> {
            if (!staging.isEmpty()) {
                int n = staging.size();
                UploadStore.addAll(staging);
                staging.clear();
                status.setText("Uploaded " + n + " file" + (n == 1 ? "" : "s") + ".");
                // Optionally auto-expand Imported Files after first upload
                importedPane.setExpanded(true);
            }
        });

        // ===== Imported Files (already collapsible) =====
        ListView<File> importedList = new ListView<>(UploadStore.getImported());
        importedList.setPlaceholder(new Label("No files uploaded."));
        importedList.setPrefHeight(120);
        importedList.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(File f, boolean empty) {
                super.updateItem(f, empty);
                setText(empty || f == null ? null : f.getName());
            }
        });
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
        importedPane.setExpanded(false); // collapsed by default

        // ===== Actions =====
        Label actionsTitle = new Label("Actions");
        actionsTitle.setStyle("-fx-font-weight: bold;");
        Button btnSentence   = new Button("Sentence Generation");
        Button btnCompletion = new Button("Word Completion");
        btnSentence.setMaxWidth(Double.MAX_VALUE);
        btnCompletion.setMaxWidth(Double.MAX_VALUE);

        btnSentence.setOnAction(e -> setCenter(HomeView.create(), "Sentence Builder - Home"));
        btnCompletion.setOnAction(e -> {
            String[] options = {"quick", "dog", "man"}; // placeholder
            setCenter(AutocompleteView.create(options), "Word Completion");
        });

        // Keyboard: Enter = Upload (if enabled), Esc = collapse/expand menu
        sideBox.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER && !btnUpload.isDisabled()) btnUpload.fire();
            if (e.getCode() == KeyCode.ESCAPE) menuToggle.fire();
        });

        sideBox.getChildren().addAll(
                uploadPane,
                importedPane,
                new Separator(),
                actionsTitle,
                btnSentence,
                btnCompletion,
                status
        );

        return sideBox;
    }

    // Keep a static ref so we can expand Imported after upload
    private static TitledPane importedPane;

    /** Swap the center content and update the subtitle in header. */
    public static void setCenter(Parent content, String subtitle) {
        if (root == null) create();
        StackPane wrapper = new StackPane(content);
        wrapper.setPadding(new Insets(16));
        root.setCenter(wrapper);

        // update subtitle (header is an HBox: [toggle, title, spacer, subtitle])
        HBox header = (HBox) root.getTop();
        if (header != null && header.getChildren().size() >= 4) {
            Label sub = (Label) header.getChildren().get(3);
            sub.setText(subtitle == null ? "" : subtitle);
        }
    }
}
