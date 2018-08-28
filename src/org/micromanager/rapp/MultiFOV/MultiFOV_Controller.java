package org.micromanager.rapp.MultiFOV;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class MultiFOV_Controller {

    private static int wellPlateID;
    private static int colSize;
    private static int rowSize;
    private static int squareSize;
    private static int space;

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

    public static boolean valideXml(boolean isXmlFIleValide){

        System.out.println("FIle" + isXmlFIleValide);

        return isXmlFIleValide;
    }


}
