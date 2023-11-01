import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * An abstract servlet initializing DataSource and defining two
 * general-use helper functions to be used by derived classes
 * @author Thomas Tran
 */
public abstract class AbstractServlet extends HttpServlet {
    //CONSTANTS ---------------
    private static final long serialVersionUID = 1L;

    //Constant HTTP response codes -- defining these to avoid magic values
    protected static final int RESPONSE_CODE_OK = 200;
    protected static final int RESPONSE_CODE_ERROR = 500;
    //-------------------------

    // Create a dataSource which registered in web.
    protected DataSource dataSource;

    /**
     * Initializes the DataSource object for subclasses to use
     */
    @Override
    public void init(ServletConfig config) {
        // This behaviour does not change within each servlet,
        // so I've put it here to prevent code repetition
        // - Thomas 4/16

        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/servicesDB");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads attributes from a query result object and formats them into a JSON array object.
     * PRE-CONDITIONS: queryResult is not NULL. queryResult contains all attributeNames in it.
     * POST-CONDITIONS: queryResult's cursor will be advanced to the end of the data. It will NOT be closed.
     *
     * @param queryResult Object returned from executing query on the DB Connection
     * @param attributeNames Array of expected attribute names from database. These same names will be used for the JSON keys
     * @return JSON Array object with all data from queryResult placed under the names in attributeNames
     * @author Thomas Tran
     */
    protected JsonArray readResults(final ResultSet queryResult, final String[] attributeNames) throws SQLException {
        JsonArray array = new JsonArray();

        while (queryResult.next()) {
            JsonObject jsonObject = new JsonObject();

            //Populate JSON object with column names
            for (String column : attributeNames) {
                jsonObject.addProperty(column, queryResult.getString(column));
            }

            //Add this object to the array
            array.add(jsonObject);
        }

        return array;
    }

    /**
     * Sends a JSON object with an error message and sets Response status to ERROR
     * @param out Response Writer
     * @param response HTTP Response
     * @param message Message to send under "errorMessage"
     */
    protected void sendError(final PrintWriter out, final HttpServletResponse response, final String message) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("errorMessage", message);
        out.write(jsonObject.toString());

        // Set response status to 500 (Internal Server Error)
        response.setStatus(RESPONSE_CODE_ERROR);
    }
}
