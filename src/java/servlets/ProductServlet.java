/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlets;

import credentials.Credentials;
import static credentials.Credentials.getConnection;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Dylan Huculak - c0630163
 */
@WebServlet(name = "ProductServlet", urlPatterns = {"/products"})
public class ProductServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
            response.setHeader("Content-Type", "text/plain-text");
            try (PrintWriter out = response.getWriter()) {
                if (!request.getParameterNames().hasMoreElements()){
                    out.println(getResults("SELECT * FROM products"));
                } else {
                    String id = request.getParameter("id");
                    out.println(getResults("SELECT * FROM product WHERE productId = ?", id));  
                }
            } catch (IOException ex){
                 Logger.getLogger(ProductServlet.class.getName()).log(Level.SEVERE, null, ex);
            }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Set<String> keySet = request.getParameterMap().keySet();
        try (PrintWriter out = response.getWriter()) {
            Connection conn = getConnection();
            if (keySet.contains("name") && keySet.contains("description") && keySet.contains("quantity")) {
                String name = request.getParameter("name");
                String description = request.getParameter("description");
                String quantity = request.getParameter("quantity");
                PreparedStatement pstmt = conn.prepareStatement("INSERT INTO products ('name', 'description', 'quantity') VALUES ('"
                    + name + "', '"
                    + description + "', '"
                    + quantity + "');");
                try {
                    pstmt.executeUpdate();
                    out.println("http://localhost:8080/Assignment-3/products/" + request.getParameter("id"));
                } catch (SQLException ex) {
                    Logger.getLogger(ProductServlet.class.getName()).log(Level.SEVERE, null, ex);
                    out.println("Error: problem inserting values.");
                    response.setStatus(500);
                }
            }
            else {
                out.println("Error: Cannot post. Insufficient data.");
                response.setStatus(500);
            }
        } catch (SQLException ex) {
            Logger.getLogger(ProductServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        Set<String> keySet = request.getParameterMap().keySet();
        try (PrintWriter out = response.getWriter()) {
            Connection conn = getConnection();
            if (keySet.contains("id") && keySet.contains("name") && keySet.contains("description") && keySet.contains("quantity")) {
                String productid = request.getParameter("id");
                String name = request.getParameter("name");
                String description = request.getParameter("description");
                String quantity = request.getParameter("quantity");
                PreparedStatement pstmt = conn.prepareStatement("UPDATE product SET name='" 
                    + name + "', description'"
                    + description + "', quantity'"
                    + quantity + "' WHERE productId = '"
                    + productid + "';");
                try {
                    pstmt.executeUpdate();
                    out.println("http://localhost:8080/Assignment-3/products/" + request.getParameter("id"));
                } catch (SQLException ex) {
                    Logger.getLogger(ProductServlet.class.getName()).log(Level.SEVERE, null, ex);
                    out.println("Error: cannot update values.");
                    response.setStatus(500);
                }
            } else {
                out.println("Error: insufficient parameters for update.");
                response.setStatus(500);
            }
        } catch (SQLException ex) {
            Logger.getLogger(ProductServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    private String getResults(String query, String... params){
        StringBuilder sb = new StringBuilder();
        try (Connection conn = Credentials.getConnection()){
            PreparedStatement pstmt = conn.prepareStatement(query);
            for (int i = 1; i <= params.length; i++) {
                pstmt.setString(i, params[i - 1]);
            }
            ResultSet rs = pstmt.executeQuery();
            sb.append("[ ");
            while (rs.next()) {
                sb.append(String.format("{ \"productId\" : %s, \"name\" : %s, \"description\" : %s, \"quantity\" : %s },\n", rs.getInt("productId"), rs.getString("name"), rs.getString("description"), rs.getInt("quantity")));
            }
            sb.setLength(Math.max(sb.length() - 2, 0));
            sb.append("]");
        } catch (SQLException ex) {
            Logger.getLogger(ProductServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        return sb.toString();
    }
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Set<String> keySet = request.getParameterMap().keySet();
        try (PrintWriter out = response.getWriter()) {
            Connection conn = getConnection();
            if (keySet.contains("id")) {
                PreparedStatement pstmt = conn.prepareStatement("DELETE FROM products WHERE productId = " + request.getParameter("productId"));
                try {
                    pstmt.executeUpdate();
                } catch (SQLException ex) {
                    Logger.getLogger(ProductServlet.class.getName()).log(Level.SEVERE, null, ex);
                    out.println("Error deleting entry");
                    response.setStatus(500);
                }
            } else {
                out.println("No data to delete");
                response.setStatus(500);
            }
        }
        catch (SQLException ex) {
            Logger.getLogger(ProductServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
            
}
