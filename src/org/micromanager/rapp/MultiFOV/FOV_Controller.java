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
    private static int colSize;
    private static int rowSize;
    private static int squareSize;
    private static int space;

    // all dimensions in µm
    String plateName = "MatriPlate Brooks";
    double xPlate = 127760;
    double yPlate = 85480;
    double xWell = 3500;
    double yWell = 2900;
    double xOff = 12130;
    double yOff = 8990;
    double distWellsX = 4500;
    double distWellsY = 4500;
    double xFOV = 200;
    double yFOV = 200;
    int cols = 24;
    int rows = 16;
    String wellShape = "rectangle";

    private ArrayList<Double> xTab = new ArrayList<>();
    private ArrayList<Double> yTab = new ArrayList<>();



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
                            colSize = Integer.parseInt(eElement.getElementsByTagName("colSize").item(0).getTextContent());
                            rowSize = Integer.parseInt(eElement.getElementsByTagName("rowSize").item(0).getTextContent());
                            squareSize = Integer.parseInt(eElement.getElementsByTagName("squareSize").item(0).getTextContent());
                            space = Integer.parseInt(eElement.getElementsByTagName("space").item(0).getTextContent());

                            System.out.println("----------------------------");
                            System.out.println("Well Types: " +eElement.getAttribute("id"));
                            System.out.println("colSize : " + colSize);
                            System.out.println("rowSize : " + rowSize);
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
        return colSize;
    }

    public static int getrowSize() {
        return rowSize;
    }

    public static int getSquareSize() {
        return squareSize;
    }

    public static int getWellSpace() {
        return space;
    }

    public String getPlateName(){return plateName;}
    public double getPlateHeight(){return yPlate;}
    public double getPlateLength(){return xPlate;}
    public double getWellSizeX(){return xWell;}
    public double getWellSizeY(){return yWell;}
    public double getWellSpacingX(){return distWellsX;}
    public double getWellSpacingY(){return distWellsY;}
    public double getFirstWellOffX(){return xOff;}
    public double getFirstWellOffY(){return yOff;}
    public double getFOVsizeX(){return xFOV;}
    public double getFOVsizeY(){return yFOV;}
    public int getColCount(){return cols;}
    public int getRowCount(){return rows;}
    public String getWellShape(){return wellShape;}

    public static boolean valideXml(boolean isXmlFIleValide){

        System.out.println("FIle" + isXmlFIleValide);

        return isXmlFIleValide;
    }

    public  void  getWholeData( ArrayList<FOV> fovs) {


        for (int i = 0; i< fovs.size() ; i++){

            double xx = fovs.get(i).getX();
            double yy = fovs.get(i).getY();

            xTab.add(xx);
            yTab.add(yy);

        }

        System.out.println( " Xcord : "+ xTab );
        System.out.println(" Ycord  : "+ yTab );

        System.out.println( " True :  "+ xTab.get(0) * 2);
        System.out.println( " DataSize :"+ fovs.size());

    }


    public ArrayList[] positionlists() {

            return new ArrayList[]{xTab   , yTab};

    }




}
