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
public class XComparator implements Comparator<FOV>{

    @Override
    public int compare(FOV o1, FOV o2) {
        Double x1 = o1.getFOV().getX();
        Double x2 = o2.getFOV().getX();
        return x1.compareTo(x2);
    }    
}
