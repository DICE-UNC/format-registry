package edu.drexel.acin.sf.servlet;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;

/**
 * Created by IntelliJ IDEA.
 * User: ids
 * Date: 3/12/12
 * Time: 1:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class VelocityHelper {
    private final VelocityEngine ve = new VelocityEngine();

    private final String templateResource;

    public VelocityHelper(String templateResource) {
        this.templateResource = templateResource;
    }

    public void writeToResponse(Writer writer, VelocityContext context) throws IOException {
        ve.evaluate(
                context,
                writer,
                getClass().getName(),
                new InputStreamReader(getClass().getResourceAsStream(templateResource))
        );
    }
}
