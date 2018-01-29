package org.micromanager.rapp;

import org.micromanager.utils.MMScriptException;

public class ThreadClass  implements Runnable{
    //private RappGui frame_;
    private RappGui rappGui_ref;

    public  ThreadClass(RappGui frame){
        rappGui_ref = frame;
    }

    @Override
    public void run() {
        // rappController_ref = new RappController();
        try {
            rappGui_ref.liveDisplayThread();
        } catch (MMScriptException e) {
            e.printStackTrace();
        }
    }
}
