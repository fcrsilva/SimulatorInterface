package endpoints;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
		String query = "SELECT post.*, `user`.age, `user`.gender, `user`.name,`user`.location FROM sentimentposts.post inner join sentimentposts.user on sentimentposts.post.user_id=sentimentposts.user.id order by post.id ASC";
		JSONArray result = new JSONArray();
		JSONObject post;
		JSONArray replies;
		JSONObject reply;
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		java.util.Date date = null;
		long postid;
		long replyid;
		try (Connection cndata = connlocal();
				Statement stmt = cndata.createStatement();
				ResultSet rs = stmt.executeQuery(query)) {

			if (rs.isClosed())
				return Response.status(Response.Status.BAD_REQUEST).entity("No Simulated Posts").build();
			while (rs.next()) {
				post = new JSONObject();
				replies = new JSONArray();
				postid = rs.getLong("id");
				post = new JSONObject();
				post.put("postId", rs.getLong("id"));
				post.put("source", "SIM");
				post.put("account", "Not implemented");
				post.put("location", rs.getString("location"));
				post.put("gender", rs.getString("gender"));
				post.put("url", "");
				post.put("imgUrl", "");
				post.put("userId", rs.getLong("user_id"));
				post.put("Fname", rs.getString("name"));
				post.put("age", rs.getLong("age"));

				try {
					date = df.parse(rs.getString("timestamp"));
				} catch (ParseException e) {
					System.err.println("ERROR parsing data" + rs.getString("timestamp"));
					date = new Date(1);
				}
				post.put("postEpoch", date.getTime());
				post.put("post", rs.getString("message"));
				post.put("mediaSpecificInfo", "true");
				post.put("likes", rs.getLong("likes"));
				post.put("views", rs.getLong("views"));
				
				while (rs.next() && rs.getLong("post_id") == postid) {
					reply = new JSONObject();
					replyid = rs.getLong("id");
					reply.put("postId", rs.getLong("id"));
					reply.put("source", "SIM");
					reply.put("account", "Not implemented");
					reply.put("location", rs.getString("location"));
					reply.put("gender", rs.getString("gender"));
					reply.put("url", "");
					reply.put("imgUrl", "");
					reply.put("userId", rs.getLong("user_id"));
					reply.put("Fname", rs.getString("name"));
					reply.put("age", rs.getLong("age"));

					try {
						date = df.parse(rs.getString("timestamp"));
					} catch (ParseException e) {
						System.err.println("ERROR parsing data" + rs.getString("timestamp"));
						date = new Date(1);
					}
					reply.put("postEpoch", date.getTime());
					reply.put("post", rs.getString("message"));
					reply.put("mediaSpecificInfo", "true");
					reply.put("likes", rs.getLong("likes"));
					reply.put("views", rs.getLong("views"));
					reply.put("parentPostId", postid);

					replies.put(reply);
				}
				rs.absolute(rs.getRow()-1);
				post.put("replies", replies);
				result.put(post);
			}
		} catch (Exception e) {
			System.out.println("ERROR3");
			e.printStackTrace();
		}
		return Response.status(Response.Status.OK).entity(result.toString()).build();

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
		p.setMaxActive(1);
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
