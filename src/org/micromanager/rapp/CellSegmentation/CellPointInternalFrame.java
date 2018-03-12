package org.micromanager.rapp.CellSegmentation;

import org.micromanager.utils.ColorEditor;
import org.micromanager.utils.ColorRenderer;

import javax.swing.*;
import javax.swing.plaf.basic.BasicInternalFrameUI;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

import java.awt.*;
import java.awt.event.MouseEvent;

import static javax.swing.JFrame.setDefaultLookAndFeelDecorated;

public class CellPointInternalFrame extends JInternalFrame {
    private int columnWidth_[];
    private int columnOrder_[];
    private final JScrollPane pointTablePane_;
    private CellPointTableModel model_;
    private JTable pointTable_ = new JTable();

    public final void createPointTable() {

        model_ = new CellPointTableModel();
        model_.addTableModelListener(model_);
        pointTable_ = new JTable() {
            @Override
            @SuppressWarnings("serial")
            protected JTableHeader createDefaultTableHeader() {
                return new JTableHeader(columnModel) {

                    @Override
                    public String getToolTipText(MouseEvent e) {
                        String tip = null;
                        java.awt.Point p = e.getPoint();
                        int index = columnModel.getColumnIndexAtX(p.x);
                        int realIndex = columnModel.getColumn(index).getModelIndex();
                        return model_.getToolTipText(realIndex);
                    }
                };
            }
        };

        pointTable_.setFont(new Font("Dialog", Font.PLAIN, 10));
        pointTable_.setAutoCreateColumnsFromModel(false);
        pointTable_.setModel(model_);
        //model_.setChannels(acqEng_.getChannels());

        CellPointCellEditor cellEditor = new CellPointCellEditor();
        CellPointCellRenderer cellRenderer = new CellPointCellRenderer();
        pointTable_.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        int columnCount = 6;
        columnWidth_ = new int[columnCount];
        columnOrder_ = new int[columnCount];
        for (int k = 0; k < model_.getColumnCount(); k++) {
            int colIndex = search(columnOrder_, k);
            if (colIndex < 0) {
                colIndex = k;
            }
            if (colIndex == model_.getColumnCount() - 1) {
                ColorRenderer cr = new ColorRenderer(true);
                ColorEditor ce = new ColorEditor(model_, model_.getColumnCount() - 1);
                TableColumn column = new TableColumn(model_.getColumnCount() - 1, 200, cr, ce);
                column.setPreferredWidth(columnWidth_[model_.getColumnCount() - 1]);
                pointTable_.addColumn(column);

            } else {
                TableColumn column = new TableColumn(colIndex, 200, cellRenderer, cellEditor);
                column.setPreferredWidth(columnWidth_[colIndex]);
                pointTable_.addColumn(column);
            }
        }
        pointTablePane_.setViewportView(pointTable_);
    }



    public final void createChannelTable() {
        model_ = new CellPointTableModel();
        model_.addTableModelListener(model_);

    }

    public CellPointInternalFrame(){
        try {
            setDefaultLookAndFeelDecorated(true);
            UIManager.setLookAndFeel("com.jtattoo.plaf.smart.SmartLookAndFeel");

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        BasicInternalFrameUI bi = (BasicInternalFrameUI)this.getUI();
        bi.setNorthPane(null);
      //  createEmptyPanels();
        setLocation(0,0);
        pointTablePane_ =new JScrollPane();
        createPointTable();
        this.add(pointTablePane_);

    }

    public CellPointTableModel getModel_() {
        return model_;
    }

    public static int search(int[] numbers, int key) {
        for (int index = 0; index < numbers.length; index++) {
            if (numbers[index] == key) {
                return index;
            }
        }
        return -1;
    }

}


