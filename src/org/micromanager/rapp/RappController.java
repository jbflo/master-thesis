package org.micromanager.rapp;

import ij.measure.Calibration;
import loci.formats.CoreMetadata;
import mmcorej.CMMCore;
import org.micromanager.MMStudio;
import org.micromanager.api.ScriptInterface;
import org.micromanager.utils.*;

import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.EllipseRoi;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.gui.OvalRoi;
import ij.gui.PointRoi;
import ij.gui.Roi;
import ij.io.RoiEncoder;
import ij.plugin.filter.GaussianBlur;
import ij.plugin.frame.RoiManager;
import ij.process.FloatPolygon;
import ij.process.ImageProcessor;

import java.awt.AWTEvent;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.Preferences;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.*;
import javax.swing.text.DefaultFormatter;

import mmcorej.CMMCore;
import mmcorej.Configuration;
import mmcorej.DeviceType;
import mmcorej.TaggedImage;

import org.json.JSONException;
import org.micromanager.api.ScriptInterface;
import org.micromanager.imagedisplay.VirtualAcquisitionDisplay;
import org.micromanager.utils.GUIUtils;
import org.micromanager.utils.MMFrame;
import org.micromanager.utils.MMListenerAdapter;


/**
 * The main Class as a Controller for the Rapp plugin. Contains logic for calibration,
 * and control for Camera and Galvos (Rapp UG-42).
 */

public class RappController extends  MMFrame implements OnStateListener {
    MMStudio gui_;
    CMMCore core_;
    CoreMetadata cm;
    private RappGui frame_;
    public boolean bleechingComp=false;
    public  Point roiPointClick = new Point();
    private static final RappController fINSTANCE =  new RappController();

    public static RappController getInstance() {
        return fINSTANCE;
    }

    public RappController() {
        gui_ = MMStudio.getInstance();
        core_ = gui_.getCore();
        frame_= RappGui.getInstance();
    }

    public void setLive(Boolean on) throws MMScriptException {
        if (on == true){
            Object img = null;
            try {
                core_.snapImage();
                img = core_.getImage();
            } catch (Exception ex) {
                Logger.getLogger(RappGui.class.getName()).log(Level.SEVERE, null, ex);
            }
            gui_.displayImage(img);
            gui_.setXYStagePosition(32.0, 32.0);
            //frame_.lbl_btn_onoff.add(img);
        }
    }



    public void getROIs() {
        ImagePlus image = IJ.getImage();
        Calibration cal = image.getCalibration();
        String unit = cal.getUnit().toString();
        double width = cal.pixelWidth;
        double height = cal.pixelHeight;
        RoiManager rm = RoiManager.getInstance();
        int roiCount = rm.getCount();
        Roi[] roiArray = rm.getRoisAsArray();
        ArrayList xRoiPosArray = new ArrayList();
        ArrayList yRoiPosArray = new ArrayList();
        ArrayList widthRoiPosArray = new ArrayList();
        ArrayList heightRoiPosArray = new ArrayList();
        ArrayList xcRoiPosArray = new ArrayList();
        ArrayList ycRoiPosArray = new ArrayList();
        for (int i=0; i<roiCount; i++){
            xRoiPosArray.add(roiArray[i].getXBase());
            yRoiPosArray.add(roiArray[i].getYBase());
            widthRoiPosArray.add(roiArray[i].getFloatWidth());
            heightRoiPosArray.add(roiArray[i].getFloatHeight());
            xcRoiPosArray.add(roiArray[i].getXBase()+Math.round(roiArray[i].getFloatWidth()/2));
            ycRoiPosArray.add(roiArray[i].getYBase()+Math.round(roiArray[i].getFloatHeight()/2));
        }
        System.out.println(xcRoiPosArray);
        System.out.println(ycRoiPosArray);

        double[] failsArrayX =  new double[xcRoiPosArray.size()];
        double[] failsArrayY =  new double[ycRoiPosArray.size()];
        for (int i = 0; i < xcRoiPosArray.size(); i++) { //iterate over the elements of the list

            failsArrayX[i] = Double.parseDouble(xcRoiPosArray.get(i).toString()); //store each element as a double in the array
            failsArrayY[i] = Double.parseDouble(ycRoiPosArray.get(i).toString()); //store each element as a double in the array
            roiPointClick.setLocation(failsArrayX[i],  failsArrayY[i]);
            System.out.println(roiPointClick);
        }
//        tableModel_.addWholeData(xcRoiPosArray);

    }

   /* private boolean containList(float x, float y) {
        Point2D p = new Point2D.Double(x,y);
        p.setLocation(4,4);
        for (Float[] coordinate : list) {
            float coordinateX = coordinate[0];
            float coordinateY = coordinate[1];

            if (coordinateX == x && coordinateY == y) {
                return true;
            }
        }
        return false;  // (x, y) not found
    }*/


    @Override
    public void stateChanged(boolean onState) {

    }
}
