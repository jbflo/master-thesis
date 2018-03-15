//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.micromanager.rapp.SequenceAcquisition;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.SwingUtilities;

import com.sun.javafx.iio.ImageStorage;
import mmcorej.TaggedImage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.micromanager.api.ImageCache;
import org.micromanager.api.ImageCacheListener;
import org.micromanager.api.TaggedImageStorage;
import org.micromanager.utils.MDUtils;
import org.micromanager.utils.MMException;
import org.micromanager.utils.MMScriptException;
import org.micromanager.utils.ProgressBar;
import org.micromanager.utils.ReportingUtils;

public class MMImageCache implements ImageCache {
   public final List<ImageCacheListener> imageStorageListeners_ = Collections.synchronizedList(new ArrayList());
   private TaggedImageStorage imageStorage_;
   private Set<String> changingKeys_;
   private JSONObject firstTags_;
   private int lastFrame_ = -1;
   private JSONObject lastTags_;
   private final ExecutorService listenerExecutor_;

   public void addImageCacheListener(ImageCacheListener l) {
      List var2 = this.imageStorageListeners_;
      synchronized(this.imageStorageListeners_) {
         this.imageStorageListeners_.add(l);
      }
   }

   public ImageCacheListener[] getImageCacheListeners() {
      List var1 = this.imageStorageListeners_;
      synchronized(this.imageStorageListeners_) {
         return (ImageCacheListener[])((ImageCacheListener[])this.imageStorageListeners_.toArray());
      }
   }

   public void removeImageCacheListener(ImageCacheListener l) {
      List var2 = this.imageStorageListeners_;
      synchronized(this.imageStorageListeners_) {
         this.imageStorageListeners_.remove(l);
      }
   }

   public MMImageCache(TaggedImageStorage imageStorage) {
      this.imageStorage_ = imageStorage;
      this.changingKeys_ = new HashSet();
      this.listenerExecutor_ = Executors.newFixedThreadPool(1);
   }

   public void finished() {
      this.imageStorage_.finished();
      String path = this.getDiskLocation();
      List var2 = this.imageStorageListeners_;
      synchronized(this.imageStorageListeners_) {
         Iterator i$ = this.imageStorageListeners_.iterator();

         while(true) {
            if (!i$.hasNext()) {
               break;
            }

            ImageCacheListener l = (ImageCacheListener)i$.next();
            l.imagingFinished(path);
            System.out.println(path);
            System.out.println(l);
         }
      }

      this.listenerExecutor_.shutdown();
   }

   public boolean isFinished() {
      return this.imageStorage_.isFinished();
   }

   public int lastAcquiredFrame() {
      synchronized(this) {
         this.lastFrame_ = Math.max(this.imageStorage_.lastAcquiredFrame(), this.lastFrame_);
         return this.lastFrame_;
      }
   }

   public String getDiskLocation() {
      return this.imageStorage_.getDiskLocation();
   }

   public void setDisplayAndComments(JSONObject settings) {
      this.imageStorage_.setDisplayAndComments(settings);
   }

   public JSONObject getDisplayAndComments() {
      return this.imageStorage_.getDisplayAndComments();
   }

   public void writeDisplaySettings() {
      this.imageStorage_.writeDisplaySettings();
   }

   public void close() {
      this.imageStorage_.close();
      List var1 = this.imageStorageListeners_;
      synchronized(this.imageStorageListeners_) {
         this.imageStorageListeners_.clear();
      }
   }

   public void saveAs(TaggedImageStorage newImageFileManager) {
      this.saveAs(newImageFileManager, true);
      this.finished();
   }

   public void saveAs(TaggedImageStorage newImageFileManager, boolean useNewStorage) {
      if (newImageFileManager != null) {
         newImageFileManager.setSummaryMetadata(this.imageStorage_.getSummaryMetadata());
         newImageFileManager.setDisplayAndComments(this.getDisplayAndComments());
         String progressBarTitle = newImageFileManager instanceof TaggedImageStorageRamFast ? "Loading images..." : "Saving images...";
         final ProgressBar progressBar = new ProgressBar(progressBarTitle, 0, 100);
         ArrayList<String> keys = new ArrayList(this.imageKeys());
         int n = keys.size();
         progressBar.setRange(0, n);
         progressBar.setProgress(0);
         progressBar.setVisible(true);
         boolean wasSuccessful = true;

       //  for(final int i = 0; i < n; ++i) {
         for(int i = 0; i < n; ++i) {
            int[] pos = MDUtils.getIndices((String)keys.get(i));

            try {
               newImageFileManager.putImage(this.getImage(pos[0], pos[1], pos[2], pos[3]));
            } catch (MMException var12) {
               ReportingUtils.logError(var12);
            } catch (IOException var13) {
               ReportingUtils.showError(var13, "Unable to write image " + i);
               wasSuccessful = false;
               break;
            }

            int finalI = i;
            SwingUtilities.invokeLater(new Runnable() {
               public void run() {
                  progressBar.setProgress(finalI);
               }
            });
         }

         if (wasSuccessful) {
            newImageFileManager.finished();
         }

         progressBar.setVisible(false);
         if (useNewStorage) {
            this.imageStorage_ = newImageFileManager;
         }

      }
   }

   public void putImage(final TaggedImage taggedImg) {
      try {
         this.checkForChangingTags(taggedImg);
         this.imageStorage_.putImage(taggedImg);
         synchronized(this) {
            this.lastFrame_ = Math.max(this.lastFrame_, MDUtils.getFrameIndex(taggedImg.tags));
            this.lastTags_ = taggedImg.tags;
         }

         JSONObject displayAndComments = this.imageStorage_.getDisplayAndComments();
         if (displayAndComments.length() > 0) {
            JSONArray channelSettings = this.imageStorage_.getDisplayAndComments().getJSONArray("Channels");
            JSONObject imageTags = taggedImg.tags;
            int chanIndex = MDUtils.getChannelIndex(imageTags);
            if (chanIndex >= channelSettings.length()) {
               JSONObject newChanObject = new JSONObject();
               MDUtils.setChannelName(newChanObject, MDUtils.getChannelName(imageTags));
               MDUtils.setChannelColor(newChanObject, MDUtils.getChannelColor(imageTags));
               channelSettings.put(chanIndex, newChanObject);
            }
         }

         List var11 = this.imageStorageListeners_;
         synchronized(this.imageStorageListeners_) {
            Iterator i$ = this.imageStorageListeners_.iterator();

            while(i$.hasNext()) {
               final ImageCacheListener l = (ImageCacheListener)i$.next();
               this.listenerExecutor_.submit(new Runnable() {
                  public void run() {
                     l.imageReceived(taggedImg);
                  }
               });
            }
         }
      } catch (Exception var10) {
         ReportingUtils.logError(var10);
      }

   }

   public JSONObject getLastImageTags() {
      synchronized(this) {
         return this.lastTags_;
      }
   }

   public TaggedImage getImage(int channel, int slice, int frame, int position) {
      TaggedImage taggedImg = null;
      if (taggedImg == null) {
         taggedImg = this.imageStorage_.getImage(channel, slice, frame, position);
         if (taggedImg != null) {
            this.checkForChangingTags(taggedImg);
         }
      }

      return taggedImg;
   }

   public JSONObject getImageTags(int channel, int slice, int frame, int position) {
      MDUtils.generateLabel(channel, slice, frame, position);
      JSONObject tags = null;
      if (tags == null) {
         tags = this.imageStorage_.getImageTags(channel, slice, frame, position);
      }

      return tags;
   }

   private void checkForChangingTags(TaggedImage taggedImg) {
      if (this.firstTags_ == null) {
         this.firstTags_ = taggedImg.tags;
      } else {
         Iterator keys = taggedImg.tags.keys();

         while(keys.hasNext()) {
            String key = (String)keys.next();

            try {
               if (!taggedImg.tags.isNull(key)) {
                  if (this.firstTags_.has(key) && !this.firstTags_.isNull(key)) {
                     if (!taggedImg.tags.getString(key).contentEquals(this.firstTags_.getString(key))) {
                        this.changingKeys_.add(key);
                     }
                  } else {
                     this.changingKeys_.add(key);
                  }
               }
            } catch (Exception var5) {
               ReportingUtils.logError(var5);
            }
         }
      }

   }

   private JSONObject getCommentsJSONObject() {
      if (this.imageStorage_ == null) {
         ReportingUtils.logError("imageStorage_ is null in getCommentsJSONObject");
         return null;
      } else {
         JSONObject comments;
         try {
            comments = this.imageStorage_.getDisplayAndComments().getJSONObject("Comments");
         } catch (JSONException var5) {
            comments = new JSONObject();

            try {
               this.imageStorage_.getDisplayAndComments().put("Comments", comments);
            } catch (JSONException var4) {
               ReportingUtils.logError(var4);
            }
         }

         return comments;
      }
   }

   public boolean getIsOpen() {
      return this.getDisplayAndComments() != null;
   }

   public void setComment(String text) {
      JSONObject comments = this.getCommentsJSONObject();

      try {
         comments.put("Summary", text);
      } catch (JSONException var4) {
         ReportingUtils.logError(var4);
      }

   }

   public void setImageComment(String comment, JSONObject tags) {
      JSONObject comments = this.getCommentsJSONObject();
      String label = MDUtils.getLabel(tags);

      try {
         comments.put(label, comment);
      } catch (JSONException var6) {
         ReportingUtils.logError(var6);
      }

   }

   public String getImageComment(JSONObject tags) {
      if (tags == null) {
         return "";
      } else {
         try {
            String label = MDUtils.getLabel(tags);
            return this.getCommentsJSONObject().getString(label);
         } catch (Exception var3) {
            return "";
         }
      }
   }

   public String getComment() {
      try {
         return this.getCommentsJSONObject().getString("Summary");
      } catch (Exception var2) {
         return "";
      }
   }

   public JSONObject getSummaryMetadata() {
      if (this.imageStorage_ == null) {
         ReportingUtils.logError("imageStorage_ is null in getSummaryMetadata");
         return null;
      } else {
         return this.imageStorage_.getSummaryMetadata();
      }
   }

   public void setSummaryMetadata(JSONObject tags) {
      if (this.imageStorage_ == null) {
         ReportingUtils.logError("imageStorage_ is null in setSummaryMetadata");
      } else {
         this.imageStorage_.setSummaryMetadata(tags);
      }
   }

   public Set<String> getChangingKeys() {
      return this.changingKeys_;
   }

   public Set<String> imageKeys() {
      return this.imageStorage_.imageKeys();
   }

   private boolean isRGB() throws JSONException, MMScriptException {
      return MDUtils.isRGB(this.getSummaryMetadata());
   }

   public String getPixelType() {
      try {
         return MDUtils.getPixelType(this.getSummaryMetadata());
      } catch (Exception var2) {
         ReportingUtils.logError(var2);
         return null;
      }
   }

   public void storeChannelDisplaySettings(int channelIndex, int min, int max, double gamma, int histMax, int displayMode) {
      try {
         JSONObject settings = this.getChannelSetting(channelIndex);
         settings.put("Max", max);
         settings.put("Min", min);
         settings.put("Gamma", gamma);
         settings.put("HistogramMax", histMax);
         settings.put("DisplayMode", displayMode);
      } catch (Exception var9) {
         ReportingUtils.logError(var9);
      }

   }

   public JSONObject getChannelSetting(int channel) {
      try {
         JSONArray array = this.getDisplayAndComments().getJSONArray("Channels");
         if (channel >= array.length()) {
            array.put(channel, new JSONObject(array.getJSONObject(0).toString()));
         }

         return !array.isNull(channel) ? array.getJSONObject(channel) : null;
      } catch (Exception var3) {
         ReportingUtils.logError(var3);
         return null;
      }
   }

   public int getBitDepth() {
      try {
         return this.imageStorage_.getSummaryMetadata().getInt("BitDepth");
      } catch (JSONException var2) {
         ReportingUtils.logError("MMImageCache.BitDepth: no tag BitDepth found");
         return 16;
      }
   }

   public Color getChannelColor(int channelIndex) {
      try {
         if (this.isRGB()) {
            return channelIndex == 0 ? Color.red : (channelIndex == 1 ? Color.green : Color.blue);
         } else {
            return new Color(this.getChannelSetting(channelIndex).getInt("Color"));
         }
      } catch (Exception var3) {
         return Color.WHITE;
      }
   }

   public void setChannelColor(int channel, int rgb) {
      JSONObject chan = this.getChannelSetting(channel);

      try {
         if (chan == null) {
            return;
         }

         chan.put("Color", rgb);
      } catch (JSONException var5) {
         ReportingUtils.logError(var5);
      }

   }

   public String getChannelName(int channelIndex) {
      try {
         if (this.isRGB()) {
            return channelIndex == 0 ? "Red" : (channelIndex == 1 ? "Green" : "Blue");
         } else {
            JSONObject channelSetting = this.getChannelSetting(channelIndex);
            return channelSetting.has("Name") ? channelSetting.getString("Name") : "";
         }
      } catch (Exception var3) {
         ReportingUtils.logError(var3);
         return "";
      }
   }

   public void setChannelName(int channel, String channelName) {
      try {
         if (this.isRGB()) {
            return;
         }

         JSONObject displayAndComments = this.getDisplayAndComments();
         JSONArray channelArray;
         if (displayAndComments.has("Channels")) {
            channelArray = displayAndComments.getJSONArray("Channels");
         } else {
            channelArray = new JSONArray();
            displayAndComments.put("Channels", channelArray);
         }

         if (channelArray.isNull(channel)) {
            channelArray.put(channel, (new JSONObject()).put("Name", channelName));
         }
      } catch (Exception var5) {
         ReportingUtils.logError(var5);
      }

   }

   public int getDisplayMode() {
      try {
         return this.getChannelSetting(0).getInt("DisplayMode");
      } catch (JSONException var2) {
         return 1;
      }
   }

   public int getChannelMin(int channelIndex) {
      try {
         return this.getChannelSetting(channelIndex).getInt("Min");
      } catch (Exception var3) {
         return 0;
      }
   }

   public int getChannelMax(int channelIndex) {
      try {
         return this.getChannelSetting(channelIndex).getInt("Max");
      } catch (Exception var3) {
         return -1;
      }
   }

   public double getChannelGamma(int channelIndex) {
      try {
         return this.getChannelSetting(channelIndex).getDouble("Gamma");
      } catch (Exception var3) {
         return 1.0D;
      }
   }

   public int getChannelHistogramMax(int channelIndex) {
      try {
         return this.getChannelSetting(channelIndex).getInt("HistogramMax");
      } catch (JSONException var3) {
         return -1;
      }
   }

   public int getNumDisplayChannels() {
      JSONArray array;
      try {
         array = this.getDisplayAndComments().getJSONArray("Channels");
      } catch (Exception var3) {
         return 1;
      }

      return array.length();
   }

   public long getDataSetSize() {
      throw new UnsupportedOperationException("Not supported yet.");
   }
}
