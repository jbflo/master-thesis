package org.micromanager.rapp.CellSegmentation;



import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

public class CellPointTableModel extends AbstractTableModel implements TableModelListener {

    private static final long serialVersionUID = 3290621191844925827L;
    public final String[] COLUMN_NAMES = new String[]{
            "Use ALl?",
            "Point_X",
            "Point_Y",
            "Use GFP Signal?",
            "Use RFP Signal?",
            "Use BFP Signal?"
    };
    private final String[] TOOLTIPS = new String[]{
            "Toggle channel/group on/off",
            "X Coordinate",
            "Y Coordinate",
            "Fluorescence Emission color"};

    public String getToolTipText(int columnIndex) {
        return TOOLTIPS[columnIndex];
    }

    @Override
    public void tableChanged(TableModelEvent e) {

    }

    @Override
    public int getRowCount() {
        return 0;
    }

    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return COLUMN_NAMES[columnIndex];
    }


    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return null;
    }
}
