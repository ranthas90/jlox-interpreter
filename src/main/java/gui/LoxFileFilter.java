package gui;

import javax.swing.filechooser.FileFilter;
import java.io.File;

public class LoxFileFilter extends FileFilter {

    @Override
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return false;
        }

        return f.getName().toUpperCase().endsWith(".LOX");
    }

    @Override
    public String getDescription() {
        return "Lox files";
    }
}
