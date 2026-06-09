package gui;

import virtualmachine.vm.VirtualMachine;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class MainWindow extends JFrame {

    private MainToolBar mainToolBar;
    private TopPanel topPanel;
    private LeftPanel leftPanel;
    private RightPanel rightPanel;
    private BottomPanel bottomPanel;

    public MainWindow() {

        createToolBar();
        createMainPanel();

        addOpenFileActionListener();
        addSaveFileActionListener();
        addRunCodeActionListener();
        addDebugCodeActionListener();
        addClearCodeActionListener();

        setTitle("JLox interpreter (v1.0.0)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
    }


    private void createToolBar() {
        mainToolBar = new MainToolBar();
        this.getContentPane().add(mainToolBar, BorderLayout.PAGE_START);
    }

    private void createMainPanel() {

        JPanel main = new JPanel(new BorderLayout());

        topPanel = new TopPanel();
        bottomPanel = new BottomPanel();

        JPanel content = new JPanel(new GridLayout(1, 2));
        leftPanel = new LeftPanel();
        rightPanel = new RightPanel();

        content.add(leftPanel);
        content.add(rightPanel);

        main.add(topPanel, BorderLayout.PAGE_START);
        main.add(content, BorderLayout.CENTER);
        main.add(bottomPanel, BorderLayout.PAGE_END);

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
                    new ErrorAlert(e.getMessage());
                }
            }
        });
    }

    private void addSaveFileActionListener() {
        mainToolBar.getSaveFileButton().addActionListener(actionListener -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int returnValue = fileChooser.showSaveDialog(this.getContentPane());
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                try (FileWriter fw = new FileWriter(fileChooser.getSelectedFile() + ".lox")) {
                    fw.write(leftPanel.getCodeTextArea().getText());
                    new SuccessAlert("File saved successfully");
                } catch (IOException e) {
                    new ErrorAlert(e.getMessage());
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
            // TODO: esta ejecución no usará el flag de debug!!!!
        });
    }

    private void addDebugCodeActionListener() {
        mainToolBar.getDebugCodeButton().addActionListener(actionListener -> {
            String sourceCode = leftPanel.getCodeTextArea().getText();
            VirtualMachine vm = new VirtualMachine();
            vm.interpret(sourceCode);
            // TODO: esta ejecución marca el flag de debug, así que tenemos que mostrar en la ventana de disassembly los chunks pila y demás
        });
    }

    private void addClearCodeActionListener() {
        mainToolBar.getClearCodeButton().addActionListener(actionListener -> {
            leftPanel.getCodeTextArea().setText("");
        });
    }
}
