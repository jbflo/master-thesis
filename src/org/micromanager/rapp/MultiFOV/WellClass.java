/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.micromanager.rapp.MultiFOV;

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Frederik
 */
public class WellClass {
    private static final WellClass fINSTANCE =  new WellClass();
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
    
    public static WellClass getInstance() {
      return fINSTANCE;
  }

    public WellClass loadProperties(File fXmlFile){
        // source: http://www.mkyong.com/java/how-to-read-xml-file-in-java-dom-parser/
        // more general than necessary, but useful as reference...
        try {
            
//	File fXmlFile = new File("C:/Program Files (x86)/Micro-Manager-1.4-32 20 Oct 2014 build/mmplugins/OpenHCAFLIM/XPLT/Greiner uClear.xplt");
	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	Document doc = dBuilder.parse(fXmlFile);
 
	//optional, but recommended
	//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
	doc.getDocumentElement().normalize();
	System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
	NodeList nList = doc.getElementsByTagName("Plate");
 
	for (int temp = 0; temp < nList.getLength(); temp++) {
 
		Node nNode = nList.item(temp);
		 
		if (nNode.getNodeType() == Node.ELEMENT_NODE & "Plate".equals(nNode.getNodeName())) {
			Element eElement = (Element) nNode;
                        plateName = eElement.getAttribute("name");
                        rows = Integer.parseInt(eElement.getAttribute("rows"));
                        cols = Integer.parseInt(eElement.getAttribute("columns"));
		}
	}
        
        nList = doc.getElementsByTagName("WellParameters");
        for (int temp  = 0; temp < nList.getLength(); temp++){
        
            Node nNode = nList.item(temp);
            
            if (nNode.getNodeType() == Node.ELEMENT_NODE & "WellParameters".equals(nNode.getNodeName())) {
			Element eElement = (Element) nNode;
                        wellShape = eElement.getAttribute("shape");
                        xWell = Double.parseDouble(eElement.getAttribute("horizontal"));
                        yWell = Double.parseDouble(eElement.getAttribute("vertical"));
                        if ("mm".equals(eElement.getAttribute("unit"))){
                            yWell = yWell * 1000;
                            xWell = xWell * 1000;
                        } 
		}
        }
        
        nList = doc.getElementsByTagName("WellSpacing");
        for (int temp  = 0; temp < nList.getLength(); temp++){
        
            Node nNode = nList.item(temp);
            
            if (nNode.getNodeType() == Node.ELEMENT_NODE & "WellSpacing".equals(nNode.getNodeName())) {
			Element eElement = (Element) nNode;
                        distWellsX = Double.parseDouble(eElement.getAttribute("horizontal"));
                        distWellsY = Double.parseDouble(eElement.getAttribute("vertical"));
                        if ("mm".equals(eElement.getAttribute("unit"))){
                            distWellsX = distWellsX * 1000;
                            distWellsY = distWellsY * 1000;
                        }               
		    }
        }
        
        nList = doc.getElementsByTagName("TopLeftWellCenterOffset");
        for (int temp  = 0; temp < nList.getLength(); temp++){
        
            Node nNode = nList.item(temp);
            
            if (nNode.getNodeType() == Node.ELEMENT_NODE & "TopLeftWellCenterOffset".equals(nNode.getNodeName())) {
			Element eElement = (Element) nNode;
                        xOff = Double.parseDouble(eElement.getAttribute("horizontal"));
                        yOff = Double.parseDouble(eElement.getAttribute("vertical"));
                        if ("mm".equals(eElement.getAttribute("unit"))){
                            xOff = xOff * 1000;
                            yOff = yOff * 1000;
                        }               
		}
        }
    } catch (Exception e) {
	e.printStackTrace();
    }
        return this;
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
    
}
