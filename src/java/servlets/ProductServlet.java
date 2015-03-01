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
import java.sql.Statement;
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
 * @author Dylan
 */
@WebServlet(name = "ProductServlet", urlPatterns = {"/product"})
public class ProductServlet extends HttpServlet {
    //VARIABLES FOR MANUALLY IMPLEMENTED METHODS
    String result;
    String url;
    
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
             response.setHeader("Content-Type", "text/plain-text");
             try (PrintWriter out = response.getWriter()) {
                 if (!request.getParameterNames().hasMoreElements()){
                     out.println(getResults("SELECT * FROM product"));
                 }
                 else {
                     String id = request.getParameter("id");
                     out.println(getResults("SELECT * FROM product WHERE ProductID = ?", id));
                     
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
                PreparedStatement pstmt = conn.prepareStatement("INSERT INTO product ('Name', 'Description', 'Quantity') VALUES ('"
                    + name + "', '"
                    + description + "', '"
                    + quantity + "')");
                try {
                    pstmt.executeUpdate();
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

    
    
    
    //MANUALLY GENERATED doPut AND doDelete METHODS---------------
    String doPut(String id, String name, String desc, int quan) {
        
        return url;
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
                sb.append(String.format("{ \"productId\" : %s, \"name\" : %s, \"description\" : %s, \"quantity\" : %s },\n", rs.getInt("ProductID"), rs.getString("Name"), rs.getString("Description"), rs.getInt("Quantity")));
            }
            sb.setLength(Math.max(sb.length() - 2, 0));
            sb.append("]");
        } catch (SQLException ex) {
            Logger.getLogger(ProductServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        return sb.toString();
    }
    
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Set<String> keySet = request.getParameterMap().keySet();
        try (PrintWriter out = response.getWriter()) {
            Connection conn = getConnection();
            if (keySet.contains("productID")) {
                PreparedStatement pstmt = conn.prepareStatement("DELETE FROM product WHERE productID = " + request.getParameter("productID"));
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
