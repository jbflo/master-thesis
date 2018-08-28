/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.micromanager.rapp.MultiFOV;


import javax.swing.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

/**
 *
 * @author Frederik
 */
public class wellMap extends JPanel {
    static BLframe parent_;
    private static final wellMap fINSTANCE =  new wellMap(parent_);
    
    Point selectionStart_ = new Point();
    Point selectionEnd_ = new Point();
    Rectangle selection_ = new Rectangle();
    boolean isSelecting_ = false;
    ArrayList<ArrayList<Boolean>> wellsSelected_;
    Color transRed = new Color(128, 0, 0, 64);
    static WellClass pp_;
    int col = 24; // pp_.getColCount();
    int row = 16; // pp_.getRowCount();

    int square = 20;
    int space = 3;
    
    int ccolS = 0;
    int rrowS = 0;
    int ccolE = 0;
    int rrowE = 0;
    
    int outerAccess = 0;

    public wellMap(BLframe parent){
        setPreferredSize(new Dimension(200, 200));
        initComponents();
        pp_ = WellClass.getInstance();
        selectionStart_ = new Point();
        selectionEnd_ = new Point();
        selection_ = new Rectangle();
        
//      wellsSelected_ = new ArrayList<ArrayList<Boolean>>();
        
        parent_ = parent;
//            this.setEnabled(false);     
        addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                selectionStart_ = e.getPoint();
                isSelecting_ = true;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                    isSelecting_ = false;
                    selectionEnd_ = e.getPoint();
                    outerAccess = 0;
                    checkStartEnd();
                    getSelectedWells();
                    repaint();
            }
            
        });

    }

    
    private void checkStartEnd() {
        int xEnd = selectionEnd_.x;
        int yEnd = selectionEnd_.y;
        int xSta = selectionStart_.x;
        int ySta = selectionStart_.y;
        if (xEnd < xSta) {
            selectionEnd_.x = xSta;
            selectionStart_.x = xEnd;
        }
        if (yEnd < ySta) {
            selectionEnd_.y = ySta;
            selectionStart_.y = yEnd;
        }
    }
    
    public static wellMap getInstance() {
            return fINSTANCE;
    }

    
//    public wellMap() {
//        initComponents();
//        pp_ = WellClass.getInstance();
//        
//    }
    @Override
    
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // do your superclass's painting routine first, and then paint on top of it.
        col = pp_.getColCount();
        row = pp_.getRowCount();
        drawWellMap(g);
        System.out.println("In paintComponent");
        System.out.println("column start: " + ccolS);
    }
    
    private void drawWellMap(Graphics g) {
       
        // checks if is 96 or 382 well plate to adjust sqaure size 
        //(other plates will work as well, everything with more than 24 col will have small squares)
        if (col<=24){
            generateWellMap(g, square, space, col, row, 0);
        }
        else{
            square = 10;
            generateWellMap(g, square, space, col, row, 1);
        }
        
    }
    
    private void getSelectedWells() {
                
                // get start well
                ccolS = (selectionStart_.x-square)/(square+space)+1;
                rrowS = (selectionStart_.y-square)/(square+space)+1;
//                String rr = xyzFunctions.convertNumToAlph(rrowS);
//                String startWell = Integer.toString(ccolS);
//                startWell = rr.concat(startWell);
                
                // get end well
                ccolE = (selectionEnd_.x-square)/(square+space)+1;
                rrowE = (selectionEnd_.y-square)/(square+space)+1;

                // generate FOVs and populate list 
                int genMode = xyzFunctions.genMode;
                int dCol = ccolE - ccolS+1;
                int dRow = rrowE - rrowS+1;
                ArrayList<FOV> fovs = xyzFunctions.generateFOVs(dCol, dRow, ccolS, rrowS, genMode);
                XYZPanel.tableModel_.addWholeData(fovs);
                
    }
    
    private void generateWellMap(Graphics g, int square, int space, int col, int row, int wellPlate) {
        int xx = 0;
        int yy = 0;
        // paint squares
        for(int i=1; i<=col; i++){
            for(int ii=1; ii<=row; ii++){
                if(ccolS<=i && i<=ccolE && rrowS<=ii && ii<=rrowE){
                    g.setColor(Color.GREEN);
                } else {
                    g.setColor(Color.RED);
                }
                xx = (i-1)*(square+space)+square;
                yy = (ii-1)*(square+space)+square;
                g.drawRect(xx,yy,square,square);
                g.drawLine((int) (xx+0.5*square), (int) (yy+0.5*square),(int) (xx+0.5*square),(int) (yy+0.5*square));
            }
        }
        g.setColor(Color.DARK_GRAY);
        
        // paint column header
        for(int i=1; i<=col; i++){
            if(wellPlate ==0){
                g.setFont(new Font("Arial Black", Font.PLAIN, 10));
            } else{
                g.setFont(new Font("Arial Black", Font.PLAIN, 8));
            }
                String cc = Integer.toString(i);
                g.drawString(cc, (i-1)*(square+space)+square*7/6,square*3/4);
        }
        
        // paint row header
        for(int ii=1; ii<=row; ii++){
                g.setFont(new Font("Arial Black", Font.PLAIN, 10));
                String rr = xyzFunctions.convertNumToAlph(ii);
                g.drawString(rr, 0,(ii-1)*(square+space)+square*7/4);
        }
    }
    
    public void drawFromOutsideClass(int startCol, int stopCol, int startRow, int stopRow){
        ccolS = startCol;
        rrowS = startRow;
        ccolE = stopCol;
        rrowE = stopRow;
        outerAccess = 1;        
        System.out.println("In drawFrom..");
        System.out.println("column start: " + ccolS);
        repaint();
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables


  
}
