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


import bsh.util.Util;
import com.google.common.base.Objects;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.ImageWindow;
import mmcorej.CMMCore;
import org.micromanager.MMOptions;
import org.micromanager.MMStudio;
import org.micromanager.SnapLiveManager;
import org.micromanager.api.MMListenerInterface;
import org.micromanager.api.ScriptInterface;
import org.micromanager.internalinterfaces.LiveModeListener;
import org.micromanager.rapp.MultiFOV.FOV_GUI;
import org.micromanager.rapp.SequenceAcquisitions.SeqAcqController;
import org.micromanager.rapp.SequenceAcquisitions.SeqAcqGui;
import org.micromanager.rapp.utils.AboutGui;
import org.micromanager.rapp.utils.FileDialog;
import org.micromanager.rapp.utils.ImageViewer;
import org.micromanager.rapp.utils.Utils;
import org.micromanager.utils.GUIUtils;
import org.micromanager.utils.ReportingUtils;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.InternalFrameUI;
import javax.swing.plaf.basic.BasicInternalFrameUI;
import javax.swing.plaf.metal.MetalToggleButtonUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
//import java.util.Objects;
import java.util.prefs.Preferences;

/**
 * @author FLorial
 */

public class RappGui extends JFrame implements LiveModeListener, ActionListener, PropertyChangeListener, MMListenerInterface {
    private Preferences mainPrefs_;
    private MMOptions options_;
    private SeqAcqGui acquisition_;
    private static FOV_GUI fov_gui_;
    private static RappGui appInterface_;
    public static RappGui getInstance() {
        return appInterface_;
    }
    private  static  RappController rappController_ref;
    private SnapLiveManager SnapLiveManager_;
    private URL default_path = this.getClass().getResource("");
    public String path = default_path.toString().substring(6);
    private JPanel right_box_setup = new JPanel();
    private JInternalFrame right_box_fov  = new JInternalFrame();
    private JPanel right_box_shoot = new JPanel();
    public  JLabel spiner = new JLabel("nothing");
    private SpinnerModel model_forExposure = new SpinnerNumberModel(100, 0, 999999, 1);
    private SpinnerModel model_forDelay = new SpinnerNumberModel(0, 0, 9999, 1);
    private SpinnerModel model_forFilterExposure = new SpinnerNumberModel(100, 0, 999999, 1);
    private JSpinner exposureT_laser_spinner = new JSpinner(model_forExposure);;
    private JSpinner exposureT_camera_spinner = new JSpinner(model_forFilterExposure);
   // protected JSpinner delayField_ = new JSpinner(model_forDelay);
    private JToggleButton lightOnOff_jbtn = new JToggleButton("Open Light");
    private JInternalFrame asButtonPanel = new JInternalFrame();
    protected JToggleButton LiveMode_btn;
    private JToggleButton pointAndShootOnOff_btn;
    protected JButton calibrate_btn;
    protected JButton about_btn;
    private JButton browseXmlFIle_btn;
    private JButton runSegmentation_btn;
    private JComboBox presetConfList_jcb ;
    //private JComboBox<String> Sequence_jcb ;
    private JComboBox groupConfList_jcb;
    private JComboBox segmenterAlgoList_jcb;
    private JTextField xml_rootField_2;
    private JLabel  jLabel_Image;
    // the index of the images
    private int pos = 0;
    MMStudio studio_;
    protected AboutGui  dlgAbout;

     /**
     * Constructor. Creates the main window for the Projector plugin. we use this Class for the main interface
     */
    public RappGui(CMMCore core, final ScriptInterface app) throws Exception {
        studio_ = (MMStudio) app;
        final FileDialog fileDialog_ = new FileDialog();

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

//        try {
//            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
//                if ("Nimbus".equals(info.getName())) {
//                    UIManager.setLookAndFeel(info.getClassName());
//                    break;
//                }
//            }
//        } catch (Exception e) {
//            // If Nimbus is not available, you can set the GUI to another look and feel.
//        }
        UIManager.getDefaults().put("SplitPane.border", BorderFactory.createEmptyBorder());

        try {
            setDefaultLookAndFeelDecorated(true);
            UIManager.setLookAndFeel("com.jtattoo.plaf.aero.AeroLookAndFeel");

        } catch (ClassNotFoundException e) {
           e.printStackTrace();
        } catch (InstantiationException e) {
           e.printStackTrace();
        } catch (IllegalAccessException e) {
           e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
           e.printStackTrace();
        }

        this.setTitle("Rapp Plugin(UGA-42 Control)");

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

        //////////////////// Right Panel Content ////////////////////////////////
        JPanel rightPanel = new JPanel();
        rightPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.decode("#34495e"), Color.decode("#ecf0f1")));
        rightPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Options",0,0,Font.getFont("arial"),  Color.decode("#192a56")));

        ////////////////////////////////////////////////////
        JSplitPane splitPaneBody = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        JSplitPane splitPaneTop = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPanel, splitPaneBody );

        ///////////// we put all the component from the left into a Horizontal Box //////////////////////////////////
        Box left_box = Box.createVerticalBox();
        leftPanel.add(left_box);
        left_box.setPreferredSize(new Dimension(150, 400));   // vertical box
        left_box.setBackground(Color.decode("#34495e"));

        left_box.add(Box.createVerticalStrut(10));

        final JToggleButton setupOption_btn =  createJButton("Settings");
        final JToggleButton stage_position = createJButton("Stage Position");
        final JToggleButton shootOption_btn = createJButton("Point And Shoot");
        final JToggleButton acquisitionOption_btn = createJButton("Sequence Acquisition");

       // setupOption_btn.setMaximumSize(new Dimension(145, 50));



        left_box.add(setupOption_btn);
       // setupOption_btn.setBackground(Color.decode("#ecf0f1"));
        setupOption_btn.setBorder(null);
        setupOption_btn.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                right_box_setup.setVisible(true);
                right_box_shoot.setVisible(false);
                right_box_fov.setVisible(false);
                asButtonPanel.setVisible(false);
                if( setupOption_btn.isSelected() ){
                    // We set the other button color just to distinguish
                    setupOption_btn.setForeground(Color.decode("#2980b9"));
                    shootOption_btn.setForeground(Color.decode("#ecf0f1"));
                    stage_position.setForeground(Color.decode("#ecf0f1"));
                    acquisitionOption_btn.setForeground(Color.decode("#ecf0f1"));
                    // we do have to set the other bButton selection to False
                    shootOption_btn.setSelected(false);
                    stage_position.setSelected(false);
                    acquisitionOption_btn.setSelected(false);

                    RappGui.getInstance().setSize(730, 590);

                }

            }
        } );

        left_box.add(Box.createVerticalStrut(15));

        // Leaning BUTTON, OPEN THE BOX OPTION TO MANAGE the MACHINE LEARNING PART
        left_box.add(stage_position);
        stage_position.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                right_box_fov.setVisible(true);
                right_box_shoot.setVisible(false);
                right_box_setup.setVisible(false);
                asButtonPanel.setVisible(false);

                if( stage_position.isSelected() ){
                    // We set the other button color just to distinguish
                    stage_position.setForeground(Color.decode("#2980b9"));
                    shootOption_btn.setForeground(Color.decode("#ecf0f1"));
                    setupOption_btn.setForeground(Color.decode("#ecf0f1"));
                    acquisitionOption_btn.setForeground(Color.decode("#ecf0f1"));
                    // we do have to set the other bButton selection to False
                    shootOption_btn.setSelected(false);
                    setupOption_btn.setSelected(false);
                    acquisitionOption_btn.setSelected(false);

                    RappGui.getInstance().setSize(1230, 700);
                    fov_gui_.repaint();
                }
            }
        });
        left_box.add(Box.createVerticalStrut(15));

        left_box.add(acquisitionOption_btn);
        acquisitionOption_btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SeqAcqGui.updateGUIContents();
                asButtonPanel.setVisible(true);
                right_box_shoot.setVisible(false);
                right_box_setup.setVisible(false);
                right_box_fov.setVisible(false);

                if (acquisitionOption_btn.isSelected()) {
                    // We set the other button color just to distinguish
                    acquisitionOption_btn.setForeground(Color.decode("#2980b9"));
                    setupOption_btn.setForeground(Color.decode("#ecf0f1"));
                    stage_position.setForeground(Color.decode("#ecf0f1"));
                    shootOption_btn.setForeground(Color.decode("#ecf0f1"));
                    // we do have to set the other bButton selection to False
                    setupOption_btn.setSelected(false);
                    stage_position.setSelected(false);
                    shootOption_btn.setSelected(false);

                    RappGui.getInstance().setSize(1150, 660);
                }
            }
        });


        left_box.add(Box.createVerticalStrut(15));

        // SHOOT BUTTON, OPEN THE BOX OPTION TO MANAGE SHOOT
        left_box.add(shootOption_btn);
        shootOption_btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                right_box_shoot.setVisible(true);
                right_box_setup.setVisible(false);
                right_box_fov.setVisible(false);
                asButtonPanel.setVisible(false);
                if( shootOption_btn.isSelected() ){
                    shootOption_btn.setForeground(Color.decode("#2980b9"));
                    setupOption_btn.setForeground(Color.decode("#ecf0f1"));
                    stage_position.setForeground(Color.decode("#ecf0f1"));
                    acquisitionOption_btn.setForeground(Color.decode("#ecf0f1"));
                    // we do have to set the other Button selection to False
                    setupOption_btn.setSelected(false);
                    stage_position.setSelected(false);
                    acquisitionOption_btn.setSelected(false);


                }

                RappGui.getInstance().setSize(710, 590);

            }
        } );

        left_box.add(Box.createVerticalStrut(15));
        left_box.add( new JSeparator(SwingConstants.HORIZONTAL) , left_box,8);
        left_box.getComponent(8).setPreferredSize(new Dimension(150,2));

        // Start or Stop the the Live Mode
        LiveMode_btn = new JToggleButton("Start Live View");
        LiveMode_btn.setMaximumSize(new Dimension(145, 60));
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
                    if(snap != null)snap.close();
                }
               // createFrame();
              //   LiveModeButton(e);
            }
        } );

        left_box.add(Box.createVerticalStrut(12));
        // Start or Stop the the Live Mode
        JButton snapAndSave_btn = new JButton("Snap And Save Image");
        snapAndSave_btn.setMaximumSize(new Dimension(145, 60));
        snapAndSave_btn.setBackground(Color.decode("#3498db"));
        snapAndSave_btn.setForeground(Color.white);
        left_box.add(snapAndSave_btn);
        snapAndSave_btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rappController_ref.snapAndSaveImage();
            }
        });
        left_box.add(Box.createVerticalStrut(12));
        // Illuminate the center of the photo targeting device's range
        JButton showCenterSpot_btn = new JButton("Display Spot at the center");
        shootOption_btn.setToolTipText("Display laser Spot at the center");
        showCenterSpot_btn.setBackground(Color.decode("#27ae60"));
        showCenterSpot_btn.setForeground(Color.white);
        showCenterSpot_btn.setMaximumSize((new Dimension(145, 60)));
        left_box.add(showCenterSpot_btn);
        showCenterSpot_btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rappController_ref.displayCenterSpot();
            }
        });

        left_box.add(Box.createVerticalStrut(30));
        about_btn = new JButton("About...");
        about_btn.setBackground(Color.decode("#3498db"));
        about_btn.setForeground(Color.white);
        about_btn.setMaximumSize((new Dimension(145, 60)));
        left_box.add(about_btn);
        about_btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                String msg = ("Cell Killing Interface\r\n\r\n" +
                        "From The KnopLab (ZMBH) .\r\n" +
                        "We present a tool for an automation of a fluorescence microscopy setup capable\n" +
                        "of selective cell isolation based on UV lasers. \r\n" +
                        "THIS SOFTWARE IS PROVIDED IN THE HOPE THAT IT MAY BE USEFUL, WITHOUT ANY REPRESENTATIONS OR WARRANTIES, INCLUDING WITHOUT LIMITATION THE WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT SHALL AUTHOR BE LIABLE FOR INDIRECT, EXEMPLARY, PUNITIVE, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING FROM USE OF THIS SOFTWARE, REGARDLESS OF THE FORM OF ACTION, AND WHETHER OR NOT THE AUTHOR HAS BEEN INFORMED OF, OR OTHERWISE MIGHT HAVE ANTICIPATED, THE POSSIBILITY OF SUCH DAMAGES.\r\n\r\n");
                dlgAbout = new AboutGui(RappGui.this, msg);
                dlgAbout.setVisible(true);

            }
        });

        ////////////////////// #Mange Right panel an Content here#  /////////////////////////////////////


        ////////////////////////////////  right_box_Settings Content //////////////////////////////////
        rightPanel.setBackground(Color.decode("#ecf0f1"));
        right_box_setup.setBackground(Color.decode("#34495e"));
        right_box_fov.setBackground(Color.decode("#34495e"));
        right_box_shoot.setBackground(Color.decode("#34495e"));

        right_box_setup.setPreferredSize(new Dimension(490, 430));
        rightPanel.add(right_box_setup);
        right_box_setup.setVisible(true);
        right_box_setup.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.EAST;
       // right_box_setup.setVisible(false);


        right_box_setup.add(new JLabel("<html><font size='4' color='white'>Laser Exposure Time (ms) :</font></html>"),gbc);
      //  gbc.gridy++;
    //    right_box_setup.add(new JLabel("<html><font size='4'  color='white'>Turn ON/OFF Plugin :</font></html>"), gbc);
//        gbc.gridy++;
//        right_box_setup.add(new JLabel("<html><font size='4' color='white'>Calibration Delays (ms)  :</font></html>"), gbc);
        gbc.gridy++;
        right_box_setup.add(new JLabel("<html><font size='4'  color='white'>Start spot Calibration :</font></html>"),gbc);
        gbc.gridy++;
        right_box_setup.add( new JSeparator(SwingConstants.HORIZONTAL),  gbc, 1); this.right_box_setup.getComponent(1).setPreferredSize(new Dimension(150,10));
        gbc.gridy++;
        right_box_setup.add(new JLabel("<html><font size='4'  color='white'>Set Default Chanel Group Configuration  :</font></html>"),gbc);
        gbc.gridy++;
        right_box_setup.add(new JLabel("<html><font size='4' color='white'>Default Chanel Exposure time (ms)  :</font></html>"),gbc);
        gbc.gridy++;
        right_box_setup.add(new JLabel("<html><font  size='4' color='white'>Set Default Configuration Preset.       :</font></html>"),gbc);
//        gbc.gridy++;
//        right_box_setup.add(new JLabel("<html><font  size='4' color='white'>Try Sequence Acquisition :</font></html>"),gbc);

        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridy = 0;
        gbc.gridx++;
        gbc.gridwidth = GridBagConstraints.REMAINDER;

        /// # Set Exposure Time For the Laser # ///

        exposureT_laser_spinner.setPreferredSize(new Dimension(150, 30));
        right_box_setup.add(exposureT_laser_spinner, gbc);
        exposureT_laser_spinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                rappController_ref.setExposure(1000 * Double.parseDouble(exposureT_laser_spinner.getValue().toString()));
                System.out.println(exposureT_laser_spinner.getValue().toString());

            }
        });

        //gbc.gridy++;
         /////////////  # Set illumination, Use to Turn On AND Off the Device # //////////////
       // right_box_setup.add( lightOnOff_jbtn, gbc);
        lightOnOff_jbtn.setPreferredSize(new Dimension(150, 30));
        lightOnOff_jbtn.setBackground(Color.decode("#68C3A3"));
        lightOnOff_jbtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (lightOnOff_jbtn.isSelected()) {
                    lightOnOff_jbtn.setText("Light Off");
                    lightOnOff_jbtn.setUI(new MetalToggleButtonUI() {
                        @Override
                        protected Color getSelectColor() {
                            return Color.decode("#d35400");
                        }
                    });
                    rappController_ref.setOnState(true);
                    //LiveModeButton.col
                } else {
                    lightOnOff_jbtn.setText("Light On");
                    lightOnOff_jbtn.setBackground(Color.decode("#68C3A3"));
                    rappController_ref.setOnState(false);
                }
            }
        });
        // Calibration delay is not working properly
//        gbc.gridy++;
//        delayField_.setPreferredSize(new Dimension(150, 30));
//        right_box_setup.add(delayField_, gbc);
        gbc.gridy++;

        calibrate_btn = new JButton("Start Calibration!");
        calibrate_btn.setPreferredSize(new Dimension(150, 30));
        right_box_setup.add(calibrate_btn, gbc); // Calibrate Button Action
        calibrate_btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
            }
        });
        gbc.gridy++;
        right_box_setup.add( new JSeparator(SwingConstants.HORIZONTAL),  gbc, 2);this.right_box_setup.getComponent(2).setPreferredSize(new Dimension(150,10));
        gbc.gridy++;
        String comboBoxConfigGroup[] = rappController_ref.getConfigGroup();
        // / Liste of available Configurations Settings
        groupConfList_jcb = new JComboBox(comboBoxConfigGroup);
        right_box_setup.add(groupConfList_jcb,gbc);
        groupConfList_jcb.setPreferredSize(new Dimension(150, 30));

        groupConfList_jcb.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String GroupConfN = groupConfList_jcb.getSelectedItem().toString();
                DefaultComboBoxModel model = new DefaultComboBoxModel(rappController_ref.getConfigPreset(GroupConfN));
                DefaultComboBoxModel model2 = new DefaultComboBoxModel(rappController_ref.getConfigPreset(GroupConfN));
                model.addElement(" ");
                presetConfList_jcb.setModel(model);
                //      Sequence_jcb.setModel(model2);
                //      Sequence_jcb.addItem("Apply ALL Sequence");
            }
        });
        /// # Set Camera Exposure Time Event  # ///
        gbc.gridy++;
        exposureT_camera_spinner.setPreferredSize(new Dimension(150, 30));
        right_box_setup.add(exposureT_camera_spinner, gbc);
        exposureT_camera_spinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                rappController_ref.setCameraExposureTime(Double.parseDouble(exposureT_camera_spinner.getValue().toString()));
            }
        });

        gbc.gridy++;
        presetConfList_jcb = new JComboBox(new DefaultComboBoxModel());
        right_box_setup.add(presetConfList_jcb, gbc);
        presetConfList_jcb.setPreferredSize(new Dimension(150, 30));
        presetConfList_jcb.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // here we set the default chanel, so when we run the acquisition the chanel change from fluorescence to BF
                // rappController_ref.setTargetingChannel(presetConfList_jcb.getSelectedItem().toString());

                String GroupConfN = (groupConfList_jcb.getSelectedItem()).toString();
                String PresetName = (presetConfList_jcb.getSelectedItem()).toString();
                // # Here we Apply the settings form the Group configuration
                rappController_ref.setDefaultGroupConfig(GroupConfN, PresetName);
            }
        });
        gbc.gridy++;


 //       Sequence_jcb = new JComboBox<String>(new DefaultComboBoxModel<>());
//        right_box_setup.add(Sequence_jcb, gbc);
//        Sequence_jcb.setPreferredSize(new Dimension(150, 30));
//        Sequence_jcb.addActionListener(e -> {
//            String GroupConfN = groupConfList_jcb.getSelectedItem().toString();
//            String SequenceName = Sequence_jcb.getSelectedItem().toString();
//            rappController_ref.fluorescenceSequence(GroupConfN, SequenceName,confirmSaving());
//
//        });
        ////////////////////////////////  right_box_shoot (SHOOT OPTION) Content //////////////////////////////////

        rightPanel.add(right_box_shoot);
        right_box_shoot.setPreferredSize(new Dimension(475, 430));
        right_box_shoot.setVisible(false);
        right_box_shoot.setLayout(new GridBagLayout());
        GridBagConstraints gbc1 = new GridBagConstraints();
        gbc1.gridx = 0;
        gbc1.gridy = 0;
        gbc1.insets = new Insets(8, 8, 8, 8);
        gbc1.anchor = GridBagConstraints.EAST;

        right_box_shoot.add(new JLabel("<html><font size='4' color='white'>Set Point And Shoot Mode to :</font></html>"), gbc1);
        gbc1.gridy++;
        right_box_shoot.add(new JLabel("<html><font size='4' color='white'>ROIs Manager   :</font></html>"), gbc1);
        gbc1.gridy++;
        right_box_shoot.add(new JLabel("<html><font size='4' color='white'>Shoot laser on point from ROis Manager  :</font></html>"),gbc1);
        gbc1.gridy++;
        right_box_shoot.add( new JSeparator(SwingConstants.HORIZONTAL),  gbc1, 1); this.right_box_shoot.getComponent(1).setPreferredSize(new Dimension(150,10));
        gbc1.gridy++;
        right_box_shoot.add(new JLabel("<html><font  size='4' color='white'>Choose Segmenter Algorithm :</font></html>"),gbc1);
        gbc1.gridy++;
        right_box_shoot.add(new JLabel("<html><font size='4' color='white'>Run Segmentation and kill All Detected  Cell :</font></html>"),gbc1);
        gbc1.gridy++;

//      right_box_shoot.add(new JLabel("<html><font size='4' color='white'>Shoot laser on Selected point from cell detection :</font></html>"), gbc1);

        gbc1.anchor = GridBagConstraints.WEST;
        gbc1.gridy = 0;
        gbc1.gridx++;
        gbc1.gridwidth = GridBagConstraints.REMAINDER;

        pointAndShootOnOff_btn = new JToggleButton("ON");
        right_box_shoot.add( pointAndShootOnOff_btn, gbc1);
        pointAndShootOnOff_btn.setPreferredSize(new Dimension(140,28));
        pointAndShootOnOff_btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RappGui.this.updatePointAndShoot();
            }
        });

        gbc1.gridy++;
        JButton setAddRois_btn = new JButton("Set / Add Rois");
        setAddRois_btn.setBackground(new Color(0x2dce98));
        setAddRois_btn.setForeground(Color.white);
        right_box_shoot.add(setAddRois_btn, gbc1);
        setAddRois_btn.setPreferredSize(new Dimension(140,28));
        setAddRois_btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RappPlugin.showRoiManager();
            }
        });

        gbc1.gridy++;
        JButton shootonMarkpoint_btn = new JButton("Kill Mark Cells");
        right_box_shoot.add(shootonMarkpoint_btn, gbc1);
        shootonMarkpoint_btn.setPreferredSize(new Dimension(140,28));
        shootonMarkpoint_btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rappController_ref.createMultiPointAndShootFromRoeList();
            }
        });

        gbc1.gridy++;
        right_box_shoot.add( new JSeparator(SwingConstants.HORIZONTAL),  gbc1, 2); this.right_box_shoot.getComponent(2).setPreferredSize(new Dimension(140,10));

        gbc1.gridy++;

        String ListSegmenterAlgogGroup[] = Utils.getSegmenterAlgoListe();
        // / Liste of available Segmeter Algo
        segmenterAlgoList_jcb = new JComboBox(ListSegmenterAlgogGroup);
        right_box_shoot.add(segmenterAlgoList_jcb,gbc1);
        segmenterAlgoList_jcb.setPreferredSize(new Dimension(140, 30));

        segmenterAlgoList_jcb.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String segmenter = (segmenterAlgoList_jcb.getSelectedItem()).toString();

            }
        });

        gbc1.gridy++;
        JButton SegImageAndKillCell_btn = new JButton("Kill Detected Cells");
        SegImageAndKillCell_btn.setBackground(new Color(0x2dce98));
        SegImageAndKillCell_btn.setForeground(Color.white);
        right_box_shoot.add(SegImageAndKillCell_btn, gbc1);
        SegImageAndKillCell_btn.setPreferredSize(new Dimension(140,28));
        SegImageAndKillCell_btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String algo = segmenterAlgoList_jcb.getSelectedItem().toString();
                if (segmenterAlgoList_jcb.getSelectedIndex() != 0) {
                    String action = RappGui.this.chooseWhereToTakeTheImage();
                    if (action == "disk") {
                        ImagePlus image = IJ.openImage(fileDialog_.ChooseFileDialog("Please Choose a Tiff Image with Cells"));
                        image.setTitle("Img_Original_" + algo);
                        image.show();
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                        if (image != null) {


                            ImagePlus image_dup_ori = image.duplicate();
                            image_dup_ori.setTitle("Img_Segmented_" + algo);
                            image_dup_ori.show();

                            List<Point2D.Double> ll = rappController_ref.imageSegmentation(image_dup_ori, "", algo, true, false);
                            rappController_ref.shootFromSegmentationListPoint(ll, Long.parseLong(exposureT_laser_spinner.getValue().toString()));
                        } else ReportingUtils.showMessage(" No Image were chosen ");
                    } else if (action == "live") {
                        app.enableLiveMode(true);
                        ImagePlus iPlus = IJ.getImage();
                        iPlus.show();

                        ImagePlus image_dup_ori = iPlus.duplicate();
                        image_dup_ori.setTitle("Img_Original_" + algo);
                        image_dup_ori.show();

                        ImagePlus image_dup = iPlus.duplicate();
                        image_dup.setTitle("Img_Segmented_" + algo);
                        image_dup.show();
                        List<Point2D.Double> ll = rappController_ref.imageSegmentation(image_dup, "", algo, true, false);
                        rappController_ref.shootFromSegmentationListPoint(ll, Long.parseLong(exposureT_laser_spinner.getValue().toString()));
                    }

                } else ReportingUtils.showMessage(" Please choose a Segmenter Algorithm");

            }
        });


        /// # Set Camera Exposure Time Event  # ///
       // gbc.gridy++;
//        JButton shootOnLearningP_btn = new JButton("Shoot on Learning");
//        right_box_shoot.add(shootOnLearningP_btn, gbc1);
//        shootOnLearningP_btn.setPreferredSize(new Dimension(140,28));
       // shootOnLearningP_btn.addActionListener(e -> rappController_ref.getListofROIs());
  //      gbc1.gridy++;
        // Point Table Internal Frame
//        try {
//            if (tablePointFrame == null) {
//                tablePointFrame= new CellPointInternalFrame();
//            }
//            tablePointFrame.setPreferredSize(new Dimension(700, 320));
//            tablePointFrame.setVisible(true);
//            right_box_shoot.add(tablePointFrame);
//            tablePointFrame.repaint();
//
//            //acquisition_.
//        } catch (Exception var2) {
//            ReportingUtils.showError(var2, "\nFrame invalid or corrupted settings.\nTry resetting .");
//        }
//
//        JInternalFrame buttonFrame = new JInternalFrame();
//
//
//        try {
//            BasicInternalFrameUI bi = (BasicInternalFrameUI)buttonFrame.getUI();
//            bi.setNorthPane(null);
//            buttonFrame.setPreferredSize(new Dimension(200, 320));
//            buttonFrame.setVisible(true);
//            right_box_shoot.add(buttonFrame);
//            buttonFrame.repaint();
//
//            JLabel rootLabel_2 = new JLabel();
//            rootLabel_2.setFont(new Font("Arial", Font.PLAIN, 10));
//            rootLabel_2.setText("XML configuration file :");
//            rootLabel_2.setBounds(10, 30, 150, 30);
//            buttonFrame.add(rootLabel_2);
//
//            xml_rootField_2 = new JTextField();
//            xml_rootField_2.setEnabled(false);
//            xml_rootField_2.setFont(new Font("Arial", Font.PLAIN, 8));
//            xml_rootField_2.setBounds(5, 55, 125, 30);
//            buttonFrame.add(xml_rootField_2);
//
//            browseXmlFIle_btn = new JButton();
//            browseXmlFIle_btn.addActionListener(e -> {
//                String xmlPath = fileDialog_.xmlFileChooserDialog();
//                if (xmlPath != null) {
//                    xml_rootField_2.setText(xmlPath);
//                }
//            });
//            browseXmlFIle_btn.setMargin(new Insets(2, 5, 2, 5));
//            browseXmlFIle_btn.setFont(new Font("Dialog", Font.PLAIN, 10));
//            browseXmlFIle_btn.setText("...");
//            browseXmlFIle_btn.setBounds(135, 55, 50, 30);
//            buttonFrame.add(browseXmlFIle_btn);
//            browseXmlFIle_btn.setToolTipText("Browse");
//
//
//            JLabel filter_exposureLabel = new JLabel();
//            filter_exposureLabel.setFont(new Font("Arial", Font.PLAIN, 10));
//            filter_exposureLabel.setText("FIlter Exposure Time (ms) :");
//            filter_exposureLabel.setBounds(5, 95, 150, 30);
//            buttonFrame.add(filter_exposureLabel);
//
//            JSpinner exposureT_filter_spinner2 =  new JSpinner(model_forExposure);
//            exposureT_filter_spinner2.setBounds(132, 95, 60, 30);
//            buttonFrame.add(exposureT_filter_spinner2);
//            exposureT_filter_spinner2.addChangeListener(e -> {
//               // rappController_ref.setExposure(1000 * Double.parseDouble(exposureT_filter_spinner.getValue().toString()));
//                System.out.println( exposureT_filter_spinner2.getValue().toString());
//            });
//
//            runSegmentation_btn = new JButton();
//            runSegmentation_btn.addActionListener(e -> {
//                if ( xml_rootField_2.getText().equals("")){
//                    ReportingUtils.showMessage("Please Try Again! "
//                            +"The XML Field is Empty");
//                }
//                else {
//                    String taggPath = fileDialog_.ChooseFileDialog("Please Choose a Bright Field Tiff Image with Cells");
//                    if ( taggPath != null ){
//                        rappController_ref.runSegmentation(xml_rootField_2.getText(), taggPath);
//                    }
//                    else {
//                        ReportingUtils.showMessage("Please Try Again! "
//                                +"No Image were chosen");
//                    }
//                }
//            });
//            runSegmentation_btn.setMargin(new Insets(2, 5, 2, 5));
//            runSegmentation_btn.setFont(new Font("Dialog", Font.PLAIN, 10));
//            runSegmentation_btn.setText("Run Segmentation");
//            runSegmentation_btn.setBounds(50, 135, 102, 30);
//            buttonFrame.add(runSegmentation_btn);
//            runSegmentation_btn.setToolTipText("Run Segmentation with Bright Field Image");
//
//            buttonFrame.add(new JLabel(""));
//            //////////////////////////////////////////////////////////////////////////////////////////////////
//            // acquisition_.
//        } catch (Exception var2) {
//            ReportingUtils.showError(var2, "\nFrame invalid or corrupted settings.\nTry resetting .");
//        }

        /////////////////////////////////// #Image Viewer Center Panel# //////////////////////////////////////////JPanel centerPanel = new JPanel();

        rightPanel.add(right_box_fov);
        right_box_fov.setPreferredSize(new Dimension(990, 545));   // vertical box
        right_box_fov.setVisible(false);

        right_box_fov.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Stage Position List",0,0,Font.getFont("arial"),  Color.decode("#34495e")));
        //right_box_fov.setVisible(true);
        BasicInternalFrameUI right_box_fov_bi = (BasicInternalFrameUI) right_box_fov.getUI();
        right_box_fov_bi.setNorthPane(null);
        right_box_fov.setBackground(Color.decode("#34495e"));

        try {
            if (fov_gui_ == null) {
                fov_gui_ = (new FOV_GUI(this, core, app));
            }
            fov_gui_.setPreferredSize(new Dimension(980, 545));
            fov_gui_.setVisible(true);
            right_box_fov.add(fov_gui_);
            fov_gui_.repaint();

            //acquisition_.
        } catch (Exception var2) {
            ReportingUtils.showError(var2, "\nAcquistion window failed to open due to invalid or corrupted settings.\nTry resetting registry settings to factory defaults (Menu Tools|Options).");
        }

        //////////////////////////////////////# Imaging # ///////////////////////////////////////////////

        rightPanel.add(asButtonPanel);
        asButtonPanel.setPreferredSize(new Dimension(910, 500));   // vertical box
        asButtonPanel.setVisible(false);
        asButtonPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Imaging Options",0,0,Font.getFont("arial"),  Color.decode("#34495e")));
        //asButtonPanel.setVisible(true);
        BasicInternalFrameUI AsButtonPanel_bi = (BasicInternalFrameUI) asButtonPanel.getUI();
        AsButtonPanel_bi.setNorthPane(null);
        asButtonPanel.setBackground(Color.decode("#34495e"));

        try {
            if (acquisition_ == null) {
                acquisition_ = (new SeqAcqGui(engine_, this.mainPrefs_, studio_, this.options_, core));
            }
            acquisition_.setPreferredSize(new Dimension(900, 500));
            acquisition_.setVisible(true);
            asButtonPanel.add(acquisition_);
            acquisition_.repaint();

            //acquisition_.
        } catch (Exception var2) {
            ReportingUtils.showError(var2, "\nAcquistion window failed to open due to invalid or corrupted settings.\nTry resetting registry settings to factory defaults (Menu Tools|Options).");
        }

        ///////////////////////// # Utilities # /////////////////////////////////////////////////////////

        splitPaneTop.setDividerLocation(80);
        splitPaneBody.setDividerLocation(200);
        this.add(splitPaneTop,BorderLayout.CENTER);
        splitPaneTop.setOneTouchExpandable(false);
        splitPaneTop.setContinuousLayout(false);

       // this.setBackground(Color.blue);
        this.setSize(730, 590);
       // this.setSize(1150, 700);
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

    private JToggleButton createPointJButton(String text) {
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

    private String chooseWhereToTakeTheImage() {
        int n = JOptionPane.showConfirmDialog(appInterface_,
                "Would you like to take an Image from disk?", "Choose Image", JOptionPane.YES_NO_CANCEL_OPTION);
        if (n == JOptionPane.YES_OPTION) {
           return "disk";
        }else if (n == JOptionPane.CANCEL_OPTION) {
            ReportingUtils.showMessage("Canceled : NO Image were Chosen");
            return "cancel" ;
        }
        else  {
            int n1 = JOptionPane.showConfirmDialog(appInterface_,
                    "Would you like to snap an Image from Live View?", "Choose Image", JOptionPane.YES_NO_CANCEL_OPTION);
            if (n1 == JOptionPane.YES_OPTION) {
                return "live";
            }
            else if (n == JOptionPane.NO_OPTION) {
                ReportingUtils.showMessage("Sorry: There is no Other Options, Please Try Again");
                return "cancel" ;
            }
            else ReportingUtils.showMessage("Canceled : NO Image were Chosen");

        }

        return "cancel" ;
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
    public void liveModeEnabled(final boolean b) {
        LiveMode_btn.setSelected(b);
        LiveMode_btn.setText(  b ? "Stop Live View" : "Start Live View" );
        LiveMode_btn.setBackground(b? Color.decode("#d35400") :Color.decode("#d35400") );
        LiveMode_btn.setUI(new MetalToggleButtonUI() {
            @Override
            protected Color getSelectColor() {
                return (b? Color.decode("#d35400") :Color.decode("#d35400") );
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {

    }

    @Override
    public void propertiesChangedAlert() {

    }

    @Override
    public void propertyChangedAlert(String s, String s1, String s2) {

    }

    @Override
    public void configGroupChangedAlert(String s, String s1) {

    }

    @Override
    public void systemConfigurationLoaded() {

    }

    @Override
    public void pixelSizeChangedAlert(double v) {

    }

    @Override
    public void stagePositionChangedAlert(String s, double v) {

    }

    @Override
    public void xyStagePositionChanged(String s, double v, double v1) {

    }

    @Override
    public void exposureChanged(String s, double v) {

    }

    @Override
    public void slmExposureChanged(String s, double v) {

    }
}