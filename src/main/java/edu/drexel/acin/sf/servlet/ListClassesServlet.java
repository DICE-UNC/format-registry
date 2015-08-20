package edu.drexel.acin.sf.servlet;

import edu.drexel.acin.sf.api.ClassDescriptor;
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
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;


/**
 * Created by IntelliJ IDEA.
 * User: ids
 * Date: 2/16/12
 * Time: 12:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class ListClassesServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(ListClassesServlet.class.getName());

    public static final String TEMPLATE_RESOURCE = "/velocity/ListClassesTemplate.vm";

    public static final URI FORMAT_URI = URI.create("http://www.nationaldesignrepository.org/ontologies/formatregistry.owl#FileFormat");

    private final Forms forms;
    private final VelocityHelper velocity;

    public ListClassesServlet() throws Exception {
        this.forms = Resources.INSTANCE.forms;
        this.velocity = new VelocityHelper(TEMPLATE_RESOURCE);
    }

    private VelocityContext buildContext() throws ServletException {
        final VelocityContext context = new VelocityContext();
        context.put("URLEncoder", URLEncoder.class);

        final Set<ClassDescriptor> formatClasses = new TreeSet<ClassDescriptor>(forms.getOntology().withSubclasses(FORMAT_URI));
        final Set<ClassDescriptor> otherClasses = new TreeSet<ClassDescriptor>(forms.getAllClasses());
        otherClasses.removeAll(formatClasses);

        context.put("formatClassDescriptors", formatClasses);
        context.put("otherClassDescriptors", otherClasses);

        context.put("CLASS_URI_PARAMETER", ListEntitiesServlet.CLASS_URI_PARAMETER);

        return context;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        final String jsonString = request.getParameter(Constants.JSON_REQUEST_PARAM);
        final Writer writer = response.getWriter();
        try {
            if ("true".equals(jsonString)) {
                response.setContentType("application/json");
                JSON.write(forms.getAllClasses(), response.getWriter());
            } else {
                response.setContentType("text/html");
                final VelocityContext context = buildContext();
                velocity.writeToResponse(writer, context);
            }
        } finally {
            writer.close();
        }
    }
}

