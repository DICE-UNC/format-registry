/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.drexel.acin.identifier;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * @author omodolu95
 */
public class CompressionTest {

    public static void websiteTest() {
        try {
            // Encode a String into bytes
            String inputString = "blahblahblah??";
            byte[] input = inputString.getBytes("UTF-8");


            System.out.println("INPUT BYTES " + input.length);
            // Compress the bytes
            byte[] output = new byte[100];
            Deflater compresser = new Deflater();
            compresser.setInput(input);
            compresser.finish();
            int compressedDataLength = compresser.deflate(output);

            System.out.println("COMPRESSED BYTES " + compressedDataLength);

            // Decompress the bytes
            Inflater decompresser = new Inflater();
            decompresser.setInput(output, 0, compressedDataLength);
            byte[] result = new byte[100];
            int resultLength = decompresser.inflate(result);
            decompresser.end();

            // Decode the bytes into a String
            String outputString = new String(result, 0, resultLength, "UTF-8");
            System.out.println("DECODED RESULT " + outputString);
        } catch (java.io.UnsupportedEncodingException ex) {
            // handle
        } catch (java.util.zip.DataFormatException ex) {
            // handle
        }

    }

    /**
     * @param args
     */
    public static void main(String[] args) throws DataFormatException {

        websiteTest();

        byte[] inBuffer;
        int temp;
        int totalOutputSize = 0;
        int totalInputSize = 0;

        FileInputStream inputString = null;

        try {
            inputString = new FileInputStream("/home/omodolu95/NetBeansProjects/ClassifierCodeEtc/src/Unknown Formats/Mars.x3d");

            inBuffer = new byte[1024];

            totalInputSize = inputString.read(inBuffer);
            if (totalInputSize != inBuffer.length) {
                System.out.println("Warning, only read " + totalInputSize + " bytes");
            }

            System.out.println(new String(inBuffer));

            // Compress the bytes
            byte[] output = new byte[50000];

            Deflater compresser = new Deflater();

            compresser.setInput(inBuffer, 0, inBuffer.length);


            while (!compresser.needsInput()) {
                temp = compresser.deflate(output, totalOutputSize, output.length - totalOutputSize);
                System.out.println("Compressed " + temp + " more bytes");
                totalOutputSize += temp;
            }

            System.out.println(compresser.needsInput());
            compresser.setInput(inBuffer);
            compresser.finish();

            while ((temp = compresser.deflate(output, totalOutputSize, output.length - totalOutputSize)) != 0) {
                System.out.println("Compressed " + temp + " more bytes");
                totalOutputSize += temp;
            }

            System.out.println("OUTPUT BYTES IS " + totalOutputSize);

            if (totalOutputSize <= 0) {
                System.out.println("O_o");
            }


            // Decompress the bytes
            Inflater decompresser = new Inflater();
            decompresser.setInput(output, 0, totalOutputSize);
            byte[] result = new byte[1000000];
            int resultLength = decompresser.inflate(result);
            decompresser.end();

            // Decode the bytes into a String
            String outputString = new String(result, 0, resultLength, "UTF-8");
            System.out.println("DECODED RESULT " + outputString);


//            int compressedDataLength = compresser.deflate(output);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CompressionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CompressionTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                inputString.close();
            } catch (IOException ex) {
                Logger.getLogger(CompressionTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
