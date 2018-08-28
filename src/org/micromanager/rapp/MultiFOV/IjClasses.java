/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.micromanager.rapp.MultiFOV;


import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.plugin.Duplicator;
import ij.plugin.filter.Analyzer;
import ij.plugin.frame.RoiManager;
import java.util.ArrayList;
import loci.formats.CoreMetadata;
import loci.formats.IFormatWriter;
import loci.formats.ome.OMEXMLMetadata;
import mmcorej.CMMCore;
import org.micromanager.MMStudio;

/**
 *
 * @author Frederik
 */
public class IjClasses {
    MMStudio gui_;
    CMMCore core_;
    CoreMetadata cm;
    private BLframe frame_;
    private static final IjClasses fINSTANCE =  new IjClasses();
    TableModel tableModel_;
    String pp;
    Acquisition acq_ = new Acquisition();
    
    public static IjClasses getInstance() {
       return fINSTANCE;
    }
    
    public IjClasses() {
        gui_ = MMStudio.getInstance();
        core_ = gui_.getCore();
        frame_= BLframe.getInstance();
        //
    }

    public void getROIs() {
        ImagePlus image = IJ.getImage();
        ij.measure.Calibration cal = image.getCalibration();
        String unit = cal.getUnit().toString();
        double width = cal.pixelWidth;
        double height = cal.pixelHeight;
        RoiManager rm = RoiManager.getInstance();
        int roiCount = rm.getCount();
        Roi[] roiArray = rm.getRoisAsArray();
        ArrayList xRoiPosArray = new ArrayList();
        ArrayList yRoiPosArray = new ArrayList();
        ArrayList widthRoiPosArray = new ArrayList();
        ArrayList heightRoiPosArray = new ArrayList();
        ArrayList xcRoiPosArray = new ArrayList();
        ArrayList ycRoiPosArray = new ArrayList();
        for (int i=0; i<roiCount; i++){
            xRoiPosArray.add(roiArray[i].getXBase());
            yRoiPosArray.add(roiArray[i].getYBase());
            widthRoiPosArray.add(roiArray[i].getFloatWidth());
            heightRoiPosArray.add(roiArray[i].getFloatHeight());
            xcRoiPosArray.add(roiArray[i].getXBase()+Math.round(roiArray[i].getFloatWidth()/2));
            ycRoiPosArray.add(roiArray[i].getYBase()+Math.round(roiArray[i].getFloatHeight()/2));
        }
        System.out.println(xcRoiPosArray);
        System.out.println(ycRoiPosArray);
//        tableModel_.addWholeData(xcRoiPosArray);
        
    }
    
    private void setControlDefault(){
        
    
    }

    public void findCells() {
        ImagePlus impOrg = IJ.getImage();
        ImagePlus imp = new Duplicator().run(impOrg);
        impOrg.setTitle("Original");
        imp.setTitle("Working on ...");
        imp.show();
        imp.updateAndRepaintWindow();
        IJ.run(imp, "Gaussian Blur...", "sigma=5");
        IJ.run(imp, "Find Maxima...", "noise=20 output=List exclude");
        ij.measure.ResultsTable resTab = Analyzer.getResultsTable();
        int resCount = resTab.getCounter();
        ArrayList xTab = new ArrayList();
        ArrayList yTab = new ArrayList();
        int[] x = new int[resCount];
        int[] y = new int[resCount];
        Roi[] r = new Roi[resCount];
        for (int i=0; i<resCount; i++){
            int xx =(int) resTab.getValueAsDouble(0, i);
            int yy =(int) resTab.getValueAsDouble(1, i);
            xTab.add(xx);
            yTab.add(yy);
            x[i]=xx;
            y[i]=yy;
            //r[i]= new Roi(xx,yy,10,10);
            imp.setRoi(new Roi(xx,yy,10,10));
            IJ.run("Draw"); 
        }
        //imp.setRoi(r);
        
        
        System.out.println(xTab);
        System.out.println(yTab);
       
        imp.updateAndRepaintWindow();

    }

    public void makeMultiTiff() {
        pp = "C://PostDocKnop//blTests//t1";
        int num = 4; // number of channels
        ArrayList<String> listIn = new ArrayList();
        listIn.add("CFP");
        listIn.add("GFP");
        listIn.add("RFP");
        listIn.add("WF");
        int count=0;
        try{
            if (gui_.isLiveModeOn() | gui_.isAcquisitionRunning()){
                gui_.enableLiveMode(false);
                gui_.closeAllAcquisitions();
            }
            OMEXMLMetadata m = acq_.setBasicMetadata(listIn);
            String millis = String.valueOf(System.currentTimeMillis());
            IFormatWriter writer = acq_.generateWriter(pp+millis+".ome.tiff", m);
            for (String listItem : listIn) {
                count=count+1;
                // read properties JB 
                try{
                    // change properties here JB 
                    System.out.println("Set filters here!");
                } catch(Exception e) {
                    System.out.println(e.getMessage());
                    System.out.println("Could not set filters!");
                
                }
                
                long dim = core_.getImageWidth() * core_.getImageHeight();
                int[] accImg = new int[(int)dim];
                
                    core_.snapImage();
                    Object img = core_.getImage();
                    // Display acquired images while the acquisition goes on
                    gui_.displayImage(img);
                    byte[] pixBB = (byte[]) img;

                writer.saveBytes(listIn.indexOf(listItem), (byte[]) pixBB);  
                
            }
            writer.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        //acq_.snapFLIMImage(pp, 4);
    }
   
    public void makeMultiTiffImageJ(){
        // JB: needs loop to read all images or display directly after acquisition
        IJ.open("C:\\PostDocKnop\\blTests\\image_CFP.tif");
        IJ.open("C:\\PostDocKnop\\blTests\\image_GFP.tif");
        IJ.open("C:\\PostDocKnop\\blTests\\image_RFP.tif");
        IJ.open("C:\\PostDocKnop\\blTests\\image_YFP.tif");
        // convert all open images to stack
        IJ.run("Images to Stack", "name=Stack title=[] use");
        // JB: save at specific path and name
        IJ.saveAs("Tiff", "C:\\PostDocKnop\\blTests\\Image_Stack_auto.tif");
        // close all open files
        IJ.run("Close All");
    }
            
    
}
