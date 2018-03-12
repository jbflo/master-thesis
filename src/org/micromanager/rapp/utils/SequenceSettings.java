package org.micromanager.rapp.utils;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.sound.midi.Sequence;
import java.util.ArrayList;
//import org.micromanager.utils.ChannelSpec;

public class SequenceSettings extends org.micromanager.api.SequenceSettings {
    public static final double Version = 1.0D;
    public int numFrames = 1;
    public double intervalMs = 0.0D;
    public ArrayList<Double> customIntervalsMs = null;
    public ArrayList<ChannelSpec> channels = new ArrayList();
    public ArrayList<Double> slices = new ArrayList();
    public boolean relativeZSlice = false;
    public boolean slicesFirst = false;
    public boolean timeFirst = false;
    public boolean keepShutterOpenSlices = false;
    public boolean keepShutterOpenChannels = false;
    public boolean useAutofocus = false;
    public int skipAutofocusCount = 0;
    public boolean save = false;
    public String root = null;
    public String prefix = null;
    public double zReference = 0.0D;
    public String comment = "";
    public String channelGroup = "";
    public boolean usePositionList = false;

    public SequenceSettings() {
    }

    public static String toJSONStream(SequenceSettings settings) {
        Gson gson = (new GsonBuilder()).setPrettyPrinting().create();
        return gson.toJson(settings);
    }

    public static SequenceSettings fromJSONStream(String stream) {
        Gson gson = new Gson();
        return (SequenceSettings)gson.fromJson(stream, SequenceSettings.class);
    }

    public static synchronized void main(String[] args) {
        SequenceSettings s = new SequenceSettings();
        String channelGroup = "Channel";
        s.numFrames = 20;
        s.slices = new ArrayList();
        s.slices.add(-1.0D);
        s.slices.add(0.0D);
        s.slices.add(1.0D);
        s.relativeZSlice = true;
        s.channels = new ArrayList();
        ChannelSpec ch1 = new ChannelSpec();
        ch1.config = "DAPI";
        ch1.exposure = 5.0D;
        s.channels.add(ch1);
        ChannelSpec ch2 = new ChannelSpec();
        ch2.config = "FITC";
        ch2.exposure = 15.0D;
        s.channels.add(ch2);
        s.prefix = "ACQ-TEST-B";
        s.root = "C:/AcquisitionData";
        s.channelGroup = channelGroup;
        String stream = toJSONStream(s);
        System.out.println("Encoded:\n" + stream);
        SequenceSettings resultSs = fromJSONStream(stream);
        System.out.println("Decoded:\n" + toJSONStream(resultSs));
    }
}
