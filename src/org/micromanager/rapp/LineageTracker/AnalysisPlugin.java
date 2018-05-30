package org.micromanager.rapp.LineageTracker;

import GUI.Image5DWithOverlay;
import ij.gui.Roi;
import java.util.List;
import java.util.Map;
import util.Cell;
import util.ExptData;

public abstract class AnalysisPlugin {

    protected ExptData exp;
    protected List<Cell> cells;
    protected Image5DWithOverlay screen;
    protected Map<Integer, List<Roi>> roiLists;

    /**
     * Called when the analysis plugins are loaded, prior to calling any of the analyse methods.
     *
     * @param exp
     * @param cells
     * @param screen
     * @param roiLists
     */
    public void setup(ExptData exp, List<Cell> cells, Image5DWithOverlay screen, Map<Integer, List<Roi>> roiLists) {
        this.exp = exp;
        this.cells = cells;
        this.screen = screen;
        this.roiLists = roiLists;
    }

    // Obtain information from the user, possibly via dialog box.
    public abstract void setup();

    // Called when the button is clicked in the control panel
    public abstract void analyze(Cell currentCell);

    // Called every time a cell is clicked on in the window
    public abstract void cellClicked(Cell currentCell);

    // Returns the name which appears in the menu, or null to disable the plugin
    public abstract String getName();
}
    A simple plugin to count the number of cells in each frame needs to import and extend the AnalysisPlugin class:

        import analysers.AnalysisPlugin;
        import ij.IJ;
        import util.Cell;

/**
 * @version March 21, 2011
 * @author Mike Downey
 */
public class CountCells extends AnalysisPlugin {

    int[] cellsInFrame;

    /**
     * Called when the 'Setup Analysis' button is clicked
     */
    @Override
    public void setup() {
        IJ.showMessage("Displays numbers of cells in each frame");
    }

    @Override
    public void analyze(Cell currentCell) {
        countCells();
        IJ.log("Frame, Cells");
        for (int i = 0; i < cellsInFrame.length; i++) {
            IJ.log(Integer.toString(i + 1) + " , " + cellsInFrame[i]);
        }
    }

    private void countCells() {
        cellsInFrame = new int[exp.getFrames()];
        for (Cell c : cells) {
            cellsInFrame[c.getFrame() - 1]++;
        }
    }

    @Override
    public void cellClicked(Cell currentCell) {
        int frame = currentCell.getFrame();
        countCells();
        IJ.log("Frame " + frame + " contains " + cellsInFrame[frame - 1] + " cells");
    }

    @Override
    public String getName() {
        return "Count Cells";
    }
}
    A plugin to display the Cell ID alongside each cell in the frame. This demonstrates use of the Image5DWithOverlay class which handles the screen output:

        import analysers.AnalysisPlugin;
        import ij.gui.GenericDialog;
        import java.awt.Color;
        import util.Cell;

/**
 * Plot the cell number alongside each cell
 *
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
