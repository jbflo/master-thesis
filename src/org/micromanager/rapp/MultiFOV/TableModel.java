/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.micromanager.rapp.MultiFOV;

import java.util.ArrayList;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Frederik
 */
public class TableModel extends AbstractTableModel{
     private ArrayList<Integer> data_ = new ArrayList<Integer>();
     private int max_ = 2048;
     private int min_ = 0;
    
    
    public TableModel(ArrayList<Integer> input, int min, int max){
        this.min_ = min;
        this.max_ = max;
        for (int i = 0; i < input.size(); i++){
            input.set(i, validateData(input.get(i)));
        }
        this.data_ = input;
        fireTableDataChanged();
//        sap_ = SeqAcqProps.getInstance().setDelaysArray(0, delays);
    }
     
      public void addWholeData(ArrayList<Integer> data){
        data_.clear();
        for (int i = 0; i < data.size(); i++){
            data.set(i, validateData(data.get(i)));
            
        }
        data_.addAll(data);
        
        fireTableDataChanged();
//        sap_.setDelaysArray(0, data_);
//        this.addEmptyRow();
    }  

    @Override
    public int getRowCount() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getColumnCount() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object getValueAt(int row, int col) {
        return ((Integer) data_.get(row));
    }

    @Override
    public String getColumnName(int i) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Class<?> getColumnClass(int i) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isCellEditable(int i, int i1) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


    @Override
    public void setValueAt(Object o, int i, int i1) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addTableModelListener(TableModelListener tl) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeTableModelListener(TableModelListener tl) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
    public int validateData(int val){
//        int out;
        
        if (val > max_)
                val = max_;
            else if (val < min_)
                val = min_;
            val = Math.round(val);
        
        return val;
    }
}



