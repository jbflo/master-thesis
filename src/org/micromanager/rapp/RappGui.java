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

import com.sun.glass.ui.Size;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.ImageWindow;
import mmcorej.CMMCore;
import org.micromanager.MMStudio;
import org.micromanager.SnapLiveManager;
import org.micromanager.acquisition.LiveModeTimer;
import org.micromanager.api.ScriptInterface;
import org.micromanager.utils.GUIUtils;
import org.micromanager.utils.MMScriptException;
import org.micromanager.utils.ReportingUtils;

import java.awt.*;
import javax.swing.*;

/**
 *
 * @author FLorial
 */

import java.awt.BorderLayout;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.net.URL;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.metal.MetalToggleButtonUI;

public class RappGui extends JFrame {

    private static RappGui appInterface_;
    public static RappGui getInstance() {
        return appInterface_;
    }
    private  static  RappController rappController_ref;
    private SnapLiveManager SnapLiveManager_;
    private MMStudio studio_;
    private URL default_path = this.getClass().getResource("");
    private String path = default_path.toString().substring(6);
    private JPanel leftPanel = new JPanel();
    private Box left_box = Box.createVerticalBox();
    private JPanel right_box_setup = new JPanel();
    private Box right_box_learning  = Box.createVerticalBox();
    private JPanel right_box_shoot = new JPanel();
    private FlowLayout experimentLayout;
    private JPanel centerPanel = new JPanel();
    private JDesktopPane desktop;
    private JPanel rightPanel = new JPanel();
    private JPanel buttonPanel  = new JPanel();
    private JSplitPane spliPaneLeft = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, centerPanel);
    private JSplitPane splitPaneRight = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, spliPaneLeft, rightPanel);
    private JSplitPane splitPaneButton = new JSplitPane(JSplitPane.VERTICAL_SPLIT, splitPaneRight, buttonPanel);
    public  JLabel spiner = new JLabel("nothing");
    private SpinnerModel model_forExposure = new SpinnerNumberModel(100, 0, 9999, 1);
    private SpinnerModel model_forDelay = new SpinnerNumberModel(0, 0, 9999, 1);
    private SpinnerModel model_forColorLevel = new SpinnerNumberModel(100, 0, 100, 1);
    private JSpinner exposureT_spinner = new JSpinner(model_forExposure);
    protected JSpinner delayField_ = new JSpinner(model_forDelay);
    private JButton setupOption_btn = new JButton("Settings");
    private JToggleButton lightOnOff_jbtn = new JToggleButton("Open Light");
    private JButton learnOption_btn = new JButton("Analysis");
    private JButton shootOption_btn = new JButton("Shoot Option");
    private JToggleButton pointAndShootOnOff_btn = new JToggleButton("ON");
    private JToggleButton LiveMode_btn = new JToggleButton("Start Live View");
    private JButton SnapAndSave_btn = new JButton("Snap And Save Image");
    private JButton showCenterSpot_btn = new JButton("Show Center Spot");
    protected JButton calibrate_btn = new JButton("Start Calibration!");
    private JButton setAddRois_btn = new JButton("Set / Add Rois");
    private JButton shootOnLearningP_btn = new JButton("Shoot on Learning ");
    private JButton loadImage_btn = new JButton("Load An Image");
    private JButton ShootonMarkpoint_btn = new JButton("Shoot on ROIs");
    private JComboBox presetConfList_jcb ;
    private JComboBox Sequence_jcb ;
    private JComboBox groupConfList_jcb;



     /**
     * Constructor. Creates the main window for the Projector plugin. we use this Class for the main interface
     */
    public RappGui(CMMCore core, ScriptInterface app) throws Exception {
        studio_= (MMStudio)  app;
        //new RappController(core, app);
        rappController_ref =  new RappController(core, app);
        SnapLiveManager_ = new SnapLiveManager( studio_, core);

        try {
            setDefaultLookAndFeelDecorated(true);
            UIManager.setLookAndFeel("com.jtattoo.plaf.smart.SmartLookAndFeel");

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
           e.printStackTrace();
        }

        this.setTitle("Rapp UGA-42 Control");

        ImageIcon icon = new ImageIcon(path.concat("Resources/camera.png"));
        this.setIconImage(icon.getImage());


        this.setLayout(new BorderLayout());
        experimentLayout = new FlowLayout();

        ///////////// Left Panel Content ////////////////////////////
        leftPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.decode("#34495e"), Color.decode("#ecf0f1")));
        leftPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Menu" ,0,0,Font.getFont("arial"),  Color.white ));

        ///////////// we put all the component from the left into a Horizontal Box //////////////////////////////////
        leftPanel.add(left_box);
        left_box.setPreferredSize(new Dimension(150, 400));   // vertical box


        left_box.add(Box.createVerticalStrut(10));

        // SETUP BUTTON, OPEN THE BOX OPTION TO MANAGE SETUP
        setupOption_btn.setMaximumSize(new Dimension(145, 50));
        left_box.add(setupOption_btn);
        setupOption_btn.setBackground(Color.decode("#ecf0f1"));
        setupOption_btn.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                right_box_setup.setVisible(true);
                setupOption_btn.setBackground(Color.decode("#3498db"));
                shootOption_btn.setBackground(Color.decode("#ecf0f1"));
                learnOption_btn.setBackground(Color.decode("#ecf0f1"));
                right_box_shoot.setVisible(false);
                right_box_learning.setVisible(false);

            }
        } );

        left_box.add(Box.createVerticalStrut(5));

        // Leaning BUTTON, OPEN THE BOX OPTION TO MANAGE the MACHINE LEARNING PART
        learnOption_btn.setMaximumSize(new Dimension(145, 50));
        learnOption_btn.setBackground(Color.decode("#ecf0f1"));
        left_box.add(learnOption_btn);
        learnOption_btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                right_box_learning.setVisible(true);
                setupOption_btn.setBackground(Color.decode("#ecf0f1"));
                shootOption_btn.setBackground(Color.decode("#ecf0f1"));
                learnOption_btn.setBackground(Color.decode("#3498db"));
                right_box_shoot.setVisible(false);
                right_box_setup.setVisible(false);
            }
        });

        left_box.add(Box.createVerticalStrut(5));

        // SHOOT BUTTON, OPEN THE BOX OPTION TO MANAGE SHOOT
        shootOption_btn.setMaximumSize(new Dimension(145, 50));
        shootOption_btn.setBackground(Color.decode("#ecf0f1"));
        left_box.add(shootOption_btn);
        shootOption_btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                right_box_shoot.setVisible(true);
                setupOption_btn.setBackground(Color.decode("#ecf0f1"));
                shootOption_btn.setBackground(Color.decode("#3498db"));
                learnOption_btn.setBackground(Color.decode("#ecf0f1"));
                right_box_setup.setVisible(false);
                right_box_learning.setVisible(false);
            }
        } );

        left_box.add(Box.createVerticalStrut(15));
        left_box.add( new JSeparator(SwingConstants.HORIZONTAL) , left_box,7);
        left_box.getComponent(7).setPreferredSize(new Dimension(150,2));


        // Start or Stop the the Live Mode
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
        SnapAndSave_btn.setMaximumSize(new Dimension(145, 50));
        SnapAndSave_btn.setBackground(Color.decode("#3498db"));
        SnapAndSave_btn.setForeground(Color.white);
        left_box.add(SnapAndSave_btn);
        SnapAndSave_btn.addActionListener(e -> {

            rappController_ref.snapAndSaveImage();

        });

        left_box.add(Box.createVerticalStrut(5));

        // Illuminate the center of the photo targeting device's range
        showCenterSpot_btn.setBackground(Color.decode("#3498db"));
        showCenterSpot_btn.setMaximumSize((new Dimension(145, 50)));
        left_box.add(showCenterSpot_btn);
        showCenterSpot_btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                rappController_ref.displayCenterSpot();
            }
        } );
        left_box.setBackground(Color.decode("#34495e"));
        leftPanel.setBackground(Color.decode("#34495e"));

        /////////////////////////////////// #Center Panel# //////////////////////////////////////////
       centerPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.decode("#34495e"), Color.decode("#ecf0f1")));
        centerPanel.setBorder(BorderFactory.createTitledBorder(
               BorderFactory.createEtchedBorder(), "View",0,0,Font.getFont("arial"),  Color.white));
        centerPanel.setBackground(Color.decode("#7f8fa6"));
       //centerPanel.add(text1);
       //centerPanel.add(text2);

       //centerPanel.add(spiner);

        //Make dragging a little faster but perhaps uglier.
        //desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
        desktop = new JDesktopPane(); //a specialized layered pane

        //createFrame(); //create first "window"
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                SnapLiveWindow window = new SnapLiveWindow();
                ImageWindow snap =  SnapLiveManager_.getSnapLiveWindow();
               // window.add(snap);
                //desktop.add(window);
               // window.setVisible(true); //necessary as of 1.3
                //window.setPreferredSize(new Dimension(300, 400));
                //centerPanel.add(window);
                try {
                    window.setSelected(true);
                } catch (java.beans.PropertyVetoException e) {}

            }
        });


       // setContentPane(desktop);
        try {
            new JLabel(core.getDeviceName(core.getCameraDevice()));
            new JLabel(core.getDeviceName(core.getGalvoDevice()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        ////////////////////// #Mange Right panel an Content here#  /////////////////////////////////////
        rightPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.decode("#34495e"), Color.decode("#ecf0f1")));
        rightPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Options",0,0,Font.getFont("arial"),  Color.white));

        ////////////////////////////////  right_box_Settings Content //////////////////////////////////
        rightPanel.setBackground(Color.decode("#34495e"));
        right_box_setup.setBackground(Color.decode("#34495e"));
        right_box_learning.setBackground(Color.decode("#34495e"));
        right_box_shoot.setBackground(Color.decode("#34495e"));


        rightPanel.add(right_box_setup);
        right_box_setup.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.EAST;
      //  right_box_setup.setPreferredSize(new Dimension(300, 500));
        right_box_setup.setVisible(false);


        right_box_setup.add(new JLabel("<html><font color='white'>Exposure Time (ms) :</font></html>"),gbc);
        gbc.gridy++;
        right_box_setup.add(new JLabel("<html><font color='white'>Light (On / OFF)   :</font></html>"), gbc);
        gbc.gridy++;
        right_box_setup.add(new JLabel("<html><font color='white'>Delays (ms)    :</font></html>"), gbc);
        gbc.gridy++;
        right_box_setup.add(new JLabel("<html><font color='white'>Start Calibration  :</font></html>"),gbc);
        gbc.gridy++;
        right_box_setup.add( new JSeparator(SwingConstants.HORIZONTAL),  gbc, 1); this.right_box_setup.getComponent(1).setPreferredSize(new Dimension(100,10));
        gbc.gridy++;
        right_box_setup.add(new JLabel("<html><font color='white'>Group Conf.      :</font></html>"),gbc);
        gbc.gridy++;
        right_box_setup.add(new JLabel("<html><font color='white'>Preset Conf.      :</font></html>"),gbc);
        gbc.gridy++;
        right_box_setup.add(new JLabel("<html><font color='white'>Sequences     :</font></html>"),gbc);

        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridy = 0;
        gbc.gridx++;
        gbc.gridwidth = GridBagConstraints.REMAINDER;

        /// # Set Exposure Time Event  # ///
        exposureT_spinner.setPreferredSize(new Dimension(100, 20));
        right_box_setup.add(exposureT_spinner, gbc);
        exposureT_spinner.addChangeListener(e -> {
            rappController_ref.setExposure(1000 * Double.parseDouble(exposureT_spinner.getValue().toString()));
            System.out.println( exposureT_spinner.getValue().toString());

        });

        gbc.gridy++;
         /////////////  # Set illumination, Use to Turn On AND Off the Device # //////////////
        right_box_setup.add( lightOnOff_jbtn, gbc);
        lightOnOff_jbtn.setPreferredSize(new Dimension(100, 20));
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
        delayField_.setPreferredSize(new Dimension(100, 20));
        right_box_setup.add(delayField_, gbc);
        gbc.gridy++;
        calibrate_btn.setPreferredSize(new Dimension(100, 20));
        right_box_setup.add(calibrate_btn, gbc); // Calibrate Button Action
        calibrate_btn.addActionListener(e -> {
            try {
                boolean running = rappController_ref.isCalibrating();
                if (running) {
                    rappController_ref.stopCalibration();
                   // calibrate_btn.setText("is Calibrate");

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
        right_box_setup.add( new JSeparator(SwingConstants.HORIZONTAL),  gbc, 2);this.right_box_setup.getComponent(2).setPreferredSize(new Dimension(100,10));


        gbc.gridy++;
        String comboBoxConfigGroup[] = rappController_ref.getConfigGroup();
        // / Liste of available Configurations Settings
        groupConfList_jcb = new JComboBox(comboBoxConfigGroup);
        right_box_setup.add(groupConfList_jcb,gbc);
        groupConfList_jcb.setPreferredSize(new Dimension(100, 20));

        groupConfList_jcb.addActionListener(e -> {
            String GroupConfN = groupConfList_jcb.getSelectedItem().toString();
            DefaultComboBoxModel model = new DefaultComboBoxModel(rappController_ref.getConfigPreset(GroupConfN));
            DefaultComboBoxModel model2 = new DefaultComboBoxModel(rappController_ref.getConfigPreset(GroupConfN));
            presetConfList_jcb.setModel(model);
            Sequence_jcb.setModel(model2);
            Sequence_jcb.addItem("Apply ALL Sequence");
        });

        gbc.gridy++;
        presetConfList_jcb = new JComboBox(new DefaultComboBoxModel());
        right_box_setup.add(presetConfList_jcb, gbc);
        presetConfList_jcb.setPreferredSize(new Dimension(100, 20));
        presetConfList_jcb.addActionListener(e -> {

            String GroupConfN = groupConfList_jcb.getSelectedItem().toString();
            String PresetName = presetConfList_jcb.getSelectedItem().toString();
            // # Here we Apply the set form the Group configuration Settings
            rappController_ref.ChangeConfigSet(GroupConfN, PresetName);
        });


        gbc.gridy++;
        Sequence_jcb = new JComboBox(new DefaultComboBoxModel());
        right_box_setup.add(Sequence_jcb, gbc);
        Sequence_jcb.setPreferredSize(new Dimension(100, 20));
        Sequence_jcb.addActionListener(e -> {
            String GroupConfN = groupConfList_jcb.getSelectedItem().toString();
            String SequenceName = Sequence_jcb.getSelectedItem().toString();
            rappController_ref.fluorescenceSequence(GroupConfN, SequenceName);
            confirmSaving();
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

        right_box_shoot.add(new JLabel("<html><font color='white'>Point And Shoot Mode :</font></html>"), gbc1);
        gbc1.gridy++;
        right_box_shoot.add(new JLabel("<html><font color='white'>ROIs Manager       :</font></html>"), gbc1);
        gbc1.gridy++;
        right_box_shoot.add(new JLabel("<html><font color='white'>ROis Point  :</font></html>"),gbc1);
        gbc1.gridy++;
        right_box_shoot.add(new JLabel("<html><font color='white'>Leaning Point   :</font></html>"), gbc1);
        gbc1.gridy++;
        right_box_shoot.add(new JLabel("<html><font color='white'>Load An Image  :</font></html>"),gbc1);

        gbc1.anchor = GridBagConstraints.WEST;
        gbc1.gridy = 0;
        gbc1.gridx++;
        gbc1.gridwidth = GridBagConstraints.REMAINDER;

        right_box_shoot.add( pointAndShootOnOff_btn, gbc1);
        pointAndShootOnOff_btn.setPreferredSize(new Dimension(100,20));
        pointAndShootOnOff_btn.addActionListener(e -> {
            updatePointAndShoot();

        });

        gbc1.gridy++;
        right_box_shoot.add(setAddRois_btn, gbc1);
        setAddRois_btn.setPreferredSize(new Dimension(100,20));
        setAddRois_btn.addActionListener(e -> {
            RappPlugin.showRoiManager();
        });

        gbc1.gridy++;
        right_box_shoot.add(ShootonMarkpoint_btn, gbc1);
        ShootonMarkpoint_btn.setPreferredSize(new Dimension(100,20));
        ShootonMarkpoint_btn.addActionListener(e -> rappController_ref.createMultiPointAndShootFromRoeList());

        gbc1.gridy++;
        right_box_shoot.add(shootOnLearningP_btn, gbc1);
        shootOnLearningP_btn.setPreferredSize(new Dimension(100,20));
       // shootOnLearningP_btn.addActionListener(e -> rappController_ref.getListofROIs());

        gbc1.gridy++;
        right_box_shoot.add(loadImage_btn, gbc1);
        loadImage_btn.setPreferredSize(new Dimension(100,20));
        loadImage_btn.addActionListener(e -> {
            ImagePlus image = IJ.openImage(path.concat("Resources/simCell2DPNG.PNG"));
            image.show();
        });



        ///////////////////////////////////# Machine Learning Code # ///////////////////////////////////
        rightPanel.add(right_box_learning);
        right_box_learning.setPreferredSize(new Dimension(150, 150));   // vertical box
        right_box_learning.setVisible(false);

        //////////////////////////////////////# Imaging # ///////////////////////////////////////////////
        buttonPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.decode("#34495e"), Color.decode("#ecf0f1")));
        buttonPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Imaging Options",0,0,Font.getFont("arial"),  Color.white));


        spliPaneLeft.setDividerLocation(170);
        splitPaneRight.setDividerLocation(600);
        splitPaneButton.setDividerLocation(500);

        this.add(splitPaneButton, BorderLayout.CENTER);
        spliPaneLeft.setOneTouchExpandable(true);
        splitPaneRight.setOneTouchExpandable(true);
        splitPaneButton.setOneTouchExpandable(true);

        this.setBackground(Color.blue);
        this.setSize(900, 700);
        this.setVisible(true);

        /// Avoid all App Close when close the Plugin GUI
        this.setDefaultCloseOperation(0); // DO_NOTHING_ON_CLOSE
        this.setVisible(true);
        this.setLocation(32, 32);
        this.addWindowListener(new WindowAdapter() { // Windows Close button action event
            @Override
            public void windowClosing(WindowEvent we) {
                confirmQuit();
            }
        });
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
        }
        else  GUIUtils.recallPosition(appInterface_);
    }

    //  Avoid user to accidentally close the window by this fonction
    private void confirmSaving() {
        int n = JOptionPane.showConfirmDialog(appInterface_,
                "Do you want to save sequence image?", "Saving", JOptionPane.YES_NO_OPTION);
        if (n == JOptionPane.YES_OPTION) {
            rappController_ref.snapAndSaveImage();
        }
        else  GUIUtils.recallPosition(appInterface_);
    }

    //Create a new internal frame.
    protected void createFrame() {
        SnapLiveWindow window = new SnapLiveWindow();
       //ImageWindow window =  SnapLiveManager_.getSnapLiveWindow();
        window.setVisible(true); //necessary as of 1.3
        desktop.add(window );

//        try {
//            window.setSelected(true);
//        } catch (java.beans.PropertyVetoException e) {}
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

}