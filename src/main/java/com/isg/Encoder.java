package com.isg;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.channels.SeekableByteChannel;
import java.util.List;
import java.util.ArrayList;
import org.jcodec.api.awt.AWTSequenceEncoder;
import org.jcodec.common.io.FileChannelWrapper;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Rational;

public class Encoder {
    public static void encodeFile(String inputFilePath, String outputVideoPath) throws IOException {
        File inFile = new File(inputFilePath);
        byte[] fileBytes = java.nio.file.Files.readAllBytes(inFile.toPath());
        String fileName = inFile.getName();
        byte[] nameBytes = fileName.getBytes("UTF-8");
        int fileSize = fileBytes.length;
        int nameLen = nameBytes.length;

        ByteArrayOutputStream metaBaos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(metaBaos);
        dos.writeBytes("ISG1");
        dos.writeInt(fileSize);
        dos.writeInt(nameLen);
        dos.write(nameBytes);
        dos.flush();
        byte[] metaBytes = metaBaos.toByteArray();
        dos.close();

        List<Boolean> metaBits = toBits(metaBytes);
        List<Boolean> dataBits = toBits(fileBytes);

        int blockSize = 2;
        int width = 1280, height = 720;
        int blocksPerRow = width / blockSize;
        int blocksPerCol = height / blockSize;

        FileChannelWrapper out = NIOUtils.writableFileChannel(outputVideoPath);
        File outFile = new File(outputVideoPath);
        AWTSequenceEncoder encoder = AWTSequenceEncoder.create25Fps(outFile);

        Runnable encodeFrame = () -> {};
        BufferedImage metaImg = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D gMeta = metaImg.createGraphics();
        gMeta.setColor(Color.BLACK);
        gMeta.fillRect(0, 0, width, height);
        for (int i = 0; i < metaBits.size() && i < blocksPerRow * blocksPerCol; i++) {
            int blockX = (i % blocksPerRow) * blockSize;
            int blockY = (i / blocksPerRow) * blockSize;
            if (metaBits.get(i)) {
                gMeta.setColor(Color.WHITE);
                gMeta.fillRect(blockX, blockY, blockSize, blockSize);
            }
        }
        gMeta.dispose();
        encoder.encodeImage(metaImg);

        int bitsPerFrame = blocksPerRow * blocksPerCol;
        int totalDataBits = dataBits.size();
        int frameIndex = 0;
        while (frameIndex * bitsPerFrame < totalDataBits) {
            BufferedImage dataImg = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
            Graphics2D g = dataImg.createGraphics();
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, width, height);
            int startBit = frameIndex * bitsPerFrame;
            int endBit = Math.min(startBit + bitsPerFrame, totalDataBits);
            for (int i = startBit; i < endBit; i++) {
                int bitIndex = i - startBit;
                int blockX = (bitIndex % blocksPerRow) * blockSize;
                int blockY = (bitIndex / blocksPerRow) * blockSize;
                if (dataBits.get(i)) {
                    g.setColor(Color.WHITE);
                    g.fillRect(blockX, blockY, blockSize, blockSize);
                }
            }
            g.dispose();
            encoder.encodeImage(dataImg);
            frameIndex++;
        }

        encoder.finish();
        out.close();
        System.out.println("Encoding complete: " + outputVideoPath);
    }

    private static List<Boolean> toBits(byte[] bytes) {
        List<Boolean> bits = new ArrayList<>(bytes.length * 8);
        for (byte b : bytes) {
            for (int i = 7; i >= 0; i--) {
                bits.add(((b >> i) & 1) == 1);
            }
        }
        return bits;
    }
}
