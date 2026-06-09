package gui;

import javax.swing.*;
import java.awt.*;

public class BottomPanel extends JPanel {

    private JTextArea outputTextArea;

    public BottomPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Output"));

        outputTextArea = new JTextArea("Output result");
        outputTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        add(new JScrollPane(outputTextArea), BorderLayout.CENTER);
    }

    public JTextArea getOutputTextArea() {
        return outputTextArea;
    }
}
