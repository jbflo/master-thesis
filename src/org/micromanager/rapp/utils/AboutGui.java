package org.micromanager.rapp.utils;


import org.micromanager.rapp.RappGui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextPane;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class AboutGui extends JDialog {
    private static final long serialVersionUID = 1L;

    public static JPanel contentPanel = new JPanel();

    /**
     * Create the dialog.
     */
    public  AboutGui(RappGui parent, String msg) {
        setModal(true);
        //+ parent.getVersion()
        setTitle("About Rapp UGA-42 Control : Interface for controlling microscope and laser system ");
        setBounds(200, 200, 462, 273);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(new BorderLayout(0, 0));
        {
            JTextPane ModuleCopyright = new JTextPane();
            ModuleCopyright.setEditable(false);
            ModuleCopyright.setText( msg);
            contentPanel.add(ModuleCopyright);
        }
        {
            JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            {
                JButton okButton = new JButton("OK");
                okButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        dispose();
                    }
                });
                okButton.setActionCommand("OK");
                buttonPane.add(okButton);
                getRootPane().setDefaultButton(okButton);
            }
        }
    }

}
