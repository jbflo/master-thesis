/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.micromanager.rapp.MultiFOV;


import java.util.logging.Level;
import java.util.logging.Logger;
import loci.formats.CoreMetadata;
import mmcorej.CMMCore;
import org.micromanager.MMStudio;

/**
 *
 * @author Frederik
 */
public class VariousClasses {
    MMStudio gui_;
    CMMCore core_;
    CoreMetadata cm;
    private BLframe frame_;
    public boolean bleechingComp = false;
    private static final VariousClasses fINSTANCE =  new VariousClasses();
    
    public static VariousClasses getInstance() {
       return fINSTANCE;
    }
    
    public VariousClasses() {
        gui_ = MMStudio.getInstance();
        core_ = gui_.getCore();
        frame_= BLframe.getInstance();
    }
    
    public void setLive(Boolean on){
        if (on == true){
            Object img = null;
            try {
                core_.snapImage();
                img = core_.getImage();
            } catch (Exception ex) {
                Logger.getLogger(BLframe.class.getName()).log(Level.SEVERE, null, ex);
            }
            gui_.displayImage(img);
        }  
       }
}

