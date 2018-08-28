
package org.micromanager.rapp.MultiFOV;

import javax.swing.JFrame;
import mmcorej.CMMCore;
import org.micromanager.MMStudio;
import org.micromanager.acquisition.AcquisitionEngine;
import org.micromanager.acquisition.AcquisitionWrapperEngine;
import org.micromanager.api.MMPlugin;
import org.micromanager.api.ScriptInterface;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Frederik
 */
public class BLrun implements MMPlugin {

    public static final String menuName = "BloodLust";
    public static final String tooltipDescription = "Kill it with fire!";
    
    public static JFrame frame_;
    static ScriptInterface si_;
    private CMMCore core_;
    private MMStudio gui_;
    private AcquisitionEngine acq_;
    
    public static AcquisitionWrapperEngine getAcquisitionWrapperEngine() {
        AcquisitionWrapperEngine engineWrapper = (AcquisitionWrapperEngine) MMStudio.getInstance().getAcquisitionEngine();
        return engineWrapper;
    }
    
  
    @Override
    public void dispose() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setApp(ScriptInterface si) {
      gui_ = (MMStudio) si;
      core_ = si.getMMCore();
      acq_ = gui_.getAcquisitionEngine();
      
      frame_ = new BLframe(core_);
      frame_.pack();
    }

    @Override
    public void show() {
        //gui_.showMessage("HELLO BloodLust!");
        if (frame_ == null) {
            frame_ = new BLframe(core_);
            gui_.addMMBackgroundListener(frame_);
//            frame_.setLocation(fa.controlFrame_.FrameXpos, fa.controlFrame_.FrameYpos);
        }
        frame_.setVisible(true);
//        toTest();
    }

    @Override
    public String getDescription() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getInfo() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getVersion() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getCopyright() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public void toTest(){
        int count=1;
        Object img = null;
        long start_time = System.currentTimeMillis();
        long wait_time = 1000;
        long end_time = start_time + wait_time;
        System.out.println("Start Test at:  " +start_time/1000);
        while (System.currentTimeMillis() < end_time) {
            try {
                core_.snapImage();
                img = core_.getImage();
                System.out.println(count);
                count++;
            } catch (Exception ex) {
                System.out.println("Error");
            }
        }
        System.out.println(count);
        long stop_time = System.currentTimeMillis();
        System.out.println("Stop Test at:  " + stop_time/1000);
        gui_.displayImage(img);
        
        
    }
    
}
