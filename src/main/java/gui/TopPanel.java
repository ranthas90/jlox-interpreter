package gui;

import javax.swing.*;
import java.awt.*;

public class TopPanel extends JPanel {

    public TopPanel() {
        setLayout(new GridLayout(1,1));
        setBorder(BorderFactory.createTitledBorder("Execution options"));

        JRadioButton ast = new JRadioButton("AST walker");
        ast.setActionCommand("AST");

        JRadioButton vm = new JRadioButton("Virtual machine");
        vm.setActionCommand("VM");
        vm.setSelected(true);

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(ast);
        buttonGroup.add(vm);

        add(ast);
        add(vm);
    }
}
