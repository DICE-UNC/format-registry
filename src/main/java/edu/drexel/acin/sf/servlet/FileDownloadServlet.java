package edu.drexel.acin.sf.servlet;

import edu.drexel.acin.sf.api.Forms;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.query.JargonQueryException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;

/**
 * Created with IntelliJ IDEA.
 * User: ids
 * Date: 7/26/12
 * Time: 3:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class FileDownloadServlet extends HttpServlet {
    private final Forms forms;
    public static final String ENTITY_URI_PARAMETER = "entityUri";
    public static final String FILENAME_PARAMETER = "filename";

    public FileDownloadServlet() {
        this.forms = Resources.INSTANCE.forms;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final String uriString = request.getParameter(ENTITY_URI_PARAMETER);
        if (uriString == null || uriString.isEmpty()) {
            throw new IllegalArgumentException();
        }
        final URI entityUri = URI.create(uriString);

        final String filename = request.getParameter(FILENAME_PARAMETER);
        if (filename == null || filename.isEmpty()) {
            throw new IllegalArgumentException();
        }

        response.setHeader("Content-disposition", "attachment; filename=" + filename);
        //Filename was specified
        try {
            forms.streamSample(entityUri, filename, response.getOutputStream());
        } catch (IOException ex) {
            throw new ServletException(ex);
        }
    }
}
