package org.micromanager.rapp.MultiFOV;

import mmcorej.CMMCore;
import org.micromanager.api.ScriptInterface;
import org.micromanager.rapp.RappPlugin;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;

public class FOV_Controller {

    private CMMCore core_ = new CMMCore();
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

    private static double firstWellX_ ;
    private static double firstWellY_ ;

    private static double wellSpacingX_ ;
    private static double wellSpacingY_;

    private static double sizeXFOV_ ;
    private static double sizeYFOV_ ;

    public static final String MATRI_6_WELL= "6WELL";
    public static final String MATRI_12_WELL= "12WELL";
    public static final String MATRI_24_WELL= "24WELL";
    public static final String MATRI_48_WELL= "48WELL";
    public static final String MATRI_96_WELL= "96WELL";
    public static final String MATRI_384_WELL= "384WELL";
    public static final String SLIDE_HOLDER ="SLIDES";
   // public static final String IBIDI_24_WELL = "Ibidi-24WELL";
    public static final String DEFAULT_XYSTAGE_NAME = "XYStage";
    public static final String CUSTOM = "CUSTOM";
    private static final String METADATA_SITE_PREFIX = "Site";

    String wellShape = "rectangle";

    private static ArrayList<Double> xTab = new ArrayList<>();
    private static ArrayList<Double> yTab = new ArrayList<>();



    public FOV_Controller( CMMCore core) {

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

                NodeList node_liste = doc.getElementsByTagName("WellParam");

                if (node_liste.getLength() == 0){
                    return false;
                }

                System.out.println("----------------------------" + wellNumber);
                for (int temp = 0; temp < node_liste.getLength(); temp++) {

                    Node nNode = node_liste.item(temp);

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

                            System.out.println("----------------------------");
                            System.out.println("Well Types: " +eElement.getAttribute("id"));
                            System.out.println("colSize : " + numColumns_);
                            System.out.println("rowSize : " + numRows_);
                            System.out.println("squareSize : " + squareSize);
                            System.out.println("space : " + space);

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

    public static int getWellPlateID (){
        return wellPlateID;
    }

    public static int getcolSize() {
        return numColumns_;
    }

    public static int getrowSize() {
        return numRows_;
    }

    public static int getSquareSize() {
        return squareSize;
    }

    public static int getWellSpace() {
        return space;
    }

    public String getPlateName(){return plateName;}


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
