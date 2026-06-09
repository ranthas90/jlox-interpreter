package gui;

import javax.swing.*;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class DisassemblerTable extends JTable {

    public DisassemblerTable(ChunkTableModel tableModel) {
        super(tableModel);
        configureColumns();
        configureTable();
    }

    private void configureTable() {
        setRowHeight(24);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setFillsViewportHeight(true);
    }

    private void configureColumns() {

        TableColumnModel columnModel = getColumnModel();

        TableColumn index = columnModel.getColumn(0);
        index.setPreferredWidth(60);
        index.setMaxWidth(80);

        TableColumn line = columnModel.getColumn(1);
        line.setPreferredWidth(60);
        line.setMaxWidth(80);

        TableColumn opcode = columnModel.getColumn(2);
        opcode.setPreferredWidth(150);
    }
}
