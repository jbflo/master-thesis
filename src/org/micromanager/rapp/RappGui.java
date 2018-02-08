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
import org.micromanager.MMStudio;
import org.micromanager.SnapLiveManager;
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.net.URL;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

public class RappGui extends JFrame {

    private static RappGui appInterface_;
    public static RappGui getInstance() {
        return appInterface_;
    }
    private  static RappPlugin rappP_;
    private  static  RappController rappController_ref;
    private SnapLiveManager SnapLiveManager_;
    private MMStudio studio_;
    String galvo_;
    private Thread SnapDisplayImage;
    private URL default_path = this.getClass().getResource("");
    String path = default_path.toString().substring(6);

    private JPanel leftPanel = new JPanel();
    private Box left_box = Box.createVerticalBox();
    private JPanel right_box_setup = new JPanel();
    private Box right_box_learning  = Box.createVerticalBox();
    private JPanel right_box_shoot = new JPanel();
    private FlowLayout experimentLayout;
    private SpinnerModel model = new SpinnerNumberModel(100, 0, 9999, 1);
    private SpinnerModel model2 = new SpinnerNumberModel(0, 0, 9999, 1);
    private JSpinner exposureT_spinner = new JSpinner(model);
    JSpinner delayField_ = new JSpinner(model2);
    JLabel text1 = new JLabel();
    JLabel text2 = new JLabel();
    JLabel lbl_for_Rois = new JLabel("Rois Settings", SwingConstants.CENTER);
    private JButton setupOption_btn = new JButton("Settings");
    private JToggleButton lightOnOff_jbtn = new JToggleButton("Open Light");
    private JButton learnOption_btn = new JButton("Learning");
    private JButton shootOption_btn = new JButton("Shoot Option");
    private JToggleButton pointAndShootOnOff_btn = new JToggleButton("ON");
    private JToggleButton LiveMode_btn = new JToggleButton("Start Live View");
    private JButton showCenterSpot_btn = new JButton("Show Center Spot");
    JButton calibrate_btn = new JButton("Start Calibration!");
    private JButton setAddRois_btn = new JButton("Set / Add Rois");
    private JButton shootOnLearningP_btn = new JButton("Shoot on Learning ");
    private JButton loadImage_btn = new JButton("Load An Image");
    private JButton ShootonMarkpoint_btn = new JButton("Shoot on Mark ");
    private JPanel centerPanel = new JPanel();
    private JDesktopPane desktop = new JDesktopPane();
    private JPanel rightPanel = new JPanel();
    private JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, centerPanel);
    private JSplitPane sp2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sp, rightPanel);


     /**
     * Constructor. Creates the main window for the Projector plugin.
     */
    public RappGui(CMMCore core, ScriptInterface app) throws Exception {
        studio_= (MMStudio)  app;
        new RappController(core, app);
        rappController_ref =  new RappController(core, app);
        SnapLiveManager_ = new SnapLiveManager( studio_, core);

        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
           e.printStackTrace();
        }

        this.setTitle("Rapp UGA-42 Control");

        ImageIcon icon = new ImageIcon(path.concat("Resources/camera.png"));
        this.setIconImage(icon.getImage());

        // we use this Class for the main interface
        this.setLayout(new BorderLayout());
        experimentLayout = new FlowLayout();
        ///////////// Left Panel Content ////////////////////////////
        leftPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        leftPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Menu"));

        ///////////// we put all the component from the left into a Horizontal Box //////////////////////////////////
        leftPanel.add(left_box);
        left_box.setPreferredSize(new Dimension(150, 400));   // vertical box
        //left_box.setBackground(Color.BLUE);

        left_box.add(Box.createVerticalStrut(10));

        // SETUP BUTTON, OPEN THE BOX OPTION TO MANAGE SETUP
        setupOption_btn.setMaximumSize(new Dimension(145, 50));
        left_box.add(setupOption_btn);

        setupOption_btn.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                right_box_setup.setVisible(true);
                setupOption_btn.setBackground(Color.cyan);
                shootOption_btn.setBackground(Color.white);
                learnOption_btn.setBackground(Color.white);
                right_box_shoot.setVisible(false);
                right_box_learning.setVisible(false);
            }
        } );

        left_box.add(Box.createVerticalStrut(5));

        // Leaning BUTTON, OPEN THE BOX OPTION TO MANAGE the MACHINE LEARNING PART
        learnOption_btn.setMaximumSize(new Dimension(145, 50));
        left_box.add(learnOption_btn);
        learnOption_btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                right_box_learning.setVisible(true);
                setupOption_btn.setBackground(Color.white);
                shootOption_btn.setBackground(Color.white);
                learnOption_btn.setBackground(Color.cyan);
                right_box_shoot.setVisible(false);
                right_box_setup.setVisible(false);
            }
        });

        left_box.add(Box.createVerticalStrut(5));

        // SHOOT BUTTON, OPEN THE BOX OPTION TO MANAGE SHOOT
        shootOption_btn.setMaximumSize(new Dimension(145, 50));
        left_box.add(shootOption_btn);
        shootOption_btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                right_box_shoot.setVisible(true);
                setupOption_btn.setBackground(Color.white);
                shootOption_btn.setBackground(Color.cyan);
                learnOption_btn.setBackground(Color.white);
                right_box_setup.setVisible(false);
                right_box_learning.setVisible(false);
            }
        } );

        left_box.add(Box.createVerticalStrut(5));

        // Start or Stop the the Live Mode
        LiveMode_btn.setMaximumSize(new Dimension(145, 50));
        left_box.add(LiveMode_btn);
        LiveMode_btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (LiveMode_btn.isSelected() ){
                    LiveMode_btn.setText("Stop Live View");
                    //LiveModeButton.col
                }else LiveMode_btn.setText("Start Live View");
                //MMStudio.getInstance().enableLiveMode(true);
                createFrame();
               LiveModeButton(e);

            }
        } );

        left_box.add(Box.createVerticalStrut(5));

        // Illuminate the center of the photo targeting device's range
        showCenterSpot_btn.setMaximumSize((new Dimension(145, 50)));
        left_box.add(showCenterSpot_btn);
        showCenterSpot_btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                rappController_ref.displayCenterSpot();
            }
        } );

        /////////////////////////////////// #Center Panel# //////////////////////////////////////////
        centerPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        centerPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "View"));
        //centerPanel.add(text1);
        //centerPanel.add(text2);
        centerPanel.add(desktop);
        desktop.add(text1);
        try {
            text1.setText(core.getDeviceName(core.getCameraDevice()));
            text2.setText(core.getDeviceName(core.getGalvoDevice()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        ////////////////////// #Mange Right panel an Content here#  /////////////////////////////////////
        rightPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        rightPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Options"));

        ////////////////////////////////  right_box_Settings Content //////////////////////////////////

        rightPanel.add(right_box_setup);
        right_box_setup.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.EAST;
      //  right_box_setup.setPreferredSize(new Dimension(300, 500));
        right_box_setup.setVisible(false);

        right_box_setup.add(new JLabel("Set Spot Interval  :"), gbc);
        gbc.gridy++;
        right_box_setup.add(new JLabel("Set Illumination   :"), gbc);
        gbc.gridy++;
        right_box_setup.add(new JLabel("Set Delays (ms)    :"), gbc);
        gbc.gridy++;
        right_box_setup.add(new JLabel("Set Calibration    :"),gbc);

        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridy = 0;
        gbc.gridx++;
        gbc.gridwidth = GridBagConstraints.REMAINDER;

        right_box_setup.add(exposureT_spinner, gbc);
            exposureT_spinner.addChangeListener(e -> rappController_ref.setExposure(1000 * Double.parseDouble(exposureT_spinner.getValue().toString()))
        );

        gbc.gridy++;

        right_box_setup.add( lightOnOff_jbtn, gbc);
        lightOnOff_jbtn.setPreferredSize(new Dimension(100, 30));
        lightOnOff_jbtn.addActionListener(e -> {
            if (lightOnOff_jbtn.isSelected()){
                lightOnOff_jbtn.setText("Off Light");
                rappController_ref.setOnState(true);
                //LiveModeButton.col
            }else {
                lightOnOff_jbtn.setText("Open Light");
                rappController_ref.setOnState(false);
            }
        });
        gbc.gridy++;
        right_box_setup.add(delayField_, gbc);
        gbc.gridy++;
        calibrate_btn.setPreferredSize(new Dimension(100, 30));
        right_box_setup.add(calibrate_btn, gbc);
        calibrate_btn.addActionListener(e -> {
            try {
                boolean running = rappController_ref.isCalibrating();
                if (running) {
                    rappController_ref.stopCalibration();
                    calibrate_btn.setText("is Calibrate");
                    System.out.println("You run");
                } else {
                    rappController_ref.runCalibration();
                    calibrate_btn.setText("Stop calibration");
                    System.out.println("You do not run");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                ReportingUtils.showError(e);
            }
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

        right_box_shoot.add(new JLabel("PointAndShoot Mode :"), gbc1);
        gbc1.gridy++;
        right_box_shoot.add(new JLabel("Rois Manager       :"), gbc1);
        gbc1.gridy++;
        right_box_shoot.add(new JLabel("ROis Point  :"),gbc1);
        gbc1.gridy++;
        right_box_shoot.add(new JLabel("Leaning Point   :"), gbc1);
        gbc1.gridy++;
        right_box_shoot.add(new JLabel("Load An Image  :"),gbc1);


        gbc1.anchor = GridBagConstraints.WEST;
        gbc1.gridy = 0;
        gbc1.gridx++;
        gbc1.gridwidth = GridBagConstraints.REMAINDER;

        right_box_shoot.add( pointAndShootOnOff_btn, gbc1);
        pointAndShootOnOff_btn.setPreferredSize(new Dimension(100,30));
        pointAndShootOnOff_btn.addActionListener(e -> {
            updatePointAndShoot();

        });

        gbc1.gridy++;
        right_box_shoot.add(setAddRois_btn, gbc1);
        setAddRois_btn.setPreferredSize(new Dimension(100,30));
        setAddRois_btn.addActionListener(e -> {
            RappPlugin.showRoiManager();
        });

        gbc1.gridy++;
        right_box_shoot.add(ShootonMarkpoint_btn, gbc1);
        ShootonMarkpoint_btn.setPreferredSize(new Dimension(100,30));
        ShootonMarkpoint_btn.addActionListener(e -> rappController_ref.createMultiPointAndShootFromRoeList());

        gbc1.gridy++;
        right_box_shoot.add(shootOnLearningP_btn, gbc1);
        shootOnLearningP_btn.setPreferredSize(new Dimension(100,30));
       // shootOnLearningP_btn.addActionListener(e -> rappController_ref.getListofROIs());

        gbc1.gridy++;
        right_box_shoot.add(loadImage_btn, gbc1);
        loadImage_btn.setPreferredSize(new Dimension(100,30));
        loadImage_btn.addActionListener(e -> {
            ImagePlus image = IJ.openImage(path.concat("Resources/simCell2DPNG.PNG"));
            image.show();
        });



        ///////////////////////////////////# Machine Learning Code # ///////////////////////////////////
        rightPanel.add(right_box_learning);
        right_box_learning.setPreferredSize(new Dimension(150, 150));   // vertical box
        right_box_learning.setVisible(false);

        sp.setDividerLocation(170);

        sp2.setDividerLocation(600);

        this.add(sp2, BorderLayout.CENTER);
        // this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.setSize(900, 500);
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

    //Create a new internal frame.
    protected void createFrame() {
        ImageWindow window = SnapLiveManager_.getSnapLiveWindow();

        //window.setVisible(true); //necessary as of 1.3
        desktop.add(window );
//        try {
//            window.setSelected(true);
//        } catch (java.beans.PropertyVetoException e) {}
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

    public void liveDisplayThread() throws MMScriptException {
        while(LiveMode_btn.isSelected()){
            new Runnable() {
                public void run() {
                    try {
                        MMStudio.getInstance().enableLiveMode(!MMStudio.getInstance().isLiveModeOn());

                    }catch (Exception ex){
                        ex.printStackTrace();
                    }}
//        SnapDisplayImage = new Thread(new ThreadClass(this));
//        SnapDisplayImage.start();

            };
          // rappController_ref.setLive(LiveMode_btn.isSelected());
        }
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