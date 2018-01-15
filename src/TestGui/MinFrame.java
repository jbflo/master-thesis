package TestGui;

/*
 * Copyright 2005 MH-Software-Entwicklung. All rights reserved.
 * Use is subject to license terms.
 */

//package com.jtattoo.demo.app;



import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

public class MinFrame extends JFrame {

    private static final LayoutManager grid = new GridLayout(0,1);

    public MinFrame() {
        super("Laser Control");

        // setup menu
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");
        //menu.setMnemonic("F");
        JMenuItem menuItem = new JMenuItem("Exit");
        //menuItem.setMnemonic("x");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, KeyEvent.ALT_MASK));
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                System.exit(0);
            }
        });
        menu.add(menuItem);
        menuBar.add(menu);
        setJMenuBar(menuBar);

        // setup widgets  Menu ZOne
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 4, 4, 4));


        // We Create all the menu bar on the left
        JPanel LeftPanel = new JPanel();
               LeftPanel.setMaximumSize(new Dimension(2, 1));

        JLabel leftLabel = new JLabel(" Option Part");
            LeftPanel.add(leftLabel);

        JButton setupMenu = new JButton("Setup");
                setupMenu.setMaximumSize(new Dimension(1, 1));
        setupMenu.setMnemonic(KeyEvent.VK_E);
        LeftPanel.add(setupMenu);


        JButton learnMenu = new JButton("Learning");
        learnMenu.setMnemonic(KeyEvent.VK_S);
        LeftPanel.add(learnMenu);

        JButton shootMenu = new JButton("Shoot");
        shootMenu.setMnemonic(KeyEvent.VK_H);
        LeftPanel.add(shootMenu);

        //LeftPanel.setLayout(grid);
        LeftPanel.setBackground(Color.blue);

        // Create a WestPanel  (Left)
        JScrollPane leftScrollPanel = new JScrollPane( LeftPanel); // We add all LeftMenubar contain in the WestPanel
        leftScrollPanel.setPreferredSize(new Dimension(2, 1));

        //////////////  We create the Screen interface here. zone d'affichage //////////////////////////////////
        JEditorPane editor = new JEditorPane("text/plain", "Hello World");
        JScrollPane centerPanel = new JScrollPane(editor);


        ///////////////////  We create A right Panel /////////////////////////////
        JPanel rightPanel = new JPanel();
      //  rightPanel.setMaximumSize(new Dimension(2, 1));

        JLabel rightLabel = new JLabel(" Control Part");
        LeftPanel.add(rightLabel);

        // Create a WestPanel  (Left)
        JScrollPane rightScrollPanel = new JScrollPane( rightPanel); // We add all LeftMenubar contain in the WestPanel
        rightScrollPanel.setPreferredSize(new Dimension(2, 1));


        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, leftScrollPanel,centerPanel);
        splitPane.setDividerLocation(148);
        contentPanel.add(splitPane, BorderLayout.CENTER);
        setContentPane(contentPanel);

        // add listeners
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        // show application
        setLocation(32, 32);
        setSize(1400, 300);
        show();
    } // end CTor MinFrame


   // public static void main(String[] args) {
    public static void main() {
        try {
            // select Look and Feel
          //  UIManager.setLookAndFeel("com.jtattoo.plaf.smart.SmartLookAndFeel");
            // start application
            new MinFrame();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    } // end main


} // end class MinFrame