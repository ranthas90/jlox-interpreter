package gui;

import javax.swing.*;

public class ErrorAlert extends JDialog {

    public ErrorAlert(String message) {
        setTitle("Unexpected error");
        getContentPane().add(new JLabel(message));
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
