package org.micromanager.rapp.SequenceAcquisitions;

import org.micromanager.utils.NumberUtils;
import org.micromanager.utils.ReportingUtils;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.prefs.Preferences;

/**
 * Cell editing using either JTextField or JComboBox depending on whether the
 * property enforces a set of allowed values.
 */
public class ChannelCellEditor extends AbstractCellEditor implements TableCellEditor {

   private static final long serialVersionUID = -8374637422965302637L;
   JTextField text_ = new JTextField();
   JComboBox combo_ = new JComboBox();
   JCheckBox checkBox_ = new JCheckBox();
   JLabel colorLabel_ = new JLabel();
   int editCol_ = -1;
   int editRow_ = -1;
   ChannelSpec channel_ = null;

   private AcquisitionEngine acqEng_;
   private Preferences exposurePrefs_;
   private Preferences colorPrefs_;

   public ChannelCellEditor(AcquisitionEngine engine,
                            Preferences exposurePrefs, Preferences colorPrefs) {
      acqEng_ = engine;
      exposurePrefs_ = exposurePrefs;
      colorPrefs_ = colorPrefs;
   }

   // This method is called when a cell value is edited by the user.
   @Override
   public Component getTableCellEditorComponent(JTable table, Object value,
           boolean isSelected, int rowIndex, int colIndex) {

      // https://stackoverflow.com/a/3055930
      if (value == null) {
         return null;
      }

      if (isSelected) {
         // cell (and perhaps other cells) are selected
      }

      ChannelTableModel model = (ChannelTableModel) table.getModel();
      ArrayList<ChannelSpec> channels = model.getChannels();
      final ChannelSpec channel = channels.get(rowIndex);
      channel_ = channel;

      colIndex = table.convertColumnIndexToModel(colIndex);

      // Configure the component with the specified value
      editRow_ = rowIndex;
      editCol_ = colIndex;
      if (colIndex == 0) {
         checkBox_.setSelected((Boolean) value);
         return checkBox_;
      } else if (colIndex != 2 && colIndex != 3) {
         if (colIndex == 4 ) {
         checkBox_.setSelected((Boolean) value);
         return checkBox_;

          } else if (colIndex != 1) {
             return this.colorLabel_;
          }else {
             // remove old listeners
             this.combo_.removeAllItems();
         // channel
         ActionListener[] l = combo_.getActionListeners();
         for (int i = 0; i < l.length; i++) {
            combo_.removeActionListener(l[i]);
         }
         combo_.removeAllItems();
         String configs[] = model.getAvailableChannels();
         for (int i = 0; i < configs.length; i++) {
            combo_.addItem(configs[i]);
         }
         combo_.setSelectedItem(channel.config);

         // end editing on selection change
         combo_.addActionListener(e -> {
            channel_.color = new Color(colorPrefs_.getInt(
                    "Color_" + acqEng_.getChannelGroup() + "_" +
                            combo_.getSelectedItem(), Color.white.getRGB()));
            channel_.exposure = exposurePrefs_.getDouble(
                    "Exposure_" + acqEng_.getChannelGroup() + "_" +
                            combo_.getSelectedItem(), 10.0);
            channel_.laser_exposure = exposurePrefs_.getDouble(
                    "Exposure_" + acqEng_.getChannelGroup() + "_" +
                            combo_.getSelectedItem(), 10.0);
            this.fireEditingStopped();
         });

         // Return the configured component
         return combo_;
      }
      } else {
            this.text_.setText(NumberUtils.doubleToDisplayString((Double)value));
            return this.text_;
      }
   }

   /** 
    * This method is called when editing is completed.
    * It must return the new value to be stored in the cell.
    */

   @Override
   public Object getCellEditorValue() {
      // TODO: if content of column does not match type we get an exception
      try {
         if (editCol_ == 0) {
            return checkBox_.isSelected();
         } else if (editCol_ == 1) {
            // As a side effect, change to the color and exposure of the new channel
            channel_.color = new Color(colorPrefs_.getInt("Color_" + acqEng_.getChannelGroup() + "_" + combo_.getSelectedItem(), Color.white.getRGB()));
            channel_.exposure = exposurePrefs_.getDouble(
                    "Exposure_" + acqEng_.getChannelGroup() + "_" + channel_.config, 10.0);
            return combo_.getSelectedItem();
         } else if (editCol_ == 2 || editCol_ == 3) {
            return NumberUtils.displayStringToDouble(text_.getText());
         } else if (editCol_ == 4 ) {
           return checkBox_.isSelected();
         } else if (editCol_ == 5) {
             return colorLabel_.getBackground();
         } else {
             return "Internal error: unknown column";
         }
      } catch (ParseException p) {
         ReportingUtils.showError(p);
      }
      String err = "Internal error: unknown column";
      return err;
   }
}
