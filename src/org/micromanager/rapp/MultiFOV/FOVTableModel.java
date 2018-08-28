/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.micromanager.rapp.MultiFOV;


import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author dk1109
 */
public class FOVTableModel extends AbstractTableModel {
    public static final int WELL_INDEX = 0;
    public static final int X_INDEX = 1;
    public static final int Y_INDEX = 2;
    public static final int Z_INDEX = 3;

    final static String um = "(" + "\u00B5" + "m)";

    private ArrayList<FOV> data_ = new ArrayList<FOV>();
    private String[] colNames_ = {"Well", "X" + um, "Y" + um, "Z" + um};

    private WellClass pp_;

//    private SeqAcqProps sap_;
    //TODO: remove duplicates!
    public FOVTableModel(WellClass pp) {
        pp_ = pp;
    }

    public FOVTableModel(String[] columnNames, WellClass pp) {
        this.colNames_ = columnNames;
        pp_ = pp;
    }

    @Override
    public String getColumnName(int col) {
        return colNames_[col];
    }

    @Override
    public Object getValueAt(int row, int col) {
        FOV fov = data_.get(row);
        switch (col) {
            case WELL_INDEX:
                return fov.getWell();
            case X_INDEX:
                return fov.getX();
            case Y_INDEX:
                return fov.getY();
            case Z_INDEX:
                return fov.getZ();
            default:
                return fov;
        }
    }
    
    public FOV getFOV(int row){
        FOV fov = data_.get(row);
        return fov;
    }

    @Override
    public Class getColumnClass(int column) {
        switch (column) {
            case WELL_INDEX:
                return String.class;
            case X_INDEX:
            case Y_INDEX:
            case Z_INDEX:
                return Double.class;
            default:
                return FOV.class;
        }
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    @Override
    public int getRowCount() {
        return data_.size();
    }

    public ArrayList<FOV> getData() {
        return data_;
    }

    public void addRow(FOV fov) {
        int row = data_.size();
        if (!data_.contains(fov)) {
            data_.add(row,fov);
            fireTableRowsInserted(row, row);
        } else {
            System.out.println("Error!");
        }
    }

    public void insertRow(int index, FOV fov){
        if (!data_.contains(fov)) {
            data_.add(index, fov);
            fireTableRowsInserted(data_.size() - 1, data_.size() - 1);
        }
//        sap_.setDelaysArray(0, data_);
    }

    public void removeRow(int row) {
        data_.remove(row);
        fireTableRowsDeleted(row, row);
//        sap_.setDelaysArray(0, data_);
    }

    public void addWholeData(ArrayList<FOV> data) {
        data_.clear();

        data_.addAll(data);

        fireTableDataChanged();
//        sap_.setDelaysArray(0, data_);
//        this.addEmptyRow();
    }

    public void clearAllData() {
        data_.clear();
        fireTableDataChanged();
//        sap_.setDelaysArray(0, data_);
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        switch (col) {
            case WELL_INDEX:
                return false;
            case X_INDEX:
            case Y_INDEX:
            case Z_INDEX:
                return true;
            default:
                return true;
        }
    }

    @Override
    public void setValueAt(Object value, int row, int column) {
        FOV fov = (FOV) data_.get(row);
        switch (column) {
            case WELL_INDEX:
                fov.setWell((String) value);
                break;
            case X_INDEX:
                fov.setX((Double) value);
                break;
            case Y_INDEX:
                fov.setY((Double) value);
                break;
            case Z_INDEX:
                fov.setZ((Double) value);
                break;
            default:
                System.out.println("invalid index");
        }
        fireTableCellUpdated(row, column);
    }

    public void setValueAtRow(FOV fov, int row) {
        data_.set(row, fov);
    }

    public void setPlateProps(WellClass pp) {
        pp_ = pp;
    }

//    public void saveFOVTableModelAsSpreadsheet(){
//       
//    // Save FilterTable in first sheet of wb .xls
//    // Initialize first sheet
//        HSSFSheet sheet1 = BLframe.wb.createSheet("XYSequencing");
//
//        
//    // Initialize first row with headers    
//         int RowSize=data_.size();
//            HSSFRow row0 = sheet1.createRow(0);
//            HSSFCell cell00 = row0.createCell(0);
//            HSSFCell cell01 = row0.createCell(1);
//            HSSFCell cell02 = row0.createCell(2);
//            HSSFCell cell03 = row0.createCell(3);
//            HSSFCell cell04 = row0.createCell(4);
//            cell00.setCellValue("Well");
//            cell01.setCellValue("X " + um);
//            cell02.setCellValue("Y" + um);
//            cell03.setCellValue("Z" + um);
//            
//    // write row for row from table to sheet        
//       for(int RowNum=0; RowNum<RowSize;RowNum++){
//            HSSFRow row = sheet1.createRow(RowNum+1);
//            HSSFCell cell0 = row.createCell(0);
//            HSSFCell cell1 = row.createCell(1);
//            HSSFCell cell2 = row.createCell(2);
//            HSSFCell cell3 = row.createCell(3);
//            HSSFCell cell4 = row.createCell(4);
//            cell0.setCellValue(data_.get(RowNum).getWell());
//            cell1.setCellValue(data_.get(RowNum).getX());
//            cell2.setCellValue(data_.get(RowNum).getY());
//            cell3.setCellValue(data_.get(RowNum).getZ());
//        }
//       
//      
//
//        //To change body of generated methods, choose Tools | Templates.
//    }
//    
//    public void loadFOVTableModelfromSpreadsheet(){
//        ArrayList<FOV> load=new ArrayList();
//        HSSFSheet worksheet = HCAFLIMPluginFrame.wbLoad.getSheet("XYSequencing");
//        int RowSize=worksheet.getPhysicalNumberOfRows();
//        for(int RowNum=0; RowNum<RowSize-1;RowNum++){
//            HSSFRow row = worksheet.getRow(RowNum+1);
//            HSSFCell cell0 = row.getCell(0);
//            HSSFCell cell1 = row.getCell(1);
//            HSSFCell cell2 = row.getCell(2);
//            HSSFCell cell3 = row.getCell(3);
//            HSSFCell cell4 = row.getCell(4);
//            
//            FOV fov = new FOV(cell1.getNumericCellValue(),cell2.getNumericCellValue(),cell3.getNumericCellValue(),cell0.getStringCellValue(), pp_);
//            fov.setGroup(cell4.getStringCellValue());
//            load.add(fov);
//        }
//        data_=load;
//        fireTableDataChanged();
//        
//    }

}
