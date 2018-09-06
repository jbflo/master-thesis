package org.micromanager.rapp.MultiFOV;

import mmcorej.CMMCore;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class posPanel extends JPanel {

    private CMMCore core_;
   // private static final XYZPanel fINSTANCE =  new XYZPanel();
    public static FOVTableModel tableModel_;
    JTable fovTable_;
    private FOV_Controller FOV_control;
    wellPanel wellPanel_;
    int genMode = 0;
    JPanel contetentPanel = new JPanel();
    private javax.swing.JButton SelectFOVsBottom;
//    private javax.swing.JTextField StartFOVField;
//    private javax.swing.JTextField StopFOVField;
    private javax.swing.JButton deleteAllFOVs_btn;
    private javax.swing.JPanel fovTablePanel;
    public javax.swing.JComboBox fullWellCombo;
//    private javax.swing.JLabel jLabel3;
//    private javax.swing.JLabel jLabel4;
    private  JButton deleteSlectFOV_btn;


    /**
     * Creates new form XYZPanel
     */
    public posPanel(FOV_GUI parent_ , CMMCore core) {

        this.setLayout(null);

        FOV_control = FOV_Controller.getInstance();
       // wMap_ = wellMap.getInstance();
        wellPanel_ = wellPanel.getInstance();

        SelectFOVsBottom = new javax.swing.JButton();
//        StartFOVField = new javax.swing.JTextField();
//        StopFOVField = new javax.swing.JTextField();
//        jLabel3 = new javax.swing.JLabel();
//        jLabel4 = new javax.swing.JLabel();
        fovTablePanel = new javax.swing.JPanel();
        fullWellCombo = new javax.swing.JComboBox();
        deleteAllFOVs_btn = new javax.swing.JButton();
        deleteSlectFOV_btn = new JButton();

        setControlDefaults();

       // this.add(contetentPanel);

        fullWellCombo.setBounds(55, 430, 80, 30 );
        this.add(fullWellCombo);

        SelectFOVsBottom.setBounds(180, 430, 100, 30 );
        this.add(SelectFOVsBottom);


        deleteAllFOVs_btn.setBounds(170, 470, 120, 30 );
        this.add(deleteAllFOVs_btn);

        deleteSlectFOV_btn.setBounds(30, 470, 120,30);
        this.add(deleteSlectFOV_btn);


//        jLabel3.setBounds(10, 420, 40, 30 );
//        this.add(jLabel3);


//        StartFOVField.setBounds(40, 420, 30, 30 );
//        this.add(StartFOVField);


//        jLabel4.setBounds(10, 460, 40, 30 );
//        this.add(jLabel4);


//        StopFOVField.setBounds(40, 460, 30, 30 );
//        this.add(StopFOVField);


        fovTablePanel.setBounds(10, 20, 300, 400 );
        this.add(fovTablePanel);



        SelectFOVsBottom.setText("Add All FOVs");
        SelectFOVsBottom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SelectFOVsBottomActionPerformed(evt);
            }
        });

//        StartFOVField.setText("A1");
//        StartFOVField.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                StartFOVFieldActionPerformed(evt);
//            }
//        });

//        StopFOVField.setText("C3");
//        StopFOVField.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                StopFOVFieldActionPerformed(evt);
//            }
//        });

//        jLabel3.setText("From");
//
//        jLabel4.setText("to");

        fovTablePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Stored xyz positions"));

        fullWellCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Full well", "Single FOV" }));
        fullWellCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fullWellComboActionPerformed(evt);
            }
        });

        deleteAllFOVs_btn.setText("Delete all FOVs");
        deleteAllFOVs_btn.addActionListener((ActionListener) evt->deleteFOVsButtonActionPerformed(evt));

        deleteSlectFOV_btn.setText("Delete Seleted FOVs");
        deleteSlectFOV_btn.addActionListener(evt->{

            //deleteFOVsButtonActionPerformed(evt);

        });


    }
   // public static XYZPanel getInstance() {
      //  return fINSTANCE;
    //}

//    private void StopFOVFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_StopFOVFieldActionPerformed
//        String input = StopFOVField.getText();
//        input = input.toUpperCase();
//        StopFOVField.setText(input);
//    } //GEN-LAST:event_StopFOVFieldActionPerformed

    private void SelectFOVsBottomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SelectFOVsBottomActionPerformed

        //String startF = StartFOVField.getText().toUpperCase();
       // StartFOVField.setText(startF);

        int wellplate = FOV_Controller.getWellPlateID();

        String startF = "A1";
        int startCol = xyzFunctions.getWellCol(startF);
        int startRow = xyzFunctions.getWellRow(startF);

       // String stopF = StopFOVField.getText().toUpperCase();

        String stopF = "A24";
        if (wellplate == 96){
            stopF = "H12";
        }else if (wellplate == 384){
            stopF = "P24";
        }
        // StopFOVField.setText(stopF);
        int stopCol = xyzFunctions.getWellCol(stopF);
        int stopRow = xyzFunctions.getWellRow(stopF);

        int dCol = stopCol - startCol+1;
        int dRow = stopRow - startRow+1;
        FOV_control.getFirstWellOffX();
        genMode = fullWellCombo.getSelectedIndex();
        ArrayList<FOV> preFovs = new ArrayList<FOV>(tableModel_.getData());
        tableModel_.clearAllData();
        ArrayList<FOV> fovs = xyzFunctions.generateFOVs(dCol, dRow, startCol, startRow, genMode);
        fovs = xyzFunctions.concatLists(preFovs, fovs);
        fovs = xyzFunctions.sortList(fovs);
        tableModel_.addWholeData(fovs);
        //WellPanel.drawFromOutsideClass(startCol, stopCol, startRow, stopRow);
        FOV_control.getWholeData(fovs);

    }

//    private void StartFOVFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_StartFOVFieldActionPerformed
//        String input = StartFOVField.getText();
//        input = input.toUpperCase();
//        StartFOVField.setText(input);
//    }//GEN-LAST:event_StartFOVFieldActionPerformed

    private void fullWellComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fullWellComboActionPerformed
        genMode = fullWellCombo.getSelectedIndex();
        xyzFunctions.genMode = genMode;
    }//GEN-LAST:event_fullWellComboActionPerformed

    private void deleteFOVsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteFOVsButtonActionPerformed
        tableModel_.clearAllData();
        wellPanel_.repaint();
       // wMap_.repaint();

    }//GEN-LAST:event_deleteFOVsButtonActionPerformed


    private void setControlDefaults() {

        tableModel_ = new FOVTableModel(FOV_control);
//        searchFOVtableModel_ = new FOVTableModel(pp_); //Not sure if this is the best way, but try it for now.
        tableModel_.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {

            }
        });

        fovTable_ = new JTable();
        fovTable_.setModel(tableModel_);
        fovTable_.setSurrendersFocusOnKeystroke(true);
        fovTable_.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
//        fovTable_.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
//        fovTable_.getColumnModel().getColumn(0).setPreferredWidth(40);
//        fovTable_.getColumnModel().getColumn(1).setPreferredWidth(40);
//        fovTable_.getColumnModel().getColumn(2).setPreferredWidth(40);
//        fovTable_.getColumnModel().getColumn(3).setPreferredWidth(40);

        JScrollPane scroller = new javax.swing.JScrollPane(fovTable_);
        fovTable_.setPreferredScrollableViewportSize(new java.awt.Dimension(160, 160));
        fovTablePanel.setLayout(new BorderLayout());
        fovTablePanel.add(scroller, BorderLayout.CENTER);

        final JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem("Delete FOV");
        deleteItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int r = fovTable_.getSelectedRow();
                tableModel_.removeRow(r);
            }
        });
        JMenuItem addItem = new JMenuItem("Add FOV");
        addItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int r = fovTable_.getSelectedRow();
                tableModel_.insertRow(r, new FOV(0,0,0,"A1"));
            }
        });
//        JMenuItem goToFOVItem = new JMenuItem("Go to FOV");
//        goToFOVItem.addActionListener(new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                int r = fovTable_.getSelectedRow();
////                FOV fov = tableModel_.getData().get(r);
//                xyzmi_.gotoFOV(tableModel_.getData().get(r));
//                if (!zAsOffset_){
//                    double zval = tableModel_.getData().get(r).getZ();
//                    xyzmi_.moveZAbsolute(zval);
//                }
//                else {
//                    // obviously, this isn't quite right - we want to get
//                    // the offset of the CURRENT FOV (perhaps from parent in
//                    // later implementations?) and subtract from that of the
//                    // NEWLY SELECTED FOV.
//                    // TODO: fix for proper zAsOffset behaviour.
//                    // Wait for move completion
//                    while (xyzmi_.isStageBusy()){
//                       System.out.println("Stage moving...");
//                    };
//
//                    if(parent_.checkifAFenabled()){
//                        // If we have gone to the FOV, and have AF, do AF
//                        xyzmi_.customAutofocus(parent_.getAFOffset());
//                    } else {
//                        // If we don't have AF, go to the 'good offset position'
//                        xyzmi_.moveZAbsolute(parent_.getFixedAFDefault());
//                    }
//                    //Now do the relative shift
//                    xyzmi_.moveZRelative(tableModel_.getData().get(r).getZ());
//                    System.out.println("Z value"+tableModel_.getData().get(r).getZ());
//                }
//            }
//        });

        popupMenu.add(addItem);
        popupMenu.add(deleteItem);
        //popupMenu.add(goToFOVItem);

        fovTable_.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
//                System.out.println("pressed");
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    JTable source = (JTable) e.getSource();
                    int row = source.rowAtPoint(e.getPoint());
                    int column = source.columnAtPoint(e.getPoint());

                    if (!source.isRowSelected(row)) {
                        source.changeSelection(row, column, false, false);
                    }
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }
}
