package org.micromanager.rapp.utils;


import org.micromanager.rapp.RappGui;

import java.awt.FlowLayout;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

public class FileDialog  {


    public FileDialog() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) { }

    }


    public String ChooseFileDialog() {

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Choose the needed file ");
        File fileToSave = null;
        int userSelection = fileChooser.showSaveDialog(RappGui.getInstance());
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            fileToSave = fileChooser.getSelectedFile();
            System.out.println("Save as file: " + fileToSave.getAbsolutePath());
        }

        return fileToSave.getAbsolutePath();
    }
    public String xmlFileChooserDialog() {

        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter xmlfilter = new FileNameExtensionFilter(
                "xml files (*.xml)", "xml");

        fileChooser.setDialogTitle("Choose the needed file ");
        fileChooser.setFileFilter(xmlfilter);
        File fileToSave = null;
        int userSelection = fileChooser.showSaveDialog(RappGui.getInstance());
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            fileToSave = fileChooser.getSelectedFile();
            System.out.println("Save as file: " + fileToSave.getAbsolutePath());
            return fileToSave.getAbsolutePath();
        }

        return null;
    }

    public String SaveFileDialog() {

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Specify a file to save");
        File fileToSave = null;
        int userSelection = fileChooser.showSaveDialog(RappGui.getInstance());
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            fileToSave = fileChooser.getSelectedFile();
            System.out.println("Save as file: " + fileToSave.getAbsolutePath());
            return fileToSave.getAbsolutePath();
        }

        return null;
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
            return dirToSave.getAbsolutePath();
        }

        return null;
    }

}
