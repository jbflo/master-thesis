package org.micromanager.rapp;

import mmcorej.CMMCore;
import org.micromanager.api.ScriptInterface;
import org.micromanager.utils.GUIUtils;
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
import javax.swing.JFrame;
import javax.swing.JPanel;

public class RappGui extends JFrame {
    private static RappGui appInterface_;
    private  static  RappController rappController_ref;


    JPanel leftPanel = new JPanel();
    Box left_box = Box.createVerticalBox();
    Box right_box_setup = Box.createVerticalBox();
    Box right_box_learning  = Box.createVerticalBox();
    Box right_box2_shoot = Box.createVerticalBox();
    JLabel lbl_menu_name = new JLabel(" Menu ", JLabel.CENTER);
    JLabel text1 = new JLabel();
    JLabel lbl_btn_onoff = new JLabel("Toggles calibration mode", SwingConstants.CENTER);
    JButton setupButton = new JButton("Setup");
    JButton learnButton = new JButton("Learning");
    JButton shootButton = new JButton("Shoot");
    JButton  pointAndShootOnButton = new JButton("ON");
    JButton pointAndShootOffButton = new JButton("OFF");
    JPanel centerPanel = new JPanel();
    JPanel rightPanel = new JPanel();
    JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, centerPanel);
    JSplitPane sp2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sp, rightPanel);

    /**
     * Constructor. Creates the main window for the Projector plugin.
     */
    public RappGui(CMMCore core, ScriptInterface app) {
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



        // we use this Class for the main interface
        this.setLayout(new BorderLayout());

        // we put all the component from the left into a Box , and the Box into the Left Panel
        leftPanel.add(left_box);
        left_box.setPreferredSize(new Dimension(150, 150));   // vertical box
        //left_box.setBackground(Color.BLUE);

        left_box.add(lbl_menu_name);
        lbl_menu_name.setPreferredSize(new Dimension(100, 20));  lbl_menu_name.setHorizontalAlignment(JLabel.CENTER); //lbl_menu_name.setHorizontalTextPosition(60);
        left_box.add(Box.createVerticalStrut(10));

        // SETUP BUTTON, OPEN THE BOX OPTION TO MANAGE SETUP
        setupButton.setMaximumSize(new Dimension(145, 50));
        left_box.add(setupButton);
        setupButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                right_box_setup.setVisible(true);
                rightPanel.setBackground(Color.blue);
            }
        } );

        left_box.add(Box.createVerticalStrut(5));

        // Leaning BUTTON, OPEN THE BOX OPTION TO MANAGE the MACHINE LEARNING PART
        learnButton.setMaximumSize(new Dimension(145, 50));
        left_box.add(learnButton);

        left_box.add(Box.createVerticalStrut(5));

        // SHOOT BUTTON, OPEN THE BOX OPTION TO MANAGE SHOOT
        shootButton.setMaximumSize(new Dimension(145, 50));
        left_box.add(shootButton);
        shootButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                right_box2_shoot.setVisible(true);
                right_box_setup.setVisible(false);
                right_box_learning.setVisible(false);
                rightPanel.setBackground(Color.green);

            }
        } );



        centerPanel.add(text1);
        try {
            text1.setText(core.getDeviceName(core.getCameraDevice().toString()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Mange Right panel an Content here
        rightPanel.add(right_box_setup);
        right_box_setup.setPreferredSize(new Dimension(150, 150));   // vertical box
        right_box_setup.setVisible(false);
        right_box_setup.setBackground(Color.blue);
        right_box_setup.add(lbl_btn_onoff); //lbl_btn_onoff.setHorizontalTextPosition(50);
        right_box_setup.add(Box.createVerticalStrut(10));
        right_box_setup.add( pointAndShootOnButton); right_box_setup.add(pointAndShootOffButton);
        right_box_setup.add(Box.createVerticalStrut(10));

        // Right Box Setup Content
        pointAndShootOnButton.setMaximumSize(new Dimension(80, 20));
        right_box_setup.add( pointAndShootOnButton);
        pointAndShootOnButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                centerPanel.setBackground(Color.green);
                pointAndShootOnButtonActionPerformed(e);
            }
        } );

        pointAndShootOffButton.setMaximumSize(new Dimension(80, 20));
        right_box_setup.add(pointAndShootOffButton);
        pointAndShootOffButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                centerPanel.setBackground(Color.red);
            }
        } );


        rightPanel.add(right_box_learning);
        right_box_learning.setPreferredSize(new Dimension(150, 150));   // vertical box
        right_box_learning.setVisible(false);
        right_box_setup.setBackground(Color.cyan);


        sp.setDividerLocation(150);

        sp2.setDividerLocation(600);

        this.add(sp2, BorderLayout.CENTER);
        // this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.setSize(800, 500);
        this.setVisible(true);

        /// Avoid all App Close when close the Plugin GUI
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);


        // show application interface
        //if (formSingleton_!= null) {
           //formSingleton_ = new RappGui( core,  app) ;
           // Place window where it was last.
          //GUIUtils.recallPosition(formSingleton_);
         //}
        this.setVisible(true);
        this.setLocation(32, 32);

    }






    public void control(){

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


    /**
     * Sets the Point and Shoot "On and Off" buttons to a given state.
     * @param turnedOn true = Point and Shoot is ON
     */
    public void updatePointAndShoot(boolean turnedOn) {
        pointAndShootOnButton.setSelected(turnedOn);
        pointAndShootOffButton.setSelected(!turnedOn);
        rappController_ref.enablePointAndShootMode(turnedOn);
    }

    private void onButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onButtonActionPerformed
        rappController_ref.setOnState(true);
        pointAndShootOffButtonActionPerformed(null);
    }//GEN-LAST:event_onButtonActionPerformed

    private void offButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_offButtonActionPerformed
        rappController_ref.setOnState(false);
    }//GEN-LAST:event_offButtonActionPerformed


    private void centerButtonActionPerformed(java.awt.event.ActionEvent e) {//GEN-FIRST:event_centerButtonActionPerformed
        offButtonActionPerformed(null);
        rappController_ref.displayCenterSpot();
    }//GEN-LAST:event_centerButtonActionPerformed

    private void pointAndShootOffButtonActionPerformed(java.awt.event.ActionEvent e) {//GEN-FIRST:event_pointAndShootOffButtonActionPerformed
        updatePointAndShoot(false);
    }//GEN-LAST:event_pointAndShootOffButtonActionPerformed

    private void pointAndShootOnButtonActionPerformed(java.awt.event.ActionEvent e) {//GEN-FIRST:event_pointAndShootOnButtonActionPerformed
        offButtonActionPerformed(null);
        try {
            updatePointAndShoot(true);
        } catch (RuntimeException ex) {
            ReportingUtils.showError(ex);
        }
    }

}