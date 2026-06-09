package gui;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class RightPanel extends JPanel {

    private DisassemblerTable disassemblerTable;

    public RightPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Disassembler"));
        disassemblerTable = new DisassemblerTable(new ChunkTableModel(List.of()));
        add(new JScrollPane(disassemblerTable), BorderLayout.CENTER);
    }
}
