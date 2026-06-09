package gui;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new MainWindow();
            frame.setVisible(true);
        });
    }
}
