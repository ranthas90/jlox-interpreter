package gui;

import javax.swing.*;
import java.awt.*;

public class LeftPanel extends JPanel {

    private JTextArea codeTextArea;

    public LeftPanel() {
        createCodeTextArea();
        createPanel();
    }

    private void createCodeTextArea() {
        codeTextArea = new JTextArea("Select a file to view/edit its code or write your lox script");
        codeTextArea.setFont(new Font("Jetbrains Mono", Font.PLAIN, 14));
        codeTextArea.setTabSize(4);
        codeTextArea.setLineWrap(false);
    }

    private void createPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Immediate code"));
        add(new JScrollPane(codeTextArea), BorderLayout.CENTER);
    }

    public JTextArea getCodeTextArea() {
        return codeTextArea;
    }
}
