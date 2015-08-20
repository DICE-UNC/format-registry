package edu.drexel.acin.sf.servlet;

import edu.drexel.acin.identifier.FiletypeClassifier;
import edu.drexel.acin.sf.api.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.sql.SQLException;

import edu.drexel.acin.sf.db.PostgresStorage;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/**
 * Created by IntelliJ IDEA. User: ids Date: 2/16/12 Time: 12:23 PM To change
 * this template use File | Settings | File Templates.
 */
public class FileIdentificationPostServlet extends HttpServlet {
    private final PostgresStorage metaStorage;
    private final Ontology ontology;

    private final FiletypeClassifier classifier;

    public FileIdentificationPostServlet(Ontology ontology, PostgresStorage storage, FiletypeClassifier classifier) {
        this.metaStorage = storage;
        this.ontology = ontology;

        this.classifier = classifier;
    }

    private int getSize(InputStream fileStream) throws IOException {
        int total = 0;
        byte[] buffer = new byte[1024];

        int numRead;

        while ((numRead = fileStream.read(buffer)) != -1) {
            total += numRead;
        }

        return total;
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Check that we have a file upload request
        if (!ServletFileUpload.isMultipartContent(request)) {
            throw new IllegalArgumentException("No multipart content in request object");
        }
        response.setContentType("text/html");

        // Create a new file upload handler
        final ServletFileUpload upload = new ServletFileUpload();

        // Parse the request
        try (PrintWriter writer = response.getWriter()) {
            final FileItemIterator iter = upload.getItemIterator(request);
            while (iter.hasNext()) {
                final FileItemStream item = iter.next();
                if (! item.isFormField()) {
                    writer.println("<!DOCTYPE html><html><head><title> File Identifier</title><link rel=\"stylesheet\" type=\"text/css\" href=\"../css/bootstrap.css\"/>"
                            + "\n" + "<link rel=\"stylesheet\" type=\"text/css\" href=\"../css/bootstrap-responsive.css\"/></head><body>");

                    writer.println("<div class='well'><h1> File Identifier Results </h1>");
                    writer.println("<p>File " + "(" + item.getName() + ")" + " was uploaded succesfully <br />");
                    writer.println("<b>Your file is of one of the following types: </b></p><br />");
                    writer.println("<br /><ul>");

                    final String[] types = classifier.predictClass(item.openStream()).split("\t");
                    final String best = types[0];

                    final URI entityUri = URI.create(best);
                    final URI classUri = metaStorage.getEntityClass(entityUri);
                    writer.println("<li><b><a href=/edit?entity=" + best + ">" + metaStorage.getForm(entityUri, ontology.getClassDescriptor(classUri), ontology.getFields(classUri)).getName() + "</a> (Best Guess)" + "</b></li>");

                    for (int i = 1; i <= 4; i++) {
                        final String othertype = types[i];
                        final URI entityUri2 = URI.create(othertype);
                        final URI classUri2 = metaStorage.getEntityClass(entityUri2);
                        writer.println("<li><a href=/edit?entity=" + othertype + ">" + metaStorage.getForm(entityUri2, ontology.getClassDescriptor(classUri2), ontology.getFields(classUri2)).getName() + "</a></li>");
                    }
                    writer.println("</ul></div></body></html>");
                }
            }
        } catch (FileUploadException | SQLException ex) {
            throw new ServletException(ex);
        }
    }
}
