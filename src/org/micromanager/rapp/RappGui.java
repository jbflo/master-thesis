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
    public Thread SnapDisplayImage;
    URL default_path = this.getClass().getResource("");
    String path = default_path.toString().substring(6);

    JPanel leftPanel = new JPanel();
    Box left_box = Box.createVerticalBox();
    Box right_box_setup = Box.createVerticalBox();
    Box right_box_learning  = Box.createVerticalBox();
    Box right_box_shoot = Box.createVerticalBox();
    JLabel text1 = new JLabel();
    JLabel text2 = new JLabel();
    JLabel lbl_btn_onoff = new JLabel("Toggles calibration mode", SwingConstants.CENTER);
    JLabel lbl_for_Rois = new JLabel("Rois Settings", SwingConstants.CENTER);
    JLabel lbl_for_onoff_light = new JLabel("Point And Shoot Mode");
    JButton setupButton = new JButton("Settings");
    JToggleButton lightOnOffButton = new JToggleButton("Open Light");
    JButton learnButton = new JButton("Learning");
    JButton shootButton = new JButton("Shoot Option");
    JToggleButton pointAndShootOnOffButton = new JToggleButton("ON");
    JToggleButton LiveModeButton = new JToggleButton("Start Live View");
    JButton showCenterSpotButton = new JButton("Show Center Spot");
    JButton calibrateButton = new JButton("Calibration");
    JButton setAddRoisButton = new JButton("Set / Add Rois");
    JButton readRoisButton = new JButton("Read Mark Rois");
    JButton loadImage = new JButton("Load An Image");
    JPanel centerPanel = new JPanel();
    JPanel rightPanel = new JPanel();
    JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, centerPanel);
    JSplitPane sp2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sp, rightPanel);


    /**
     * Constructor. Creates the main window for the Projector plugin.
     */
    public RappGui(CMMCore core, ScriptInterface app) {
        rappController_ref =  RappController.getInstance();

        try {
            UIManager.setLookAndFeel("com.jtattoo.plaf.smart.SmartLookAndFeel");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
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
        setupButton.setMaximumSize(new Dimension(145, 50));
        left_box.add(setupButton);
        setupButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                right_box_setup.setVisible(true);
                right_box_shoot.setVisible(false);
                right_box_learning.setVisible(false);
            }
        } );

        left_box.add(Box.createVerticalStrut(5));

        // Leaning BUTTON, OPEN THE BOX OPTION TO MANAGE the MACHINE LEARNING PART
        learnButton.setMaximumSize(new Dimension(145, 50));
        left_box.add(learnButton);
        learnButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                right_box_learning.setVisible(true);
                right_box_shoot.setVisible(false);
                right_box_setup.setVisible(false);
            }
        });

        left_box.add(Box.createVerticalStrut(5));

        // SHOOT BUTTON, OPEN THE BOX OPTION TO MANAGE SHOOT
        shootButton.setMaximumSize(new Dimension(145, 50));
        left_box.add(shootButton);
        shootButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                right_box_shoot.setVisible(true);
                right_box_setup.setVisible(false);
                right_box_learning.setVisible(false);

            }
        } );

        left_box.add(Box.createVerticalStrut(5));

        // Start or Stop the the Live Mode
        LiveModeButton.setMaximumSize(new Dimension(145, 50));
        left_box.add(LiveModeButton);
        LiveModeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (LiveModeButton.isSelected() == true ){
                    LiveModeButton.setText("Stop Live View");
                    //LiveModeButton.col
                }else LiveModeButton.setText("Start Live View");
               LiveModeButton(e);

            }
        } );

        left_box.add(Box.createVerticalStrut(5));

        // Illuminate the center of the photo targeting device's range
        showCenterSpotButton.setMaximumSize((new Dimension(145, 50)));
        left_box.add(showCenterSpotButton);
        showCenterSpotButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

            }
        } );



        /////////////////////////// Center Panel ////////////// ////////
        centerPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        centerPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "View"));
        centerPanel.add(text1);
        centerPanel.add(text2);
        try {
            text1.setText(core.getDeviceName(core.getCameraDevice().toString()));
            text2.setText(core.getDeviceName(core.getGalvoDevice().toString()));
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
        right_box_setup.add(lbl_btn_onoff); //lbl_btn_onoff.setHorizontalTextPosition(50);
        right_box_setup.add(Box.createVerticalStrut(10));

        lightOnOffButton.setMaximumSize(new Dimension(80, 20));
        right_box_setup.add( lightOnOffButton);
        lightOnOffButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (lightOnOffButton.isSelected() == true ){
                    lightOnOffButton.setText("Off Light");
                    //LiveModeButton.col
                }else lightOnOffButton.setText("Open Light");
            }
        });

        right_box_setup.add(Box.createVerticalStrut(10));

        calibrateButton.setMaximumSize(new Dimension(80, 20));
        right_box_setup.add(calibrateButton);

        // Right Box Setup Content

        right_box_setup.add(Box.createVerticalStrut(10));
        right_box_setup.add(lbl_for_Rois);

        ////////////////////////////////  right_box_shoot Content //////////////////////////////////

        rightPanel.add(right_box_shoot);
        right_box_shoot.setPreferredSize(new Dimension(150, 150));   // vertical box
        right_box_shoot.setVisible(false);


        pointAndShootOnOffButton.setMaximumSize(new Dimension(80, 20));
        right_box_shoot.add( pointAndShootOnOffButton);
        pointAndShootOnOffButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                String galvo_ = core.getGalvoDevice();
                if (pointAndShootOnOffButton.isSelected() == true ){
                    pointAndShootOnOffButton.setText("OFF");
                    try {
                       // core.setGalvoIlluminationState(galvo_, true);
                        Point2D ppos=core.getGalvoPosition(galvo_); System.out.println(ppos);
                       // Point2D newppos = null;  newppos.setLocation( ppos.getX() +100, ppos.getY() +100); //core.setGalvoPosition(galvo_, newppos.getX(), newppos.getY());
                        core.pointGalvoAndFire(galvo_,25264.0, 24494.0, 500000);
                    } catch (Exception ex) {
                        ReportingUtils.logError(ex);
                        ex.printStackTrace();
                    }
                    //LiveModeButton.col
                }else {
                    pointAndShootOnOffButton.setText("ON");
                    try {
                       // core.setGalvoIlluminationState(galvo_, false);
                        core.pointGalvoAndFire(galvo_,40000.0, 40000.0, 500000);
                       // core.pointGalvoAndFire(galvo_, 900, 900, 500000);
                    } catch (Exception ex) {
                        ReportingUtils.logError(ex);
                        ex.printStackTrace();
                    }
                }



                // rappP_.shootL();
                //  pointAndShootOnButtonActionPerformed(e);
            }
        } );

        right_box_shoot.add(Box.createVerticalStrut(5));

        right_box_shoot.add(readRoisButton);
        readRoisButton.setMaximumSize(new Dimension(80, 20));
        readRoisButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rappController_ref.getROIs();
            }
        });

        right_box_shoot.add(Box.createVerticalStrut(5));
        right_box_shoot.add(loadImage);
        loadImage.setMaximumSize(new Dimension(80, 20));
        loadImage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ImagePlus image = IJ.openImage(path.concat("Resources/simCell2DPNG.PNG"));
                image.show();
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
        while(LiveModeButton.isSelected()){
           rappController_ref.setLive(LiveModeButton.isSelected());
        }
    }


    /**
     * Sets the Point and Shoot "On and Off" buttons to a given state.
     * @param turnedOn true = Point and Shoot is ON
     */
    public void updatePointAndShoot(boolean turnedOn) {
        pointAndShootOnOffButton.setSelected(turnedOn);
       // pointAndShootOffButton.setSelected(!turnedOn);
      // rappController_ref.enablePointAndShootMode(turnedOn);
    }

    private void LiveModeButton(java.awt.event.ActionEvent evt) {
        // open new Thread for snap Image
        SnapDisplayImage = new Thread(new ThreadClass(this));
        SnapDisplayImage.start();

    }

    private void onButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onButtonActionPerformed
        //rappController_ref.setOnState(true);
        pointAndShootOffButtonActionPerformed(null);
    }//GEN-LAST:event_onButtonActionPerformed

    private void offButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_offButtonActionPerformed
      ///  rappController_ref.setOnState(false);
    }//GEN-LAST:event_offButtonActionPerformed


    private void centerButtonActionPerformed(java.awt.event.ActionEvent e) {//GEN-FIRST:event_centerButtonActionPerformed
        offButtonActionPerformed(null);
       // rappController_ref.displayCenterSpot();
    }//GEN-LAST:event_centerButtonActionPerformed

    private void pointAndShootOffButtonActionPerformed(java.awt.event.ActionEvent e) {//GEN-FIRST:event_pointAndShootOffButtonActionPerformed
        updatePointAndShoot(false);
    }//GEN-LAST:event_pointAndShootOffButtonActionPerformed

    private void pointAndShootOnButtonActionPerformed(java.awt.event.ActionEvent e) {//GEN-FIRST:event_pointAndShootOnButtonActionPerformed
        //offButtonActionPerformed(null);
      /*  String galvo_ = core_.getGalvoDevice();
        try {
             core_.setGalvoIlluminationState(galvo_, true);
             core_.pointGalvoAndFire(galvo_, 900, 900, 500000);
        } catch (Exception ex) {
            ReportingUtils.logError(ex);
        }
*/

       /* try {
            updatePointAndShoot(true);
        } catch (RuntimeException ex) {
            ReportingUtils.showError(ex);
        }*/
    }

}