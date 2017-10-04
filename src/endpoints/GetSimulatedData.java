package endpoints;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.concurrent.Future;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


@Path("/getSimulatedData")
public class GetSimulatedData {
	@Context
	UriInfo ui;
	private static DataSource condata = null;
	private static Connection cndata;
	public static final String err_unknown = "ERROR ";
	public static final String err_dbconnect = "Cannot connect to database Please Try Again Later.";
	public static final String err_cr = "Cannot connect to common repository";

	/**
	 * Builds a matrix where the rows are all the rules in the selected design
	 * project and the columns are the design projects with those rules. Still need
	 * to include the polarity value of each rule.
	 * 
	 * @return - a JSON string with the lean rule matrix
	 * @throws JSONException
	 * @throws SQLException
	 */
	@GET
	@Produces(MediaType.TEXT_HTML)
	public Response welcome() throws JSONException, SQLException {
		String query = "SELECT sentimentposts.post.*, sentimentposts.user.*  FROM sentimentposts.post inner join sentimentposts.user on sentimentposts.post.user_id=sentimentposts.user.id and sentimentposts.post.post_id is null";
		//String response = "";
		Long postid;
		JSONArray data = new JSONArray();
		JSONArray replies = new JSONArray();
		JSONObject post = new JSONObject();
		JSONObject reply = new JSONObject();

		try {
			cndata = connlocal();
		} catch (ClassNotFoundException | SQLException e2) {
			e2.printStackTrace();
		}
		try (Statement stmt = cndata.createStatement()) {
			try (ResultSet rs = stmt.executeQuery(query)) {

				if (rs.isClosed())
					return Response.status(Response.Status.BAD_REQUEST).entity("No Simulated Posts").build();
				while (rs.next()) {
					postid = rs.getLong("id");
					// response += "[{\"postId\":" + rs.getLong("id") + ",\"location\":" +
					// rs.getString("location")
					// + ",\"postEpoch\":" + rs.getTimestamp("timestamp") + ",\"post\":\""
					// + rs.getString("message") + "\",\"url\":\"\"" + ",\"userId\":" +
					// rs.getString("user_id")
					// + ",\"replies\":";
					post = new JSONObject();
					post.put("postId", rs.getLong("id"));
					post.put("location", rs.getString("location"));
					SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					java.util.Date date = null;
					try {
						date = df.parse(rs.getString("timestamp"));
					} catch (ParseException e) {
						e.printStackTrace();
					}
					post.put("postEpoch", date.getTime());
					post.put("post", rs.getString("message"));
					post.put("url", "");
					post.put("userId", rs.getString("user_id"));
					post.put("Fname", rs.getString("name"));
					post.put("age", rs.getLong("age"));
					post.put("gender", rs.getString("gender"));
					post.put("likes", rs.getLong("likes"));
					post.put("views", rs.getLong("views"));



					query = "SELECT sentimentposts.post.*, sentimentposts.user.*  FROM sentimentposts.post inner join sentimentposts.user on sentimentposts.post.user_id=sentimentposts.user.id and sentimentposts.post.post_id = ?";
					try (PreparedStatement stmt2 = cndata.prepareStatement(query)) {
						stmt2.setString(1, postid.toString());
						try (ResultSet rs1 = stmt2.executeQuery()) {
							if (rs1.isClosed())
								return Response.status(Response.Status.BAD_REQUEST).entity("No Simulated Posts")
										.build();
							while (rs1.next()) {
								reply = new JSONObject();
								reply.put("postId", rs1.getLong("id"));
								reply.put("post", rs1.getString("message"));
								reply.put("userId", rs1.getLong("user_id"));
								reply.put("parentPostId", rs1.getLong("post_id"));
								reply.put("account", "Morris Ground 1");
								replies.put(reply);
							}
							post.put("replies", replies);
						}
						replies = new JSONArray();
						post.put("source", "simulated");
						post.put("account","Morris Ground 1");
						data.put(post);
					} catch (Exception e) {
						e.printStackTrace();
						System.out.println("ERROR1");
						try {
							cndata.close();
						} catch (SQLException e1) {
							e1.printStackTrace();
							System.out.println("ERROR2");
						}
					}
				}
			}
		} catch (Exception e) {
			System.out.println("ERROR3");
			e.printStackTrace();
			try {
				cndata.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
				System.out.println("ERROR4");
			}
		}

		try {
			cndata.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
			System.out.println("ERROR5");
		}

		return Response.status(Response.Status.OK).entity(data.toString()).build();

	}

	public ResultSet executeQuery1(String query) {
		try {
			cndata = connlocal();
		} catch (ClassNotFoundException | SQLException e2) {
			e2.printStackTrace();
		}
		try (Statement stmt = cndata.createStatement()) {
			try (ResultSet rs = stmt.executeQuery(query)) {
				try {
					cndata.close();
				} catch (SQLException e1) {
					System.out.println("ERROR");
				}
				return rs;
			}
		} catch (Exception e) {
			System.out.println("ERROR");
			try {
				cndata.close();
			} catch (SQLException e1) {
				System.out.println("ERROR");
			}
		}

		return null;
	}

	/**
	 * Connlocal.
	 *
	 * @return the connection
	 * @throws ClassNotFoundException
	 *             the class not found exception
	 */
	public static Connection connlocal() throws ClassNotFoundException, SQLException {
		try {

			if (condata == null)
				startconnections();

			Future<Connection> future = condata.getConnectionAsync();
			while (!future.isDone()) {
				try {
					Thread.sleep(100); // simulate work
				} catch (InterruptedException x) {
					Thread.currentThread().interrupt();
				}
			}

			return future.get(); // should return instantly
		} catch (Exception e) {
			System.out.println(err_dbconnect);
			return null;
		}
	}

	public static void startconnections() {
		PoolProperties p = new PoolProperties();
		p.setUrl("jdbc:mysql://127.0.0.1:3306/sentimentposts?autoReconnect=true&useSSL=false");
		p.setDriverClassName("com.mysql.jdbc.Driver");
		p.setUsername("diversity");
		p.setPassword("!diversity!");
		p.setJmxEnabled(true);
		p.setTestWhileIdle(false);
		p.setTestOnBorrow(true);
		p.setFairQueue(true);
		p.setValidationQuery("SELECT 1");
		p.setTestOnReturn(false);
		p.setValidationInterval(30000);
		p.setTimeBetweenEvictionRunsMillis(30000);
		p.setMaxActive(40);
		p.setMaxIdle(1);
		p.setInitialSize(1);
		p.setMaxWait(10000);
		p.setRemoveAbandonedTimeout(60);
		p.setMinEvictableIdleTimeMillis(30000);
		p.setMinIdle(1);
		p.setLogAbandoned(true);
		p.setRemoveAbandoned(true);
		p.setJdbcInterceptors("org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"
				+ "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer;"
				+ "org.apache.tomcat.jdbc.pool.interceptor.ResetAbandonedTimer");
		condata = new DataSource();
		condata.setPoolProperties(p);
	}
}
