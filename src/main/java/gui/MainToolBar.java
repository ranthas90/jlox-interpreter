package gui;

import javax.swing.*;
import java.awt.*;

public class MainToolBar extends JToolBar {

    private JButton openFileButton = new JButton();
    private JButton openFolderButton = new JButton();
    private JButton runCodeButton = new JButton();
    private JButton clearCodeButton = new JButton();

    public MainToolBar() {
        setFloatable(false);
        createButton(openFileButton, "/images/new-document.png", "Open file");
        createButton(openFolderButton, "/images/open-folder.png", "Open folder");
        addSeparator();
        createButton(runCodeButton, "/images/play-button.png", "Run code");
        createButton(clearCodeButton, "/images/recycle-bin.png", "Clear code");

        // TODO: Ya veremos que hago con este botón
        openFolderButton.addActionListener(e -> {
            JDialog dialog = new JDialog();
            dialog.setSize(400, 200);
            dialog.setLocationRelativeTo(null);
            dialog.getContentPane().add(new JLabel("Not implemented yet!"));
            dialog.setVisible(true);
        });
    }

    private void createButton(JButton button, String imagePath, String altText) {
        // TODO: check null due to image not found
        ImageIcon icon = new ImageIcon(MainWindow.class.getResource(imagePath));
        double maxSize = 16.0D;

        int width = icon.getIconWidth();
        int height = icon.getIconHeight();

        double scale = Math.min((maxSize / width), (maxSize / height));
        int newWidth = (int) (width * scale);
        int newHeight = (int) (height * scale);
        Image scaledIcon = icon.getImage().getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        ImageIcon buttonIcon = new ImageIcon(scaledIcon, altText);
        button.setIcon(buttonIcon);

        add(button);
    }

    public JButton getOpenFileButton() {
        return openFileButton;
    }

    public JButton getRunCodeButton() {
        return runCodeButton;
    }

    public JButton getClearCodeButton() {
        return clearCodeButton;
    }
}
