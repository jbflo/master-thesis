package org.micromanager.rapp.utils;


import ij.ImagePlus;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.LUT;
import ij.process.ShortProcessor;
import java.awt.Color;
import java.awt.Point;
import java.awt.image.ColorModel;
import mmcorej.CMMCore;
import mmcorej.TaggedImage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.micromanager.rapp.utils.TaggedImageStorage;
import org.micromanager.rapp.acquisition.TaggedImageStorageDiskDefault;
//import org.micromanager.acquisition.TaggedImageStorageDiskDefault;
//import org.micromanager.api.TaggedImageStorage;
import org.micromanager.utils.MDUtils;
import org.micromanager.utils.MMException;
import org.micromanager.utils.ReportingUtils;

public class ImageUtils {
    private static Class<?> storageClass_ = TaggedImageStorageDiskDefault.class;

    public ImageUtils() {
    }

    public static int BppToImageType(long Bpp) {
        int BppInt = (int)Bpp;
        switch(BppInt) {
            case 1:
                return 0;
            case 2:
                return 1;
            case 3:
            default:
                return 0;
            case 4:
                return 4;
        }
    }

    public static int getImageProcessorType(ImageProcessor proc) {
        if (proc instanceof ByteProcessor) {
            return 0;
        } else if (proc instanceof ShortProcessor) {
            return 1;
        } else {
            return proc instanceof ColorProcessor ? 4 : -1;
        }
    }

    public static ImageProcessor makeProcessor(CMMCore core) {
        return makeProcessor(core, (Object)null);
    }

    public static ImageProcessor makeProcessor(CMMCore core, Object imgArray) {
        int w = (int)core.getImageWidth();
        int h = (int)core.getImageHeight();
        int Bpp = (int)core.getBytesPerPixel();
        byte type;
        switch(Bpp) {
            case 1:
                type = 0;
                break;
            case 2:
                type = 1;
                break;
            case 3:
            default:
                type = 0;
                break;
            case 4:
                type = 4;
        }

        return makeProcessor(type, w, h, imgArray);
    }

    public static ImageProcessor makeProcessor(int type, int w, int h, Object imgArray) {
        if (imgArray == null) {
            return makeProcessor(type, w, h);
        } else {
            switch(type) {
                case 0:
                    return new ByteProcessor(w, h, (byte[])((byte[])imgArray), (ColorModel)null);
                case 1:
                    return new ShortProcessor(w, h, (short[])((short[])imgArray), (ColorModel)null);
                case 2:
                    return new FloatProcessor(w, h, (float[])((float[])imgArray), (ColorModel)null);
                case 3:
                default:
                    return null;
                case 4:
                    if (imgArray instanceof byte[]) {
                        imgArray = convertRGB32BytesToInt((byte[])((byte[])imgArray));
                    }

                    return new ColorProcessor(w, h, (int[])((int[])imgArray));
            }
        }
    }

    public static ImageProcessor makeProcessor(TaggedImage taggedImage) {
        JSONObject tags = taggedImage.tags;

        try {
            return makeProcessor(MDUtils.getIJType(tags), MDUtils.getWidth(tags), MDUtils.getHeight(tags), taggedImage.pix);
        } catch (Exception var3) {
            ReportingUtils.logError(var3);
            return null;
        }
    }

    public static ImageProcessor makeProcessor(int type, int w, int h) {
        if (type == 0) {
            return new ByteProcessor(w, h);
        } else if (type == 1) {
            return new ShortProcessor(w, h);
        } else if (type == 2) {
            return new FloatProcessor(w, h);
        } else {
            return type == 4 ? new ColorProcessor(w, h) : null;
        }
    }

    public static ImageProcessor makeMonochromeProcessor(TaggedImage taggedImage) {
        try {
            Object processor;
            if (MDUtils.isRGB32(taggedImage)) {
                ColorProcessor colorProcessor = new ColorProcessor(MDUtils.getWidth(taggedImage.tags), MDUtils.getHeight(taggedImage.tags), convertRGB32BytesToInt((byte[])((byte[])taggedImage.pix)));
                processor = colorProcessor.convertToByteProcessor();
            } else {
                processor = makeProcessor(taggedImage);
            }

            return (ImageProcessor)processor;
        } catch (Exception var3) {
            ReportingUtils.logError(var3);
            return null;
        }
    }

    public static ImageProcessor subtractImageProcessors(ImageProcessor proc1, ImageProcessor proc2) throws MMException {
        if (proc1.getWidth() == proc2.getWidth() && proc1.getHeight() == proc2.getHeight()) {
            try {
                if (proc1 instanceof ByteProcessor && proc2 instanceof ByteProcessor) {
                    return subtractByteProcessors((ByteProcessor)proc1, (ByteProcessor)proc2);
                } else if (proc1 instanceof ShortProcessor && proc2 instanceof ShortProcessor) {
                    return subtractShortProcessors((ShortProcessor)proc1, (ShortProcessor)proc2);
                } else if (proc1 instanceof ShortProcessor && proc2 instanceof ByteProcessor) {
                    return subtractShortByteProcessors((ShortProcessor)proc1, (ByteProcessor)proc2);
                } else if (proc1 instanceof ShortProcessor && proc2 instanceof FloatProcessor) {
                    return subtractShortFloatProcessors((ShortProcessor)proc1, (FloatProcessor)proc2);
                } else if (proc1 instanceof FloatProcessor && proc2 instanceof ByteProcessor) {
                    return subtractFloatProcessors((FloatProcessor)proc1, (ByteProcessor)proc2);
                } else if (proc1 instanceof FloatProcessor && proc2 instanceof ShortProcessor) {
                    return subtractFloatProcessors((FloatProcessor)proc1, (ShortProcessor)proc2);
                } else if (proc1 instanceof FloatProcessor) {
                    return subtractFloatProcessors((FloatProcessor)proc1, (FloatProcessor)proc2);
                } else {
                    throw new MMException("Types of images to be subtracted were not compatible");
                }
            } catch (ClassCastException var3) {
                throw new MMException("Types of images to be subtracted were not compatible");
            }
        } else {
            throw new MMException("Error: Images are of unequal size");
        }
    }

    public static ImageProcessor subtractFloatProcessors(FloatProcessor proc1, ByteProcessor proc2) {
        return new FloatProcessor(proc1.getWidth(), proc1.getHeight(), subtractPixelArrays((float[])((float[])proc1.getPixels()), (byte[])((byte[])proc2.getPixels())), (ColorModel)null);
    }

    public static ImageProcessor subtractFloatProcessors(FloatProcessor proc1, ShortProcessor proc2) {
        return new FloatProcessor(proc1.getWidth(), proc1.getHeight(), subtractPixelArrays((float[])((float[])proc1.getPixels()), (short[])((short[])proc2.getPixels())), (ColorModel)null);
    }

    public static ImageProcessor subtractFloatProcessors(FloatProcessor proc1, FloatProcessor proc2) {
        return new FloatProcessor(proc1.getWidth(), proc1.getHeight(), subtractPixelArrays((float[])((float[])proc1.getPixels()), (float[])((float[])proc2.getPixels())), (ColorModel)null);
    }

    private static ByteProcessor subtractByteProcessors(ByteProcessor proc1, ByteProcessor proc2) {
        return new ByteProcessor(proc1.getWidth(), proc1.getHeight(), subtractPixelArrays((byte[])((byte[])proc1.getPixels()), (byte[])((byte[])proc2.getPixels())), (ColorModel)null);
    }

    private static ShortProcessor subtractShortByteProcessors(ShortProcessor proc1, ByteProcessor proc2) {
        return new ShortProcessor(proc1.getWidth(), proc1.getHeight(), subtractPixelArrays((short[])((short[])proc1.getPixels()), (byte[])((byte[])proc2.getPixels())), (ColorModel)null);
    }

    private static ShortProcessor subtractShortProcessors(ShortProcessor proc1, ShortProcessor proc2) {
        return new ShortProcessor(proc1.getWidth(), proc1.getHeight(), subtractPixelArrays((short[])((short[])proc1.getPixels()), (short[])((short[])proc2.getPixels())), (ColorModel)null);
    }

    private static ShortProcessor subtractShortFloatProcessors(ShortProcessor proc1, FloatProcessor proc2) {
        return new ShortProcessor(proc1.getWidth(), proc1.getHeight(), subtractPixelArrays((short[])((short[])proc1.getPixels()), (float[])((float[])proc2.getPixels())), (ColorModel)null);
    }

    public static byte[] subtractPixelArrays(byte[] array1, byte[] array2) {
        int l = array1.length;
        byte[] result = new byte[l];

        for(int i = 0; i < l; ++i) {
            result[i] = (byte)Math.max(0, unsignedValue(array1[i]) - unsignedValue(array2[i]));
        }

        return result;
    }

    public static short[] subtractPixelArrays(short[] array1, short[] array2) {
        int l = array1.length;
        short[] result = new short[l];

        for(int i = 0; i < l; ++i) {
            result[i] = (short)Math.max(0, unsignedValue(array1[i]) - unsignedValue(array2[i]));
        }

        return result;
    }

    public static short[] subtractPixelArrays(short[] array1, byte[] array2) {
        int l = array1.length;
        short[] result = new short[l];

        for(int i = 0; i < l; ++i) {
            result[i] = (short)Math.max(0, unsignedValue(array1[i]) - unsignedValue(array2[i]));
        }

        return result;
    }

    public static short[] subtractPixelArrays(short[] array1, float[] array2) {
        int l = array1.length;
        short[] result = new short[l];

        for(int i = 0; i < l; ++i) {
            result[i] = (short)Math.max(0, unsignedValue(array1[i]) - unsignedValue((short)((int)array2[i])));
        }

        return result;
    }

    public static float[] subtractPixelArrays(float[] array1, byte[] array2) {
        int l = array1.length;
        float[] result = new float[l];

        for(int i = 0; i < l; ++i) {
            result[i] = array1[i] - (float)unsignedValue(array2[i]);
        }

        return result;
    }

    public static float[] subtractPixelArrays(float[] array1, short[] array2) {
        int l = array1.length;
        float[] result = new float[l];

        for(int i = 0; i < l; ++i) {
            result[i] = array1[i] - (float)unsignedValue(array2[i]);
        }

        return result;
    }

    public static float[] subtractPixelArrays(float[] array1, float[] array2) {
        int l = array1.length;
        float[] result = new float[l];

        for(int i = 0; i < l; ++i) {
            result[i] = array1[i] - array2[i];
        }

        return result;
    }

    public static Point findMaxPixel(ImagePlus img) {
        ImageProcessor proc = img.getProcessor();
        float[] pix = (float[])((float[])proc.getPixels());
        int width = img.getWidth();
        double max = 0.0D;
        int imax = -1;

        int i;
        for(i = 0; i < pix.length; ++i) {
            if ((double)pix[i] > max) {
                max = (double)pix[i];
                imax = i;
            }
        }

        i = imax / width;
        int x = imax % width;
        return new Point(x, i);
    }

    public static Point findMaxPixel(ImageProcessor proc) {
        int width = proc.getWidth();
        int imax = findArrayMax(proc.getPixels());
        int y = imax / width;
        int x = imax % width;
        return new Point(x, y);
    }

    public static byte[] get8BitData(Object bytesAsObject) {
        return (byte[])((byte[])bytesAsObject);
    }

    public static short[] get16BitData(Object shortsAsObject) {
        return (short[])((short[])shortsAsObject);
    }

    public static int[] get32BitData(Object intsAsObject) {
        return (int[])((int[])intsAsObject);
    }

    public static int findArrayMax(Object pix) {
        if (pix instanceof byte[]) {
            return findArrayMax((byte[])((byte[])pix));
        } else if (pix instanceof int[]) {
            return findArrayMax((int[])((int[])pix));
        } else if (pix instanceof short[]) {
            return findArrayMax((short[])((short[])pix));
        } else {
            return pix instanceof float[] ? findArrayMax((float[])((float[])pix)) : -1;
        }
    }

    public static int findArrayMax(float[] pix) {
        int imax = -1;
        float max = 1.4E-45F;

        for(int i = 0; i < pix.length; ++i) {
            float pixel = pix[i];
            if (pixel > max) {
                max = pixel;
                imax = i;
            }
        }

        return imax;
    }

    public static int findArrayMax(short[] pix) {
        int imax = -1;
        short max = -32768;

        for(int i = 0; i < pix.length; ++i) {
            short pixel = pix[i];
            if (pixel > max) {
                max = pixel;
                imax = i;
            }
        }

        return imax;
    }

    public static int findArrayMax(byte[] pix) {
        int imax = -1;
        byte max = -128;

        for(int i = 0; i < pix.length; ++i) {
            byte pixel = pix[i];
            if (pixel > max) {
                max = pixel;
                imax = i;
            }
        }

        return imax;
    }

    public static int findArrayMax(int[] pix) {
        int imax = -1;
        int max = -2147483648;

        for(int i = 0; i < pix.length; ++i) {
            int pixel = pix[i];
            if (pixel > max) {
                max = pixel;
                imax = i;
            }
        }

        return imax;
    }

    public static byte[] convertRGB32IntToBytes(int[] pixels) {
        byte[] bytes = new byte[pixels.length * 4];
        int j = 0;

        for(int i = 0; i < pixels.length; ++i) {
            bytes[j++] = (byte)(pixels[i] & 255);
            bytes[j++] = (byte)(pixels[i] >> 8 & 255);
            bytes[j++] = (byte)(pixels[i] >> 16 & 255);
            bytes[j++] = 0;
        }

        return bytes;
    }

    public static int[] convertRGB32BytesToInt(byte[] pixels) {
        int[] ints = new int[pixels.length / 4];

        for(int i = 0; i < ints.length; ++i) {
            ints[i] = pixels[4 * i] + (pixels[4 * i + 1] << 8) + (pixels[4 * i + 2] << 16);
        }

        return ints;
    }

    public static byte[] getRGB32PixelsFromColorPanes(byte[][] planes) {
        int j = 0;
        byte[] pixels = new byte[planes.length * 4];

        for(int i = 0; i < planes.length; ++i) {
            pixels[j++] = planes[2][i];
            pixels[j++] = planes[1][i];
            pixels[j++] = planes[0][i];
            pixels[j++] = 0;
        }

        return pixels;
    }

    public static short[] getRGB64PixelsFromColorPlanes(short[][] planes) {
        int j = -1;
        short[] pixels = new short[planes[0].length * 4];

        for(int i = 0; i < planes[0].length; ++i) {
            ++j;
            pixels[j] = planes[2][i];
            ++j;
            pixels[j] = planes[1][i];
            ++j;
            pixels[j] = planes[0][i];
            ++j;
            pixels[j] = 0;
        }

        return pixels;
    }

    public static byte[][] getColorPlanesFromRGB32(byte[] pixels) {
        byte[] r = new byte[pixels.length / 4];
        byte[] g = new byte[pixels.length / 4];
        byte[] b = new byte[pixels.length / 4];
        int j = 0;

        for(int i = 0; i < pixels.length / 4; ++i) {
            b[i] = pixels[j++];
            g[i] = pixels[j++];
            r[i] = pixels[j++];
            ++j;
        }

        byte[][] planes = new byte[][]{r, g, b};
        return planes;
    }

    public static short[][] getColorPlanesFromRGB64(short[] pixels) {
        short[] r = new short[pixels.length / 4];
        short[] g = new short[pixels.length / 4];
        short[] b = new short[pixels.length / 4];
        int j = 0;

        for(int i = 0; i < pixels.length / 4; ++i) {
            b[i] = pixels[j++];
            g[i] = pixels[j++];
            r[i] = pixels[j++];
            ++j;
        }

        short[][] planes = new short[][]{r, g, b};
        return planes;
    }

    public static byte[] singleChannelFromRGB32(byte[] pixels, int channel) {
        if (channel != 0 && channel != 1 && channel != 2) {
            return null;
        } else {
            byte[] p = new byte[pixels.length / 4];

            for(int i = 0; i < p.length; ++i) {
                p[i] = pixels[2 - channel + 4 * i];
            }

            return p;
        }
    }

    public static short[] singleChannelFromRGB64(short[] pixels, int channel) {
        if (channel != 0 && channel != 1 && channel != 2) {
            return null;
        } else {
            short[] p = new short[pixels.length / 4];

            for(int i = 0; i < p.length; ++i) {
                p[i] = pixels[2 - channel + 4 * i];
            }

            return p;
        }
    }

    public static LUT makeLUT(Color color, double gamma) {
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        int size = 256;
        byte[] rs = new byte[size];
        byte[] gs = new byte[size];
        byte[] bs = new byte[size];

        for(int x = 0; x < size; ++x) {
            double xn = (double)x / (double)(size - 1);
            double yn = Math.pow(xn, gamma);
            rs[x] = (byte)((int)(yn * (double)r));
            gs[x] = (byte)((int)(yn * (double)g));
            bs[x] = (byte)((int)(yn * (double)b));
        }

        return new LUT(8, size, rs, gs, bs);
    }

    public static void setImageStorageClass(Class storageClass) {
        storageClass_ = storageClass;
    }

    public static Class getImageStorageClass() {
        return storageClass_;
    }

    public static TaggedImageStorage newImageStorageInstance(String acqPath, boolean newDataSet, JSONObject summaryMetadata) {
        try {
            return (TaggedImageStorage)storageClass_.getConstructor(String.class, Boolean.class, JSONObject.class).newInstance(acqPath, newDataSet, summaryMetadata);
        } catch (Exception var4) {
            ReportingUtils.logError(var4);
            return null;
        }
    }

    public static int unsignedValue(byte b) {
        return b & 255;
    }

    public static int unsignedValue(short s) {
        return s & '\uffff';
    }

    public static int getMin(Object pixels) {
        int min;
        int i;
        if (pixels instanceof byte[]) {
            byte[] bytes = (byte[])((byte[])pixels);
            min = 2147483647;

            for(i = 0; i < bytes.length; ++i) {
                min = Math.min(min, unsignedValue(bytes[i]));
            }

            return min;
        } else if (!(pixels instanceof short[])) {
            return -1;
        } else {
            short[] shorts = (short[])((short[])pixels);
            min = 2147483647;

            for(i = 0; i < shorts.length; ++i) {
                min = Math.min(min, unsignedValue(shorts[i]));
            }

            return min;
        }
    }

    public static int getMax(Object pixels) {
        int min;
        int i;
        if (pixels instanceof byte[]) {
            byte[] bytes = (byte[])((byte[])pixels);
            min = -2147483648;

            for(i = 0; i < bytes.length; ++i) {
                min = Math.max(min, unsignedValue(bytes[i]));
            }

            return min;
        } else if (!(pixels instanceof short[])) {
            return -1;
        } else {
            short[] shorts = (short[])((short[])pixels);
            min = -2147483648;

            for(i = 0; i < shorts.length; ++i) {
                min = Math.max(min, unsignedValue(shorts[i]));
            }

            return min;
        }
    }

    public static int[] getMinMax(Object pixels) {
        int[] result = new int[2];
        int max = -2147483648;
        int min = 2147483647;
        int i;
        if (pixels instanceof byte[]) {
            byte[] bytes = (byte[])((byte[])pixels);

            for(i = 0; i < bytes.length; ++i) {
                max = Math.max(max, unsignedValue(bytes[i]));
                min = Math.min(min, unsignedValue(bytes[i]));
            }

            result[0] = min;
            result[1] = max;
            return result;
        } else if (!(pixels instanceof short[])) {
            return null;
        } else {
            short[] shorts = (short[])((short[])pixels);

            for(i = 0; i < shorts.length; ++i) {
                min = Math.min(min, unsignedValue(shorts[i]));
                max = Math.max(max, unsignedValue(shorts[i]));
            }

            result[0] = min;
            result[1] = max;
            return result;
        }
    }

    public static TaggedImage makeTaggedImage(ImageProcessor proc) {
        JSONObject tags = new JSONObject();

        try {
            MDUtils.setChannelIndex(tags, 0);
            MDUtils.setSliceIndex(tags, 0);
            MDUtils.setPositionIndex(tags, 0);
            MDUtils.setFrameIndex(tags, 0);
            MDUtils.setWidth(tags, proc.getWidth());
            MDUtils.setHeight(tags, proc.getHeight());
            MDUtils.setPixelType(tags, getImageProcessorType(proc));
        } catch (Exception var3) {
            return null;
        }

        return new TaggedImage(proc.getPixels(), tags);
    }

    public static TaggedImage makeTaggedImage(Object pixels, int channelIndex, int sliceIndex, int positionIndex, int frameIndex, int width, int height, int numberOfBytesPerPixel) {
        JSONObject tags = new JSONObject();

        try {
            MDUtils.setChannelIndex(tags, channelIndex);
            MDUtils.setSliceIndex(tags, sliceIndex);
            MDUtils.setPositionIndex(tags, positionIndex);
            MDUtils.setFrameIndex(tags, frameIndex);
            MDUtils.setWidth(tags, width);
            MDUtils.setHeight(tags, height);
            MDUtils.setPixelTypeFromByteDepth(tags, numberOfBytesPerPixel);
        } catch (Exception var10) {
            return null;
        }

        return new TaggedImage(pixels, tags);
    }

    public static TaggedImage copyMetadata(TaggedImage image) {
        JSONArray names = image.tags.names();
        String[] keys = new String[names.length()];

        try {
            for(int j = 0; j < names.length(); ++j) {
                keys[j] = names.getString(j);
            }

            return new TaggedImage(image.pix, new JSONObject(image.tags, keys));
        } catch (JSONException var4) {
            ReportingUtils.logError(var4, "Unable to duplicate image metadata");
            return null;
        }
    }

    private static void show(String title, ImageProcessor proc) {
        (new ImagePlus(title, proc)).show();
    }

    public class MinAndMax {
        int min;
        int max;

        public MinAndMax() {
        }
    }
}
