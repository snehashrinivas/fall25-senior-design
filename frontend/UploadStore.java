/**
 * This class is to show the files that the user has uploaded during the session through the menu on the left side.
 * It also includes functionality to parse these files and add them to the backend for processing and generation.
 * Written by Sneha Shrinivas
 */
package frontend;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.util.Collection;

import backend.UpdatedPreProcessing;


public final class UploadStore {
    private static final ObservableList<File> imported = FXCollections.observableArrayList();
    private UploadStore() {}

    public static ObservableList<File> getImported() { return imported; }

    /**
     * Adds all uploaded files to the import list and send them to backend for preprocessing
     * @param files
     * Written by Sneha Shrinivas
     */
    public static void addAll(Collection<File> files) {
        imported.addAll(files);

        // for each file just uploaded, send it to backend for preprocessing
        for (File f : files) {
            UpdatedPreProcessing.processFileFromGui(f);
        }
    }

    public static void remove(File f) { imported.remove(f); }
    public static void clear() { imported.clear(); }
}
