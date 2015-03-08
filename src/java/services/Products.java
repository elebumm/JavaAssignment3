/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services;

import credentials.Credentials;
import static credentials.Credentials.getConnection;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 *
 * @author Dylan Huculak - c0630163
 */
@Path("/products")
public class Products {

    /**
     * doGet that returns all rows from table when no id is given
     * @return Response from SELECT statement
     */
    @GET
    @Produces("application/json")
    public Response doGet() {
        return Response.ok(getResults("SELECT * FROM products"),
                MediaType.APPLICATION_JSON).build();   
    }
    
    /**
     * @doGet that returns row of id given
     * @param id
     * @return Response from SELECT FROM WHERE statement
     */
    @GET
    @Path("{id}")
    @Produces("application/json")
    public Response doGet(@PathParam("id") int id) {
        return Response.ok(getResults("SELECT * FROM products WHERE productId = " 
                + String.valueOf(id)),
                    MediaType.APPLICATION_JSON).build();   
    }

    /** 
     * doPost that inserts row with data given
     * @param insert
     * @return Response 500 if method completes (should not complete if INSERT successful)
     */
    @POST
    @Consumes("application/json")
    public Response doPost(String insert) {
        Response postResponse;
        int maxId = 0;
        JsonObject json = Json.createReader(new StringReader(insert))
                .readObject();
        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn
                .prepareStatement("INSERT INTO products (name,description,quantity) VALUES ("
                + "'" + json.getString("name") + "',"
                + "'" + json.getString("description") + "',"
                + String.valueOf(json.getInt("quantity")) + ")",
                    Statement.RETURN_GENERATED_KEYS);
            
                // Get highest id (autoincremented id of last row)
                Statement checkId = conn.createStatement();
                checkId.execute("SELECT MAX(productId) FROM products");
                ResultSet checkIdResults = checkId.getResultSet();
                if ( checkIdResults.next() ) {
                    maxId = checkIdResults.getInt(1);
                } 
                
                postResponse = Response.ok("http://localhost:8080/Assignment-3/products/" + String.valueOf(maxId) ).build();
        } catch (SQLException ex) {
            Logger.getLogger(Products.class.getName())
                    .log(Level.SEVERE, null, ex);
            postResponse = Response.status(500).build();
        }
        
        return postResponse;
    }
    
    /**
     * doPut method that updates row of id given with data given
     * @param id
     * @param update
     * @return Response 500 if method completes (should not complete if INSERT successful)
     */
    @PUT
    @Path("{id}")
    @Consumes("application/json")
    public Response doPut(@PathParam("productId") int id, String update) {
        JsonObject json = Json.createReader(new StringReader(update))
                .readObject();
        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement("UPDATE products SET name='" 
                + json.getString("name") + "'," + " description='" 
                + json.getString("description") + "'," + " quantity=" 
                + String.valueOf(json.getInt("quantity")) + "," + " WHERE productId = '" 
                + id + "'", Statement.RETURN_GENERATED_KEYS);

        } catch (SQLException ex) {
            Logger.getLogger(Products.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
        
        return Response.status(500).build();
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
            Logger.getLogger(Products.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
        return sb.toString();
    }
    
    /**
     * doDelete method that deletes row of id given
     * @param id
     * @return empty string for a successful deletion
     */
    @DELETE
    @Path("{id}")
    @Consumes("application/json")
    public Response doDelete(@PathParam("productId") int id) {
        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn
                .prepareStatement("DELETE FROM products WHERE productId = " 
                    + String.valueOf(id)); 
            try {
                pstmt.executeUpdate();
            } catch (SQLException ex) {
                Logger.getLogger(Products.class.getName())
                    .log(Level.SEVERE, null, ex);
                return Response.status(500).build();
            }
        }
        catch (SQLException ex) {
            Logger.getLogger(Products.class.getName())
                .log(Level.SEVERE, null, ex);
        }
        return Response.status(Status.OK).entity("").build();
    }
            
}
