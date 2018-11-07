
package org.micromanager.rapp.utils;

import ij.process.FloatPolygon;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.Polygon;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
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

   // rearrangement to minimise a metric (the distance between consecutive points).
   public static void rebuild(List<Point2D.Double> l) {
      for (int i = 0; i < l.size() - 1; i++) {
         Point2D.Double a = l.get(i);
         // Find the closest.
         int closest = i;
         double howClose = Double.MAX_VALUE;
         for (int j = i + 1; j < l.size(); j++) {
            double howFar = a.distance(l.get(j));
            if (howFar < howClose) {
               closest = j;
               howClose = howFar;
            }
         }
         if (closest != i + 1) {
            // Swap it in.
            Collections.swap(l, i + 1, closest);
         }
      }
   }


}
