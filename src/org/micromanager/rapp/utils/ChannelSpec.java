package org.micromanager.rapp.utils;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.awt.Color;

public class ChannelSpec {
    public static final String DEFAULT_CHANNEL_GROUP = "Channel";
    public static final double Version = 1.0D;
   // public Boolean doZStack = true;
    public String config = "";
    public double exposure = 10.0D;
    //public double zOffset = 0.0D;
    public Color color;
    public org.micromanager.utils.ContrastSettings contrast;
  //  public int skipFactorFrame;
    public boolean useChannel;
    public String camera;

    public ChannelSpec() {
        this.color = Color.gray;
    //     this.skipFactorFrame = 0;
        this.useChannel = true;
        this.camera = "";
        this.contrast = new org.micromanager.utils.ContrastSettings(0, 65535);
        this.color = Color.WHITE;
    }

    public static String toJSONStream(ChannelSpec cs) {
        Gson gson = (new GsonBuilder()).setPrettyPrinting().create();
        return gson.toJson(cs);
    }

    public static ChannelSpec fromJSONStream(String stream) {
        Gson gson = new Gson();
        ChannelSpec cs = (ChannelSpec)gson.fromJson(stream, ChannelSpec.class);
        return cs;
    }

    public static synchronized void main(String[] args) {
        ChannelSpec cs = new ChannelSpec();
        String stream = toJSONStream(cs);
        System.out.println("Encoded:\n" + stream);
        ChannelSpec resultCs = fromJSONStream(stream);
        System.out.println("Decoded:\n" + toJSONStream(resultCs));
    }
}
