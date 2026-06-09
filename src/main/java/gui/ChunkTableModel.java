package gui;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class ChunkTableModel extends AbstractTableModel {

    private final List<ChunkRecord> chunks;
    private final String[] columns = new String[] {"Index", "Line", "Opcode", "Details"};

    public ChunkTableModel(List<ChunkRecord> chunks) {
        this.chunks = chunks;
    }

    @Override
    public int getRowCount() {
        return chunks.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        ChunkRecord chunk = chunks.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> chunk.index();
            case 1 -> chunk.line();
            case 2 -> chunk.opcode();
            case 3 -> chunk.details();
            default -> null;
        };
    }
}
