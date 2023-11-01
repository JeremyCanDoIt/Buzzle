
import java.io.IOException;
import java.sql.*;
import java.util.*;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

public class SAXParserServices extends DefaultHandler {

    List<Map<String, String>> myServices;

    private String tempVal;

    //to maintain context
    private Map<String, String> tempService;

    public SAXParserServices() {
        myServices = new ArrayList<Map<String, String>>();
    }

    public void runParser() {
        parseDocument();
    }

    //Returns a List of Maps of representing Services
    public List<Map<String, String>> returnServices() {
        return myServices;
    }

    private void parseDocument() {

        //get a factory
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {

            //get a new instance of parser
            SAXParser sp = spf.newSAXParser();

            //parse the file and also register this class for call backs
            sp.parse("services-cambridge.xml", this);

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
            tempService = new HashMap<String, String>();

        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {

        if (qName.equalsIgnoreCase("record")) {
            //add it to the list
            myServices.add(tempService);

        }
        else if (qName.equalsIgnoreCase("seller_id")) {
            tempService.put("seller_id",tempVal );
        }
        else if (qName.equalsIgnoreCase("title")) {
            tempService.put("title",tempVal );
        } else if (qName.equalsIgnoreCase("description")) {
            tempService.put("description",tempVal );
        } else if (qName.equalsIgnoreCase("price")) {
            tempService.put("price",tempVal );
        }
        else if (qName.equalsIgnoreCase("posted_date")) {
            tempService.put("posted_date",tempVal );
        } else if (qName.equalsIgnoreCase("status")) {
            tempService.put("status",tempVal );
        } else if (qName.equalsIgnoreCase("payment_type")) {
            tempService.put("payment_type",tempVal );
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
                final String query = "SELECT u.id FROM User u INNER JOIN Seller s ON u.id = s.sellerID";
                PreparedStatement statement = connection.prepareStatement(query);
                ResultSet results = statement.executeQuery();
                final HashSet<Integer> sellerIDs = new HashSet<>();

                while (results.next()) {
                    sellerIDs.add(results.getInt("id"));
                }

                statement.close();
                results.close();

                final StringBuilder builder = new StringBuilder("INSERT IGNORE INTO Services(sellerID, title, description, price, posted_date, status, payment_type) VALUES ");

                for (final Map<String, String> entry : myServices) {
                    final StringBuilder temp = new StringBuilder();

                    temp.append("(")
                            .append(entry.get("seller_id"))
                            .append(",\"")
                            .append(entry.get("title"))
                            .append("\",\"")
                            .append(entry.get("description"))
                            .append("\",")
                            .append(entry.get("price"))
                            .append(",\"")
                            .append(entry.get("posted_date"))
                            .append("\",\"")
                            .append(entry.get("status"))
                            .append("\",\"")
                            .append(entry.get("payment_type"))
                            .append("\")");

                    //Validate the data (price and id)
                    try {
                        Float.parseFloat(entry.get("price"));

                        int id = Integer.parseInt(entry.get("seller_id"));
                        //Validate the seller id
                        if (!sellerIDs.contains(id)) {
                            System.out.println("Inconsistency Detected (INVALID SELLER ID): " + temp);
                            continue;
                        }
                    }
                    catch (NumberFormatException e) {
                        System.out.println("Inconsistency Detected (INVALID PRICE): " + temp);
                        continue;
                    }

                    //Append to the main stringbuilder
                    builder.append(temp).append(',');
                }

                //last char is an extra comma
                builder.deleteCharAt(builder.length() - 1);

                Statement insertStatement = connection.createStatement();
                int result = insertStatement.executeUpdate(builder.toString());

                System.out.println("Services table insert completed, " + result + " rows affected.");
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
        SAXParserServices spe = new SAXParserServices();
        spe.runParser();

//        spe.printData();

        spe.insertIntoDatabase();
    }
}