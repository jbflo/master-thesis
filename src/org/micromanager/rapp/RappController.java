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


public class RappController extends  MMFrame implements OnStateListener {


    private Boolean disposing_ = false;

    /**
     * Constructor. Creates the main window for the Projector plugin.
     */
    private RappController(CMMCore core, ScriptInterface app) {
//        initComponents();
//        app_ = app;
//        core_ = app.getMMCore();
//        String slm = core_.getSLMDevice();
//        String galvo = core_.getGalvoDevice();
//
//        if (slm.length() > 0) {
//            dev_ = new SLM(core_, 20);
//        } else if (galvo.length() > 0) {
//            dev_ = new Galvo(core_);
//        } else {
//            dev_ = null;
//        }

//        loadMapping();
//        pointAndShootMouseListener = createPointAndShootMouseListenerInstance();
//
//        Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
//            @Override
//            public void eventDispatched(AWTEvent e) {
//                enablePointAndShootMode(pointAndShooteModeOn_.get());
//            }
//        }, AWTEvent.WINDOW_EVENT_MASK);


//        commitSpinnerOnValidEdit(pointAndShootIntervalSpinner);
//        commitSpinnerOnValidEdit(startFrameSpinner);
//        commitSpinnerOnValidEdit(repeatEveryFrameSpinner);
//        commitSpinnerOnValidEdit(repeatEveryIntervalSpinner);
//        commitSpinnerOnValidEdit(roiLoopSpinner);
//        pointAndShootIntervalSpinner.setValue(dev_.getExposure() / 1000);
//        sequencingButton.setVisible(MosaicSequencingFrame.getMosaicDevices(core).size() > 0);
//
//        app_.addMMListener(new MMListenerAdapter() {
//            @Override
//            public void slmExposureChanged(String deviceName, double exposure) {
//                if (deviceName.equals(dev_.getName())) {
//                    pointAndShootIntervalSpinner.setValue(exposure);
//                }
//            }
//        });

       // this.loadAndRestorePosition(500, 300);
       // updateROISettings();
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
