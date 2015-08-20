package edu.drexel.acin.sf.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by IntelliJ IDEA. User: ids Date: 2/16/12 Time: 12:23 PM To change
 * this template use File | Settings | File Templates.
 */
public class FileIdentificationServlet extends HttpServlet {

    //TODO: this doesn't need to be a servlet

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try (PrintWriter writer = response.getWriter()) {
            writer.println("<!DOCTYPE html><html><head><title> File Identifier</title><link rel=\"stylesheet\" type=\"text/css\" href=\"css/bootstrap.css\"/>"
                    + "\n" + "<link rel=\"stylesheet\" type=\"text/css\" href=\"css/bootstrap-responsive.css\"/></head><body><form method='post' enctype='multipart/form-data' action='/identify-results'>");
            writer.println("<div class='well'><h1> Identify File</h1>");

            writer.println("File" + "<input type='file' name='myfile'/><br/>");
            writer.println("<input type = 'submit' name = 'submit' value =Submit /></div></form></body></html>");
        }
    }
}
