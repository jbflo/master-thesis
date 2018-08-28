/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.micromanager.rapp.MultiFOV;


import java.util.Comparator;

/**
 *
 * @author Frederik
 */
public class WellComparator implements Comparator<FOV>{

    @Override
    public int compare(FOV o1, FOV o2) {
        String x1 = o1.getFOV().getWell();
        String x2 = o2.getFOV().getWell();
        return x1.compareTo(x2);
    }    
}
