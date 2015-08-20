package edu.drexel.acin.sf.servlet;

import edu.drexel.acin.sf.api.Forms;
import edu.drexel.acin.sf.util.Constants;
import edu.drexel.acin.sf.util.JSON;
import org.apache.velocity.VelocityContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: ids
 * Date: 2/16/12
 * Time: 12:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class ListEntitiesServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(ListEntitiesServlet.class.getName());

    public static final String CLASS_URI_PARAMETER = "class";
    public static final String TEMPLATE_RESOURCE = "/velocity/ListEntitiesTemplate.vm";

    private final Forms forms;
    private final VelocityHelper velocity;

    public ListEntitiesServlet() {
        this.forms = Resources.INSTANCE.forms;
        this.velocity = new VelocityHelper(TEMPLATE_RESOURCE);
    }

    private VelocityContext buildContext(URI classUri) throws ServletException, SQLException {
        final VelocityContext context = new VelocityContext();
        context.put("forms", forms);
        context.put("URLEncoder", URLEncoder.class);

        context.put("ENTITY_URI_PARAMETER", DisplayEntityServlet.ENTITY_URI_PARAMETER);
        context.put("CLASS_URI_PARAMETER", DisplayEntityServlet.CLASS_URI_PARAMETER);
        context.put("entities", forms.getAllEntities(classUri));
        context.put("classDescriptor", forms.getClassDescriptor(classUri));

        return context;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final String uriString = request.getParameter(CLASS_URI_PARAMETER);
        if (uriString == null || uriString.isEmpty()) {
            throw new IllegalArgumentException();
            //TODO: nice error page
        }

        final URI classUri = URI.create(uriString);

        final String jsonString = request.getParameter(Constants.JSON_REQUEST_PARAM);
        try (Writer writer = response.getWriter()) {
            if ("true".equals(jsonString)) {
                response.setContentType("application/json");
                JSON.write(forms.getAllEntities(classUri), writer);
            } else {
                response.setContentType("text/html");
                final VelocityContext context = buildContext(classUri);
//				System.out.println(((Map<URI, String>) context.get("entities")).size());
//				System.out.println(((ClassDescriptor) context.get("classDescriptor")));
                velocity.writeToResponse(writer, context);
            }
        } catch (SQLException ex) {
            throw new ServletException(ex);
        }
    }
}
