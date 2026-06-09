package gui;

import virtualmachine.vm.VirtualMachine;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class MainWindow extends JFrame {

    private MainToolBar mainToolBar;
    private TopPanel topPanel;
    private LeftPanel leftPanel;
    private RightPanel rightPanel;

    public MainWindow() {

        createToolBar();
        createMainPanel();

        addOpenFileActionListener();
        addRunCodeActionListener();
        addClearCodeActionListener();

        setTitle("JLox interpreter (v1.0.0)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
    }


    private void createToolBar() {
        mainToolBar = new MainToolBar();
        this.getContentPane().add(mainToolBar, BorderLayout.PAGE_START);
    }

    private void createMainPanel() {

        JPanel main = new JPanel(new BorderLayout());

        // Top panel
        topPanel = new TopPanel();

        // Content panel
        JPanel content = new JPanel(new GridLayout(1, 2));

        // Left panel
        leftPanel = new LeftPanel();

        // Right panel
        rightPanel = new RightPanel();

        content.add(leftPanel);
        content.add(rightPanel);

        main.add(topPanel, BorderLayout.PAGE_START);
        main.add(content, BorderLayout.CENTER);

        this.getContentPane().add(main, BorderLayout.CENTER);
    }

    private void addOpenFileActionListener() {
        mainToolBar.getOpenFileButton().addActionListener(actionListener -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.addChoosableFileFilter(new LoxFileFilter());
            int returnValue = fileChooser.showDialog(this.getContentPane(), "Select");
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try {
                    List<String> lines = Files.readAllLines(file.toPath());
                    leftPanel.getCodeTextArea().setText(String.join("\r\n", lines));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void addRunCodeActionListener() {
        mainToolBar.getRunCodeButton().addActionListener(actionListener -> {
            String sourceCode = leftPanel.getCodeTextArea().getText();
            VirtualMachine vm = new VirtualMachine();
            vm.interpret(sourceCode);
            // TODO: copia el contenido de left panel, y lo pasa al AST/VM para su ejecución.
            // TODO: el resultado de los dos se muestra en la ventana de output
            // TODO: El disassembler deberá mostrar el resultado en la ventana correspondiente
        });
    }

    private void addClearCodeActionListener() {
        mainToolBar.getClearCodeButton().addActionListener(actionListener -> {
            leftPanel.getCodeTextArea().setText("");
        });
    }
}
