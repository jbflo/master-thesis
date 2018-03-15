//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.micromanager.rapp.SequenceAcquisition;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import mmcorej.TaggedImage;
import org.json.JSONException;
import org.json.JSONObject;
import org.micromanager.api.TaggedImageStorage;
import org.micromanager.utils.DirectBuffers;
import org.micromanager.utils.ImageLabelComparator;
import org.micromanager.utils.MDUtils;
import org.micromanager.utils.MMException;
import org.micromanager.utils.MMScriptException;
import org.micromanager.utils.ReportingUtils;

public class TaggedImageStorageRamFast implements TaggedImageStorage {
   private boolean finished_ = false;
   private TreeMap<String, TaggedImageStorageRamFast.DirectTaggedImage> imageMap_ = new TreeMap(new ImageLabelComparator());
   private TaggedImageStorageRamFast.LRUCache<String, TaggedImage> lruCache_;
   private JSONObject summaryMetadata_;
   private JSONObject displaySettings_;
   private int lastFrame_ = -1;
   private String diskLocation_;

   public TaggedImageStorageRamFast(JSONObject summaryMetadata) {
      this.setSummaryMetadata(summaryMetadata);
      this.displaySettings_ = new JSONObject();
      this.lruCache_ = new TaggedImageStorageRamFast.LRUCache(10L);
   }

   private ByteBuffer bufferFromJSON(JSONObject json) {
      return DirectBuffers.bufferFromString(json.toString());
   }

   private JSONObject JSONFromBuffer(ByteBuffer byteBuffer) throws JSONException {
      return new JSONObject(DirectBuffers.stringFromBuffer(byteBuffer));
   }

   private TaggedImageStorageRamFast.DirectTaggedImage taggedImageToDirectTaggedImage(TaggedImage taggedImage) throws JSONException, MMScriptException {
      TaggedImageStorageRamFast.DirectTaggedImage direct = new TaggedImageStorageRamFast.DirectTaggedImage();
      direct.tagsBuffer = this.bufferFromJSON(taggedImage.tags);
      direct.pixelBuffer = DirectBuffers.bufferFromArray(taggedImage.pix);
      return direct;
   }

   private TaggedImage directTaggedImageToTaggedImage(TaggedImageStorageRamFast.DirectTaggedImage directImage) {
      if (directImage != null) {
         try {
            return new TaggedImage(DirectBuffers.arrayFromBuffer(directImage.pixelBuffer), this.JSONFromBuffer(directImage.tagsBuffer));
         } catch (JSONException var3) {
            ReportingUtils.logError(var3);
            return null;
         }
      } else {
         return null;
      }
   }

   public void putImage(TaggedImage taggedImage) throws MMException {
      String label = MDUtils.getLabel(taggedImage.tags);

      try {
         TaggedImageStorageRamFast.DirectTaggedImage directImage = this.taggedImageToDirectTaggedImage(taggedImage);
         this.lruCache_.put(label, taggedImage);
         this.imageMap_.put(label, directImage);
         this.lastFrame_ = Math.max(this.lastFrame_, MDUtils.getFrameIndex(taggedImage.tags));
      } catch (Exception var4) {
         ReportingUtils.logError(var4);
      }

   }

   public TaggedImage getImage(int channel, int slice, int frame, int position) {
      if (this.imageMap_ == null) {
         return null;
      } else {
         String label = MDUtils.generateLabel(channel, slice, frame, position);
         TaggedImage cachedImage = (TaggedImage)this.lruCache_.get(label);
         return cachedImage != null ? cachedImage : this.directTaggedImageToTaggedImage((TaggedImageStorageRamFast.DirectTaggedImage)this.imageMap_.get(label));
      }
   }

   public JSONObject getImageTags(int channelIndex, int sliceIndex, int frameIndex, int positionIndex) {
      return this.getImage(channelIndex, sliceIndex, frameIndex, positionIndex).tags;
   }

   public Set<String> imageKeys() {
      return this.imageMap_.keySet();
   }

   public void finished() {
      this.finished_ = true;
   }

   public boolean isFinished() {
      return this.finished_;
   }

   public final void setSummaryMetadata(JSONObject md) {
      this.summaryMetadata_ = md;
      if (this.summaryMetadata_ != null) {
         try {
            boolean slicesFirst = this.summaryMetadata_.getBoolean("SlicesFirst");
            boolean timeFirst = this.summaryMetadata_.getBoolean("TimeFirst");
            TreeMap<String, TaggedImageStorageRamFast.DirectTaggedImage> oldImageMap = this.imageMap_;
            this.imageMap_ = new TreeMap(new ImageLabelComparator(slicesFirst, timeFirst));
            this.imageMap_.putAll(oldImageMap);
         } catch (JSONException var5) {
            ReportingUtils.logError("Couldn't find SlicesFirst or TimeFirst in summary metadata");
         }
      }

   }

   public JSONObject getSummaryMetadata() {
      return this.summaryMetadata_;
   }

   public void setDisplayAndComments(JSONObject settings) {
      this.displaySettings_ = settings;
   }

   public JSONObject getDisplayAndComments() {
      return this.displaySettings_;
   }

   public void close() {
      this.imageMap_.clear();
      this.lruCache_.clear();
      this.summaryMetadata_ = null;
      this.displaySettings_ = null;
   }

   public void setDiskLocation(String diskLocation) {
      this.diskLocation_ = diskLocation;
   }

   public String getDiskLocation() {
      return this.diskLocation_;
   }

   public int lastAcquiredFrame() {
      return this.lastFrame_;
   }

   public long getDataSetSize() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void writeDisplaySettings() {
   }

   private class LRUCache<T, U> extends LinkedHashMap<T, U> {
      final long max_size_;

      LRUCache(long max_size) {
         this.max_size_ = max_size;
      }

      protected boolean removeEldestEntry(Entry eldest) {
         return (long)super.size() > this.max_size_;
      }
   }

   private class DirectTaggedImage {
      Buffer pixelBuffer;
      ByteBuffer tagsBuffer;

      private DirectTaggedImage() {
      }
   }
}
