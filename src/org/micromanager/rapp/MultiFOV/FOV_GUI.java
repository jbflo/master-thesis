package org.micromanager.rapp.MultiFOV;

import mmcorej.CMMCore;
import org.micromanager.rapp.RappGui;
import org.micromanager.rapp.SequenceAcquisitions.SeqAcqGui;
import org.micromanager.rapp.utils.AboutGui;

import javax.swing.*;
import java.awt.*;        // Using AWT's Graphics and Color
import java.awt.event.*;  // Using AWT's event classes and listener interfaces
import javax.swing.border.Border;

public class FOV_GUI extends JFrame {

    protected JPanel main_well_panel;
    FOV_Controller FOV_control;

    private JLabel title_lbl;
    private JButton about_btn ;
    private static RappGui parent_ = RappGui.getInstance();
    protected AboutGui  dlgAbout;

    private  CMMCore core_ = new CMMCore();


    // Constructor to setup the UI components and event handlers
    public FOV_GUI(RappGui parent, CMMCore core) {
        parent_ = parent;
        core_ = core;
        new FOV_GUI();  // Let the constructor do the job
    }

    public FOV_GUI() {

        FOV_control = FOV_Controller.getInstance();

        FOVTableModel FOVTableModel_ = new FOVTableModel(FOV_control);

        main_well_panel = new JPanel();
        main_well_panel.setLayout(null);
        main_well_panel.setBackground(Color.decode("#34495e"));



        title_lbl = new JLabel(" Interface for controlling Stage position ");
        title_lbl.setFont(new Font("Arial Black", Font.PLAIN, 20));
        title_lbl.setForeground(Color.decode("#dfe8f0"));
        title_lbl.setBounds(70, 25,500, 30);
        main_well_panel.add(title_lbl);

        wellPanel well_panel = new wellPanel(this);
        well_panel.setBounds(0, 70,590, 410);
        well_panel.setBackground(Color.decode("#edf3f3"));
        Border bd = BorderFactory.createEmptyBorder(0,10, 0, 0);
        main_well_panel.add(well_panel);


        posPanel xyPos_panel = new posPanel(this, core_);
        xyPos_panel.setBackground(Color.decode("#edf3f3"));

        about_btn = new JButton();
        about_btn.setText("About...");
        about_btn.setBounds(20, 485, 120, 30);
        main_well_panel.add(about_btn);
        about_btn.addActionListener(evt->{

//            String msg = ("Cell Killing Interface\r\n\r\n" +
//                    "From The KnopLab (ZMBH) .\r\n" +
//                    "We present a tool for an automation position of the camera stage setup capable\n" +
//                    "of ......... \r\n");
//
//            dlgAbout = new AboutGui(parent_, msg);
//            dlgAbout.setVisible(true);
           // FOVTableModel_.getWholeData();
        });

        JSplitPane splitPaneBody = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, main_well_panel, xyPos_panel);
        splitPaneBody.setDividerLocation(590);
        this.add(splitPaneBody,BorderLayout.CENTER);

        //setDefaultCloseOperation(0);
        setTitle("Set Multi");
        setSize(920, 550);
        setResizable(false);
        setLocationRelativeTo(null);  // center the application window
        setVisible(true);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                SeqAcqGui.rootField_xmlWellFile.setText(" ");
            }
        });

    }

    // The entry main() method
    public static void main(String[] args) {
        // Run GUI codes in the Event-Dispatching thread for thread safety
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
              //  new FOV_GUI(parent_, core_);  // Let the constructor do the job
            }
        });
    }


}

