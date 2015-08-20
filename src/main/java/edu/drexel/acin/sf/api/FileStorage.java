package edu.drexel.acin.sf.api;

import java.io.*;
import java.math.BigInteger;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Created by Isaac Simmons on 9/23/2014.
 */
public abstract class FileStorage {

    protected abstract String getBasePath();
    protected String getPathSeparator() {
        return "/";
    }

    private String getEntityPath(URI uri) {
        final MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("md5 digest not supported");
        }
        byte[] md5sum = digest.digest(uri.toString().getBytes());
        String dirName = String.format("%032X", new BigInteger(1, md5sum));

        return getBasePath() + getPathSeparator() + dirName;
    }

    private String getFilePath(URI uri, String filename) {
        return getEntityPath(uri) + getPathSeparator() + filename;
    }

    public void saveFile(URI uri, String filename, InputStream content) throws IOException {
        createDirectoryIfNotExists(getEntityPath(uri));
        saveFile(getFilePath(uri, filename), content);
    }

    protected abstract void saveFile(String path, InputStream content) throws IOException;

    public void deleteFile(URI uri, String filename) throws IOException {
        deleteFile(getFilePath(uri, filename));
    }

    protected abstract void createDirectoryIfNotExists(String path) throws IOException;

    protected abstract void deleteFile(String path) throws IOException;

    public void getFile(URI uri, String filename, OutputStream out) throws IOException {
        getFile(getFilePath(uri, filename), out);
    }

    protected abstract void getFile(String path, OutputStream out) throws IOException;

    public InputStream getFile(URI uri, String filename) throws IOException {
        return getFile(getFilePath(uri, filename));
    }

    protected abstract InputStream getFile(String path) throws IOException;

    public void deleteEntity(URI uri) throws IOException {
        deleteEntity(getEntityPath(uri));
    }

    protected abstract void deleteEntity(String path) throws IOException;
    //TODO: make sure implementing classes can deal with non-empty directories
}
