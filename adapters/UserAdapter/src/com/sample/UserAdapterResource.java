/*
 *
    COPYRIGHT LICENSE: This information contains sample code provided in source code form. You may copy, modify, and distribute
    these sample programs in any form without payment to IBM® for the purposes of developing, using, marketing or distributing
    application programs conforming to the application programming interface for the operating platform for which the sample code is written.
    Notwithstanding anything to the contrary, IBM PROVIDES THE SAMPLE SOURCE CODE ON AN "AS IS" BASIS AND IBM DISCLAIMS ALL WARRANTIES,
    EXPRESS OR IMPLIED, INCLUDING, BUT NOT LIMITED TO, ANY IMPLIED WARRANTIES OR CONDITIONS OF MERCHANTABILITY, SATISFACTORY QUALITY,
    FITNESS FOR A PARTICULAR PURPOSE, TITLE, AND ANY WARRANTY OR CONDITION OF NON-INFRINGEMENT. IBM SHALL NOT BE LIABLE FOR ANY DIRECT,
    INDIRECT, INCIDENTAL, SPECIAL OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OR OPERATION OF THE SAMPLE SOURCE CODE.
    IBM HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS OR MODIFICATIONS TO THE SAMPLE SOURCE CODE.

 */

package com.sample;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;
import com.worklight.adapters.rest.api.WLServerAPI;
import com.worklight.adapters.rest.api.WLServerAPIProvider;


@Path("/")
public class UserAdapterResource {
	/*
	 * For more info on JAX-RS see https://jsr311.java.net/nonav/releases/1.1/index.html
	 */
	
	//Define logger (Standard java.util.Logger)
    static Logger logger = Logger.getLogger(UserAdapterResource.class.getName());

    //Define the server api to be able to perform server operations
    WLServerAPI api = WLServerAPIProvider.getWLServerAPI();
    
    static DataSource ds = null;
    static Context ctx = null;

    
    public static void init() throws NamingException {
    	ctx = new InitialContext();
    	
    	//The JDBC configuration is inside the server.xml
    	//Liberty will handle connection pooling for us.
        ds = (DataSource)ctx.lookup("jdbc/mobilefirst_training");
    }

	@POST
	public Response createUser(@FormParam("userId") String userId, 
								@FormParam("firstName") String firstName, 
								@FormParam("lastName") String lastName, 
								@FormParam("password") String password) 
										throws SQLException{
		
		Connection con = ds.getConnection();
		PreparedStatement insertUser = con.prepareStatement("INSERT INTO users (userId, firstName, lastName, password) VALUES (?,?,?,?)");
		
		try{
			insertUser.setString(1, userId);
			insertUser.setString(2, firstName);
			insertUser.setString(3, lastName);
			insertUser.setString(4, password);
			insertUser.executeUpdate();
			//Return a 200 OK
			return Response.ok().build();
		}
		catch (SQLIntegrityConstraintViolationException violation) {
			//Trying to create a user that already exists
			return Response.status(Status.CONFLICT).entity(violation.getMessage()).build();
		}
		finally{
			//Close resources in all cases
			insertUser.close();
			con.close();
		}

		
	}
	
	@GET
	@Produces("application/json")
	@Path("/{userId}")
	public Response getUser(@PathParam("userId") String userId) throws SQLException{
		Connection con = ds.getConnection();
		PreparedStatement getUser = con.prepareStatement("SELECT * FROM users WHERE userId = ?");

		try{
			JSONObject result = new JSONObject();

			getUser.setString(1, userId);
			ResultSet data = getUser.executeQuery();
			
			if(data.first()){
				result.put("userId", data.getString("userId"));
				result.put("firstName", data.getString("firstName"));
				result.put("lastName", data.getString("lastName"));
				result.put("password", data.getString("password"));
				return Response.ok(result).build();

			} else{
				return Response.status(Status.NOT_FOUND).entity("User not found...").build();
			}
		
		}
		finally{
			//Close resources in all cases
			getUser.close();
			con.close();
		}
		
	}
	
	@GET
	@Produces("application/json")
	public Response getAllUsers() throws SQLException{
		JSONArray results = new JSONArray();
		Connection con = ds.getConnection();
		PreparedStatement getAllUsers = con.prepareStatement("SELECT * FROM users");
		ResultSet data = getAllUsers.executeQuery();
		
		while(data.next()){
			JSONObject item = new JSONObject();
			item.put("userId", data.getString("userId"));
			item.put("firstName", data.getString("firstName"));
			item.put("lastName", data.getString("lastName"));
			item.put("password", data.getString("password"));
			
			results.add(item);
		}
		
		getAllUsers.close();
		con.close();
		
		return Response.ok(results).build();
	}
	
	@PUT
	@Path("/{userId}")
	public Response updateUser(@PathParam("userId") String userId, 
								@FormParam("firstName") String firstName, 
								@FormParam("lastName") String lastName, 
								@FormParam("password") String password) 
										throws SQLException{
		Connection con = ds.getConnection();
		PreparedStatement getUser = con.prepareStatement("SELECT * FROM users WHERE userId = ?");
		
		try{
			getUser.setString(1, userId);
			ResultSet data = getUser.executeQuery();
			
			if(data.first()){
				PreparedStatement updateUser = con.prepareStatement("UPDATE users SET firstName = ?, lastName = ?, password = ? WHERE userId = ?");
				
				updateUser.setString(1, firstName);
				updateUser.setString(2, lastName);
				updateUser.setString(3, password);
				updateUser.setString(4, userId);
				
				updateUser.executeUpdate();
				updateUser.close();
				return Response.ok().build();

							
			} else{
				return Response.status(Status.NOT_FOUND).entity("User not found...").build();
			}
		}
		finally{
			//Close resources in all cases
			getUser.close();
			con.close();
		}
		
	}
	
	@DELETE
	@Path("/{userId}")
	public Response deleteUser(@PathParam("userId") String userId) throws SQLException{
		Connection con = ds.getConnection();
		PreparedStatement getUser = con.prepareStatement("SELECT * FROM users WHERE userId = ?");
		
		try{
			getUser.setString(1, userId);
			ResultSet data = getUser.executeQuery();
			
			if(data.first()){
				PreparedStatement deleteUser = con.prepareStatement("DELETE FROM users WHERE userId = ?");
				deleteUser.setString(1, userId);
				deleteUser.executeUpdate();
				deleteUser.close();
				return Response.ok().build();
							
			} else{
				return Response.status(Status.NOT_FOUND).entity("User not found...").build();
			}
		}
		finally{
			//Close resources in all cases
			getUser.close();
			con.close();
		}
		
	}
	
}
