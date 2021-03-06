package org.micromanager.rapp.SequenceAcquisitions;

import org.micromanager.MMOptions;
import org.micromanager.api.ScriptInterface;
import org.micromanager.utils.ReportingUtils;
import org.micromanager.utils.TooltipTextMaker;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.prefs.Preferences;


/**
 * Data representation class for the channels list in the MDA dialog.
 */
public class ChannelTableModel extends AbstractTableModel implements TableModelListener {

   private static final long serialVersionUID = 3290621191844925827L;
   private ArrayList<ChannelSpec> channels_;
   private final ScriptInterface studio_;
   private final AcquisitionEngine acqEng_;
   private final Preferences exposurePrefs_;
   private final Preferences colorPrefs_;
   private final MMOptions options_;

   public final String[] COLUMN_NAMES = new String[]{
      "Use ?",
      "Configuration",
      "Exposure (ms) ",
      "Laser Exposure (ms)",
      "Segmented ?",
      "Kill Cells ?"
     // "Color"
   };

   private final String[] TOOLTIPS = new String[]{
      "Toggle channel/group on/off",
      "Choose preset property values for channel or group",
      "Set exposure time in ms",
      TooltipTextMaker.addHTMLBreaksForTooltip("Set a Z offset specific to this channel/group (the main "
      + "object in one of the channels/groups is in a different focal plane from the other channels/groups"),
      "Collect images in multiple Z planes?",
      TooltipTextMaker.addHTMLBreaksForTooltip("Setting :  taking images in "
      + "that channel (after taking the first image) for the indicated "
      + "number of time intervals. The 5D-Image Viewer will 'fill in' these skipped "
      + "frames with the previous image. In some situations it may be "
      + "desirable to acquire certain channels at lower sampling rates, "
      + "to reduce photo-toxicity and to save disk space. "),
      "Select channel/group color for display in viewer"};

   public String getToolTipText(int columnIndex) {
      return TOOLTIPS[columnIndex];
   }

   public ChannelTableModel(ScriptInterface studio, AcquisitionEngine eng, Preferences exposurePrefs, Preferences colorPrefs, MMOptions options) {
      studio_ = studio;
      acqEng_ = eng;
      exposurePrefs_ = exposurePrefs;
      colorPrefs_ = colorPrefs;
      options_ = options;
   }

   @Override
   public int getRowCount() {
      if (channels_ == null) {
         return 0;
      } else {
         return channels_.size();
      }
   }

   @Override
   public int getColumnCount() {
      return COLUMN_NAMES.length;
   }

   @Override
   public String getColumnName(int columnIndex) {
      return COLUMN_NAMES[columnIndex];
   }

   @Override
   public Object getValueAt(int rowIndex, int columnIndex) {
      if (channels_ != null && rowIndex < channels_.size()) {
         if (columnIndex == 0) {
            return channels_.get(rowIndex).useChannel;
         } else if (columnIndex == 1) {
            return channels_.get(rowIndex).config;
         } else if (columnIndex == 2) {
            return channels_.get(rowIndex).exposure;
         } else if (columnIndex == 3) {
            return channels_.get(rowIndex).laser_exposure;
         } else if (columnIndex == 4) {
            return channels_.get(rowIndex).useSegmentation;
         } else if (columnIndex == 5) {
            return channels_.get(rowIndex).KillCell;
         }
      }
      return null;
   }

   @Override
   public Class getColumnClass(int c) {
      return getValueAt(0, c).getClass();
   }

   @Override
   public void setValueAt(Object value, int row, int col) {
          if (row < channels_.size() && value != null) {

          ChannelSpec channel = channels_.get(row);
          if (col == 0) {
             channel.useChannel = ((Boolean) value);
          } else if (col == 1) {
             channel.config = value.toString();
             channel.exposure = exposurePrefs_.getDouble("Exposure_" + acqEng_.getChannelGroup() + "_" + channel.config, 10.0);
          } else if (col == 2) {
             channel.exposure = ((Double) value);
             exposurePrefs_.putDouble("Exposure_" + acqEng_.getChannelGroup() + "_" + channel.config,channel.exposure);
          } else if (col == 3) {
             channel.laser_exposure = ((Double) value);
          } else if (col == 4) {
            channel.useSegmentation = ((Boolean) value);
          } else if (col == 5) {
             channel.KillCell = ((Boolean) value);
          }
          acqEng_.setChannel(row, channel);
       }
   }


   @Override
   public boolean isCellEditable(int nRow, int nCol) {

      if (nCol == 4) {
          return acqEng_.isDoSegmentationEnabled();
      }
      if (nCol == 5) {
        return channels_.get(nRow).useSegmentation;
      }
      return true;
   }

   /*
    * Catched events thrown by the ColorEditor
    * Will write the new color into the Color Prefs
    */
   @Override
   public void tableChanged(TableModelEvent e) {
      int row = e.getFirstRow();
      if (row < 0) {
         return;
      }
      int col = e.getColumn();
      if (col < 0) {
         return;
      }
      ChannelSpec channel = channels_.get(row);
      TableModel model = (TableModel) e.getSource();


//      if (col == 5) {
//         Color color = (Color) model.getValueAt(row, col);
//         colorPrefs_.putInt("Color_" + acqEng_.getChannelGroup() + "_" + channel.config, color.getRGB());
//      }

   }

   public void setChannels(ArrayList<ChannelSpec> ch) {
      channels_ = ch;
   }

   public ArrayList<ChannelSpec> getChannels() {
      return channels_;
   }

   /**
    * Adds a new channel to the list in the MDA window
    */
   public void addNewChannel() {
      ChannelSpec channel = new ChannelSpec();
      channel.config = "";
      if (acqEng_.getChannelConfigs().length > 0) {

         for (String config : acqEng_.getChannelConfigs()) {
            boolean unique = true;
            for (ChannelSpec chan : channels_) {
               if (config.contentEquals(chan.config)) {
                  unique = false;
               }
            }
            if (unique) {
               channel.config = config;
               break;
            }
         }
         if (channel.config.length() == 0) {
            ReportingUtils.showMessage("No more channels are available\nin this channel group.");
         } else {
//            channel.color = new Color(colorPrefs_.getInt(
//                    "Color_" + acqEng_.getChannelGroup() + "_" +
//                    channel.config, Color.white.getRGB()));
            channel.exposure = exposurePrefs_.getDouble(
                    "Exposure_" + acqEng_.getChannelGroup() + "_" +
                    channel.config, 10.0);
            channels_.add(channel);
         }
      }
   }

   public void removeChannel(int chIndex) {
      if (chIndex >= 0 && chIndex < channels_.size()) {
         channels_.remove(chIndex);
      }
   }

   /**
    * Used to change the order of the channels in the MDA window
    * @param rowIdx
    * @return
    */
   public int rowDown(int rowIdx) {
      if (rowIdx >= 0 && rowIdx < channels_.size() - 1) {
         ChannelSpec channel = channels_.get(rowIdx);
         channels_.remove(rowIdx);
         channels_.add(rowIdx + 1, channel);
         return rowIdx + 1;
      }
      return rowIdx;
   }

   /**
    * Used to change the order of the channels in the MDA window
    */
   public int rowUp(int rowIdx) {
      if (rowIdx >= 1 && rowIdx < channels_.size()) {
         ChannelSpec channel = channels_.get(rowIdx);
         channels_.remove(rowIdx);
         channels_.add(rowIdx - 1, channel);
         return rowIdx - 1;
      }
      return rowIdx;
   }

   public String[] getAvailableChannels() {
      return acqEng_.getChannelConfigs();
   }

   /**
    * Remove all channels from the list which are not compatible with
    * the current acquisition settings
    */
   public void cleanUpConfigurationList() {
      String config;
      for (Iterator<ChannelSpec> it = channels_.iterator(); it.hasNext();) {
         config = it.next().config;
         if (!config.contentEquals("") && !acqEng_.isConfigAvailable(config)) {
            it.remove();
         }
      }
      fireTableStructureChanged();
   }

   /**
    * reports if the same channel name is used twice
    */
   public boolean duplicateChannels() {
      for (int i = 0; i < channels_.size() - 1; i++) {
         for (int j = i + 1; j < channels_.size(); j++) {
            if (channels_.get(i).config.equals(channels_.get(j).config)) {
               return true;
            }
         }
      }
      return false;
   }
   
   /**
    * Updates the exposure time in the given preset 
    * 
    * @param channelGroup - if it does not match current channelGroup, 
    * no action will be taken
    * 
    * @param channel - preset for which to change exposire time
    * @param exposure - desired exposure time
    */
   public void setChannelExposureTime(String channelGroup, String channel, 
           double exposure) {
      if (!channelGroup.equals(acqEng_.getChannelGroup()))
         return;
      for (ChannelSpec ch : channels_) {
         if (ch.config.equals(channel)) {
            ch.exposure = exposure;
            this.fireTableDataChanged();
         }

      }
   }
}
