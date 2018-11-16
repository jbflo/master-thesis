///////////////////////////////////////////////////////////////////////////////
//FILE:          RappPlugin.java
//PROJECT:       Micro-Manager Laser Automated Plugin
//SUBSYSTEM:     RAPP plugin
//-----------------------------------------------------------------------------
//AUTHOR:        FLorial,
//SOURCE :       ProjectorPlugin, Arthur Edelstein,
//COPYRIGHT:     ZMBH, University of Heidelberg, 2017-2018
//LICENSE:       This file is distributed under the
/////////////////////////////////////////////////////////////////////////////////


package org.micromanager.rapp;


///////////////     Java /  Java-swim Import class And Plugin       /////////////

import ij.IJ;
import ij.plugin.frame.RoiManager;
import mmcorej.CMMCore;
import org.micromanager.MMStudio;
import org.micromanager.api.MMListenerInterface;
import org.micromanager.api.MMPlugin;
import org.micromanager.api.ScriptInterface;
import org.micromanager.internalinterfaces.LiveModeListener;
import org.micromanager.utils.GUIUtils;
import org.micromanager.utils.ReportingUtils;
import javax.swing.plaf.metal.MetalToggleButtonUI;
import java.awt.*;
import java.awt.event.ItemEvent;

/////////////////  Micro-Manager Package ////////////////////////


public class RappPlugin implements MMPlugin, MMListenerInterface, LiveModeListener {

    private RappGui form_;
    public static final String menuName = "Rapp control";
    public static final String tooltipDescription = "Automated cell recognition for killing and sorting ";
    private static ScriptInterface app_;
    private static CMMCore core_;
    public static MMStudio studio_;



    @Override // MM
    public void dispose() {
        if (form_ != null) {
            form_.dispose();
        }
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override  // MM
    public void setApp(ScriptInterface app) {
        app_ = app;
        studio_ = (MMStudio) app_;
        core_ = app_.getMMCore();
    }

    public static CMMCore getMMcore(){
        return  core_;
    }

    public static ScriptInterface getScripI(){
        return app_;
    }

    @Override // MM
    public void show() {
        /// (Try) Make sure there is a Camera or a Galvo Device connected
        try {
            if (core_.getCameraDevice().length()==0 && core_.getGalvoDevice().length()==0 ) {
                ReportingUtils.showMessage("Please load a Camera Devices " +
                        "And a Galvo-based phototargeting device : Rapp UGA-42  " +
                        "before using the RappPlugin plugin.");
                return;
            }
        } catch (Exception e) {
            // If Nimbus is not available, you can set the GUI to another look and feel.
        }

        try {
            form_ = RappGui.showAppInterface(core_, app_);
            app_.addMMBackgroundListener(form_);
            app_.addMMListener(form_);
        } catch (Exception e) {
            ReportingUtils.showMessage("Please Try Again! The Gui Couldn't load properly");
            e.printStackTrace();
        }
    }

    // #Show the ImageJ Roi Manager and return a reference to it.
    public static RoiManager showRoiManager() {
        IJ.run("ROI Manager...");
        final RoiManager roiManager = RoiManager.getInstance();
        GUIUtils.recallPosition(roiManager);
        // "Get the "Show All" checkbox and make sure it is checked.
        Checkbox checkbox = (Checkbox) ((Panel) roiManager.getComponent(1)).getComponent(9);
        checkbox.setState(true);
        // Simulated click of the "Show All" checkbox to force ImageJ
        // to show all of the ROIs.
        roiManager.itemStateChanged(new ItemEvent(checkbox, 0, null, ItemEvent.SELECTED));
        return roiManager;
    }

    @Override
    public String getDescription() {

        return null;
    }

    @Override
    public String getInfo() {
        return "Interface for controlling microscope and laser system : For Cell Killing Project";
    }

    @Override
    public String getVersion() {
        return "1.4";
    }

    @Override
    public String getCopyright() {
        return "ZMBH, Heidelberg University / Knop Lab, 2018";
    }


    @Override
    public void liveModeEnabled(final boolean b) {
        RappGui.getInstance().LiveMode_btn.setSelected(b);
        RappGui.getInstance().LiveMode_btn.setText(  b ? "Stop Live View" : "Start Live View" );
        RappGui.getInstance().LiveMode_btn.setBackground(b? Color.decode("#d35400") :Color.decode("#d35400") );
        RappGui.getInstance().LiveMode_btn.setUI(new MetalToggleButtonUI() {
            @Override
            protected Color getSelectColor() {
                return (b? Color.decode("#d35400") :Color.decode("#d35400") );
            }
        });


    }

    @Override
    public void propertiesChangedAlert() {
        System.out.println("liveModeEnabled");

        if (! MMStudio.getInstance().isLiveModeOn()) {
            System.out.println("PropertyChangeEvent");
//            RappGui.getInstance().LiveMode_btn.setSelected(false);
//            RappGui.getInstance().LiveMode_btn.setUI(new MetalToggleButtonUI() {
//                @Override
//                protected Color getSelectColor() {
//                    return Color.decode("#d35400");
//                }
//            });
        }  else  System.out.println("000000");
    }

    @Override
    public void propertyChangedAlert(String s, String s1, String s2) {

    }

    @Override
    public void configGroupChangedAlert(String s, String s1) {

    }

    @Override
    public void systemConfigurationLoaded() {

    }

    @Override
    public void pixelSizeChangedAlert(double v) {

    }

    @Override
    public void stagePositionChangedAlert(String s, double v) {

    }

    @Override
    public void xyStagePositionChanged(String s, double v, double v1) {

    }

    @Override
    public void exposureChanged(String s, double v) {

    }

    @Override
    public void slmExposureChanged(String s, double v) {

    }
}
