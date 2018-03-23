//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.micromanager.rapp.SequenceAcquisition;

import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.prefs.Preferences;
import javax.swing.Icon;
import javax.swing.JOptionPane;
import mmcorej.CMMCore;
import mmcorej.Configuration;
import mmcorej.PropertySetting;
import mmcorej.StrVector;
import mmcorej.TaggedImage;
import org.json.JSONObject;
import org.micromanager.api.DataProcessor;
import org.micromanager.api.IAcquisitionEngine2010;
import org.micromanager.api.ImageCache;
import org.micromanager.api.PositionList;
import org.micromanager.api.ScriptInterface;
//import org.micromanager.api.SequenceSettings;
import org.micromanager.events.EventManager;
import org.micromanager.events.PipelineEvent;
import org.micromanager.events.ProcessorEvent;
import org.micromanager.internalinterfaces.AcqSettingsListener;
import org.micromanager.rapp.RappPlugin;
import org.micromanager.utils.AutofocusManager;
//import org.micromanager.utils.ChannelSpec;
import org.micromanager.utils.ContrastSettings;
import org.micromanager.utils.MMException;
import org.micromanager.utils.NumberUtils;
import org.micromanager.utils.ReportingUtils;
import org.micromanager.acquisition.DefaultTaggedImageSink;

public class AcquisitionWrapperEngine implements AcquisitionEngine {
   private CMMCore core_;
   protected ScriptInterface studio_;
   private PositionList posList_;
   private String zstage_;
   private double sliceZStepUm_;
   private double sliceZBottomUm_;
   private double sliceZTopUm_;
   private boolean useSlices_;
   private boolean useFrames_;
   private boolean useChannels_;
   private boolean useMultiPosition_;
   private boolean keepShutterOpenForStack_;
   private boolean keepShutterOpenForChannels_;
   private ArrayList<ChannelSpec> channels_ = new ArrayList();
   private String rootName_;
   private String dirName_;
   private int numFrames_;
   private double interval_;
   private double minZStepUm_;
   private String comment_;
   private boolean saveFiles_;
   private int acqOrderMode_;
   private boolean useAutoFocus_;
   private int afSkipInterval_;
   protected HashMap<String, Class<? extends DataProcessor<TaggedImage>>> nameToProcessorClass_ = new HashMap();
   protected List<DataProcessor<TaggedImage>> taggedImageProcessors_ = new ArrayList();
   private boolean absoluteZ_;
   private IAcquisitionEngine2010 acquisitionEngine2010;
   private ArrayList<Double> customTimeIntervalsMs_;
   private boolean useCustomIntervals_ = false;
   protected JSONObject summaryMetadata_;
   protected ImageCache imageCache_;
   private ArrayList<AcqSettingsListener> settingsListeners_ = new ArrayList();
   private AcquisitionManager acqManager_;

   public AcquisitionWrapperEngine(AcquisitionManager mgr) {

      this.core_ = RappPlugin.getMMcore();
      this.studio_ = RappPlugin.getScripI();
      posList_ = new PositionList();
      this.acqManager_ = mgr;
   }

   public String acquire() throws MMException {
      return this.runAcquisition(this.getSequenceSettings(), this.acqManager_);
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

   protected IAcquisitionEngine2010 getAcquisitionEngine2010() {
      if (this.acquisitionEngine2010 == null) {
         this.acquisitionEngine2010 = this.studio_.getAcquisitionEngine2010();
      }

      return this.acquisitionEngine2010;
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
         BlockingQueue<TaggedImage> engineOutputQueue = this.getAcquisitionEngine2010().run(acquisitionSettings, true, this.studio_.getPositionList(), this.studio_.getAutofocusManager().getDevice());

         this.summaryMetadata_ = this.getAcquisitionEngine2010().getSummaryMetadata();

         BlockingQueue<TaggedImage> procStackOutputQueue = ProcessorStack.run(engineOutputQueue, this.taggedImageProcessors_);
         String acqName = acqManager.createAcquisition(this.summaryMetadata_, acquisitionSettings.save, this, this.studio_.getHideMDADisplayOption());
         MMAcquisition acq = acqManager.getAcquisition(acqName);
         this.imageCache_ = acq.getImageCache();
         DefaultTaggedImageSink sink = new DefaultTaggedImageSink(procStackOutputQueue, (org.micromanager.api.ImageCache) this.imageCache_);
         sink.start(new Runnable() {
            public void run() {
               AcquisitionWrapperEngine.this.getAcquisitionEngine2010().stop();
            }
         });
         return acqName;
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

   public int getNumFrames() {
      int numFrames = this.numFrames_;
      if (!this.useFrames_) {
         numFrames = 1;
      }

      return numFrames;
   }

   private int getNumPositions() {
      int numPositions = Math.max(1, this.posList_.getNumberOfPositions());
      if (!this.useMultiPosition_) {
         numPositions = 1;
      }

      return numPositions;
   }

   private int getNumSlices() {
      if (!this.useSlices_) {
         return 1;
      } else {
         return this.sliceZStepUm_ == 0.0D ? 2147483647 : 1 + (int)Math.abs((this.sliceZTopUm_ - this.sliceZBottomUm_) / this.sliceZStepUm_);
      }
   }

   private int getTotalImages() {
      int totalImages = this.getNumFrames() * this.getNumSlices() * this.getNumChannels() * this.getNumPositions();
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

   public void attachRunnable(int frame, int position, int channel, int slice, Runnable runnable) {
      this.getAcquisitionEngine2010().attachRunnable(frame, position, channel, slice, runnable);
   }

   public void clearRunnables() {
      this.getAcquisitionEngine2010().clearRunnables();
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

   public void addImageProcessor(DataProcessor<TaggedImage> taggedImageProcessor) {
      if (!this.taggedImageProcessors_.contains(taggedImageProcessor)) {
         this.taggedImageProcessors_.add(taggedImageProcessor);
         EventManager.post(new PipelineEvent(this.taggedImageProcessors_));
      }

   }

   public void removeImageProcessor(DataProcessor<TaggedImage> taggedImageProcessor) {
      this.taggedImageProcessors_.remove(taggedImageProcessor);
      taggedImageProcessor.dispose();
      EventManager.post(new PipelineEvent(this.taggedImageProcessors_));
   }

   public void setImageProcessorPipeline(List<DataProcessor<TaggedImage>> pipeline) {
      this.taggedImageProcessors_.clear();
      this.taggedImageProcessors_.addAll(pipeline);
      EventManager.post(new PipelineEvent(this.taggedImageProcessors_));
   }

   public ArrayList<DataProcessor<TaggedImage>> getImageProcessorPipeline() {
      return new ArrayList(this.taggedImageProcessors_);
   }

   public void registerProcessorClass(Class<? extends DataProcessor<TaggedImage>> processorClass, String name) {
      if (this.nameToProcessorClass_.get(name) != null) {
         ReportingUtils.logError("Tried to register an additional DataProcessor under the name \"" + name + "\"; ignoring it.");
      } else {
         this.nameToProcessorClass_.put(name, processorClass);
         EventManager.post(new ProcessorEvent(name, processorClass));
      }

   }

   public List<String> getSortedDataProcessorNames() {
      Set<String> keys = this.nameToProcessorClass_.keySet();
      ArrayList<String> sortedKeys = new ArrayList();
      sortedKeys.addAll(keys);
      Collections.sort(sortedKeys);
      return sortedKeys;
   }

   public DataProcessor<TaggedImage> makeProcessor(String name, ScriptInterface gui) {
      Class processorClass = (Class)this.nameToProcessorClass_.get(name);

      DataProcessor newProcessor;
      try {
         newProcessor = (DataProcessor)processorClass.newInstance();
         newProcessor.setApp(gui);
         this.addImageProcessor(newProcessor);
      } catch (Exception var6) {
         ReportingUtils.logError("Failed to create processor " + name + " mapped to class " + processorClass + ": " + var6);
         newProcessor = null;
      }

      return newProcessor;
   }

   public DataProcessor<TaggedImage> getProcessorRegisteredAs(String name) {
      Class<? extends DataProcessor<TaggedImage>> processorClass = (Class)this.nameToProcessorClass_.get(name);
      Iterator i$ = this.taggedImageProcessors_.iterator();

      DataProcessor processor;
      do {
         if (!i$.hasNext()) {
            return null;
         }

         processor = (DataProcessor)i$.next();
      } while(processor.getClass() != processorClass);

      return processor;
   }

   public String getNameForProcessorClass(Class<? extends DataProcessor<TaggedImage>> processorClass) {
      Iterator i$ = this.nameToProcessorClass_.keySet().iterator();

      String name;
      do {
         if (!i$.hasNext()) {
            return null;
         }

         name = (String)i$.next();
      } while(this.nameToProcessorClass_.get(name) != processorClass);

      return name;
   }

   public void disposeProcessors() {
      Iterator i$ = this.taggedImageProcessors_.iterator();

      while(i$.hasNext()) {
         DataProcessor<TaggedImage> processor = (DataProcessor)i$.next();
         processor.dispose();
      }

   }

   public SequenceSettings getSequenceSettings() {
      SequenceSettings acquisitionSettings = new SequenceSettings();
      this.updateChannelCameras();
      if (this.useFrames_) {
         if (this.useCustomIntervals_) {
            acquisitionSettings.customIntervalsMs = this.customTimeIntervalsMs_;
            acquisitionSettings.numFrames = acquisitionSettings.customIntervalsMs.size();
         } else {
            acquisitionSettings.numFrames = this.numFrames_;
            acquisitionSettings.intervalMs = this.interval_;
         }
      } else {
         acquisitionSettings.numFrames = 0;
      }

      if (this.useSlices_) {
         double start = this.sliceZBottomUm_;
         double stop = this.sliceZTopUm_;
         double step = Math.abs(this.sliceZStepUm_);
         if (step == 0.0D) {
            throw new UnsupportedOperationException("zero Z step size");
         }

         int count = this.getNumSlices();
         if (start > stop) {
            step = -step;
         }

         for(int i = 0; i < count; ++i) {
            acquisitionSettings.slices.add(start + (double)i * step);
         }
      }

      acquisitionSettings.relativeZSlice = !this.absoluteZ_;

      try {
         String zdrive = this.core_.getFocusDevice();
         acquisitionSettings.zReference = zdrive.length() > 0 ? this.core_.getPosition(this.core_.getFocusDevice()) : 0.0D;
      } catch (Exception var10) {
         ReportingUtils.logError(var10);
      }

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

      acquisitionSettings.timeFirst = this.acqOrderMode_ == 3 || this.acqOrderMode_ == 2;
      acquisitionSettings.slicesFirst = this.acqOrderMode_ == 3 || this.acqOrderMode_ == 1;
      acquisitionSettings.useAutofocus = this.useAutoFocus_;
      acquisitionSettings.skipAutofocusCount = this.afSkipInterval_;
      acquisitionSettings.keepShutterOpenChannels = this.keepShutterOpenForChannels_;
      acquisitionSettings.keepShutterOpenSlices = this.keepShutterOpenForStack_;
      acquisitionSettings.save = this.saveFiles_;
      if (this.saveFiles_) {
         acquisitionSettings.root = this.rootName_;
         acquisitionSettings.prefix = this.dirName_;
      }

      acquisitionSettings.comment = this.comment_;
      acquisitionSettings.usePositionList = this.useMultiPosition_;
      return acquisitionSettings;
   }

   public void setSequenceSettings(SequenceSettings ss) {
      this.updateChannelCameras();
      this.useFrames_ = true;
      if (this.useCustomIntervals_) {
         this.customTimeIntervalsMs_ = ss.customIntervalsMs;
         this.numFrames_ = ss.customIntervalsMs.size();
      } else {
         this.numFrames_ = ss.numFrames;
         this.interval_ = ss.intervalMs;
      }

      this.useSlices_ = true;
      if (ss.slices.size() == 0) {
         this.useSlices_ = false;
      } else if (ss.slices.size() == 1) {
         this.sliceZBottomUm_ = (Double)ss.slices.get(0);
         this.sliceZTopUm_ = this.sliceZBottomUm_;
         this.sliceZStepUm_ = 0.0D;
      } else {
         this.sliceZBottomUm_ = (Double)ss.slices.get(0);
         this.sliceZTopUm_ = (Double)ss.slices.get(ss.slices.size() - 1);
         this.sliceZStepUm_ = (Double)ss.slices.get(1) - (Double)ss.slices.get(0);
         if (this.sliceZBottomUm_ > this.sliceZBottomUm_) {
            this.sliceZStepUm_ = -this.sliceZStepUm_;
         }
      }

      this.absoluteZ_ = !ss.relativeZSlice;
      if (ss.channels.size() > 0) {
         this.useChannels_ = true;
      } else {
         this.useChannels_ = false;
      }

      this.channels_ = ss.channels;
      if (ss.timeFirst && ss.slicesFirst) {
         this.acqOrderMode_ = 3;
      }

      if (ss.timeFirst && !ss.slicesFirst) {
         this.acqOrderMode_ = 2;
      }

      if (!ss.timeFirst && ss.slicesFirst) {
         this.acqOrderMode_ = 1;
      }

      if (!ss.timeFirst && !ss.slicesFirst) {
         this.acqOrderMode_ = 0;
      }

      this.useAutoFocus_ = ss.useAutofocus;
      this.afSkipInterval_ = ss.skipAutofocusCount;
      this.keepShutterOpenForChannels_ = ss.keepShutterOpenChannels;
      this.keepShutterOpenForStack_ = ss.keepShutterOpenSlices;
      this.saveFiles_ = ss.save;
      this.rootName_ = ss.root;
      this.dirName_ = ss.prefix;
      this.comment_ = ss.comment;
      this.useMultiPosition_ = ss.usePositionList;
   }

   public void stop(boolean interrupted) {
      try {
         if (this.acquisitionEngine2010 != null) {
            this.acquisitionEngine2010.stop();
         }
      } catch (Exception var3) {
         ReportingUtils.showError(var3, "Acquisition engine stop request failed");
      }

   }

   public boolean abortRequest() {
      if (this.isAcquisitionRunning()) {
         String[] options = new String[]{"Abort", "Cancel"};
         int result = JOptionPane.showOptionDialog((Component)null, "Abort current acquisition task?", "Micro-Manager", -1, 3, (Icon)null, options, options[1]);
         if (result == 0) {
            this.stop(true);
            return true;
         }
      }

      return false;
   }

   public boolean abortRequested() {
      return this.acquisitionEngine2010.stopHasBeenRequested();
   }

   public void shutdown() {
      this.stop(true);
   }

   public void setPause(boolean state) {
      if (state) {
         this.acquisitionEngine2010.pause();
      } else {
         this.acquisitionEngine2010.resume();
      }

   }

   public boolean isAcquisitionRunning() {
      return this.acquisitionEngine2010 != null ? this.acquisitionEngine2010.isRunning() : false;
   }

   public boolean isFinished() {
      return this.acquisitionEngine2010 != null ? this.acquisitionEngine2010.isFinished() : false;
   }

   public boolean isMultiFieldRunning() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public long getNextWakeTime() {
      return this.acquisitionEngine2010.nextWakeTime();
   }

   public void setCore(CMMCore core_, AutofocusManager afMgr) {
      this.core_ = core_;
   }

   public void setPositionList(PositionList posList) {
      this.posList_ = posList;
   }

   public void setParentGUI(ScriptInterface parent) {
      this.studio_ = parent;
   }

   public void setZStageDevice(String stageLabel_) {
      this.zstage_ = stageLabel_;
   }

   public void setUpdateLiveWindow(boolean b) {
   }

   public void setFinished() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public int getCurrentFrameCount() {
      return 0;
   }

   public double getFrameIntervalMs() {
      return this.interval_;
   }

   public double getSliceZStepUm() {
      return this.sliceZStepUm_;
   }

   public double getSliceZBottomUm() {
      return this.sliceZBottomUm_;
   }

   public void setChannel(int row, ChannelSpec channel) {
      this.channels_.set(row, channel);
   }

   public String getFirstConfigGroup() {
      if (this.core_ == null) {
         return "";
      } else {
         String[] groups = this.getAvailableGroups();
         return groups != null && groups.length >= 1 ? this.getAvailableGroups()[0] : "";
      }
   }

   public String[] getChannelConfigs() {
      return this.core_ == null ? new String[0] : this.core_.getAvailableConfigs(this.core_.getChannelGroup()).toArray();
   }

   public String getChannelGroup() {
      return this.core_.getChannelGroup();
   }

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

   public void clear() {
      if (this.channels_ != null) {
         this.channels_.clear();
      }

      this.numFrames_ = 0;
   }

   public void setFrames(int numFrames, double interval) {
      this.numFrames_ = numFrames;
      this.interval_ = interval;
   }

   public double getMinZStepUm() {
      return this.minZStepUm_;
   }

   public void setSlices(double bottom, double top, double step, boolean absolute) {
      this.sliceZBottomUm_ = bottom;
      this.sliceZTopUm_ = top;
      this.sliceZStepUm_ = step;
      this.absoluteZ_ = absolute;
      this.settingsChanged();
   }

   public boolean getZAbsoluteMode() {
      return this.absoluteZ_;
   }

   public boolean isFramesSettingEnabled() {
      return this.useFrames_;
   }

   public void enableFramesSetting(boolean enable) {
      this.useFrames_ = enable;
   }

   public boolean isChannelsSettingEnabled() {
      return this.useChannels_;
   }

   public void enableChannelsSetting(boolean enable) {
      this.useChannels_ = enable;
   }

   public boolean isZSliceSettingEnabled() {
      return this.useSlices_;
   }

   public double getZTopUm() {
      return this.sliceZTopUm_;
   }

   public void keepShutterOpenForStack(boolean open) {
      this.keepShutterOpenForStack_ = open;
   }

   public boolean isShutterOpenForStack() {
      return this.keepShutterOpenForStack_;
   }

   public void keepShutterOpenForChannels(boolean open) {
      this.keepShutterOpenForChannels_ = open;
   }

   public boolean isShutterOpenForChannels() {
      return this.keepShutterOpenForChannels_;
   }

   public void enableZSliceSetting(boolean boolean1) {
      this.useSlices_ = boolean1;
   }

   public void enableMultiPosition(boolean selected) {
      this.useMultiPosition_ = selected;
   }

   public boolean isMultiPositionEnabled() {
      return this.useMultiPosition_;
   }

   public ArrayList<ChannelSpec> getChannels() {
      return this.channels_;
   }

   public void setChannels(ArrayList<ChannelSpec> channels) {
      this.channels_ = channels;
   }

   public String getRootName() {
      return this.rootName_;
   }

   public void setRootName(String absolutePath) {
      this.rootName_ = absolutePath;
   }

   public void setCameraConfig(String cfg) {
   }

   public void setDirName(String text) {
      this.dirName_ = text;
   }

   public void setComment(String text) {
      this.comment_ = text;
      this.settingsChanged();
   }


   public boolean addChannel(String config, double exp, Boolean doSegmentation, ContrastSettings con8, ContrastSettings con16,  Color c, boolean use) {
      return this.addChannel(config, exp, doSegmentation,  con8,  c, use);
   }

   public boolean addChannel(String config, double exp, Boolean doSegmentation,  ContrastSettings con, Color c, boolean use) {
      if (this.isConfigAvailable(config)) {
         ChannelSpec channel = new ChannelSpec();
         channel.config = config;
         channel.useChannel = use;
         channel.exposure = exp;
         channel.doSegmentation = doSegmentation;
         channel.contrast = con;
         channel.color = c;
         this.channels_.add(channel);
         return true;
      } else {
         ReportingUtils.logError("\"" + config + "\" is not found in the current Channel group.");
         return false;
      }
   }

   public boolean addChannel(String config, double exp,  ContrastSettings c8, ContrastSettings c16, Color c) {
      return this.addChannel(config, exp, true,  c16,  c, true);
   }

   public void setSaveFiles(boolean selected) {
      this.saveFiles_ = selected;
   }

   public boolean getSaveFiles() {
      return this.saveFiles_;
   }

   public void setDisplayMode(int mode) {
   }

   public int getAcqOrderMode() {
      return this.acqOrderMode_;
   }

   public int getDisplayMode() {
      return 0;
   }

   public void setAcqOrderMode(int mode) {
      this.acqOrderMode_ = mode;
   }

   public void enableAutoFocus(boolean enabled) {
      this.useAutoFocus_ = enabled;
   }

   public boolean isAutoFocusEnabled() {
      return this.useAutoFocus_;
   }

   public int getAfSkipInterval() {
      return this.afSkipInterval_;
   }

   public void setAfSkipInterval(int interval) {
      this.afSkipInterval_ = interval;
   }

   public void setParameterPreferences(Preferences prefs) {
   }

   public void setSingleFrame(boolean selected) {
   }

   public void setSingleWindow(boolean selected) {
   }

   public String installAutofocusPlugin(String className) {
      throw new UnsupportedOperationException("Not supported yet.");
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

   public String getVerboseSummary() {
      int numFrames = this.getNumFrames();
      int numSlices = this.getNumSlices();
      int numPositions = this.getNumPositions();
      int numChannels = this.getNumChannels();
      int totalImages = this.getTotalImages();
      long totalMB = this.getTotalMB();
      double totalDurationSec = 0.0D;
      Double d;
      if (!this.useCustomIntervals_) {
         totalDurationSec = this.interval_ * (double)numFrames / 1000.0D;
      } else {
         for(Iterator i$ = this.customTimeIntervalsMs_.iterator(); i$.hasNext(); totalDurationSec += d / 1000.0D) {
            d = (Double)i$.next();
         }
      }

      int hrs = (int)(totalDurationSec / 3600.0D);
      double remainSec = totalDurationSec - (double)(hrs * 3600);
      int mins = (int)(remainSec / 60.0D);
      remainSec -= (double)(mins * 60);
      String txt = "Number of time points: " + (!this.useCustomIntervals_ ? numFrames : this.customTimeIntervalsMs_.size()) + "\nNumber of positions: " + numPositions + "\nNumber of slices: " + numSlices + "\nNumber of channels: " + numChannels + "\nTotal images: " + totalImages + "\nTotal memory: " + (totalMB <= 1024L ? totalMB + " MB" : NumberUtils.doubleToDisplayString((double)totalMB / 1024.0D) + " GB") + "\nDuration: " + hrs + "h " + mins + "m " + NumberUtils.doubleToDisplayString(remainSec) + "s";
      if (!this.useFrames_ && !this.useMultiPosition_ && !this.useChannels_ && !this.useSlices_) {
         return txt;
      } else {
         StringBuffer order = new StringBuffer("\nOrder: ");
         if (this.useFrames_ && this.useMultiPosition_) {
            if (this.acqOrderMode_ != 1 && this.acqOrderMode_ != 0) {
               order.append("Position, Time");
            } else {
               order.append("Time, Position");
            }
         } else if (this.useFrames_) {
            order.append("Time");
         } else if (this.useMultiPosition_) {
            order.append("Position");
         }

         if ((this.useFrames_ || this.useMultiPosition_) && (this.useChannels_ || this.useSlices_)) {
            order.append(", ");
         }

         if (this.useChannels_ && this.useSlices_) {
            if (this.acqOrderMode_ != 1 && this.acqOrderMode_ != 3) {
               order.append("Slice, Channel");
            } else {
               order.append("Channel, Slice");
            }
         } else if (this.useChannels_) {
            order.append("Channel");
         } else if (this.useSlices_) {
            order.append("Slice");
         }

         return txt + order.toString();
      }
   }

   public boolean isConfigAvailable(String config) {
      StrVector vcfgs = this.core_.getAvailableConfigs(this.core_.getChannelGroup());

      for(int i = 0; (long)i < vcfgs.size(); ++i) {
         if (config.compareTo(vcfgs.get(i)) == 0) {
            return true;
         }
      }

      return false;
   }

   public String[] getCameraConfigs() {
      return this.core_ == null ? new String[0] : this.core_.getAvailableConfigs("Camera").toArray();
   }

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

      return (String[])strGroups.toArray(new String[0]);
   }

   public double getCurrentZPos() {
      if (this.isFocusStageAvailable()) {
         double z = 0.0D;

         try {
            z = this.core_.getPosition(this.core_.getFocusDevice());
         } catch (Exception var4) {
            ReportingUtils.showError(var4);
         }

         return z;
      } else {
         return 0.0D;
      }
   }

   public boolean isPaused() {
      return this.acquisitionEngine2010.isPaused();
   }

   public void restoreSystem() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   protected boolean isFocusStageAvailable() {
      return this.zstage_ != null && this.zstage_.length() > 0;
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

   public List<DataProcessor<TaggedImage>> getImageProcessors() {
      return this.taggedImageProcessors_;
   }

   public void setCustomTimeIntervals(double[] customTimeIntervals) {
      if (customTimeIntervals != null && customTimeIntervals.length != 0) {
         this.enableCustomTimeIntervals(true);
         this.customTimeIntervalsMs_ = new ArrayList();
         double[] arr$ = customTimeIntervals;
         int len$ = customTimeIntervals.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            double d = arr$[i$];
            this.customTimeIntervalsMs_.add(d);
         }
      } else {
         this.customTimeIntervalsMs_ = null;
         this.enableCustomTimeIntervals(false);
      }

   }

   public double[] getCustomTimeIntervals() {
      if (this.customTimeIntervalsMs_ == null) {
         return null;
      } else {
         double[] intervals = new double[this.customTimeIntervalsMs_.size()];

         for(int i = 0; i < this.customTimeIntervalsMs_.size(); ++i) {
            intervals[i] = (Double)this.customTimeIntervalsMs_.get(i);
         }

         return intervals;
      }
   }

   public void enableCustomTimeIntervals(boolean enable) {
      this.useCustomIntervals_ = enable;
   }

   public boolean customTimeIntervalsEnabled() {
      return this.useCustomIntervals_;
   }

   public JSONObject getSummaryMetadata() {
      return this.summaryMetadata_;
   }

   public org.micromanager.api.ImageCache getImageCache() {
      return this.imageCache_;
   }

   public String getComment() {
      return this.comment_;
   }
}
