import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;


// Declaring a WebServlet called SingleSellerServlet which maps to api endpoint single-seller
@WebServlet(name = "SingleSellerServlet", urlPatterns = "/api/single-seller")
public class SingleSellerServlet extends AbstractServlet {
    private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        final PrintWriter writer = response.getWriter();

        //Gets the ID of the seller the user wishes to view
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

            //FIRST, CHECK IF USER IS A FREELANCER OR A COMPANY
            //CHECK IF COMPANY, AND IF NOT THEN ASSUME FREELANCER
            String query = "SELECT COUNT(*) as cnt FROM Company WHERE companyID = ? ";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, id);

            ResultSet result = statement.executeQuery();

            if (!result.next()) {
                System.err.println("SingleSellerServlet: FREELANCER/COMPANY Check Failed!");
                sendError(writer, response, "Seller not found");
            }

            final String[] columns;
            final JsonObject outputObject = new JsonObject();

            //Seller is a Company
            if (result.getInt("cnt") > 0) {
                outputObject.addProperty("seller_type", "company");
                query = "SELECT u.id as user_id, u.username as username, u.displayName as name, s.websiteLink as website, s.profileDescription as profile_description, c.address as address, c.phoneNumber as phone_number FROM Seller s INNER JOIN User u ON s.sellerID = u.id INNER JOIN Company c ON u.id = c.companyID WHERE u.id = ? ";
                columns = new String[]{
                        "user_id",
                        "username",
                        "name",
                        "website",
                        "profile_description",
                        "address",
                        "phone_number"
                };
            }
            //Seller is not a company -- assume they are a freelancer
            else {
                outputObject.addProperty("seller_type", "freelancer");
                query = "SELECT u.id as user_id, u.username as username, u.displayName as name, s.websiteLink as website, s.profileDescription as profile_description FROM Seller s INNER JOIN User u ON s.sellerID = u.id WHERE u.id = ? ";
                columns = new String[]{
                        "user_id",
                        "username",
                        "name",
                        "website",
                        "profile_description"
                };
            }

            //Free resources and continue
            statement.close();
            result.close();


            //PERFORM QUERY FOR SINGLE SELLER INFORMATION -----
            // Perform the query
            statement = conn.prepareStatement(query);
            statement.setInt(1, id);

            // Read results
            result = statement.executeQuery();
            final JsonObject sellerInfo = readResults(result, columns).get(0).getAsJsonObject();

            //Free resources and continue
            statement.close();
            result.close();

            // Add specializations
            sellerInfo.add("specializations", GetSpecializations(conn, id));

            // -----------------------------------------------------

            outputObject.add("single_seller", sellerInfo);
            outputObject.add("related_services", GetRelatedServices(conn, id));

            // Log to localhost log
            request.getServletContext().log("getting single seller page results");

            // Write JSON string to output
            writer.write(outputObject.toString());
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

    protected JsonArray GetSpecializations(final Connection conn, int sellerID) throws SQLException {
        //Prepare and execute query
        final String query = "SELECT DISTINCT sp.category AS category FROM Seller s NATURAL JOIN SellerSpecialize NATURAL JOIN Specialization sp WHERE s.sellerID = ? ";
        final String[] columns = {"category"};

        final PreparedStatement statement = conn.prepareStatement(query);
        statement.setInt(1, sellerID);

        //Read results
        final ResultSet result = statement.executeQuery();
        final JsonArray ret = super.readResults(result, columns);

        //Free resources and close
        statement.close();
        result.close();
        return ret;
    }

    protected JsonArray GetRelatedServices(final Connection conn, int sellerID) throws SQLException {
        //PERFORM SECOND QUERY FOR RELATED SERVICE LISTINGS -----
        final String query = "SELECT " +
                "ser.id as service_id," +
                "u.id as seller_id," +
                "ser.title as service_title," +
                "u.displayName as seller_name," +
                "ser.posted_date as service_date," +
                "ser.price as service_price, " +
                "ser.status as service_status " +
                "FROM Services ser " +
                "NATURAL JOIN Seller sel " +
                "INNER JOIN User u ON sel.sellerID = u.id " +
                "WHERE u.id = ? " +
                "ORDER BY ser.posted_date DESC " +
                "LIMIT 100";

        final String[] columns = {
                "service_id",
                "seller_id",
                "service_title",
                "seller_name",
                "service_date",
                "service_price",
                "service_status",
        };

        // Perform the query and read results
        final PreparedStatement statement = conn.prepareStatement(query);
        statement.setInt(1, sellerID);

        final ResultSet rs = statement.executeQuery();
        final JsonArray relatedServices = readResults(rs, columns);

        //Free resources and return result
        rs.close();
        statement.close();
        return relatedServices;
    }


}
