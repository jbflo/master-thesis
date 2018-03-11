package org.micromanager.rapp.utils;


import ij.gui.ImageWindow;
import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.geom.Point2D.Double;
import java.util.List;
import mmcorej.CMMCore;
import mmcorej.TaggedImage;
import org.json.JSONObject;
import org.micromanager.acquisition.MMAcquisition;
import org.micromanager.api.*;
//import org.micromanager.api.IAcquisitionEngine2010;
import org.micromanager.api.SequenceSettings;
import org.micromanager.dialogs.AcqControlDlg;
import org.micromanager.positionlist.PositionListDlg;
import org.micromanager.utils.AutofocusManager;
import org.micromanager.utils.MMScriptException;

public interface ScriptInterface {
    void sleep(long var1) throws MMScriptException;

    void message(String var1) throws MMScriptException;

    void clearMessageWindow() throws MMScriptException;

    void refreshGUI();

    void refreshGUIFromCache();

    void snapSingleImage();

    void openAcquisition(String var1, String var2, int var3, int var4, int var5, int var6, boolean var7, boolean var8) throws MMScriptException;

    /** @deprecated */
    @Deprecated
    String createAcquisition(JSONObject var1, boolean var2, boolean var3);

    void initializeAcquisition(String var1, int var2, int var3, int var4, int var5) throws MMScriptException;

    void addImageToAcquisition(String var1, int var2, int var3, int var4, int var5, TaggedImage var6) throws MMScriptException;

    void setAcquisitionAddImageAsynchronous(String var1) throws MMScriptException;

    void snapAndAddImage(String var1, int var2, int var3, int var4, int var5) throws MMScriptException;

    /** @deprecated */
    @Deprecated
    MMAcquisition getAcquisition(String var1) throws MMScriptException;

    String getUniqueAcquisitionName(String var1);

    String getCurrentAlbum();

    void addToAlbum(TaggedImage var1) throws MMScriptException;

    Boolean acquisitionExists(String var1);

    void closeAcquisition(String var1) throws MMScriptException;

    void closeAllAcquisitions();

    String[] getAcquisitionNames();

    int getAcquisitionImageWidth(String var1) throws MMScriptException;

    int getAcquisitionImageHeight(String var1) throws MMScriptException;

    int getAcquisitionImageBitDepth(String var1) throws MMScriptException;

    int getAcquisitionImageByteDepth(String var1) throws MMScriptException;

    int getAcquisitionMultiCamNumChannels(String var1) throws MMScriptException;

    void setAcquisitionProperty(String var1, String var2, String var3) throws MMScriptException;

    void setImageProperty(String var1, int var2, int var3, int var4, String var5, String var6) throws MMScriptException;

    String runAcquisition() throws MMScriptException;

    String runAcquisition(String var1, String var2) throws MMScriptException;

    void loadAcquisition(String var1) throws MMScriptException;

    void setPositionList(PositionList var1) throws MMScriptException;

    PositionList getPositionList() throws MMScriptException;

    void setChannelColor(String var1, int var2, Color var3) throws MMScriptException;

    void setChannelName(String var1, int var2, String var3) throws MMScriptException;

    void setChannelExposureTime(String var1, String var2, double var3);

    void setChannelContrast(String var1, int var2, int var3, int var4) throws MMScriptException;

    void setContrastBasedOnFrame(String var1, int var2, int var3) throws MMScriptException;

    double getChannelExposureTime(String var1, String var2, double var3);

    void closeAcquisitionWindow(String var1) throws MMScriptException;

    Double getXYStagePosition() throws MMScriptException;

    void setStagePosition(double var1) throws MMScriptException;

    void setRelativeStagePosition(double var1) throws MMScriptException;

    void setXYStagePosition(double var1, double var3) throws MMScriptException;

    void setRelativeXYStagePosition(double var1, double var3) throws MMScriptException;

    String getXYStageName();

    void setXYOrigin(double var1, double var3) throws MMScriptException;

    void saveConfigPresets();

    ImageWindow getSnapLiveWin();

    ImageCache getCacheForWindow(ImageWindow var1) throws IllegalArgumentException;

    String installAutofocusPlugin(String var1);

    CMMCore getMMCore();

    Autofocus getAutofocus();

    void showAutofocusDialog();

    void logMessage(String var1);

    void showMessage(String var1);

    void showMessage(String var1, Component var2);

    void logError(Exception var1, String var2);

    void logError(Exception var1);

    void logError(String var1);

    void showError(Exception var1, String var2);

    void showError(Exception var1);

    void showError(String var1);

    void showError(Exception var1, String var2, Component var3);

    void showError(Exception var1, Component var2);

    void showError(String var1, Component var2);

    void addMMListener(MMListenerInterface var1);

    void removeMMListener(MMListenerInterface var1);

    void addMMBackgroundListener(Component var1);

    void removeMMBackgroundListener(Component var1);

    Color getBackgroundColor();

    boolean displayImage(TaggedImage var1);

    boolean isLiveModeOn();

    void enableLiveMode(boolean var1);

    Rectangle getROI() throws MMScriptException;

    void setROI(Rectangle var1) throws MMScriptException;

    ImageCache getAcquisitionImageCache(String var1) throws MMScriptException;

    void markCurrentPosition();

    /** @deprecated */
    @Deprecated
    AcqControlDlg getAcqDlg();

    /** @deprecated */
    @Deprecated
    PositionListDlg getXYPosListDlg();

    boolean isAcquisitionRunning();

    boolean versionLessThan(String var1) throws MMScriptException;

    void logStartupProperties();

    void makeActive();

    AutofocusManager getAutofocusManager();

    String getBackgroundStyle();

    String getVersion();

    void setBackgroundStyle(String var1);

    void setConfigChanged(boolean var1);

    void showXYPositionList();

    String openAcquisitionData(String var1, boolean var2) throws MMScriptException;

    String openAcquisitionData(String var1, boolean var2, boolean var3) throws MMScriptException;

    void enableRoiButtons(boolean var1);

    void setImageSavingFormat(Class var1) throws MMScriptException;

    IAcquisitionEngine2010 getAcquisitionEngine2010();

    boolean getHideMDADisplayOption();

    void addImageProcessor(DataProcessor<TaggedImage> var1);

    void removeImageProcessor(DataProcessor<TaggedImage> var1);

    List<DataProcessor<TaggedImage>> getImageProcessorPipeline();

    void setImageProcessorPipeline(List<DataProcessor<TaggedImage>> var1);

    void registerProcessorClass(Class<? extends DataProcessor<TaggedImage>> var1, String var2);

    void setPause(boolean var1);

    boolean isPaused();

    void attachRunnable(int var1, int var2, int var3, int var4, Runnable var5);

    void clearRunnables();

    org.micromanager.api.SequenceSettings getAcquisitionSettings();

    void setAcquisitionSettings(SequenceSettings var1);

    String getAcquisitionPath();

    void promptToSaveAcquisition(String var1, boolean var2) throws MMScriptException;

    void registerForEvents(Object var1);

    void unregisterForEvents(Object var1);

    void autostretchCurrentWindow();
}