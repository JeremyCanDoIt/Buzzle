import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

public class CaptchaVerify {
    private static final HashMap<String, String> privateKeyMap = new HashMap<>();

    static {
        privateKeyMap.put("web", "6LfAutYlAAAAAG1vSrH-e3d_lYUAmFTCDjGKuz6Q");
        privateKeyMap.put("android", "6LccNEMmAAAAAP5T0u6iTqUp2uB0UFGhupG1PRyC");
    }

    private static final String SITE_VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";

    public static boolean Verify(final String resp, final String platform) {
        if (resp == null || resp.isBlank()) return false;

        //Check if a valid platform
        if (!privateKeyMap.containsKey(platform)) return false;
        System.out.println("verifying with " + platform +  " captcha key");

        try {
            final URL url = new URL(SITE_VERIFY_URL);

            //Build HTTPS connection to recaptcha verifier
            final HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            conn.setDoOutput(true); //What does this do?

            //Load data and send to server
            final String params = "secret=" + privateKeyMap.get(platform) + "&response=" + resp;
            final OutputStream out = conn.getOutputStream();
            out.write(params.getBytes());
            out.flush();
            out.close();

            // Get the InputStream from Connection to read data sent from the server.
            final InputStream inputStream = conn.getInputStream();
            final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

            final JsonObject jsonObject = new Gson().fromJson(inputStreamReader, JsonObject.class);
            inputStreamReader.close();

            return jsonObject.get("success").getAsBoolean();
        }
        catch (Exception e) {
            System.err.println("CAPTCHA VERIFY CRITICAL ERROR: " + e.getMessage());
        }

        return false;
    }
}
