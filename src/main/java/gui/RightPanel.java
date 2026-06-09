package gui;

import javax.swing.*;
import java.awt.*;

public class RightPanel extends JPanel {

    private JTextArea disassemblyTextArea;
    private JTextArea outputTextArea;

    public RightPanel() {
        setLayout(new GridLayout(2, 1));
        createTopPanel();
        createBottomPanel();
    }

    private void createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Disassembler"));
        disassemblyTextArea = new JTextArea("Disassembly result");
        disassemblyTextArea.setFont(new Font("Jetbrains Mono", Font.PLAIN, 14));
        panel.add(new JScrollPane(disassemblyTextArea), BorderLayout.CENTER);
        add(panel);
    }

    private void createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Output"));
        outputTextArea = new JTextArea("Output result");
        outputTextArea.setFont(new Font("Jetbrains Mono", Font.PLAIN, 14));
        panel.add(new JScrollPane(outputTextArea), BorderLayout.CENTER);
        add(panel);
    }

    public JTextArea getOutputTextArea() {
        return outputTextArea;
    }
}
