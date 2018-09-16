/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.micromanager.rapp.MultiFOV;


import java.util.ArrayList;
import java.util.Collections;
import mmcorej.CMMCore;

/**
 *
 * @author Frederik
 */
public class xyzFunctions {

    private FOV_GUI parent_;
    private CMMCore core_;

    static FOV_Controller FOV_control;

    private static final xyzFunctions fINSTANCE =  new xyzFunctions();
    public static int genMode = 0;
    
    public xyzFunctions() {
        FOV_control = FOV_control.getInstance();
    }
    
    public static xyzFunctions getInstance() {
            return fINSTANCE;
    }
    
    public static int getWellCol(String orWell) {
        int out = 0;
        int sWell = orWell.length();
        String WellC = null;
        String WellR = null;
        if (sWell == 2) {
            WellR = orWell.substring(0, 1);
            WellC = orWell.substring(1);
        } else if(sWell == 4){
            WellR = orWell.substring(0, 2);
            WellC = orWell.substring(2);
        } else if(sWell == 3){
            try {
               Integer.parseInt(orWell.substring(1));
                    WellR = orWell.substring(0, 1);
                    WellC = orWell.substring(1);
              } catch (NumberFormatException e) {
                    WellR = orWell.substring(0, 2);
                    WellC = orWell.substring(2); 
              }
        }
        out = Integer.parseInt(WellC);
        return out;
    }

    public static int getWellRow(String orWell) {
        int out = 0;
        int sWell = orWell.length();
        String WellC = null;
        String WellR = null;
        if (sWell == 2) {
            WellR = orWell.substring(0, 1);
            WellC = orWell.substring(1);
        } else if(sWell == 4){
            WellR = orWell.substring(0, 2);
            WellC = orWell.substring(2);
        } else if(sWell == 3){
            try {
               Integer.parseInt(orWell.substring(1));
                    WellR = orWell.substring(0, 1);
                    WellC = orWell.substring(1);
              } catch (NumberFormatException e) {
                    WellR = orWell.substring(0, 2);
                    WellC = orWell.substring(2); 
              }
        }
        out = convertAlphToNum(WellR);
        return out;
    }

    public static int convertAlphToNum(String WellR) {
        int out = 0;
        if (null != WellR)switch (WellR) {
            case "A":
                out = 1;
                break;
            case "B":
                out = 2;
                break;
            case "C":
                out = 3;
                break;
            case "D":
                out = 4;
                break;
            case "E":
                out = 5;
                break;
            case "F":
                out = 6;
                break;
            case "G":
                out = 7;
                break;
            case "H":
                out = 8;
                break;
            case "I":
                out = 9;
                break;
            case "J":
                out = 10;
                break;
            case "K":
                out = 11;
                break;
            case "L":
                out = 12;
                break;
            case "M":
                out = 13;
                break;
            case "N":
                out = 14;
                break;
            case "O":
                out = 15;
                break;
            case "P":
                out = 16;
                break;
            case "Q":
                out = 17;
                break;
            case "R":
                out = 18;
                break;
            case "S":
                out = 19;
                break;
            case "T":
                out = 20;
                break;
            case "U":
                out = 21;
                break;
            case "V":
                out = 22;
                break;
            case "W":
                out = 23;
                break;
            case "X":
                out = 24;
                break;
            case "Y":
                out = 25;
                break;
            case "Z":
                out = 26;
                break;
            case "AA":
                out = 27;
                break;
            case "AB":
                out = 28;
                break;
            case "AC":
                out = 29;
                break;
            case "AD":
                out = 30;
                break;
            case "AE":
                out = 31;
                break;
            case "AF":
                out = 32;
                break;
        }
        return out;
    }
    
    public static String convertNumToAlph(int WellR) {
        String out = null;
        if (WellR == 1) {
            out = "A";
        } else if (WellR == 2) {
            out = "B";
        } else if (WellR == 3) {
            out = "C";
        } else if (WellR == 4) {
            out = "D";
        } else if (WellR == 5) {
            out = "E";
        } else if (WellR == 6) {
            out = "F";
        } else if (WellR == 7) {
            out = "G";
        } else if (WellR == 8) {
            out = "H";
        } else if (WellR == 9) {
            out = "I";
        } else if (WellR == 10) {
            out = "J";
        } else if (WellR == 11) {
            out = "K";
        } else if (WellR == 12) {
            out = "L";
        } else if (WellR == 13) {
            out = "M";
        } else if (WellR == 14) {
            out = "N";
        } else if (WellR == 15) {
            out = "O";
        } else if (WellR == 16) {
            out = "P";
        } else if (WellR == 17) {
            out = "Q";
        } else if (WellR == 18) {
            out = "R";
        } else if (WellR == 19) {
            out = "S";
        } else if (WellR == 20) {
            out = "T";
        } else if (WellR == 21) {
            out = "U";
        } else if (WellR == 22) {
            out = "V";
        } else if (WellR == 23) {
            out = "W";
        } else if (WellR == 24) {
            out = "X";
        } else if (WellR == 25) {
            out = "Y";
        } else if (WellR == 26) {
            out = "Z";
        } else if (WellR == 27) {
            out = "AA";
        } else if (WellR == 28) {
            out = "AB";
        } else if (WellR == 29) {
            out = "AC";
        } else if (WellR == 30) {
            out = "AD";
        } else if (WellR == 31) {
            out = "AE";
        } else if (WellR == 32) {
            out = "AF";
        }
        return out;
    }
    
    public static ArrayList<FOV> concatLists(ArrayList<FOV> preFovs, ArrayList<FOV> fovs) {
        int length = fovs.size();
        for (int i = 0; i < length; i++) {
            if (!preFovs.contains(fovs.get(i))){
                preFovs.add(fovs.get(i));
            }
        }
        return preFovs;
    }

    
    public static ArrayList<FOV> sortList(ArrayList<FOV> fovs) {
//        System.out.println("Before sorting: ");
//            for (FOV sas : fovs){
//                System.out.println(sas.toString());
//            }
        Collections.sort(fovs, new XComparator());
        Collections.sort(fovs, new WellComparatorSen());
        return fovs;
    }
    
    public static ArrayList<FOV> generateFOVs(int dCol, int dRow, int startCol, int startRow, int genMode) {
        double xF = 0;
        double yF = 0;
        double wellSpaceX = FOV_control.getWellSpacingX();
        double wellSpaceY = FOV_control.getWellSpacingY();
        double wellOffX = FOV_control.getFirstWellOffX();
        double wellOffY = FOV_control.getFirstWellOffY();
        double FOVx = FOV_control.getFOVsizeX();
        double FOVy = FOV_control.getFOVsizeY();
        double wellX = FOV_control.getWellSizeX();
        double wellY = FOV_control.getWellSizeY();
        
        if (startCol+dCol>FOV_control.getColCount()){
            dCol=FOV_control.getColCount()-startCol+1;
        }
        if (startRow+dRow>FOV_control.getRowCount()){
            dRow=FOV_control.getRowCount()-startRow+1;
        }
        
        ArrayList<FOV> fovs = new ArrayList<FOV>();
        if(genMode==1){
            for (int i = 1; i <= dCol; i++){
                for (int ii = 1; ii <= dRow; ii++){
                    FOV FOVtoAdd = new FOV(0,0,0,"A1");
                    xF = wellOffX+(i-2+startCol)*wellSpaceX+0.5*wellX;
                    yF = wellOffY+(ii-2+startRow)*wellSpaceY+0.5*wellX;
                    FOVtoAdd.setX(xF);
                    FOVtoAdd.setY(yF);
                    String rr = xyzFunctions.convertNumToAlph(ii+startRow-1);
                    String cc = Integer.toString(i+startCol-1);
                    FOVtoAdd.setWell(rr.concat(cc));
                    fovs.add(FOVtoAdd);
                }
            }
            return fovs;
        } else{
            for (int i = 1; i <= dCol; i++){
                for (int ii = 1; ii <= dRow; ii++){
                    int countX = (int) (wellX/FOVx+0.5);
                    int countY = (int) (wellY/FOVy+0.5);
                    double newFOVx = countX*FOVx;
                    double newFOVy = countX*FOVy;
                    double diffX = newFOVx-FOVx;
                    double diffY = newFOVy-FOVy;

                    for (int iii = 1; iii <= countX; iii++){
                        for (int iiii = 1; iiii <= countY; iiii++){
                            FOV FOVtoAdd = new FOV(0,0,0,"A1");
                            xF = wellOffX+(i-2+startCol)*wellSpaceX+(wellX-diffX)*0.5+(iii-1)*FOVx;
                            yF = wellOffY+(ii-2+startRow)*wellSpaceY+(wellY-diffY)*0.5+(iiii-1)*FOVy;
                            FOVtoAdd.setX(xF);
                            FOVtoAdd.setY(yF);
                            String rr = xyzFunctions.convertNumToAlph(ii+startRow-1);
                            String cc = Integer.toString(i+startCol-1);
                            FOVtoAdd.setWell(rr.concat(cc));
                            fovs.add(FOVtoAdd);
                        }
                    }
                }
            }
            return fovs;
        }
    }
}
