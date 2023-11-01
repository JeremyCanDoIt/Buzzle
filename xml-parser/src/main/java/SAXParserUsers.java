import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.jasypt.util.password.StrongPasswordEncryptor;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.io.File;  // Import the File class
import java.io.FileWriter;
public class SAXParserUsers extends DefaultHandler {

    List<Map<String, String>> myServices;

    private String tempVal;

    //to maintain context
    private Map<String, String> tempService;

    public SAXParserUsers() {
        myServices = new ArrayList<Map<String, String>>();
    }

    public void runParser() {
        parseDocument();
    }

    //Returns a List of Maps of representing Users
    public List<Map<String, String>> returnUsers() {
        return myServices;
    }

    private void parseDocument() {

        //get a factory
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {

            //get a new instance of parser
            SAXParser sp = spf.newSAXParser();

            //parse the file and also register this class for call backs
            sp.parse("users-cambridge.xml", this);

        } catch (SAXException se) {
            se.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }

    /**
     * Iterate through the list and print
     * the contents
     */
    public void printData() {

        System.out.println("No of Employees '" + myServices.size() + "'.");

        Iterator<Map<String, String>> it = myServices.iterator();
        while (it.hasNext()) {
            System.out.println(it.next().toString());
        }
    }

    //Event Handlers
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        //reset
        tempVal = "";
        if (qName.equalsIgnoreCase("record")) {
            //create a new instance of employee
            tempService = new HashMap<String,String>();

        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {

        if (qName.equalsIgnoreCase("record")) {
            //add it to the list
            myServices.add(tempService);

        } else if (qName.equalsIgnoreCase("username")) {
            tempService.put("username",tempVal );
        } else if (qName.equalsIgnoreCase("password")) {
            StrongPasswordEncryptor spe = new StrongPasswordEncryptor();

            tempService.put("password",spe.encryptPassword(tempVal) );
        } else if (qName.equalsIgnoreCase("displayName")) {
            tempService.put("displayName",tempVal );
        }
        else if (qName.equalsIgnoreCase("locationID")) {
            tempService.put("locationID",tempVal );
        }
        else{
            System.out.println("Inconsistency Detected: " + qName + ":" + tempVal);
        }

    }

    public void insertIntoDatabase() {
        try {
            String loginUser = "mytestuser";
            String loginPasswd = "$FLb6%SjnmXP5R";
            String loginUrl = "jdbc:mysql://localhost:3306/servicesDB";
            Class.forName("com.mysql.jdbc.Driver").newInstance();

            try (final Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd)) {

                //get the location ids to validate
                final String query = "SELECT locationID AS id FROM GeneralLocation";
                PreparedStatement statement = connection.prepareStatement(query);
                ResultSet results = statement.executeQuery();
                final HashSet<Integer> locIDs = new HashSet<>();

                while (results.next()) {
                    locIDs.add(results.getInt("id"));
                }

                statement.close();
                results.close();

                final StringBuilder builder = new StringBuilder("INSERT IGNORE INTO User(username, password, displayName, locationID) VALUES ");
                //{password=EIjb8yJNpwPf, displayName=Hardy Francescuzzi, locationID=239, username=hfrancescuzzirr}

                for (final Map<String, String> entry : myServices) {
                    final StringBuilder temp = new StringBuilder();

                    temp.append("(\"")
                            .append(entry.get("username"))
                            .append("\",\"")
                            .append(entry.get("password"))
                            .append("\",\"")
                            .append(entry.get("displayName"))
                            .append("\",")
                            .append(entry.get("locationID"))
                            .append(")");

                    //validate the data
                    try {
                        int locID = Integer.parseInt(entry.get("locationID"));

                        if (!locIDs.contains(locID)) {
                            System.out.println("Inconsistency Report (INVALID LOCATION ID): " + temp);
                            continue;
                        }
                    }
                    catch (NumberFormatException e) {
                        System.out.println("Inconsistency Report (INVALID LOCATION ID): " + temp);
                        continue;
                    }

                    builder.append(temp).append(',');
                }

                //last char is an extra comma
                builder.deleteCharAt(builder.length() - 1);

                Statement insertStatement = connection.createStatement();
                int result = insertStatement.executeUpdate(builder.toString());

                System.out.println("User table insert completed, " + result + " rows affected.");
            }
            catch (Exception e2) {
                System.out.println(e2.toString());
            }
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }

    }

    public static void main(String[] args) {
        SAXParserUsers spe = new SAXParserUsers();
        spe.runParser();
        spe.insertIntoDatabase();
    }

}