///////////////////////////////////////////////////////////////////////////////
//FILE:          RappPlugin.java
//PROJECT:       Micro-Manager Laser Automated Plugin
//SUBSYSTEM:     RAPP plugin
//-----------------------------------------------------------------------------
//AUTHOR:        FLorial,
//SOURCE :       ProjectorPlugin, Arthur Edelstein
//COPYRIGHT:     ZMBH, University of Heidelberg, 2017-2018
//LICENSE:       This file is distributed under the
/////////////////////////////////////////////////////////////////////////////////

package org.micromanager.rapp;


import ij.IJ;
import ij.ImagePlus;
import ij.gui.ImageWindow;
import mmcorej.CMMCore;
import org.micromanager.MMOptions;
import org.micromanager.MMStudio;
import org.micromanager.SnapLiveManager;
import org.micromanager.api.ScriptInterface;
import org.micromanager.internalinterfaces.LiveModeListener;
import org.micromanager.rapp.CellSegmentation.CellPointInternalFrame;
import org.micromanager.rapp.SequenceAcquisitions.SeqAcqController;
import org.micromanager.rapp.SequenceAcquisitions.SeqAcqGui;
import org.micromanager.rapp.utils.FileDialog;
import org.micromanager.rapp.utils.ImageViewer;
import org.micromanager.utils.GUIUtils;
import org.micromanager.utils.ReportingUtils;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.plaf.basic.BasicInternalFrameUI;
import javax.swing.plaf.metal.MetalToggleButtonUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.util.Objects;
import java.util.prefs.Preferences;

/**
 * @author FLorial
 */

public class RappGui extends JFrame implements LiveModeListener {
    private Preferences mainPrefs_;
    private MMOptions options_;
    private SeqAcqGui acquisition_;
    //private SimpleObjectProperty<SeqAcqGui> acquisition_ = new SimpleObjectProperty<>(this, "acquisition_");
    private CellPointInternalFrame tablePointFrame;
    private static ImageViewer imageViewer_;
    private static RappGui appInterface_;
    public static RappGui getInstance() {
        return appInterface_;
    }
    private  static  RappController rappController_ref;
    private SnapLiveManager SnapLiveManager_;
    private URL default_path = this.getClass().getResource("");
    private String path = default_path.toString().substring(6);
    private JPanel right_box_setup = new JPanel();
    private Box right_box_learning  = Box.createVerticalBox();
    private JPanel right_box_shoot = new JPanel();
    public  JLabel spiner = new JLabel("nothing");
    private SpinnerModel model_forExposure = new SpinnerNumberModel(100, 0, 999999, 1);
    private SpinnerModel model_forDelay = new SpinnerNumberModel(0, 0, 9999, 1);
    private SpinnerModel model_forFilterExposure = new SpinnerNumberModel(100, 0, 999999, 1);
    private JSpinner exposureT_laser_spinner;
    private JSpinner exposureT_filter_spinner;
    protected JSpinner delayField_ = new JSpinner(model_forDelay);
    private JToggleButton lightOnOff_jbtn = new JToggleButton("Open Light");
    private JInternalFrame asButtonPanel = new JInternalFrame();
    private JToggleButton LiveMode_btn;
    private JToggleButton pointAndShootOnOff_btn;
    protected JButton calibrate_btn;
    private JButton browseXmlFIle_btn;
     private JButton runSegmentation_btn;
    private JComboBox<String> presetConfList_jcb ;
    private JComboBox<String> Sequence_jcb ;
    private JComboBox<String> groupConfList_jcb;
    private JTextField xml_rootField_2;
    private JLabel  jLabel_Image;
    // the index of the images
    private int pos = 0;
    MMStudio studio_;




     /**
     * Constructor. Creates the main window for the Projector plugin. we use this Class for the main interface
     */
    public RappGui(CMMCore core, ScriptInterface app) throws Exception {
        studio_ = (MMStudio) app;
        org.micromanager.rapp.utils.FileDialog fileDialog_ = new FileDialog();
        //AcquisitionManager acqMgr_ = new AcquisitionManager();
        SeqAcqController engine_ = new SeqAcqController();
        rappController_ref =  new RappController(core, app);
        SnapLiveManager_ = new SnapLiveManager(studio_, core);
        this.options_ = new MMOptions();
        try {
            this.mainPrefs_ = Preferences.userNodeForPackage(this.getClass());
        } catch (Exception var8) {
            ReportingUtils.logError(var8);
        }

        try {
            this.options_.loadSettings();
        } catch (NullPointerException var9) {
            ReportingUtils.logError(var9);
        }

        try
        {
            for (UIManager.LookAndFeelInfo lnf :
                    UIManager.getInstalledLookAndFeels()) {
                if ("Metal".equals(lnf.getName())) {
                    UIManager.setLookAndFeel(lnf.getClassName());
                    break;
                }
            }
        } catch (Exception e) { /* Lazy handling this >.> */ }
        UIManager.getDefaults().put("SplitPane.border", BorderFactory.createEmptyBorder());

//        try {
//            setDefaultLookAndFeelDecorated(true);
//            UIManager.setLookAndFeel("com.jtattoo.plaf.smart.SmartLookAndFeel");
//
//        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
//           e.printStackTrace();
//        }

        this.setTitle("Rapp UGA-42 Control");

        ImageIcon icon = new ImageIcon(path.concat("Resources/camera.png"));
        this.setIconImage(icon.getImage());


        this.setLayout(new BorderLayout());
        FlowLayout experimentLayout = new FlowLayout();

        /////////////////////// TOp Panel //////////////////////////////////////
        JPanel topPanel = new JPanel();
        topPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.decode("#7f8fa6"), Color.decode("#192a56")));
        topPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Welcome..." ,0,0,Font.getFont("arial"),  Color.decode("#192a56") ));
        topPanel.setBackground(Color.decode("#ecf0f1"));

        JLabel headerLabel = new JLabel("<html><font color='#34495e'> Interface for controlling microscope and laser system </font></html>" );
        headerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        topPanel.add(headerLabel);
        ///////////// Left Panel Content ////////////////////////////
        JPanel leftPanel = new JPanel();
        leftPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.decode("#34495e"), Color.decode("#ecf0f1")));
        leftPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Menu" ,0,0,Font.getFont("arial"),  Color.white ));

        leftPanel.setBackground(Color.decode("#34495e"));
        ///////////// we put all the component from the left into a Horizontal Box //////////////////////////////////
        Box left_box = Box.createVerticalBox();
        leftPanel.add(left_box);
        left_box.setPreferredSize(new Dimension(150, 400));   // vertical box
        left_box.setBackground(Color.decode("#34495e"));

        left_box.add(Box.createVerticalStrut(10));

        JToggleButton setupOption_btn =  createJButton("Settings");
        JToggleButton learnOption_btn = createJButton("Analysis");
        JToggleButton shootOption_btn = createJButton("Point And Shoot");
        JToggleButton acquisitionOption_btn = createJButton("Sequence Acquisition");

       // setupOption_btn.setMaximumSize(new Dimension(145, 50));



        left_box.add(setupOption_btn);
       // setupOption_btn.setBackground(Color.decode("#ecf0f1"));
        setupOption_btn.setBorder(null);
        setupOption_btn.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                right_box_setup.setVisible(true);
                right_box_shoot.setVisible(false);
                right_box_learning.setVisible(false);
                asButtonPanel.setVisible(false);
                if( setupOption_btn.isSelected() ){
                    // We set the other button color just to distinguish
                    setupOption_btn.setForeground(Color.decode("#2980b9"));
                    shootOption_btn.setForeground(Color.decode("#ecf0f1"));
                    learnOption_btn.setForeground(Color.decode("#ecf0f1"));
                    acquisitionOption_btn.setForeground(Color.decode("#ecf0f1"));
                    // we do have to set the other bButton selection to False
                    shootOption_btn.setSelected(false);
                    learnOption_btn.setSelected(false);
                    acquisitionOption_btn.setSelected(false);

                }

            }
        } );

        left_box.add(Box.createVerticalStrut(15));

        // Leaning BUTTON, OPEN THE BOX OPTION TO MANAGE the MACHINE LEARNING PART
        left_box.add(learnOption_btn);
        learnOption_btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                right_box_learning.setVisible(true);
                right_box_shoot.setVisible(false);
                right_box_setup.setVisible(false);
                asButtonPanel.setVisible(false);
                if( learnOption_btn.isSelected() ){
                    // We set the other button color just to distinguish
                    learnOption_btn.setForeground(Color.decode("#2980b9"));
                    shootOption_btn.setForeground(Color.decode("#ecf0f1"));
                    setupOption_btn.setForeground(Color.decode("#ecf0f1"));
                    acquisitionOption_btn.setForeground(Color.decode("#ecf0f1"));
                    // we do have to set the other bButton selection to False
                    shootOption_btn.setSelected(false);
                    setupOption_btn.setSelected(false);
                    acquisitionOption_btn.setSelected(false);
                }
            }
        });
        left_box.add(Box.createVerticalStrut(15));

        left_box.add(acquisitionOption_btn);
        acquisitionOption_btn.addActionListener(e -> {
            asButtonPanel.setVisible(true);
            right_box_shoot.setVisible(false);
            right_box_setup.setVisible(false);
            right_box_learning.setVisible(false);

            if( acquisitionOption_btn.isSelected() ){
                // We set the other button color just to distinguish
                acquisitionOption_btn.setForeground(Color.decode("#2980b9"));
                setupOption_btn.setForeground(Color.decode("#ecf0f1"));
                learnOption_btn.setForeground(Color.decode("#ecf0f1"));
                shootOption_btn.setForeground(Color.decode("#ecf0f1"));
                // we do have to set the other bButton selection to False
                setupOption_btn.setSelected(false);
                learnOption_btn.setSelected(false);
                shootOption_btn.setSelected(false);
            }
        });


        left_box.add(Box.createVerticalStrut(15));

        // SHOOT BUTTON, OPEN THE BOX OPTION TO MANAGE SHOOT
        left_box.add(shootOption_btn);
        shootOption_btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                right_box_shoot.setVisible(true);
                right_box_setup.setVisible(false);
                right_box_learning.setVisible(false);
                asButtonPanel.setVisible(false);
                if( shootOption_btn.isSelected() ){
                    shootOption_btn.setForeground(Color.decode("#2980b9"));
                    setupOption_btn.setForeground(Color.decode("#ecf0f1"));
                    learnOption_btn.setForeground(Color.decode("#ecf0f1"));
                    acquisitionOption_btn.setForeground(Color.decode("#ecf0f1"));
                    // we do have to set the other Button selection to False
                    setupOption_btn.setSelected(false);
                    learnOption_btn.setSelected(false);
                    acquisitionOption_btn.setSelected(false);
                }


            }
        } );

        left_box.add(Box.createVerticalStrut(15));
        left_box.add( new JSeparator(SwingConstants.HORIZONTAL) , left_box,8);
        left_box.getComponent(8).setPreferredSize(new Dimension(150,2));

        // Start or Stop the the Live Mode
        LiveMode_btn = new JToggleButton("Start Live View");
        LiveMode_btn.setMaximumSize(new Dimension(145, 50));
        LiveMode_btn.setBackground(Color.decode("#27ae60"));
        LiveMode_btn.setForeground(Color.white);
        left_box.add(LiveMode_btn);
        LiveMode_btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (LiveMode_btn.isSelected() ){
                    LiveMode_btn.setUI(new MetalToggleButtonUI() {
                        @Override
                        protected Color getSelectColor() {
                            return Color.decode("#d35400");
                        }
                    });
                    LiveMode_btn.setText("Close Live View");
                    MMStudio.getInstance().enableLiveMode(true);

                }else {
                    LiveMode_btn.setText("Start Live View");
                    LiveMode_btn.setBackground(Color.decode("#27ae60"));
                    MMStudio.getInstance().enableLiveMode(false);
                    ImageWindow snap =  SnapLiveManager_.getSnapLiveWindow();
                    snap.close();
                }
               // createFrame();
              //   LiveModeButton(e);
            }
        } );

        left_box.add(Box.createVerticalStrut(5));
        // Start or Stop the the Live Mode
        JButton snapAndSave_btn = new JButton("Snap And Save Image");
        snapAndSave_btn.setMaximumSize(new Dimension(145, 50));
        snapAndSave_btn.setBackground(Color.decode("#3498db"));
        snapAndSave_btn.setForeground(Color.white);
        left_box.add(snapAndSave_btn);
        snapAndSave_btn.addActionListener(e -> {
          //  rappController_ref.snapAndSaveImage();
        });
        left_box.add(Box.createVerticalStrut(5));
        // Illuminate the center of the photo targeting device's range
        JButton showCenterSpot_btn = new JButton("Show Center Spot");
        showCenterSpot_btn.setBackground(Color.decode("#3498db"));
        showCenterSpot_btn.setMaximumSize((new Dimension(145, 50)));
        left_box.add(showCenterSpot_btn);
        showCenterSpot_btn.addActionListener(e -> rappController_ref.displayCenterSpot());


        ////////////////////// #Mange Right panel an Content here#  /////////////////////////////////////
        JPanel rightPanel = new JPanel();
        rightPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.decode("#34495e"), Color.decode("#ecf0f1")));
        rightPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Options",0,0,Font.getFont("arial"),  Color.decode("#192a56")));

        ////////////////////////////////  right_box_Settings Content //////////////////////////////////
        rightPanel.setBackground(Color.decode("#ecf0f1"));
        right_box_setup.setBackground(Color.decode("#34495e"));
        right_box_learning.setBackground(Color.decode("#34495e"));
        right_box_shoot.setBackground(Color.decode("#34495e"));

        right_box_setup.setPreferredSize(new Dimension(775, 430));
        rightPanel.add(right_box_setup);
        right_box_setup.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.EAST;
        right_box_setup.setVisible(false);


        right_box_setup.add(new JLabel("<html><font size='4' color='white'>Laser Exposure Time (ms) :</font></html>"),gbc);
        gbc.gridy++;
        right_box_setup.add(new JLabel("<html><font size='4'  color='white'>Turn Laser Illumination ON/OFF, MM Side  :</font></html>"), gbc);
        gbc.gridy++;
        right_box_setup.add(new JLabel("<html><font size='4' color='white'>Calibration Delays (ms)    :</font></html>"), gbc);
        gbc.gridy++;
        right_box_setup.add(new JLabel("<html><font size='4'  color='white'>Start Calibration :</font></html>"),gbc);
        gbc.gridy++;
        right_box_setup.add( new JSeparator(SwingConstants.HORIZONTAL),  gbc, 1); this.right_box_setup.getComponent(1).setPreferredSize(new Dimension(150,10));
        gbc.gridy++;
        right_box_setup.add(new JLabel("<html><font size='4'  color='white'>Set Chanel Group Configuration  :</font></html>"),gbc);
        gbc.gridy++;
        right_box_setup.add(new JLabel("<html><font size='4' color='white'>Chanel Exposure time (ms)  :</font></html>"),gbc);
        gbc.gridy++;
        right_box_setup.add(new JLabel("<html><font  size='4' color='white'>Set Configuration Preset.       :</font></html>"),gbc);
        gbc.gridy++;
        right_box_setup.add(new JLabel("<html><font  size='4' color='white'>Try Sequence .       :</font></html>"),gbc);

        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridy = 0;
        gbc.gridx++;
        gbc.gridwidth = GridBagConstraints.REMAINDER;

        /// # Set Exposure Time Event  # ///
        exposureT_laser_spinner =  new JSpinner(model_forExposure);
        exposureT_laser_spinner.setPreferredSize(new Dimension(150, 30));
        right_box_setup.add(exposureT_laser_spinner, gbc);
        exposureT_laser_spinner.addChangeListener(e -> {
            rappController_ref.setExposure(Double.parseDouble(exposureT_laser_spinner.getValue().toString()));
            System.out.println( exposureT_laser_spinner.getValue().toString());

        });

        gbc.gridy++;
         /////////////  # Set illumination, Use to Turn On AND Off the Device # //////////////
        right_box_setup.add( lightOnOff_jbtn, gbc);
        lightOnOff_jbtn.setPreferredSize(new Dimension(150, 30));
        lightOnOff_jbtn.setBackground(Color.decode("#68C3A3"));
        lightOnOff_jbtn.addActionListener(e -> {
            if (lightOnOff_jbtn.isSelected()){
                lightOnOff_jbtn.setText("Off Light");
                lightOnOff_jbtn.setUI(new MetalToggleButtonUI() {
                    @Override
                    protected Color getSelectColor() {
                        return Color.decode("#d35400");
                    }
                });
                rappController_ref.setOnState(true);
                //LiveModeButton.col
            }else {
                lightOnOff_jbtn.setText("Open Light");
                lightOnOff_jbtn.setBackground(Color.decode("#68C3A3"));
                rappController_ref.setOnState(false);
            }
        });
        gbc.gridy++;
        delayField_.setPreferredSize(new Dimension(150, 30));
        right_box_setup.add(delayField_, gbc);
        gbc.gridy++;

        calibrate_btn = new JButton("Start Calibration!");
        calibrate_btn.setPreferredSize(new Dimension(150, 30));
        right_box_setup.add(calibrate_btn, gbc); // Calibrate Button Action
        calibrate_btn.addActionListener(e -> {
            try {
                boolean running = rappController_ref.isCalibrating();
                if (running) {
                    rappController_ref.stopCalibration();
                } else {
                    rappController_ref.runCalibration();
                    calibrate_btn.setText("Stop calibration");

                }
            } catch (Exception ex) {
                ex.printStackTrace();
                ReportingUtils.showError(e);
            }
        });
        gbc.gridy++;
        right_box_setup.add( new JSeparator(SwingConstants.HORIZONTAL),  gbc, 2);this.right_box_setup.getComponent(2).setPreferredSize(new Dimension(150,10));
        gbc.gridy++;
        String comboBoxConfigGroup[] = rappController_ref.getConfigGroup();
        // / Liste of available Configurations Settings
        groupConfList_jcb = new JComboBox<>(comboBoxConfigGroup);
        right_box_setup.add(groupConfList_jcb,gbc);
        groupConfList_jcb.setPreferredSize(new Dimension(150, 30));

        groupConfList_jcb.addActionListener(e -> {
            String GroupConfN = Objects.requireNonNull(groupConfList_jcb.getSelectedItem()).toString();
            DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(rappController_ref.getConfigPreset(GroupConfN));
            DefaultComboBoxModel<String> model2 = new DefaultComboBoxModel<>(rappController_ref.getConfigPreset(GroupConfN));
            presetConfList_jcb.setModel(model);
            Sequence_jcb.setModel(model2);
            Sequence_jcb.addItem("Apply ALL Sequence");
        });
        /// # Set Exposure Time Event  # ///
        gbc.gridy++;
        exposureT_filter_spinner =  new JSpinner(model_forFilterExposure);
        exposureT_filter_spinner.setPreferredSize(new Dimension(150, 30));
        right_box_setup.add(exposureT_filter_spinner, gbc);
        exposureT_filter_spinner.addChangeListener(e -> {
            //rappController_ref.setExposure(1000 * Double.parseDouble(exposureT_filter_spinner.getValue().toString()));
           // System.out.println( exposureT_filter_spinner.getValue().toString());
        });

        gbc.gridy++;
        presetConfList_jcb = new JComboBox<>(new DefaultComboBoxModel<String>());
        right_box_setup.add(presetConfList_jcb, gbc);
        presetConfList_jcb.setPreferredSize(new Dimension(150, 30));
        presetConfList_jcb.addActionListener(e -> {

            String GroupConfN = Objects.requireNonNull(groupConfList_jcb.getSelectedItem()).toString();
            String PresetName = Objects.requireNonNull(presetConfList_jcb.getSelectedItem()).toString();
            // # Here we Apply the set form the Group configuration Settings
            rappController_ref.ChangeConfigSet(GroupConfN, PresetName);
        });
        gbc.gridy++;


        Sequence_jcb = new JComboBox<String>(new DefaultComboBoxModel<>());
        right_box_setup.add(Sequence_jcb, gbc);
        Sequence_jcb.setPreferredSize(new Dimension(150, 30));
        Sequence_jcb.addActionListener(e -> {
            String GroupConfN = groupConfList_jcb.getSelectedItem().toString();
            String SequenceName = Sequence_jcb.getSelectedItem().toString();
            rappController_ref.fluorescenceSequence(GroupConfN, SequenceName,confirmSaving());

        });

        ////////////////////////////////  right_box_shoot (SHOOT OPTION) Content //////////////////////////////////

        rightPanel.add(right_box_shoot);
        right_box_shoot.setVisible(false);
        right_box_shoot.setLayout(new GridBagLayout());
        GridBagConstraints gbc1 = new GridBagConstraints();
        gbc1.gridx = 0;
        gbc1.gridy = 0;
        gbc1.insets = new Insets(8, 8, 8, 8);
        gbc1.anchor = GridBagConstraints.EAST;

        right_box_shoot.add(new JLabel("<html><font size='4' color='white'>Point And Shoot Mode :</font></html>"), gbc1);
        gbc1.gridy++;
        right_box_shoot.add(new JLabel("<html><font size='4' color='white'>ROIs Manager       :</font></html>"), gbc1);
        gbc1.gridy++;
        right_box_shoot.add(new JLabel("<html><font size='4' color='white'>Shoot laser on point from ROis Manager  :</font></html>"),gbc1);
        gbc1.gridy++;
        right_box_shoot.add(new JLabel("<html><font size='4' color='white'>Load  Selected Cell detection point  :</font></html>"),gbc1);
        gbc1.gridy++;
        right_box_shoot.add(new JLabel("<html><font size='4' color='white'>Shoot laser on Selected point from cell detection :</font></html>"), gbc1);


        gbc1.anchor = GridBagConstraints.WEST;
        gbc1.gridy = 0;
        gbc1.gridx++;
        gbc1.gridwidth = GridBagConstraints.REMAINDER;

        pointAndShootOnOff_btn = new JToggleButton("ON");
        right_box_shoot.add( pointAndShootOnOff_btn, gbc1);
        pointAndShootOnOff_btn.setPreferredSize(new Dimension(120,25));
        pointAndShootOnOff_btn.addActionListener(e -> {
            updatePointAndShoot();

        });

        gbc1.gridy++;
        JButton setAddRois_btn = new JButton("Set / Add Rois");
        right_box_shoot.add(setAddRois_btn, gbc1);
        setAddRois_btn.setPreferredSize(new Dimension(120,25));
        setAddRois_btn.addActionListener(e -> {
            RappPlugin.showRoiManager();
        });

        gbc1.gridy++;
        JButton shootonMarkpoint_btn = new JButton("Shoot on ROIs");
        right_box_shoot.add(shootonMarkpoint_btn, gbc1);
        shootonMarkpoint_btn.setPreferredSize(new Dimension(120,25));
        shootonMarkpoint_btn.addActionListener(e -> rappController_ref.createMultiPointAndShootFromRoeList());

        gbc1.gridy++;
        JButton loadImage_btn = new JButton("Load point");
        right_box_shoot.add(loadImage_btn, gbc1);
        loadImage_btn.setPreferredSize(new Dimension(120,25));
        loadImage_btn.addActionListener(e -> {
            ImagePlus image = IJ.openImage(fileDialog_.ChooseFileDialog());
            try {
                Thread.sleep(100);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            image.show();
            rappController_ref.findCells();
        });

        gbc1.gridy++;
        JButton shootOnLearningP_btn = new JButton("Shoot on Learning ");
        right_box_shoot.add(shootOnLearningP_btn, gbc1);
        shootOnLearningP_btn.setPreferredSize(new Dimension(120,25));
       // shootOnLearningP_btn.addActionListener(e -> rappController_ref.getListofROIs());

        // Point Table Internal Frame
        try {
            if (tablePointFrame == null) {
                tablePointFrame= new CellPointInternalFrame();
            }
            tablePointFrame.setPreferredSize(new Dimension(700, 320));
            tablePointFrame.setVisible(true);
            right_box_shoot.add(tablePointFrame);
            tablePointFrame.repaint();

            //acquisition_.
        } catch (Exception var2) {
            ReportingUtils.showError(var2, "\nFrame invalid or corrupted settings.\nTry resetting .");
        }

        JInternalFrame buttonFrame = new JInternalFrame();
        try {
            BasicInternalFrameUI bi = (BasicInternalFrameUI)buttonFrame.getUI();
            bi.setNorthPane(null);
            buttonFrame.setPreferredSize(new Dimension(200, 320));
            buttonFrame.setVisible(true);
            right_box_shoot.add(buttonFrame);
            buttonFrame.repaint();

            JLabel rootLabel_2 = new JLabel();
            rootLabel_2.setFont(new Font("Arial", Font.PLAIN, 10));
            rootLabel_2.setText("XML configuration file :");
            rootLabel_2.setBounds(10, 30, 150, 30);
            buttonFrame.add(rootLabel_2);

            xml_rootField_2 = new JTextField();
            xml_rootField_2.setEnabled(false);
            xml_rootField_2.setFont(new Font("Arial", Font.PLAIN, 8));
            xml_rootField_2.setBounds(5, 55, 125, 30);
            buttonFrame.add(xml_rootField_2);

            browseXmlFIle_btn = new JButton();
            browseXmlFIle_btn.addActionListener(e -> {
                String xmlPath = fileDialog_.xmlFileChooserDialog();
                if (xmlPath != null) {
                    xml_rootField_2.setText(xmlPath);
                }
            });
            browseXmlFIle_btn.setMargin(new Insets(2, 5, 2, 5));
            browseXmlFIle_btn.setFont(new Font("Dialog", Font.PLAIN, 10));
            browseXmlFIle_btn.setText("...");
            browseXmlFIle_btn.setBounds(135, 55, 50, 30);
            buttonFrame.add(browseXmlFIle_btn);
            browseXmlFIle_btn.setToolTipText("Browse");


            JLabel filter_exposureLabel = new JLabel();
            filter_exposureLabel.setFont(new Font("Arial", Font.PLAIN, 10));
            filter_exposureLabel.setText("FIlter Exposure Time (ms) :");
            filter_exposureLabel.setBounds(5, 95, 150, 30);
            buttonFrame.add(filter_exposureLabel);

            JSpinner exposureT_filter_spinner2 =  new JSpinner(model_forExposure);
            exposureT_filter_spinner2.setBounds(132, 95, 60, 30);
            buttonFrame.add(exposureT_filter_spinner2);
            exposureT_filter_spinner2.addChangeListener(e -> {
               // rappController_ref.setExposure(1000 * Double.parseDouble(exposureT_filter_spinner.getValue().toString()));
                System.out.println( exposureT_filter_spinner2.getValue().toString());
            });



            runSegmentation_btn = new JButton();
            runSegmentation_btn.addActionListener(e -> {
                if ( xml_rootField_2.getText().equals("")){
                    ReportingUtils.showMessage("Please Try Again! "
                            +"The XML Field is Empty");
                }
                else {
                    String taggPath = fileDialog_.SaveFileDialog();
                    if ( taggPath != null ){
                        rappController_ref.runSegmentation(xml_rootField_2.getText(), taggPath);
                    }
                    else {
                        ReportingUtils.showMessage("Please Try Again! "
                                +"Choose a proper TaggeImage ");
                    }
                }
            });
            runSegmentation_btn.setMargin(new Insets(2, 5, 2, 5));
            runSegmentation_btn.setFont(new Font("Dialog", Font.PLAIN, 10));
            runSegmentation_btn.setText("Run Segmentation");
            runSegmentation_btn.setBounds(50, 135, 102, 30);
            buttonFrame.add(runSegmentation_btn);
            runSegmentation_btn.setToolTipText("Run Segmentation with Bright Field Image");

            buttonFrame.add(new JLabel(""));
            //////////////////////////////////////////////////////////////////////////////////////////////////
            //acquisition_.
        } catch (Exception var2) {
            ReportingUtils.showError(var2, "\nFrame invalid or corrupted settings.\nTry resetting .");
        }



        /////////////////////////////////// #Image Viewer Center Panel# //////////////////////////////////////////JPanel centerPanel = new JPanel();

        rightPanel.add(right_box_learning);
        right_box_learning.setPreferredSize(new Dimension(910, 575));   // vertical box
        right_box_learning.setVisible(false);

        try {
            if (imageViewer_== null) {
                imageViewer_= new ImageViewer();
            }
            imageViewer_.setPreferredSize(new Dimension(910, 575));
            right_box_learning.add(imageViewer_);
            imageViewer_.setVisible(true);
            imageViewer_.repaint();

            //acquisition_.
        } catch (Exception var2) {
            ReportingUtils.showError(var2, "\nAcquistion window failed to open due to invalid or corrupted settings.\nTry resetting registry settings to factory defaults (Menu Tools|Options).");
        }


        //////////////////////////////////////# Imaging # ///////////////////////////////////////////////

        rightPanel.add(asButtonPanel);
        asButtonPanel.setPreferredSize(new Dimension(910, 545));   // vertical box
        asButtonPanel.setVisible(false);
        asButtonPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Imaging Options",0,0,Font.getFont("arial"),  Color.WHITE));
        asButtonPanel.setVisible(true);
        BasicInternalFrameUI AsButtonPanel_bi = (BasicInternalFrameUI) asButtonPanel.getUI();
        AsButtonPanel_bi.setNorthPane(null);
        asButtonPanel.setBackground(Color.decode("#34495e"));

        try {
            if (acquisition_ == null) {
                acquisition_ = (new SeqAcqGui(engine_, this.mainPrefs_, studio_, this.options_));
            }
            acquisition_.setPreferredSize(new Dimension(900, 545));
            acquisition_.setVisible(true);
            asButtonPanel.add(acquisition_);
            acquisition_.repaint();

            //acquisition_.
        } catch (Exception var2) {
            ReportingUtils.showError(var2, "\nAcquistion window failed to open due to invalid or corrupted settings.\nTry resetting registry settings to factory defaults (Menu Tools|Options).");
        }

        ///////////////////////// # Utilities # /////////////////////////////////////////////////////////

        JSplitPane splitPaneBody = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        JSplitPane splitPaneTop = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPanel, splitPaneBody );
        splitPaneTop.setDividerLocation(80);
        splitPaneBody.setDividerLocation(200);
        this.add(splitPaneTop,BorderLayout.CENTER);
        splitPaneTop.setOneTouchExpandable(false);
        splitPaneTop.setContinuousLayout(false);

        this.setBackground(Color.blue);
        this.setSize(1150, 700);
        this.setVisible(true);

        /// Avoid all App Close when close the Plugin GUI
        this.setDefaultCloseOperation(0); // DO_NOTHING_ON_CLOSE
        this.setLocation(32, 32);
        this.addWindowListener(new WindowAdapter() {

            // Windows Close button action event
            @Override
            public void windowClosing(WindowEvent we) {
                confirmQuit();
            }

        });
        ImageWindow snapWin = SnapLiveManager_.getSnapLiveWindow();
//       // snapWin.windowClosing(WindowEvent ew) {
//
//        }
//        if (SnapLiveManager_.getSnapLiveWindow().isClosed() == true) {
//            LiveMode_btn.setSelected(false);
//            LiveMode_btn.setUI(new MetalToggleButtonUI() {
//                @Override
//                protected Color getSelectColor() {
//                    return Color.decode("#d35400");
//                }
//            });
//        }
    }


    private JToggleButton createJButton(String text) {
        JToggleButton button = new JToggleButton(text);
        button.setForeground(Color.decode("#ecf0f1"));
        button.setBorder(null);
        button.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        ImageIcon icon = new ImageIcon(path.concat("Resources/Images/" + text + ".png"));
        Image img = icon.getImage();
        Image new_img = img.getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH);
        button.setIcon(new ImageIcon(new_img));
        button.setSelectedIcon(new ImageIcon(new_img));
        return button;
    }


    /**
     * Shows the GUI, which is a singleton.
     * @param core MMCore
     * @param app  ScritpInterface
     * @return singleton instance
     */
    public static RappGui showAppInterface(CMMCore core, ScriptInterface app) throws Exception {
        if (appInterface_ == null) {
            appInterface_ = new RappGui(core, app);
            // Place window where it was last.
            GUIUtils.recallPosition(appInterface_);
        }
        appInterface_.setVisible(true);
        return appInterface_;
    }

   //  Avoid user to accidentally close the window by this fonction
    private void confirmQuit() {
        int n = JOptionPane.showConfirmDialog(appInterface_,
                "Quit: are you sure to quit?", "Quit", JOptionPane.YES_NO_OPTION);
        if (n == JOptionPane.YES_OPTION) {
            LiveMode_btn.setSelected(false);
            dispose();
            studio_.makeActive();
        }
        else  GUIUtils.recallPosition(appInterface_);
    }

    //  Avoid user to accidentally close the window by this fonction
    private Boolean confirmSaving() {
        int n = JOptionPane.showConfirmDialog(appInterface_,
                "Do you want to save sequence image?", "Saving", JOptionPane.YES_NO_OPTION);
        if (n == JOptionPane.YES_OPTION) {
            return true;
        }
        else  GUIUtils.recallPosition(appInterface_);
        return false;
    }

    /**
     * Sets the Point and Shoot "On and Off" buttons to a given state.
     */
    public void updatePointAndShoot() {
        //String galvo_ = core.getGalvoDevice();
        if (pointAndShootOnOff_btn.isSelected()){
            pointAndShootOnOff_btn.setText("OFF");
            try {
                rappController_ref.enablePointAndShootMode(true);
                // core.setGalvoIlluminationState(galvo_, true);
                // Point2D ppos=core.getGalvoPosition(galvo_); System.out.println(ppos);
                // Point2D newppos = null;  newppos.setLocation( ppos.getX() +100, ppos.getY() +100); //core.setGalvoPosition(galvo_, newppos.getX(), newppos.getY());
                // core.pointGalvoAndFire(galvo_,25264.0, 24494.0, 500000);
            } catch (Exception ex) {
                ReportingUtils.logError(ex);
                ex.printStackTrace();
            }
            //LiveModeButton.col
        }else {
            pointAndShootOnOff_btn.setText("ON");
            try {
                rappController_ref.enablePointAndShootMode(false);
            } catch (Exception ex) {
                ReportingUtils.logError(ex);
                ex.printStackTrace();
            }
        }
    }

    private void LiveModeButton(java.awt.event.ActionEvent evt) {
        // open new Thread for snap Image
        new Runnable() {
            public void run() {
                try {

                }catch (Exception ex){
                   ex.printStackTrace();
            }}
//        SnapDisplayImage = new Thread(new ThreadClass(this));
//        SnapDisplayImage.start();

        };
    }

    @Override
    public void liveModeEnabled(boolean b) {

    }
}