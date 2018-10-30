package org.micromanager.rapp.MultiFOV;

import mmcorej.CMMCore;
import org.micromanager.api.ScriptInterface;
import org.micromanager.rapp.RappGui;
import org.micromanager.rapp.RappPlugin;
import org.micromanager.utils.ReportingUtils;
import org.w3c.dom.*;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;

public class FOV_Controller {

    private CMMCore core_ = new CMMCore();
    private ScriptInterface app_ =  RappPlugin.getScripI();
    private static final FOV_Controller fINSTANCE =  new FOV_Controller();
    private static String wellPlateID;
    private static int numColumns_;
    private static int numRows_;
    private static int squareSize;
    private static int space;

    // all dimensions in µm
    private static String plateName ;

    private static double sizeXUm_ ;
    private static double sizeYUm_ ;


    private static double wellSizeX_ ;
    private static double wellSizeY_ ;

    private static double firstWellX_ ;
    private static double firstWellY_ ;

    private static double wellSpacingX_ ;
    private static double wellSpacingY_;

    private static double sizeXFOV_ ;
    private static double sizeYFOV_ ;

    private static double wellXOff;
    private static double wellYOff;

    public static Point2D.Double cornet_off1 = new Point2D.Double();
    public static Point2D.Double cornet_off2 = new Point2D.Double();
    public static Point2D.Double cornet_off3 = new Point2D.Double();
    public static Point2D.Double cornet_off4 = new Point2D.Double();

    
    public static  ArrayList<String> wellTypes = new ArrayList<>();

    // public static final String IBIDI_24_WELL = "Ibidi-24WELL";

    public static final String DEFAULT_XYSTAGE_NAME = "XYStage";
    public static final String CUSTOM = "CUSTOM";
    private static final String METADATA_SITE_PREFIX = "Site";

    String wellShape = "rectangle";

    private static ArrayList<Double> xTab = new ArrayList<>();
    private static ArrayList<Double> yTab = new ArrayList<>();



    public FOV_Controller( CMMCore core, ScriptInterface app) {
        this.app_ = app;
        this.core_ = core;

    }

    public FOV_Controller() {

    }

    public static FOV_Controller getInstance() {
        return fINSTANCE;
    }


    public static boolean readXmlFileOnce (String filePath) {
        try {

            File fXmlFile = new File(filePath);

            if(fXmlFile.exists()){
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(fXmlFile);
                doc.getDocumentElement().normalize();
                System.out.println("Root element : " + doc.getDocumentElement().getNodeName());
                // Node node_ = doc.getElementsByTagName("WellParam").item(node_index);
                Element plate = doc.getDocumentElement();
                NodeList listaPlate = plate.getElementsByTagName("plate");
                int tam = listaPlate.getLength();
                wellTypes.clear();
                System.out.println("size : "+ tam);

                for (int i = 0; i < tam; i++) {
                    Element elem = (Element) listaPlate.item(i);
                    System.out.println("elem : " + elem.getTextContent());
                    wellTypes.add(elem.getTextContent());
                }

                return true;
            } else return false;



        } catch (Exception e) {
            e.printStackTrace();
            return false;

        }

    }

    public static boolean readXmlFile (String filePath, String plateName) {
        try {

            File fXmlFile = new File(filePath);

            if(fXmlFile.exists()){
                
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(fXmlFile);

                doc.getDocumentElement().normalize();

                System.out.println("Root element : " + doc.getDocumentElement().getNodeName());

                NodeList node_liste = doc.getElementsByTagName("WellParam");

                if (node_liste.getLength() == 0){
                    return false;
                }


                /////////////////////////////////////////////////////////////////////

                for (int temp = 0; temp < node_liste.getLength(); temp++) {

                    Node nNode = node_liste.item(temp);
                   // System.out.println("----------------------------" + nNode.getTextContent());
                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element eElement = (Element) nNode;
                        if (eElement.getAttribute("id").equals(plateName)){
                            System.out.println("plate:" + plateName);
                            System.out.println("Equal to "+ eElement.getAttribute("id"));
                            wellPlateID = eElement.getAttribute("id");
                            numColumns_ = Integer.parseInt(eElement.getElementsByTagName("numColumns_").item(0).getTextContent());
                            numRows_   = Integer.parseInt(eElement.getElementsByTagName("numRows_").item(0).getTextContent());
                            squareSize = Integer.parseInt(eElement.getElementsByTagName("squareSize").item(0).getTextContent());
                            space      = Integer.parseInt(eElement.getElementsByTagName("space").item(0).getTextContent());
                            sizeXUm_   = Double.parseDouble(eElement.getElementsByTagName("sizeXUm_").item(0).getTextContent());
                            sizeYUm_   = Double.parseDouble(eElement.getElementsByTagName("sizeYUm_").item(0).getTextContent());
                            wellSizeX_ = Double.parseDouble(eElement.getElementsByTagName("wellSizeX_").item(0).getTextContent());;
                            wellSizeY_ = Double.parseDouble(eElement.getElementsByTagName("wellSizeY_").item(0).getTextContent());;
                            firstWellX_ = Double.parseDouble(eElement.getElementsByTagName("firstWellX_").item(0).getTextContent());;
                            firstWellY_ = Double.parseDouble(eElement.getElementsByTagName("firstWellY_").item(0).getTextContent());;
                            wellSpacingX_ = Double.parseDouble(eElement.getElementsByTagName("wellSpacingX_").item(0).getTextContent());;
                            wellSpacingY_= Double.parseDouble(eElement.getElementsByTagName("wellSpacingY_").item(0).getTextContent());;
                            sizeXFOV_ = Double.parseDouble(eElement.getElementsByTagName("sizeXFOV_").item(0).getTextContent());;
                            sizeYFOV_ = Double.parseDouble(eElement.getElementsByTagName("sizeYFOV_").item(0).getTextContent());;
                          //  return true;
                        } //else return false;

                    }//else return false;
                }
                return true;
            } else return false;



        } catch (Exception e) {
            e.printStackTrace();
            return false;

        }

    }


    public Point2D.Double calibrateXY() {

        ArrayList[] posXY = this.positionlists();
        Point2D.Double pointOff_ = new Point2D.Double();



        if (posXY[0].size() == 0) {
            ReportingUtils.showMessage("Could not Calibrate Stage Due to 0 position list," +
                    " Please Load the plate then select the well A1 before starting Stage Calibration"
            );
        } else {

            JDialog dialog = new JDialog(RappGui.getInstance(), "XYStage Calibration Process Step 1", false);
            JDialog dialog2 = new JDialog(RappGui.getInstance(), "XYStage Calibration Process Step 2", false);

            dialog.setVisible(true);
            JOptionPane  jop1 = new JOptionPane( "Please Select the case A1 from the grid " +
                    "then Manually position the XY stage over the corner (top left) of the well A1 and press OK",
                  JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);

            JOptionPane  jop2 = new JOptionPane( "Please Select the last case From the first Cologne of the grid (Should be AF1 for 1536_well) " +
                    "Then Manually position the XY stage over the corner (bottom left) the last well of the first cologne  and press OK and press OK",
                    JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);

            dialog.setContentPane(jop1);
            jop1.addPropertyChangeListener(evt->{
                try {
                    if (jop1.getValue().equals(JOptionPane.OK_OPTION)) {
                        double x_pos_ini = (double) posXY[0].get(0); //store each element as a double in the array
                        double y_pos_ini = (double) posXY[1].get(0); //store each element as a double in the array
                        Point2D.Double cornet_pos;
                        cornet_pos = core_.getXYStagePosition();
                        wellXOff = (x_pos_ini + cornet_pos.getX());
                        wellYOff = (-y_pos_ini + cornet_pos.getY());

                        System.out.println("Xoff = " + wellXOff + "__ Yoff= " + wellYOff);
                        cornet_off1.setLocation(wellXOff, wellYOff);

                        pointOff_.x = wellXOff;
                        pointOff_.y = wellYOff;

                        dialog2.setVisible(true);
                        dialog.setVisible(false);
                    }
                    //else dialog.dispose();


                        //  Thread.sleep(1000 );
                        //  app_.setXYOrigin(this.getFirstWellOffX(), this.getFirstWellOffY());
                        //  regenerate();
                        //           Point2D.Double pt = app_.getXYStagePosition();
                        //       JOptionPane.showMessageDialog(RappGui.getInstance(), "XY Stage set at position: " + pt.x + "," + pt.y);
                        //   JOptionPane.showMessageDialog(RappGui.getInstance(), "XY Stage set at position: " + wellXOff + "," + wellYOff);
                        //  return pointOff_;

                    } catch (Exception e) {
                        //displayError(e.getMessage());
                        dialog.dispose();
                        e.printStackTrace();
                        //  return null;
                    }

            });

            dialog2.setContentPane(jop2);
            jop2.addPropertyChangeListener(evt->{
                try {

                    if (jop2.getValue().equals(JOptionPane.OK_OPTION)) {
                        double x_pos_ini2 = (double) posXY[0].get(0); //store each element as a double in the array
                        double y_pos_ini2 = (double) posXY[1].get(0); //store each element as a double in the array
                        Point2D.Double cornet_pos2;
                        cornet_pos2 = core_.getXYStagePosition();
                        double  wellXOff1 = (x_pos_ini2 + cornet_pos2.getX());
                        double wellYOff1 = (-y_pos_ini2 + cornet_pos2.getY());

                        cornet_off2.setLocation(wellXOff1, wellYOff1);
                        dialog2.setVisible(false);

                        JOptionPane.showMessageDialog(RappGui.getInstance(), "XY Stage set at position: " + wellXOff + "," + wellYOff);
                    }

                } catch (Exception e) {
                    //displayError(e.getMessage());
                    e.printStackTrace();
                    //  return null;
                }finally {
              //      dialog.dispose();
                //    dialog2.dispose();
                }

            });

            dialog.pack();
            dialog.setLocationRelativeTo(RappGui.getInstance());
            dialog2.pack();
            dialog2.setLocationRelativeTo(RappGui.getInstance());


        }
        return pointOff_;
    }

    public Point2D.Double getXYOffset(){

        Point2D.Double pointOff_ = new Point2D.Double();

        pointOff_.x = wellXOff;
        pointOff_.y = wellYOff;

        return pointOff_;

    }


    public static double getAngle() {
        // NOTE: Remember that most math has the Y axis as positive above the X.
        // However, for screens we have Y as positive below. For this reason,
        // the Y values are inverted to get the expected results.
        final double deltaY = (cornet_off1.y - cornet_off2.y);
        final double deltaX = (cornet_off2.x - cornet_off1.x);
        final double result = Math.toRadians(Math.atan2(deltaY, deltaX));
        System.out.println("The Angle is :" + result);
        return (result < 0) ? (360d + result) : result;
      //  return result;
    }

    public Point2D.Double computeXYStagepos(){
        try {
            Math.cos(Math.toRadians(354));
            return core_.getXYStagePosition();
        } catch (Exception x){
            x.printStackTrace();
            return null;
        }
    }

    public void moveStageToA1 (){
        try{

//            double xoff = 41150;
//            double yoff = -43735;
//            double xpos = 12.18 * 1000;
//            double ypos = 8.74 * 1000;
//            core_.setXYPosition(xpos+xoff ,ypos+yoff);
//            JOptionPane.showMessageDialog(RappGui.getInstance(), "XY Stage set to Well A1: " +
//                    "If Not: Manually position the XY stage over the corner top left of the well A1 and press Calibrate XY...\"" );

            System.out.println(core_.getAutoFocusOffset());

            core_.setAutoFocusOffset(9.9);

            System.out.println(core_.getAutoFocusOffset());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public  String getWellPlateID (){
        return wellPlateID;
    }

    public  int getcolSize() {
        return numColumns_;
    }

    public  int getrowSize() {
        return numRows_;
    }

    public  int getSquareSize() {
        return squareSize;
    }

    public  int getWellSpace() {
        return space;
    }

    public  String getPlateName(){return plateName;}

    public double getWellOffX(){return wellXOff;}
    public double getWellOffY(){return wellYOff;}

    public double getPlateLength(){return sizeXUm_;}
    public double getPlateHeight(){return sizeYUm_;}

    public double getWellSizeX(){return wellSizeX_;}
    public double getWellSizeY(){return wellSizeY_;}

    public double getWellSpacingX(){return wellSpacingX_;}
    public double getWellSpacingY(){return wellSpacingY_;}

    public double getFirstWellOffX(){return firstWellX_;}
    public double getFirstWellOffY(){return firstWellY_;}

    public double getFOVsizeX(){return sizeXFOV_;}
    public double getFOVsizeY(){return sizeYFOV_;}

    public int getColCount(){return numColumns_;}
    public int getRowCount(){return numRows_;}
    public String getWellShape(){return wellShape;}

    public static boolean valideXml(boolean isXmlFIleValide){

        System.out.println("FIle" + isXmlFIleValide);

        return isXmlFIleValide;
    }

    public  void  getWholeData( ArrayList<FOV> fovs) {

       ArrayList<Double> xTab_ = new ArrayList<>();
       ArrayList<Double> yTab_ = new ArrayList<>();
//        Point2D.Double cornet_pos ;
//        double defXoff = 0.0 ;
//        double defyoff = 0.0 ;
//
//        try {
//           cornet_pos = core_.getXYStagePosition();
//           defXoff = xTab.get(0) - cornet_pos.getX();
//           defyoff = yTab.get(0) - cornet_pos.getY() ;
//       //    core_.setXYPosition((4)+defXoff ,(4)+defyoff);
//            System.out.println("pos: " +core_.getXYStagePosition());
//
//            System.out.println( " OffsetTTT :  "+ defXoff);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }


        for (int i = 0; i< fovs.size() ; i++){

            double xx = fovs.get(i).getX();
            double yy = fovs.get(i).getY();

            xTab_.add(xx);
            yTab_.add(yy);

        }


        System.out.println( " DataSize :"+ fovs.size());

        xTab = xTab_;
        yTab = yTab_;

        System.out.println( " DataSize :"+ fovs.size());

        System.out.println( " Xcord : "+ xTab );
        System.out.println(" Ycord  : "+ yTab );


    }



    public ArrayList[] positionlists() {

        System.out.println( " Xcord : "+ xTab );
        System.out.println(" Ycord  : "+ yTab );

        return new ArrayList[]{xTab   , yTab};

    }




}
