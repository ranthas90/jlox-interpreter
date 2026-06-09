package gui;

import javax.swing.*;

public class SuccessAlert extends JDialog {

    public SuccessAlert(String message) {
        setTitle("Success");
        add(new JLabel(message));
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
