/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.micromanager.rapp.MultiFOV;

/**
 *
 * @author dk1109
 */
public class FOV implements Comparable<FOV> {

    double x_;
    double y_;
    double z_;
    double width_ = 200; // in um ffor 40x, ORCA
    double height_ = 200;   //40x obj
    
    String well_;
    FOV_Controller FOVclass_;
    // initialise with nonsense - remove entirely?
//    FOV(){
//        x_ = 0;
//        y_ = 0;
//        z_ = 0;
//        well_ = "Z99";
//    }
    /**
     * Initialise FOV with all fields
     *
     * @param x
     * @param y
     * @param z
     * @param well
     */
    public FOV(double x, double y, double z, String well) {
        x_ = x;
        y_ = y;
        z_ = z;
        well_ = well;
    }


//    /**
//     * Initialise a FOV in the centre of a given well, given plate properties.
//     *
//     * @param well
//     * @param pp
//     * @param z
//     */

    public FOV getFOV() {
        return this;
    }

    public double getX() {
        return x_;
    }

    public double getY() {
        return y_;
    }

    public double getZ() {
        //z_=(double) round(z_,2);
        return z_;
    }

    public String getWell() {
        return well_;
    }

    public void setX(double x) {
        x_ = x;
    }

    public void setY(double y) {
        y_ = y;
    }

    public void setZ(double z) {
        z_ = z;
    }

    public void setWell(String well) {
        well_ = well;
    }


    public double getWidth_() {
        double newWidth=(width_*40); //Changed to x var_relay, not divide...
        return newWidth;
    }

    public double getHeight_() {
        double newheight=(height_*40); //Changed to x var_relay, not divide...
        return newheight;
    }

    public void setWidth_(double width_) {
        this.width_ = width_;
    }

    public void setHeight_(double height_) {
        this.height_ = height_;
    }
      
    
   

    // Override compareTo so that FOV.sort orders by well value
    @Override
    public int compareTo(FOV fov) {
        final int GREATER = 1;
        final int LESS = -1;
        final int EQUAL = 0;

        if (this == fov) {
            return EQUAL;
        }

        String[] well = new String[2];
        String[] wellLetter = new String[2];
        int[] wellNumber = new int[2];
        int[] letterIndex = new int[2];
        well[0] = this.getWell();
        well[1] = fov.getWell();

        for (int ind = 0; ind < 2; ind++) {

            int i = 0;
            while (!Character.isDigit(well[ind].charAt(i))) {
                i++;
            }

            wellLetter[ind] = well[ind].substring(0, i);
            wellNumber[ind] = Integer.parseInt(well[ind].substring(i, well[ind].length()));
            for (int k = 0; k < i; k++) {
                letterIndex[ind] += (int) well[ind].charAt(k) - 64;
            }
        }

        if  (letterIndex[0] > letterIndex[1])
            return GREATER;
        if (letterIndex[0] < letterIndex[1])
            return LESS;
        if (letterIndex[0] == letterIndex[1]){
            if (wellNumber[0] > wellNumber[1])
                return GREATER;
            if (wellNumber[0] < wellNumber[1])
                    return LESS;
        }

        return EQUAL;
    }
    
    // Override equals so that FOV.contains checks LOGICAL equality, not 
    // reference equality; and ONLY for xy equality
    // http://users.csc.calpoly.edu/~kmammen/documents/java/howToOverrideEquals.html
    @Override
    public boolean equals(Object other){
        if (other == null)
            return false;
        
        if (this.getClass() != other.getClass())
            return false;
       
        double thisX = this.getX();
        double thisY = this.getY();
        double otherX = ((FOV) other).getX();
        double otherY = ((FOV) other).getY();
        
        if (thisX == otherX & thisY == otherY){
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + (int) (Double.doubleToLongBits(this.x_) ^ (Double.doubleToLongBits(this.x_) >>> 32));
        hash = 79 * hash + (int) (Double.doubleToLongBits(this.y_) ^ (Double.doubleToLongBits(this.y_) >>> 32));
        return hash;
    }
    
    @Override
    public String toString(){
        return "FOV: Well = " + this.well_ + ", x = " + this.x_ 
                + ", y = " + this.y_ + ", z = " + this.z_;
    }
}
