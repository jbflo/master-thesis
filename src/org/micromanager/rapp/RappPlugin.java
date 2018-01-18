///////////////////////////////////////////////////////////////////////////////
//FILE:          RappPlugin.java
//PROJECT:       Micro-Manager Laser Automated Plugin
//SUBSYSTEM:     Projector plugin
//-----------------------------------------------------------------------------
//AUTHOR:        FLorial, Source : ProjectorPlugin, Arthur Edelstein
//COPYRIGHT:     ZMBH, University of Heidelberg, 2017-2018
//LICENSE:       This file is distributed under the
/////////////////////////////////////////////////////////////////////////////////


package org.micromanager.rapp;


///////////////     Java /  Java-swim Import class And Plugin       /////////////

import javax.swing.*;
import java.util.concurrent.LinkedBlockingQueue;
/////////////////  Micro-Manager Package ////////////////////////

import mmcorej.CMMCore;
import mmcorej.TaggedImage;
import org.micromanager.MMStudio;
import org.micromanager.acquisition.AcquisitionEngine;
import org.micromanager.acquisition.AcquisitionWrapperEngine;
import org.micromanager.api.MMPlugin;
import org.micromanager.api.ScriptInterface;
import org.micromanager.utils.MMScriptException;
import org.micromanager.utils.ReportingUtils;


import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;


public class RappPlugin implements MMPlugin  {

    private RappGui form_;
    public static final String menuName = "Rapp control";
    public static final String tooltipDescription = "Automated cell recognition for killing and sorting ";
    //private MMStudioMainFrame app_;

    static ScriptInterface app_;
    private CMMCore core_;
    private MMStudio mgui_;
    private MMStudio.DisplayImageRoutine displayImageRoutine_;
    private RappGui gui_;
    private AcquisitionEngine acq_;
    private final String ACQ_NAME = "Rapp control";
    private int multiChannelCameraNrCh_;

    public static AcquisitionWrapperEngine getAcquisitionWrapperEngine() {
        AcquisitionWrapperEngine engineWrapper = (AcquisitionWrapperEngine) MMStudio.getInstance().getAcquisitionEngine();
        return engineWrapper;
    }

    public static CMMCore getMMcore(){
        CMMCore core1_  = getMMcore();
        return  core1_;
    }


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
        mgui_ = (MMStudio)  app_;
        core_ = app_.getMMCore();

        displayImageRoutine_ = new MMStudio.DisplayImageRoutine() {
            @Override
            public void show(final TaggedImage ti) {
                try {
                    mgui_.addImage(ACQ_NAME, ti, true, true);
                } catch (MMScriptException e) {
                    ReportingUtils.logError(e);
                }
            }
        };

    }


    @Override // MM
    public void show() {
        /// (Try) calling the Interface Class on Package TestGui
        try {
            // select Look and Feel
            UIManager.setLookAndFeel("com.jtattoo.plaf.smart.SmartLookAndFeel");

            if (mgui_.acquisitionExists(ACQ_NAME))
                mgui_.closeAcquisition(ACQ_NAME);

            if (core_.getCameraDevice().length()==0 && core_.getGalvoDevice().length()==0 ) {
                ReportingUtils.showMessage("Please load a Camera Devices " +
                        "or a Galvo-based phototargeting device " +
                        "before using the Projector plugin.");
                return;
            }

            LinkedBlockingQueue<TaggedImage> imageQueue_ =
                    new LinkedBlockingQueue<TaggedImage>();
            multiChannelCameraNrCh_ = (int) core_.getNumberOfCameraChannels();

            mgui_.openAcquisition(ACQ_NAME, "tmp", core_.getRemainingImageCount(),
                    multiChannelCameraNrCh_, 1, true);

            String camera = core_.getCameraDevice();
            long width = core_.getImageWidth();
            long height = core_.getImageHeight();
            long depth = core_.getBytesPerPixel();
            long bitDepth = core_.getImageBitDepth();

            mgui_.initializeAcquisition(ACQ_NAME, (int) width,(int) height, (int) depth, (int)bitDepth);
            mgui_.runDisplayThread(imageQueue_, displayImageRoutine_);

        }
        catch (Exception ex) {
            ex.printStackTrace();
        } // end of Try Catch

        //new RappGui(core_, app_);
        form_ = RappGui.showAppInterface(core_, app_);
        //gui_.getContentPane().add( );
    }


    @Override
    public String getDescription() {

        return null;
    }

    @Override
    public String getInfo() {
        return "Gui to Control Laser Machine";
    }

    @Override
    public String getVersion() {
        return "1.4";
    }

    @Override
    public String getCopyright() {
        return "Heidelberg University / Knop Lab, 2018";
    }


}
