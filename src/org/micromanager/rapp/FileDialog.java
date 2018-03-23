package org.micromanager.rapp;


import java.awt.FlowLayout;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.UIManager;

public class FileDialog  {


    public FileDialog() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) { }

    }



    public String ChooseFileDialog() {

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Specify a file to save");
        File fileToSave = null;
        int userSelection = fileChooser.showSaveDialog(RappGui.getInstance());
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            fileToSave = fileChooser.getSelectedFile();
            System.out.println("Save as file: " + fileToSave.getAbsolutePath());
        }

        return fileToSave.getAbsolutePath();
    }
    public String ChooseDirectoryDialog() {

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Specify a file to save");
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        File dirToSave = null;
        int userSelection = fileChooser.showSaveDialog(RappGui.getInstance());
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            dirToSave = fileChooser.getSelectedFile();
            System.out.println("Save as file: " + dirToSave.getAbsolutePath());
        }
        return dirToSave.getAbsolutePath();

    }

}
