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

@WebServlet(name = "EmployeeLoginServlet", urlPatterns = "/api/_employee-login")
public class EmployeeLoginServlet extends AbstractServlet {
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

        //FIRST, VERIFY THE CAPTCHA
        //TODO: FIXME: ACTIVATE THIS BEFORE TURNING IN
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

        final String username = req.getParameter("username");
        final String password = req.getParameter("password");

        //Either one is null -- send an error code
        if (username == null || password == null) {
            sendError(out, resp, "Email or password not provided.");
            return;
        }

        try (Connection conn = dataSource.getConnection()) {
            resp.setStatus(AbstractServlet.RESPONSE_CODE_OK);

            //First check if the user actually exists
            PreparedStatement statement = conn.prepareStatement("SELECT email, password FROM Employee WHERE email = ?;");
            statement.setString(1, username);
            ResultSet results = statement.executeQuery();

            //NO MATCHING USERNAME -- EXIT AND SEND ERROR!
            if (!results.next()) {
                obj.addProperty("success", false);
                obj.addProperty("message", "Invalid email.");
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
            final String email = results.getString("email");
            req.getSession().setAttribute("employee", new Employee(email));
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