import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

@WebServlet(name = "SingleServiceServlet", urlPatterns = "/api/single-service")
public class SingleServiceServlet extends AbstractServlet {
    private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        final PrintWriter writer = response.getWriter();

        //Gets the ID of the service the user wishes to view
        final String idStr = request.getParameter("id");
        final int id;
        try {
            id = Integer.parseInt(idStr);
        }
        catch (NumberFormatException|NullPointerException e) {
            sendError(writer, response, e.getMessage());
            writer.close();
            return;
        }

        // Establish connection to database -- wrap in try with resources block to automatically close connection
        try (final Connection conn = dataSource.getConnection()) {
            // Perform the query
            final String query = "SELECT sel.sellerID as seller_id, ser.title as service_title, ser.description as service_description, ser.price as service_price, ser.payment_type as service_payment_type, ser.posted_date as service_posted_date, ser.status as service_status, u.displayName as seller_name, u.username as seller_username, avg(sr.rating) as seller_avg_rating FROM Services ser INNER JOIN Seller sel on ser.sellerID = sel.sellerID LEFT JOIN SellerReview sr on sel.sellerID = sr.sellerID INNER JOIN User u ON sel.sellerID = u.id WHERE ser.id = ? ";
            final String[] columns = {
                    "seller_id",
                    "service_title",
                    "service_description",
                    "service_price",
                    "service_payment_type",
                    "service_posted_date",
                    "service_status",
                    "seller_name",
                    "seller_username",
                    "seller_avg_rating",
            };

            final PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, id);

            //Read results
            final ResultSet rs = statement.executeQuery();
            final JsonArray jsonArray = readResults(rs, columns);

            //Free resources
            rs.close();
            statement.close();

            // Log to localhost log
            request.getServletContext().log("getting " + jsonArray.size() + " results");

            // Write JSON string to output
            writer.write(jsonArray.toString());
            response.setStatus(RESPONSE_CODE_OK);
        }
        //Something went wrong -- write an error message instead
        catch (Exception e) {
            sendError(writer, response, e.getMessage());
        }
        finally {
            writer.close();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        doGet(req, resp);
    }
}
