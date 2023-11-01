import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;

@WebServlet(name = "ShoppingCartServlet", urlPatterns = "/api/shopping-cart")
public class ShoppingCartServlet extends AbstractServlet {

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");

        final HttpSession session = request.getSession();
        final PrintWriter writer = response.getWriter();
        final Object sessionObj = session.getAttribute("cart");
        final HashMap<Integer, Integer> cartMap = ((sessionObj instanceof HashMap) ? (HashMap<Integer, Integer>) sessionObj : new HashMap<>());

        try (final Connection conn = dataSource.getConnection()){
            final JsonArray cart = new JsonArray();

            final String query = "SELECT ser.id as service_id, sel.sellerID as seller_id, ser.title as service_title, ser.description as service_description, ser.price as service_price, ser.payment_type as service_payment_type, ser.posted_date as service_posted_date, ser.status as service_status, u.displayName as seller_name, u.username as seller_username, avg(sr.rating) as seller_avg_rating FROM Services ser INNER JOIN Seller sel on ser.sellerID = sel.sellerID LEFT JOIN SellerReview sr on sel.sellerID = sr.sellerID INNER JOIN User u ON sel.sellerID = u.id WHERE ser.id = ? LIMIT 1 ";

            final String[] columns = {
                    "service_id",
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

            //TODO: This performs a SQL query for every single cart object, is probably inefficient
            for (final int key : cartMap.keySet()) {
                final JsonObject entry = new JsonObject();

                final PreparedStatement statement = conn.prepareStatement(query);
                statement.setInt(1, key);
                final ResultSet set = statement.executeQuery();

                if (set.next()) {
                    for (final String col : columns) {
                        entry.addProperty(col, set.getString(col));
                    }
                    entry.addProperty("quantity", cartMap.get(key));

                    cart.add(entry);
                }

                statement.close();
                set.close();
            }

            // write all the data into the jsonObject
            final JsonObject ret = new JsonObject();
            ret.add("cart", cart);
            ret.addProperty("sessionID", session.getId());
            ret.addProperty("lastAccessTime", session.getLastAccessedTime());

            writer.write(ret.toString());
            response.setStatus(AbstractServlet.RESPONSE_CODE_OK);
        }
        catch (Exception e) {
            sendError(writer, response, e.getMessage());
        }
        finally {
            writer.close();
        }
    }


    /**
     * Adding to the cart form the single service page
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");

        //Read the action parameter and determine what the user is trying to do
        switch (request.getParameter("action")) {
            case "add_item":
                actionAddItem(request, response);
                break;
            case "change_qty":
                actionChangeQty(request, response);
                break;
            case "remove":
                actionRemove(request, response);
                break;
            default:
                sendError(response.getWriter(), response, "Invalid action.");
        }
    }

    protected void actionAddItem(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        response.setStatus(AbstractServlet.RESPONSE_CODE_OK);

        //Fetch the previous HashMap or creates one if it doesn't exist
        //Maps Service ID -> Quantity
        final Object sessionObj = session.getAttribute("cart");
        final HashMap<Integer, Integer> cartMap = ((sessionObj instanceof HashMap) ? (HashMap<Integer, Integer>) sessionObj : new HashMap<>());
        final PrintWriter writer = response.getWriter();
        final JsonObject ret = new JsonObject();
        //Parse the new ID
        int id;
        try {
            id = Integer.parseInt(request.getParameter("service_id"));
        }
        //Something went wrong -> return error code
        catch (NumberFormatException e) {
            ret.addProperty("success", false);
            ret.addProperty("errorMessage", "Invalid ID");
            writer.write(ret.toString());
            writer.close();
            return;
        }

        //If ID is already in the hashmap, then increment its count
        if (cartMap.containsKey(id)) {
            cartMap.put(id, cartMap.get(id) + 1);
            session.setAttribute("cart", cartMap);
            ret.addProperty("success", true);
            writer.write(ret.toString());
            writer.close();
        }

        //Otherwise, we are adding a new id to the map.
        //Verify that the id actually exists in the database, and the status is OPEN.
        else {
            final String query = "SELECT s.status AS status FROM Services s WHERE s.id = ? ";
            try (Connection conn = dataSource.getConnection()) {
                PreparedStatement statement = conn.prepareStatement(query);
                statement.setInt(1, id);
                ResultSet results = statement.executeQuery();

                //Service exists, now make sure the status is OPEN
                if (results.next()) {
                    final String status = results.getString("status");
                    if (status != null && status.equals("OPEN")) {
                        //Add to the hashmap and save it to session
                        cartMap.put(id, 1);
                        session.setAttribute("cart", cartMap);
                        ret.addProperty("success", true);
                    }
                    //Not open -> cannot add to cart since someone else has already bought it
                    else {
                        ret.addProperty("success", false);
                        ret.addProperty("errorMessage", "Service is not available.");
                    }
                }
                //NEXT call failed --> the result is empty --> the id doesnt exist
                else {
                    ret.addProperty("success", false);
                    ret.addProperty("errorMessage", "Invalid ID");
                }
                writer.write(ret.toString());
                writer.close();

                results.close();
                statement.close();
            }
            //Something went wrong
            catch (SQLException e) {
                sendError(writer, response, e.getMessage());
                writer.close();
            }
        }
    }

    protected void actionChangeQty(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        PrintWriter writer = response.getWriter();

        //Fetch the previous HashMap
        //Maps Service ID -> Quantity
        final Object sessionObj = session.getAttribute("cart");
        final HashMap<Integer, Integer> cartMap;

        if (sessionObj instanceof HashMap) {
            cartMap = (HashMap<Integer, Integer>) sessionObj;
        }
        //Either the hashmap doesnt exist or something went wrong, send error
        else {
            sendError(writer, response, "Invalid cart");
            writer.close();
            return;
        }

        //Parse the new ID
        int id;
        try {
            id = Integer.parseInt(request.getParameter("service_id"));
        }
        //Something went wrong -> return error code
        catch (NumberFormatException e) {
            sendError(writer, response, "Invalid ID");
            writer.close();
            return;
        }

        //Parse the change (delta)
        int delta;
        try {
            delta = Integer.parseInt(request.getParameter("qty_change"));
        }
        //Something went wrong -> return error code
        catch (NumberFormatException e) {
            sendError(writer, response, "Invalid Quantity Change");
            writer.close();
            return;
        }

        //Apply the change
        if (cartMap.containsKey(id)) {
            final int newQty = cartMap.get(id) + delta;
            //If goes under 1 then just remove it from the cart
            if (newQty <= 0) {
                cartMap.remove(id);
            }
            else {
                cartMap.put(id, newQty);
            }

            session.setAttribute("cart", cartMap);
            final JsonObject ret = new JsonObject();
            ret.addProperty("success", true);
            writer.write(ret.toString());
            response.setStatus(AbstractServlet.RESPONSE_CODE_OK);
        }

        writer.close();
    }

    protected void actionRemove(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final HttpSession session = request.getSession();
        final PrintWriter writer = response.getWriter();

        //Fetch the previous HashMap
        //Maps Service ID -> Quantity
        final Object sessionObj = session.getAttribute("cart");
        final HashMap<Integer, Integer> cartMap;

        if (sessionObj instanceof HashMap) {
            cartMap = (HashMap<Integer, Integer>) sessionObj;
        }
        //Either the hashmap doesnt exist or something went wrong, send error
        else {
            sendError(writer, response, "Invalid cart");
            writer.close();
            return;
        }

        //Parse the new ID
        int id;
        try {
            id = Integer.parseInt(request.getParameter("service_id"));
        }
        //Something went wrong -> return error code
        catch (NumberFormatException e) {
            sendError(writer, response, "Invalid ID");
            writer.close();
            return;
        }

        final JsonObject ret = new JsonObject();
        ret.addProperty("success", cartMap.remove(id) != null);
        writer.write(ret.toString());
        writer.close();
    }
}
