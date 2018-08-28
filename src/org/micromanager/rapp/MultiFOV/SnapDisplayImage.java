/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.micromanager.rapp.MultiFOV;


/**
 *
 * @author Frederik
 */

public class SnapDisplayImage implements Runnable {
    private BLframe frame;
    
    public SnapDisplayImage(BLframe frame_){
        // initialize new instance of HCAFLIMPluginFrame
        frame = frame_;
    }

    @Override
    public void run() {
        frame.liveDisplayThread();
        }
    
}