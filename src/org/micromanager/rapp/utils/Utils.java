
package org.micromanager.rapp.utils;

import ij.process.FloatPolygon;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.Polygon;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Florial
 */
public class Utils {



   public static List<Polygon> FloatToNormalPolygon(List<FloatPolygon> floatPolygons) {
      // manually copy from FloatPolygon to Polygon
      List<Polygon> roiPolygons = new ArrayList<Polygon>();
      for (FloatPolygon fp : floatPolygons) {
         Polygon p = new Polygon();
         for (int i = 0; i < fp.npoints; i++) {
            p.addPoint((int) (0.5 + fp.xpoints[i]),
                    (int) (0.5 + fp.ypoints[i]));
         }
         roiPolygons.add(p);
      }
      return roiPolygons;
   }

   public static String[]  getSegmenterAlgoListe(){

      return new String[]{" ", "Find Peak", "Analyse Particles"};
   }


}
