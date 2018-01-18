package org.micromanager.rapp;

import mmcorej.CMMCore;
import org.micromanager.api.ScriptInterface;
import org.micromanager.utils.GUIUtils;
import org.micromanager.utils.MMFrame;
import org.micromanager.utils.MMListenerAdapter;

import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
import org.micromanager.utils.ImageUtils;
import org.micromanager.utils.JavaUtils;
import org.micromanager.utils.MMFrame;
import org.micromanager.utils.MMListenerAdapter;
import org.micromanager.utils.MathFunctions;
import org.micromanager.utils.ReportingUtils;


/**
 * The main Class as a Controller for the Rapp plugin. Contains logic for calibration,
 * and control for Camera and Galvos (Rapp UG-42).
 */

public class RappController extends  MMFrame implements OnStateListener {


    private final RappDevice dev_;
    private final MouseListener pointAndShootMouseListener;
    private final AtomicBoolean pointAndShooteModeOn_ = new AtomicBoolean(false);
    private final CMMCore core_;
    private final ScriptInterface app_;
    private Map<Polygon, AffineTransform> mapping_ = null;
    private String mappingNode_ = null;
    private String targetingChannel_;
    private String targetingShutter_;
    private Boolean disposing_ = false;


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
        long newExposure = (long) intervalUs;
        if (previousExposure != newExposure) {
            dev_.setExposure(newExposure);
        }
    }

    /**
     * Turns the projection device on or off.
     * @param onState on=true
     */
    public void setOnState(boolean onState) {
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
        if (x >= dev_.getXMinimum() && x < (dev_.getXRange() + dev_.getXMinimum())
                && y >= dev_.getYMinimum() && y < (dev_.getYRange() + dev_.getYMinimum())) {
            dev_.displaySpot(x, y);
        }
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
            mapping_ = (Map<Polygon, AffineTransform>) JavaUtils.getObjectFromPrefs(
                    prefs,
                    dev_.getName(),
                    new HashMap<Polygon, AffineTransform>());
        }
        return mapping_;
    }



    // Returns true if a particular image is mirrored.
    private static boolean isImageMirrored(ImagePlus imgp) {
        try {
            String mirrorString = VirtualAcquisitionDisplay.getDisplay(imgp)
                    .getCurrentMetadata().getString("ImageFlipper-Mirror");
            return (mirrorString.contentEquals("On"));
        } catch (JSONException e) {
            return false;
        } catch (NullPointerException npe) {
            return false;
        }
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


    // ## Point and shoot

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
                    final Point2D.Double devP = transformAndMirrorPoint(loadMapping(), canvas.getImage(),
                            new Point2D.Double(pOffscreen.x, pOffscreen.y));
                    final Configuration originalConfig = prepareChannel();
                    final boolean originalShutterState = prepareShutter();
                    makeRunnableAsync(
                            new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        if (devP != null) {
                                            displaySpot(devP.x, devP.y);
                                        }
                                        returnShutter(originalShutterState);
                                        returnChannel(originalConfig);
                                    } catch (Exception e) {
                                        ReportingUtils.showError(e);
                                    }
                                }
                            }).run();

                }
            }
        };
    }

    // Turn on/off point and shoot mode.
    public void enablePointAndShootMode(boolean on) {
        if (on && (mapping_ == null)) {
            ReportingUtils.showError("Please calibrate the phototargeting device first, using the Setup tab.");
            throw new RuntimeException("Please calibrate the phototargeting device first, using the Setup tab.");
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


    private RappController(CMMCore core, ScriptInterface app) {
        app_ = app;
        core_ = app.getMMCore();
        String slm = core_.getSLMDevice();
        String galvo = core_.getGalvoDevice();

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

//        isSLM_ = dev_ instanceof SLM;
//        // Only an SLM (not a galvo) has pixels.
//        allPixelsButton.setVisible(isSLM_);
//        // No point in looping ROIs on an SLM.
//        roiLoopSpinner.setVisible(!isSLM_);
//        roiLoopLabel.setVisible(!isSLM_);
//        roiLoopTimesLabel.setVisible(!isSLM_);
//        pointAndShootOffButton.setSelected(true);
//        populateChannelComboBox(Preferences.userNodeForPackage(this.getClass()).get("channel", ""));
//        populateShutterComboBox(Preferences.userNodeForPackage(this.getClass()).get("shutter", ""));
//        this.addWindowFocusListener(new WindowAdapter() {
//            @Override
//            public void windowGainedFocus(WindowEvent e) {
//                if (!disposing_)
//                {
//                    populateChannelComboBox(null);
//                    populateShutterComboBox(null);
//                }
//            }
//        });

    }



    public RappDevice getDevice() {
        return dev_;
    }
    @Override
    public void dispose()
    {
        disposing_ = true;
        super.dispose();
    }

    @Override
    public void stateChanged(boolean onState) {

    }
}
