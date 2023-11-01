import jakarta.servlet.ServletConfig;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * Abstract Servlet that defines another data source
 * to Master on top of the regular one.
 * @author Thomas
 */
public abstract class AbstractWriteServlet extends AbstractServlet {
    protected DataSource writeDataSource;

    @Override
    public void init(ServletConfig config) {
        //base class
        super.init(config);

        try {
            writeDataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/servicesDB_Master");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

}
