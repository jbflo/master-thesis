package org.micromanager.rapp;

import org.micromanager.utils.MMScriptException;

public class ThreadClass  implements Runnable{
    //private RappGui frame_;
    private RappGui rappController_ref;

    public  ThreadClass(RappGui frame){
        rappController_ref = frame;
    }

    @Override
    public void run() {
        // rappController_ref = new RappController();
        try {
            rappController_ref.liveDisplayThread();
        } catch (MMScriptException e) {
            e.printStackTrace();
        }
    }
}
