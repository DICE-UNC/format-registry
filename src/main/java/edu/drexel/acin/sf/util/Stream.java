package edu.drexel.acin.sf.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created with IntelliJ IDEA.
 * User: ids
 * Date: 9/12/12
 * Time: 12:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class Stream {
    public static void copyAndClose(InputStream in, OutputStream out) throws IOException {
        try {
            Stream.copy(in, out);
        } finally {
            try {
                in.close();
            } catch (IOException ignored) {
            }
            try {
                out.close();
            } catch (IOException ignored) {
            }
        }
    }

    public static void copy(InputStream in, OutputStream out) throws IOException {
        final byte[] buf = new byte[4096];
        int len;
        while ((len = in.read(buf)) != -1) {
            out.write(buf, 0, len);
        }
    }
}
