package org.micromanager.rapp.SequenceAcquisition;


import java.io.IOException;
import java.util.Set;
import mmcorej.TaggedImage;
import org.json.JSONObject;
import org.micromanager.utils.MMException;

public interface TaggedImageStorage  {
    String storagePluginName = null;

    TaggedImage getImage(int var1, int var2, int var3, int var4);

    JSONObject getImageTags(int var1, int var2, int var3, int var4);

    void putImage(TaggedImage var1) throws MMException, IOException;

    Set<String> imageKeys();

    void finished();

    boolean isFinished();

    void setSummaryMetadata(JSONObject var1);

    JSONObject getSummaryMetadata();

    void setDisplayAndComments(JSONObject var1);

    JSONObject getDisplayAndComments();

    void close();

    String getDiskLocation();

    int lastAcquiredFrame();

    long getDataSetSize();

    void writeDisplaySettings();
}
