package org.micromanager.rapp.utils;


public class AcqOrderMode {
    public static final int SEGMENTATION_POS_KILL_CHANNEL = 0;
    public static final int TIME_POS_CHANNEL_SLICE = 1;
    public static final int POS_TIME_SLICE_CHANNEL = 2;
    public static final int POS_TIME_CHANNEL_SLICE = 3;
    private int id_;
    private boolean segmentationEnable_;
    private boolean posEnabled_;
    private boolean killEnabled_;
    private boolean channelEnabled_;

    public AcqOrderMode(int id) {
        this.id_ = id;
        this.segmentationEnable_ = true;
        this.posEnabled_ = true;
        this.killEnabled_ = true;
        this.channelEnabled_ = true;
    }

    public String toString() {
        StringBuffer name = new StringBuffer();
        if (this.channelEnabled_ && this.posEnabled_) {
            if (this.id_ != 1 && this.id_ != 0) {
                name.append("Position, Channel");
            } else {
                name.append(" Position, Channel");
            }
        } else if (this.channelEnabled_) {
            name.append("Channel");
        } else if (this.posEnabled_) {
            name.append("Position");
        }



        if ((this.segmentationEnable_ || this.posEnabled_) && (this.channelEnabled_ || this.killEnabled_)) {
        //    name.append(", ");
        }

        if (this.segmentationEnable_ && this.killEnabled_) {
            if (this.id_ != 1 && this.id_ != 3) {
                name.append(", Segmentation, Kill");
            } else {
                name.append(", Segmentation, Kill");
            }
        } else if (this.channelEnabled_ && this.segmentationEnable_) {
            name.append(", Segmentation");
        }

        return name.toString();
    }

    public void setEnabled(boolean segmentation, boolean position, boolean kill, boolean channel) {
        this.segmentationEnable_ = segmentation;
        this.posEnabled_ = position;
        this.killEnabled_ = kill;
        this.channelEnabled_ = channel;
    }

    public int getID() {
        return this.id_;
    }
}
