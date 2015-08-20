package edu.drexel.acin.sf.file;

import edu.drexel.acin.sf.api.*;
import edu.drexel.acin.sf.util.Stream;

import java.io.*;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: ids
 * Date: 9/12/12
 * Time: 10:40 AM
 * To change this template use File | Settings | File Templates.
 */
public class LocalFileStorage extends FileStorage {
    private final File baseDirectory;

    public LocalFileStorage(File baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    @Override
    protected String getBasePath() {
        return baseDirectory.getPath();
    }

    @Override
    protected void saveFile(String path, InputStream content) throws IOException {
        final File file = new File(path);
        final FileOutputStream out = new FileOutputStream(file);
        Stream.copyAndClose(content, out);
    }

    @Override
    protected void createDirectoryIfNotExists(String path) throws IOException {
        final File file = new File(path);
        if (! file.isDirectory()) {
            if (! file.mkdirs()) {
                throw new IOException("Unable to create new directory " + path);
            }
        }
    }

    @Override
    protected void deleteEntity(String path) throws IOException {
        final File file = new File(path);
        if (file.isDirectory()) {
            final File[] children = file.listFiles();
            if (children != null) {
                for (File child: children) {
                    if (!file.delete()) {
                        throw new IOException("Unable to delete child entity " + child.getPath());
                    }
                }
            }
            if (! file.delete()) {
                throw new IOException("Unable to delete entity directory " + file.getPath());
            }
        }
    }

    @Override
    public void deleteFile(String path) throws IOException{
        final File file = new File(path);
        if (file.exists() && !file.delete()) {
            throw new IOException("Unable to delete file " + path);
        }
    }

    @Override
    protected void getFile(String path, OutputStream out) throws IOException {
        final File file = new File(path);
        final FileInputStream in = new FileInputStream(file);
        Stream.copyAndClose(in, out);
    }

    @Override
    protected InputStream getFile(String path) throws IOException {
        return new FileInputStream(path);
    }
}
