package org.micromanager.rapp;

import javax.media.jai.PlanarImage;
import com.sun.media.jai.codec.ByteArraySeekableStream;
import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageDecoder;
import com.sun.media.jai.codec.SeekableStream;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.awt.Image;
import java.awt.image.RenderedImage;

import javax.swing.JOptionPane;
import javax.swing.JLabel;
import javax.swing.ImageIcon;

public class ImageViewer2  {

    static Image load(byte[] data) throws Exception{
        Image image = null;
        SeekableStream stream = new ByteArraySeekableStream(data);
        String[] names = ImageCodec.getDecoderNames(stream);
        ImageDecoder dec =
                ImageCodec.createImageDecoder(names[0], stream, null);
        RenderedImage im = dec.decodeAsRenderedImage();
        image = PlanarImage.wrapRenderedImage(im).getAsBufferedImage();
        return image;
    }

    public static void main(String[] args) throws Exception{
        String path;
        if (args.length==0) {
            path = JOptionPane.showInputDialog(null, "Image Path",
                    "c:/applications/sidebar.tif");
        }
        else {
            path = args[0];
        }
        FileInputStream in = new FileInputStream(path);
        FileChannel channel = in.getChannel();
        ByteBuffer buffer = ByteBuffer.allocate((int)channel.size());
        channel.read(buffer);
        Image image = load(buffer.array());
        // make sure that the image is not too big
        //  scale with a width of 500
        Image imageScaled =
                image.getScaledInstance(500, -1,  Image.SCALE_SMOOTH);
        //
        System.out.println("image: " + path + "\n" + image);
        //
        JOptionPane.showMessageDialog(null, new JLabel(
                new ImageIcon( imageScaled )) );
    }
}
