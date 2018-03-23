//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.micromanager.rapp.SequenceAcquisition;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import mmcorej.CMMCore;
import mmcorej.TaggedImage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.micromanager.MMStudio;
import org.micromanager.acquisition.*;
import org.micromanager.api.ImageCache;
import org.micromanager.acquisition.MMImageCache;
import org.micromanager.api.TaggedImageStorage;
//import org.micromanager.dialogs.AcqControlDlg;
import org.micromanager.imagedisplay.VirtualAcquisitionDisplay;
import org.micromanager.utils.ImageUtils;
import org.micromanager.utils.JavaUtils;
import org.micromanager.utils.MDUtils;
import org.micromanager.utils.MMException;
import org.micromanager.utils.MMScriptException;
import org.micromanager.utils.ReportingUtils;

public class MMAcquisition {
    public static final Color[] DEFAULT_COLORS;
    private BlockingQueue<TaggedImage> outputQueue_;
    private boolean isAsynchronous_;
    private int numFrames_;
    private int numChannels_;
    private int numSlices_;
    private int numPositions_;
    protected String name_;
    protected int width_;
    protected int height_;
    protected int byteDepth_;
    protected int bitDepth_;
    protected int multiCamNumCh_;
    private boolean initialized_;
    private long startTimeMs_;
    private final String comment_;
    private String rootDirectory_;
    private VirtualAcquisitionDisplay virtAcq_;
    private ImageCache imageCache_;
    private final boolean existing_;
    private final boolean virtual_;
    private final boolean show_;
    private JSONArray channelColors_;
    private JSONArray channelNames_;
    private JSONObject summary_;
    private final String NOTINITIALIZED;

    public MMAcquisition(String name, String dir) throws MMScriptException {
        this(name, dir, false, false, false);
    }

    public MMAcquisition(String name, String dir, boolean show) throws MMScriptException {
        this(name, dir, show, false, false);
    }

    public MMAcquisition(String name, String dir, boolean show, boolean diskCached, boolean existing) throws MMScriptException {
        this.outputQueue_ = null;
        this.isAsynchronous_ = false;
        this.numFrames_ = 0;
        this.numChannels_ = 0;
        this.numSlices_ = 0;
        this.numPositions_ = 0;
        this.width_ = 0;
        this.height_ = 0;
        this.byteDepth_ = 1;
        this.bitDepth_ = 8;
        this.multiCamNumCh_ = 1;
        this.initialized_ = false;
        this.comment_ = "";
        this.channelColors_ = new JSONArray();
        this.channelNames_ = new JSONArray();
        this.summary_ = new JSONObject();
        this.NOTINITIALIZED = "Acquisition was not initialized";
        this.name_ = name;
        this.rootDirectory_ = dir;
        this.show_ = show;
        this.existing_ = existing;
        this.virtual_ = diskCached;
    }

    public MMAcquisition(String name, JSONObject summaryMetadata, boolean diskCached, AcquisitionEngine eng, boolean show) {
        this.outputQueue_ = null;
        this.isAsynchronous_ = false;
        this.numFrames_ = 0;
        this.numChannels_ = 0;
        this.numSlices_ = 0;
        this.numPositions_ = 0;
        this.width_ = 0;
        this.height_ = 0;
        this.byteDepth_ = 1;
        this.bitDepth_ = 8;
        this.multiCamNumCh_ = 1;
        this.initialized_ = false;
        this.comment_ = "";
        this.channelColors_ = new JSONArray();
        this.channelNames_ = new JSONArray();
        this.summary_ = new JSONObject();
        this.NOTINITIALIZED = "Acquisition was not initialized";
        this.name_ = name;
        this.virtual_ = diskCached;
        this.existing_ = false;
        this.show_ = show;

        try {
            if (summaryMetadata.has("Directory") && summaryMetadata.get("Directory").toString().length() > 0) {
                try {
                    String acqDirectory = this.createAcqDirectory(summaryMetadata.getString("Directory"), summaryMetadata.getString("Prefix"));
                    summaryMetadata.put("Prefix", acqDirectory);
                    String acqPath = summaryMetadata.getString("Directory") + File.separator + acqDirectory;
                    TaggedImageStorage imageFileManager = ImageUtils.newImageStorageInstance(acqPath, true, (JSONObject)null);
                    this.imageCache_ = new MMImageCache(imageFileManager);
                    if (!this.virtual_) {
                        this.imageCache_.saveAs(new TaggedImageStorageRamFast((JSONObject)null), true);
                    }
                } catch (Exception var9) {
                    ReportingUtils.showError(var9, "Unable to create directory for saving images.");
                    eng.stop(true);
                    this.imageCache_ = null;
                }
            } else {
                TaggedImageStorage imageFileManager = new TaggedImageStorageRamFast((JSONObject)null);
                this.imageCache_ = new MMImageCache(imageFileManager);
            }

            this.imageCache_.setSummaryMetadata(summaryMetadata);
            if (this.show_) {
                this.virtAcq_ = new VirtualAcquisitionDisplay(this.imageCache_,(org.micromanager.acquisition.AcquisitionEngine) eng, name, false);
                this.imageCache_.addImageCacheListener(this.virtAcq_);
            }

            this.summary_ = summaryMetadata;
        } catch (JSONException var10) {
            ReportingUtils.showError(var10);
        }

    }

    private String createAcqDirectory(String root, String prefix) throws Exception {
        File rootDir = JavaUtils.createDirectory(root);
        int curIndex = this.getCurrentMaxDirIndex(rootDir, prefix + "_");
        return prefix + "_" + (1 + curIndex);
    }

    private int getCurrentMaxDirIndex(File rootDir, String prefix) throws NumberFormatException {
        int maxNumber = 0;
        File[] arr$ = rootDir.listFiles();
        int len$ = arr$.length;

        for(int i$ = 0; i$ < len$; ++i$) {
            File acqDir = arr$[i$];
            String theName = acqDir.getName();
            if (theName.startsWith(prefix)) {
                try {
                    Pattern p = Pattern.compile("\\Q" + prefix + "\\E" + "(\\d+).*+");
                    Matcher m = p.matcher(theName);
                    if (m.matches()) {
                        int number = Integer.parseInt(m.group(1));
                        if (number >= maxNumber) {
                            maxNumber = number;
                        }
                    }
                } catch (NumberFormatException var12) {
                }
            }
        }

        return maxNumber;
    }

    public void setImagePhysicalDimensions(int width, int height, int byteDepth, int bitDepth, int multiCamNumCh) throws MMScriptException {
        if (this.initialized_) {
            throw new MMScriptException("Can't change image dimensions - the acquisition is already initialized");
        } else {
            this.width_ = width;
            this.height_ = height;
            this.byteDepth_ = byteDepth;
            this.bitDepth_ = bitDepth;
            this.multiCamNumCh_ = multiCamNumCh;
        }
    }

    public int getWidth() {
        return this.width_;
    }

    public int getHeight() {
        return this.height_;
    }

    public int getByteDepth() {
        return this.byteDepth_;
    }

    public int getBitDepth() {
        return this.bitDepth_;
    }

    public int getMultiCameraNumChannels() {
        return this.multiCamNumCh_;
    }

    public int getFrames() {
        return this.numFrames_;
    }

    public int getChannels() {
        return this.numChannels_;
    }

    public int getSlices() {
        return this.numSlices_;
    }

    public int getPositions() {
        return this.numPositions_;
    }

    public void setDimensions(int frames, int channels, int slices) throws MMScriptException {
        this.setDimensions(frames, channels, slices, 0);
    }

    public void setDimensions(int frames, int channels, int slices, int positions) throws MMScriptException {
        if (this.initialized_) {
            throw new MMScriptException("Can't change dimensions - the acquisition is already initialized");
        } else {
            this.numFrames_ = frames;
            this.numChannels_ = channels;
            this.numSlices_ = slices;
            this.numPositions_ = positions;
        }
    }

    public void setRootDirectory(String dir) throws MMScriptException {
        if (this.initialized_) {
            throw new MMScriptException("Can't change root directory - the acquisition is already initialized");
        } else {
            this.rootDirectory_ = dir;
        }
    }

    public void initializeSimpleAcq() throws MMScriptException {
        if (this.initialized_) {
            throw new MMScriptException("Acquisition is already initialized");
        } else {
            TaggedImageStorage imageFileManager = new TaggedImageStorageLive();
            MMImageCache imageCache = new MMImageCache(imageFileManager);
            if (!this.existing_) {
                this.createDefaultAcqSettings(imageCache);
            }

            MMStudio.getInstance().getSnapLiveManager().createSnapLiveDisplay(this.name_, imageCache);
            if (this.show_) {
                this.virtAcq_ = MMStudio.getInstance().getSnapLiveManager().getSnapLiveDisplay();
                this.virtAcq_.show();
                this.imageCache_ = this.virtAcq_.getImageCache();
                this.imageCache_.addImageCacheListener(this.virtAcq_);
            }

            this.initialized_ = true;
        }
    }

    public void initialize() throws MMScriptException {
        if (this.initialized_) {
            throw new MMScriptException("Acquisition is already initialized");
        } else {
            String name = this.name_;
            String dirName;
            if (this.virtual_ && this.existing_) {
                dirName = this.rootDirectory_ + File.separator + name;

                Object imageFileManager;
                try {
                    boolean multipageTiff = MultipageTiffReader.isMMMultipageTiff(dirName);
                    if (multipageTiff) {
                        imageFileManager = new TaggedImageStorageMultipageTiff(dirName, false, (JSONObject)null);
                    } else {
                        imageFileManager = new TaggedImageStorageDiskDefault(dirName, false, (JSONObject)null);
                    }
                } catch (Exception var9) {
                    throw new MMScriptException(var9);
                }

                this.imageCache_ = new MMImageCache((TaggedImageStorage)imageFileManager);
            }

            if (this.virtual_ && !this.existing_) {
                dirName = this.rootDirectory_ + File.separator + name;
                if ((new File(dirName)).exists()) {
                    try {
                        String acqDirectory = this.createAcqDirectory(this.rootDirectory_, this.name_);
                        if (this.summary_ != null) {
                            this.summary_.put("Prefix", acqDirectory);
                            this.summary_.put("Channels", this.numChannels_);
                            MDUtils.setPixelTypeFromByteDepth(this.summary_, this.byteDepth_);
                        }

                        dirName = this.rootDirectory_ + File.separator + acqDirectory;
                    } catch (Exception var8) {
                        throw new MMScriptException("Failed to figure out acq saving path.");
                    }
                }

                TaggedImageStorage imageFileManager = ImageUtils.newImageStorageInstance(dirName, true, this.summary_);
                this.imageCache_ = new MMImageCache(imageFileManager);
            }

            if (!this.virtual_ && !this.existing_) {
                TaggedImageStorage imageFileManager = new TaggedImageStorageRamFast((JSONObject)null);
                this.imageCache_ = new MMImageCache(imageFileManager);
            }

            if (!this.virtual_ && this.existing_) {
                dirName = this.rootDirectory_ + File.separator + name;

                Object tempImageFileManager;
                try {
                    boolean multipageTiff = MultipageTiffReader.isMMMultipageTiff(dirName);
                    if (multipageTiff) {
                        tempImageFileManager = new TaggedImageStorageMultipageTiff(dirName, false, (JSONObject)null);
                    } else {
                        tempImageFileManager = new TaggedImageStorageDiskDefault(dirName, false, (JSONObject)null);
                    }
                } catch (Exception var7) {
                    throw new MMScriptException(var7);
                }

                this.imageCache_ = new MMImageCache((TaggedImageStorage)tempImageFileManager);
                if ((double)((TaggedImageStorage)tempImageFileManager).getDataSetSize() > 0.9D * (double)JavaUtils.getAvailableUnusedMemory()) {
                    throw new MMScriptException("Not enough room in memory for this data set.\nTry opening as a virtual data set instead.");
                }

                TaggedImageStorageRamFast ramStore = new TaggedImageStorageRamFast((JSONObject)null);
                ramStore.setDiskLocation(((TaggedImageStorage)tempImageFileManager).getDiskLocation());
                this.imageCache_.saveAs(ramStore);
            }

            CMMCore core = MMStudio.getInstance().getCore();
            if (!this.existing_) {
                int camCh = (int)core.getNumberOfCameraChannels();
                int i;
                if (camCh > 1) {
                    for(i = 0; i < camCh; ++i) {
                        if (this.channelNames_.length() < 1 + i) {
                            this.setChannelName(i, core.getCameraChannelName((long)i));
                        }
                    }
                } else {
                    for(i = 0; i < this.numChannels_; ++i) {
                        if (this.channelNames_.length() < 1 + i) {
                            this.setChannelName(i, "Default" + i);
                        }
                    }
                }

                if (this.bitDepth_ == 0) {
                    this.bitDepth_ = (int)core.getImageBitDepth();
                }

                this.createDefaultAcqSettings(this.imageCache_);
            }

            if (this.imageCache_.getSummaryMetadata() != null) {
                if (this.show_) {
                    this.virtAcq_ = new VirtualAcquisitionDisplay(this.imageCache_, null, name, true);
                    this.imageCache_.addImageCacheListener(this.virtAcq_);
                    this.virtAcq_.show();
                }

                this.initialized_ = true;
            }

        }
    }

    private void createDefaultAcqSettings(ImageCache imageCache) {
        String[] keys = new String[this.summary_.length()];
        Iterator<String> it = this.summary_.keys();

        for(int var4 = 0; it.hasNext(); ++var4) {
            keys[0] = (String)it.next();
        }

        try {
            JSONObject summaryMetadata = new JSONObject(this.summary_, keys);
            CMMCore core = MMStudio.getInstance().getCore();
            summaryMetadata.put("BitDepth", this.bitDepth_);
            summaryMetadata.put("Channels", this.numChannels_);
            this.setDefaultChannelTags(summaryMetadata);
            summaryMetadata.put("Comment", "");
            String compName = null;

            try {
                compName = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException var9) {
                ReportingUtils.showError(var9);
            }

            if (compName != null) {
                summaryMetadata.put("ComputerName", compName);
            }

            summaryMetadata.put("Date", (new SimpleDateFormat("yyyy-MM-dd")).format(Calendar.getInstance().getTime()));
            summaryMetadata.put("Depth", core.getBytesPerPixel());
            summaryMetadata.put("Frames", this.numFrames_);
            summaryMetadata.put("GridColumn", 0);
            summaryMetadata.put("GridRow", 0);
            summaryMetadata.put("Height", this.height_);
            int ijType = -1;
            if (this.byteDepth_ == 1) {
                ijType = 0;
            } else if (this.byteDepth_ == 2) {
                ijType = 1;
            } else if (this.byteDepth_ == 8) {
                ijType = 64;
            } else if (this.byteDepth_ == 4 && core.getNumberOfComponents() == 1L) {
                ijType = 2;
            } else if (this.byteDepth_ == 4 && core.getNumberOfComponents() == 4L) {
                ijType = 4;
            }

            summaryMetadata.put("IJType", ijType);
            summaryMetadata.put("MetadataVersion", 10);
            summaryMetadata.put("MicroManagerVersion", MMStudio.getInstance().getVersion());
            summaryMetadata.put("NumComponents", 1);
            summaryMetadata.put("Positions", this.numPositions_);
            summaryMetadata.put("Source", "Micro-Manager");
            summaryMetadata.put("PixelAspect", 1.0D);
            summaryMetadata.put("PixelSize_um", core.getPixelSizeUm());
            summaryMetadata.put("PixelType", (core.getNumberOfComponents() == 1L ? "GRAY" : "RGB") + 8 * this.byteDepth_);
            summaryMetadata.put("Slices", this.numSlices_);
            summaryMetadata.put("SlicesFirst", false);
            summaryMetadata.put("StartTime", MDUtils.getCurrentTime());
            summaryMetadata.put("Time", Calendar.getInstance().getTime());
            summaryMetadata.put("TimeFirst", true);
            summaryMetadata.put("UserName", System.getProperty("user.name"));
            summaryMetadata.put("UUID", UUID.randomUUID());
            summaryMetadata.put("Width", this.width_);
            this.startTimeMs_ = System.currentTimeMillis();
            imageCache.setSummaryMetadata(summaryMetadata);
        } catch (JSONException var10) {
            ReportingUtils.showError(var10);
        }

    }

    public static int getMultiCamDefaultChannelColor(int index, String channelName) {
        Preferences root = Preferences.userNodeForPackage(SeqAcqGui.class);
        Preferences colorPrefs = root.node(root.absolutePath() + "/" + "ColorSettings");
        int color = DEFAULT_COLORS[index % DEFAULT_COLORS.length].getRGB();
        String channelGroup = MMStudio.getInstance().getCore().getChannelGroup();
        if (channelGroup == null) {
            channelGroup = "";
        }

        color = colorPrefs.getInt("Color_Camera_" + channelName, colorPrefs.getInt("Color_" + channelGroup + "_" + channelName, color));
        return color;
    }

    private void setDefaultChannelTags(JSONObject md) {
        JSONArray channelMaxes = new JSONArray();
        JSONArray channelMins = new JSONArray();
        JSONArray newColors = new JSONArray();
        JSONArray newNames = new JSONArray();

        int i;
        for(i = 0; i < this.numChannels_; ++i) {
            try {
                if (i < this.channelColors_.length()) {
                    newColors.put(i, this.channelColors_.get(i));
                }

                if (i < this.channelNames_.length()) {
                    newNames.put(i, this.channelNames_.get(i));
                }
            } catch (JSONException var14) {
                ReportingUtils.logError(var14, "Couldn't copy over names and colors!");
            }
        }

        this.channelColors_ = newColors;
        this.channelNames_ = newNames;
        if (this.numChannels_ == 1) {
            try {
                if (this.channelColors_.length() == 0) {
                    this.channelColors_.put(0, Color.white.getRGB());
                }

                if (this.channelNames_.length() == 0) {
                    this.channelNames_.put(0, "Default");
                }

                try {
                    CMMCore core = MMStudio.getInstance().getCore();
                    String name = core.getCurrentConfigFromCache(core.getChannelGroup());
                    if (!name.equals("") || this.channelNames_.length() == 0) {
                        this.channelNames_.put(0, name);
                    }
                } catch (Exception var15) {
                    ;
                }

                channelMins.put(0);
                channelMaxes.put(Math.pow(2.0D, (double)md.getInt("BitDepth")) - 1.0D);
            } catch (JSONException var16) {
                ReportingUtils.logError(var16);
            }
        } else {
            for(i = 0; i < this.numChannels_; ++i) {
                if (this.channelColors_.length() > i) {
                    try {
                        this.channelColors_.put(i, getMultiCamDefaultChannelColor(i, this.channelNames_.getString(i)));
                    } catch (JSONException var13) {
                        ReportingUtils.logError(var13);
                    }
                }

                try {
                    this.channelNames_.get(i);
                } catch (JSONException var12) {
                    try {
                        this.channelNames_.put(i, String.valueOf(i));
                    } catch (JSONException var11) {
                        ;
                    }
                }

                try {
                    channelMaxes.put(Math.pow(2.0D, (double)md.getInt("BitDepth")) - 1.0D);
                    channelMins.put(0);
                } catch (JSONException var10) {
                    ReportingUtils.logError(var10);
                }
            }
        }

        try {
            md.put("ChColors", this.channelColors_);
            md.put("ChNames", this.channelNames_);
            md.put("ChContrastMax", channelMaxes);
            md.put("ChContrastMin", channelMins);
        } catch (JSONException var9) {
            ReportingUtils.logError(var9);
        }

    }

    public void insertImage(Object pixels, int frame, int channel, int slice) throws MMScriptException {
        this.insertImage((Object)pixels, frame, channel, slice, 0);
    }

    public void insertImage(Object pixels, int frame, int channel, int slice, int position) throws MMScriptException {
        if (!this.initialized_) {
            throw new MMScriptException("Acquisition data must be initialized before inserting images");
        } else {
            try {
                JSONObject tags = new JSONObject();
                MDUtils.setChannelName(tags, this.getChannelName(channel));
                MDUtils.setChannelIndex(tags, channel);
                MDUtils.setFrameIndex(tags, frame);
                MDUtils.setPositionIndex(tags, position);
                if (this.numPositions_ > 1) {
                    MDUtils.setPositionName(tags, "Pos" + position);
                }

                MDUtils.setSliceIndex(tags, slice);
                MDUtils.setHeight(tags, this.height_);
                MDUtils.setWidth(tags, this.width_);
                MDUtils.setPixelTypeFromByteDepth(tags, this.byteDepth_);
                TaggedImage tg = new TaggedImage(pixels, tags);
                this.insertImage(tg);
            } catch (JSONException var8) {
                throw new MMScriptException(var8);
            }
        }
    }

    public void insertTaggedImage(TaggedImage taggedImg, int frame, int channel, int slice) throws MMScriptException {
        if (!this.initialized_) {
            throw new MMScriptException("Acquisition data must be initialized before inserting images");
        } else {
            try {
                JSONObject tags = taggedImg.tags;
                MDUtils.setFrameIndex(tags, frame);
                MDUtils.setChannelIndex(tags, channel);
                MDUtils.setSliceIndex(tags, slice);
                MDUtils.setPixelTypeFromByteDepth(tags, this.byteDepth_);
                MDUtils.setPositionIndex(tags, 0);
                this.insertImage(taggedImg);
            } catch (JSONException var6) {
                throw new MMScriptException(var6);
            }
        }
    }

    public void insertImage(TaggedImage taggedImg, int frame, int channel, int slice, int position) throws MMScriptException, JSONException {
        JSONObject tags = taggedImg.tags;
        MDUtils.setFrameIndex(tags, frame);
        MDUtils.setChannelIndex(tags, channel);
        MDUtils.setSliceIndex(tags, slice);
        MDUtils.setPositionIndex(tags, position);
        this.insertImage(taggedImg, this.show_);
    }

    public void insertImage(TaggedImage taggedImg, int frame, int channel, int slice, int position, boolean updateDisplay) throws MMScriptException, JSONException {
        JSONObject tags = taggedImg.tags;
        MDUtils.setFrameIndex(tags, frame);
        MDUtils.setChannelIndex(tags, channel);
        MDUtils.setSliceIndex(tags, slice);
        MDUtils.setPositionIndex(tags, position);
        this.insertImage(taggedImg, updateDisplay, true);
    }

    public void insertImage(TaggedImage taggedImg, int frame, int channel, int slice, int position, boolean updateDisplay, boolean waitForDisplay) throws MMScriptException, JSONException {
        JSONObject tags = taggedImg.tags;
        MDUtils.setFrameIndex(tags, frame);
        MDUtils.setChannelIndex(tags, channel);
        MDUtils.setSliceIndex(tags, slice);
        MDUtils.setPositionIndex(tags, position);
        this.insertImage(taggedImg, updateDisplay, waitForDisplay);
    }

    public void insertImage(TaggedImage taggedImg) throws MMScriptException {
        this.insertImage(taggedImg, this.show_);
    }

    public void insertImage(TaggedImage taggedImg, boolean updateDisplay) throws MMScriptException {
        this.insertImage(taggedImg, updateDisplay && this.show_, true);
    }

    public void insertImage(TaggedImage taggedImg, boolean updateDisplay, boolean waitForDisplay) throws MMScriptException {
        if (!this.initialized_) {
            throw new MMScriptException("Acquisition data must be initialized before inserting images");
        } else {
            try {
                JSONObject tags = taggedImg.tags;
                if (MDUtils.getWidth(tags) != this.width_ || MDUtils.getHeight(tags) != this.height_) {
                    ReportingUtils.logError("Metadata width and height: " + MDUtils.getWidth(tags) + "  " + MDUtils.getHeight(tags) + "   Acquisition Width and height: " + this.width_ + " " + this.height_);
                    throw new MMScriptException("Image dimensions do not match MMAcquisition.");
                }

                if (!MDUtils.getPixelType(tags).contentEquals(getPixelType(this.byteDepth_))) {
                    throw new MMScriptException("Pixel type does not match MMAcquisition.");
                }

                if (!MDUtils.getPixelType(tags).startsWith("RGB")) {
                    int channel = MDUtils.getChannelIndex(tags);
                    MDUtils.setChannelName(tags, this.getChannelName(channel));
                }

                long elapsedTimeMillis = System.currentTimeMillis() - this.startTimeMs_;
                MDUtils.setElapsedTimeMs(tags, (double)elapsedTimeMillis);
                MDUtils.setImageTime(tags, MDUtils.getCurrentTime());
                if (this.isAsynchronous_) {
                    if (this.outputQueue_ == null) {
                        this.outputQueue_ = new LinkedBlockingQueue(1);
                        DefaultTaggedImageSink sink = new DefaultTaggedImageSink(this.outputQueue_, this.imageCache_);
                        sink.start();
                    }

                    if (!this.outputQueue_.offer(taggedImg, 1L, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("Queue full");
                    }
                } else {
                    this.imageCache_.putImage(taggedImg);
                }
            } catch (IOException var10) {
                throw new MMScriptException(var10);
            } catch (IllegalStateException var11) {
                throw new MMScriptException(var11);
            } catch (InterruptedException var12) {
                throw new MMScriptException(var12);
            } catch (JSONException var13) {
                throw new MMScriptException(var13);
            } catch (MMException var14) {
                throw new MMScriptException(var14);
            } catch (MMScriptException var15) {
                throw new MMScriptException(var15);
            }

            if (this.show_) {
                try {
                    this.virtAcq_.albumChanged();
                } catch (Exception var9) {
                    throw new MMScriptException(var9);
                }

                if (updateDisplay) {
                    try {
                        if (this.virtAcq_ != null) {
                            this.virtAcq_.updateDisplay(taggedImg);
                        }
                    } catch (Exception var8) {
                        ReportingUtils.logError(var8);
                        throw new MMScriptException("Unable to show image");
                    }
                }
            }

        }
    }

    public void close() {
        if (this.virtAcq_ != null && this.virtAcq_.acquisitionIsRunning()) {
            this.virtAcq_.abort();
        }

        if (this.outputQueue_ != null) {
            this.outputQueue_.add(TaggedImageQueue.POISON);
            this.outputQueue_ = null;
        }

        if (this.imageCache_ != null && !this.imageCache_.isFinished()) {
            this.imageCache_.finished();
        }

    }

    public boolean isInitialized() {
        return this.initialized_;
    }

    public boolean closeImageWindow() {
        if (this.virtAcq_ != null && !this.virtAcq_.close()) {
            return false;
        } else {
            this.close();
            return true;
        }
    }

    public ImageCache getImageCache() {
        return this.imageCache_;
    }

    public JSONObject getSummaryMetadata() {
        return this.isInitialized() ? this.imageCache_.getSummaryMetadata() : null;
    }

    public String getChannelName(int channel) {
        if (this.isInitialized()) {
            try {
                JSONArray chNames = this.getSummaryMetadata().getJSONArray("ChNames");
                if (chNames != null && channel < chNames.length()) {
                    String name = chNames.getString(channel);
                    return name;
                } else {
                    return "";
                }
            } catch (JSONException var4) {
                ReportingUtils.logError(var4);
                return "";
            }
        } else {
            try {
                return this.channelNames_.getString(channel);
            } catch (JSONException var5) {
                return "";
            }
        }
    }

    public void setChannelName(int channel, String name) throws MMScriptException {
        if (this.isInitialized()) {
            try {
                this.imageCache_.getDisplayAndComments().getJSONArray("Channels").getJSONObject(channel).put("Name", name);
                this.imageCache_.getSummaryMetadata().getJSONArray("ChNames").put(channel, name);
                if (this.show_) {
                    this.virtAcq_.updateChannelNamesAndColors();
                }
            } catch (JSONException var5) {
                throw new MMScriptException("Problem setting Channel name");
            }
        } else {
            try {
                this.channelNames_.put(channel, name);
            } catch (JSONException var4) {
                throw new MMScriptException(var4);
            }
        }

    }

    public void setChannelColor(int channel, int rgb) throws MMScriptException {
        if (this.isInitialized()) {
            try {
                this.imageCache_.setChannelColor(channel, rgb);
                this.imageCache_.getSummaryMetadata().getJSONArray("ChColors").put(channel, rgb);
                if (this.show_) {
                    this.virtAcq_.updateChannelNamesAndColors();
                    this.virtAcq_.updateAndDraw(true);
                }
            } catch (JSONException var5) {
                throw new MMScriptException(var5);
            }
        } else {
            try {
                this.channelColors_.put(channel, rgb);
            } catch (JSONException var4) {
                throw new MMScriptException(var4);
            }
        }

    }

    public void promptToSave(boolean promptToSave) {
        if (this.show_) {
            VirtualAcquisitionDisplay.getDisplay(this.virtAcq_.getHyperImage()).promptToSave(promptToSave);
        }

    }

    public void setChannelContrast(int channel, int min, int max) throws MMScriptException {
        if (this.show_) {
            if (!this.isInitialized()) {
                throw new MMScriptException("Acquisition was not initialized");
            }

            this.virtAcq_.setChannelContrast(channel, min, max, 1.0D);
        }

    }

    public void setContrastBasedOnFrame(int frame, int slice) throws MMScriptException {
        if (this.show_) {
            if (!this.isInitialized()) {
                throw new MMScriptException("Acquisition was not initialized");
            }

            int currentFrame = this.virtAcq_.getHyperImage().getFrame();
            int currentSlice = this.virtAcq_.getHyperImage().getSlice();
            int currentChannel = this.virtAcq_.getHyperImage().getChannel();
            this.virtAcq_.getHyperImage().setPosition(currentChannel, slice, frame);
            this.virtAcq_.getHistograms().autoscaleAllChannels();
            this.virtAcq_.getHyperImage().setPosition(currentChannel, currentSlice, currentFrame);
        }

    }

    public void setProperty(String propertyName, String value) throws MMScriptException {
        if (this.isInitialized()) {
            try {
                this.imageCache_.getSummaryMetadata().put(propertyName, value);
            } catch (JSONException var5) {
                throw new MMScriptException("Failed to set property: " + propertyName);
            }
        } else {
            try {
                this.summary_.put(propertyName, value);
            } catch (JSONException var4) {
                throw new MMScriptException("Failed to set property: " + propertyName);
            }
        }

    }

    public void setProperty(int frame, int channel, int slice, String propName, String value) throws MMScriptException {
        if (this.isInitialized()) {
            try {
                JSONObject tags = this.imageCache_.getImage(channel, slice, frame, 0).tags;
                tags.put(propName, value);
            } catch (JSONException var7) {
                throw new MMScriptException(var7);
            }
        } else {
            throw new MMScriptException("Can not set property before acquisition is initialized");
        }
    }

    public void setSummaryProperties(JSONObject md) throws MMScriptException {
        if (this.isInitialized()) {
            try {
                JSONObject tags = this.imageCache_.getSummaryMetadata();
                Iterator iState = md.keys();

                while(iState.hasNext()) {
                    String key = (String)iState.next();
                    tags.put(key, md.get(key));
                }
            } catch (JSONException var6) {
                throw new MMScriptException(var6);
            }
        } else {
            try {
                Iterator iState = md.keys();

                while(iState.hasNext()) {
                    String key = (String)iState.next();
                    this.summary_.put(key, md.get(key));
                }
            } catch (JSONException var5) {
                throw new MMScriptException(var5);
            }
        }

    }

    public boolean windowClosed() {
        if (this.show_ && this.initialized_) {
            return this.virtAcq_ == null || this.virtAcq_.windowClosed();
        } else {
            return false;
        }
    }

    public boolean getShow() {
        return this.show_;
    }

    private static String getPixelType(int depth) {
        switch(depth) {
            case 1:
                return "GRAY8";
            case 2:
                return "GRAY16";
            case 3:
            case 5:
            case 6:
            case 7:
            default:
                return null;
            case 4:
                return "RGB32";
            case 8:
                return "RGB64";
        }
    }

    public int getLastAcquiredFrame() {
        return this.imageCache_ != null ? this.imageCache_.lastAcquiredFrame() : 0;
    }

    public VirtualAcquisitionDisplay getAcquisitionWindow() {
        return this.virtAcq_;
    }

    public void setAsynchronous() {
        this.isAsynchronous_ = true;
    }

    static {
        DEFAULT_COLORS = new Color[]{Color.red, Color.green, Color.blue, Color.pink, Color.orange, Color.yellow};
    }
}
