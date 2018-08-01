package org.micromanager.rapp.utils;


import org.micromanager.rapp.RappGui;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;

public class FileDialog  {


    public FileDialog() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) { }

    }


    public String ChooseFileDialog(String title) {

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(title);
        File choose_file;
        int userSelection = fileChooser.showSaveDialog(RappGui.getInstance());
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            choose_file = fileChooser.getSelectedFile();
            System.out.println("Save as file: " + choose_file.getAbsolutePath());
            return choose_file.getAbsolutePath();
        }
        return null ;

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
        fileChooser.setDialogTitle("Specify Directory to save the file");
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
        fileChooser.setDialogTitle("Specify Directory to save the file");
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        File dirToSave = null;
        int userSelection = fileChooser.showSaveDialog(RappGui.getInstance());
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            dirToSave = fileChooser.getSelectedFile();
            System.out.println("Save as file: " + dirToSave.getAbsolutePath()+"\\");
            return dirToSave.getAbsolutePath()+"\\";
        }

        return null;
    }

}
