package com.isg;

public class ISG {
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: java -jar isg.jar <encode|decode> <input> <output>");
            return;
        }
        String mode = args[0];
        String inputPath = args[1];
        String outputPath = args[2];
        try {
            if (mode.equalsIgnoreCase("encode")) {
                Encoder.encodeFile(inputPath, outputPath);
            } else if (mode.equalsIgnoreCase("decode")) {
                Decoder.decodeFile(inputPath, outputPath);
            } else {
                System.err.println("Invalid mode. Use 'encode' or 'decode'.");
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
