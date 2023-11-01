import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

@WebServlet(name = "PaymentServlet", urlPatterns = "/api/payment")
public class PaymentServlet extends AbstractWriteServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");

        switch (req.getParameter("action")) {
            case "load_page":
                actionLoadPage(req, resp);
                break;
//            case "add_card":
//                actionAddCard(req, resp);
//                break;
            case "purchase":
                actionPurchase(req, resp);
                break;
            default:
                sendError(resp.getWriter(), resp, "Invalid action.");
        }
    }

    protected void actionLoadPage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final HttpSession session = request.getSession();
        final PrintWriter writer = response.getWriter();

        try (final Connection conn = dataSource.getConnection()) {
            final Object sessionObj = session.getAttribute("cart");
            final HashMap<Integer, Integer> cartMap = ((sessionObj instanceof HashMap) ? (HashMap<Integer, Integer>) sessionObj : new HashMap<>());
            final Iterator<Integer> iter = cartMap.keySet().iterator();

            if (iter.hasNext()) {
                final StringBuilder builder = new StringBuilder();
                builder.append(iter.next());

                while (iter.hasNext()) {
                    builder.append(",").append(iter.next());
                }

                //Getting the price

                //NOTE: NOT USING PREPARED STATEMENT HERE.
                //DATA IS ALREADY VALIDATED IN OUR INTERNAL BACKEND HASHMAP, MUST BE AN INTEGER. PROBABLY NO CHANCE OF INJECTION.
                //AND UNFORTUNATELY PreparedStatement.setArray() DOES NOT WORK WITH MYSQL, SO PREPARED STATEMENT IS IMPRACTICAL HERE.
                String query = "select s.id as id, s.price as price from Services s where s.id in (" + builder + ")";
                final Statement statement = conn.createStatement();
                ResultSet resultSet = statement.executeQuery(query);

                double price = 0.0D;
                while (resultSet.next()) {
                    price += (resultSet.getDouble("price") * cartMap.get(resultSet.getInt("id")));
                }

                resultSet.close();
                statement.close();

                //Get the users id from the session to get their credit cards
                final Object userObj = request.getSession().getAttribute("user");
                if (userObj instanceof User) {
                    final User user = (User) userObj;
                    final int userID = user.getId();

                    query = "select c.cardNum as card_number, c.name as card_name, c.expiration as card_exp from UserCreditCard u natural join CreditCard c where userID = ?";

                    final String[] columns = {
                            "card_number",
                            "card_name",
                            "card_exp"
                    };

                    final PreparedStatement preparedStatement = conn.prepareStatement(query);
                    preparedStatement.setInt(1, userID);

                    resultSet = preparedStatement.executeQuery();
                    final JsonArray cardInfo = readResults(resultSet, columns);

                    //TODO: handle case where user does not have a credit card

                    final JsonObject ret = new JsonObject();
                    ret.add("cards", cardInfo);
                    ret.addProperty("total_price" , price);
                    writer.write(ret.toString());
                    response.setStatus(AbstractServlet.RESPONSE_CODE_OK);
                }
            }
        }
        catch (Exception e) {
            sendError(writer, response, e.getMessage());
        }
        finally {
            writer.close();
        }

    }
//    protected void actionAddCard(HttpServletRequest request, HttpServletResponse response) throws IOException {
//
//    }
    protected void actionPurchase(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final PrintWriter writer = response.getWriter();

        try (final Connection conn = dataSource.getConnection();
             final Connection writeConn = writeDataSource.getConnection()) {
            String query = "SELECT COUNT(*) AS cnt FROM CreditCard c WHERE c.cardNum = ? AND c.name = ? AND c.expiration = ? ";

            final PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1,request.getParameter("card_number"));
            statement.setString(2, request.getParameter("card_name"));
            statement.setDate(3, Date.valueOf(request.getParameter("card_exp")));

            final ResultSet resultSet = statement.executeQuery();
            resultSet.next();

            final JsonObject ret = new JsonObject();
            if (resultSet.getInt("cnt") > 0) {
                resultSet.close();
                statement.close();

                //We have a matching card -> update all the entries
                final HttpSession session = request.getSession();

                final Object sessionObj = session.getAttribute("cart");
                final HashMap<Integer, Integer> cartMap;

                if (sessionObj instanceof HashMap) {
                    cartMap = (HashMap<Integer, Integer>) sessionObj;
                    final Iterator<Integer> iter = cartMap.keySet().iterator();

                    if (iter.hasNext()) {
                        final StringBuilder builder = new StringBuilder();
                        builder.append(iter.next());

                        while (iter.hasNext()) {
                            builder.append(",").append(iter.next());
                        }

                        Object userobj = session.getAttribute("user");
                        if (userobj instanceof User) {
                            //Change the entries

                            //NOTE: NOT USING PREPARED STATEMENT HERE.
                            //ALL DATA IS ALREADY VALIDATED IN OUR BACKEND SESSION (USER ID and CART HASHMAP). PROBABLY NO CHANCE OF INJECTION.
                            //AND UNFORTUNATELY PreparedStatement.setArray() DOES NOT WORK WITH MYSQL, SO PREPARED STATEMENT IS IMPRACTICAL HERE.
                            query = "UPDATE Services SET status = \"IN-PROGRESS\", customerID = " + ((User) userobj).getId() + " WHERE id IN (" + builder + ") ";
                            final Statement st = writeConn.createStatement();
                            st.executeUpdate(query);

                            //Clear the cart
                            session.removeAttribute("cart");

                            ret.addProperty("success", true);
                        }
                        else {
                            ret.addProperty("success", false);
                            ret.addProperty("errorMessage", "Invalid User.");
                        }
                    }
                    else {
                        ret.addProperty("success", false);
                        ret.addProperty("errorMessage", "Invalid cart.");
                    }
                }
                else {
                    //Something went wrong with the cart
                    ret.addProperty("success", false);
                    ret.addProperty("errorMessage", "Invalid cart.");
                }
            }
            //No matching credit card found
            else {
                ret.addProperty("success", false);
                ret.addProperty("errorMessage", "Invalid card.");
            }

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
}
