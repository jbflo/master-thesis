package org.micromanager.rapp.utils;

import mmcorej.TaggedImage;

public interface ImageCacheListener {
    void imageReceived(TaggedImage var1);

    void imagingFinished(String var1);
}
