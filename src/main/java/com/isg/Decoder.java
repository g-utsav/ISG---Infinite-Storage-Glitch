package com.isg;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.channels.ReadableByteChannel;
import java.util.*;
import org.jcodec.api.FrameGrab;
import org.jcodec.common.io.FileChannelWrapper;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Picture;

public class Decoder {
    public static void decodeFile(String inputVideoPath, String outputFilePath) throws Exception {
        File videoFile = new File(inputVideoPath);
        FileChannelWrapper in = NIOUtils.readableFileChannel(videoFile.getPath());
        FrameGrab grab = FrameGrab.createFrameGrab(in);

        Picture pic = grab.getNativeFrame();
        if (pic == null) {
            throw new IOException("No frames found in video.");
        }
        BufferedImage metaImg = org.jcodec.scale.AWTUtil.toBufferedImage(pic);

        int width = metaImg.getWidth();
        int height = metaImg.getHeight();
        int blockSize = 2;
        int blocksPerRow = width / blockSize;
        int blocksPerCol = height / blockSize;

        List<Boolean> metaBits = new ArrayList<>();
        for (int y = 0; y < blocksPerCol; y++) {
            for (int x = 0; x < blocksPerRow; x++) {
                int rgb = metaImg.getRGB(x * blockSize, y * blockSize);
                int r = (rgb >> 16) & 0xFF;
                metaBits.add(r > 128);
            }
        }

        ByteArrayInputStream bais;
        DataInputStream dis;
        {
            byte[] metaBytes = bitsToBytes(metaBits);
            bais = new ByteArrayInputStream(metaBytes);
            dis = new DataInputStream(bais);
        }
        byte[] magic = new byte[4];
        dis.readFully(magic);
        String magicStr = new String(magic, "ASCII");
        if (!magicStr.equals("ISG1")) {
            throw new IOException("Invalid magic, not an ISG video.");
        }
        int fileSize = dis.readInt();
        int nameLen = dis.readInt();
        byte[] nameBytes = new byte[nameLen];
        dis.readFully(nameBytes);
        String fileName = new String(nameBytes, "UTF-8");

        File outFile = new File(outputFilePath);
        if (outFile.isDirectory()) {
            outFile = new File(outFile, fileName);
        }

        int totalDataBits = fileSize * 8;
        List<Boolean> dataBits = new ArrayList<>();
        while (dataBits.size() < totalDataBits) {
            pic = grab.getNativeFrame();
            if (pic == null) break;
            BufferedImage img = org.jcodec.scale.AWTUtil.toBufferedImage(pic);
            for (int y = 0; y < blocksPerCol; y++) {
                for (int x = 0; x < blocksPerRow; x++) {
                    if (dataBits.size() >= totalDataBits) break;
                    int rgb = img.getRGB(x * blockSize, y * blockSize);
                    int r = (rgb >> 16) & 0xFF;
                    dataBits.add(r > 128);
                }
            }
        }
        in.close();

        if (dataBits.size() < totalDataBits) {
            throw new IOException("Video ended before all data bits were read (possible corruption).");
        }

        byte[] fileBytes = bitsToBytes(dataBits);
        try (FileOutputStream fos = new FileOutputStream(outFile)) {
            fos.write(fileBytes, 0, fileSize);
        }
        System.out.println("Decoding complete: " + outFile.getAbsolutePath());
    }

    private static byte[] bitsToBytes(List<Boolean> bits) {
        int byteLen = (bits.size() + 7) / 8;
        byte[] bytes = new byte[byteLen];
        for (int i = 0; i < bits.size(); i++) {
            if (bits.get(i)) {
                bytes[i / 8] |= (0x80 >> (i % 8));
            }
        }
        return bytes;
    }
}
