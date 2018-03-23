package org.micromanager.rapp.SequenceAcquisitions;

import mmcorej.CMMCore;
import mmcorej.Configuration;
import mmcorej.TaggedImage;
import org.json.JSONObject;
import org.micromanager.acquisition.DefaultTaggedImageSink;
import org.micromanager.api.*;
import org.micromanager.internalinterfaces.AcqSettingsListener;
import org.micromanager.rapp.RappPlugin;
import org.micromanager.rapp.SequenceAcquisition.*;
import org.micromanager.rapp.SequenceAcquisition.ChannelSpec;
import org.micromanager.rapp.SequenceAcquisition.SequenceSettings;
import org.micromanager.utils.AutofocusManager;
import org.micromanager.utils.ContrastSettings;
import org.micromanager.utils.MMException;
import org.micromanager.utils.ReportingUtils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class SeqAcqController implements AcquisitionEngine {
    private CMMCore core_;
    private ScriptInterface studio_;
    private PositionList posList_;
    private ArrayList<AcqSettingsListener> settingsListeners_ = new ArrayList();
    private ArrayList<ChannelSpec> channels_ = new ArrayList();
    private String comment_;
    private boolean saveFiles_;
    private int acqOrderMode_;
    private boolean useAutoFocus_;
    private String rootName_;
    private String dirName_;
    private boolean useChannels_;
    private boolean useMultiPosition_;
    private boolean keepShutterOpenForStack_;
    private boolean keepShutterOpenForChannels_;




    public SeqAcqController (){

        this.core_ = RappPlugin.getMMcore();
        this.studio_ = RappPlugin.getScripI();
        posList_ = new PositionList();

    }

    public String acquire() throws MMException {
       // return this.runAcquisition(this.getSequenceSettings(), this.acqManager_);
        return null;
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

    protected String runAcquisition(SequenceSettings acquisitionSettings, AcquisitionManager acqManager) {
        if (this.saveFiles_) {
            File root = new File(this.rootName_);
            if (!root.canWrite()) {
                int result = JOptionPane.showConfirmDialog((Component)null, "The specified root directory\n" + root.getAbsolutePath() + "\ndoes not exist. Create it?", "Directory not found.", 0);
                if (result != 0) {
                    ReportingUtils.showMessage("Acquisition canceled.");
                    return null;
                }

                root.mkdirs();
                if (!root.canWrite()) {
                    ReportingUtils.showError("Unable to save data to selected location: check that location exists.\nAcquisition canceled.");
                    return null;
                }
            } else if (!this.enoughDiskSpace()) {
                ReportingUtils.showError("Not enough space on disk to save the requested image set; acquisition canceled.");
                return null;
            }
        }

        this.studio_.enableLiveMode(false);

        try {
//            BlockingQueue<TaggedImage> engineOutputQueue = this.getAcquisitionEngine2010().run(acquisitionSettings, true, this.studio_.getPositionList(), this.studio_.getAutofocusManager().getDevice());
//
//            this.summaryMetadata_ = this.getAcquisitionEngine2010().getSummaryMetadata();
//
//            BlockingQueue<TaggedImage> procStackOutputQueue = ProcessorStack.run(engineOutputQueue, this.taggedImageProcessors_);
//            String acqName = acqManager.createAcquisition(this.summaryMetadata_, acquisitionSettings.save, this, this.studio_.getHideMDADisplayOption());
//            MMAcquisition acq = acqManager.getAcquisition(acqName);
//            this.imageCache_ = acq.getImageCache();
//            DefaultTaggedImageSink sink = new DefaultTaggedImageSink(procStackOutputQueue, (org.micromanager.api.ImageCache) this.imageCache_);
//            sink.start(new Runnable() {
//                public void run() {
//                    AcquisitionWrapperEngine.this.getAcquisitionEngine2010().stop();
//                }
//            });
//            return acqName;
            return null;
        } catch (Throwable var8) {
            ReportingUtils.showError(var8);
            return null;
        }
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
        CMMCore core = this.studio_.getMMCore();
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

    }

    @Override
    public String getFirstConfigGroup() {
        return null;
    }

    @Override
    public String[] getChannelConfigs() {
        return new String[0];
    }



    @Override
    public String getChannelGroup() {
        return null;
    }

    @Override
    public boolean setChannelGroup(String newGroup) {
        return false;
    }

    @Override
    public void clear() {

    }



    @Override
    public boolean isChannelsSettingEnabled() {
        return false;
    }

    @Override
    public void enableChannelsSetting(boolean enable) {

    }


    @Override
    public void keepShutterOpenForChannels(boolean open) {

    }

    @Override
    public boolean isShutterOpenForChannels() {
        return false;
    }



    @Override
    public void enableMultiPosition(boolean selected) {

    }

    @Override
    public boolean isMultiPositionEnabled() {
        return false;
    }

    @Override
    public ArrayList<ChannelSpec> getChannels() {
        return null;
    }

    @Override
    public void setChannels(ArrayList<ChannelSpec> channels) {

    }

    @Override
    public String getRootName() {
        return null;
    }

    @Override
    public void setRootName(String absolutePath) {

    }

    @Override
    public void setCameraConfig(String config) {

    }

    @Override
    public void setDirName(String text) {

    }

    @Override
    public void setComment(String text) {

    }

    @Override
    public boolean addChannel(String name, double exp, ContrastSettings s8, ContrastSettings s16, Color c) {
        return false;
    }

    @Override
    public boolean addChannel(String name, double exp, Boolean doSegmentation, ContrastSettings s8, ContrastSettings s16, Color c, boolean use) {
        return false;
    }

    @Override
    public boolean addChannel(String name, double exp, Boolean doSegmentation, ContrastSettings con, Color c, boolean use) {
        return false;
    }

    @Override
    public void setSaveFiles(boolean selected) {

    }

    @Override
    public boolean getSaveFiles() {
        return false;
    }

    @Override
    public int getDisplayMode() {
        return 0;
    }

    @Override
    public void setDisplayMode(int mode) {

    }

    @Override
    public int getAcqOrderMode() {
        return 0;
    }

    @Override
    public void setAcqOrderMode(int mode) {

    }

    @Override
    public void enableAutoFocus(boolean enabled) {

    }

    @Override
    public boolean isAutoFocusEnabled() {
        return false;
    }





    @Override
    public void setSingleWindow(boolean selected) {

    }

    @Override
    public String installAutofocusPlugin(String className) {
        return null;
    }

    @Override
    public String getVerboseSummary() {
        return null;
    }

    @Override
    public boolean isConfigAvailable(String config_) {
        return false;
    }

    @Override
    public String[] getCameraConfigs() {
        return new String[0];
    }

    @Override
    public String[] getAvailableGroups() {
        return new String[0];
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
