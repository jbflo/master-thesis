///////////////////////////////////////////////////////////////////////////////
//FILE:          AcqControlDlg.java
//PROJECT:       Micro-Manager
//SUBSYSTEM:     mmstudio
//-----------------------------------------------------------------------------
//
// AUTHOR:       Nenad Amodaj, nenad@amodaj.com, Dec 1, 2005
//
// COPYRIGHT:    University of California, San Francisco, 2006
//
// LICENSE:      This file is distributed under the BSD license.
//               License text is included with the source distribution.
//
//               This file is distributed in the hope that it will be useful,
//               but WITHOUT ANY WARRANTY; without even the implied warranty
//               of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
//               IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//               CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//               INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.

package org.micromanager.rapp.SequenceAcquisitions;


import com.swtdesigner.SwingResourceManager;
import mmcorej.CMMCore;
import org.micromanager.MMOptions;
import org.micromanager.MMStudio;
import org.micromanager.SnapLiveManager;
import org.micromanager.acquisition.ComponentTitledBorder;
import org.micromanager.api.ScriptInterface;
import org.micromanager.dialogs.AdvancedOptionsDialog;
import org.micromanager.internalinterfaces.AcqSettingsListener;
import org.micromanager.rapp.MultiFOV.FOV_Controller;
import org.micromanager.rapp.MultiFOV.FOV_GUI;
import org.micromanager.rapp.RappGui;
import org.micromanager.rapp.utils.AcqOrderMode;
import org.micromanager.rapp.RappPlugin;
import org.micromanager.rapp.utils.FileDialog;
import org.micromanager.rapp.utils.Utils;
import org.micromanager.utils.*;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicInternalFrameUI;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Time-lapse, channel and z-stack acquisition setup dialog.
 * This dialog specifies all parameters for the MDA acquisition.
 */
public class SeqAcqGui extends JInternalFrame implements PropertyChangeListener ,
        AcqSettingsListener {

   private static final long serialVersionUID = 1L;
  // protected JButton listButton_;
   protected JCheckBox fullWellListe_jcb;
   protected static int well_plate_type;

   private JSpinner afSkipInterval_;
   protected static JComboBox acqOrderBox_;
   protected static JComboBox listOfsegmenter_jcb;
   public static final String NEW_ACQFILE_NAME = "MMAcquistion.xml";
   public static final String ACQ_SETTINGS_NODE = "AcquistionSettings";
   public static final String COLOR_SETTINGS_NODE = "ColorSettings";
   private static final String EXPOSURE_SETTINGS_NODE = "AcqExposureSettings";
   private static JComboBox channelGroupCombo_;
   protected static final JTextArea commentTextArea_ = new JTextArea();
   //private final JComboBox zValCombo_;
   private static final JTextField nameField_ = new JTextField();
   private static final JTextField rootField_ =new JTextField();
   private final  JTextField rootField_2;
   public static  JTextField rootField_xmlWellFile;
   private static JTextArea summaryTextArea_;
  // private final JComboBox timeUnitCombo_;
  // private final JFormattedTextField interval_;
  // private final JFormattedTextField zStep_;
  // private final JFormattedTextField zTop_;
  // private final JFormattedTextField zBottom_;
   protected static AcquisitionEngine acqEng_;
   private final JScrollPane channelTablePane_;
   private static JTable channelTable_;
  // private final JSpinner numFrames_;
   private static ChannelTableModel model_;
   private final MMOptions options_;
   private final Preferences prefs_;
   private final Preferences acqPrefs_;
   private final Preferences colorPrefs_;
   private final Preferences exposurePrefs_;
   private File acqFile_;
   private String acqDir_;
   private int zVals_ = 0;
   protected static  JButton acquireButton_;
  // private final JButton setBottomButton_;
  // private final JButton setTopButton_;
   protected JComboBox displayModeCombo_;
   private ScriptInterface studio_;
   private final GUIColors guiColors_;
   private final NumberFormat numberFormat_;
   private final JLabel namePrefixLabel_;
   private final JLabel saveTypeLabel_;
   private static  JRadioButton singleButton_;
   private static JRadioButton multiButton_;
   private final JLabel rootLabel_;
   private final JLabel choose_segmenter;
   private final JLabel xml_Seg_Label_2;
//   private final JLabel txt_pos_Label;
   private final JButton browseRootButton_;
   private final JButton browseRootButton_2;
 //  private  final JButton browseRootButton_plate;
   private final JLabel displayMode_;
   //private final JCheckBox stackKeepShutterOpenCheckBox_;
  // private final JCheckBox chanKeepShutterOpenCheckBox_;
   protected static JProgressBar progressBar;
   protected static JTextArea taskOutput;
   private static  AcqOrderMode[] acqOrderModes_;
   private AdvancedOptionsDialog advancedOptionsWindow_;
   // persistent properties (app settings);
   private static final String ACQ_FILE_DIR = "dir";
   private static final String ACQ_INTERVAL = "acqInterval";
   private static final String ACQ_TIME_UNIT = "acqTimeInit";
   private static final String ACQ_ZBOTTOM = "acqZbottom";
   private static final String ACQ_ZTOP = "acqZtop";
   private static final String ACQ_ENABLE_MULTI_POSITION = "enableMultiPosition";
   private static final String ACQ_ENABLE_MULTI_CHANNEL = "enableMultiChannels";
   private static final String ACQ_ENABLE_SEGMENTATION = "enableSegmentation";
   private static final String ACQ_ENABLE_KILLCELL = "killCell";
   private static final String ACQ_ORDER_MODE = "acqOrderMode";
   private static final String ACQ_CHANNEL_GROUP = "acqChannelGroup";
   private static final String ACQ_NUM_CHANNELS = "acqNumchannels";
//   private static final String ACQ_CHANNELS_KEEP_SHUTTER_OPEN = "acqChannelsKeepShutterOpen";
//   private static final String ACQ_STACK_KEEP_SHUTTER_OPEN = "acqStackKeepShutterOpen";
   private static final String CHANNEL_NAME_PREFIX = "acqChannelName";
   private static final String CHANNEL_USE_PREFIX = "acqChannelUse";
   private static final String SEGMENTATION_USE_PREFIX = "acqSegmentationUse";
   private static final String KILL_CELL_PREFIX = "acqKillCell";
   private static final String CHANNEL_EXPOSURE_PREFIX = "acqChannelExp";
   private static final String CHANNEL_LASER_EXPOSURE_PREFIX = "acqChannelLaserExp";
   private static final String CHANNEL_CONTRAST_MIN_PREFIX = "acqChannelContrastMin";
   private static final String CHANNEL_CONTRAST_MAX_PREFIX = "acqChannelContrastMax";
   private static final String CHANNEL_CONTRAST_GAMMA_PREFIX = "acqChannelContrstGamma";
   private static final String CHANNEL_COLOR_R_PREFIX = "acqChannelColorR";
   private static final String CHANNEL_COLOR_G_PREFIX = "acqChannelColorG";
   private static final String CHANNEL_COLOR_B_PREFIX = "acqChannelColorB";
   private static final String ACQ_Z_VALUES = "acqZValues";
   private static final String ACQ_DIR_NAME = "acqDirName";
   private static final String ACQ_ROOT_NAME = "acqRootName";
   private static final String ACQ_SAVE_FILES = "acqSaveFiles";
   private static final String ACQ_DISPLAY_MODE = "acqDisplayMode";
   private static final String ACQ_AF_ENABLE = "autofocus_enabled";
   private static final String ACQ_COLUMN_WIDTH = "column_width";
   private static final String ACQ_COLUMN_ORDER = "column_order";
   private static final int ACQ_DEFAULT_COLUMN_WIDTH = 77;
   private static final String CUSTOM_INTERVAL_PREFIX = "customInterval";
   private static final String ACQ_ENABLE_CUSTOM_INTERVALS = "enableCustomIntervals";
   private static final FileDialogs.FileType ACQ_SETTINGS_FILE = new FileDialogs.FileType("ACQ_SETTINGS_FILE", "Acquisition settings",
           System.getProperty("user.home") + "/AcqSettings.xml",
           true, "xml");
   private static final String ACQ_NUMBER_X_WELLS = "acqNXWells";
   private static final String ACQ_NUMBER_Y_WELLS = "acqNYWells";
   private static final String ACQ_WELL_WIDTH = "acqWellWidth";
   private static final String ACQ_WELL_DISTANCE = "acqWellDistance";
   private static final String ACQ_FIELD_OF_VIEW = "acqFieldOfView";
   private static final String ACQ_ENABLE_WELL_PLATE = "acqWellPlate";
   private int columnWidth_[];
   private int columnOrder_[];
 //  private final JPanel framesSubPanel_;
 //  private static final CardLayout framesSubPanelLayout_;
   private static final String DEFAULT_FRAMES_PANEL_NAME = "Default frames panel";
   private static final String OVERRIDE_FRAMES_PANEL_NAME = "Override frames panel";
   protected JPanel buttonPanel ;
   private static CheckBoxPanel channelsPanel_;
   private static CheckBoxPanel segmentationPanel_;
   protected static CheckBoxPanel positionsPanel_;
  // private JPanel acquisitionOrderPanel_;
  // private CheckBoxPanel afPanel_;
   private JPanel summaryPanel_;
   private static CheckBoxPanel savePanel_;
   private ComponentTitledPanel commentsPanel_;
   private Border dayBorder_;
   private Border nightBorder_;
   private ArrayList<JPanel> panelList_;
   private static boolean disableGUItoSettings_ = false;
   protected static boolean saveMultiTiff_ = true;
   private SnapLiveManager SnapLiveManager_;
   private CMMCore core_;
   private static SeqAcqGui appInterface_;

    public static SeqAcqGui getInstance() {
        return appInterface_;
    }

   public final void createChannelTable() {
      model_ = new ChannelTableModel(studio_, acqEng_, exposurePrefs_, colorPrefs_, options_);
      model_.addTableModelListener(model_);

      channelTable_ = new JTable() {
         @Override
         @SuppressWarnings("serial")
         protected JTableHeader createDefaultTableHeader() {
            return new JTableHeader(columnModel) {
               @Override
               public String getToolTipText(MouseEvent e) {
                  String tip = null;
                  Point p = e.getPoint();
                  int index = columnModel.getColumnIndexAtX(p.x);
                  int realIndex = columnModel.getColumn(index).getModelIndex();
                  return model_.getToolTipText(realIndex);
               }
            };
         }
      };

      channelTable_.setFont(new Font("Dialog", Font.PLAIN, 9));
      channelTable_.setAutoCreateColumnsFromModel(false);
     // channelTable_.getTableHeader().setBackground(Color.decode("#34495e"));
      channelTable_.setModel(model_);
      model_.setChannels(acqEng_.getChannels());

      ChannelCellEditor cellEditor = new ChannelCellEditor(acqEng_,
            exposurePrefs_, colorPrefs_);
      ChannelCellRenderer cellRenderer = new ChannelCellRenderer(acqEng_);
    //  channelTable_.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
      for (int k = 0; k < model_.getColumnCount(); k++) {
         int colIndex = search(columnOrder_, k);
         if (colIndex < 0) {
            colIndex = k;
         }
//         if (colIndex == model_.getColumnCount() - 1) {
//            ColorRenderer cr = new ColorRenderer(true);
//            ColorEditor ce = new ColorEditor(model_, model_.getColumnCount() - 1);
//            TableColumn column = new TableColumn(model_.getColumnCount() - 1, 300, cr, ce);
//            column.setPreferredWidth(columnWidth_[model_.getColumnCount() - 1]);
//            channelTable_.addColumn(column);
//
//         } else {
            TableColumn column = new TableColumn(colIndex, 300, cellRenderer, cellEditor);
            column.setPreferredWidth(columnWidth_[colIndex]);
            channelTable_.addColumn(column);
      //   }
      }

      channelTablePane_.setViewportView(channelTable_);
   }


   public JPanel createPanel(String text, int left, int top, int right, int bottom) {
      return createPanel(text, left, top, right, bottom, false);
   }

   public JPanel createPanel(String text, int left, int top, int right, int bottom, boolean checkBox) {
      ComponentTitledPanel thePanel;

      if (checkBox) {
         thePanel = new CheckBoxPanel(text);
      } else {
         thePanel = new LabelPanel(text);
      }

      thePanel.setTitleFont(new Font("Dialog", Font.BOLD, 12));
      thePanel.setForeground(Color.decode("#34495e"));

      panelList_.add(thePanel);
      thePanel.setBounds(left, top, right - left, bottom - top);
      dayBorder_ = BorderFactory.createEtchedBorder();
      nightBorder_ = BorderFactory.createEtchedBorder(Color.gray, Color.darkGray);

    //  updatePanelBorder(thePanel);
      thePanel.setLayout(null);
      getContentPane().add(thePanel);
      return thePanel;
   }

   public void updatePanelBorder(JPanel thePanel) {
      TitledBorder border = (TitledBorder) thePanel.getBorder();
      if (studio_.getBackgroundStyle().contentEquals("Day")) {
         border.setBorder(dayBorder_);
      } else {
         border.setBorder(nightBorder_);
      }
   }

   public final void createEmptyPanels() {

      panelList_ = new ArrayList<JPanel>();

      channelsPanel_ = (CheckBoxPanel) createPanel("Channels", 3, 1, 705, 170, true);

      segmentationPanel_= (CheckBoxPanel) createPanel("Segmentation", 710,1,885,170,true );
      buttonPanel =  createPanel("Run", 710, 175, 885, 350);

      // framesPanel_ = (CheckBoxPanel) createPanel("Time points", 5, 308, 220, 451, true); // (text, left, top, right, bottom)
      savePanel_ = (CheckBoxPanel) createPanel("Save images", 3, 175, 510, 290, true);
      positionsPanel_ = (CheckBoxPanel) createPanel("Multiple positions (XY)", 515, 175, 705, 290, true);
  //   afPanel_ = (CheckBoxPanel) createPanel("Autofocus", 715, 295, 875, 295, true);

      summaryPanel_ = createPanel("Summary", 515, 290, 705, 455);
      // acquisitionOrderPanel_ = createPanel("Acquisition order", 515, 300, 705, 435);
      commentsPanel_ = (ComponentTitledPanel) createPanel("Acquisition Comments",3, 290, 510,385,false);
   }

   private void createToolTips() {
    //  String position_list_tooltip_img = getClass().getResource("org/micromanager/rapp/Resources/Images/384-well-plate.jpg").toString();
      String position_list_tooltip =
              "<html> "
                      + "Acquire images from a series of positions in the XY plane <br>"
                      + "You Can Edit Position List to choose a specific position <br>" +
                      " Or you can Enable Full well Imaging  <br>"
                      + "<img src="
     //                 + position_list_tooltip_img + " >"
                      + "</html>"
              ;
      positionsPanel_.setToolTipText(position_list_tooltip);
      //  String imageName = getClass().getResource("/org/micromanager/icons/acq_order_figure.png").toString();
      String acqOrderToolTip =
              "<html> The acquisition order is Automatically selected when you have a multiple dimensions<br>"
              + "(i.e.  X positions, Channel, Segmentation, Cell Killing, and or Saving)  is selected. <br>"
              + "During image acquisition, the values of each dimension are iterated automatically."
              + "The microscope will acquire images in the following order : "
              + "<br> \"Position\"and \"Channel\" always precede \" Chanel Saving? \" , \"Segmentation\" and \"Cell Killing\" <br><br>"
              + "For example, you can only change the order of the Saving" + "<img src="
                   //   + imageName + ">"
                      + "</html>"
              ;
      //acquisitionOrderPanel_.setToolTipText(acqOrderToolTip);
      acqOrderBox_.setToolTipText(acqOrderToolTip);
      //afPanel_.setToolTipText("Toggle autofocus on/off");
      channelsPanel_.setToolTipText("Lets you acquire images in multiple channels (groups of "
              + "properties with multiple preset values");
      savePanel_.setToolTipText(TooltipTextMaker.addHTMLBreaksForTooltip("If the Save images option is selected, "
              + "images will be saved to disk continuously during the acquisition." //+
             // " If this option is not selected, images "
          //    + "are accumulated only in the 5D-Image window, and once the acquisition is finished, image data can be saved"
            //  + " to disk. However, saving files automatically during acquisition secures the acquired data against an "
           //   + "unexpected computer failure or accidental closing of image window. Even when saving to disk, some of the"
      //        + " acquired images are still kept in memory, facilitating fast playback. If such behavior is not desired, "
      //        + "check the 'Conserve RAM' option (Tools | Options)"
      ));
      segmentationPanel_.setToolTipText(TooltipTextMaker.addHTMLBreaksForTooltip("The Segmentation panel should be selected for the killing available " +
                      "If the Segmentation panel is selected  Images will be segmented before .... "));
   }

   /**
    * Acquisition control dialog box.
    * Specification of all parameters required for the acquisition.
    * @param acqEng - acquisition engine
    * @param prefs - application preferences node
    * @param gui
    * @param options
    */
   public SeqAcqGui(AcquisitionEngine acqEng, Preferences prefs,
                    ScriptInterface gui, MMOptions options, CMMCore core) {
      super();
//      try {
//         for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
//            if ("Nimbus".equals(info.getName())) {
//               UIManager.setLookAndFeel(info.getClassName());
//               break;
//            }
//         }
//      } catch (Exception e) {
//         // If Nimbus is not available, you can set the GUI to another look and feel.
//      }
      prefs_ = prefs;
      studio_ = gui;
      core_ = core;
      guiColors_ = new GUIColors();
      options_ = options;
      SnapLiveManager_ = new SnapLiveManager((MMStudio) studio_, core_);

      FOV_Controller  FOV_control = new FOV_Controller(core, gui);

      Preferences root = Preferences.userNodeForPackage(this.getClass());
      acqPrefs_ = root.node(root.absolutePath() + "/" + ACQ_SETTINGS_NODE);
      colorPrefs_ = root.node(root.absolutePath() + "/" + COLOR_SETTINGS_NODE);
      exposurePrefs_ = root.node(root.absolutePath() + "/" + EXPOSURE_SETTINGS_NODE);

      numberFormat_ = NumberFormat.getNumberInstance();

      acqEng_ = acqEng;
      acqEng.addSettingsListener(this);
      getContentPane().setLayout(null);

      // Set JFrame Form and Location
      BasicInternalFrameUI bi = (BasicInternalFrameUI)this.getUI();
      bi.setNorthPane(null);
      createEmptyPanels();
      setLocation(0,0);

//      // Frames panel
//      JPanel defaultPanel = new JPanel();
//      JPanel overridePanel = new JPanel();
//      defaultPanel.setLayout(null);
//      JLabel overrideLabel = new JLabel("Custom time intervals enabled");
//
//      overrideLabel.setFont(new Font("Arial", Font.BOLD, 12));
//      overrideLabel.setForeground(Color.red);
//
//      JButton disableCustomIntervalsButton = new JButton("Disable custom intervals");
//      disableCustomIntervalsButton.addActionListener(e -> {
//         acqEng_.enableCustomTimeIntervals(false);
//         updateGUIContents();
//      });
//      disableCustomIntervalsButton.setFont(new Font("Arial", Font.PLAIN, 10));
//
//      overridePanel.add(overrideLabel, BorderLayout.PAGE_START);
//      overridePanel.add(disableCustomIntervalsButton, BorderLayout.PAGE_END);
//
//      //framesPanel_.setLayout(new BorderLayout());
//      //framesSubPanelLayout_ = new CardLayout();
//      //framesSubPanel_ = new JPanel(framesSubPanelLayout_);
//
//
//      final JLabel numberLabel = new JLabel();
//      numberLabel.setFont(new Font("Arial", Font.PLAIN, 10));
//
//      numberLabel.setText("Number");
//      defaultPanel.add(numberLabel);
//      numberLabel.setBounds(15, 0, 54, 24);
//
//      SpinnerModel sModel = new SpinnerNumberModel(
//              new Integer(1),
//              new Integer(1),
//              null,
//              new Integer(1));
//
//      numFrames_ = new JSpinner(sModel);
//      ((JSpinner.DefaultEditor) numFrames_.getEditor()).getTextField().setFont(new Font("Arial", Font.PLAIN, 10));
//
//      defaultPanel.add(numFrames_);
//      numFrames_.setBounds(60, 0, 70, 24);
//      numFrames_.addChangeListener(new ChangeListener() {
//
//         @Override
//         public void stateChanged(ChangeEvent e) {
//            applySettings();
//         }
//      });
//
//      final JLabel intervalLabel = new JLabel();
//      intervalLabel.setFont(new Font("Arial", Font.PLAIN, 10));
//      intervalLabel.setText("Interval");
//      intervalLabel.setToolTipText("Interval between successive time points.  Setting an interval"
//              + "of 0 will cause micromanager to acquire 'burts' of images as fast as possible");
//      defaultPanel.add(intervalLabel);
//      intervalLabel.setBounds(15, 27, 43, 24);
//
//      interval_ = new JFormattedTextField(numberFormat_);
//      interval_.setFont(new Font("Arial", Font.PLAIN, 10));
//      interval_.setValue(1.0);
//      interval_.addPropertyChangeListener("value", this);
//      defaultPanel.add(interval_);
//      interval_.setBounds(60, 27, 55, 24);
//
//      timeUnitCombo_ = new JComboBox();
//      timeUnitCombo_.addActionListener(new ActionListener() {
//
//         @Override
//         public void actionPerformed(final ActionEvent e) {
//           // interval_.setText(NumberUtils.doubleToDisplayString(convertMsToTime(acqEng_.getFrameIntervalMs(), timeUnitCombo_.getSelectedIndex())));
//         }
//      });
//      timeUnitCombo_.setModel(new DefaultComboBoxModel(new String[]{"ms", "s", "min"}));
//      timeUnitCombo_.setFont(new Font("Arial", Font.PLAIN, 10));
//      timeUnitCombo_.setBounds(120, 27, 67, 24);
//      defaultPanel.add(timeUnitCombo_);


//
//       txt_pos_Label = new JLabel();
//       txt_pos_Label.setFont(new Font("Arial", Font.PLAIN, 10));
//       txt_pos_Label.setText("Load plate configuration file :");
//       txt_pos_Label.setBounds(10, 40, 150, 26);
//       positionsPanel_.add(txt_pos_Label);
//
//       rootField_xmlWellFile = new JTextField();
//       rootField_xmlWellFile.setFont(new Font("Arial", Font.PLAIN, 10));
//       rootField_xmlWellFile.setBounds(10, 70, 130, 22);
//       rootField_xmlWellFile.setEditable(false);
//       positionsPanel_.add(rootField_xmlWellFile);
//
//       browseRootButton_plate = new JButton();
//       browseRootButton_plate.addActionListener(e->{
//
//           String plateChoosed = chooseWellPlate();
//
//           if (plateChoosed != null & plateChoosed != "cancel"){
//               String path = FileDialog.xmlFileChooserDialog("Load plate configuration file :");
//
//               if (plateChoosed == "384") {
//                   well_plate_type = 384;
//                   rootField_xmlWellFile.setText(path);
//                //
//               }
//               else if (plateChoosed =="96"){
//                   well_plate_type = 96;
//                   rootField_xmlWellFile.setText(path);
//               }
//
//           }//else ReportingUtils.showMessage(" Please Choose a plate ");
//       });
//
//       browseRootButton_plate.setMargin(new Insets(2, 5, 2, 5));
//       browseRootButton_plate.setFont(new Font("Dialog", Font.PLAIN, 10));
//       browseRootButton_plate.setText("...");
//       browseRootButton_plate.setBounds(145, 70, 40, 24);
//       positionsPanel_.add(browseRootButton_plate);
//       browseRootButton_plate.setToolTipText("Load well map configuration");

       //////////////////////////////////////////////////////////////////////////////////////////////////


      // Positions (XY) panel
//      listButton_ = new JButton();
//      listButton_.addActionListener(e->{
//          String file = rootField_xmlWellFile.getText();
//
//          if (file.equals("")){
//              ReportingUtils.showMessage(" Please Choose well map configuration file ");
//          }
//          else {
//              boolean valide = FOV_control.valideXml( FOV_Controller.readXmlFile(file, well_plate_type));
//              if (valide){
//            //   JFrame frame_ = new FOV_GUI(RappGui.getInstance(), core);
//            //   frame_.setVisible(true);
//              }else {
//                  ReportingUtils.showMessage(" Please Choose a correct xml configuration file to load Well Map");
//              }
//
//          }
//      });
//
//      listButton_.setToolTipText("Open XY list dialog");
//      listButton_.setIcon(SwingResourceManager.getIcon(SeqAcqGui.class, "Resources/camera.png"));
//      listButton_.setText("Edit position list...");
//      listButton_.setMargin(new Insets(2, 5, 2, 5));
//      listButton_.setFont(new Font("Dialog", Font.PLAIN, 10));
//      listButton_.setBounds(25, 120, 136, 26);
//      positionsPanel_.add(listButton_);


//      fullWellListe_jcb = new JCheckBox();
//      fullWellListe_jcb.addActionListener(e->{
//
//      });
//     // path.concat("Resources/camera.png")
//      fullWellListe_jcb.setIcon(SwingResourceManager.getIcon(SeqAcqGui.class, "Resources/camera.png"));
//
//      fullWellListe_jcb.setToolTipText("Full well Imaging of the Plate");
//      fullWellListe_jcb.setText("Enable Full well Imaging");
//      fullWellListe_jcb.setMargin(new Insets(2, 5, 2, 5));
//      fullWellListe_jcb.setFont(new Font("Dialog", Font.PLAIN, 10));
//      fullWellListe_jcb.setBounds(20, 160, 160, 26);
//      positionsPanel_.add(fullWellListe_jcb);


      // Slices panel

//      slicesPanel_.addActionListener(new ActionListener() {
//
//         @Override
//         public void actionPerformed(final ActionEvent e) {
//            // enable disable all related contrtols
//            applySettings();
//         }
//      });

//      final JLabel zbottomLabel = new JLabel();
//      zbottomLabel.setFont(new Font("Arial", Font.PLAIN, 10));
//      zbottomLabel.setText("Z-start [um]");
//      zbottomLabel.setBounds(30, 30, 69, 15);
//     // slicesPanel_.add(zbottomLabel);
//
//      zBottom_ = new JFormattedTextField(numberFormat_);
//      zBottom_.setFont(new Font("Arial", Font.PLAIN, 10));
//      zBottom_.setBounds(95, 27, 54, 21);
//      zBottom_.setValue(1.0);
//      zBottom_.addPropertyChangeListener("value", this);
    //  slicesPanel_.add(zBottom_);

//      setBottomButton_ = new JButton();
//      setBottomButton_.addActionListener(new ActionListener() {
//
//         @Override
//         public void actionPerformed(final ActionEvent e) {
//            setBottomPosition();
//         }
//      });
//      setBottomButton_.setMargin(new Insets(-5, -5, -5, -5));
//      setBottomButton_.setFont(new Font("", Font.PLAIN, 10));
//      setBottomButton_.setText("Set");
//      setBottomButton_.setToolTipText("Set value as microscope's current Z position");
//      setBottomButton_.setBounds(150, 27, 50, 22);
//    //  slicesPanel_.add(setBottomButton_);
//
//      final JLabel ztopLabel = new JLabel();
//      ztopLabel.setFont(new Font("Arial", Font.PLAIN, 10));
//      ztopLabel.setText("Z-end [um]");
//      ztopLabel.setBounds(30, 53, 69, 15);
    //  slicesPanel_.add(ztopLabel);

//      zTop_ = new JFormattedTextField(numberFormat_);
//      zTop_.setFont(new Font("Arial", Font.PLAIN, 10));
//      zTop_.setBounds(95, 50, 54, 21);
//      zTop_.setValue(1.0);
//      zTop_.addPropertyChangeListener("value", this);
//     // slicesPanel_.add(zTop_);
//
//      setTopButton_ = new JButton();
//      setTopButton_.addActionListener(new ActionListener() {
//
//         @Override
//         public void actionPerformed(final ActionEvent e) {
//            setTopPosition();
//         }
//      });
//      setTopButton_.setMargin(new Insets(-5, -5, -5, -5));
//      setTopButton_.setFont(new Font("Dialog", Font.PLAIN, 10));
//      setTopButton_.setText("Set");
//      setTopButton_.setToolTipText("Set value as microscope's current Z position");
//      setTopButton_.setBounds(150, 50, 50, 22);
    //  slicesPanel_.add(setTopButton_);

//      final JLabel zstepLabel = new JLabel();
//      zstepLabel.setFont(new Font("Arial", Font.PLAIN, 10));
//      zstepLabel.setText("Z-step [um]");
//      zstepLabel.setBounds(30, 76, 69, 15);
     // slicesPanel_.add(zstepLabel);

//      zStep_ = new JFormattedTextField(numberFormat_);
//      zStep_.setFont(new Font("Arial", Font.PLAIN, 10));
//      zStep_.setBounds(95, 73, 54, 21);
//      zStep_.setValue(1.0);
//      zStep_.addPropertyChangeListener("value", this);
    //  slicesPanel_.add(zStep_);

//      zValCombo_ = new JComboBox();
//      zValCombo_.setFont(new Font("Arial", Font.PLAIN, 10));
//      zValCombo_.addActionListener(new ActionListener() {
//
//         @Override
//         public void actionPerformed(final ActionEvent e) {
//            zValCalcChanged();
//         }
//      });
//      zValCombo_.setModel(new DefaultComboBoxModel(new String[]{"relative Z", "absolute Z"}));
//      zValCombo_.setBounds(30, 97, 110, 22);
     // slicesPanel_.add(zValCombo_);

//      stackKeepShutterOpenCheckBox_ = new JCheckBox();
//      stackKeepShutterOpenCheckBox_.setText("Keep shutter open");
//      stackKeepShutterOpenCheckBox_.setFont(new Font("Arial", Font.PLAIN, 10));
//      stackKeepShutterOpenCheckBox_.addActionListener(new ActionListener() {
//
//         @Override
//         public void actionPerformed(final ActionEvent e) {
//            applySettings();
//         }
//      });
//      stackKeepShutterOpenCheckBox_.setSelected(false);
//      stackKeepShutterOpenCheckBox_.setBounds(60, 121, 150, 22);
      //slicesPanel_.add(stackKeepShutterOpenCheckBox_);

      // Acquisition order panel

      acqOrderBox_ = new JComboBox();
      acqOrderBox_.setFont(new Font("", Font.PLAIN, 10));
      acqOrderBox_.setBounds(6, 36, 160, 22);
      //acquisitionOrderPanel_.add(acqOrderBox_);

      acqOrderModes_ = new AcqOrderMode[1];
      acqOrderModes_[0] = new AcqOrderMode(AcqOrderMode.SEGMENTATION_POS_KILL_CHANNEL);
//      acqOrderModes_[1] = new AcqOrderMode(AcqOrderMode.TIME_POS_CHANNEL_SLICE);
//      acqOrderModes_[2] = new AcqOrderMode(AcqOrderMode.POS_TIME_SLICE_CHANNEL);
//      acqOrderModes_[3] = new AcqOrderMode(AcqOrderMode.POS_TIME_CHANNEL_SLICE);
      acqOrderBox_.addItem(acqOrderModes_[0]);
//      acqOrderBox_.addItem(acqOrderModes_[1]);
//      acqOrderBox_.addItem(acqOrderModes_[2]);
//      acqOrderBox_.addItem(acqOrderModes_[3]);


      // Summary panel

      summaryTextArea_ = new JTextArea();
      summaryTextArea_.setFont(new Font("Arial", Font.PLAIN, 11));
      summaryTextArea_.setEditable(false);
      summaryTextArea_.setBounds(4, 19, 160, 200);
      summaryTextArea_.setMargin(new Insets(2, 2, 2, 2));
      summaryTextArea_.setLineWrap(true);
      summaryTextArea_.setWrapStyleWord(true);
      summaryTextArea_.setOpaque(false);
      summaryPanel_.add(summaryTextArea_);

      // Autofocus panel

//      afPanel_.addActionListener(new ActionListener() {
//
//         @Override
//         public void actionPerformed(ActionEvent arg0) {
//            applySettings();
//         }
//      });

//      afButton_ = new JButton();
//      afButton_.setToolTipText("Set autofocus options");
//      afButton_.addActionListener(new ActionListener() {
//
//         @Override
//         public void actionPerformed(ActionEvent arg0) {
//            afOptions();
//         }
//      });
//      afButton_.setText("Options...");
//      afButton_.setIcon(SwingResourceManager.getIcon(SeqAcqGui.class, "icons/wrench_orange.png"));
//      afButton_.setMargin(new Insets(2, 5, 2, 5));
//      afButton_.setFont(new Font("Dialog", Font.PLAIN, 10));
//      afButton_.setBounds(32, 33, 100, 28);
//      afPanel_.add(afButton_);


//      final JLabel afSkipFrame1 = new JLabel();
//      afSkipFrame1.setFont(new Font("Dialog", Font.PLAIN, 10));
//      afSkipFrame1.setText("Skip frame(s): ");
//      afSkipFrame1.setToolTipText(TooltipTextMaker.addHTMLBreaksForTooltip("The number of 'frames skipped' corresponds"
//              + "to the number of time intervals of image acquisition that pass before micromanager autofocuses again.  Micromanager "
//              + "will always autofocus when moving to a new position regardless of this value"));
//
//
//      afSkipFrame1.setBounds(20, 75, 70, 21);
//      afPanel_.add(afSkipFrame1);



      // Channels panel
      channelsPanel_.addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            if (!channelsPanel_.isSelected()){
               segmentationPanel_.setSelected(false);
               positionsPanel_.setSelected(false);
               savePanel_.setSelected(false);
            }
            applySettings();
         }
      });

      final JLabel channelsLabel = new JLabel();
      channelsLabel.setFont(new Font("Arial", Font.PLAIN, 10));
      channelsLabel.setBounds(20, 139, 80, 24);
      channelsLabel.setText("Channel group:");
      channelsPanel_.add(channelsLabel);


      channelGroupCombo_ = new JComboBox();
      channelGroupCombo_.setFont(new Font("", Font.PLAIN, 10));
      updateGroupsCombo();

      channelGroupCombo_.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent arg0) {
            String newGroup = (String) channelGroupCombo_.getSelectedItem();

            if (acqEng_.setChannelGroup(newGroup)) {
               model_.cleanUpConfigurationList();
               if (studio_.getAutofocusManager() != null) {
                  try {
                     studio_.getAutofocusManager().refresh();
                  } catch (MMException e) {
                     ReportingUtils.showError(e);
                  }
               }
            } else {
               updateGroupsCombo();
            }
         }
      });

      channelGroupCombo_.setBounds(100, 140, 150, 22);
      channelsPanel_.add(channelGroupCombo_);

      channelTablePane_ = new JScrollPane();
      channelTablePane_.setFont(new Font("Arial", Font.PLAIN, 10));
      channelTablePane_.setBounds(10, 25, 680, 104);
      channelsPanel_.add(channelTablePane_);

      final JButton addButton = new JButton();
      addButton.setFont(new Font("Arial", Font.PLAIN, 10));
      addButton.setMargin(new Insets(0, 0, 0, 0));
      addButton.addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            System.out.println(acqEng_.isDoSegmentationEnabled());
            model_.addNewChannel();
            model_.fireTableStructureChanged();
            applySettings();
         }
      });

      addButton.setText("New");
      addButton.setToolTipText("Create new channel for currently selected channel group");
      addButton.setBounds(330, 139, 68, 22);
      channelsPanel_.add(addButton);

      final JButton removeButton = new JButton();
      removeButton.setFont(new Font("Arial", Font.PLAIN, 10));
      removeButton.setMargin(new Insets(-5, -5, -5, -5));
      removeButton.addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            int sel = channelTable_.getSelectedRow();
            if (sel > -1) {

               model_.removeChannel(sel);
               model_.fireTableStructureChanged();
               if (channelTable_.getRowCount() > sel) {
                  channelTable_.setRowSelectionInterval(sel, sel);
               }
            }
            applySettings();
         }
      });
      removeButton.setText("Remove");
      removeButton.setToolTipText("Remove currently selected channel");
      removeButton.setBounds(420, 139, 68, 22);
      channelsPanel_.add(removeButton);

      final JButton upButton = new JButton();
      upButton.setFont(new Font("Arial", Font.PLAIN, 10));
      upButton.setMargin(new Insets(0, 0, 0, 0));
      upButton.addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            int sel = channelTable_.getSelectedRow();
            if (sel > -1) {
               applySettings();
               int newSel = model_.rowUp(sel);
               model_.fireTableStructureChanged();
               channelTable_.setRowSelectionInterval(newSel, newSel);

            }
            applySettings();
         }
      });
      upButton.setText("Up");
      upButton.setToolTipText(TooltipTextMaker.addHTMLBreaksForTooltip(
              "Move currently selected channel up (Channels higher on list are acquired first)"));
      upButton.setBounds(510, 139, 68, 22);
      channelsPanel_.add(upButton);

      final JButton downButton = new JButton();
      downButton.setFont(new Font("Arial", Font.PLAIN, 10));
      downButton.setMargin(new Insets(0, 0, 0, 0));
      downButton.addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            int sel = channelTable_.getSelectedRow();
            if (sel > -1) {
               applySettings();
               int newSel = model_.rowDown(sel);
               model_.fireTableStructureChanged();
               channelTable_.setRowSelectionInterval(newSel, newSel);
               applySettings();
            }
         }
      });
      downButton.setText("Down");
      downButton.setToolTipText(TooltipTextMaker.addHTMLBreaksForTooltip(
              "Move currently selected channel down (Channels lower on list are acquired later)"));
      downButton.setBounds(600, 139, 68, 22);
      channelsPanel_.add(downButton);

//      chanKeepShutterOpenCheckBox_ = new JCheckBox();
//      chanKeepShutterOpenCheckBox_.setText("Keep shutter open");
//      chanKeepShutterOpenCheckBox_.setFont(new Font("Arial", Font.PLAIN, 10));
//      chanKeepShutterOpenCheckBox_.addActionListener(new ActionListener() {
//
//         @Override
//         public void actionPerformed(final ActionEvent e) {
//            applySettings();
//         }
//      });
//      chanKeepShutterOpenCheckBox_.setSelected(false);
//      chanKeepShutterOpenCheckBox_.setBounds(260, 139, 150, 22);
//      channelsPanel_.add(chanKeepShutterOpenCheckBox_);


      // Save panel

      savePanel_.addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(final ActionEvent e) {
//            if (!savePanel_.isSelected()) {
//               displayModeCombo_.setSelectedIndex(0);
//            }
            applySettings();
         }
      });

      displayMode_ = new JLabel();
      displayMode_.setFont(new Font("Arial", Font.PLAIN, 10));
     // displayMode_.setText("Display");
      displayMode_.setBounds(150, 15, 49, 21);
      savePanel_.add(displayMode_);

      displayModeCombo_ = new JComboBox();
      displayModeCombo_.setFont(new Font("", Font.PLAIN, 10));
      displayModeCombo_.setBounds(188, 14, 150, 24);
//      displayModeCombo_.addItem(new DisplayMode(DisplayMode.ALL));
//      displayModeCombo_.addItem(new DisplayMode(DisplayMode.LAST_FRAME));
//      displayModeCombo_.addItem(new DisplayMode(DisplayMode.SINGLE_WINDOW));
      displayModeCombo_.setEnabled(false);
      displayModeCombo_.setVisible(false);
      savePanel_.add(displayModeCombo_);


      rootLabel_ = new JLabel();
      rootLabel_.setFont(new Font("Arial", Font.PLAIN, 10));
      rootLabel_.setText("Directory root");
      rootLabel_.setBounds(10, 30, 72, 22);
      savePanel_.add(rootLabel_);


      rootField_.setFont(new Font("Arial", Font.PLAIN, 10));
      rootField_.setBounds(90, 30, 354, 22);
      savePanel_.add(rootField_);

      browseRootButton_ = new JButton();
      browseRootButton_.addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(final ActionEvent e) {
            setRootDirectory();
         }
      });
      browseRootButton_.setMargin(new Insets(2, 5, 2, 5));
      browseRootButton_.setFont(new Font("Dialog", Font.PLAIN, 10));
      browseRootButton_.setText("...");
      browseRootButton_.setBounds(452, 30, 47, 24);
      savePanel_.add(browseRootButton_);
      browseRootButton_.setToolTipText("Browse");

      namePrefixLabel_ = new JLabel();
      namePrefixLabel_.setFont(new Font("Arial", Font.PLAIN, 10));
      namePrefixLabel_.setText("Name prefix");
      namePrefixLabel_.setBounds(10, 55, 76, 22);
      savePanel_.add(namePrefixLabel_);


      nameField_.setFont(new Font("Arial", Font.PLAIN, 10));
      nameField_.setBounds(90, 55, 354, 22);
      savePanel_.add(nameField_);

      saveTypeLabel_ = new JLabel("Saving format: ");
      saveTypeLabel_.setFont(new Font("Arial", Font.PLAIN, 10));
      saveTypeLabel_.setBounds(10,80, 100,22);
      savePanel_.add(saveTypeLabel_);


      singleButton_ = new JRadioButton("Separate image files");
      singleButton_.setFont(new Font("Arial", Font.PLAIN, 10));
      singleButton_.setBounds(90,80,150,22);
      savePanel_.add(singleButton_);
      singleButton_.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            saveMultiTiff_ = false;
         }});

      multiButton_ = new JRadioButton("Image stack file");
      multiButton_.setFont(new Font("Arial", Font.PLAIN, 10));
      multiButton_.setBounds(243,80,200,22);
      savePanel_.add(multiButton_);
      multiButton_.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            saveMultiTiff_ = true;
         }});

      ButtonGroup buttonGroup = new ButtonGroup();
      buttonGroup.add(singleButton_);
      buttonGroup.add(multiButton_);
      updateSavingTypeButtons();


       segmentationPanel_.addActionListener(new ActionListener() {
           @Override
           public void actionPerformed(ActionEvent e) {
               if (!segmentationPanel_.isSelected()) {
                   acqEng_.enableKillCell(false);
                   acqEng_.enableSegmentation(false);
               }
               applySettings();
           }
       });

      choose_segmenter = new JLabel();
      choose_segmenter.setFont(new Font("Arial", Font.PLAIN, 10));
      choose_segmenter.setText("Choose Segmenter Algorithm :");
      choose_segmenter.setBounds(10, 30, 150, 22);
      segmentationPanel_.add(choose_segmenter);


      String ListSegmenterAlgogGroup[] = Utils.getSegmenterAlgoListe();
      // / Liste of available Segmeter Algo
      listOfsegmenter_jcb = new JComboBox(ListSegmenterAlgogGroup);
      listOfsegmenter_jcb.setFont(new Font("", Font.PLAIN, 10));
      listOfsegmenter_jcb.setBounds(10, 60, 160, 22);
      segmentationPanel_.add(listOfsegmenter_jcb);



      xml_Seg_Label_2 = new JLabel();
      xml_Seg_Label_2.setFont(new Font("Arial", Font.PLAIN, 10));
      xml_Seg_Label_2.setText("XML configuration file :");
      xml_Seg_Label_2.setBounds(10, 90, 150, 22);
    //  segmentationPanel_.add(xml_Seg_Label_2);

      rootField_2 = new JTextField();
      rootField_2.setFont(new Font("Arial", Font.PLAIN, 10));
      rootField_2.setBounds(10, 120, 120, 22);
      //segmentationPanel_.add(rootField_2);

      browseRootButton_2 = new JButton();
      browseRootButton_2.addActionListener(new ActionListener() {

           @Override
           public void actionPerformed(final ActionEvent e) {
               setRootDirectory();
           }
       });
       browseRootButton_2.setMargin(new Insets(2, 5, 2, 5));
       browseRootButton_2.setFont(new Font("Dialog", Font.PLAIN, 10));
       browseRootButton_2.setText("...");
       browseRootButton_2.setBounds(135, 120, 35, 24);
    //   segmentationPanel_.add(browseRootButton_2);
       browseRootButton_2.setToolTipText("Browse");
      //////////////////////////////////////////////////////////////////////////////////////////////////

      JScrollPane commentScrollPane = new JScrollPane();
      commentScrollPane.setBounds(10, 28, 485, 50);
      commentsPanel_.add(commentScrollPane);


      commentScrollPane.setViewportView(commentTextArea_);
      commentTextArea_.setFont(new Font("", Font.PLAIN, 10));
      commentTextArea_.setToolTipText("Comment for the current acquistion");
      commentTextArea_.setWrapStyleWord(true);
      commentTextArea_.setLineWrap(true);
      commentTextArea_.setBorder(new EtchedBorder(EtchedBorder.LOWERED));




//      this.setBackground(Color.decode("#34495e"));
//      getContentPane().setBackground(Color.decode("#34495e"));
      // Main buttons
      final JButton closeButton = new JButton();
      closeButton.setFont(new Font("Arial", Font.PLAIN, 10));
      closeButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            saveSettings();
            saveAcqSettings();
       //     AcqControlDlg.this.dispose();
            studio_.makeActive();
         }
      });
      closeButton.setText("Close");
      closeButton.setBounds(40, 40, 80, 22);



     // buttonPanel.add(closeButton);
      progressBar = new JProgressBar(0, 100);
      //progressBar.setPreferredSize(new Dimension(180, 40));
      this.add(progressBar);
      progressBar.setBounds(20, 395, 120, 60);
      progressBar.setValue(0);
      progressBar.setStringPainted(true);

      taskOutput = new JTextArea(5, 20);
      taskOutput.setEditable(false);
      taskOutput.setToolTipText("Process progress calculation");
      taskOutput.setWrapStyleWord(true);
      taskOutput.setLineWrap(true);
      taskOutput.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
      JScrollPane jScrollPane_ = new JScrollPane(taskOutput);
      jScrollPane_.setBounds(155, 395, 330, 60);
      jScrollPane_.setViewportView(taskOutput);
      this.add(jScrollPane_);

      acquireButton_ = new JButton();
      acquireButton_.setActionCommand("start");
      acquireButton_.addActionListener(RappGui.getInstance());
      acquireButton_.setMargin(new Insets(-9, -9, -9, -9));
      acquireButton_.setFont(new Font("Arial", Font.BOLD, 12));
      acquireButton_.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
              AbstractCellEditor ae = (AbstractCellEditor) channelTable_.getCellEditor();
              if (ae != null) {
                  ae.stopCellEditing();
              }
              // Instances of javax.swing.SwingWorker are not reusuable, so
              // we create new instances as needed.

              SeqAcqGui.this.runAcquisition();

          }
      });

      acquireButton_.setText("Run Sequence!");
      acquireButton_.setBounds(25, 35, 120, 30);
      buttonPanel.add(acquireButton_);

      final JButton stopButton = new JButton();
      stopButton.addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(final ActionEvent e) {
            acqEng_.abortRequest();
         }
      });
      stopButton.setText("Stop");
      stopButton.setFont(new Font("Arial", Font.BOLD, 12));
      stopButton.setBounds(50, 70, 80, 30);
      buttonPanel.add(stopButton);



      final JButton loadButton = new JButton();
      loadButton.setFont(new Font("Arial", Font.PLAIN, 10));
      loadButton.setMargin(new Insets(-5, -5, -5, -5));
      loadButton.addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            loadAcqSettingsFromFile();
         }
      });

      loadButton.setText("Load...");
      loadButton.setBounds(50, 105, 80, 30);
      buttonPanel.add(loadButton);
      loadButton.setToolTipText("Load acquisition settings");

      final JButton saveAsButton = new JButton();
      saveAsButton.setFont(new Font("Arial", Font.PLAIN, 10));
      saveAsButton.addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            saveAsAcqSettingsToFile();
         }
      });
      saveAsButton.setToolTipText("Save current acquisition settings as");
      saveAsButton.setText("Save as...");
      saveAsButton.setBounds(50, 140, 80, 30);
      saveAsButton.setMargin(new Insets(-5, -5, -5, -5));
      buttonPanel.add(saveAsButton);

//      final JButton advancedButton = new JButton();
//      advancedButton.setFont(new Font("Arial", Font.PLAIN, 10));
//      advancedButton.addActionListener(e -> {
//         showAdvancedDialog();
//         updateGUIContents();
//      });
//
//      advancedButton.setText("Advanced");
//      advancedButton.setBounds(50, 175, 80, 30);
//      buttonPanel.add(advancedButton);



      // update GUI contents
      // -------------------

      // add update event listeners
      positionsPanel_.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent arg0) {
              applySettings();
          }
      });
      displayModeCombo_.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
              applySettings();
          }
      });
      acqOrderBox_.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
              applySettings();
          }
      });

      // load acquistion settings
      loadAcqSettings();

      // create the table of channels
      createChannelTable();

      // update summary
      updateGUIContents();

      // update settings in the acq engine
      applySettings();

      createToolTips();

      // load window position from prefs
    //  this.loadAndRestorePosition(100, 100);
    //  this.setSize(521, 690);

   }



   /**
    * Called when a field's "value" property changes.
    * Causes the Summary to be updated
    */
   @Override
   public void propertyChange(PropertyChangeEvent e) {
      // update summary
      applySettings();
      summaryTextArea_.setText(acqEng_.getVerboseSummary());

//      if ("progress" == e.getPropertyName()) {
//         int progress = (Integer) e.getNewValue();
//         progressBar.setValue(progress);
//         taskOutput.append(String.format("Completed %d%% of task.\n", task.getProgress()));
//      }
   }

   /**
    * Sets the exposure time of a given channel
    * The channel has to be preset in the current channel group
    * Will also update the exposure associated with this channel in the preferences,
    * i.e. even if the preset is not shown, this exposure time will be used
    * next time it is shown
    *
    * @param channelGroup - name of the channelgroup.  If it does not match the current
    * channel group, no action will be taken
    * @param channel - name of the preset in the current channel group
    * @param exposure  - new exposure time
    */
   public void setChannelExposureTime(String channelGroup, String channel,
           double exposure) {
      if (!channelGroup.equals(acqEng_.getChannelGroup()) ||
               acqEng_.getChannelConfigs().length <= 0) {
         return;
      }
      for (String config : acqEng_.getChannelConfigs()) {
         if (channel.equals(config)) {
            exposurePrefs_.putDouble("Exposure_" + acqEng_.getChannelGroup()
                    + "_" + channel, exposure);
            model_.setChannelExposureTime(channelGroup, channel, exposure);
         }
      }
   }

   /**
    * Returns exposure time for the desired preset in the given channelgroup
    * Acquires its info from the preferences
    *
    * @param channelGroup
    * @param channel -
    * @param defaultExp - default value
    * @return exposure time
    */
   public double getChannelExposureTime(String channelGroup, String channel,
           double defaultExp) {
      return exposurePrefs_.getDouble("Exposure_" + channelGroup
                          + "_" + channel, defaultExp);
   }

   protected void afOptions() {
      if (studio_.getAutofocusManager().getDevice() != null) {
         studio_.getAutofocusManager().showOptionsDialog();
      }
   }

   public boolean inArray(String member, String[] group) {
      for (int i = 0; i < group.length; i++) {
         if (member.equals(group[i])) {
            return true;
         }
      }
      return false;
   }


   public static   final void updateSavingTypeButtons() {
      if (!saveMultiTiff_){
         singleButton_.setSelected(true);
      } else if (saveMultiTiff_) {
         multiButton_.setSelected(true);
      }
   }

   public void close() {
      try {
         saveSettings();
      } catch (Throwable t) {
         ReportingUtils.logError(t, "in saveSettings");
      }
      try {
         saveAcqSettings();
      } catch (Throwable t) {
         ReportingUtils.logError(t, "in saveAcqSettings");
      }
      try {
         dispose();
      } catch (Throwable t) {
         ReportingUtils.logError(t, "in dispose");
      }
      if (null != studio_) {
         try {
            studio_.makeActive();
         } catch (Throwable t) {
            ReportingUtils.logError(t, "in makeActive");
         }
      }
   }

   public final void updateGroupsCombo() {
      String groups[] = acqEng_.getAvailableGroups();
      if (groups.length != 0) {
         channelGroupCombo_.setModel(new DefaultComboBoxModel(groups));
         if (!inArray(acqEng_.getChannelGroup(), groups)) {
            acqEng_.setChannelGroup(acqEng_.getFirstConfigGroup());
         }

         channelGroupCombo_.setSelectedItem(acqEng_.getChannelGroup());
      }
   }

   public void updateChannelAndGroupCombo() {
      updateGroupsCombo();
      model_.cleanUpConfigurationList();
   }

   public final synchronized void loadAcqSettings() {
      disableGUItoSettings_ = true;
      // load acquisition engine preferences
      acqEng_.clear();

      int unit = acqPrefs_.getInt(ACQ_TIME_UNIT, 0);
      //timeUnitCombo_.setSelectedIndex(unit);

      double bottom = acqPrefs_.getDouble(ACQ_ZBOTTOM, 0.0);
      double top = acqPrefs_.getDouble(ACQ_ZTOP, 0.0);


      acqEng_.enableMultiPosition(acqPrefs_.getBoolean(ACQ_ENABLE_MULTI_POSITION, acqEng_.isMultiPositionEnabled()));
      positionsPanel_.setSelected(acqEng_.isMultiPositionEnabled());
      positionsPanel_.repaint();

      acqEng_.enableChannelsSetting(acqPrefs_.getBoolean(ACQ_ENABLE_MULTI_CHANNEL, false));
      channelsPanel_.setSelected(acqEng_.isChannelsSettingEnabled());
      channelsPanel_.repaint();

      acqEng_.enableSegmentation(acqPrefs_.getBoolean(ACQ_ENABLE_SEGMENTATION, acqEng_.isDoSegmentationEnabled()));
      segmentationPanel_.setSelected(acqEng_.isDoSegmentationEnabled());
      segmentationPanel_.repaint();

      acqEng_.enableKillCell(acqPrefs_.getBoolean(ACQ_ENABLE_KILLCELL, acqEng_.isKillCellEnabled()));

      savePanel_.setSelected(acqPrefs_.getBoolean(ACQ_SAVE_FILES, false));

      nameField_.setText(acqPrefs_.get(ACQ_DIR_NAME, "Untitled"));
      String os_name = System.getProperty("os.name", "");
      rootField_.setText(acqPrefs_.get(ACQ_ROOT_NAME, System.getProperty("user.home") + "/AcquisitionData"));


      acqEng_.setDisplayMode(acqPrefs_.getInt(ACQ_DISPLAY_MODE, acqEng_.getDisplayMode()));
      acqEng_.enableAutoFocus(acqPrefs_.getBoolean(ACQ_AF_ENABLE, acqEng_.isAutoFocusEnabled()));
      acqEng_.setChannelGroup(acqPrefs_.get(ACQ_CHANNEL_GROUP, acqEng_.getFirstConfigGroup()));
     // afPanel_.setSelected(acqEng_.isAutoFocusEnabled());
     // acqEng_.keepShutterOpenForChannels(acqPrefs_.getBoolean(ACQ_CHANNELS_KEEP_SHUTTER_OPEN, false));
      //acqEng_.keepShutterOpenForStack(acqPrefs_.getBoolean(ACQ_STACK_KEEP_SHUTTER_OPEN, false));


      boolean doWholePlate = acqPrefs_.getBoolean(ACQ_ENABLE_WELL_PLATE, false);
      if (doWholePlate) {
         acqEng_.setWholePlate(
                 acqPrefs_.getInt(ACQ_NUMBER_X_WELLS, acqEng_.getNumberXWells()),
                 acqPrefs_.getInt(ACQ_NUMBER_Y_WELLS, acqEng_.getNumberYWells()),
                 acqPrefs_.getDouble(ACQ_WELL_WIDTH, acqEng_.getWellWidth()),
                 acqPrefs_.getDouble(ACQ_WELL_DISTANCE, acqEng_.getWellDistance()),
                 acqPrefs_.getDouble(ACQ_FIELD_OF_VIEW, acqEng_.getFieldOfView())
         );
      }

      ArrayList<Double> customIntervals = new ArrayList<Double>();
      int h = 0;
      while (acqPrefs_.getDouble(CUSTOM_INTERVAL_PREFIX + h, -1) >= 0.0) {
         customIntervals.add(acqPrefs_.getDouble(CUSTOM_INTERVAL_PREFIX + h, -1));
         h++;
      }
      double[] intervals = new double[customIntervals.size()];
      for (int j = 0; j < intervals.length; j++) {
         intervals[j] = customIntervals.get(j);
      }
      acqEng_.setCustomTimeIntervals(intervals);
      acqEng_.enableCustomTimeIntervals(acqPrefs_.getBoolean(ACQ_ENABLE_CUSTOM_INTERVALS, false));

      int numChannels = acqPrefs_.getInt(ACQ_NUM_CHANNELS, 0);

      ChannelSpec defaultChannel = new ChannelSpec();

      acqEng_.getChannels().clear();
      for (int i = 0; i < numChannels; i++) {
         String name = acqPrefs_.get(CHANNEL_NAME_PREFIX + i, "Undefined");
         boolean use = acqPrefs_.getBoolean(CHANNEL_USE_PREFIX + i, true);
         boolean useSeg = acqPrefs_.getBoolean(SEGMENTATION_USE_PREFIX +i,true);
         boolean killCell = acqPrefs_.getBoolean(KILL_CELL_PREFIX +i,true);
         double exp = acqPrefs_.getDouble(CHANNEL_EXPOSURE_PREFIX + i, 0.0);
         double laserExp = acqPrefs_.getDouble(CHANNEL_LASER_EXPOSURE_PREFIX + i, 0.0);
         ContrastSettings con = new ContrastSettings();
         con.min = acqPrefs_.getInt(CHANNEL_CONTRAST_MIN_PREFIX + i, defaultChannel.contrast.min);
         con.max = acqPrefs_.getInt(CHANNEL_CONTRAST_MAX_PREFIX + i, defaultChannel.contrast.max);
         con.gamma = acqPrefs_.getDouble(CHANNEL_CONTRAST_GAMMA_PREFIX + i, defaultChannel.contrast.gamma);
         int r = acqPrefs_.getInt(CHANNEL_COLOR_R_PREFIX + i, defaultChannel.color.getRed());
         int g = acqPrefs_.getInt(CHANNEL_COLOR_G_PREFIX + i, defaultChannel.color.getGreen());
         int b = acqPrefs_.getInt(CHANNEL_COLOR_B_PREFIX + i, defaultChannel.color.getBlue());
         Color c = new Color(r, g, b);
         acqEng_.addChannel(name, exp, laserExp, useSeg, killCell, con, c, use);
      }

      // Restore Column Width and Column order
      int columnCount = 7;
      columnWidth_ = new int[columnCount];
      columnOrder_ = new int[columnCount];
      for (int k = 0; k < columnCount; k++) {
         columnWidth_[k] = acqPrefs_.getInt(ACQ_COLUMN_WIDTH + k, ACQ_DEFAULT_COLUMN_WIDTH);
         columnOrder_[k] = acqPrefs_.getInt(ACQ_COLUMN_ORDER + k, k);
      }

      disableGUItoSettings_ = false;
   }

   public synchronized void saveAcqSettings() {
      try {
         acqPrefs_.clear();
      } catch (BackingStoreException e) {
         ReportingUtils.showError(e);
      }

      applySettings();

      //acqPrefs_.putBoolean(ACQ_ENABLE_MULTI_FRAME, acqEng_.isFramesSettingEnabled());
      acqPrefs_.putBoolean(ACQ_ENABLE_MULTI_CHANNEL, acqEng_.isChannelsSettingEnabled());
      //acqPrefs_.putInt(ACQ_NUMFRAMES, acqEng_.getNumFrames());
      //acqPrefs_.putDouble(ACQ_INTERVAL, acqEng_.getFrameIntervalMs());
      //acqPrefs_.putInt(ACQ_TIME_UNIT, timeUnitCombo_.getSelectedIndex());
      //acqPrefs_.putDouble(ACQ_ZBOTTOM, acqEng_.getSliceZBottomUm());
      //acqPrefs_.putDouble(ACQ_ZTOP, acqEng_.getZTopUm());
      //acqPrefs_.putDouble(ACQ_ZSTEP, acqEng_.getSliceZStepUm());
      //acqPrefs_.putBoolean(ACQ_ENABLE_SLICE_SETTINGS, acqEng_.isZSliceSettingEnabled());
      acqPrefs_.putBoolean(ACQ_ENABLE_MULTI_POSITION, acqEng_.isMultiPositionEnabled());
      acqPrefs_.putInt(ACQ_Z_VALUES, zVals_);
      acqPrefs_.putBoolean(ACQ_SAVE_FILES, savePanel_.isSelected());
      acqPrefs_.put(ACQ_DIR_NAME, nameField_.getText());
      acqPrefs_.put(ACQ_ROOT_NAME, rootField_.getText());

      acqPrefs_.putInt(ACQ_ORDER_MODE, acqEng_.getAcqOrderMode());

      acqPrefs_.putInt(ACQ_DISPLAY_MODE, acqEng_.getDisplayMode());
      acqPrefs_.putBoolean(ACQ_AF_ENABLE, acqEng_.isAutoFocusEnabled());
      //acqPrefs_.putInt(ACQ_AF_SKIP_INTERVAL, acqEng_.getAfSkipInterval());
     // acqPrefs_.putBoolean(ACQ_CHANNELS_KEEP_SHUTTER_OPEN, acqEng_.isShutterOpenForChannels());
      //acqPrefs_.putBoolean(ACQ_STACK_KEEP_SHUTTER_OPEN, acqEng_.isShutterOpenForStack());
      acqPrefs_.putBoolean(ACQ_ENABLE_WELL_PLATE, acqEng_.isWholePlateEnabled());
      acqPrefs_.putInt(ACQ_NUMBER_X_WELLS, acqEng_.getNumberXWells());
      acqPrefs_.putInt(ACQ_NUMBER_Y_WELLS, acqEng_.getNumberYWells());
      acqPrefs_.putDouble(ACQ_WELL_WIDTH, acqEng_.getWellWidth());
      acqPrefs_.putDouble(ACQ_WELL_DISTANCE, acqEng_.getWellDistance());
      acqPrefs_.putDouble(ACQ_FIELD_OF_VIEW, acqEng_.getFieldOfView());

      acqPrefs_.put(ACQ_CHANNEL_GROUP, acqEng_.getChannelGroup());
      ArrayList<ChannelSpec> channels = acqEng_.getChannels();
      acqPrefs_.putInt(ACQ_NUM_CHANNELS, channels.size());
      for (int i = 0; i < channels.size(); i++) {
         ChannelSpec channel = channels.get(i);
         acqPrefs_.put(CHANNEL_NAME_PREFIX + i, channel.config);
         acqPrefs_.putBoolean(CHANNEL_USE_PREFIX + i, channel.useChannel);
         acqPrefs_.putDouble(CHANNEL_EXPOSURE_PREFIX + i, channel.exposure);
        // acqPrefs_.putBoolean(CHANNEL_DOZSTACK_PREFIX + i, channel.doZStack);
        // acqPrefs_.putDouble(CHANNEL_ZOFFSET_PREFIX + i, channel.zOffset);
         acqPrefs_.putInt(CHANNEL_CONTRAST_MIN_PREFIX + i, channel.contrast.min);
         acqPrefs_.putInt(CHANNEL_CONTRAST_MAX_PREFIX + i, channel.contrast.max);
         acqPrefs_.putDouble(CHANNEL_CONTRAST_GAMMA_PREFIX + i, channel.contrast.gamma);
         acqPrefs_.putInt(CHANNEL_COLOR_R_PREFIX + i, channel.color.getRed());
         acqPrefs_.putInt(CHANNEL_COLOR_G_PREFIX + i, channel.color.getGreen());
         acqPrefs_.putInt(CHANNEL_COLOR_B_PREFIX + i, channel.color.getBlue());
       //  acqPrefs_.putInt(CHANNEL_SKIP_PREFIX + i, channel.skipFactorFrame);
      }

      //Save custom time intervals
      double[] customIntervals = acqEng_.getCustomTimeIntervals();
      if (customIntervals != null && customIntervals.length > 0) {
         for (int h = 0; h < customIntervals.length; h++) {
            acqPrefs_.putDouble(CUSTOM_INTERVAL_PREFIX + h, customIntervals[h]);
         }
      }

      acqPrefs_.putBoolean(ACQ_ENABLE_CUSTOM_INTERVALS, acqEng_.customTimeIntervalsEnabled());


      // Save model column widths and order
      for (int k = 0; k < model_.getColumnCount(); k++) {
         acqPrefs_.putInt(ACQ_COLUMN_WIDTH + k, findTableColumn(channelTable_, k).getWidth());
         acqPrefs_.putInt(ACQ_COLUMN_ORDER + k, channelTable_.convertColumnIndexToView(k));
      }
      try {
         acqPrefs_.flush();
      } catch (BackingStoreException ex) {
         ReportingUtils.logError(ex);
      }
   }

   // Returns the TableColumn associated with the specified column
   // index in the model
   public TableColumn findTableColumn(JTable table, int columnModelIndex) {
      Enumeration<?> e = table.getColumnModel().getColumns();
      for (; e.hasMoreElements();) {
         TableColumn col = (TableColumn) e.nextElement();
         if (col.getModelIndex() == columnModelIndex) {
            return col;
         }
      }
      return null;
   }

//   protected void enableZSliceControls(boolean state) {
//      zBottom_.setEnabled(state);
//      zTop_.setEnabled(state);
//      zStep_.setEnabled(state);
//      zValCombo_.setEnabled(state);
//   }

   protected void setRootDirectory() {
      File result = FileDialogs.openDir(RappGui.getInstance(),
              "Please choose a directory root for image data",
              MMStudio.MM_DATA_SET);
      if (result != null) {
         rootField_.setText(result.getAbsolutePath());
         acqEng_.setRootName(result.getAbsolutePath());
      }
   }

   public void setTopPosition() {
     // double z = acqEng_.getCurrentZPos();
     // zTop_.setText(NumberUtils.doubleToDisplayString(z));
      applySettings();
      // update summary
      summaryTextArea_.setText(acqEng_.getVerboseSummary());
   }

   protected void setBottomPosition() {
      //double z = acqEng_.getCurrentZPos();
      //zBottom_.setText(NumberUtils.doubleToDisplayString(z));
      applySettings();
      // update summary
      summaryTextArea_.setText(acqEng_.getVerboseSummary());
   }

   protected void loadAcqSettingsFromFile() {
      File f = FileDialogs.openFile(RappGui.getInstance(), "Load acquisition settings", ACQ_SETTINGS_FILE);
      if (f != null) {
         try {
            loadAcqSettingsFromFile(f.getAbsolutePath());
         } catch (MMScriptException ex) {
            ReportingUtils.showError("Failed to load Acquisition setting file");
         }
      }
   }

   public void loadAcqSettingsFromFile(String path) throws MMScriptException {
      acqFile_ = new File(path);
      try {
         FileInputStream in = new FileInputStream(acqFile_);
         acqPrefs_.clear();
         Preferences.importPreferences(in);
         loadAcqSettings();
         GUIUtils.invokeAndWait(new Runnable() {
            @Override
            public void run() {
               updateGUIContents();
            }
         });
         acqDir_ = acqFile_.getParent();
         if (acqDir_ != null) {
            prefs_.put(ACQ_FILE_DIR, acqDir_);
         }
      } catch (Exception e) {
         throw new MMScriptException (e);
      }
   }

   protected boolean saveAsAcqSettingsToFile() {
      saveAcqSettings();
      File f = FileDialogs.save(RappGui.getInstance(), "Save the acquisition settings file", ACQ_SETTINGS_FILE);
      if (f != null) {
         FileOutputStream os;
         try {
            os = new FileOutputStream(f);
            acqPrefs_.exportNode(os);
         } catch (FileNotFoundException e) {
            ReportingUtils.showError(e);
            return false;
         } catch (IOException e) {
            ReportingUtils.showError(e);
            return false;
         } catch (BackingStoreException e) {
            ReportingUtils.showError(e);
            return false;
         }
         return true;
      }
      return false;
   }

   private long estimateMemoryUsage() {
      // XXX This ought to be done by the acquisition engine
      boolean channels = channelsPanel_.isSelected();
     // boolean frames = framesPanel_.isSelected();
     // boolean slices = slicesPanel_.isSelected();

      boolean frames = false;
      boolean slices = false;
      boolean positions = positionsPanel_.isSelected();

      //int numFrames = Math.max(1, (Integer) numFrames_.getValue());
//      if (acqEng_.customTimeIntervalsEnabled()) {
//         int h = 0;
//         while (acqPrefs_.getDouble(CUSTOM_INTERVAL_PREFIX + h, -1) >= 0.0) {
//            h++;
//         }
//         numFrames = Math.max(1, h);
//      }

//      double zTop, zBottom, zStep;
//      try {
//         zTop = NumberUtils.displayStringToDouble(zTop_.getText());
//         zBottom = NumberUtils.displayStringToDouble(zBottom_.getText());
//         zStep = NumberUtils.displayStringToDouble(zStep_.getText());
//      } catch (ParseException ex) {
//         ReportingUtils.showError("Invalid Z-Stacks input value");
//         return -1;
//      }
//
//      int numSlices = Math.max(1, (int) (1 + Math.floor(
//              (Math.abs(zTop - zBottom) /  zStep))));

      int numPositions = 1;
      try {
         numPositions = Math.max(1, studio_.getPositionList().getNumberOfPositions());
      } catch (MMScriptException ex) {
         ReportingUtils.showError(ex);
      }

      int numImages;

      if (channels) {
         ArrayList<ChannelSpec> list = ((ChannelTableModel) channelTable_.getModel() ).getChannels();
         ArrayList<Integer> imagesPerChannel = new ArrayList<Integer>();
         for (int i = 0; i < list.size(); i++) {
            if (!list.get(i).useChannel )
               continue;
            int num = 1;
//            if (frames) {
//               num *= Math.max(1,numFrames / (list.get(i).skipFactorFrame + 1));
//            }
//            if (slices && list.get(i).doZStack) {
//               num *= numSlices;
//            }
            if (positions) {
               num *= numPositions;
            }
            imagesPerChannel.add(num);
         }
         numImages = 0;
         for (Integer i : imagesPerChannel) {
            numImages += i;
         }
      } else {
         numImages = 1;
         if (slices) {
          //  numImages *= numSlices;
         }
         if (frames) {
           // numImages *= numFrames;
         }
         if (positions) {
            numImages *= numPositions;
         }
      }

      CMMCore core = MMStudio.getInstance().getCore();
      long byteDepth = core.getBytesPerPixel();
      long width = core.getImageWidth();
      long height = core.getImageHeight();
      long bytesPerImage = byteDepth*width*height;

      return bytesPerImage * numImages;
   }

   // Returns false if user chooses to cancel.
   private boolean warnIfMemoryMayNotBeSufficient() {
      if (savePanel_.isSelected())
         return true;

      long acqTotalBytes = estimateMemoryUsage();
      if (acqTotalBytes < 0) {
         return false;
      }

      // Currently, images are stored in direct byte buffers in the case of
      // acquire-to-RAM. This means that the image (pixel and metadata) data do
      // not fill up the Java heap memory. The best we can do is to try to
      // estimate the available physical memory.
      //
      // In reality, there is a hard cap to the direct memory size in the
      // HotSpot JVM, but there is no way to get that limit, much less how much
      // of it is currently in use (there is the non-API
      // sun.misc.VM.maxDirectMemory() method, which we could call via
      // reflection where available, but we would need to estimate current
      // usage on our own). The limit can be set from the JVM command line
      // using e.g. -XX:MaxDirectMemorySize=16G.
      //
      // As of this writing, we ship the 64-bit version with
      // -XX:MaxDirectMemroySize=1000g, which essentially disables the limit,
      // so the assumptions we make here are not completely off.

      long freeRAM;
      java.lang.management.OperatingSystemMXBean osMXB =
         java.lang.management.ManagementFactory.getOperatingSystemMXBean();
      try { // Use HotSpot extensions if available
         Class<?> sunOSMXBClass = Class.forName("com.sun.management.OperatingSystemMXBean");
         java.lang.reflect.Method freeMemMethod = sunOSMXBClass.getMethod("getFreePhysicalMemorySize");
         freeRAM = ((Long) freeMemMethod.invoke(osMXB)).longValue();
      }
      catch (Exception e) {
         return true; // We just don't warn the user in this case.
      }

      // There is no hard reason for the 80% factor.
      if (acqTotalBytes > 0.8 * freeRAM) {
         int answer = JOptionPane.showConfirmDialog(this,
               "<html><body><p width='400'>" +
               "Available RAM may not be sufficient for this acquisition " +
               "(the estimate is approximate). After RAM is exhausted, the " +
               "acquisition may slow down or fail.</p>" +
               "<p>Would you like to start the acquisition anyway?</p>" +
               "</body></html>",
               "Insufficient memory warning",
               JOptionPane.YES_NO_OPTION);
         return answer == 0;
      }
      return true;
   }

   public String runAcquisition() {
      if (acqEng_.isAcquisitionRunning()) {
         JOptionPane.showMessageDialog(this, "Cannot start acquisition: previous acquisition still in progress.");
         return null;
      }
      // ImageWindow snap =  SnapLiveManager_.getSnapLiveWindow();
      if(SnapLiveManager_.getSnapLiveWindow() == null){
         JOptionPane.showMessageDialog(this, "Cannot start acquisition : No image Found,  Open Live window to acquire Image");
         return null;
      }

      if (!warnIfMemoryMayNotBeSufficient()) {
         JOptionPane.showMessageDialog(this, "Cannot start acquisition due to lack of disk space");
         return null;
      }

      try {
         applySettings();
         //saveAcqSettings(); // This is too slow.
         ChannelTableModel model = (ChannelTableModel) channelTable_.getModel();
         if (acqEng_.isChannelsSettingEnabled() && model.duplicateChannels()) {
            JOptionPane.showMessageDialog(this, "Cannot start acquisition using the same channel twice");
            return null;
         }
         return acqEng_.acquire();
      } catch (MMException e) {
         ReportingUtils.showError(e);
         return null;
      }
   }

   public String runAcquisition(String acqName, String acqRoot) {
      if (acqEng_.isAcquisitionRunning()) {
         JOptionPane.showMessageDialog(this, "Unable to start the new acquisition task: previous acquisition still in progress.");
         return null;
      }

      if (! warnIfMemoryMayNotBeSufficient()) {
         return null;
      }

      try {
         applySettings();
         ChannelTableModel model = (ChannelTableModel) channelTable_.getModel();
         if (acqEng_.isChannelsSettingEnabled() && model.duplicateChannels()) {
            JOptionPane.showMessageDialog(this, "Cannot start acquisition using the same channel twice");
            return null;
         }
         acqEng_.setDirName(acqName);
         acqEng_.setRootName(acqRoot);
         acqEng_.setSaveFiles(true);
         return acqEng_.acquire();
      } catch (MMException e) {
         ReportingUtils.showError(e);
         return null;
      }
   }

   public boolean isAcquisitionRunning() {
      return acqEng_.isAcquisitionRunning();
   }

   public static int search(int[] numbers, int key) {
      for (int index = 0; index < numbers.length; index++) {
         if (numbers[index] == key) {
            return index;
         }
      }
      return -1;
   }

//   private static void checkForCustomTimeIntervals() {
//      if (acqEng_.customTimeIntervalsEnabled()) {
//         framesSubPanelLayout_.show(framesSubPanel_, OVERRIDE_FRAMES_PANEL_NAME);
//      } else {
//         framesSubPanelLayout_.show(framesSubPanel_, DEFAULT_FRAMES_PANEL_NAME);
//      }
//   }

   public static final void updateGUIContents() {
      if (disableGUItoSettings_) {
         return;
      }
      disableGUItoSettings_ = true;
      // Disable update prevents action listener loops


      // TODO: remove setChannels()
      model_.setChannels(acqEng_.getChannels());

//      double intervalMs = acqEng_.getFrameIntervalMs();
//      interval_.setText(numberFormat_.format(convertMsToTime(intervalMs, timeUnitCombo_.getSelectedIndex())));

//      zBottom_.setText(NumberUtils.doubleToDisplayString(acqEng_.getSliceZBottomUm()));
//      zTop_.setText(NumberUtils.doubleToDisplayString(acqEng_.getZTopUm()));
//      zStep_.setText(NumberUtils.doubleToDisplayString(acqEng_.getSliceZStepUm()));

    //  boolean framesEnabled = acqEng_.isFramesSettingEnabled();
      //framesPanel_.setSelected(framesEnabled);
//      Component[] comps = framesSubPanel_.getComponents();
//      for (Component c: comps)
//         for (Component co: ((JPanel)c).getComponents() )
//            co.setEnabled(framesEnabled);


     // checkForCustomTimeIntervals();
     // slicesPanel_.setSelected(acqEng_.isZSliceSettingEnabled());
      positionsPanel_.setSelected(acqEng_.isMultiPositionEnabled());
      segmentationPanel_.setSelected(acqEng_.isDoSegmentationEnabled());
      //afPanel_.setSelected(acqEng_.isAutoFocusEnabled());
//      acqOrderBox_.setEnabled(positionsPanel_.isSelected() || framesPanel_.isSelected()
//        || slicesPanel_.isSelected()      || channelsPanel_.isSelected());
      acqOrderBox_.setEnabled(positionsPanel_.isSelected() || segmentationPanel_.isSelected() ||  channelsPanel_.isSelected());

      //afSkipInterval_.setEnabled(acqEng_.isAutoFocusEnabled());

      // These values need to be cached or we will loose them due to the Spinners OnChanged methods calling applySetting
//      Integer numFrames = acqEng_.getNumFrames();
//      Integer afSkipInterval = acqEng_.getAfSkipInterval();
//      if (acqEng_.isFramesSettingEnabled()) {
//         numFrames_.setValue(numFrames);
//      }

//      afSkipInterval_.setValue(afSkipInterval);
//
//      enableZSliceControls(acqEng_.isZSliceSettingEnabled());
      model_.fireTableStructureChanged();

      channelGroupCombo_.setSelectedItem(acqEng_.getChannelGroup());
//      try {
//         displayModeCombo_.setSelectedIndex(acqEng_.getDisplayMode());
//      } catch (IllegalArgumentException e) {
//         displayModeCombo_.setSelectedIndex(0);
//      }


      for (AcqOrderMode mode : acqOrderModes_) {
         ArrayList<ChannelSpec> channels = acqEng_.getChannels();

         if (channels.iterator().hasNext()) {
            mode.setEnabled(segmentationPanel_.isSelected(), positionsPanel_.isSelected(), channels.iterator().next().KillCell, channelsPanel_.isSelected());
         }else {
            mode.setEnabled(segmentationPanel_.isSelected(), positionsPanel_.isSelected(), false, channelsPanel_.isSelected());
         }
      }

      // add correct acquisition order options
      int selectedIndex = acqEng_.getAcqOrderMode();
      acqOrderBox_.removeAllItems();
//      if (framesPanel_.isSelected() && positionsPanel_.isSelected()
//               && channelsPanel_.isSelected()) {
//         acqOrderBox_.addItem(acqOrderModes_[0]);
//         acqOrderBox_.addItem(acqOrderModes_[1]);
//         acqOrderBox_.addItem(acqOrderModes_[2]);
//       //  acqOrderBox_.addItem(acqOrderModes_[3]);
//      } else if (framesPanel_.isSelected() && positionsPanel_.isSelected()) {
//         if (selectedIndex == 0 || selectedIndex == 2) {
//            acqOrderBox_.addItem(acqOrderModes_[0]);
//            acqOrderBox_.addItem(acqOrderModes_[2]);
//         } else {
//            acqOrderBox_.addItem(acqOrderModes_[1]);
//            acqOrderBox_.addItem(acqOrderModes_[3]);
//         }
//      } else if (channelsPanel_.isSelected() && slicesPanel_.isSelected()) {
//         if (selectedIndex == 0 || selectedIndex == 1) {
//            acqOrderBox_.addItem(acqOrderModes_[0]);
//            acqOrderBox_.addItem(acqOrderModes_[1]);
//         } else {
//            acqOrderBox_.addItem(acqOrderModes_[2]);
//            acqOrderBox_.addItem(acqOrderModes_[3]);
//         }
//      } else {
         acqOrderBox_.addItem(acqOrderModes_[selectedIndex]);
         //  }

      acqOrderBox_.setSelectedItem(acqOrderModes_[acqEng_.getAcqOrderMode()]);


     // zValCombo_.setSelectedIndex(zVals_);
     // stackKeepShutterOpenCheckBox_.setSelected(acqEng_.isShutterOpenForStack());
    //  chanKeepShutterOpenCheckBox_.setSelected(acqEng_.isShutterOpenForChannels());

      channelTable_.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

      boolean selected = channelsPanel_.isSelected();
      channelTable_.setEnabled(selected);
      channelTable_.getTableHeader().setForeground(selected ? Color.black : Color.gray);

      updateSavingTypeButtons();

      // update summary
      summaryTextArea_.setText(acqEng_.getVerboseSummary());

      disableGUItoSettings_ = false;
   }

   private void updateDoubleValue(double value, JTextField field) {
       try {
           if (NumberUtils.displayStringToDouble(field.getText()) != value) {
               field.setText(NumberUtils.doubleToDisplayString(value));
           }
       } catch (ParseException e) {
           field.setText(NumberUtils.doubleToDisplayString(value));
       }
   }

   private void updateCheckBox(boolean setting,  CheckBoxPanel panel) {
      if (panel.isSelected() != setting) {
          panel.setSelected(setting);
      }
   }

   @Override
   public void settingsChanged() {
   }

   protected static void applySettings() {
      if (disableGUItoSettings_) {
         return;
      }
      disableGUItoSettings_ = true;

      AbstractCellEditor ae = (AbstractCellEditor) channelTable_.getCellEditor();
      if (ae != null) {
         ae.stopCellEditing();
      }

      // double zStep = NumberUtils.displayStringToDouble(zStep_.getText());
//         if (Math.abs(zStep) < acqEng_.getMinZStepUm()) {
//            zStep = acqEng_.getMinZStepUm();
//         }
//         acqEng_.setSlices(NumberUtils.displayStringToDouble(zBottom_.getText()), NumberUtils.displayStringToDouble(zTop_.getText()), zStep, zVals_ == 0 ? false : true);
//        // acqEng_.enableZSliceSetting(slicesPanel_.isSelected());
      acqEng_.enableMultiPosition(positionsPanel_.isSelected());


      acqEng_.enableChannelsSetting(channelsPanel_.isSelected());
      acqEng_.setChannels(((ChannelTableModel) channelTable_.getModel()).getChannels());
      acqEng_.enableSegmentation(segmentationPanel_.isSelected());
      ArrayList<ChannelSpec> channels = acqEng_.getChannels();
      if (channels.iterator().hasNext()) {
         acqEng_.enableKillCell(channels.iterator().next().KillCell);
      }

      //acqEng_.enableFramesSetting(framesPanel_.isSelected());
//      acqEng_.enableFramesSetting(false);
//      acqEng_.setFrames((Integer) numFrames_.getValue(),
//      convertTimeToMs(NumberUtils.displayStringToDouble(interval_.getText()), timeUnitCombo_.getSelectedIndex()));
      //   acqEng_.setAfSkipInterval(NumberUtils.displayStringToInt(afSkipInterval_.getValue().toString()));
      //  acqEng_.keepShutterOpenForChannels(chanKeepShutterOpenCheckBox_.isSelected());
      //  acqEng_.keepShutterOpenForStack(stackKeepShutterOpenCheckBox_.isSelected());

      acqEng_.setSaveFiles(savePanel_.isSelected());
      // avoid dangerous characters in the name that will be used as a directory name
      String name = nameField_.getText().replaceAll("[/\\*!':]", "-");
      acqEng_.setDirName(name);
      acqEng_.setRootName(rootField_.getText());

      // update summary

      acqEng_.setComment(commentTextArea_.getText());

     // acqEng_.enableAutoFocus(afPanel_.isSelected());

      disableGUItoSettings_ = false;
      updateGUIContents();
   }


    private String chooseWellPlate() {
        int n = JOptionPane.showConfirmDialog(this,
                "Do you Have a 384 well plate ?", "Choose Plate", JOptionPane.YES_NO_CANCEL_OPTION);
        if (n == JOptionPane.YES_OPTION) {
            return "384";
        }else if (n == JOptionPane.CANCEL_OPTION) {
            ReportingUtils.showMessage("Canceled : NO Plate were Chosen");
            return "cancel" ;
        }
        else  {
            int n1 = JOptionPane.showConfirmDialog(this,
                    "Do you have a 96 well plate?", "Choose plate", JOptionPane.YES_NO_CANCEL_OPTION);
            if (n1 == JOptionPane.YES_OPTION) {
                return "96";
            }
            else if (n == JOptionPane.NO_OPTION) {
                ReportingUtils.showMessage("Sorry: There is no Other Options, Please Try Again");
                return "cancel" ;
            }
            else ReportingUtils.showMessage("Canceled : NO plate were Chosen");
        }
        return "cancel" ;

    }

   /**
    * Save settings to application properties.
    *
    */
   private void saveSettings() {
    //  this.savePosition();
   }

   private double convertTimeToMs(double interval, int units) {
      if (units == 1) {
         return interval * 1000; // sec
      } else if (units == 2) {
         return interval * 60.0 * 1000.0; // min
      } else if (units == 0) {
         return interval; // ms
      }
      ReportingUtils.showError("Unknown units supplied for acquisition interval!");
      return interval;
   }

   private double convertMsToTime(double intervalMs, int units) {
      if (units == 1) {
         return intervalMs / 1000; // sec
      } else if (units == 2) {
         return intervalMs / (60.0 * 1000.0); // min
      } else if (units == 0) {
         return intervalMs; // ms
      }
      ReportingUtils.showError("Unknown units supplied for acquisition interval!");
      return intervalMs;
   }

//   private void zValCalcChanged() {
//      if (zValCombo_.getSelectedIndex() == 0) {
//         setTopButton_.setEnabled(false);
//         setBottomButton_.setEnabled(false);
//      } else {
//         setTopButton_.setEnabled(true);
//         setBottomButton_.setEnabled(true);
//      }
//
//      if (zVals_ == zValCombo_.getSelectedIndex()) {
//         return;
//      }
//
//      zVals_ = zValCombo_.getSelectedIndex();
//      double zBottomUm, zTopUm;
//      try {
//         zBottomUm = NumberUtils.displayStringToDouble(zBottom_.getText());
//         zTopUm = NumberUtils.displayStringToDouble(zTop_.getText());
//      } catch (ParseException e) {
//         ReportingUtils.logError(e);
//         return;
//      }
//
//    //  double curZ = acqEng_.getCurrentZPos();
//
//      double newTop, newBottom;
////      if (zVals_ == 0) {
////         // convert from absolute to relative
////         newTop = zTopUm - curZ;
////         newBottom = zBottomUm - curZ;
////      } else {
////         // convert from relative to absolute
////         newTop = zTopUm + curZ;
////         newBottom = zBottomUm + curZ;
////      }
////      zBottom_.setText(NumberUtils.doubleToDisplayString(newBottom));
////      zTop_.setText(NumberUtils.doubleToDisplayString(newTop));
//      applySettings();
//   }

   /**
    * This method is called from the Options dialog, to set the background style
    */
   public void setBackgroundStyle(String style) {
      setBackground(guiColors_.background.get(style));
      repaint();
   }

   private void showAdvancedDialog() {
      if (advancedOptionsWindow_ == null) {
      //   advancedOptionsWindow_ = new AdvancedOptionsDialog(acqEng_,studio_);
      }
      advancedOptionsWindow_.setVisible(true);
   }

   @SuppressWarnings("serial")
   public class ComponentTitledPanel extends JPanel {
      public ComponentTitledBorder compTitledBorder;
      public boolean borderSet_ = false;
      public Component titleComponent;

      @Override
      public void setBorder(Border border) {
         if (compTitledBorder != null && borderSet_) {
            compTitledBorder.setBorder(border);
         } else {
            super.setBorder(border);
         }
      }

      @Override
      public Border getBorder() {
         return compTitledBorder;
      }

      public void setTitleFont(Font font) {
         titleComponent.setFont(font);
      }
   }

   @SuppressWarnings("serial")
   public class LabelPanel extends ComponentTitledPanel {
      LabelPanel(String title) {
         super();
         titleComponent = new JLabel(title);
         JLabel label = (JLabel) titleComponent;
         label.setOpaque(true);
         label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
         compTitledBorder = new ComponentTitledBorder(label, this, BorderFactory.createEtchedBorder());
         this.setBorder(compTitledBorder);
         borderSet_ = true;
      }

      public void writeObject(ObjectOutputStream stream) throws MMException  {
         throw new MMException("Do not serialize this class");
      }
   }

   @SuppressWarnings("serial")
   public class CheckBoxPanel extends ComponentTitledPanel {

      JCheckBox checkBox;

      CheckBoxPanel(String title) {
         super();
         titleComponent = new JCheckBox(title);
         checkBox = (JCheckBox) titleComponent;

         compTitledBorder = new ComponentTitledBorder(checkBox, this, BorderFactory.createEtchedBorder());
         this.setBorder(compTitledBorder);
         borderSet_ = true;

         final CheckBoxPanel thisPanel = this;

         checkBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               boolean enable = checkBox.isSelected();
               thisPanel.setChildrenEnabled(enable);
            }

            public void writeObject(ObjectOutputStream stream) throws MMException  {
               throw new MMException("Do not serialize this class");
            }
         });
      }

      public void setChildrenEnabled(boolean enabled) {
         Component comp[] = this.getComponents();
         for (int i = 0; i < comp.length; i++) {
            if (comp[i].getClass().equals(JPanel.class)) {
               Component subComp[] = ((JPanel) comp[i]).getComponents();
               for (int c = 0; c < subComp.length; c++) {
                  subComp[c].setEnabled(enabled);
               }
            } else {
               comp[i].setEnabled(enabled);
            }
         }
      }

      public boolean isSelected() {
         return checkBox.isSelected();
      }

      public void setSelected(boolean selected) {
         checkBox.setSelected(selected);
         setChildrenEnabled(selected);
      }

      public void addActionListener(ActionListener actionListener) {
         checkBox.addActionListener(actionListener);
      }

      public void removeActionListeners() {
         for (ActionListener l : checkBox.getActionListeners()) {
            checkBox.removeActionListener(l);
         }
      }
   }
}
