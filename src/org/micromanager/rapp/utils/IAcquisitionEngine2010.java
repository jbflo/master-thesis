package org.micromanager.rapp.utils;

import java.util.concurrent.BlockingQueue;
import mmcorej.TaggedImage;
import org.json.JSONObject;
import org.micromanager.api.Autofocus;
import org.micromanager.api.PositionList;

public interface IAcquisitionEngine2010 {
    BlockingQueue<TaggedImage> run(SequenceSettings var1);

    BlockingQueue<TaggedImage> run(SequenceSettings var1, boolean var2, PositionList var3, Autofocus var4);

    BlockingQueue<TaggedImage> run(SequenceSettings var1, boolean var2);

    JSONObject getSummaryMetadata();

    void pause();

    void resume();

    void stop();

    boolean isRunning();

    boolean isPaused();

    boolean isFinished();

    boolean stopHasBeenRequested();

    long nextWakeTime();

    void attachRunnable(int var1, int var2, int var3, int var4, Runnable var5);

    void clearRunnables();
}
