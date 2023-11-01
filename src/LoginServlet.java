import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.jasypt.util.password.StrongPasswordEncryptor;

@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends AbstractServlet {
    private static final long serialVersionUID = 1L;

    /**
     * This is called when the user clicks "Login" in the front end
     * The submitted information will contain the username and the password from the form
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        /*
            Run a query on the given username and password
            Create a user object from that -> save into session
            Return JSON response
        */
        resp.setContentType("application/json");
        final PrintWriter out = resp.getWriter();
        final JsonObject obj = new JsonObject();

        //Check if captcha can be bypassed (used for JMeter)
        final String bypass = req.getParameter("recaptcha-bypass");
        if (bypass == null || !bypass.equals("CZYuAEjdtrpysA")) { //secret recaptcha bypass value

            //FIRST, VERIFY THE CAPTCHA
            final String platform = req.getParameter("platform");
            final String gRecaptchaResponse = req.getParameter("g-recaptcha-response");
            System.out.println("LOGIN: gRecaptchaResponse: " + gRecaptchaResponse);
            if (!CaptchaVerify.Verify(gRecaptchaResponse, platform)) {
                //Verification failed -- return failure

                obj.addProperty("success", false);
                obj.addProperty("message", "Recaptcha failed.");
                out.write(obj.toString());
                out.close();
                return;
            }
        }

        final String username = req.getParameter("username");
        final String password = req.getParameter("password");

        //Either one is null -- send an error code
        if (username == null || password == null) {
            sendError(out, resp, "Username or password not provided.");
            return;
        }

        try (Connection conn = dataSource.getConnection()) {
            resp.setStatus(AbstractServlet.RESPONSE_CODE_OK);

            //First check if the user actually exists
            PreparedStatement statement = conn.prepareStatement("SELECT id, password FROM User WHERE username = ?;");
            statement.setString(1, username);
            ResultSet results = statement.executeQuery();

            //NO MATCHING USERNAME -- EXIT AND SEND ERROR!
            if (!results.next()) {
                obj.addProperty("success", false);
                obj.addProperty("message", "Invalid username.");
                out.write(obj.toString());

                statement.close();
                results.close();
                return;
            }

            // We have a matching username.
            // Now actually check for the password

            //Password did not match -- error
            final StrongPasswordEncryptor encryptor = new StrongPasswordEncryptor();
            if (!encryptor.checkPassword(password, results.getString("password"))) {
                obj.addProperty("success", false);
                obj.addProperty("message", "Invalid password.");
                out.write(obj.toString());

                statement.close();
                results.close();
                return;
            }

            //Everything checks out, save the User object into the session and finish response
            int id = results.getInt("id");
            req.getSession().setAttribute("user", new User(id, username));
            obj.addProperty("success", true);
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