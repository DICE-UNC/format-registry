/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.drexel.acin.identifier;

import edu.drexel.acin.sf.api.FileStorage;
import edu.drexel.acin.sf.db.PostgresStorage;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Set;

/**
 * @author omodolu95
 */
public class ClassifierTrainer {

    public static void train(FiletypeClassifier classifier, PostgresStorage metaStorage, FileStorage storage) throws IOException, URISyntaxException, SQLException {
        Set<URI> entities = metaStorage.listClassEntities(URI.create("http://www.nationaldesignrepository.org/ontologies/formatregistry.owl#EngineeringFileFormat")).keySet();

        int count;
        for (URI entityUri : entities) {
            count = 0;

            //TODO: use metaStorage to pull "hasSample" records
//            for (String fileName : storage.listFiles(entityUri)) {
//                if (count++ > 5) {
//                    break; //Only take the first five examples for any given type
//                }
//                classifier.addToClassifier(new FiletypeExample(storage.getFile(entityUri, fileName), entityUri.toString()));
//            }
        }
    }
}
