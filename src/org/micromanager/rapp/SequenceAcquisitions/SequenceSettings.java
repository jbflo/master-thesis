package org.micromanager.rapp.SequenceAcquisitions;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.ArrayList;


public class SequenceSettings {
    public static final double Version = 1.0D;
    public double intervalMs = 0.0D;
    public ArrayList<ChannelSpec> channels = new ArrayList();
    public boolean keepShutterOpenChannels = false;
    public boolean useAutofocus = false;
    public int skipAutofocusCount = 0;
    public boolean save = false;
    public String root = null;
    public String prefix = null;
    public String comment = "";
    public String channelGroup = "";
    public boolean usePositionList = false;
    public boolean useSegmentation = false;
    public boolean killCell = false;
    public boolean imagePlate = false;
    public int numberOfXWells = 1;
    public int numberOfYWells = 1;
    public double wellDistance = 0;
    public double wellWidth = 0;
    public double fieldOfView = 220;
    public boolean useWholePlateImaging = false;

    public SequenceSettings() {

    }

    public static String toJSONStream(SequenceSettings settings) {
        Gson gson = (new GsonBuilder()).setPrettyPrinting().create();
        return gson.toJson(settings);
    }

    public static SequenceSettings fromJSONStream(String stream) {
        Gson gson = new Gson();
        return gson.fromJson(stream, SequenceSettings.class);
    }

    public static synchronized void main(String[] args) {
        SequenceSettings s = new SequenceSettings();
        String channelGroup = "Channel";
        s.channels = new ArrayList();
        ChannelSpec ch1 = new ChannelSpec();
        s.killCell = true;
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
