import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


// Declaring a WebServlet called ServicesServlet, which maps to url "/api/services"
@WebServlet(name = "ServicesServlet", urlPatterns = "/api/services")
public class ServicesServlet extends AbstractServlet {

    /**
     * Maps inputted GET parameters to AND clauses in the SQL query
     */
    private static HashMap<String, String> paramQueryMap = null;

    @Override
    public void init(ServletConfig config) {
        super.init(config);

        if (paramQueryMap == null) {
            paramQueryMap = new HashMap<>();
            //For searching
            paramQueryMap.put("search", "AND ser.title LIKE CONCAT('%',?,'%') ");
            paramQueryMap.put("year", "AND YEAR(ser.posted_date) = ? ");
            paramQueryMap.put("seller", "AND u.displayName LIKE CONCAT('%',?,'%') ");
            paramQueryMap.put("specialization", "AND spec.category LIKE CONCAT('%',?,'%') ");
            paramQueryMap.put("min-price", "AND ser.price >= ? ");
            paramQueryMap.put("max-price", "AND ser.price <= ? ");
            paramQueryMap.put("status", "AND ser.status = ? ");

            //For browsing
            paramQueryMap.put("starts-with", "AND ser.title LIKE CONCAT(?,'%') ");

            //TODO: this is hardcoded but idk how you would not hardcode it
            paramQueryMap.put("starts-with-special", "AND ser.title REGEXP '^[^A-Z0-9].*' ");

            paramQueryMap.put("spec-exact", "AND spec.category = ? ");

            //For full-text search
            paramQueryMap.put("full-text", "AND MATCH(title) AGAINST(? IN BOOLEAN MODE) ");
        }
    }

    private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //TIME MEASURING (TS START)
        long tjStart = 0;
        long tjEnd = 0;
        long tsStart = System.nanoTime();

        //Format response
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();


        // Establish connection to database -- wrap in try with resources block to automatically close connection
        try (Connection conn = dataSource.getConnection()) {
            Map<String, String[]> paramMap = request.getParameterMap();
            JsonObject returnObject = new JsonObject();

            //Checks if any of the parameters are inputted
            if (request.getQueryString() != null && !request.getQueryString().isBlank()) {
                //Save this search to the session
                request.getSession().setAttribute("saved_state", request.getParameterMap());
                request.getSession().setAttribute("saved_state_string", request.getQueryString());
            }
            //Nothing is inputted -> search is empty -> load from session if exists
            else {
                Object restoredState = request.getSession().getAttribute("saved_state");
                Object restoredString = request.getSession().getAttribute("saved_state_string");
                if (restoredState instanceof Map && restoredString instanceof String) {
                    paramMap = (Map<String, String[]>) restoredState;
                    returnObject.addProperty("restored_state_string", (String) restoredString);
                }
            }

            //BUILDING THE BASE QUERY --------------
            final StringBuilder query = new StringBuilder("SELECT DISTINCT ser.id as service_id, u.id as seller_id, ser.title as service_title, u.displayName as seller_name, ser.posted_date as service_date, ser.price as service_price, ser.status as service_status FROM Services ser NATURAL JOIN Seller sel INNER JOIN User u ON sel.sellerID = u.id NATURAL LEFT JOIN SellerSpecialize ssp NATURAL LEFT JOIN Specialization spec WHERE 1=1 ");
            final StringBuilder maxPageSizeQuery = new StringBuilder("SELECT COUNT(DISTINCT ser.id) AS cnt FROM Services ser NATURAL JOIN Seller sel INNER JOIN User u ON sel.sellerID = u.id NATURAL LEFT JOIN SellerSpecialize ssp NATURAL LEFT JOIN Specialization spec WHERE 1=1 ");

            final ArrayList<String> values = new ArrayList<>();
            //Read the GET Parameters for narrowing down search
            for (final String key : paramQueryMap.keySet()) {
                final String[] params = paramMap.get(key);
                if (params != null) {
                    for (String param : params) {
                        if (param != null && !param.isBlank()) {
                            query.append(paramQueryMap.get(key));
                            maxPageSizeQuery.append(paramQueryMap.get(key));

                            //TODO: this is hardcoded
                            if (key.equals("starts-with-special"));
                            else if (key.equals("full-text")) {
                                values.add(MatchUtil.FormatMatchString(param));
                            }
                            else values.add(param);

                            break;
                        }
                    }
                }
            }

            //-----------------------------------------
            //SORTING ORDER
            boolean sortSuccess = false;
            if (paramMap.containsKey("sort-order")) {
                final String[] sortParams = paramMap.get("sort-order");
                for (final String sortParam : sortParams) {
                    if (sortParam != null && !sortParam.isBlank()) {
                        if (sortParam.length() == 4) {
                            if (sortParam.charAt(0) != sortParam.charAt(2)) {
                                try {
                                    String str =
                                            "ORDER BY " +
                                                    parseSortOrder(sortParam.charAt(0), sortParam.charAt(1)) +
                                                    ", " +
                                                    parseSortOrder(sortParam.charAt(2), sortParam.charAt(3)) +
                                                    " ";

                                    query.append(str);
                                    sortSuccess = true;
                                }
                                catch (IllegalArgumentException e) {
                                    break;
                                }
                            }
                        }
                        break;
                    }
                }
            }

            //if failed --> fallback to default sort order
            if (!sortSuccess) {
                query.append("ORDER BY ser.title ASC, ser.posted_date ASC ");
            }
            //-----------------------------------------

            //-----------------------------------------
            //ADDING PAGINATION (LIMIT AND OFFSET)

            //fetch the parameters we need (page size and page number)
            // set to default values
            int pageSize = parseIntOrDefault(paramMap, "page-size", 10);
            int pageNumber = parseIntOrDefault(paramMap, "page", 1);

            //Front end page number is 1-based (i.e. it goes from 1 -> max)
            //But SQL offset calculation is from 0. So, subtract one.
            pageNumber--;

            //Clamp page size -- must be within interval [10, 100]
            pageSize = Math.max(Math.min(pageSize, 100), 10);

            //Perform the maxpagesize query to find out how many pages we can have
            PreparedStatement statement = conn.prepareStatement(maxPageSizeQuery.toString());
            for (int i = 0; i < values.size(); i++) {
                statement.setString((i + 1), values.get(i));
            }
            //get the max pages from the result
            ResultSet rs = statement.executeQuery();
            rs.next();

            //Max Pages is calc'd from FLOOR(COUNT/pagesize)
            //Since we are using integer division the floor is already built in
            int maxPages = (rs.getInt("cnt") / pageSize);

            //Now that we know max pages we can
            //Clamp page number -- must be in interval [0, maxPages]
            //--> remember this is 1 less than the page number from the front end
            pageNumber = Math.max(Math.min(pageNumber, maxPages), 0);

            //Now that we have the correct values...
            //APPEND THE LIMIT AND OFFSET CLAUSE TO THE MAIN QUERY

            //NOTE: we are not using PreparedStatement ? here because
            // 1. My current code sets everything using String which I would have to do some around for two ints
            // 2. These are validated, clamped int values, so I don't think an injection can be performed via this route
            // - Thomas 4/28
            query.append("LIMIT ").append(pageSize).append(" ");
            query.append("OFFSET ").append((pageSize * pageNumber)).append(" ");

            //Close the resources
            statement.close();
            rs.close();
            //-----------------------------------------

            final String[] columns = {
                    "service_id",
                    "seller_id",
                    "service_title",
                    "seller_name",
                    "service_date",
                    "service_price",
                    "service_status",
            };

            //PREPARE STATEMENT AND POPULATE DATA
            statement = conn.prepareStatement(query.toString());
            for (int i = 0; i < values.size(); i++) {
                statement.setString((i + 1), values.get(i));
            }

            //EXECUTE AND READ RESULTS
            tjStart = System.nanoTime(); //TIMING TJ START --------
            rs = statement.executeQuery();
            tjEnd = System.nanoTime(); //TIMING TJ END --------
            JsonArray jsonArray = readResults(rs, columns);

            //Free resources
            rs.close();
            statement.close();

            // Log to localhost log
            request.getServletContext().log("getting " + jsonArray.size() + " results");

            returnObject.add("values", jsonArray);
            returnObject.addProperty("max-pages", (maxPages + 1)); //add 1 because this is for the front end

            // Write JSON string to output
            out.write(returnObject.toString());
            response.setStatus(RESPONSE_CODE_OK);

        }
        //Something went wrong -- write an error message instead
        catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(RESPONSE_CODE_ERROR);
        } finally {
            out.close();
        }

        //TIME MEASURING (TS END)
        long tsEnd = System.nanoTime();

        long tsElapsed = tsEnd - tsStart;
        long tjElapsed = tjEnd - tjStart;

        final String logline = tsElapsed + "," + tjElapsed + '\n';
        Files.write(Paths.get(request.getServletContext().getRealPath("/") + "buzzle-log.txt"), logline.getBytes(), StandardOpenOption.APPEND, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        doGet(req, resp);
    }

    private int parseIntOrDefault(final Map<String, String[]> map, final String param, final int def) {
        if (map.containsKey(param)) {
            for (final String candidate : map.get(param)) {
                if (candidate != null && !candidate.isBlank()) {
                    try {
                        return Integer.parseInt(candidate);
                    }
                    catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        return def;
    }

    private String parseSortOrder(char c1, char c2) throws IllegalArgumentException {
        String ret = "";
        switch (c1) {
            case '0':
                ret += "ser.title";
                break;
            case '1':
                ret += "ser.posted_date";
                break;
            default:
                throw new IllegalArgumentException();
        }

        ret += " ";

        switch (c2) {
            case 'A':
                ret += "ASC";
                break;
            case 'D':
                ret += "DESC";
                break;
            default:
                throw new IllegalArgumentException();
        }

        return ret;
    }
}
