package edu.drexel.acin.sf.servlet;

import com.google.gson.reflect.TypeToken;
import edu.drexel.acin.sf.api.Forms;
import edu.drexel.acin.sf.util.JSON;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.query.JargonQueryException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.URI;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: ids
 * Date: 7/3/12
 * Time: 9:36 AM
 * To change this template use File | Settings | File Templates.
 */
public class NewEntityServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(NewEntityServlet.class.getName());

    private static final Type STRING_HASHMAP = new TypeToken<HashMap<String, String>>() {
    }.getType();

    private final Forms forms;

    public NewEntityServlet() {
        this.forms = Resources.INSTANCE.forms;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        final String jsonString = request.getParameter("formData");
        final Map<String, String> post = JSON.parse(jsonString, STRING_HASHMAP);

        final String uriString = post.get(DisplayEntityServlet.CLASS_URI_PARAMETER);
        if (uriString == null || uriString.isEmpty()) {
            throw new IllegalArgumentException("Missing class URI");
        }

        final URI classUri = URI.create(uriString);

        final String entityId = post.get("entity_id");
        if (entityId == null || entityId.isEmpty()) {
            throw new IllegalArgumentException("Missing entity identifier");
        }
        final URI entityUri = URI.create(entityId);

        final String entityLabel = post.get("entity_name");
        if (entityLabel == null || entityLabel.isEmpty()) {
            throw new IllegalArgumentException("Missing entity name");
        }

        try {
            forms.createNewEntity(classUri, entityUri, entityLabel);
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Trouble creating new entity", ex);
        }

        final Map<String, Object> res = new HashMap<String, Object>(3);
        res.put("id", entityId);
        res.put("name", entityLabel);
        res.put("created", Boolean.TRUE);
        try (PrintWriter writer = response.getWriter()) {
            JSON.write(res, writer);
        }
    }
}
