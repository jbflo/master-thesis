package org.micromanager.rapp;

import mmcorej.CMMCore;
import org.micromanager.api.ScriptInterface;
import org.micromanager.utils.GUIUtils;

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

public class PluginGui extends JFrame {
        private static PluginGui formSingleton_;
        //public static JLabel text1;

        public PluginGui(CMMCore core, ScriptInterface app) {

            this.setLayout(new BorderLayout());

            JPanel leftPanel = new JPanel();
            Box box = Box.createVerticalBox();  box.setPreferredSize(new Dimension(150,150));   // vertical box
            leftPanel.add(box);
         //   leftPanel.setBackground(Color.BLUE);

            JLabel leftLabel = new JLabel(" Option Part");
            box.add(leftLabel); leftLabel.setPreferredSize(new Dimension(100,20));

            box.add(Box.createVerticalStrut(10));
            JButton setupButton = new JButton("Setup");  setupButton.setMaximumSize(new Dimension(145, 50));
            box.add(setupButton);

            box.add(Box.createVerticalStrut(5));
            JButton learnButton = new JButton("Learning");learnButton.setMaximumSize(new Dimension(145, 50));
            box.add(learnButton);

            box.add(Box.createVerticalStrut(5));
            JButton shootButton = new JButton("Shoot");shootButton.setMaximumSize(new Dimension(145, 50));
            box.add(shootButton);

            shootButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {

                 //   d.setVisible(true);
                }
            });



            JPanel centerPanel = new JPanel();

            JLabel text1 =new JLabel();
            JLabel text2 =new JLabel();
            try {
                text1.setText(core.getDeviceName(core.getCameraDevice().toString()));
                text2.setText(core.getDeviceName(core.getGalvoDevice().toString()));


                text1.setText(core.getDeviceName(core.getCameraDevice().toString()));
            } catch (Exception e) {
                e.printStackTrace();
            }

            centerPanel.add(text1);
            centerPanel.add(text2);
           // centerPanel.setBackground(Color.CYAN);

            JPanel rightPanel = new JPanel();
           // rightPanel.setBackground(Color.GREEN);

            JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, centerPanel);
                       sp.setDividerLocation(150);
            JSplitPane sp2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sp, rightPanel);
            sp2.setDividerLocation(600);

            this.add(sp2, BorderLayout.CENTER);
           // this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            this.setSize(800, 500);
            this.setVisible(true);

            /// Avoid all App Close when close the Plugin GUI
            this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);


            // show application
           // if (formSingleton_== null) {
             //   formSingleton_ = new PluginGui( core,  app) ;
                // Place window where it was last.
              //  GUIUtils.recallPosition(formSingleton_);
           // }
            //formSingleton_.setVisible(true);
            this.setLocation(32, 32);

        }

        public void control(){

        }


    /**
     * Shows the form, which is a singleton.
     * @param core MMCore
     * @param app  ScritpInterface
     * @return singleton instance
     */

//     public static void main(String[] args) {
//   // public static void main() {
//
//            SwingUtilities.invokeLater(new Runnable(){
//                public void run() {
//                    try {
//                        UIManager.setLookAndFeel("com.jtattoo.plaf.smart.SmartLookAndFeel");
//                        new PluginGui();
//                    } catch (ClassNotFoundException e) {
//                        e.printStackTrace();
//                    } catch (InstantiationException e) {
//                        e.printStackTrace();
//                    } catch (IllegalAccessException e) {
//                        e.printStackTrace();
//                    } catch (UnsupportedLookAndFeelException e) {
//                        e.printStackTrace();
//                    }
//
//
//                }
//            });
//        }
    }