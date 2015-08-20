package edu.drexel.acin.sf.servlet;

import edu.drexel.acin.sf.api.FileStorage;
import edu.drexel.acin.sf.api.Forms;
import edu.drexel.acin.sf.api.Ontology;
import edu.drexel.acin.sf.db.PostgresStorage;
import edu.drexel.acin.sf.file.LocalFileStorage;
import edu.drexel.acin.sf.owl.ResourceLoader;
import edu.drexel.acin.sf.util.ConfigReader;

import java.io.File;
import java.util.Properties;

import static edu.drexel.acin.sf.util.ConfigReader.*;

public enum Resources {
    INSTANCE;

    public final Forms forms;

    private Resources() {
        try {
            final Properties props = ConfigReader.getProperties();

            final Ontology ontology = ResourceLoader.load("/formatregistry.owl");
            final FileStorage fileStorage = new LocalFileStorage(new File(props.getProperty(LOCAL_PATH_KEY)));
            final PostgresStorage metaStorage = new PostgresStorage(
                    props.getProperty(DATABASE_URL_KEY),
                    props.getProperty(DATABASE_USERNAME_KEY),
                    props.getProperty(DATABASE_PASSWORD_KEY)
            );

            this.forms = new Forms(ontology, fileStorage, metaStorage);
        } catch (Exception ex) {
            throw new RuntimeException("Unable to initialize forms resource", ex);
        }
    }
}
