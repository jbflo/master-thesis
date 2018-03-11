package org.micromanager.rapp.utils;


import java.awt.Color;
import java.util.Set;
import mmcorej.TaggedImage;
import org.json.JSONObject;
import org.micromanager.api.TaggedImageStorage;

public interface ImageCache extends TaggedImageStorage {
    void addImageCacheListener(ImageCacheListener var1);

    Set<String> getChangingKeys();

    String getComment();

    ImageCacheListener[] getImageCacheListeners();

    JSONObject getLastImageTags();

    void removeImageCacheListener(ImageCacheListener var1);

    void saveAs(TaggedImageStorage var1);

    void saveAs(TaggedImageStorage var1, boolean var2);

    boolean getIsOpen();

    void setComment(String var1);

    void setImageComment(String var1, JSONObject var2);

    String getImageComment(JSONObject var1);

    void storeChannelDisplaySettings(int var1, int var2, int var3, double var4, int var6, int var7);

    JSONObject getChannelSetting(int var1);

    int getBitDepth();

    int getDisplayMode();

    Color getChannelColor(int var1);

    void setChannelColor(int var1, int var2);

    String getChannelName(int var1);

    int getChannelMin(int var1);

    int getChannelMax(int var1);

    double getChannelGamma(int var1);

    int getChannelHistogramMax(int var1);

    int getNumDisplayChannels();

    String getPixelType();

    TaggedImage getImage(int var1, int var2, int var3, int var4);
}
