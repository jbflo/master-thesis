///////////////////////////////////////////////////////////////////////////////
//FILE:          RappPlugin.java
//PROJECT:       Micro-Manager Laser Automated Plugin
//SUBSYSTEM:     RAPP plugin
//-----------------------------------------------------------------------------
//AUTHOR:        FLorial,
//SOURCE :       ProjectorPlugin, MultiD Acquisition
//COPYRIGHT:     ZMBH, University of Heidelberg, 2017-2018
//LICENSE:       This file is distributed under the
/////////////////////////////////////////////////////////////////////////////////


package org.micromanager.rapp;

import com.knoplab.segmenter.Cell;
import com.knoplab.segmenter.Segmentation;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.gui.PointRoi;
import ij.gui.Roi;
import ij.io.FileSaver;
import ij.plugin.Duplicator;
import ij.plugin.filter.Analyzer;
import ij.plugin.filter.GaussianBlur;
import ij.plugin.frame.RoiManager;
import ij.process.FloatProcessor;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import mmcorej.CMMCore;
import mmcorej.Configuration;
import mmcorej.StrVector;
import mmcorej.TaggedImage;
import org.json.JSONException;
import org.json.JSONObject;
import org.micromanager.MMStudio;
import org.micromanager.acquisition.TaggedImageStorageMultipageTiff;
import org.micromanager.api.MultiStagePosition;
import org.micromanager.api.PositionList;
import org.micromanager.api.ScriptInterface;
import org.micromanager.api.StagePosition;
import org.micromanager.imagedisplay.VirtualAcquisitionDisplay;
import org.micromanager.rapp.SequenceAcquisitions.SeqAcqController;
import org.micromanager.rapp.utils.*;
import org.micromanager.rapp.utils.FileDialog;
import org.micromanager.utils.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.Preferences;

import static java.util.Arrays.sort;

/**
 * This Class is approximately use as a Controller for the Rapp plugin. Contains logic for calibration,
 * and control for Camera and Galvos (Rapp UG-42).
 */

public class RappController extends  MMFrame implements OnStateListener {
    private URL default_path = this.getClass().getResource("");
    private String path = default_path.toString().substring(6);
    private final RappDevice dev_;
    private static RappPlugin rappPlugin = new RappPlugin() ;
    private final MouseListener pointAndShootMouseListener;
    private final AtomicBoolean pointAndShooteModeOn_ = new AtomicBoolean(false);
    private final CMMCore core_;
    private final ScriptInterface app_  ;
    private final boolean isSLM_;
    private Roi[] individualRois_ = {};
    private Map<Polygon, AffineTransform> mapping_ = null;
    private String mappingNode_ = null;
    private String targetingChannel_;
    AtomicBoolean stopRequested_ = new AtomicBoolean(false);
    AtomicBoolean isRunning_ = new AtomicBoolean(false);
    private String targetingShutter_;
    private static String defaultGroupConfig_;
    private static String defaultConfPrest_;
    private Boolean disposing_ = false;
    private static VirtualAcquisitionDisplay display_;
    private final MMStudio gui_;
    private String camera;
    private String galvo;
    public boolean bleechingComp=false;
    public List<Point2D.Double> roiPointClick = new ArrayList<>();
    public Point2D.Double roiTemp  = new Point2D.Double();
    FileDialog fileDialog_ = new FileDialog();


    //private static final RappController fINSTANCE =  new RappController(core_, app_);

//    public static RappController getInstance() {
//        return fINSTANCE;
//    }


    public RappController( CMMCore core, ScriptInterface app) throws Exception {
        app_ = app;
        gui_ = MMStudio.getInstance();
        core_ = core;
        camera = core_.getCameraDevice();
        String slm = core_.getSLMDevice();
        galvo = core_.getGalvoDevice();

        if (slm.length() > 0) {
            dev_ = new SLM(core_, 20);
        } else if (galvo.length() > 0) {
            dev_ = new Galvo(core_);
        } else {
            dev_ = null;
        }

        loadMapping();
        pointAndShootMouseListener = createPointAndShootMouseListenerInstance();

        Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
            @Override
            public void eventDispatched(AWTEvent e) {
                enablePointAndShootMode(pointAndShooteModeOn_.get());
            }
        }, AWTEvent.WINDOW_EVENT_MASK);
        isSLM_ = dev_ instanceof SLM;
    }

    /**
     * Simple utility methods for points
     * Adds a point to an existing polygon.
     */
    private static void addVertex(Polygon polygon, Point p) {
        polygon.addPoint(p.x, p.y);
    }

     /**
     * Returns the vertices of the given polygon as a series of points.
     */
     private static Point[] getVertices(Polygon polygon) {
         Point vertices[] = new Point[polygon.npoints];
         for (int i = 0; i < polygon.npoints; ++i) {
             vertices[i] = new Point(polygon.xpoints[i], polygon.ypoints[i]);
         }
         return vertices;
     }

    /**
     * Gets the vectorial mean of an array of Points.
     */
     private static Point2D.Double meanPosition2D(Point[] points) {
         double xsum = 0;
         double ysum = 0;
         int n = points.length;
         for (int i = 0; i < n; ++i) {
            xsum += points[i].x;
            ysum += points[i].y;
         }
         return new Point2D.Double(xsum/n, ysum/n);
     }

    /**
     * Converts a Point with double values for x,y to a point
     * with x and y rounded to the nearest integer.
     */
     private static Point toIntPoint(Point2D.Double pt) {
         return new Point((int) (0.5 + pt.x), (int) (0.5 + pt.y));
     }

     /**
      * Converts a Point with integer values to a Point with x and y doubles.
     */
     private static Point2D.Double toDoublePoint(Point pt) {
        return new Point2D.Double(pt.x, pt.y);
     }

    // ## Methods for handling targeting channel and shutter

    /**
     * Reads the available channels from Micro-Manager Channel Group
     * and populates the targeting channel drop-down menu.
     */
   /* final void populateChannelComboBox(String initialChannel) {
        if (initialChannel == null) {
            initialChannel = (String) channelComboBox.getSelectedItem();
        }
        channelComboBox.removeAllItems();
        channelComboBox.addItem("");
        // try to avoid crash on shutdown
        if (core_ != null) {
            for (String preset : core_.getAvailableConfigs(core_.getChannelGroup())) {
                channelComboBox.addItem(preset);
            }
            channelComboBox.setSelectedItem(initialChannel);
        }
    }

    *//**
     * Reads the available shutters from Micro-Manager and
     * lists them in the targeting shutter drop-down menu.
     *//*
    final void populateShutterComboBox(String initialShutter) {
        if (initialShutter == null) {
            initialShutter = (String) shutterComboBox.getSelectedItem();
        }
        shutterComboBox.removeAllItems();
        shutterComboBox.addItem("");
        // trying to avoid crashes on shutdown
        if (core_ != null) {
            for (String shutter : core_.getLoadedDevicesOfType(DeviceType.ShutterDevice)) {
                shutterComboBox.addItem(shutter);
            }
            shutterComboBox.setSelectedItem(initialShutter);
        }
    }*/

    /**
     * Sets the targeting channel. channelName should be
     * a channel from the current ChannelGroup.
     */
    void setTargetingChannel(String channelName) {
        targetingChannel_ = channelName;
        if (channelName != null) {
            Preferences.userNodeForPackage(this.getClass()).put("channel", channelName);
        }
    }

    /**
     * Sets the targeting shutter.
     * Should be the name of a loaded Shutter device.
     */
    void setTargetingShutter(String shutterName) {
        targetingShutter_ = shutterName;
        if (shutterName != null) {
            Preferences.userNodeForPackage(this.getClass()).put("shutter", shutterName);
        }
    }

    /**
     * Sets the Channel Group to the targeting channel, if it exists.
     * @return
     */
    public Configuration prepareChannel() {
        Configuration originalConfig = null;
        String channelGroup = core_.getChannelGroup();
        try {
            if (targetingChannel_ != null && targetingChannel_.length() > 0) {
                originalConfig = core_.getConfigGroupState(channelGroup);
                if (!originalConfig.isConfigurationIncluded(core_.getConfigData(channelGroup, targetingChannel_))) {
                    if (app_.isAcquisitionRunning()) {
                        app_.setPause(true);
                    }
                    core_.setConfig(channelGroup, targetingChannel_);
                }
            }
        } catch (Exception ex) {
            ReportingUtils.logError(ex);
        }
        return originalConfig;
    }

    /**
     * Should be called with the value returned by prepareChannel.
     * Returns Channel Group to its original settings, if needed.
     * @param originalConfig value returned by prepareChannel
     */
    public void returnChannel(Configuration originalConfig) {
        if (originalConfig != null) {
            try {
                core_.setSystemState(originalConfig);
                if (app_.isAcquisitionRunning() && app_.isPaused()) {
                    app_.setPause(false);
                }
            } catch (Exception ex) {
                ReportingUtils.logError(ex);
            }
        }
    }

    /**
     * Opens the targeting shutter, if it has been specified.
     * @return true if it was already open
     */
    public boolean prepareShutter() {
        try {
            if (targetingShutter_ != null && targetingShutter_.length() > 0) {
                boolean originallyOpen = core_.getShutterOpen(targetingShutter_);
                if (!originallyOpen) {
                    core_.setShutterOpen(targetingShutter_, true);
                    core_.waitForDevice(targetingShutter_);
                }
                return originallyOpen;
            }
        } catch (Exception ex) {
            ReportingUtils.logError(ex);
        }
        return true; // by default, say it was already open
    }

    /**
     * Closes a targeting shutter if it exists and if it was originally closed.
     * Should be called with the value returned by prepareShutter.
     * @param originallyOpen - whether or not the shutter was originally open
     */
    public void returnShutter(boolean originallyOpen) {
        try {
            if (targetingShutter_ != null &&
                    (targetingShutter_.length() > 0) &&
                    !originallyOpen) {
                core_.setShutterOpen(targetingShutter_, false);
                core_.waitForDevice(targetingShutter_);
            }
        } catch (Exception ex) {
            ReportingUtils.logError(ex);
        }
    }

    // ## Simple methods for device control.

    /**
     * Sets the exposure time for the phototargeting device.
     * @param intervalUs  new exposure time in micros
     */
    public void setExposure(double intervalUs) {
        long previousExposure = dev_.getExposure();
       // RappGui.getInstance().spiner.setText(String.valueOf(dev_.getExposure()));
      //  System.out.println( RappGui.getInstance().spiner.getText());
        long newExposure = (long) intervalUs;
        if ( previousExposure != newExposure) {
            dev_.setExposure(newExposure);
        }
    }

    public  void setCameraExposureTime(double value){

        try{

            if(value != Double.compare(value, 0.0)){
            core_.setExposure(value);
            }
        }
        catch (Exception ex){

        }

    }

    /**
     * Turns the projection device on or off.
     * @param onState on=true
     */
     public   void setOnState(boolean onState) {
        if (onState) {
            dev_.turnOn();
        } else {
            dev_.turnOff();
        }
    }

    /**
     * Illuminate a spot at position x,y.
     */
    private void displaySpot(double x, double y) {
       // if (x >= dev_.getXMinimum() && x < (dev_.getXRange() + dev_.getXMinimum())
           //     && y >= dev_.getYMinimum() && y < (dev_.getYRange() + dev_.getYMinimum())) {
            dev_.displaySpot(x, y);
       // }else System.out.println("not shoot");
    }

    /**
     * Illuminate a spot at the center of the Galvo/SLM range, for
     * the exposure time.
     */
    void displayCenterSpot() {
        double x = dev_.getXRange() / 2 + dev_.getXMinimum();
        double y = dev_.getYRange() / 2 + dev_.getYMinimum();
        dev_.displaySpot(x, y);
    }

    //// ################### ## Calibration #################################
    // ## Generating, loading and saving calibration mappings

    /**
     * Returns the java Preferences node where we store the Calibration mapping.
     * Each channel/camera combination is assigned a different node.
     */
    private Preferences getCalibrationNode() {
        try {
            return Preferences.userNodeForPackage(RappPlugin.class)
                    .node("calibration")
                    .node(dev_.getChannel())
                    .node(core_.getCameraDevice());
        } catch (NullPointerException npe) {
            return null;
        }
    }

    /**
     * Load the mapping for the current calibration node. The mapping maps each
     * polygon cell to an AffineTransform.
     */
    private Map<Polygon, AffineTransform> loadMapping() {
        Preferences prefs = getCalibrationNode();
        if (prefs == null) {
            return null;
        }
        String nodeStr = prefs.toString();
        if (mappingNode_ == null || !nodeStr.contentEquals(mappingNode_)) {
            mappingNode_ = nodeStr;
            mapping_ = JavaUtils.getObjectFromPrefs(
                    prefs,
                    dev_.getName(),
                    new HashMap<Polygon, AffineTransform>());
        }
        return mapping_;
    }

    /**
     * Save the mapping for the current calibration node. The mapping
     * maps each polygon cell to an AffineTransform.
     */
    private void saveMapping(HashMap<Polygon, AffineTransform> mapping) {
        JavaUtils.putObjectInPrefs(getCalibrationNode(), dev_.getName(), mapping);
        mapping_ = mapping;
        mappingNode_ = getCalibrationNode().toString();
    }

    // ## Methods for generating a calibration mapping.

    // Find the brightest spot in an ImageProcessor. The image is first blurred
    // and then the pixel with maximum intensity is returned.
    private static Point findPeak(ImageProcessor proc) {
        ImageProcessor blurImage = proc.duplicate();
        blurImage.setRoi((Roi) null);
        GaussianBlur blur = new GaussianBlur();
        blur.blurGaussian(blurImage, 10, 10, 0.01);
        //showProcessor("findPeak",proc);
        Point x = ImageUtils.findMaxPixel(blurImage);
        x.translate(1, 1);
        return x;
    }

    /**
     * Display a spot using the projection device, and return its current
     * location on the camera.  Does not do sub-pixel localization, but could
     * (just would change its return type, most other code would be OK with this)
     */
    private Point measureSpotOnCamera(Point2D.Double projectionPoint, boolean addToAlbum) {
        if (stopRequested_.get()) {
            return null;
        }
        try {
            dev_.turnOff();
            // JonD: wait to make sure the device gets turned off
            Thread.sleep(300);
            core_.snapImage();
            TaggedImage image = core_.getTaggedImage();
            ImageProcessor proc1 = ImageUtils.makeMonochromeProcessor(image);
            // JonD: should use the exposure that the user has set to avoid hardcoding a value;
            // if the user wants a different exposure time for calibration it's easy to specify
            // => commenting out next two lines
            // long originalExposure = dev_.getExposure();
            // dev_.setExposure(500000);
            displaySpot(projectionPoint.x, projectionPoint.y);
            // NS: Timing between displaySpot and snapImage is critical
            // we have no idea how fast the device will respond
            // if we add "dev_.waitForDevice(), then the RAPP UGA-40 will already have ended
            // its exposure before returning control
            // For now, wait for a user specified delay
        //    int delayMs = Integer.parseInt(RappGui.getInstance().delayField_.getValue().toString());
          //  Thread.sleep(delayMs);
            core_.snapImage();
            // NS: just make sure to wait until the spot is no longer displayed
            // JonD: time to wait is simply the exposure time less any delay
   //         Thread.sleep((int) (dev_.getExposure()/1000) - delayMs);
            // JonD: see earlier comment => commenting out next line
            // dev_.setExposure(originalExposure);
            TaggedImage taggedImage2 = core_.getTaggedImage();
            ImageProcessor proc2 = ImageUtils.makeMonochromeProcessor(taggedImage2);
            app_.displayImage(taggedImage2);
            // saving images to album is useful for debugging
            // TODO figure out why this doesn't work towards the end; maybe limitation on # of images in album
            // if (addToAlbum) {
            //    app_.addToAlbum(taggedImage2);
            // }
            ImageProcessor diffImage = ImageUtils.subtractImageProcessors(proc2.convertToFloatProcessor(), proc1.convertToFloatProcessor());
            Point maxPt = findPeak(diffImage);
            app_.getSnapLiveWin().getImagePlus().setRoi(new PointRoi(maxPt.x, maxPt.y));
            // NS: what is this second sleep good for????
            // core_.sleep(500);
            return maxPt;
        } catch (Exception e) {
            ReportingUtils.showError(e);
            return null;
        }
    }

    /**
     * Illuminate a spot at ptSLM, measure its location on the camera, and
     * add the resulting point pair to the spotMap.
     */
    private void measureAndAddToSpotMap(Map<Point2D.Double, Point2D.Double> spotMap,
                                        Point2D.Double ptSLM) {
        Point ptCam = measureSpotOnCamera(ptSLM, false);
        Point2D.Double ptCamDouble = new Point2D.Double(ptCam.x, ptCam.y);
        spotMap.put(ptCamDouble, ptSLM);
    }

    /**
     * Illuminates and images five control points near the center,
     * and return an affine transform mapping from image coordinates
     * to phototargeter coordinates.
     */
    private AffineTransform generateLinearMapping() {
        double centerX = dev_.getXRange() / 2 + dev_.getXMinimum();
        double centerY = dev_.getYRange() / 2 + dev_.getYMinimum();
        double spacing = Math.min(dev_.getXRange(), dev_.getYRange()) / 25;  // use 4% of galvo/SLM range
        Map<Point2D.Double, Point2D.Double> spotMap
                = new HashMap<Point2D.Double, Point2D.Double>();

        measureAndAddToSpotMap(spotMap, new Point2D.Double(centerX, centerY));
        measureAndAddToSpotMap(spotMap, new Point2D.Double(centerX, centerY + spacing));
        measureAndAddToSpotMap(spotMap, new Point2D.Double(centerX + spacing, centerY));
        measureAndAddToSpotMap(spotMap, new Point2D.Double(centerX, centerY - spacing));
        measureAndAddToSpotMap(spotMap, new Point2D.Double(centerX - spacing, centerY));
        if (stopRequested_.get()) {
            return null;
        }
        try {
            // require that the RMS value between the mapped points and the measured points be less than 5% of image size
            // also require that the RMS value be less than the spacing between points in the galvo/SLM coordinate system
            // (2nd requirement was probably the intent of the code until r15505, but parameters were interchanged in call)
            final long imageSize = Math.min(core_.getImageWidth(), core_.getImageHeight());
            return MathFunctions.generateAffineTransformFromPointPairs(spotMap, imageSize*0.05, spacing);
        } catch (Exception e) {
            throw new RuntimeException("Spots aren't detected as expected. Is DMD in focus and roughly centered in camera's field of view?");
        }
    }

    /**
     * Generate a nonlinear calibration mapping for the current device settings.
     * A rectangular lattice of points is illuminated one-by-one on the
     * projection device, and locations in camera pixels of corresponding
     * spots on the camera image are recorded. For each rectangular
     * cell in the grid, we take the four point mappings (camera to projector)
     * and generate a local AffineTransform using linear least squares.
     * Cells with suspect measured corner positions are discarded.
     * A mapping of cell polygon to AffineTransform is generated.
     */
    private Map<Polygon, AffineTransform> generateNonlinearMapping() {

        // get the affine transform near the center spot
        final AffineTransform firstApproxAffine = generateLinearMapping();

        // then use this single transform to estimate what SLM coordinates
        // correspond to the image's corner positions
        final Point2D.Double camCorner1 = (Point2D.Double) firstApproxAffine.transform(new Point2D.Double(0, 0), null);
        final Point2D.Double camCorner2 = (Point2D.Double) firstApproxAffine.transform(new Point2D.Double((int) core_.getImageWidth(), (int) core_.getImageHeight()), null);
        final Point2D.Double camCorner3 = (Point2D.Double) firstApproxAffine.transform(new Point2D.Double(0, (int) core_.getImageHeight()), null);
        final Point2D.Double camCorner4 = (Point2D.Double) firstApproxAffine.transform(new Point2D.Double((int) core_.getImageWidth(), 0), null);

        // figure out camera's bounds in SLM coordinates
        // min/max because we don't know the relative orientation of the camera and SLM
        // do some extra checking in case camera/SLM aren't at exactly 90 degrees from each other,
        // but still better that they are at 0, 90, 180, or 270 degrees from each other
        // TODO can create grid along camera location instead of SLM's if camera is the limiting factor; this will make arbitrary rotation possible
        final double camLeft = Math.min(Math.min(Math.min(camCorner1.x, camCorner2.x), camCorner3.x), camCorner4.x);
        final double camRight = Math.max(Math.max(Math.max(camCorner1.x, camCorner2.x), camCorner3.x), camCorner4.x);
        final double camTop = Math.min(Math.min(Math.min(camCorner1.y, camCorner2.y), camCorner3.y), camCorner4.y);
        final double camBottom = Math.max(Math.max(Math.max(camCorner1.y, camCorner2.y), camCorner3.y), camCorner4.y);

        // these are the SLM's bounds
        final double slmLeft = dev_.getXMinimum();
        final double slmRight = dev_.getXRange() + dev_.getXMinimum();
        final double slmTop = dev_.getYMinimum();
        final double slmBottom = dev_.getYRange() + dev_.getYMinimum();

        // figure out the "overlap region" where both the camera and SLM
        // can "see", expressed in SLM coordinates
        final double left = Math.max(camLeft, slmLeft);
        final double right = Math.min(camRight, slmRight);
        final double top = Math.max(camTop, slmTop);
        final double bottom = Math.min(camBottom, slmBottom);
        final double width = right - left;
        final double height = bottom - top;

        // compute a grid of SLM points inside the "overlap region"
        // nGrid is how many polygons in both X and Y
        // require (nGrid + 1)^2 spot measurements to get nGrid^2 squares
        // TODO allow user to change nGrid
        final int nGrid = 7;
        Point2D.Double slmPoint[][] = new Point2D.Double[1 + nGrid][1 + nGrid];
        Point2D.Double camPoint[][] = new Point2D.Double[1 + nGrid][1 + nGrid];

        // tabulate the camera spot at each of SLM grid points
        for (int i = 0; i <= nGrid; ++i) {
            for (int j = 0; j <= nGrid; ++j) {
                double xoffset = ((i + 0.5) * width / (nGrid + 1.0));
                double yoffset = ((j + 0.5) * height / (nGrid + 1.0));
                slmPoint[i][j] = new Point2D.Double(left + xoffset, top + yoffset);
                Point spot = measureSpotOnCamera(slmPoint[i][j], true);
                if (spot != null) {
                    camPoint[i][j] = toDoublePoint(spot);
                }
            }
        }

        if (stopRequested_.get()) {
            return null;
        }

        // now make a grid of (square) polygons (in camera's coordinate system)
        // and generate an affine transform for each of these square regions
        Map<Polygon, AffineTransform> bigMap
                = new HashMap<Polygon, AffineTransform>();
        for (int i = 0; i <= nGrid - 1; ++i) {
            for (int j = 0; j <= nGrid - 1; ++j) {
                Polygon poly = new Polygon();
                addVertex(poly, toIntPoint(camPoint[i][j]));
                addVertex(poly, toIntPoint(camPoint[i][j + 1]));
                addVertex(poly, toIntPoint(camPoint[i + 1][j + 1]));
                addVertex(poly, toIntPoint(camPoint[i + 1][j]));

                Map<Point2D.Double, Point2D.Double> map
                        = new HashMap<Point2D.Double, Point2D.Double>();
                map.put(camPoint[i][j], slmPoint[i][j]);
                map.put(camPoint[i][j + 1], slmPoint[i][j + 1]);
                map.put(camPoint[i + 1][j], slmPoint[i + 1][j]);
                map.put(camPoint[i + 1][j + 1], slmPoint[i + 1][j + 1]);
                double srcDX = Math.abs((camPoint[i+1][j].x - camPoint[i][j].x))/4;
                double srcDY = Math.abs((camPoint[i][j+1].y - camPoint[i][j].y))/4;
                double srcTol = Math.max(srcDX, srcDY);

                try {
                    AffineTransform transform = MathFunctions.generateAffineTransformFromPointPairs(map, srcTol, Double.MAX_VALUE);
                    bigMap.put(poly, transform);
                } catch (Exception e) {
                    ReportingUtils.logError("Bad cell in mapping.");
                }
            }
        }
        return bigMap;
    }

    /**
     * Runs the full calibration. First
     * generates a linear mapping (a first approximation) and then generates
     * a second piece-wise "non-linear" mapping of affine transforms. Saves
     * the mapping to Java Preferences.
     */
    public void runCalibration() {
        final boolean liveModeRunning = app_.isLiveModeOn();
        app_.enableLiveMode(false);
        if (!isRunning_.get()) {
            stopRequested_.set(false);
            Thread th = new Thread("RappPlugin calibration thread") {
                @Override
                public void run() {
                    try {
                        isRunning_.set(true);
                        Roi originalROI = IJ.getImage().getRoi();
                        // JonD: don't understand why we do this
                        // app_.snapSingleImage();
                        // do the heavy lifting of generating the local affine transform map
                        HashMap<Polygon, AffineTransform> mapping =
                                (HashMap<Polygon, AffineTransform>) generateNonlinearMapping();
                        dev_.turnOff();
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException ex) {
                            ReportingUtils.logError(ex);
                        }
                        // save local affine transform map to preferences
                        // TODO allow different mappings to be stored for different channels (e.g. objective magnification)
                        if (!stopRequested_.get()) {
                            saveMapping(mapping);
                        }

                        app_.enableLiveMode(liveModeRunning);
                        JOptionPane.showMessageDialog(IJ.getImage().getWindow(), "Calibration "
                                + (!stopRequested_.get() ? "finished." : "canceled."));
                        IJ.getImage().setRoi(originalROI);
                    } catch (HeadlessException e) {
                        ReportingUtils.showError(e);
                    } catch (RuntimeException e) {
                        ReportingUtils.showError(e);
                    } finally {
                        isRunning_.set(false);
                        stopRequested_.set(false);
                        RappGui.getInstance().calibrate_btn.setText("Calibrated");
                    }
                }
            };
            th.start();
        }
    }

    /**
     * Returns true if the calibration is currently running.
     * @return true if calibration is running
     */
    public boolean isCalibrating() {
        return isRunning_.get();
    }

    /**
     * Requests an interruption to calibration while it is running.
     */
    public void stopCalibration() {
        stopRequested_.set(true);
    }

    // ## Transforming points according to a nonlinear calibration mapping.

    // Transform a point, pt, given the mapping, which is a Map of polygon cells
    // to AffineTransforms.
    private static Point2D.Double transformPoint(Map<Polygon, AffineTransform> mapping, Point2D.Double pt) {
        Set<Polygon> set = mapping.keySet();
        // First find out if the given point is inside a cell, and if so,
        // transform it with that cell's AffineTransform.
        for (Polygon poly : set) {
            if (poly.contains(pt)) {
                return (Point2D.Double) mapping.get(poly).transform(pt, null);
            }
        }
        // The point isn't inside any cell, so search for the closest cell
        // and use the AffineTransform from that.
        double minDistance = Double.MAX_VALUE;
        Polygon bestPoly = null;
        for (Polygon poly : set) {
            double distance = meanPosition2D(getVertices(poly)).distance(pt.x, pt.y);
            if (minDistance > distance) {
                bestPoly = poly;
                minDistance = distance;
            }
        }
        if (bestPoly == null) {
            throw new RuntimeException("Unable to map point to device.");
        }
        return (Point2D.Double) mapping.get(bestPoly).transform(pt, null);
    }


    // Returns true if a particular image is mirrored.
    private static boolean isImageMirrored(ImagePlus imgp) {
        try {
            String mirrorString = VirtualAcquisitionDisplay.getDisplay(imgp)
                    .getCurrentMetadata().getString("ImageFlipper-Mirror");
            return (mirrorString.contentEquals("On"));
        } catch (JSONException | NullPointerException e) {
            return false;
        }
    }

    // Flips a point if it has been mirrored.
    private static Point2D.Double mirrorIfNecessary(Point2D.Double pOffscreen, ImagePlus imgp) {
        if (isImageMirrored(imgp)) {
            return new Point2D.Double(imgp.getWidth() - pOffscreen.x, pOffscreen.y);
        } else {
            return pOffscreen;
        }
    }

    // Transform and mirror (if necessary) a point on an image to
    // a point on phototargeter coordinates.
    private static Point2D.Double transformAndMirrorPoint(Map<Polygon, AffineTransform> mapping,
                                                          ImagePlus imgp, Point2D.Double pt) {
        Point2D.Double pOffscreen = mirrorIfNecessary(pt, imgp);
        return transformPoint(mapping, pOffscreen);
    }

     // ################### Part 2: ## Point and shoot #################################

     // Turn on/off point and shoot mode.
     public void enablePointAndShootMode(boolean on) {
         if (on && (mapping_ == null)) {
             ReportingUtils.showError("Please calibrate the phototargeting device first, using the Settings option.");
             throw new RuntimeException("Please calibrate the phototargeting device first, using the Settings option");
         }
         pointAndShooteModeOn_.set(on);
         ImageWindow window = WindowManager.getCurrentWindow();
         if (window != null) {
             ImageCanvas canvas = window.getCanvas();
             if (on) {
                 if (canvas != null) {
                     boolean found = false;
                     for (MouseListener listener : canvas.getMouseListeners()) {
                         if (listener == pointAndShootMouseListener) {
                             found = true;
                         }
                     }
                     if (!found) {
                         canvas.addMouseListener(pointAndShootMouseListener);
                     }
                 }
             } else {
                 for (MouseListener listener : canvas.getMouseListeners()) {
                     if (listener == pointAndShootMouseListener) {
                         canvas.removeMouseListener(listener);
                     }
                 }
             }
         }
     }
    // Creates a MouseListener instance for future use with Point and Shoot
    // mode. When the MouseListener is attached to an ImageJ window, any
    // clicks will result in a spot being illuminated.
    private MouseListener createPointAndShootMouseListenerInstance() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.isShiftDown()) {
                    Point p = e.getPoint();
                    ImageCanvas canvas = (ImageCanvas) e.getSource();
                    Point pOffscreen = new Point(canvas.offScreenX(p.x), canvas.offScreenY(p.y));
                    System.out.println(pOffscreen.x + "_" + pOffscreen.y );
                    final Point2D.Double devP = transformAndMirrorPoint(loadMapping(), canvas.getImage(),
                            new Point2D.Double(pOffscreen.x, pOffscreen.y));
                    System.out.println(devP);
                    final Configuration originalConfig = prepareChannel();
                    final boolean originalShutterState = prepareShutter();
                    makeRunnableAsync(
                            () -> {
                                try {
                                    if (devP != null) {
                                        displaySpot(devP.x, devP.y);
                                    }else ReportingUtils.showError("Please Try Again! Your click return Null");

                                    returnShutter(originalShutterState);
                                    returnChannel(originalConfig);
                                } catch (Exception e1) {
                                    ReportingUtils.showError(e1);
                                }
                            }).run();
                }
            }
        };
    }

    ///////////////////////////////# Read the all The Marked ROis and Shoot on Them #///////////////////////////
    public void createMultiPointAndShootFromRoeList() {
        makeRunnableAsync(
            () -> {
                ImagePlus image;

                RoiManager rm = RoiManager.getInstance();


                if (rm != null) {
                    int roiCount = rm.getCount();
                    Roi[] roiArray =   rm.getSelectedRoisAsArray();
                    image = IJ.getImage();
                    ArrayList widthRoiPosArray = new ArrayList(roiCount);
                    ArrayList heightRoiPosArray = new ArrayList(roiCount);
                    ArrayList xcRoiPosArray = new ArrayList(roiCount);
                    ArrayList ycRoiPosArray = new ArrayList(roiCount);

                    if (roiCount != 0){
                        for (int i = 0; i < roiCount; i++) {
                            widthRoiPosArray.add(roiArray[i].getFloatWidth());
                            heightRoiPosArray.add(roiArray[i].getFloatHeight());
                            xcRoiPosArray.add(roiArray[i].getXBase() + Math.round(roiArray[i].getFloatWidth() / 2));
                            ycRoiPosArray.add(roiArray[i].getYBase() + Math.round(roiArray[i].getFloatHeight() / 2));
                        }
                    }  else ReportingUtils.showError("Please set / Add Rois Manager before Shooting  ");

                    double[] failsArrayX = new double[xcRoiPosArray.size()];
                    double[] failsArrayY = new double[ycRoiPosArray.size()];
                    System.out.println(xcRoiPosArray.size());

                    for (int i = 0; i < xcRoiPosArray.size(); i++) { //iterate over the elements of the list
                        failsArrayX[i] = Double.parseDouble(xcRoiPosArray.get(i).toString()); //store each element as a double in the array
                        failsArrayY[i] = Double.parseDouble(ycRoiPosArray.get(i).toString()); //store each element as a double in the array

                        final Point2D.Double devP = transformAndMirrorPoint(loadMapping(), image,
                                new Point2D.Double(failsArrayX[i], failsArrayY[i]));
                        System.out.println(devP);

                        final Configuration originalConfig = prepareChannel();
                        final boolean originalShutterState = prepareShutter();
                        try {
                            Point2D.Double galvoPos = core_.getGalvoPosition(galvo);
                            if (galvoPos != devP) {
                                // core_.setGalvoIlluminationState(galvo, false);
                                Thread.sleep(200);
                                core_.setGalvoPosition(galvo, devP.x, devP.y);
                                Thread.sleep(200);
                                //core_.setGalvoIlluminationState(galvo,true);
                                //core_.waitForDevice(galvo);
                            } else ReportingUtils.showError("Please Try Again! Galvo problem");
                            Thread.sleep(dev_.getExposure());
                            displaySpot(devP.x, devP.y);
                            returnShutter(originalShutterState);
                            returnChannel(originalConfig);
                            Thread.sleep(1000); // Do Nothing for 1000 ms (4s)
                        } catch (Exception ec) {
                            ReportingUtils.showError(ec);
                        }
                    }
                }  else ReportingUtils.showError("Please set / Add Rois Manager before Shooting  ");

        }).run();
    }
///////////////////////////////# Receive All The Point from the Machine Learning P and Shoot on them #///////////////////////////
    public ArrayList[] imageSegmentation(ImagePlus impproc, String path, String Algo,  boolean kill , boolean save) {

        impproc.show();
        impproc.updateAndRepaintWindow();

        if (Algo == "Find Peak"){
            IJ.run(impproc, "Gaussian Blur...", "sigma=5");
            if (kill){
                IJ.run(impproc, "Find Maxima...", "noise=20 output=List add");
            }else {
                IJ.run(impproc, "Find Maxima...", "noise=20 output=List exclude");
            }

        }else if (Algo == ""){

            IJ.run(impproc,"Threshold...", "Default B&W");
            if (kill){
                 IJ.run(impproc,"Analyze Particles...", "size=0-infinity pixel add");  //change range for cell size filtering

            }else {
                IJ.run(impproc,"Analyze Particles...", "size=0-infinity pixel summarize ");

            }
        }

        ij.measure.ResultsTable resTab = Analyzer.getResultsTable();
        int resCount = resTab.getCounter();
        ArrayList xTab = new ArrayList();
        ArrayList yTab = new ArrayList();
        double[] x = new double[resCount];
        double[] y = new double[resCount];
        Roi[] r = new Roi[resCount];
        RoiManager rm = RoiManager.getInstance();
        ImageProcessor ip = impproc.getProcessor();
        String text="0";
        for (int i=0; i<resCount; i++){
            if (SeqAcqController.stopAcqRequested_.get()) {
                ReportingUtils.showMessage("Acquisition Stop.");
                break;
            }
            final Configuration originalConfig = prepareChannel();
            final boolean originalShutterState = prepareShutter();

            double xx = resTab.getValueAsDouble(0, i);
            double yy = resTab.getValueAsDouble(1, i);
            xTab.add(xx);
            yTab.add(yy);
            x[i]=xx;
            y[i]=yy;
            text = String.valueOf(i) ;
            r[i]= new Roi(xx-5,yy-5,10,10);
            impproc.setRoi(new Roi(xx-5,yy-5,10,10));

            ip = impproc.getProcessor();
            Font font = new Font("SansSerif", Font.PLAIN, 32);
            ip.setFont(font);
            ip.setColor(new Color(255, 255, 0));
            ip.drawString(text, (int) xx, (int)yy);
            impproc.updateAndDraw();

            IJ.run("Draw");
        }
       // impproc.setRoi(r);
        System.out.println( " Xcord "+ xTab);
        System.out.println(" Ycord "+ yTab);
        impproc.updateAndRepaintWindow();
        if(path !=null && save){
            IJ.save(impproc, path+ "_"+"Segmented.tif");
        }

        return new ArrayList[]{xTab, yTab};
    }

    public void shootFromSegmentationListPoint(ArrayList[] segmentatio_pt, long laser_exp) {

        //sort(segmentatio_pt);
        if (segmentatio_pt.length != 0 ) {
            //Roi[] roiArray = rm.;

            ImagePlus iplus_ = IJ.getImage();
            double[] failsArrayX =  new double[segmentatio_pt[0].size()];
            double[] failsArrayY =  new double[segmentatio_pt[1].size()];

            if (defaultGroupConfig_ != null && defaultConfPrest_ != null){
                System.out.println(defaultGroupConfig_ + "__" + defaultConfPrest_);
                try {
                    core_.setConfig(defaultGroupConfig_, defaultConfPrest_);
                    app_.setChannelExposureTime(defaultGroupConfig_, defaultConfPrest_, 10);
                }catch (Exception ex){
                    ReportingUtils.showError("Unable to change Default Configuration Group Name and Preset ");
                }

            }else System.out.println("___"); // Don't need to do anything

            System.out.println(" SIze: "+ segmentatio_pt[1].size());
            System.out.println(" Val: "+ segmentatio_pt.toString());
            for (int i =0 ; i < segmentatio_pt[0].size(); i++)
            {
                if (SeqAcqController.stopAcqRequested_.get()) {
                   // ReportingUtils.showMessage("Acquisition Stop.");
                    break;
                }
                //iterate over the elements of the list
                System.out.println( " Xval" + segmentatio_pt[0].get(i).toString());
                System.out.println(" Yval" + segmentatio_pt[1].get(i).toString());

                System.out.println("_________________________________");

                failsArrayX[i] = (double) segmentatio_pt[0].get(i); //store each element as a double in the array
                failsArrayY[i] = (double) segmentatio_pt[1].get(i); //store each element as a double in the array

                //failsArrayX[i] = Double.parseDouble(segmentatio_pt[0].get(i).toString()); //store each element as a double in the array
                //failsArrayY[i] = Double.parseDouble(segmentatio_pt[1].get(i).toString()); //store each element as a double in the array
                  Point2D.Double devP = transformPoint(loadMapping(), new Point2D.Double(failsArrayX[i], failsArrayY[i]));
                //final Point2D.Double devP = transformAndMirrorPoint(loadMapping(), iplus_,
                    //    new Point2D.Double(failsArrayX[i], failsArrayY[i]));
             //   System.out.println(devP);

                final Configuration originalConfig = prepareChannel();
                final boolean originalShutterState = prepareShutter();
                try {
                    Point2D.Double galvoPos = core_.getGalvoPosition(galvo);
//                            if (galvoPos != devP){
//                                // core_.setGalvoIlluminationState(galvo, false);
//                                Thread.sleep(200);
//                                core_.setGalvoPosition(galvo, devP.x, devP.y);
//                                Thread.sleep(200);
//                                //core_.setGalvoIlluminationState(galvo,true);
//                                //core_.waitForDevice(galvo);
//                            }else ReportingUtils.showError("Please Try Again! Galvo problem");//
                    this.setExposure(laser_exp);
                    returnShutter(originalShutterState);
                    returnChannel(originalConfig);
                    displaySpot(devP.x, devP.y);
                    Thread.sleep( laser_exp); // Do Nothing and let the spot in this position the value of laser_exp

                }catch (Exception ec){
                    ReportingUtils.showError(ec);
                    break;
                }
            }
        }
        else ReportingUtils.showError("No Cell Coordinate were found on this Image Segmented, Click Ok to Continue \\\" ");
    }



    // Generates a runnable that runs the selected ROIs.
    private Runnable phototargetROIsRunnable(final String runnableName) {
        return new Runnable() {
            @Override
            public void run() {
               // runRois();
            }
            @Override
            public String toString() {
                return runnableName;
            }
        };
    }

    /**
     * Converts a Runnable to one that runs asynchronously.
     * @param runnable synchronous Runnable
     * @return asynchronously running Runnable
     */
    public static Runnable makeRunnableAsync(final Runnable runnable) {
        return new Runnable() {
            @Override
            public void run() {
                new Thread() {
                    @Override
                    public void run() {
                        runnable.run();
                    }
                }.start();
            }
        };
    }

    /////////////////////// Liste of Segmenter Algoritme //////////////////////////////////



    // Sleep until the designated clock time.
    private static void sleepUntil(long clockTimeMillis) {
        long delta = clockTimeMillis - System.currentTimeMillis();
        if (delta > 0) {
            try {
                Thread.sleep(delta);
            } catch (InterruptedException ex) {
                ReportingUtils.logError(ex);
            }
        }
    }

    public  String[]  getConfigGroup(){
        StrVector conf_group =  core_.getAvailableConfigGroups();

        return conf_group.toArray();
    }


    public  String[]  getConfigPreset( String groupN){
        StrVector conf_group =  core_.getAvailableConfigGroups();
        StrVector conf_preset = null;
        for (int i=0; i < conf_group.size() ; i++){
          conf_preset  =  core_.getAvailableConfigs(groupN);
        }
        return conf_preset.toArray();
    }

    public void ChangeConfigSet(String groupName, String configName) {
            try {
                core_.waitForDevice(core_.getCameraDevice());
                if (groupName != null && configName != null){
                    core_.setConfig(groupName, configName);
                    defaultGroupConfig_ = groupName;
                    defaultConfPrest_ = configName;
                    //System.out.println(defaultGroupConfig_ + "__" + defaultConfPrest_);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
                ReportingUtils.showError("Please Try Again! were unable to change Configuration preset");

            }
    }

    public void fluorescenceSequence(String groupName, String sequenceName, boolean save) {
        String configNmae = sequenceName;
        TaggedImage engineOutputQueue = null;
        TaggedImageStorageMultipageTiff stackStorage =null;
        ImagePlus iPlus = null;
        ImageStack stack = new ImageStack();
        String fileToSave = "";
        String dirToSave = "";
        try {
            // Set Core_Shutter to use Spectra
            core_.waitForDevice(core_.getCameraDevice());
            core_.setAutoShutter(false);
            Thread.sleep(100); // wait and set  Spectra sate to One
            app_.enableLiveMode(false);
            JSONObject summary = new JSONObject();

            summary.put("Prefix",sequenceName);
            //summary.put("Slices", 1);
            //summary.put("Positions", 1);
            summary.put("Channels", 2);

            summary.put("PixelType", "GRAY16");
            summary.put("Width",512);
            summary.put("Height",512);
            //these are used to create display settings
            summary.put("ChColors", new org.json.JSONArray("[1,1]"));
            summary.put("ChNames", new org.json.JSONArray("[DAPI,FITC]"));
            summary.put("ChMins", new org.json.JSONArray("[0,0]"));
            summary.put("ChMaxes", new org.json.JSONArray("[65535,65535]"));
            if (save) {
               fileToSave = fileDialog_.SaveFileDialog();
             // dirToSave = fileDialog_.ChooseDirectoryDialog();
              // IJ.save(iPlus, fileToSave.getAbsolutePath() + ".tif");
            }

            if (groupName != null && sequenceName != null){
                if (sequenceName =="Apply ALL Sequence"){
                    String[] allPreset = getConfigPreset(groupName);
                    System.out.println(allPreset.length);
                    //core_.startSequenceAcquisition(camera, allPreset.length,10,true);
                     // for(int i=0; i< allPreset.length; i++){
                    for(int i=0; i<2; i++){
                          core_.setConfig(groupName, allPreset[i]);
                          summary.put("Channels", allPreset[i].length());
                          System.out.println(allPreset[i].length());
                          Thread.sleep(1000);
                          core_.snapImage();
                          engineOutputQueue = core_.getTaggedImage();
                          core_.getLastTaggedImage();
                          //app_.snapSingleImage();
                          iPlus = IJ.getImage();
                          stack = iPlus.getStack();
                          if(save || fileToSave != null || dirToSave != null) {
                              IJ.save(iPlus, fileToSave + "_" + allPreset[i] + ".tif");
                              // new FileSaver(iPlus).saveAsTiffStack(fileToSave);
                              // IJ.saveAsTiff(iPlus,fileToSave);
                               stackStorage = new TaggedImageStorageMultipageTiff(fileToSave, true, summary, false, true, true);
                              // TaggedImageStorageDiskDefault separateStorage = new TaggedImageStorageDiskDefault(fileToSave, true, summary);
                               if (engineOutputQueue == null) engineOutputQueue = core_.getTaggedImage();
                              // separateStorage.putImage(engineOutputQueue);
                               stackStorage.putImage( engineOutputQueue);
                          }

                      }


                    }
                else{
                    core_.setConfig(groupName, configNmae);
                    summary.put("Channels",sequenceName.length());
                    Thread.sleep(1000);
                    core_.snapImage();
                    engineOutputQueue = core_.getTaggedImage();
                    if(save || fileToSave != null || dirToSave != null) {
                      // stackStorage = new TaggedImageStorageMultipageTiff(fileToSave, true, summary, false, true, true);
                      //  TaggedImageStorageDiskDefault separateStorage = new TaggedImageStorageDiskDefault(dirToSave, true, summary);
                        // if (engineOutputQueue == null) engineOutputQueue = core_.getTaggedImage();
                      //  stackStorage.putImage( engineOutputQueue);
                       // separateStorage.putImage(engineOutputQueue);
                        new FileSaver(iPlus).saveAsTiffStack(dirToSave);
                    }

                    }
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
            ReportingUtils.showError("Please Try Again! were unable to change filter color");

        }
    }


    public void  runSegmentation (String xmlPath,  String taggPath_){

        ImagePlus  iPlus = IJ.openImage(taggPath_);
        ImageStack stack = iPlus.getStack();
        // Object pixel = iPlus.getPixel(iPlus.getWidth(), iPlus.getHeight());
        Object pixelArray = stack.getPixels(1);
        JSONObject summary = new JSONObject();
        try {
            summary.put("Width", iPlus.getWidth());
            summary.put("Height",iPlus.getHeight());
            summary.put("BitDepth",iPlus.getBitDepth());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        TaggedImage image = new TaggedImage(pixelArray, summary);

        try {
            Segmentation seg = new Segmentation(xmlPath, image);
            Cell cell;
            System.out.println("Has next? " + seg.hasNext());
            while (seg.hasNext()) {
                cell = seg.next();
                System.out.println(cell.getCenterX() + ":" + cell.getCenterY());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList[] findCells(ImagePlus impOrg) {
        // ImagePlus impOrg = IJ.getImage();
        ImagePlus imp = new Duplicator().run(impOrg);
        impOrg.setTitle("Original");
        imp.setTitle("Working on ...");

        imp.show();
        imp.updateAndRepaintWindow();

//      IJ.run(imp, "Gaussian Blur...", "sigma=5");
//      IJ.run(imp, "Find Maxima...", "noise=20 output=List exclude");

        IJ.run(imp,"Threshold...", "Default B&W");
        IJ.run(imp,"Analyze Particles...", "size=0-infinity pixel summarize add");   //change range for cell size filtering

        ij.measure.ResultsTable resTab = Analyzer.getResultsTable();
        int resCount = resTab.getCounter();
        ArrayList xTab = new ArrayList();
        ArrayList yTab = new ArrayList();
        double[] x = new double[resCount];
        double[] y = new double[resCount];
        Roi[] r = new Roi[resCount];

        for (int i=0; i<resCount; i++){
            double xx = resTab.getValueAsDouble(0, i);
            double yy = resTab.getValueAsDouble(1, i);
            xTab.add(xx);
            yTab.add(yy);
            x[i]=xx;
            y[i]=yy;
            r[i]= new Roi(xx,yy,10,10);
            imp.setRoi(new Roi(xx,yy,10,10));
            IJ.run("Draw");
        }
        //imp.setRoi(r);
        System.out.println(xTab);
        System.out.println(yTab);
        imp.updateAndRepaintWindow();
      return new ArrayList[]{xTab, yTab};
    }

    //#################################  Method for Saving Image ###############################################
     public void snapAndSaveImage() {

        app_.enableLiveMode(true);

        //=  IJ.openImage(path.concat("Resources/img.tif")); ;
//         JFileChooser fileChooser = new JFileChooser();
//         fileChooser.setDialogTitle("Specify a file to save");
//         File fileToSave = null;
//         int userSelection = fileChooser.showSaveDialog(this);
//         if (userSelection == JFileChooser.APPROVE_OPTION) {
//              fileToSave = fileChooser.getSelectedFile();
//             System.out.println("Save as file: " + fileToSave.getAbsolutePath());
//         }

         //write image
         try{

             PositionList list =  app_.getPositionList();


//             MultiStagePosition mps = new MultiStagePosition();
//             for(int i = 0; i < mps.size(); ++i) {
//                 try {
//                     StagePosition sp = mps.get(i);
////                     if (sp.numAxes == 1) {
////                         System.out.println("name: " + sp.stageName + " posx"+ sp.x);
////                         core_.setPosition(sp.stageName, sp.x);
////                     } else if (sp.numAxes == 2) {
//                         System.out.println("Size: " + mps.size());
//                         System.out.println("name: " + sp.stageName + " posx"+ sp.x + " posy"+ sp.y);
//                         core_.setXYPosition(sp.stageName, sp.x, sp.y);
//                   //  }
//
//                     core_.waitForDevice(sp.stageName);
//                 } catch (Exception var4) {
//                     throw new Exception("XY stage error");
//                 }
//             }

             //core_.setOriginX();
            double xoff = 41150;
            double yoff = -43735;

         //   double defXoff = 1wellx - xStagepos;
          //   double defyoff = 1welly - yStagepos ;

            double xpos = 12.18 * 1000;
            double ypos = 8.74 * 1000;

<<<<<<< HEAD
            core_.setXYPosition(xpos+xoff ,ypos+yoff);

        //     core_.setRelativeXYPosition(xpos+xoff, ypos+yoff);
=======
             core_.setXYPosition(xpos + xoff ,ypos + yoff);

          //   core_.setRelativeXYPosition(xpos+xoff, ypos+yoff);
>>>>>>> 65f0e7f695660d3a995168071e3ada99efe11a58

          //   core_.setXYPosition(55061.30082047731,-37577.600559949875);
          System.out.println("pos: " +core_.getXYStagePosition());






//             ImagePlus iPlus = IJ.getImage();
//             iPlus.show();
//
//
//             String path = fileDialog_.ChooseDirectoryDialog();
//             if(path != null) {
//                 IJ.saveAs(iPlus, ".tif", path +core_.getCurrentConfig(core_.getChannelGroup()));
//             }


            // app_.snapSingleImage();
             // core_.snapImage();
//             TaggedImage tmp = core_.getTaggedImage();
//             app_.openAcquisition(fileToSave.getAbsolutePath(),fileToSave.getAbsolutePath(), 2,1,1,3,true,false );
//
//             System.out.println(app_.getPositionList().getNumberOfPositions());
//             iPlus = IJ.getImage();
//             if (iPlus.getStackSize() <= 1) {
//                 IJ.save(iPlus, fileToSave.getAbsolutePath() + ".tif");
//             }else{
//                 for (int i=0; i< iPlus.getStackSize(); i++){
//                     IJ.save(iPlus, fileToSave.getAbsolutePath() + ".ome.tif");
//                 }
//             }
//             System.out.println(iPlus.getStackSize());
         } catch (Exception e) {
             e.printStackTrace();
         }

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
