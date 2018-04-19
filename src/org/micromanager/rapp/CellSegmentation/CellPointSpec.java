package org.micromanager.rapp.CellSegmentation;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.awt.*;

public class CellPointSpec {
    public static final String DEFAULT_CHANNEL_GROUP = "Channel";
    public static final double Version = 1.0D;
    public double x_position = 0.0;
    public double y_position = 0.0;
    public String cellToKill = "";
    public double exposure = 10.0D;
    public Color color;
    public org.micromanager.utils.ContrastSettings contrast;
    public boolean useChannel;
    public String camera;

    public CellPointSpec() {
        this.color = Color.gray;
        this.useChannel = true;
        this.camera = "";
        this.contrast = new org.micromanager.utils.ContrastSettings(0, 65535);
        this.color = Color.WHITE;
    }

    public static String toJSONStream(CellPointSpec cs) {
        Gson gson = (new GsonBuilder()).setPrettyPrinting().create();
        return gson.toJson(cs);
    }

    public static CellPointSpec fromJSONStream(String stream) {
        Gson gson = new Gson();
        CellPointSpec cs = (CellPointSpec)gson.fromJson(stream, CellPointSpec.class);
        return cs;
    }

    public static synchronized void main(String[] args) {
        CellPointSpec cs = new CellPointSpec();
        String stream = toJSONStream(cs);
        System.out.println("Encoded:\n" + stream);
        CellPointSpec resultCs = fromJSONStream(stream);
        System.out.println("Decoded:\n" + toJSONStream(resultCs));
    }
}
