package org.micromanager.rapp.MultiFOV;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class wellPanel extends JPanel  {
    /**
     * This method is called from within the constructor to initialize the Table position.
     */
    static  MultiFOV_GUI parent_;

    public  MultiFOV_Controller fov_controller = new MultiFOV_Controller();

   // private static final MultiFOV_GUI fINSTANCE =  new MultiFOV_GUI(parent_);
    Color bgColor = Color.LIGHT_GRAY;  // Panel's background color
    Point selectionStart_ = new Point();
    Point selectionEnd_ = new Point();
    Rectangle selection_ = new Rectangle();
    boolean isSelecting_ = false;
    ArrayList<ArrayList<Boolean>> wellsSelected_;
    Color transRed = new Color(128, 0, 0, 64);

    static WellClass pp_;
    //int col = 24; // pp_.getColCount();
   // int row = 16; // pp_.getRowCount();

    private int wellplate = MultiFOV_Controller.getWellPlateID();
    private int col = MultiFOV_Controller.getcolSize();
    private int row = MultiFOV_Controller.getrowSize();

     //int square = 20;
    private int square = MultiFOV_Controller.getSquareSize();
     //int space = 3;
    private int space = MultiFOV_Controller.getWellSpace();

    int ccolS = 0;
    int rrowS = 0;
    int ccolE = 0;
    int rrowE = 0;

    int outerAccess = 0;

   public wellPanel(MultiFOV_GUI parent){

    //   initComponents();
       //pp_ = WellClass.getInstance();
       selectionStart_ = new Point();
       selectionEnd_ = new Point();
       selection_ = new Rectangle();

//      wellsSelected_ = new ArrayList<ArrayList<Boolean>>();

       parent_ = parent;
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



    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // do your superclass's painting routine first, and then paint on top of it.
        //col = pp_.getColCount();
        //row = pp_.getRowCount();
        drawWellMap(g);
        System.out.println("In paintComponent");
        System.out.println("column start: " + ccolS);
    }

    private void drawWellMap(Graphics g) {

        // checks if is 96 or 382 well plate to adjust sqaure size
        //(other plates will work as well, everything with more than 24 col will have small squares)
//        if (col<=MultiFOV_Controller.getcolSize()){
//            generateWellMap(g, square, space, col, row, wellplate);
//        }
//        else{
//          //  square = 10;
            generateWellMap(g, square, space, col, row, wellplate);
     //   }

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
        posPanel.tableModel_.addWholeData(fovs);
    }

    private void generateWellMap(Graphics g, int square, int space, int col, int row, int wellPlate) {
        int xx = 0;
        int yy = 0;
        // paint squares
        for(int i=1; i<=col; i++){
            for(int ii=1; ii<=row; ii++){
                if(ccolS<=i && i<=ccolE && rrowS<=ii && ii<=rrowE){
                    g.setColor(Color.decode("#FF3333"));
                } else {
                    g.setColor(Color.decode("#34495e"));
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
            if(wellPlate == 96){
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




}
