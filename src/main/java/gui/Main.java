package gui;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            SwingUtilities.invokeLater(() -> {
                JFrame frame = new MainWindow();
                frame.setVisible(true);
            });

        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            ErrorAlert error = new ErrorAlert(e.getMessage());
            error.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        }
    }
}
