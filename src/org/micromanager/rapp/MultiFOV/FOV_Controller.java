package org.micromanager.rapp.MultiFOV;

import mmcorej.CMMCore;
import org.micromanager.api.ScriptInterface;
import org.micromanager.rapp.RappGui;
import org.micromanager.rapp.RappPlugin;
import org.micromanager.utils.MMScriptException;
import org.micromanager.utils.ReportingUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;

public class FOV_Controller {

    private CMMCore core_ = new CMMCore();
    private ScriptInterface app_ =  RappPlugin.getScripI();
    private static final FOV_Controller fINSTANCE =  new FOV_Controller();
    private static int wellPlateID;
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

    double xWell = 2900;
    double yWell = 2900;

    private static double firstWellX_ ;
    private static double firstWellY_ ;

    private static double wellSpacingX_ ;
    private static double wellSpacingY_;

    private static double sizeXFOV_ ;
    private static double sizeYFOV_ ;

    private static double wellXOff;
    private static double wellYOff;

    public static final  String MATRI_6_WELL= "6WELL";
    public static final  String MATRI_12_WELL= "12WELL";
    public static final  String MATRI_24_WELL= "24WELL";
    public static final  String MATRI_48_WELL= "48WELL";
    public static final  String MATRI_96_WELL= "96WELL";
    public static final  String MATRI_384_WELL= "384WELL";
    public static final  String SLIDE_HOLDER ="SLIDES";

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

    public static boolean readXmlFile (String filePath, int wellNumber) {
        try {

            File fXmlFile = new File(filePath);

            if(fXmlFile.exists()){
                
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(fXmlFile);

                doc.getDocumentElement().normalize();

                System.out.println("Root element :" + doc.getDocumentElement().getNodeName());

               // Node node_ = doc.getElementsByTagName("WellParam").item(node_index);
                NodeList node_listeWelltypes = doc.getElementsByTagName("WellTypes");
                if (node_listeWelltypes.getLength() == 0){
                    return false;
                }
                for (int tmp = 0; tmp < node_listeWelltypes.getLength(); tmp++) {
                    Node nNode_types = node_listeWelltypes.item(tmp);
                  //  if (nNode_types.getNodeType() == Node.ELEMENT_NODE) {
                        Element eElement_types = (Element) nNode_types;
                        System.out.println(eElement_types.getAttribute("id"));
                        System.out.println("sortSize :" + nNode_types.getTextContent().length());
                        System.out.println("sort :" + nNode_types.getTextContent().charAt(1));

                        wellTypes.add( eElement_types.getElementsByTagName("well_1").item(0).getTextContent());
                  //  }
                }
                System.out.println("Solve : "+wellTypes);

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
                        if (Integer.parseInt(eElement.getAttribute("id")) == wellNumber ){

                            wellPlateID = Integer.parseInt(eElement.getAttribute("id"));
                            numColumns_ = Integer.parseInt(eElement.getElementsByTagName("numColumns_").item(0).getTextContent());
                            numRows_ = Integer.parseInt(eElement.getElementsByTagName("numRows_").item(0).getTextContent());
                            squareSize = Integer.parseInt(eElement.getElementsByTagName("squareSize").item(0).getTextContent());
                            space = Integer.parseInt(eElement.getElementsByTagName("space").item(0).getTextContent());

                            sizeXUm_  = Double.parseDouble(eElement.getElementsByTagName("sizeXUm_").item(0).getTextContent());
                            sizeYUm_ = Double.parseDouble(eElement.getElementsByTagName("sizeYUm_").item(0).getTextContent());

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

    public void calibrateXY(){

        ArrayList[] posXY = this.positionlists();

        if (posXY[0].size() == 0) {
            ReportingUtils.showMessage("Could not Calibrate Stage Due to 0 position list," +
                    " Please go to Stage position tab to add some position" +
                    " Or disable multi position (X,Y) Panel "
            );
        }else {

        int ret = JOptionPane.showConfirmDialog(RappGui.getInstance(), "Manually position the XY stage over the center of the well A01 and press OK",
                "XYStage origin setup", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ret == JOptionPane.OK_OPTION) {
            try {
                double x_pos_ini = (double) posXY[0].get(0); //store each element as a double in the array
                double y_pos_ini = (double) posXY[1].get(0); //store each element as a double in the array
                Point2D.Double cornet_pos ;

//                cornet_pos = core_.getXYStagePosition();
//                wellXOff = (x_pos_ini + cornet_pos.getX()) ;
//                wellYOff = (-y_pos_ini + cornet_pos.getY()) ;
//                System.out.println("Xoff = " +wellXOff + "__ Yoff= " + wellYOff);

                Thread.sleep(1000 );

                app_.setXYOrigin(this.getFirstWellOffX(), this.getFirstWellOffY());
                //regenerate();
                Point2D.Double pt = app_.getXYStagePosition();
                JOptionPane.showMessageDialog(RappGui.getInstance(), "XY Stage set at position: " + pt.x + "," + pt.y);
             //   JOptionPane.showMessageDialog(RappGui.getInstance(), "XY Stage set at position: " +wellXOff + "," + wellYOff);
            } catch (Exception e) {
                //displayError(e.getMessage());
                e.printStackTrace();
            }
          }
        }

    }

    public  int getWellPlateID (){
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
