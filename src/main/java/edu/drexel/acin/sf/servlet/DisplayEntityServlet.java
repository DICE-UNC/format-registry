package edu.drexel.acin.sf.servlet;

import edu.drexel.acin.sf.api.FormSubmit;
import edu.drexel.acin.sf.api.Forms;
import edu.drexel.acin.sf.util.Constants;
import edu.drexel.acin.sf.util.JSON;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.velocity.VelocityContext;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.query.JargonQueryException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: ids
 * Date: 2/17/12
 * Time: 1:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class DisplayEntityServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(DisplayEntityServlet.class.getName());

    public static final String TEMPLATE_RESOURCE = "/velocity/DisplayEntityTemplate.vm";
    public static final String CLASS_URI_PARAMETER = "class";
    public static final String ENTITY_URI_PARAMETER = "entity";

    private final Forms forms;
    private final VelocityHelper velocity;

    public DisplayEntityServlet() {
        this.forms = Resources.INSTANCE.forms;
        this.velocity = new VelocityHelper(TEMPLATE_RESOURCE);
    }

    private VelocityContext buildContext(URI entityUri) throws ServletException {
        final VelocityContext context = new VelocityContext();
        context.put("URLEncoder", URLEncoder.class);
        context.put("entityUri", entityUri);
        return context;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final String entityString = request.getParameter(ENTITY_URI_PARAMETER);
        if (entityString == null || entityString.isEmpty()) {
            throw new IllegalArgumentException();
        }

        final URI entityUri = URI.create(entityString);

        final String jsonString = request.getParameter(Constants.JSON_REQUEST_PARAM);
        final Writer writer = response.getWriter();
        try {
            if ("true".equals(jsonString)) {
                response.setContentType("application/json");
                JSON.write(forms.getForm(entityUri), writer);
            } else {
                response.setContentType("text/html");
                final VelocityContext context = buildContext(entityUri);
                velocity.writeToResponse(writer, context);
            }
        } catch (SQLException ex) {
            throw new ServletException("Trouble saving form results", ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final String entityString = request.getParameter(ENTITY_URI_PARAMETER);
        if (entityString == null || entityString.isEmpty()) {
            throw new IllegalArgumentException();
        }

        final URI entityUri = URI.create(entityString);

        final Writer writer = response.getWriter();
        try {
            FormSubmit form;

            if (ServletFileUpload.isMultipartContent(request)) {
                final ServletFileUpload upload = new ServletFileUpload();
                try {
                    final FileItemIterator iter = upload.getItemIterator(request);
                    while (iter.hasNext()) {
                        final FileItemStream item = iter.next();
                        final String itemFieldName = item.getFieldName();
                        if ("formData".equals(itemFieldName)) {
                            form = JSON.parse(item.openStream(), FormSubmit.class);
                            try {
                                forms.updateEntity(form);
                            } catch (SQLException ex) {
                                logger.log(Level.SEVERE, "Trouble saving submitted form data", ex);
                                throw ex;
                            }
                        } else if (itemFieldName.startsWith("file:")) {
                            final String fileName = itemFieldName.substring(5);
                            forms.saveSample(entityUri, fileName, item.openStream());
                        }
                    }
                } catch (FileUploadException ex) {
                    throw new ServletException(ex);
                }
            }

            response.setContentType("application/json");
            writer.write("{\"ok\": true}");
        } catch (SQLException ex) {
            throw new ServletException("Trouble saving form results", ex);
        }
    }
}
