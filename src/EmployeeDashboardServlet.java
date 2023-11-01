import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import org.jasypt.util.password.StrongPasswordEncryptor;

@WebServlet(name = "EmployeeDashboardServlet", urlPatterns = "/api/_dashboard")
public class EmployeeDashboardServlet extends AbstractWriteServlet {
    private static final long serialVersionUID = 1L;

    /**
     * This is called when the Employee clicks "Login" in the front end
     * The submitted information will contain the username and the password from the form
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");

        switch (req.getParameter("action")) {
            case "add_seller":
                addSeller(req, resp);
                break;
            case "add_service":
                addService(req, resp);
                break;
            default:
                sendError(resp.getWriter(), resp, "Invalid action.");
                break;
        }

    }

    protected void addSeller(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            final PrintWriter out = resp.getWriter();
            final JsonObject obj = new JsonObject();

        try (final Connection writeConn = writeDataSource.getConnection()) {
                resp.setStatus(AbstractServlet.RESPONSE_CODE_OK);

                //Set auto commit to false so in case one insert succeeds and one fails,
                // we dont leave database in undefined state
                writeConn.setAutoCommit(false);

                final StrongPasswordEncryptor encryptor = new StrongPasswordEncryptor();

                //insert into user table
                PreparedStatement statement = writeConn.prepareStatement("INSERT INTO User(username, password, displayName, locationID) VALUES (?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
                statement.setString(1, req.getParameter("username"));
                statement.setString(2, encryptor.encryptPassword(req.getParameter("password")));
                statement.setString(3, req.getParameter("display-name"));
                statement.setInt(4, Integer.parseInt(req.getParameter("location")));
                int updated = statement.executeUpdate();

                if (updated > 0) {
                    ResultSet a = statement.getGeneratedKeys();
                    int userid;
                    if (a.next()) {
                        userid = a.getInt(1);

                        //insert into seller table
                        final String link = req.getParameter("website-link");
                        final String desc = req.getParameter("profile-desc");

                        statement = writeConn.prepareStatement("INSERT INTO Seller(sellerID, websiteLink, profileDescription) VALUES (?,?,?)");
                        statement.setInt(1, userid);
                        statement.setString(2, link); //can be null, check for error here
                        statement.setString(3, desc); //can be null, check for error here
                        int sellerUpdated = statement.executeUpdate();

                        statement.close();
                        if (sellerUpdated > 0) {
                            writeConn.commit();
                            obj.addProperty("success", true);
                        }
                        else {
                            obj.addProperty("success", false);
                            obj.addProperty("errorMessage", "Seller insert failed.");
                        }

                    }
                    else {
                        statement.close();
                        obj.addProperty("success", false);
                        obj.addProperty("errorMessage", "User ID Query failed.");
                    }
                    a.close();
                    statement.close();
                }
                else {
                   obj.addProperty("success", false);
                    obj.addProperty("errorMessage", "User table insert failed.");
                }

                out.write(obj.toString());
            }
            catch (Exception e) {
                sendError(out, resp, e.getMessage());
            }
            finally {
                out.close();
            }
    }

    protected void addService(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        final PrintWriter out = resp.getWriter();
        final JsonObject obj = new JsonObject();

        try (final Connection writeConn = writeDataSource.getConnection()) {
            resp.setStatus(AbstractServlet.RESPONSE_CODE_OK);

            final java.util.Date currentDate = new java.util.Date();

            final PreparedStatement statement = writeConn.prepareStatement("CALL add_service(?,?,?,?,?,?,?)"); //7 args
            statement.setString(1, req.getParameter("title"));
            statement.setString(2, req.getParameter("description"));
            statement.setFloat(3, Float.parseFloat(req.getParameter("price")));
            statement.setDate(4, new Date(currentDate.getTime())); //set to current date
            statement.setString(5, "OPEN");
            statement.setString(6, req.getParameter("payment_type"));
            statement.setString(7, req.getParameter("seller_name"));

            int res = statement.executeUpdate();

            if (res > 0) {
                obj.addProperty("success", true);
            }
            else {
                obj.addProperty("success", false);
                obj.addProperty("errorMessage", "Procedure call failed.");
            }

            out.write(obj.toString());
            statement.close();
        }
        catch (Exception e) {
            sendError(out, resp, e.getMessage());
        }
        finally {
            out.close();
        }
    }

    /**
     * On Page Load
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        final PrintWriter out = resp.getWriter();
        final JsonObject obj = new JsonObject();

        try (Connection conn = dataSource.getConnection()) {
            resp.setStatus(AbstractServlet.RESPONSE_CODE_OK);

            //First get the schema
            PreparedStatement statement = conn.prepareStatement("SELECT TABLE_NAME, COLUMN_NAME, DATA_TYPE FROM information_schema.columns WHERE TABLE_SCHEMA = \"servicesDB\" ORDER BY TABLE_NAME;");
            ResultSet results = statement.executeQuery();

            //Format it properly-- we have data on two different levels of aggregation here
            final HashMap<String, JsonArray> tableMap = new HashMap<>();
            while (results.next()) {
                final String tableName = results.getString("TABLE_NAME");
                //Insert a new array if it doesnt exist
                if (!tableMap.containsKey(tableName)) {
                    tableMap.put(tableName, new JsonArray());
                }

                final JsonArray array = tableMap.get(tableName);
                final JsonObject entry = new JsonObject();

                entry.addProperty("column_name", results.getString("COLUMN_NAME"));
                entry.addProperty("data_type", results.getString("DATA_TYPE"));
                array.add(entry);
            }

            //Structure it into schema object and put into final return obj
            final JsonArray schema = new JsonArray();

            for (final String tableName : tableMap.keySet()) {
                final JsonObject tableEntry = new JsonObject();
                tableEntry.addProperty("name", tableName);
                tableEntry.add("columns", tableMap.get(tableName));
                schema.add(tableEntry);
            }
            obj.add("schema", schema);
            statement.close();
            results.close();

            //Now perform the query for the locations
            statement = conn.prepareStatement("SELECT locationID as location_id, city as city, state as state FROM GeneralLocation");
            results = statement.executeQuery();

            final JsonArray locationData = super.readResults(results, new String[]{"location_id", "city", "state"});
            obj.add("location_data", locationData);

            out.write(obj.toString());
            statement.close();
            results.close();
        }
        catch (Exception e) {
            sendError(out, resp, e.getMessage());
        }
        finally {
            out.close();
        }
    }
}