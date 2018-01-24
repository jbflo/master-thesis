package org.micromanager.rapp;

public class ThreadClass  implements Runnable{
    //private RappGui frame_;
    private RappGui rappController_ref;

    public  ThreadClass(RappGui frame){
        rappController_ref = frame;
    }

    @Override
    public void run() {
        // rappController_ref = new RappController();
        rappController_ref.liveDisplayThread();
    }
}
