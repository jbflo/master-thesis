package org.micromanager.rapp.LineageTracker;

import ij.gui.GenericDialog;
import java.awt.Color;
import util.Cell;

/**
 * Plot the cell number alongside each cell
 * @version 04-May-2011
 * @author Mike Downey
 */
public class NumberCells extends AnalysisPlugin {

    double fontSize=12;

    @Override
    public void setup() {
        GenericDialog gd = new GenericDialog("Select Text Size");
        gd.addNumericField("Size in Pixels", fontSize, 0);
        gd.showDialog();
        if(gd.wasCanceled())
            return;
        if(gd.wasOKed()){
            fontSize = gd.getNextNumber();
        }
    }

    @Override
    public void analyze(Cell currentCell) {
        for(Cell c: cells)
            cellClicked(c);
    }

    /**
     * Plots the cell number alongside the cell.
     *
     * @param currentCell
     */
    @Override
    public void cellClicked(Cell currentCell) {
        if(screen!=null){
            screen.plotNumber(currentCell.getFrame(), currentCell.getX(), currentCell.getY(),
                    currentCell.getCellID(), fontSize/10, Color.white, false);
        }
    }

    @Override
    public String getName() {
        return("Draw Cell Numbers");
    }

}



    Details of the Cell & Image5DWithOverlay classes will be published soon.

        Tracking Plugins
        Tracking or lineage construction plugins need to extend the AbstractTracker class. This provides 2 abstract methods and several overrideable methods:

public abstract List<Cell> run() throws IOException;

public abstract String getName();

// Override this (to return null) in methods which do not use parameters.
public TrackingParameters getTrackingParameters() {
        return tp;
        }

// If desired, the overriding method can print the tracking parameters.
public String getDetails() {
        return "";
        }

/**
 * Passed a File array which represents available tracking files. Returns a boolean
 * stating whether it is possible to run the tracking algorithm. Any plugins
 * which depend on loading a file instead of running tracking will need to
 * examine the files to see if any are suitable.
 *
 * @param f
 * @return whether it is possible to run/load the selected tracking
 */
public boolean trackable(File[] filesinDir) {
        return true;
        }