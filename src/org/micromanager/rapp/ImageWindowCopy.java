//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.micromanager.rapp;

import ij.CompositeImage;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Menus;
import ij.Prefs;
import ij.WindowManager;
import ij.gui.*;
import ij.io.FileSaver;
import ij.macro.Interpreter;
import ij.measure.Calibration;
import ij.plugin.frame.Channels;
import ij.util.Java2;

import javax.swing.*;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;

public class ImageWindowCopy extends JInternalFrame implements FocusListener, WindowListener, WindowStateListener, MouseWheelListener {
    public static final int MIN_WIDTH = 128;
    public static final int MIN_HEIGHT = 32;
    private static final String LOC_KEY = "image.loc";
    protected ImagePlus imp;
    protected ImageJ ij;
    protected ImageCanvas ic;
    private double initialMagnification;
    private int newWidth;
    private int newHeight;
    protected boolean closed;
    private boolean newCanvas;
    private boolean unzoomWhenMinimizing;
    Rectangle maxWindowBounds;
    Rectangle maxBounds;
    long setMaxBoundsTime;
    private boolean firstSmallWindow;
    private static final int XINC = 12;
    private static final int YINC = 16;
    private static final int TEXT_GAP = 10;
    private static int xbase = -1;
    private static int ybase;
    private static int xloc;
    private static int yloc;
    private static int count;
    private static boolean centerOnScreen;
    private static Point nextLocation;
    private int textGap;
    public boolean running;
    public boolean running2;
   // ImageWindow previousWindow = imp.getWindow();

    public ImageWindowCopy(String title) {
        super(title);
        this.initialMagnification = 1.0D;
        this.unzoomWhenMinimizing = true;
        this.textGap = centerOnScreen ? 0 : 10;
    }

    public ImageWindowCopy(ImagePlus imp) {
        this(imp, (ImageCanvas)null);
    }

    public ImageWindowCopy(ImagePlus imp, ImageCanvas ic) {
        super(imp.getTitle());
        this.initialMagnification = 1.0D;
        this.unzoomWhenMinimizing = true;
        this.textGap = centerOnScreen ? 0 : 10;
        if (Prefs.blackCanvas && this.getClass().getName().equals("ij.gui.ImageWindow")) {
            this.setForeground(Color.white);
            this.setBackground(Color.black);
        } else {
            this.setForeground(Color.black);
            if (IJ.isLinux()) {
                this.setBackground(ImageJ.backgroundColor);
            } else {
                this.setBackground(Color.white);
            }
        }

        boolean openAsHyperStack = imp.getOpenAsHyperStack();
        this.ij = IJ.getInstance();
        this.imp = imp;
        if (ic == null) {
            ic = new ImageCanvas(imp);
            this.newCanvas = true;
        }

        this.ic = ic;

        this.setLayout(new ImageLayout(ic));
        this.add(ic);
        this.addFocusListener(this);
        //previousWindow.addWindowListener(this);
        //previousWindow.addWindowStateListener(this);
        this.addKeyListener(this.ij);
        this.setFocusTraversalKeysEnabled(false);
//        if (!(this instanceof StackWindow)) {
//            this.addMouseWheelListener(this);
//        }

        this.setResizable(true);
//        if (!(previousWindow instanceof HistogramWindow) || !IJ.isMacro() || !Interpreter.isBatchMode()) {
//            WindowManager.addWindow(previousWindow);
//            imp.setWindow(previousWindow);
//        }

//        if (previousWindow != null) {
//            if (this.newCanvas) {
//                this.setLocationAndSize(false);
//            } else {
//              //  ic.update(this.getCanvas());
//            }
//
//            Point loc = previousWindow.getLocation();
//            this.setLocation(loc.x, loc.y);
//            if (!(previousWindow instanceof StackWindow)) {
//                this.pack();
//                this.show();
//            }
//
//            if (ic.getMagnification() != 0.0D) {
//                imp.setTitle(imp.getTitle());
//            }
//
//            boolean unlocked = imp.lockSilently();
//            boolean changes = imp.changes;
//            imp.changes = false;
//            previousWindow.close();
//            imp.changes = changes;
//            if (unlocked) {
//                imp.unlock();
//            }
//
//            if (this.imp != null) {
//                this.imp.setOpenAsHyperStack(openAsHyperStack);
//            }
//
//            WindowManager.setCurrentWindow(previousWindow);
//        } else {
//            this.setLocationAndSize(false);
//            if (this.ij != null && !IJ.isMacintosh()) {
//                Image img = this.ij.getIconImage();
//                if (img != null) {
//                    try {
//                        previousWindow.setIconImage(img);
//                    } catch (Exception var8) {
//                        ;
//                    }
//                }
//            }
//
//            if (nextLocation != null) {
//                this.setLocation(nextLocation);
//            } else if (centerOnScreen) {
//                GUI.center(previousWindow);
//            }
//
//            nextLocation = null;
//            centerOnScreen = false;
//            if (!Interpreter.isBatchMode() && (IJ.getInstance() != null || !(previousWindow instanceof HistogramWindow))) {
//                this.show();
//            } else {
//                WindowManager.setTempCurrentImage(imp);
//                Interpreter.addBatchModeImage(imp);
//            }
//        }

    }

    private void setLocationAndSize(boolean updating) {
        int width = this.imp.getWidth();
        int height = this.imp.getHeight();
        Rectangle maxWindow = this.getMaxWindow(0, 0);
        if (WindowManager.getWindowCount() <= 1) {
            xbase = -1;
        }

        if (width > maxWindow.width / 2 && xbase > maxWindow.x + 5 + 72) {
            xbase = -1;
        }

        if (xbase == -1) {
            count = 0;
            xbase = maxWindow.x + (maxWindow.width > 1800 ? 24 : 12);
            if (width * 2 > maxWindow.width) {
                ybase = maxWindow.y;
            } else {
                Point loc = Prefs.getLocation("image.loc");
                if (loc != null && loc.x < maxWindow.width * 2 / 3 && loc.y < maxWindow.height / 3) {
                    xbase = loc.x;
                    ybase = loc.y;
                } else {
                    xbase = maxWindow.x + maxWindow.width / 2 - width / 2;
                    ybase = maxWindow.y;
                }

                this.firstSmallWindow = true;
                if (IJ.debugMode) {
                    IJ.log("ImageWindow.xbase: " + xbase + " " + loc);
                }
            }

            xloc = xbase;
            yloc = ybase;
        }

        int x = xloc;
        int y = yloc;
        xloc += 12;
        yloc += 16;
        ++count;
        if (count % 6 == 0) {
            xloc = xbase;
            yloc = ybase;
        }

        //int sliderHeight = previousWindow instanceof StackWindow ? 20 : 0;
       // int screenHeight = maxWindow.y + maxWindow.height - sliderHeight;

        double mag;
        double mag2 = 0;
//        for(mag = 1.0D; (double)xbase + (double)width * mag > (double)(maxWindow.x + maxWindow.width) || (double)ybase + (double)height * mag >= (double)screenHeight; mag = mag2) {
//            mag2 = ImageCanvas.getLowerZoomLevel(mag);
//            if (mag2 == mag) {
//                break;
//            }
//        }

//        if (mag < 1.0D) {
//            this.initialMagnification = mag;
//            this.ic.setDrawingSize((int)((double)width * mag), (int)((double)height * mag));
//        }

        //this.ic.setMagnification(mag);
//        if ((double)y + (double)height * mag > (double)screenHeight) {
//            y = ybase;
//        }

        if (!updating) {
            this.setLocation(x, y);
        }

        if (Prefs.open100Percent && this.ic.getMagnification() < 1.0D) {
            while(this.ic.getMagnification() < 1.0D) {
                this.ic.zoomIn(0, 0);
            }

            //this.setSize(Math.min(width, maxWindow.width - x), Math.min(height, screenHeight - y));
            this.validate();
        } else {
            this.pack();
        }

    }

    Rectangle getMaxWindow(int xloc, int yloc) {
        Rectangle bounds = GUI.getMaxWindowBounds();
        if (xloc > bounds.x + bounds.width || yloc > bounds.y + bounds.height) {
            Rectangle bounds2 = this.getSecondaryMonitorBounds(xloc, yloc);
            if (bounds2 != null) {
                return bounds2;
            }
        }

        Dimension ijSize = this.ij != null ? this.ij.getSize() : new Dimension(0, 0);
        if (bounds.height > 600) {
            bounds.y += ijSize.height;
            bounds.height -= ijSize.height;
        }

        return bounds;
    }

    private Rectangle getSecondaryMonitorBounds(int xloc, int yloc) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gs = ge.getScreenDevices();
        Rectangle bounds = null;

        for(int j = 0; j < gs.length; ++j) {
            GraphicsDevice gd = gs[j];
            GraphicsConfiguration[] gc = gd.getConfigurations();

            for(int i = 0; i < gc.length; ++i) {
                Rectangle bounds2 = gc[i].getBounds();
                if (bounds2 != null && bounds2.contains(xloc, yloc)) {
                    bounds = bounds2;
                    break;
                }
            }
        }

        if (IJ.debugMode) {
            IJ.log("getSecondaryMonitorBounds: " + bounds);
        }

        return bounds;
    }

    public double getInitialMagnification() {
        return this.initialMagnification;
    }

    public Insets getInsets() {
        Insets insets = super.getInsets();
        double mag = this.ic.getMagnification();
        int extraWidth = (int)((128.0D - (double)this.imp.getWidth() * mag) / 2.0D);
        if (extraWidth < 0) {
            extraWidth = 0;
        }

        int extraHeight = (int)((32.0D - (double)this.imp.getHeight() * mag) / 2.0D);
        if (extraHeight < 0) {
            extraHeight = 0;
        }

        insets = new Insets(insets.top + this.textGap + extraHeight, insets.left + extraWidth, insets.bottom + extraHeight, insets.right + extraWidth);
        return insets;
    }

    public void drawInfo(Graphics g) {
        if (this.textGap != 0) {
            Insets insets = super.getInsets();
            if (this.imp.isComposite()) {
                CompositeImage ci = (CompositeImage)this.imp;
                if (ci.getMode() == 1) {
                    Color c = ci.getChannelColor();
                    if (Color.green.equals(c)) {
                        c = new Color(0, 180, 0);
                    }

                    g.setColor(c);
                }
            }

            Java2.setAntialiasedText(g, true);
            g.drawString(this.createSubtitle(), insets.left + 5, insets.top + 10);
        }

    }

    public String createSubtitle() {
        String s = "";
        int nSlices = this.imp.getStackSize();
        int newline;
        if (nSlices > 1) {
            ImageStack stack = this.imp.getStack();
            newline = this.imp.getCurrentSlice();
            s = s + newline + "/" + nSlices;
            String label = stack.getShortSliceLabel(newline);
            if (label != null && label.length() > 0) {
                if (this.imp.isHyperStack()) {
                    label = label.replace(';', ' ');
                }

                s = s + " (" + label + ")";
            }

//            if (previousWindow instanceof StackWindow && this.running2) {
//                return s;
//            }

            s = s + "; ";
        } else {
            String label = (String)this.imp.getProperty("Label");
            if (label != null) {
                newline = label.indexOf(10);
                if (newline > 0) {
                    label = label.substring(0, newline);
                }

                int len = label.length();
                if (len > 4 && label.charAt(len - 4) == '.' && !Character.isDigit(label.charAt(len - 1))) {
                    label = label.substring(0, len - 4);
                }

                if (label.length() > 60) {
                    label = label.substring(0, 60);
                }

                s = label + "; ";
            }
        }

        int type = this.imp.getType();
        Calibration cal = this.imp.getCalibration();
        if (cal.scaled()) {
            s = s + IJ.d2s((double)this.imp.getWidth() * cal.pixelWidth, 2) + "x" + IJ.d2s((double)this.imp.getHeight() * cal.pixelHeight, 2) + " " + cal.getUnits() + " (" + this.imp.getWidth() + "x" + this.imp.getHeight() + "); ";
        } else {
            s = s + this.imp.getWidth() + "x" + this.imp.getHeight() + " pixels; ";
        }

        double size = (double)this.imp.getWidth() * (double)this.imp.getHeight() * (double)this.imp.getStackSize() / 1024.0D;
        switch(type) {
            case 0:
            case 3:
                s = s + "8-bit";
                break;
            case 1:
                s = s + "16-bit";
                size *= 2.0D;
                break;
            case 2:
                s = s + "32-bit";
                size *= 4.0D;
                break;
            case 4:
                s = s + "RGB";
                size *= 4.0D;
        }

        if (this.imp.isInvertedLut()) {
            s = s + " (inverting LUT)";
        }

        String s2 = null;
        String s3 = null;
        if (size < 1024.0D) {
            s2 = IJ.d2s(size, 0);
            s3 = "K";
        } else if (size < 10000.0D) {
            s2 = IJ.d2s(size / 1024.0D, 1);
            s3 = "MB";
        } else if (size < 1048576.0D) {
            s2 = IJ.d2s((double)Math.round(size / 1024.0D), 0);
            s3 = "MB";
        } else {
            s2 = IJ.d2s(size / 1048576.0D, 1);
            s3 = "GB";
        }

        if (s2.endsWith(".0")) {
            s2 = s2.substring(0, s2.length() - 2);
        }

        return s + "; " + s2 + s3;
    }

    public void paint(Graphics g) {
        this.drawInfo(g);
        Rectangle r = this.ic.getBounds();
        int extraWidth = 128 - r.width;
        int extraHeight = 32 - r.height;
        if (extraWidth <= 0 && extraHeight <= 0 && !Prefs.noBorder && !IJ.isLinux()) {
            g.drawRect(r.x - 1, r.y - 1, r.width + 1, r.height + 1);
        }

    }

    public boolean close() {
        boolean isRunning = this.running || this.running2;
        this.running = this.running2 = false;
        boolean virtual = this.imp.getStackSize() > 1 && this.imp.getStack().isVirtual();
        if (isRunning) {
            IJ.wait(500);
        }

        if (this.ij == null || IJ.getApplet() != null || Interpreter.isBatchMode() || IJ.macroRunning() || virtual) {
            this.imp.changes = false;
        }

        if (this.imp.changes) {
            String name = this.imp.getTitle();
            String msg;
            if (name.length() > 22) {
                msg = "Save changes to\n\"" + name + "\"?";
            } else {
                msg = "Save changes to \"" + name + "\"?";
            }

           // YesNoCancelDialog d = new YesNoCancelDialog(previousWindow, "ImageJ", msg);
//            if (d.cancelPressed()) {
//                return false;
//            }
//
//            if (d.yesPressed()) {
//                FileSaver fs = new FileSaver(this.imp);
//                if (!fs.save()) {
//                    return false;
//                }
//            }
        }

        this.closed = true;
        if (WindowManager.getWindowCount() == 0) {
            xloc = 0;
            yloc = 0;
        }

        if (this.firstSmallWindow) {
            Prefs.saveLocation("image.loc", this.getLocation());
        }

       // WindowManager.removeWindow(previousWindow);
        if (this.ij != null && this.ij.quitting()) {
            return true;
        } else {
            this.dispose();
            if (this.imp != null) {
                this.imp.flush();
            }

            this.imp = null;
            return true;
        }
    }

    public ImagePlus getImagePlus() {
        return this.imp;
    }

    public void setImage(ImagePlus imp2) {
        ImageCanvas ic = this.getCanvas();
        if (ic != null && imp2 != null) {
            this.imp = imp2;
           // this.imp.setWindow(previousWindow);
            //ic.updateImage(this.imp);
            ic.setImageUpdated();
            ic.repaint();
            this.repaint();
        }
    }

    public void updateImage(ImagePlus imp) {
        if (imp != this.imp) {
            throw new IllegalArgumentException("imp!=this.imp");
        } else {
            this.imp = imp;
           // this.ic.updateImage(imp);
            this.setLocationAndSize(true);
//            if (previousWindow instanceof StackWindow) {
//                StackWindow sw = (StackWindow)previousWindow;
//                int stackSize = imp.getStackSize();
//                int nScrollbars = sw.getNScrollbars();
//                if (stackSize == 1 && nScrollbars > 0) {
//                //    sw.removeScrollbars();
//                } else if (stackSize > 1 && nScrollbars == 0) {
//                 ///   sw.addScrollbars(imp);
//                }
//            }

            this.pack();
            this.repaint();
            this.maxBounds = this.getMaximumBounds();
           // previousWindow.setMaximizedBounds(this.maxBounds);
            this.setMaxBoundsTime = System.currentTimeMillis();
        }
    }

    public ImageCanvas getCanvas() {
        return this.ic;
    }

    static ImagePlus getClipboard() {
        return ImagePlus.getClipboard();
    }

    public Rectangle getMaximumBounds() {
        double width = (double)this.imp.getWidth();
        double height = (double)this.imp.getHeight();
        double iAspectRatio = width / height;
        Rectangle maxWindow = GUI.getMaxWindowBounds();
        this.maxWindowBounds = maxWindow;
        if (iAspectRatio / ((double)maxWindow.width / (double)maxWindow.height) > 0.75D) {
            maxWindow.y += 22;
            maxWindow.height -= 22;
        }

        Dimension extraSize = this.getExtraSize();
        double maxWidth = (double)(maxWindow.width - extraSize.width);
        double maxHeight = (double)(maxWindow.height - extraSize.height);
        double mAspectRatio = maxWidth / maxHeight;
        int wWidth;
        int wHeight;
        double mag;
        if (iAspectRatio >= mAspectRatio) {
            mag = maxWidth / width;
            wWidth = maxWindow.width;
            wHeight = (int)(height * mag + (double)extraSize.height);
        } else {
            mag = maxHeight / height;
            wHeight = maxWindow.height;
            wWidth = (int)(width * mag + (double)extraSize.width);
        }

        int xloc = (int)(maxWidth - (double)wWidth) / 2;
        if (xloc < 0) {
            xloc = 0;
        }

        return new Rectangle(xloc, maxWindow.y, wWidth, wHeight);
    }

    Dimension getExtraSize() {
        Insets insets = this.getInsets();
        int extraWidth = insets.left + insets.right + 10;
        int extraHeight = insets.top + insets.bottom + 10;
        if (extraHeight == 20) {
            extraHeight = 42;
        }

        int members = this.getComponentCount();

        for(int i = 1; i < members; ++i) {
            Component m = this.getComponent(i);
            Dimension d = m.getPreferredSize();
            extraHeight += d.height + 5;
            if (IJ.debugMode) {
                IJ.log(i + "  " + d.height + " " + extraHeight);
            }
        }

        return new Dimension(extraWidth, extraHeight);
    }

    public Component add(Component comp) {
        comp = super.add(comp);
        //this.maxBounds = this.getMaximumBounds();
        //previousWindow.setMaximizedBounds(this.maxBounds);
        this.setMaxBoundsTime = System.currentTimeMillis();
        return comp;
    }

    public void maximize() {
        if (this.maxBounds != null) {
            int width = this.imp.getWidth();
            int height = this.imp.getHeight();
            double aspectRatio = (double)width / (double)height;
            Dimension extraSize = this.getExtraSize();
            int extraHeight = extraSize.height;
            double mag = (double)(this.maxBounds.height - extraHeight) / (double)height;
            if (IJ.debugMode) {
                IJ.log("maximize: " + mag + " " + this.ic.getMagnification() + " " + this.maxBounds);
            }

            //this.setSize(previousWindow.getMaximizedBounds().width, previousWindow.getMaximizedBounds().height);
            if (mag <= this.ic.getMagnification() && aspectRatio >= 0.5D && aspectRatio <= 2.0D) {
                this.unzoomWhenMinimizing = false;
            } else {
                //this.ic.setMagnification2(mag);
                //this.ic.setSrcRect(new Rectangle(0, 0, width, height));
                this.ic.setDrawingSize((int)((double)width * mag), (int)((double)height * mag));
                this.validate();
                this.unzoomWhenMinimizing = true;
            }

        }
    }

    public void minimize() {
        if (this.unzoomWhenMinimizing) {
            this.ic.unzoom();
        }

        this.unzoomWhenMinimizing = true;
    }

    public boolean isClosed() {
        return this.closed;
    }

    public void focusGained(FocusEvent e) {
        if (!Interpreter.isBatchMode() && this.ij != null && !this.ij.quitting() && this.imp != null) {
            if (IJ.debugMode) {
                IJ.log("focusGained: " + this.imp);
            }

           // WindowManager.setCurrentWindow(previousWindow);
        }

    }

    public void windowActivated(WindowEvent e) {
        if (IJ.debugMode) {
            IJ.log("windowActivated: " + this.imp.getTitle());
        }

        ImageJ ij = IJ.getInstance();
        boolean quitting = ij != null && ij.quitting();
        if (IJ.isMacintosh() && ij != null && !quitting) {
            IJ.wait(10);
          //  previousWindow.setMenuBar(Menus.getMenuBar());
        }

        if (this.imp != null) {
            if (!this.closed && !quitting && !Interpreter.isBatchMode()) {
              //  WindowManager.setCurrentWindow(previousWindow);
            }

            if (this.imp.isComposite()) {
                Channels.updateChannels();
            }

            this.imp.setActivated();
        }
    }

    public void windowClosing(WindowEvent e) {
        if (!this.closed) {
            if (this.ij != null) {
              //  WindowManager.setCurrentWindow(previousWindow);
                IJ.doCommand("Close");
            } else {
                this.dispose();
              //  WindowManager.removeWindow(previousWindow);
            }

        }
    }

    public void windowStateChanged(WindowEvent e) {
        int oldState = e.getOldState();
        int newState = e.getNewState();
        if ((oldState & 6) == 0 && (newState & 6) != 0) {
            this.maximize();
        } else if ((oldState & 6) != 0 && (newState & 6) == 0) {
            this.minimize();
        }

    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowDeactivated(WindowEvent e) {
    }

    public void focusLost(FocusEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowOpened(WindowEvent e) {
    }

    public void mouseWheelMoved(MouseWheelEvent event) {
        int rotation = event.getWheelRotation();
        int width = this.imp.getWidth();
        int height = this.imp.getHeight();
        Rectangle srcRect = this.ic.getSrcRect();
        int xstart = srcRect.x;
        int ystart = srcRect.y;
        if (!IJ.spaceBarDown() && srcRect.height != height) {
            srcRect.y += rotation * Math.max(height / 200, 1);
            if (srcRect.y < 0) {
                srcRect.y = 0;
            }

            if (srcRect.y + srcRect.height > height) {
                srcRect.y = height - srcRect.height;
            }
        } else {
            srcRect.x += rotation * Math.max(width / 200, 1);
            if (srcRect.x < 0) {
                srcRect.x = 0;
            }

            if (srcRect.x + srcRect.width > width) {
                srcRect.x = width - srcRect.width;
            }
        }

        if (srcRect.x != xstart || srcRect.y != ystart) {
            this.ic.repaint();
        }

    }

    public void copy(boolean cut) {
        this.imp.copy(cut);
    }

    public void paste() {
        this.imp.paste();
    }

    public void mouseMoved(int x, int y) {
        this.imp.mouseMoved(x, y);
    }

    public String toString() {
        return this.imp != null ? this.imp.getTitle() : "";
    }

    public static void centerNextImage() {
        centerOnScreen = true;
    }

    public static void setNextLocation(Point loc) {
        nextLocation = loc;
    }

    public static void setNextLocation(int x, int y) {
        nextLocation = new Point(x, y);
    }

    public void setLocationAndSize(int x, int y, int width, int height) {
        this.setBounds(x, y, width, height);
        this.getCanvas().fitToWindow();
        this.pack();
    }
}
