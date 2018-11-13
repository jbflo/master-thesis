package org.micromanager.rapp.MultiFOV;

import mmcorej.CMMCore;
import org.micromanager.api.ScriptInterface;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class wellPanel extends JPanel  {
    /**
     * This method is called from within the constructor to initialize the Table position.
     */
    static FOV_GUI parent_;

    private static CMMCore core_ ;
    private ScriptInterface app_;
    private static final wellPanel fINSTANCE =  new wellPanel( parent_, core_);
    Color bgColor = Color.LIGHT_GRAY;  // Panel's background color
    private  Point selectionStart_ ;
    private  Point selectionEnd_ ;
    private  ArrayList<Point> selection_list;
    Rectangle selection_ = new Rectangle();

    boolean isSelecting_ = false;
    ArrayList<ArrayList<Boolean>> wellsSelected_;
    Color transRed = new Color(128, 0, 0, 64);

//    public enum Tool {SELECT, MOVE}
//    private Tool mode_;

    static DefaultTableModel model = new javax.swing.table.DefaultTableModel();

    public static AtomicBoolean stageAction_move = new AtomicBoolean(false);

    static FOV_Controller FOV_control ;

    public static wellPanel getInstance() {
        return fINSTANCE;
    }


    private String wellplate;
    private int col ;
    private int row ;

    //int square = 20;
    private int square ;
    //int space = 3;
    private int space ;

    //int col = 24; // pp_.getColCount();
    // int row = 16; // pp_.getRowCount();


    int ccolS = 0;
    int rrowS = 0;
    int ccolE = 0;
    int rrowE = 0;

    int outerAccess = 0;




   public wellPanel(FOV_GUI parent , CMMCore core){
       core_ = core;
       FOV_control = FOV_Controller.getInstance();
       wellplate = FOV_control.getWellPlateID();
       col = FOV_control.getcolSize();
       row = FOV_control.getrowSize();
       square = FOV_control.getSquareSize();
       space = FOV_control.getWellSpace();
       // initComponents();
       selectionStart_ = new Point();
       selectionEnd_ = new Point();
       selection_list = new ArrayList<Point>();
       selection_ = new Rectangle();

       parent_ = parent;
       addMouseListener(new MouseAdapter() {

           @Override
           public void mouseClicked(final MouseEvent e){
               if (e.isControlDown()) {
                   selection_list.add(e.getPoint());
                   isSelecting_ = true;
               }
           }

           @Override
           public void mousePressed(MouseEvent e) {
               selection_list.add(e.getPoint());
               if (e.isControlDown()){
                   selectionStart_ = selection_list.get(0);
               } else
               selectionStart_ = e.getPoint();
               isSelecting_ = true;
           }

           @Override
           public void mouseReleased(MouseEvent e) {
               isSelecting_ = false;
               if (e.isControlDown()){
                   selectionEnd_ = selection_list.get(selection_list.size() - 1);
               }else
                selectionEnd_ = e.getPoint();

               outerAccess = 0;
              // We got the new Value of the plate reloaded
               wellplate = FOV_control.getWellPlateID();
               col = FOV_control.getcolSize();
               row = FOV_control.getrowSize();

               square = FOV_control.getSquareSize();
               space = FOV_control.getWellSpace();

               repaint();
               checkStartEnd();
               getSelectedWells();

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

        // checks if is 96 or 384 well plate to adjust sqaure size
        //(other plates will work as well, everything with more than 24 col will have small squares)
            generateWellMap(g, square, space, col, row);

    }

    private void getSelectedWells() {
        // get start well
        ccolS = (selectionStart_.x -15-square)/(square+space)+1;
        rrowS = (selectionStart_.y-10-square)/(square+space)+1;

//                String rr = xyzFunctions.convertNumToAlph(rrowS);
//                String startWell = Integer.toString(ccolS);
//                startWell = rr.concat(startWell);

        // get end well
        ccolE = (selectionEnd_.x- 15-square)/(square+space)+1;
        rrowE = (selectionEnd_.y -10-square)/(square+space)+1;

        // generate FOVs and populate list
        int genMode = xyzFunctions.genMode;
        int dCol = ccolE - ccolS+1;
        int dRow = rrowE - rrowS+1;

        ArrayList<FOV> fovs = xyzFunctions.generateFOVs(dCol, dRow, ccolS, rrowS, genMode);

        posPanel.tableModel_.addWholeData(fovs);


       // posPanel.tableModel_.data.setModel(model); //f.data is the JTable

        FOV_control.getWholeData(fovs);

        System.out.println("mode_:" + stageAction_move.get());

        if (stageAction_move.get()) {

            Point2D.Double Calibrated_pt = FOV_control.getXYOffset();
            ArrayList[] posXY = FOV_control.positionlists();

            double x_pos =  Double.parseDouble(posXY[0].get(0).toString()); //store each element as a double in the array
            double y_pos = Double.parseDouble(posXY[1].get(0).toString()); //store each element as a double in the array

            // here we try to get the position, if the plate is rotate
           // double x1 = (Math.cos(FOV_Controller.getAngle()) * x_pos) - (Math.sin(FOV_Controller.getAngle()) * y_pos);
           // double y2 = (Math.sin(FOV_Controller.getAngle()) * x_pos) + ( Math.cos(FOV_Controller.getAngle()) * y_pos);
          //  double xxPos = -x1+ Calibrated_pt.getX();
          //  double yyPos = y2+ Calibrated_pt.getY();

            double xxPos = -x_pos+ Calibrated_pt.getX();
            double yyPos = y_pos+ Calibrated_pt.getY();

            try{
                System.out.println("xx = " + xxPos + "__ yy= " +yyPos);
                core_.setXYPosition(xxPos, yyPos);
            }
            catch (Exception e){
              e.printStackTrace();
            }
        }

        System.out.println("Values : " + dCol + " _ " + dRow + " _ " +ccolS + " _ " +rrowS+ " _ " + genMode);

    }

    private void generateWellMap(Graphics g, int square, int space, int col, int row) {
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
                g.drawRect(xx+15,yy+10,square,square);
                g.setFont(new Font("Arial Black", Font.PLAIN, 8));
                g.setColor(Color.GREEN);
                g.drawLine((int) ((xx+15)*square), (int) ((yy+10)*square),(int) ((xx+15)*square),(int) ((yy+10)*square));
            }
        }
        g.setColor(Color.DARK_GRAY);

        // paint column header
        for(int i=1; i<=col; i++){
            g.setFont(new Font("Arial Black", Font.PLAIN, 8));

            String cc = Integer.toString(i);
            g.drawString(cc, (i-1)*(square+space)+(square+9)*7/5,square+2);
        //  g.drawString(cc, square-20,square-20);
        }

        // paint row header
        for(int ii=1; ii<=row; ii++){
            g.setFont(new Font("Arial Black", Font.PLAIN, 10));
            String rr = xyzFunctions.convertNumToAlph(ii);
            //g.drawString(rr, 0,(ii-1)*(square+space)+square*7/4);
            g.drawString(rr, square -7,(ii-1)*(square+space)+(square+10)*7/5);
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
