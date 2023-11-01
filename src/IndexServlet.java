import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mysql.cj.util.StringUtils;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Date;

@WebServlet(name = "IndexServlet", urlPatterns = "/api/index")
public class IndexServlet extends AbstractServlet {

    private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //Format response
        response.setContentType("application/json");

        final PrintWriter out = response.getWriter();

        // Establish connection to database -- wrap in try with resources block to automatically close connection
        try (final Connection conn = dataSource.getConnection()) {
            final String query = "SELECT category FROM Specialization";
            final String[] columns = {"category"};

            // Perform the query and read results
            final PreparedStatement statement = conn.prepareStatement(query);
            final ResultSet rs = statement.executeQuery();
            final JsonArray jsonArray = readResults(rs, columns);

            //Free resources
            rs.close();
            statement.close();

            // Log to localhost log
            request.getServletContext().log("getting " + jsonArray.size() + " results");

            // Write JSON string to output
            out.write(jsonArray.toString());
            response.setStatus(RESPONSE_CODE_OK);
        }
        //Something went wrong -- write an error message instead
        catch (Exception e) {
            sendError(out, response, e.getMessage());
        }
        finally {
            out.close();
        }
    }

    /**
     * Autocomplete
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //Format response
        response.setContentType("application/json");

        final PrintWriter out = response.getWriter();

        // Establish connection to database -- wrap in try with resources block to automatically close connection
        try (final Connection conn = dataSource.getConnection()) {
            final String query = "SELECT title AS value, id AS data FROM Services WHERE MATCH(title) AGAINST(? IN BOOLEAN MODE) ORDER BY title ASC, posted_date ASC LIMIT 10";
            final String[] columns = {"value", "data"};

            // Perform the query and read results
            final PreparedStatement statement = conn.prepareStatement(query);

            statement.setString(1, MatchUtil.FormatMatchString(request.getParameter("ac-query")));

            final ResultSet rs = statement.executeQuery();
            final JsonArray jsonArray = readResults(rs, columns);

            //Free resources
            rs.close();
            statement.close();

            // Log to localhost log
            request.getServletContext().log("getting " + jsonArray.size() + " results");

            // Write JSON string to output
            out.write(jsonArray.toString());
            response.setStatus(RESPONSE_CODE_OK);
        }
        //Something went wrong -- write an error message instead
        catch (Exception e) {
            sendError(out, response, e.getMessage());
        }
        finally {
            out.close();
        }
    }
}
