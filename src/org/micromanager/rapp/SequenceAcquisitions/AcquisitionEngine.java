///////////////////////////////////////////////////////////////////////////////
//FILE:          AcquisitionEngine.java
//PROJECT:       Micro-Manager
//SUBSYSTEM:     mmstudio
//-----------------------------------------------------------------------------
//
// AUTHOR:       Nenad Amodaj, nenad@amodaj.com, November 1, 2005
//
// COPYRIGHT:    University of California, San Francisco, 2006
//
// LICENSE:      This file is distributed under the BSD license.
//               License text is included with the source distribution.
//
//               This file is distributed in the hope that it will be useful,
//               but WITHOUT ANY WARRANTY; without even the implied warranty
//               of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
//               IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//               CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//               INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.
//
// CVS:          $Id: AcquisitionEngine.java 318 2007-07-02 22:29:55Z nenad $
//

package org.micromanager.rapp.SequenceAcquisitions;

import mmcorej.CMMCore;
import mmcorej.TaggedImage;
import org.json.JSONObject;
import org.micromanager.api.DataProcessor;
import org.micromanager.api.ImageCache;
import org.micromanager.api.PositionList;
import org.micromanager.api.ScriptInterface;
import org.micromanager.internalinterfaces.AcqSettingsListener;
import org.micromanager.utils.AutofocusManager;
import org.micromanager.utils.ContrastSettings;
import org.micromanager.utils.MMException;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

//import org.micromanager.rapp.utils.ImageCache;
//import org.micromanager.utils.ChannelSpec;

/**
 * The original Acquisition engine interface. This interface is still used
 * by scripts and the AcqDialog and should be retained. The current
 * implementation of the interface is the AcquisitionWrapperEngine,
 * which simply adapters the old interface to an object implementing the
 * new interface, IAcquisitionEngine2010.
 */
public interface AcquisitionEngine {
   

   String cameraGroup_ = "Camera";
   DecimalFormat FMT2 = new DecimalFormat("#0.00");
   String DEFAULT_ROOT_NAME = "C:/AcquisitionData";
   
   // initialization
   void setCore(CMMCore core_, AutofocusManager afMgr);

   /**
    * Sets the global position list attached to the parent Micro-Manager gui.
    */
   void setPositionList(PositionList posList);

   /**
    * Provides the acquisition engine with the parent Micro-Manager gui.
    */
   void setParentGUI(ScriptInterface parent);


   /**
    * Sets whether the Live window will be updated during acquistion
    */
   void setUpdateLiveWindow(boolean b);
   
   // run-time control

   /**
    * Starts acquisition as defined in the Multi-Dimensional Acquistion Window.
    * Returns the acquisition name.
    * @throws MMException
  //  * @throws MMAcqDataException
    */
   String acquire() throws MMException;

   /**
    * Stops a running Acquisition
    * @param   interrupted when set, multifield acquisition will also be stopped
    */
   void stop(boolean interrupted);


   /**
    * Request immediate abort of current task
    */
   boolean abortRequest();

   /**
    * Signals that a running acquisition is done.
    */
   void setFinished();

   /**
    * Returns true when Acquisition is running
    */
   boolean isAcquisitionRunning();

   /**
    * Determines if a multi-field acquistion is running
    */
   boolean isMultiFieldRunning();


   /**
    * enables/diasables the use of custom time points
    * @param enable 
    */
   void enableCustomTimeIntervals(boolean enable);
   
   /*
    * returns true if acquisition engine is se p to use custom time intervals
    */
   boolean customTimeIntervalsEnabled();
   
   /**
    * Used to provide acquisition with custom time intervals in between frames
    * passing null resets to default time points
    */
   void setCustomTimeIntervals(double[] customTimeIntervalsMs);

   /*
    * returns list of custom time intervals, or null if none are specified   
    */
   double[] getCustomTimeIntervals();

   /**
    * Unconditional shutdown.  Will stop acuiqistion and multi-field acquisition
    */
   void shutdown();

   /**
    * Pause/Unpause a running acquistion
    */
   void setPause(boolean state);
   
   // settings



   /**
    * Sets channel specification in the given row
    */
   void setChannel(int row, ChannelSpec channel);

   /**
    * Find out which groups are available
    */
   String getFirstConfigGroup();

   /**
    * Find out which channels are currently available for the selected channel group.
    * @return - list of channel (preset) names
    */
   String[] getChannelConfigs();

   /**
    * Returns the configuration preset group currently selected in the Multi-Dimensional Acquistion Window
    */
   String getChannelGroup();

   /**
    * Set the channel group if the current hardware configuration permits.
    * @param newGroup
    * @return - true if successful
    */
   boolean setChannelGroup(String newGroup);

   /**
    * Resets the engine
    */
   void clear();



   /*
    * Returns whether channels will be included in the acquired dimensions.
    */
   boolean isChannelsSettingEnabled();

   /*
    * Sets whether channels are to be included in the settings. If this
    * value is set to false, then only a single channel is acquired, with
    * whatever the current device settings are.
    */
   void enableChannelsSetting(boolean enable);
    /*
     * Returns whether channels will be included in the acquired dimensions.
     */
    boolean isKillCellEnabled();

    /*
     * Sets whether channels are to be included in the settings. If this
     * value is set to false, then only a single channel is acquired, with
     * whatever the current device settings are.
     */
    void enableKillCell(boolean enable);


    /**
    * Flag indicating whether to override autoshutter behavior and keep the shutter
    * open for channel imaging.  This only has an effect when autoshutter is on, and when
    * mode "Channels First" has been chosen.
    */
    void keepShutterOpenForChannels(boolean open);

   /**
    * Returns flag indicating whether to override autoshutter behavior
    * during channel acquisition
    */
   boolean isShutterOpenForChannels();

   /**
    * Sets a flag that signals whether multiple positions will be acquired
    * @param selected - acquires at multiple stage positions when true
    */
   void enableMultiPosition(boolean selected);

   /**
    * Returns true when multiple positions will be acquired
    * @return whether or not acquisition will be executed at multiple stage
    * positions
    */
   boolean isMultiPositionEnabled();

    /**
     * Sets a flag that signals whether multiple positions will be acquired
     * @param selected - acquires at multiple stage positions when true
     */
    void enableSegmentation(boolean selected);

    /**
     * Returns true when multiple positions will be acquired
     * @return whether or not acquisition will be executed at multiple stage
     * positions
     */
    boolean isDoSegmentationEnabled();

    /**
    * Access to the channels used in this acquisition
    * @return - Channels used in this acquisition
    */
    ArrayList<ChannelSpec> getChannels();

   /**
    * Sets the channels to be used in this acquisition
    * @param channels
    */
   void setChannels(ArrayList<ChannelSpec> channels);

   /**
    * Returns path to the location where the acquisitions will be stored on
    * disk
    */
   String getRootName();

   /**
    * Sets the absolute path for where the acquisitions will be stored on disk.
    * @param absolutePath
    */
   void setRootName(String absolutePath);



   /**
    * @Deprecated
    */
   void setCameraConfig(String config);

   /**
    * Sets the name for the directory in which the images and data are
    * contained. Also known as the "prefix". This dir will be nested inside the root
    * directory specified by setRootName.
    */
   void setDirName(String text);

   /*
    * Sets the default comment to be included in the acquisition's summary metadata.
    * Equivalent to the comment box in the Multi-Dimensional Acquisition setup window.
    */
   void setComment(String text);


   /**
    * @Deprecated
    */
//   public boolean addChannel(String name, double exp, double offset,
//           ContrastSettings s8, ContrastSettings s16, int skip, Color c);

   boolean addChannel(String name, double exp, double LaserExp, Boolean killCell,
                      ContrastSettings s8, ContrastSettings s16, Color c);


   /*
    * Adds a channel to the acquisition settings.
    * @param name - The name of the channel, matching a configuration preset in the channel group.
    * @param exp - The exposure time for this channel
    * @param doZStack - If false, then z stacks will be skipped for this channel
    * @param offset - If nonzero, offsets z positions for this channel by the provided amount, in microns.
    * @param s8 - Provides contrast settings for this channel for 8-bit images.
    * @param s16 - Provides contrast settings for this channel for 16-bit images.
    * @param skip - If nonzero, this channel is skipped for some frames.
    * @param c - Provides the preferred color for this channel
    * @param use - If false, this channel will not be included in the acquisition.
    */
//   public boolean addChannel(String name, double exp, Boolean doZStack,
//           double offset, ContrastSettings s8, ContrastSettings s16, int skip, Color c,
//           boolean use);

   boolean addChannel(String name, double exp, double LaserExp, Boolean doSegmentation, Boolean killCell,
                             ContrastSettings s8, ContrastSettings s16, Color c,
                             boolean use);


   /*
    * Adds a channel to the acquisition settings.
    * @param name - The name of the channel, matching a configuration preset in the channel group.
    * @param exp - The exposure time for this channel
    * @param doZStack - If false, then z stacks will be skipped for this channel
    * @param offset - If nonzero, offsets z positions for this channel by the provided amount, in microns.
    * @param con - Provides contrast settings for this channel.
    * @param skip - If nonzero, this channel is skipped for some frames.
    * @param c - Provides the preferred color for this channel
    * @param use - If false, this channel will not be included in the acquisition.
    */
//   public boolean addChannel(String name, double exp, Boolean doZStack,
//           double offset, ContrastSettings con, int skip, Color c,
//           boolean use);

   boolean addChannel(String name, double exp, double LaserExp, Boolean doSegmentation, Boolean killCell,
                             ContrastSettings con, Color c,
                             boolean use);
   /*
    * Sets whether image data should be stored to disk or to RAM during
    * acquisition.
    * @param selected - If true, image data will be saved to disk during acquisition.
    *
    */
   void setSaveFiles(boolean selected);

   /*
    * Returns the settings that if true, indicates images will be saved
    * to disk during acquisition.
    */
   boolean getSaveFiles();

   /**
    * @Deprecated
    */
   int getDisplayMode();

   /**
    * @Deprecated
    */
   void setDisplayMode(int mode);


   /**
    * Returns the setting for the order of the four dimensions (P, T, C, Z).
    * Possible values are enumerated in org.micromanager.utils.AcqOrderMode
    */
   int getAcqOrderMode();

   /**
    * Sets the value for the order of the four dimensions (P, T, C, Z).
    * Possible values are enumerated in org.micromanager.utils.AcqOrderMode
    */

   /*
    * If set to true, autofocus will be used during the acquisition.
    */
   void enableAutoFocus(boolean enabled);

   /*
    * Returns true if autofocus is requested for the acquisition.
    */
   boolean isAutoFocusEnabled();


    /*
    * @Deprecated
    */
    void setSingleWindow(boolean selected);

   /*
    * @Deprecated
    */
   String installAutofocusPlugin(String className);
   
   // utility
   String getVerboseSummary();

   
   boolean isConfigAvailable(String config_);

   /*
    * @Deprecated
    * Returns available configurations for the camera group.
    */
   String[] getCameraConfigs();

   /*
    * Returns the available groups in Micro-Manager's configuration settings.
    */
   String[] getAvailableGroups();


   /*
    * Returns true if the acquisition is currently paused.
    */
   boolean isPaused();

   /**
    * Adds an image processor to the DataProcessor pipeline.
    */
   void addImageProcessor(DataProcessor<TaggedImage> processor);

   /**
    * Removes an image processor from the DataProcessor pipeline.
    */
   void removeImageProcessor(DataProcessor<TaggedImage> taggedImageProcessor);

   /**
    * Replace the current DataProcessor pipeline with the provided one.
    */
   void setImageProcessorPipeline(List<DataProcessor<TaggedImage>> pipeline);

   /**
    * Return a copy of the entire DataProcessor pipeline.
    */
   ArrayList<DataProcessor<TaggedImage>> getImageProcessorPipeline();

   /**
    * Register a DataProcessor class for later use under a unique name.
    */
   void registerProcessorClass(Class<? extends DataProcessor<TaggedImage>> processorClass, String name);

   /**
    * Get a sorted list of registered DataProcessor names.
    */
   List<String> getSortedDataProcessorNames();

   /**
    * Given a DataProcessor name (see above), create a new DataProcessor
    * and add it to the image processor pipeline.
    */
   DataProcessor<TaggedImage> makeProcessor(String Name, ScriptInterface gui);

   /**
    * Return the first DataProcessor in the pipeline registered under the 
    * given name, or null if there is none.
    */
   DataProcessor<TaggedImage> getProcessorRegisteredAs(String name);

   /**
    * Return the String under which the given DataProcessor type is 
    * registered, or null if there is none.
    */
   String getNameForProcessorClass(Class<? extends DataProcessor<TaggedImage>> processor);

   /**
    * Dispose of the GUIs generated by all DataProcessors we know about.
    */
   void disposeProcessors();

   /*
    * Returns true if abortRequest() has been called -- the acquisition may
    * still be running.
    */
   boolean abortRequested();

   /*
    * Returns a time (in milliseconds) indicating when the next image is
    * expected to be acquired.
    */
   long getNextWakeTime();

   /*
    * Returns true if the acquisition has finished running and no more hardware
    * events will be run.
    */
   boolean isFinished();
   
   /*
    * Attach a runnable to the acquisition engine. Each index (f, p, c, s) can
    * be specified. Passing a value of -1 should result in the runnable being attached
    * at all values of that index. For example, if the first argument is -1,
    * then the runnable should execute at every frame.
    *
    * Subject to change.
    */
   void attachRunnable(int frame, int position, int channel, int slice, Runnable runnable);


   /*
    * Remove runnables from the acquisition engine
    */
   void clearRunnables();

   /*
    * Get the summary metadata for the most recent acquisition.
    */
   JSONObject getSummaryMetadata();

    /*
     * Get the image cache for the most recent acquisition.
     */
    ImageCache getImageCache();

    List<DataProcessor<TaggedImage>> getImageProcessors();

    String getComment();

    void addSettingsListener(AcqSettingsListener listener);

    void removeSettingsListener(AcqSettingsListener listener);
    
    boolean getZAbsoluteMode();
}
