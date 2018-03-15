//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.micromanager.rapp.SequenceAcquisition;

import ij.gui.ImageWindow;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Set;
import java.util.prefs.Preferences;
import mmcorej.TaggedImage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.micromanager.api.ImageCache;
import org.micromanager.utils.GUIUtils;
import org.micromanager.utils.MDUtils;
import org.micromanager.utils.MMScriptException;
import org.micromanager.utils.ReportingUtils;

public class AcquisitionManager {
   private static final String ALBUM_WIN_X = "album_x";
   private static final String ALBUM_WIN_Y = "album_y";
   Hashtable<String, MMAcquisition> acqs_ = new Hashtable();
   private String album_ = null;

   public AcquisitionManager() {
   }

   public void openAcquisition(String name, String rootDir) throws MMScriptException {
      if (this.acquisitionExists(name)) {
         throw new MMScriptException("The name is in use");
      } else {
         MMAcquisition acq = new MMAcquisition(name, rootDir);
         this.acqs_.put(name, acq);
      }
   }

   public void openAcquisition(String name, String rootDir, boolean show) throws MMScriptException {
      this.openAcquisition(name, rootDir, show, false);
   }

   public void openAcquisition(String name, String rootDir, boolean show, boolean diskCached) throws MMScriptException {
      this.openAcquisition(name, rootDir, show, diskCached, false);
   }

   public void openAcquisition(String name, String rootDir, boolean show, boolean diskCached, boolean existing) throws MMScriptException {
      if (this.acquisitionExists(name)) {
         throw new MMScriptException("The name is in use");
      } else {
         this.acqs_.put(name, new MMAcquisition(name, rootDir, show, diskCached, existing));
      }
   }

   public void closeAcquisition(final String name) throws MMScriptException {
      if (name != null) {
         final MMScriptException[] ex = new MMScriptException[]{null};

         try {
            GUIUtils.invokeAndWait(new Runnable() {
               public void run() {
                  if (!AcquisitionManager.this.acqs_.containsKey(name)) {
                     ex[0] = new MMScriptException("The acquisition named \"" + name + "\" does not exist");
                  } else {
                     ((MMAcquisition)AcquisitionManager.this.acqs_.get(name)).close();
                     AcquisitionManager.this.acqs_.remove(name);
                  }

               }
            });
            if (ex[0] != null) {
               throw ex[0];
            }
         } catch (Exception var4) {
            ReportingUtils.showError(var4);
         }

      }
   }

   public boolean closeImageWindow(String name) throws MMScriptException {
      if (!this.acquisitionExists(name)) {
         throw new MMScriptException("The name does not exist");
      } else {
         return ((MMAcquisition)this.acqs_.get(name)).closeImageWindow();
      }
   }

   public boolean closeAllImageWindows() throws MMScriptException {
      String[] acqNames = this.getAcquisitionNames();
      String[] arr$ = acqNames;
      int len$ = acqNames.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         String acqName = arr$[i$];
         if (!this.closeImageWindow(acqName)) {
            return false;
         }
      }

      return true;
   }

   public boolean acquisitionExists(String name) {
      if (this.acqs_.containsKey(name)) {
         MMAcquisition acq = (MMAcquisition)this.acqs_.get(name);
         if (acq.getShow() && acq.windowClosed()) {
            acq.close();
            this.acqs_.remove(name);
            return false;
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   public MMAcquisition getAcquisition(String name) throws MMScriptException {
      if (this.acquisitionExists(name)) {
         return (MMAcquisition)this.acqs_.get(name);
      } else {
         throw new MMScriptException("Undefined acquisition name: " + name);
      }
   }

   public void closeAll() {
      Enumeration e = this.acqs_.elements();

      while(e.hasMoreElements()) {
         ((MMAcquisition)e.nextElement()).close();
      }

      this.acqs_.clear();
   }

   public String getUniqueAcquisitionName(String name) {
      char separator = '_';

      while(this.acquisitionExists(name)) {
         int lastSeparator = name.lastIndexOf(separator);
         if (lastSeparator == -1) {
            name = name + separator + "1";
         } else {
            try {
               Integer i = Integer.parseInt(name.substring(lastSeparator + 1));
               i = i + 1;
               name = name.substring(0, lastSeparator) + separator + i;
            } catch (NumberFormatException var7) {
               name = name + separator + "1";
            }
         }
      }

      return name;
   }

   public String getCurrentAlbum() {
      return this.album_ == null ? this.createNewAlbum() : this.album_;
   }

   public String createNewAlbum() {
      this.album_ = this.getUniqueAcquisitionName("Album");
      return this.album_;
   }

   public String addToAlbum(TaggedImage image, JSONObject displaySettings) throws MMScriptException {
      boolean newNeeded = true;
      MMAcquisition acq = null;
      String album = this.getCurrentAlbum();
      JSONObject tags = image.tags;

      int imageWidth;
      int imageHeight;
      int imageDepth;
      int imageBitDepth;
      int numChannels;
      try {
         imageWidth = MDUtils.getWidth(tags);
         imageHeight = MDUtils.getHeight(tags);
         imageDepth = MDUtils.getDepth(tags);
         imageBitDepth = MDUtils.getBitDepth(tags);
         numChannels = MDUtils.getNumChannels(tags);
      } catch (Exception var22) {
         throw new MMScriptException("Something wrong with image tags.");
      }

      if (this.acquisitionExists(album)) {
         acq = (MMAcquisition)this.acqs_.get(album);

         try {
            if (acq.getWidth() == imageWidth && acq.getHeight() == imageHeight && acq.getByteDepth() == imageDepth && acq.getMultiCameraNumChannels() == numChannels && !acq.getImageCache().isFinished()) {
               newNeeded = false;
            }
         } catch (Exception var21) {
            ReportingUtils.logError(var21, "Couldn't check if we need a new album");
         }
      }

      JSONObject lastTags;
      if (newNeeded) {
         album = this.createNewAlbum();
         this.openAcquisition(album, "", true, false);
         acq = this.getAcquisition(album);
         boolean mustHackDims = false;

         try {
            mustHackDims = numChannels > 1 || MDUtils.getNumberOfComponents(image.tags) > 1;
         } catch (JSONException var23) {
            ReportingUtils.logError(var23, "Unable to determine number of components of image");
         }

         acq.setDimensions(mustHackDims ? 2 : 1, numChannels, 1, 1);
         acq.setImagePhysicalDimensions(imageWidth, imageHeight, imageDepth, imageBitDepth, numChannels);

         try {
            lastTags = new JSONObject();
            MDUtils.setPixelTypeFromString(lastTags, MDUtils.getPixelType(tags));
            acq.setSummaryProperties(lastTags);
         } catch (JSONException var20) {
            ReportingUtils.logError(var20);
         }

         acq.initialize();
         final ImageWindow win = acq.getAcquisitionWindow().getImagePlus().getWindow();
         final Preferences prefs = Preferences.userNodeForPackage(this.getClass());
         win.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
               Point loc = win.getLocation();
               prefs.putInt("album_x", loc.x);
               prefs.putInt("album_y", loc.y);
            }
         });
         win.setLocation(prefs.getInt("album_x", 0), prefs.getInt("album_y", 0));
      }

      int newImageFrame = acq.getLastAcquiredFrame() + 1;
      if (numChannels > 1) {
         try {
            lastTags = acq.getImageCache().getLastImageTags();
            int lastCh = -1;
            if (lastTags != null) {
               lastCh = MDUtils.getChannelIndex(lastTags);
            }

            if (lastCh == 0) {
               newImageFrame = acq.getLastAcquiredFrame();
            }
         } catch (JSONException var19) {
            ReportingUtils.logError(var19);
         }
      }

      try {
         MDUtils.setFrameIndex(tags, newImageFrame);
      } catch (JSONException var18) {
         ReportingUtils.showError(var18);
      }

      try {
         acq.getSummaryMetadata().put("Frames", newImageFrame + 1);
      } catch (JSONException var17) {
         ReportingUtils.logError("Couldn't update number of frames in album summary metadata");
      }

      acq.insertImage(image);
      if (numChannels == 1) {
         try {
            if (MDUtils.getFrameIndex(tags) == 0 && displaySettings != null) {
               this.copyDisplaySettings(acq, displaySettings);
            }
         } catch (JSONException var16) {
            ReportingUtils.logError(var16);
         }
      } else {
         try {
            if (numChannels > 1 && MDUtils.getChannelIndex(tags) == numChannels - 1 && acq.getLastAcquiredFrame() == 0 && displaySettings != null) {
               this.copyDisplaySettings(acq, displaySettings);
            }
         } catch (JSONException var15) {
            ReportingUtils.logError(var15);
         }
      }

      return album;
   }

   private void copyDisplaySettings(MMAcquisition acq, JSONObject displaySettings) {
      if (displaySettings != null) {
         ImageCache ic = (ImageCache) acq.getImageCache();

         for(int i = 0; i < ic.getNumDisplayChannels(); ++i) {
            try {
               JSONObject channelSetting = (JSONObject)((JSONArray)displaySettings.get("Channels")).get(i);
               int color = channelSetting.getInt("Color");
               int min = channelSetting.getInt("Min");
               int max = channelSetting.getInt("Max");
               double gamma = channelSetting.getDouble("Gamma");
               String name = channelSetting.getString("Name");
               int histMax;
               if (channelSetting.has("HistogramMax")) {
                  histMax = channelSetting.getInt("HistogramMax");
               } else {
                  histMax = -1;
               }

               int displayMode = 1;
               if (channelSetting.has("DisplayMode")) {
                  displayMode = channelSetting.getInt("DisplayMode");
               }

               ic.storeChannelDisplaySettings(i, min, max, gamma, histMax, displayMode);
               acq.getAcquisitionWindow().setChannelHistogramDisplayMax(i, histMax);
               acq.getAcquisitionWindow().setChannelContrast(i, min, max, gamma);
               acq.getAcquisitionWindow().setDisplayMode(displayMode);
               acq.setChannelColor(i, color);
               acq.setChannelName(i, name);
            } catch (JSONException var14) {
               ReportingUtils.logError("Something wrong with Display and Comments");
            } catch (MMScriptException var15) {
               ReportingUtils.logError(var15);
            }
         }

      }
   }

   public String[] getAcquisitionNames() {
      Set<String> keySet = this.acqs_.keySet();
      String[] keys = new String[keySet.size()];
      return (String[])keySet.toArray(keys);
   }

   public String createAcquisition(JSONObject summaryMetadata, boolean diskCached, AcquisitionEngine engine, boolean displayOff) {
      String name = this.getUniqueAcquisitionName("Acq");
      this.acqs_.put(name, new MMAcquisition(name, summaryMetadata, diskCached, engine, !displayOff));
      return name;
   }
}
