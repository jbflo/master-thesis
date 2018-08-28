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
    public  AboutGui(RappGui parent, JTextPane msg) {
        setModal(true);
        //+ parent.getVersion()
        setTitle("About Rapp UGA-42 Control : Interface for controlling microscope and laser system ");
        setBounds(200, 200, 462, 273);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(new BorderLayout(0, 0));
        {
            //JTextPane ModuleCopyright = new JTextPane();
          //  ModuleCopyright.setEditable(false);
//            ModuleCopyright.setText("Cell Killing Interface\r\n\r\n" +
//                    "From The KnopLab (ZMBH) .\r\n" +
//                    "We present a tool for an automation of a fluorescence microscopy setup capable\n" +
//                    "of selective cell isolation based on UV lasers. \r\n"+
//
//                    "THIS SOFTWARE IS PROVIDED IN THE HOPE THAT IT MAY BE USEFUL, WITHOUT ANY REPRESENTATIONS OR WARRANTIES, INCLUDING WITHOUT LIMITATION THE WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT SHALL AUTHOR BE LIABLE FOR INDIRECT, EXEMPLARY, PUNITIVE, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING FROM USE OF THIS SOFTWARE, REGARDLESS OF THE FORM OF ACTION, AND WHETHER OR NOT THE AUTHOR HAS BEEN INFORMED OF, OR OTHERWISE MIGHT HAVE ANTICIPATED, THE POSSIBILITY OF SUCH DAMAGES.\r\n\r\n");
            contentPanel.add(msg);
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
