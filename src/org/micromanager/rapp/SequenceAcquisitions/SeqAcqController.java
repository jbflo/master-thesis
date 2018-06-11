package org.micromanager.rapp.SequenceAcquisitions;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.Duplicator;
import mmcorej.*;
import org.json.JSONObject;
import org.micromanager.MMOptions;
import org.micromanager.api.DataProcessor;
import org.micromanager.api.ImageCache;
import org.micromanager.api.PositionList;
import org.micromanager.api.ScriptInterface;
import org.micromanager.internalinterfaces.AcqSettingsListener;
import org.micromanager.rapp.RappController;
import org.micromanager.rapp.RappGui;
import org.micromanager.rapp.RappPlugin;
import org.micromanager.utils.*;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class SeqAcqController implements AcquisitionEngine {
    private CMMCore core_;
    private ScriptInterface app_;
    private PositionList posList_;
    private ArrayList<AcqSettingsListener> settingsListeners_ = new ArrayList();
    private ArrayList<ChannelSpec> channels_ = new ArrayList();
    private  static RappController rappController_ref;
    private String comment_;
    private boolean saveFiles_;
    private int acqOrderMode_;
    private boolean useAutoFocus_;
    private String rootName_;
    private String dirName_;
    private boolean useChannels_;
    private boolean useSegmentation_;
    private boolean killCell_;
    private boolean useMultiPosition_;
    private boolean keepShutterOpenForChannels_;
    public static AtomicBoolean stopAcqRequested_ = new AtomicBoolean(false);
    private AtomicBoolean isRunning_ = new AtomicBoolean(false);



    public SeqAcqController (){
        this.core_ = RappPlugin.getMMcore();
        this.app_ = RappPlugin.getScripI();
        posList_ = new PositionList();
        try {
            rappController_ref =  new RappController(core_, app_);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String acquire() {
       // return this.runAcquisition(this.getSequenceSettings(), this.acqManager_);
        return this.runSeqAcquisition(this.getSequenceSettings());
    }

    public void addSettingsListener(AcqSettingsListener listener) {
        this.settingsListeners_.add(listener);
    }

    public void removeSettingsListener(AcqSettingsListener listener) {
        this.settingsListeners_.remove(listener);
    }

    public void settingsChanged() {
        Iterator i$ = this.settingsListeners_.iterator();
        while(i$.hasNext()) {
            AcqSettingsListener listener = (AcqSettingsListener)i$.next();
            listener.settingsChanged();
        }
    }


    protected boolean imagesSaving(){

        if (saveFiles_) {
            File root = new File(rootName_);
            if (!root.canWrite()) {
                int result = JOptionPane.showConfirmDialog(null, "The specified root directory\n" + root.getAbsolutePath() + "\ndoes not exist. Create it?", "Directory not found.", 0);
                if (result != 0) {
                    ReportingUtils.showMessage("Acquire Image won't save");
                    return false;
                }
                root.mkdirs();
                if (!root.canWrite()) {
                    ReportingUtils.showError("Unable to save data to selected location: check that location exists.\nAcquisition canceled.");
                    return false;
                }
            } else if (!enoughDiskSpace()) {
                ReportingUtils.showError("Not enough space on disk to save the requested image set; acquisition canceled.");
                return false;
            }
        }
        return saveFiles_;
    }

    protected String runSeqAcquisition(SequenceSettings acquisitionSettings) {
        //app_.enableLiveMode(false);
        ArrayList<ChannelSpec> channels =  acquisitionSettings.channels;
        String chanelGroup_ = acquisitionSettings.channelGroup;

        try {
            core_.waitForDevice(core_.getCameraDevice());
            Thread.sleep(100); // wait and start acquisition
        } catch (Exception e) {
            e.printStackTrace();
        }
        //for (ChannelSpec presetConfig : channels){
        //     channelsConf.add(presetConfig.config.toString());
        //    }

        saveFiles_ = this.imagesSaving();
        if (!isRunning_.get()) {
            stopAcqRequested_.set(false);
            SeqAcqGui.progressBar.setIndeterminate(true);
            Thread th = new Thread("Sequence Acquisition thread") {
                @Override
                public void run() {
                    try {
                        SeqAcqGui.acquireButton_.setEnabled(false);
                        isRunning_.set(true);
                        ImagePlus iPlus ;
                        int progress = 0;
                        SeqAcqGui.taskOutput.setText("");
                        RappGui.getInstance().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        for (ChannelSpec presetConfig : channels){
                            if (stopAcqRequested_.get()) {
                                //ReportingUtils.showMessage("Acquisition Stop.");
                                break;
                            }

                            //app_.enableLiveMode(false); // Make sure the Live Mode is off

                            // Then Change The Chanel Config (Preset )
                            core_.setConfig(chanelGroup_, presetConfig.config);
                            // Set the Chanel Exposure Time
                            app_.setChannelExposureTime(chanelGroup_, presetConfig.config, presetConfig.exposure);
                            // Take a Photo for each fix chanel
                            core_.waitForConfig(chanelGroup_, presetConfig.config.toString());
                            Thread.sleep(1000 + (long)presetConfig.exposure);

                            iPlus = IJ.getImage();
                         //   Thread.sleep(1000);

                            if (channels.subList(0, channels.size()).get(0).config.equals(presetConfig.config)){
                                System.out.println( "You Are :" + presetConfig.config);
                            }

                            // if the Images was not save , we do the segmentation for the image in Memory
                            if(!saveFiles_  && acquisitionSettings.useSegmentation){
                                IJ.open();
                                ArrayList[] ll =  rappController_ref.brightFieldSegmenter(iPlus, presetConfig.config.toString(), "", presetConfig.KillCell, saveFiles_);
                                if (presetConfig.KillCell) {
                                    app_.enableLiveMode(true); //  Open the live mode before shooting
                                    rappController_ref.shootFromSegmentationListPoint(ll, (long) presetConfig.laser_exposure);
                                }
                            }

                            if (saveFiles_ && !SeqAcqGui.saveMultiTiff_) {
                                // The acquires Images are saving as separate Image.

                                IJ.save(iPlus, rootName_ +  "\\"+ dirName_+ "_"+ presetConfig.config.toLowerCase() + ".tif");
                                if(acquisitionSettings.useSegmentation){
                                    String path_seq = rootName_ +  "\\"+ dirName_+ "_"+ presetConfig.config.toLowerCase();
                                    ImagePlus image_ =   IJ.openImage(rootName_ +  "\\"+ dirName_+ "_"+ presetConfig.config.toLowerCase() + ".tif");

                                    ArrayList[] ll =  rappController_ref.brightFieldSegmenter(image_, presetConfig.config.toString(), path_seq, presetConfig.KillCell, saveFiles_);
                                    System.out.println(presetConfig.KillCell);
                                    if (presetConfig.KillCell) {
                                        app_.enableLiveMode(true); //  Open the live mode before shooting
                                        rappController_ref.shootFromSegmentationListPoint(ll, (long) presetConfig.laser_exposure);
                                    }
                                }

                            } else if(saveFiles_ && SeqAcqGui.saveMultiTiff_){
                                // The acquires Images are saving as a stack Image.
                                IJ.save(iPlus, rootName_ +  "\\"+ dirName_+ "_"+ presetConfig.config.toLowerCase() + ".tif");
                                IJ.open(rootName_ +  "\\"+ dirName_+ "_"+ presetConfig.config.toLowerCase() + ".tif");
                                String path_seq = rootName_ +  "\\"+ dirName_+ "_"+ presetConfig.config.toLowerCase();
                                ImagePlus image_ =   IJ.openImage(rootName_ +  "\\"+ dirName_+ "_"+ presetConfig.config.toLowerCase() + ".tif");
                                if(acquisitionSettings.useSegmentation){
                                    ArrayList[] ll =  rappController_ref.brightFieldSegmenter(image_, presetConfig.config.toString(), path_seq, presetConfig.KillCell, saveFiles_);
                                    if (presetConfig.KillCell) {
                                        app_.enableLiveMode(true); //  Open the live mode before shooting
                                        rappController_ref.shootFromSegmentationListPoint(ll, (long) presetConfig.laser_exposure);
                                    }
                                }
                            }


                            progress  +=  100 / channels.size();
                            SeqAcqGui.progressBar.setValue(progress);
                            SeqAcqGui.taskOutput.append(String.format("Completed %d%% of Sequence Task.\n", progress));
                        }

                        if(saveFiles_ && SeqAcqGui.saveMultiTiff_){
                            IJ.run("Images to Stack", "name=Stack title=[] use");
                            IJ.saveAs("Tiff", rootName_ +  "\\"+ dirName_+ "_"+ "Stack" + ".tif");
                        }
                        JOptionPane.showMessageDialog(IJ.getImage().getWindow(), "Sequence Acquisition "
                                + (!stopAcqRequested_.get() ? "finished." : "canceled."));
                        System.out.println(core_.getCurrentConfig("Channel"));
                    } catch (Exception e) {
                        ReportingUtils.showError(e);
                        e.printStackTrace();
                        ReportingUtils.showMessage("Acquisition Stop Due to some Error," +
                                " Please check your input data. "+
                                " We are Sorry about that"
                                );
                    } finally {
                        SwingUtilities.invokeLater(() -> SeqAcqGui.progressBar.setIndeterminate(false));
                        RappGui.getInstance().setCursor(null); // turn off the wait cursor
                        SeqAcqGui.acquireButton_.setEnabled(true); // Activate the Button again
                        isRunning_.set(false);
                        stopAcqRequested_.set(false);
                    }
                }
            };
            th.start();
        }
        return null;
    }

    private int getNumChannels() {
        int numChannels = 0;
        if (this.useChannels_) {
            Iterator i$ = this.channels_.iterator();

            while(i$.hasNext()) {
                ChannelSpec channel = (ChannelSpec)i$.next();
                if (channel.useChannel) {
                    ++numChannels;
                }
            }
        } else {
            numChannels = 1;
        }

        return numChannels;
    }

    private int getNumChannelsToKill() {
        int numKillChannels = 0;
        if (this.useChannels_ && this.killCell_) {
            Iterator i$ = this.channels_.iterator();

            while(i$.hasNext()) {
                ChannelSpec channel = (ChannelSpec)i$.next();
                if (channel.useChannel && channel.KillCell) {
                    ++numKillChannels;
                }
            }
        } else {
            numKillChannels = 1;
        }
        return numKillChannels;
    }


    private int getNumPositions() {
        int numPositions = Math.max(1, this.posList_.getNumberOfPositions());
        if (!this.useMultiPosition_) {
            numPositions = 1;
        }

        return numPositions;
    }


    private int getTotalImages() {
        int totalImages =  this.getNumChannels() * this.getNumPositions();
        return totalImages;
    }

    private long getTotalMB() {
        CMMCore core = this.app_.getMMCore();
        long totalMB = core.getImageWidth() * core.getImageHeight() * core.getBytesPerPixel() * (long)this.getTotalImages() / 1048576L;
        return totalMB;
    }

    private void updateChannelCameras() {
        ChannelSpec channel;
        for(Iterator i$ = this.channels_.iterator(); i$.hasNext(); channel.camera = this.getSource(channel)) {
            channel = (ChannelSpec)i$.next();
        }

    }



    private String getSource(ChannelSpec channel) {
        try {
            Configuration state = this.core_.getConfigState(this.core_.getChannelGroup(), channel.config);
            return state.isPropertyIncluded("Core", "Camera") ? state.getSetting("Core", "Camera").getPropertyValue() : "";
        } catch (Exception var3) {
            ReportingUtils.logError(var3);
            return "";
        }
    }

    protected boolean enoughDiskSpace() {
        File root = new File(this.rootName_);

        do {
            if (root.exists()) {
                long usableMB = root.getUsableSpace() / 1048576L;
                return 1.25D * (double)this.getTotalMB() < (double)usableMB;
            }

            root = root.getParentFile();
        } while(root != null);

        return false;
    }

    public SequenceSettings getSequenceSettings() {
        SequenceSettings acquisitionSettings = new SequenceSettings();
        this.updateChannelCameras();
//        if (this.useFrames_) {
//            if (this.useCustomIntervals_) {
//                acquisitionSettings.customIntervalsMs = this.customTimeIntervalsMs_;
//                acquisitionSettings.numFrames = acquisitionSettings.customIntervalsMs.size();
//            } else {
//                acquisitionSettings.numFrames = this.numFrames_;
//                acquisitionSettings.intervalMs = this.interval_;
//            }
//        } else {
//            acquisitionSettings.numFrames = 0;
//        }

//        if (this.useSlices_) {
//            double start = this.sliceZBottomUm_;
//            double stop = this.sliceZTopUm_;
//            double step = Math.abs(this.sliceZStepUm_);
//            if (step == 0.0D) {
//                throw new UnsupportedOperationException("zero Z step size");
//            }
//
//            int count = this.getNumSlices();
//            if (start > stop) {
//                step = -step;
//            }
//
//            for(int i = 0; i < count; ++i) {
//                acquisitionSettings.slices.add(start + (double)i * step);
//            }
//        }



        if (this.useChannels_) {
            Iterator i$ = this.channels_.iterator();

            while(i$.hasNext()) {
                ChannelSpec channel = (ChannelSpec)i$.next();
                if (channel.useChannel) {
                    acquisitionSettings.channels.add(channel);
                }
            }

            acquisitionSettings.channelGroup = this.core_.getChannelGroup();
        }

       // acquisitionSettings.timeFirst = this.acqOrderMode_ == 3 || this.acqOrderMode_ == 2;
      //  acquisitionSettings.slicesFirst = this.acqOrderMode_ == 3 || this.acqOrderMode_ == 1;
        acquisitionSettings.useAutofocus = this.useAutoFocus_;
    //    acquisitionSettings.skipAutofocusCount = this.afSkipInterval_;
        acquisitionSettings.keepShutterOpenChannels = this.keepShutterOpenForChannels_;
     //   acquisitionSettings.keepShutterOpenSlices = this.keepShutterOpenForStack_;
        acquisitionSettings.save = this.saveFiles_;
        if (this.saveFiles_) {
            acquisitionSettings.root = this.rootName_;
            acquisitionSettings.prefix = this.dirName_;
        }

        acquisitionSettings.comment = this.comment_;
        acquisitionSettings.usePositionList = this.useMultiPosition_;
        acquisitionSettings.useSegmentation = this.useSegmentation_;
        return acquisitionSettings;
    }

    public void setSequenceSettings(SequenceSettings ss) {
        this.updateChannelCameras();
//        this.useFrames_ = true;
//        if (this.useCustomIntervals_) {
//            this.customTimeIntervalsMs_ = ss.customIntervalsMs;
//            this.numFrames_ = ss.customIntervalsMs.size();
//        } else {
//            this.numFrames_ = ss.numFrames;
//            this.interval_ = ss.intervalMs;
//        }

//        this.useSegmentation_ = true;

//        this.useSlices_ = true;
//        if (ss.slices.size() == 0) {
//            this.useSlices_ = false;
//        } else if (ss.slices.size() == 1) {
//            this.sliceZBottomUm_ = (Double)ss.slices.get(0);
//            this.sliceZTopUm_ = this.sliceZBottomUm_;
//            this.sliceZStepUm_ = 0.0D;
//        } else {
//            this.sliceZBottomUm_ = (Double)ss.slices.get(0);
//            this.sliceZTopUm_ = (Double)ss.slices.get(ss.slices.size() - 1);
//            this.sliceZStepUm_ = (Double)ss.slices.get(1) - (Double)ss.slices.get(0);
//            if (this.sliceZBottomUm_ > this.sliceZBottomUm_) {
//                this.sliceZStepUm_ = -this.sliceZStepUm_;
//            }
//        }

      //  this.absoluteZ_ = !ss.relativeZSlice;
        this.useChannels_ = ss.channels.size() > 0;

        this.channels_ = ss.channels;
//        if (ss.timeFirst && ss.slicesFirst) {
//            this.acqOrderMode_ = 3;
//        }
//
//        if (ss.timeFirst && !ss.slicesFirst) {
//            this.acqOrderMode_ = 2;
//        }

//        if (!ss.timeFirst && ss.slicesFirst) {
//            this.acqOrderMode_ = 1;
//        }
//
//        if (!ss.timeFirst && !ss.slicesFirst) {
//            this.acqOrderMode_ = 0;
//        }

        this.useAutoFocus_ = ss.useAutofocus;
       // this.afSkipInterval_ = ss.skipAutofocusCount;
        this.keepShutterOpenForChannels_ = ss.keepShutterOpenChannels;
      //  this.keepShutterOpenForStack_ = ss.keepShutterOpenSlices;
        this.saveFiles_ = ss.save;
        this.rootName_ = ss.root;
        this.dirName_ = ss.prefix;
        this.comment_ = ss.comment;
        this.useMultiPosition_ = ss.usePositionList;
        this.useSegmentation_ = ss.useSegmentation;
    }


    @Override
    public void setCore(CMMCore core_, AutofocusManager afMgr) {

    }

    @Override
    public void setPositionList(PositionList posList) {

    }

    @Override
    public void setParentGUI(ScriptInterface parent) {

    }



    @Override
    public void setUpdateLiveWindow(boolean b) {

    }

    @Override
    public void stop(boolean interrupted) {

    }

    @Override
    public boolean abortRequest() {
        if (isRunning_.get()){
            isRunning_.set(false);
            stopAcqRequested_.set(true);
        }

        return false;
    }

    @Override
    public void setFinished() {

    }

    @Override
    public boolean isAcquisitionRunning() {
        return false;
    }

    @Override
    public boolean isMultiFieldRunning() {
        return false;
    }


    @Override
    public void enableCustomTimeIntervals(boolean enable) {
    }

    @Override
    public boolean customTimeIntervalsEnabled() {
        return false;
    }

    @Override
    public void setCustomTimeIntervals(double[] customTimeIntervalsMs) {
    }

    @Override
    public double[] getCustomTimeIntervals() {
        return new double[0];
    }

    @Override
    public void shutdown() {

    }

    @Override
    public void setPause(boolean state) {

    }

    @Override
    public void setChannel(int row, ChannelSpec channel) {
        this.channels_.set(row, channel);
    }

    @Override
    public String getFirstConfigGroup() {
        if (this.core_ == null) {
            return "";
        } else {
            String[] groups = this.getAvailableGroups();
            return groups != null && groups.length >= 1 ? this.getAvailableGroups()[0] : "";
        }
    }

    @Override
    public String[] getChannelConfigs() {
        return this.core_ == null ? new String[0] : this.core_.getAvailableConfigs(this.core_.getChannelGroup()).toArray();
    }


    @Override
    public String getChannelGroup(){
        return this.core_.getChannelGroup();
    }

    @Override
    public boolean setChannelGroup(String group) {
        if (this.groupIsEligibleChannel(group)) {
            try {
                this.core_.setChannelGroup(group);
                return true;
            } catch (Exception var5) {
                try {
                    this.core_.setChannelGroup("");
                } catch (Exception var4) {
                    ReportingUtils.showError(var4);
                }
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public void clear() {
        if (this.channels_ != null) {
            this.channels_.clear();
        }
      //  this.numFrames_ = 0;
    }

    @Override
    public boolean isChannelsSettingEnabled() {
        return this.useChannels_;
    }

    @Override
    public void enableChannelsSetting(boolean enable) {
        this.useChannels_ = enable;
    }

    @Override
    public boolean isKillCellEnabled() {
        return this.killCell_;
    }

    @Override
    public void enableKillCell(boolean selected) {
          this.killCell_ = selected;
    }

    @Override
    public void keepShutterOpenForChannels(boolean open) {
        this.keepShutterOpenForChannels_ = open;
    }

    @Override
    public boolean isShutterOpenForChannels() {
        return this.keepShutterOpenForChannels_;
    }

    @Override
    public void enableMultiPosition(boolean selected) {
        this.useMultiPosition_ = selected;
    }

    @Override
    public boolean isMultiPositionEnabled() {
        return this.useMultiPosition_;
    }

    @Override
    public void enableSegmentation(boolean selected) {
        this.useSegmentation_ = selected;
    }

    @Override
    public boolean isDoSegmentationEnabled() {
        return useSegmentation_;
    }

    @Override
    public ArrayList<ChannelSpec> getChannels() {
        return this.channels_;
    }

    @Override
    public void setChannels(ArrayList<ChannelSpec> channels) {
        this.channels_ = channels;
    }

    @Override
    public String getRootName() {
        return this.rootName_;
    }

    @Override
    public void setRootName(String absolutePath) {
        this.rootName_ = absolutePath;
    }

    @Override
    public void setCameraConfig(String config) {

    }

    @Override
    public void setDirName(String text) {
        this.dirName_ = text;
    }

    @Override
    public void setComment(String text) {
        this.comment_ = text;
        this.settingsChanged();
    }

    @Override
    public boolean addChannel(String config, double exp,  double laserExp, Boolean doSegmentation, Boolean killCell, ContrastSettings con8, ContrastSettings con16,  Color c, boolean use) {
        return this.addChannel(config, exp, laserExp, doSegmentation,killCell,  con8,  c, use);
    }

    @Override
    public boolean addChannel(String config, double exp, double laserExp, Boolean doSegmentation, Boolean killCell, ContrastSettings con, Color c, boolean use) {
        if (this.isConfigAvailable(config)) {
            ChannelSpec channel = new ChannelSpec();
            channel.config = config;
            channel.useChannel = use;
            channel.exposure = exp;
            channel.laser_exposure = laserExp;
           // channel.doSegmentation = doSegmentation;
            channel.KillCell= killCell;
            channel.contrast = con;
            channel.color = c;
            this.channels_.add(channel);
            return true;
        } else {
            ReportingUtils.logError("\"" + config + "\" is not found in the current Channel group.");
            return false;
        }
    }

    @Override
    public boolean addChannel(String config, double exp, double laserExp, Boolean killCell,  ContrastSettings c8, ContrastSettings c16, Color c) {
        return this.addChannel(config, exp, laserExp, true, true, c16,  c, true);
    }
    @Override
    public void setSaveFiles(boolean selected) {
        this.saveFiles_ = selected;
    }
    @Override
    public boolean getSaveFiles() {
        return this.saveFiles_;
    }

    @Override
    public int getDisplayMode() {
        return 0;
    }

    @Override
    public void setDisplayMode(int mode) {

    }

    @Override
    public int getAcqOrderMode(){return this.acqOrderMode_;
    }

    @Override
    public void setAcqOrderMode(int mode) {
        this.acqOrderMode_ = mode;
    }
    @Override
    public void enableAutoFocus(boolean enabled) {
        this.useAutoFocus_ = enabled;
    }

    @Override
    public boolean isAutoFocusEnabled() {
        return this.useAutoFocus_;
    }


    @Override
    public void setSingleWindow(boolean selected) {

    }

    @Override
    public String installAutofocusPlugin(String className) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getVerboseSummary() {
        int numPositions = this.getNumPositions();
        int numChannels = this.getNumChannels();
        int numKillChannels = this.getNumChannelsToKill();
        int totalImages = this.getTotalImages();
        long totalMB = this.getTotalMB();
        double totalDurationSec = 0.0D;

        int hrs = (int)(totalDurationSec / 3600.0D);
        double remainSec = totalDurationSec - (double)(hrs * 3600);
        int mins = (int)(remainSec / 60.0D);
        remainSec -= (double)(mins * 60);
        String txt = "\nNumber of positions: " + numPositions +  "\nNumber of channels: " + numChannels + "\nNumber of Chanel to Kill: " + numKillChannels + "\nTotal images: " + totalImages + "\nTotal memory: " + (totalMB <= 1024L ? totalMB + " MB" : NumberUtils.doubleToDisplayString((double)totalMB / 1024.0D) + " GB") + "\nDuration: " + hrs + "h " + mins + "m " + NumberUtils.doubleToDisplayString(remainSec) + "s";
        if (!this.useMultiPosition_ && !this.useChannels_ && !this.useSegmentation_) {
            return txt;
        } else {
            StringBuffer order = new StringBuffer("\nOrder: ");

             if (this.useMultiPosition_) {
                order.append("Position");
            }

            else if (this.useChannels_) {
                order.append("Channel");
            }
             else if (this.useSegmentation_) {
                 order.append("Segmentation_");
             }
            else if (this.killCell_) {
                order.append("Kill");
            }

            return txt + order.toString();
        }
    }

    @Override
    public boolean isConfigAvailable(String config) {
        StrVector vcfgs = this.core_.getAvailableConfigs(this.core_.getChannelGroup());

        for(int i = 0; (long)i < vcfgs.size(); ++i) {
            if (config.compareTo(vcfgs.get(i)) == 0) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String[] getCameraConfigs() {
        return this.core_ == null ? new String[0] : this.core_.getAvailableConfigs("Camera").toArray();
    }

    @Override
    public String[] getAvailableGroups() {
        StrVector groups;
        try {
            groups = this.core_.getAllowedPropertyValues("Core", "ChannelGroup");
        } catch (Exception var5) {
            ReportingUtils.logError(var5);
            return new String[0];
        }

        ArrayList<String> strGroups = new ArrayList();
        Iterator i$ = groups.iterator();

        while(i$.hasNext()) {
            String group = (String)i$.next();
            if (this.groupIsEligibleChannel(group)) {
                strGroups.add(group);
            }
        }

        return strGroups.toArray(new String[0]);
    }

    private boolean groupIsEligibleChannel(String group) {
        StrVector cfgs = this.core_.getAvailableConfigs(group);
        if (cfgs.size() == 1L) {
            try {
                Configuration presetData = this.core_.getConfigData(group, cfgs.get(0));
                if (presetData.size() == 1L) {
                    PropertySetting setting = presetData.getSetting(0L);
                    String devLabel = setting.getDeviceLabel();
                    String propName = setting.getPropertyName();
                    if (this.core_.hasPropertyLimits(devLabel, propName)) {
                        return false;
                    }
                }
            } catch (Exception var7) {
                ReportingUtils.logError(var7);
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean isPaused() {
        return false;
    }

    @Override
    public void addImageProcessor(DataProcessor<TaggedImage> processor) {

    }

    @Override
    public void removeImageProcessor(DataProcessor<TaggedImage> taggedImageProcessor) {

    }

    @Override
    public void setImageProcessorPipeline(List<DataProcessor<TaggedImage>> pipeline) {

    }

    @Override
    public ArrayList<DataProcessor<TaggedImage>> getImageProcessorPipeline() {
        return null;
    }

    @Override
    public void registerProcessorClass(Class<? extends DataProcessor<TaggedImage>> processorClass, String name) {

    }

    @Override
    public List<String> getSortedDataProcessorNames() {
        return null;
    }

    @Override
    public DataProcessor<TaggedImage> makeProcessor(String Name, ScriptInterface gui) {
        return null;
    }

    @Override
    public DataProcessor<TaggedImage> getProcessorRegisteredAs(String name) {
        return null;
    }

    @Override
    public String getNameForProcessorClass(Class<? extends DataProcessor<TaggedImage>> processor) {
        return null;
    }

    @Override
    public void disposeProcessors() {

    }

    @Override
    public boolean abortRequested() {
        return false;
    }

    @Override
    public long getNextWakeTime() {
        return 0;
    }

    @Override
    public boolean isFinished() {
        return false;
    }

    @Override
    public void attachRunnable(int frame, int position, int channel, int slice, Runnable runnable) {

    }

    @Override
    public void clearRunnables() {

    }

    @Override
    public JSONObject getSummaryMetadata() {
        return null;
    }

    @Override
    public ImageCache getImageCache() {
        return null;
    }

    @Override
    public List<DataProcessor<TaggedImage>> getImageProcessors() {
        return null;
    }

    @Override
    public String getComment() {
        return null;
    }



    @Override
    public boolean getZAbsoluteMode() {
        return false;
    }
}
