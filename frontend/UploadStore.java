/*
 * This class is to show the files that the user has uploaded during the session through the menu on the left side.
 * Written by Sneha Shrinivas
 */
package frontend;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.File;
import java.util.Collection;


public final class UploadStore {
    private static final ObservableList<File> imported = FXCollections.observableArrayList();
    private UploadStore() {}

    public static ObservableList<File> getImported() { return imported; }
    public static void addAll(Collection<File> files) { imported.addAll(files); }
    public static void remove(File f) { imported.remove(f); }
    public static void clear() { imported.clear(); }
}
