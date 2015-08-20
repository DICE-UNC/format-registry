package edu.drexel.acin.sf.irods;

import edu.drexel.acin.sf.api.FileStorage;
import edu.drexel.acin.sf.util.ConfigReader;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.*;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: ids
 * Date: 12/5/11
 * Time: 3:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class IRODSStorage extends FileStorage {
    private static final Logger logger = Logger.getLogger(IRODSStorage.class.getName());

    private final IRODSFileSystem fileSystem = IRODSFileSystem.instance();
    private final IRODSAccessObjectFactory accessFactory = fileSystem.getIRODSAccessObjectFactory();
    private final IRODSAccount account;

    private final String irodsPathPrefix;
    //TODO: is this redundant with the home directory?

    public IRODSStorage(Properties props) throws IOException, JargonException {
        this(props.getProperty(ConfigReader.RODS_USERNAME_KEY),
                props.getProperty(ConfigReader.RODS_PASSWORD_KEY),
                props.getProperty(ConfigReader.RODS_ZONE_KEY),
                props.getProperty(ConfigReader.RODS_RESOURCE_KEY),
                props.getProperty(ConfigReader.RODS_HOST_KEY),
                Integer.parseInt(props.getProperty(ConfigReader.RODS_PORT_KEY)),
                props.getProperty(ConfigReader.RODS_HOMEDIR_KEY),
                props.getProperty(ConfigReader.COLLECTION_KEY));
    }

    public IRODSStorage(String username, String password, String zone, String resource, String host,
                        int port, String home, String irodsPathPrefix) throws JargonException, IOException {
        this.account = new IRODSAccount(host, port, username, password, home, zone, resource);
        accessFactory.authenticateIRODSAccount(account);
        this.irodsPathPrefix = irodsPathPrefix;
        setupCollection(irodsPathPrefix);
    }

    private CollectionAO getCollectionAO() throws JargonException {
        return this.accessFactory.getCollectionAO(this.account);
    }

    private DataObjectAO getDataObjectAO() throws JargonException {
        return this.accessFactory.getDataObjectAO(this.account);
    }

    private Stream2StreamAO getStream2StreamAO() throws JargonException {
        return this.accessFactory.getStream2StreamAO(this.account);
    }

    private IRODSFileFactory getFileFactory() throws JargonException {
        return this.accessFactory.getIRODSFileFactory(this.account);
    }

    private boolean setupCollection(String path) throws JargonException {
        final IRODSFile collectionFile = getCollectionAO().instanceIRODSFileForCollectionPath(path);
        return setupCollection(collectionFile);
    }

    private boolean setupCollection(IRODSFile file) {
        if (file.exists()) {
            return true;
        }
        logger.log(Level.INFO, "Creating collection: " + file.getAbsolutePath());
        return file.mkdirs();
    }

    @Override
    public void deleteFile(String rodsPath) throws IOException {
        final IRODSFile rodsFile;
        try {
            rodsFile = getDataObjectAO().instanceIRODSFileForPath(rodsPath);
        } catch (JargonException e) {
            throw new IOException(e);
        }
        rodsFile.delete();
    }

    public void shutdown() {
        try {
            fileSystem.close();
        } catch (JargonException ex) {
            logger.log(Level.WARNING, "Trouble closing IRODSFileSystem", ex);
        }
    }

    public boolean deleteCollection(String path) throws JargonException {
        final IRODSFile file = getCollectionAO().instanceIRODSFileForCollectionPath(path);
        return deleteCollection(file);
    }

    private boolean deleteCollection(IRODSFile file) throws JargonException {
        return file.exists() && file.delete();
    }

    public List<String> listFiles(String path) throws JargonException {
        final List<String> filenames = new ArrayList<>();
        final IRODSFile collection = getFileFactory().instanceIRODSFile(path);
        for (File file : collection.listFiles()) {
            filenames.add(file.getName());
        }
        return filenames;
    }

    @Override
    protected String getBasePath() {
        return irodsPathPrefix;
    }

    @Override
    protected void createDirectoryIfNotExists(String path) throws IOException {
        try {
            setupCollection(path);
        } catch (JargonException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    protected void saveFile(String path, InputStream in) throws IOException {
        try {
            final OutputStream out = getFileFactory().instanceIRODSFileOutputStream(path);
            getStream2StreamAO().streamToStreamCopy(in, out);
        } catch (JargonException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    protected void getFile(String path, OutputStream out) throws IOException {
        try {
            final InputStream in = getFileFactory().instanceIRODSFileInputStream(path);
            getStream2StreamAO().streamToStreamCopy(in, out);
        } catch (JargonException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    protected InputStream getFile(String path) throws IOException {
        try {
            return getFileFactory().instanceIRODSFileInputStream(path);
        } catch (JargonException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    protected void deleteEntity(String path) throws IOException {
        try {
            for (String filename: listFiles(path)) {
                deleteFile(path + getPathSeparator() + filename);
            }
            deleteCollection(path);
        } catch (JargonException ex) {
            throw new IOException(ex);
        }
    }
}
