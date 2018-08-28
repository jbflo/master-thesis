package org.micromanager.rapp.MultiFOV;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */



import java.util.ArrayList;
import mmcorej.CMMCore;
import org.micromanager.MMStudio;
import loci.formats.ome.OMEXMLMetadata;
import loci.formats.services.OMEXMLServiceImpl;
import loci.common.services.ServiceException;
import loci.formats.ImageWriter;
import loci.common.DataTools;
import loci.formats.CoreMetadata;
import ome.xml.model.enums.DimensionOrder;
import ome.xml.model.enums.PixelType;
import ome.xml.model.primitives.PositiveInteger;
import ome.xml.model.primitives.PositiveFloat;
import ome.xml.model.primitives.NonNegativeInteger;
import java.io.IOException;
import com.quirkware.guid.PlatformIndependentGuidGen;
import static jdk.nashorn.internal.objects.NativeArray.length;
import ome.units.quantity.Length;
import loci.formats.FormatException;
import loci.formats.IFormatWriter;
//import mdbtools.dbengine.functions.Length;
import mmcorej.TaggedImage;
import org.micromanager.api.ImageCache;


/**
 *
 * @author dk1109
 */
public class Acquisition {
    MMStudio gui_;
    CMMCore core_;
    CoreMetadata cm;
    private BLframe frame_;
    public boolean bleechingComp=false;
    
    public Acquisition() {
        gui_ = MMStudio.getInstance();
        core_ = gui_.getCore();
        frame_= BLframe.getInstance();
    }

    public void snapFLIMImage(String path, ArrayList<String> listIn) {

        try{

            if (gui_.isLiveModeOn() | gui_.isAcquisitionRunning()){
                gui_.enableLiveMode(false);
                gui_.closeAllAcquisitions();
            }
            
            
            
            OMEXMLMetadata m = setBasicMetadata(listIn);
            IFormatWriter writer = generateWriter(path+".ome.tiff", m);
            for (String listItem : listIn) {
                // read properties JB 
                
                try{
                core_.wait(1000); // change prperties here JB 
                    
                } catch(Exception e) {
                    System.out.println(e.getMessage());
                    System.out.println("Could not set delay in Delay box!");
                
                }
                

                // EITHER
//                core_.snapImage();
                long dim = core_.getImageWidth() * core_.getImageHeight();
//                Object img = core_.getImage();
                int[] accImg = new int[(int)dim];
 //               for (int fr = 0; fr < sas.getFilters().getAccFrames(); fr++){
                    core_.snapImage();
                    Object img = core_.getImage();
                    // Display acquired images while the acquisition goes on
                    gui_.displayImage(img);
                    // this bit c.f. FrameAverager
                    if (core_.getBytesPerPixel() == 2){
                        short[] pixS = (short[]) img;
                        for (int j = 0; j < dim; j++) {
                            accImg[j] = (int) (accImg[j] + (int) (pixS[j] & 0xffff));
                        }
                    } else if (core_.getBytesPerPixel() == 1){
                        byte[] pixB = (byte[]) img;
                        for (int j = 0; j < dim; j++) {
                            accImg[j] = (int) (accImg[j] + (int) (pixB[j] & 0xff));
                        }
                    }

 //               }
                
                
                saveLayersToOMETiff(writer, accImg, listIn.indexOf(listItem));


            }
            // OR
//            saveAcqToOMETiff(writer, acq, delays.size());
            ////
            // clean up writer when finished...
            writer.close();
       } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }
    
    public void snapFLIMImage(String path, ArrayList<Integer> delays, String binning) {
        int binningD = Integer.parseInt(binning);
        try{

            if (gui_.isLiveModeOn() | gui_.isAcquisitionRunning()){
                gui_.enableLiveMode(false);
                gui_.closeAllAcquisitions();
            }
            
            
            
            OMEXMLMetadata m = setBasicMetadataBinning(delays, binningD);
            IFormatWriter writer = generateWriter(path+".ome.tiff", m);
            for (Integer delay : delays) {
                int del=delays.indexOf(delay);
                try{
                core_.setProperty("Delay box", "Delay (ps)", delay);
                } catch(Exception e) {
                    System.out.println(e.getMessage());
                    System.out.println("Could not set delay in Delay box!");
                
                }
                

                // EITHER
//                core_.snapImage();
                long dim = core_.getImageWidth() * core_.getImageHeight();
//                Object img = core_.getImage();
                int[] accImg = new int[(int)dim];
//                for (int fr = 0; fr < sas.getFilters().getAccFrames(); fr++){
//                    if (frame_.terminate != true){
//                       core_.snapImage();
//                    }
                    Object img = core_.getImage();
                    // Display acquired images while the acquisition goes on
                    gui_.displayImage(img);
                    // this bit c.f. FrameAverager
                    if (core_.getBytesPerPixel() == 2){
                        short[] pixS = (short[]) img;
                        for (int j = 0; j < dim; j++) {
                            accImg[j] = (int) (accImg[j] + (int) (pixS[j] & 0xffff));
                        }
                    } else if (core_.getBytesPerPixel() == 1){
                        byte[] pixB = (byte[]) img;
                        for (int j = 0; j < dim; j++) {
                            accImg[j] = (int) (accImg[j] + (int) (pixB[j] & 0xff));
                        }
                    }

 //               }
                if(binningD>1){
                    accImg=binningByte(accImg, binningD);
                }   
                
                saveLayersToOMETiff(writer, accImg, delays.indexOf(delay));

            }
 
            writer.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }
    
    private void saveAcqToOMETiff(IFormatWriter writer, String acq, int length)
            throws Exception {
        ImageCache imgCache = gui_.getAcquisitionImageCache(acq);
        for (int ind = 0; ind < length; ind++) {
            TaggedImage timg = imgCache.getImage(ind, 0, 0, 0); // returns null if no image exists at these coordinates
            Object img = timg.pix;
            if (img instanceof byte[]) {
                System.out.println("Img is in bytes");
                writer.saveBytes(ind, (byte[]) img);
            } else if (img instanceof short[]) {
//                byte[] bytes = DataTools.shortsToBytes((short[]) img, true);
                short[] img2 = (short[]) img;
                byte[] bytes = DataTools.shortsToBytes(img2, false);
                System.out.println("Img is short[], converting to bytes, i = " + ind);
//                writer.savePlane(ind,img);
                writer.saveBytes(ind, bytes);
            } else {
                System.out.println("I don't know what type img is!");
            }
        }

    }

    
    void saveLayersToOMETiff(IFormatWriter writer, Object img, int layer)
            throws Exception {
//        Object img = core_.getImage();
        if (img instanceof byte[]) {
            System.out.println("Img is in bytes");
            writer.saveBytes(layer, (byte[]) img);
        } else if (img instanceof short[]) {
            byte[] bytes = DataTools.shortsToBytes((short[]) img, true);
//            System.out.println("Img is short[], converting to bytes, i = " + layer);
            writer.saveBytes(layer, bytes);
        } else  if (img instanceof int[]){
            byte[] bytes = DataTools.intsToBytes((int[]) img, true);
            writer.saveBytes(layer, bytes);
        } else
        {
            System.out.println("I don't know what type img is!");
        }
    }

    IFormatWriter generateWriter(String path, OMEXMLMetadata m)
        throws FormatException, IOException {
        IFormatWriter writer = new ImageWriter().getWriter(path);
        writer.setWriteSequentially(true);
        writer.setMetadataRetrieve(m);
        writer.setCompression("LZW");

        writer.setId(path);

        return writer;
    }

    private PositiveFloat checkPixelPitch() {

        PositiveFloat pitch = new PositiveFloat(1.0);

        try {
            String binningStr = core_.getProperty(core_.getCameraDevice(), "Binning");
            float binning = 1;
            if (binningStr.equals("1")) {
                binning = 1;
            } else if (binningStr.equals("2")) {
                binning = 2;
            } else if (binningStr.equals("4")) {
                binning = 4;
            } else if (binningStr.equals("8")) {
                binning = 8;
            }

            pitch = new PositiveFloat(binning * 6.45);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return pitch;
    }
    

    OMEXMLMetadata setBasicMetadata(ArrayList<String> delays)
            throws ServiceException {

        OMEXMLServiceImpl serv = new OMEXMLServiceImpl();
        OMEXMLMetadata m = serv.createOMEXMLMetadata();

        int no_delays = delays.size();
        // delays must be parsed as strings for metadata
        String[] delArrayStr = new String[no_delays];
        for (int ind = 0; ind < no_delays; ind++) {
            delArrayStr[ind] = String.valueOf(delays.get(ind));
        }

        try {
            m.createRoot();
            m.setImageID("Image:0", 0);
            m.setPixelsID("Pixels:0", 0);
            m.setPixelsDimensionOrder(DimensionOrder.XYZCT, 0);
            m.setChannelID("Channel:0:0", 0, 0);
            m.setChannelSamplesPerPixel(new PositiveInteger(1), 0, 0);
            m.setPixelsBinDataBigEndian(Boolean.FALSE, 0, 0);
//            m.setPixelsType(PixelType.UINT8, 0);
//            m.setImageDescription(sas.toString(), 0);

            long bpp = core_.getBytesPerPixel();


                if (bpp == 1) {
                    m.setPixelsType(PixelType.UINT8, 0);
                }
                if (bpp == 2) {
                    m.setPixelsType(PixelType.UINT16, 0);
                }

            PositiveInteger w1 = new PositiveInteger((int) core_.getImageWidth());
            PositiveInteger h1 = new PositiveInteger((int) core_.getImageHeight());
            PositiveInteger g1 = new PositiveInteger(no_delays);

            m.setPixelsSizeX((w1), 0);
            m.setPixelsSizeY((h1), 0);
            m.setPixelsSizeZ(new PositiveInteger(1), 0);
            m.setPixelsSizeC(new PositiveInteger(1), 0);
            m.setPixelsSizeT(g1, 0);

            PositiveFloat pitch = checkPixelPitch();
            double pitchD = pitch.getValue();
            Length len = new Length(1,ome.units.UNITS.MICROM);
            m.setPixelsPhysicalSizeX(new Length(pitchD,ome.units.UNITS.MICROM),0);
            m.setPixelsPhysicalSizeY(new Length(pitchD,ome.units.UNITS.MICROM),0);
            m.setPixelsPhysicalSizeZ(new Length(1.0,ome.units.UNITS.MICROM), 0);

            PlatformIndependentGuidGen p = PlatformIndependentGuidGen.getInstance();

            for (int ii = 0; ii < no_delays; ii++) {

                m.setUUIDFileName(delArrayStr[ii], 0, ii);
                m.setUUIDValue(p.genNewGuid(), 0, ii);
                m.setTiffDataPlaneCount(new NonNegativeInteger(0), 0, ii);
                m.setTiffDataIFD(new NonNegativeInteger(0), 0, ii);
                m.setTiffDataFirstZ(new NonNegativeInteger(0), 0, ii);
                m.setTiffDataFirstC(new NonNegativeInteger(0), 0, ii);
                m.setTiffDataFirstT(new NonNegativeInteger(0), 0, ii);
                m.setPlaneTheC(new NonNegativeInteger(0), 0, ii);
                m.setPlaneTheZ(new NonNegativeInteger(0), 0, ii);
                m.setPlaneTheT(new NonNegativeInteger(ii), 0, ii);
                m.setTiffDataPlaneCount(new NonNegativeInteger(ii), 0, ii);
                System.out.println("done loop ind " + ii);
            }

            // deal FLIMfit issue loading single plane images with moduloAlongT
            if (no_delays > 2){ 
                CoreMetadata cm = new CoreMetadata();

                cm.moduloT.labels = delArrayStr;
                cm.moduloT.unit = "ps";
                cm.moduloT.typeDescription = "Gated";
                cm.moduloT.type = loci.formats.FormatTools.LIFETIME;
                serv.addModuloAlong(m, cm, 0);
                System.out.println("did addModulo");
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return m;
    }
    
    private int[] binningByte(int[] accImg, int binningD) {
      
         

//        accImg=new int[9000];
//        Arrays.fill(accImg, 12);
//        for(int aa=0;aa<8;aa++){
//            for(int bb=0;bb<8;bb++){
//                accImg[aa*8+bb]=bb;
//            }
//        }
//        
//        binningD=2;
        int width=(int) core_.getImageWidth();
        int height=(int) core_.getImageHeight();
//        int width=9;
//        int height=1000;
        int sizeAccImg=accImg.length;
        int[][] image=new int[width][height];
        double widthB= width/binningD;
        double heightB= height/binningD;
        int sizeAccImgB=accImg.length/binningD/binningD;
        int[] accImgB=new int[sizeAccImgB];
        //gui_.displayImage(accImg); 
        //disImag(accImg, width, height);
        int masterCount=0;
        for (int x=0 ; x<width ; x++){
            for (int y=0 ; y<height ; y++){
                //System.out.println("LineY: "+accImg[x*y]);
                //System.out.println("xValue: "+x+"   yValue: "+y+"   accImg: "+masterCount);
                int nn=width*x+x*y;
                image[x][y]=accImg[masterCount];
                masterCount=masterCount+1;
            }
        }
        int masterX=0;
        int masterY=0;
        int ChuckedPixelSum=0;
        for(int masterCountB=0; masterCountB<sizeAccImg/binningD/binningD; masterCountB++){
            for(int countBinX=0; countBinX<binningD; countBinX++){
                for(int countBinY=0; countBinY<binningD; countBinY++){
                    try{
                        accImgB[masterCountB]=accImgB[masterCountB]+image[countBinX+masterX*binningD][countBinY+masterY*binningD];
                        //System.out.println("accImgB: "+masterCountB+ "    value: "+accImgB[masterCountB]);
                    }catch (Exception e) {
//                        System.out.println(e.getMessage());
//                        System.out.println("Chucked pixel: "+masterCountB+ "    masterX: "+masterX+"    masterY: "+masterY);
//                        ChuckedPixelSum=ChuckedPixelSum+1;
                        accImgB[masterCountB]=0;
                    }
                }    
            }
        masterY=masterY+1;
        if(masterY==heightB){
        masterY=0;
        masterX=masterX+1;
        }
        }
        
        return accImgB;

        }
    
        private OMEXMLMetadata setBasicMetadataBinning(ArrayList<Integer> delays,int binningD)
            throws ServiceException {

        OMEXMLServiceImpl serv = new OMEXMLServiceImpl();
        OMEXMLMetadata m = serv.createOMEXMLMetadata();

        int no_delays = delays.size();
        // delays must be parsed as strings for metadata
        String[] delArrayStr = new String[no_delays];
        for (int ind = 0; ind < no_delays; ind++) {
            delArrayStr[ind] = String.valueOf(delays.get(ind));
        }

        try {
            m.createRoot();
            m.setImageID("Image:0", 0);
            m.setPixelsID("Pixels:0", 0);
            m.setPixelsDimensionOrder(DimensionOrder.XYZCT, 0);
            m.setChannelID("Channel:0:0", 0, 0);
            m.setChannelSamplesPerPixel(new PositiveInteger(1), 0, 0);
            m.setPixelsBinDataBigEndian(Boolean.FALSE, 0, 0);
//            m.setImageDescription(sas.toString(), 0);

            long bpp = core_.getBytesPerPixel();

//            if (sas.getFilters().getAccFrames() == 1){
//                if (bpp == 1) {
//                    m.setPixelsType(PixelType.UINT8, 0);
//                }
//                if (bpp == 2) {
//                    m.setPixelsType(PixelType.UINT16, 0);
//                }
//            }
//            else if (sas.getFilters().getAccFrames() > 0){ 
                System.out.println("setting pixeltype to 32");
                m.setPixelsType(PixelType.UINT32, 0);
                
//            }

            PositiveInteger w1 = new PositiveInteger((int) core_.getImageWidth()/binningD);
            PositiveInteger h1 = new PositiveInteger((int) core_.getImageHeight()/binningD);
            PositiveInteger g1 = new PositiveInteger(no_delays);

            m.setPixelsSizeX((w1), 0);
            m.setPixelsSizeY((h1), 0);
            m.setPixelsSizeZ(new PositiveInteger(1), 0);
            m.setPixelsSizeC(new PositiveInteger(1), 0);
            m.setPixelsSizeT(g1, 0);

            PositiveFloat pitch = checkPixelPitch();
            double pitchD = pitch.getValue()/binningD;
            Length len = new Length(1,ome.units.UNITS.MICROM);
            m.setPixelsPhysicalSizeX(new Length(pitchD,ome.units.UNITS.MICROM),0);
            m.setPixelsPhysicalSizeY(new Length(pitchD,ome.units.UNITS.MICROM),0);
            m.setPixelsPhysicalSizeZ(new Length(1.0,ome.units.UNITS.MICROM), 0);

            PlatformIndependentGuidGen p = PlatformIndependentGuidGen.getInstance();

            for (int ii = 0; ii < no_delays; ii++) {

                m.setUUIDFileName(delArrayStr[ii], 0, ii);
                m.setUUIDValue(p.genNewGuid(), 0, ii);
                m.setTiffDataPlaneCount(new NonNegativeInteger(0), 0, ii);
                m.setTiffDataIFD(new NonNegativeInteger(0), 0, ii);
                m.setTiffDataFirstZ(new NonNegativeInteger(0), 0, ii);
                m.setTiffDataFirstC(new NonNegativeInteger(0), 0, ii);
                m.setTiffDataFirstT(new NonNegativeInteger(0), 0, ii);
                m.setPlaneTheC(new NonNegativeInteger(0), 0, ii);
                m.setPlaneTheZ(new NonNegativeInteger(0), 0, ii);
                m.setPlaneTheT(new NonNegativeInteger(ii), 0, ii);
                m.setTiffDataPlaneCount(new NonNegativeInteger(ii), 0, ii);
                System.out.println("done loop ind " + ii);
            }

            // deal FLIMfit issue loading single plane images with moduloAlongT
            if (no_delays > 2){ 
                CoreMetadata cm = new CoreMetadata();

                cm.moduloT.labels = delArrayStr;
                cm.moduloT.unit = "ps";
                cm.moduloT.typeDescription = "Gated";
                cm.moduloT.type = loci.formats.FormatTools.LIFETIME;
                serv.addModuloAlong(m, cm, 0);
                System.out.println("did addModulo");
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return m;
    }
    
}
    
