package org.micromanager.rapp;

import ij.IJ;
import ij.ImagePlus;
import mmcorej.CMMCore;
import org.micromanager.MMStudio;
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
    private MMStudio gui_;
    String galvo_;
    private Thread SnapDisplayImage;
    private URL default_path = this.getClass().getResource("");
    String path = default_path.toString().substring(6);

    JPanel leftPanel = new JPanel();
    Box left_box = Box.createVerticalBox();
    Box right_box_setup = Box.createVerticalBox();
    Box right_box_learning  = Box.createVerticalBox();
    Box right_box_shoot = Box.createVerticalBox();
    SpinnerModel model = new SpinnerNumberModel(100, 0, 100000, 0.1);
    SpinnerModel model2 = new SpinnerNumberModel(0, 0, 100, 1);
    JSpinner spinner = new JSpinner(model);
    JSpinner delayField_ = new JSpinner(model2);
    JLabel text1 = new JLabel();
    JLabel text2 = new JLabel();
    JLabel lbl_btn_onoff = new JLabel("Toggles calibration mode", SwingConstants.CENTER);
    JLabel lbl_for_Rois = new JLabel("Rois Settings", SwingConstants.CENTER);
    JLabel lbl_for_onoff_light = new JLabel("Point And Shoot Mode");
    JButton setupOption_btn = new JButton("Settings");
    JToggleButton lightOnOff_jbtn = new JToggleButton("Open Light");
    JButton learnOption_btn = new JButton("Learning");
    JButton shootOption_btn = new JButton("Shoot Option");
    JToggleButton pointAndShootOnOff_btn = new JToggleButton("ON");
    JToggleButton LiveMode_btn = new JToggleButton("Start Live View");
    JButton showCenterSpot_btn = new JButton("Show Center Spot");
    JButton calibrate_btn = new JButton("Start Calibration!");
    JButton setAddRois_btn = new JButton("Set / Add Rois");
    JButton readRois_btn = new JButton("Read Mark Rois");
    JButton loadImage_btn = new JButton("Load An Image");
    JButton ShootonMarkpoint_btn = new JButton("Shoot on Mark Point ");
    JPanel centerPanel = new JPanel();
    JPanel rightPanel = new JPanel();
    JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, centerPanel);
    JSplitPane sp2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sp, rightPanel);


    /**
     * Constructor. Creates the main window for the Projector plugin.
     */
    public RappGui(CMMCore core, ScriptInterface app) {
        new RappController(core, app);
        rappController_ref =  new RappController(core, app); ;

        try {
            UIManager.setLookAndFeel("com.jtattoo.plaf.smart.SmartLookAndFeel");
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        this.setTitle("Rapp UGA-42 Control");


        ImageIcon icon = new ImageIcon(path.concat("Resources/camera.png"));
        this.setIconImage(icon.getImage());

        // we use this Class for the main interface
        this.setLayout(new BorderLayout());

        ///////////// Left Panel Content ////////////////////////////
        leftPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        leftPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Menu"));

        // we put all the component from the left into a Box , and the Box into the Left Panel
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



        /////////////////////////// Center Panel ////////////// ////////
        centerPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        centerPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "View"));
        centerPanel.add(text1);
        centerPanel.add(text2);
        try {
            text1.setText(core.getDeviceName(core.getCameraDevice()));
            text2.setText(core.getDeviceName(core.getGalvoDevice()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        /////////////////// Mange Right panel an Content here  /////////////////////////////////////
        rightPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        rightPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Options"));
        ////////////////////////////////  right_box_Settings Content //////////////////////////////////

        rightPanel.add(right_box_setup);
        right_box_setup.setPreferredSize(new Dimension(150, 150));   // vertical box
        right_box_setup.setVisible(false);

        right_box_setup.add("Set Exposure TIme: ", spinner);
        spinner.setMaximumSize(new Dimension(100, 30));
        right_box_setup.add(Box.createVerticalStrut(10));


        right_box_setup.add(lbl_btn_onoff); //lbl_btn_onoff.setHorizontalTextPosition(50);
        lightOnOff_jbtn.setMaximumSize(new Dimension(80, 20));
        right_box_setup.add("Illuminate :", lightOnOff_jbtn);
        lightOnOff_jbtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (lightOnOff_jbtn.isSelected()){
                    lightOnOff_jbtn.setText("Off Light");
                    rappController_ref.setOnState(true);
                    //LiveModeButton.col
                }else {
                    lightOnOff_jbtn.setText("Open Light");
                    rappController_ref.setOnState(false);
                }
            }
        });


        right_box_setup.add(Box.createVerticalStrut(10));

        calibrate_btn.setMaximumSize(new Dimension(80, 20));
        right_box_setup.add(calibrate_btn);
        calibrate_btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
            }
        });
        right_box_setup.add("Calibration Delay" ,delayField_);

        // Right Box Setup Content

        right_box_setup.add(Box.createVerticalStrut(10));
        right_box_setup.add(lbl_for_Rois);

        ////////////////////////////////  right_box_shoot Content //////////////////////////////////

        rightPanel.add(right_box_shoot);
        right_box_shoot.setPreferredSize(new Dimension(150, 150));   // vertical box
        right_box_shoot.setVisible(false);


        pointAndShootOnOff_btn.setMaximumSize(new Dimension(100, 30));
        right_box_shoot.add( pointAndShootOnOff_btn);
        pointAndShootOnOff_btn.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                updatePointAndShoot();
                // rappP_.shootL();
                //  pointAndShootOnButtonActionPerformed(e);
            }
        } );

        right_box_shoot.add(Box.createVerticalStrut(5));

        right_box_shoot.add(readRois_btn);
        readRois_btn.setMaximumSize(new Dimension(80, 20));
        readRois_btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rappController_ref.getListofROIs();
            }
        });

        right_box_shoot.add(Box.createVerticalStrut(5));
        right_box_shoot.add(loadImage_btn);
        loadImage_btn.setMaximumSize(new Dimension(100, 30));
        loadImage_btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ImagePlus image = IJ.openImage(path.concat("Resources/simCell2DPNG.PNG"));
                image.show();
            }
        });
        ////////////////////////////////////////////////////////////////////
        right_box_shoot.add(Box.createVerticalStrut(5));
        right_box_shoot.add(ShootonMarkpoint_btn);
        ShootonMarkpoint_btn.setPreferredSize(new Dimension(100,30));
        ShootonMarkpoint_btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rappController_ref.createMultiPointAndShootFromRoeList();
            }
        });

        //////////////////////////////////////////////////////////////////
        rightPanel.add(right_box_learning);
        right_box_learning.setPreferredSize(new Dimension(150, 150));   // vertical box
        right_box_learning.setVisible(false);



        sp.setDividerLocation(150);

        sp2.setDividerLocation(600);

        this.add(sp2, BorderLayout.CENTER);
        // this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.setSize(800, 500);
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




    /**
     * Shows the GUI, which is a singleton.
     * @param core MMCore
     * @param app  ScritpInterface
     * @return singleton instance
     */
    public static RappGui showAppInterface(CMMCore core, ScriptInterface app) {
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
           rappController_ref.setLive(LiveMode_btn.isSelected());
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
        SnapDisplayImage = new Thread(new ThreadClass(this));
        SnapDisplayImage.start();

    }



}