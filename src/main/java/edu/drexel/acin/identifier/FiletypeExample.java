package edu.drexel.acin.identifier;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Deflater;

/**
 * Training Example base class. In May 2013, declared class abstract. Should not
 * instantiate objects of this base class. Previous modifications were in
 * February 2013
 *
 * @author Vincent Cicirello (Richard Stockton College)
 * @version May 2013
 */
public class FiletypeExample {

    private static final int FILE_HEADER_SIZE = 16384;
    private static final int COMPRESSION_LEVEL = 5;
    //    private static final int DEFLATE_STRATEGY = Deflater.BEST_SPEED;
    private final int compressedSize;
    private final String filetype;
    private final byte[] inBuffer;

    /**
     * Constructs a new instance of the FiletypeExample. Assumes you already
     * have its type. Constructor computes its compressed size.
     *
     * @param filename The filename of the file containing the instance.
     * @param filetype The type.
     * @throws IOException
     */
    public FiletypeExample(String filename, String filetype) throws IOException {
        this(new FileInputStream(filename), filetype);
    }

    /**
     * Constructs a new instance of the FiletypeExample for a file of unknown
     * type.
     *
     * @param filename The filename of the file containing the instance.
     * @throws IOException
     */
    public FiletypeExample(String filename) throws IOException {
        this(new FileInputStream(filename), filename);
    }

    /**
     * Constructs a new instance of the FiletypeExample. Assumes you already
     * have its type. Constructor computes its compressed size.
     *
     * @param filename The filename of the file containing the instance.
     * @param filetype The type.
     * @throws IOException
     */
    public FiletypeExample(InputStream fileStream, String filetype) throws IOException {
        this.filetype = filetype;
        try {
            this.inBuffer = loadData(fileStream);

        } catch (IOException ex) {
            System.out.println("Trouble loading from stream");
            System.out.println(filetype);
            throw ex;
        }
        this.compressedSize = computeCompressedSize(this.inBuffer);
    }

    /**
     * Constructs a new instance of the FiletypeExample for a file of unknown
     * type.
     *
     * @param filename The filename of the file containing the instance.
     * @throws IOException
     */
    public FiletypeExample(InputStream fileStream) throws IOException {
        this(fileStream, "UNKNOWN");
    }

    /**
     * Computes the Normalized Compression Distance (NCD)
     *
     * @param other The other instance.
     * @return NCD
     * @throws IOException
     */
    public double NCD(FiletypeExample other) throws IOException {
        double Cxy = computeCompressedCombinedSize(other);
        double Cyx = other.computeCompressedCombinedSize(this);
        double ncd = (Math.min(Cyx, Cxy) - Math.min(compressedSize, other.compressedSize)) / Math.max(compressedSize, other.compressedSize);

        return ncd;
    }

    /**
     * Compression-based Dissimilarity Measure
     *
     * @param other The other instance
     * @return CDM
     * @throws IOException
     */
    public double CDM(FiletypeExample other) throws IOException {
        double Cxy = computeCompressedCombinedSize(other);
        double Cyx = other.computeCompressedCombinedSize(this);
        double cdm = Math.min(Cyx, Cxy) / (compressedSize + other.compressedSize);

        return cdm;

    }

    protected int computeCompressedCombinedSize(FiletypeExample other) throws IOException {

        byte[] outBuffer = new byte[16384]; // [1024];
        int temp;

        Deflater defl1 = new Deflater(COMPRESSION_LEVEL);
//            defl1.setStrategy(DEFLATE_STRATEGY);

        int totalSize = 0;

        defl1.setInput(inBuffer);

        while (!defl1.needsInput()) {
            temp = defl1.deflate(outBuffer);
            totalSize += temp;
        }

        defl1.setInput(other.inBuffer);
        defl1.finish();
        while (!defl1.needsInput()) {
            temp = defl1.deflate(outBuffer);
            totalSize += temp;
        }
        return totalSize;
    }

    private static int computeCompressedSize(byte[] buff) throws IOException {

        byte[] outBuffer = new byte[16384]; // [1024];

        //TODO: test this with too-small outBuffers

        int compBytes;

        Deflater defl1 = new Deflater(COMPRESSION_LEVEL);
//            defl1.setStrategy(DEFLATE_STRATEGY);
        defl1.setInput(buff);

        defl1.finish();

        int size = defl1.deflate(outBuffer);

        return size;
    }

    /**
     * Gets the compressed size of this filetype example.
     *
     * @return the compressedSize
     */
    public int getCompressedSize() {
        return compressedSize;
    }

    /**
     * Gets the filetype for this filetype instance.
     *
     * @return the filetype
     */
    public String getFiletype() {
        return filetype;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "(FiletypeExample\ncompressedSize="
                + compressedSize + "\nfiletype=" + filetype + "\n)";
    }

    private static byte[] loadData(InputStream in) throws IOException {
        final byte[] tempBuffer = new byte[FILE_HEADER_SIZE];
        int totalBytes = 0;
        int bytesRead = 0;

        while (bytesRead != -1 && totalBytes < FILE_HEADER_SIZE) {
//                        bytesRead = in.read(tempBuffer);

            bytesRead = in.read(tempBuffer, totalBytes, FILE_HEADER_SIZE - totalBytes);
            if (bytesRead != -1) {
                totalBytes += bytesRead;
            }
        }
        in.close();
        if (totalBytes == 0) {
            throw new IOException("No bytes read from stream");

        }

        if (totalBytes == FILE_HEADER_SIZE) {
            return tempBuffer;
        }

        final byte[] result = new byte[totalBytes];
        System.out.print(tempBuffer.length + " " + result.length + " " + totalBytes);
        System.arraycopy(tempBuffer, 0, result, 0, totalBytes);
        return result;
    }
}
