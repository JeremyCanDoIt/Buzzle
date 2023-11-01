# CS122B Spring 2023 Group JT

## SQL Database
We used a custom domain for our project, so we have a custom database schema and instance.
The .sql files to set up the database are found on the git branch called `data-cooking`. 

I've added a copy of the two files here for grading purposes.
They are identical to the ones in Demo Video 2 and on the other branch.
Please note that we did make minor changes to the database between Project 1 and 2.

## Logins

User Login: (`/buzzle_project/login.html`)
 - username: `mshulemf4`
 - password: `YzRyIb`

Employee Login: (`/buzzle_project/_employee-login.html`)
 - email: `classta@email.edu`
 - password: `classta`

## Custom Domain Name
http://buzzle122b.xyz/
- # General
    - #### Team#:
     
    - #### Names:
     - Jeremy Chang (jeremsc2)
     - Thomas Tran (thomatt6)
    - #### Project 5 Video Demo Link:

    - #### Instruction of deployment:
    
    - #### Collaborations and Work Distribution:
          Jeremy Chang (jeremsc2):
          - Helped duplicate tomcat server for master, slave, and load balancer
          - Created CSV file
          - Jmeter
          - time measurement and performance results

          Thomas Tran (thomatt6):
          - Enabled JDBC Connection Pooling
          - Duplicated tomcat server for master, slave, and load balancer
          - Wrote script for average time
          - Jmeter
          - time measurement and performance results



- # Connection Pooling
    - #### Include the filename/path of all code/configuration files in GitHub of using JDBC Connection Pooling.
    
    - #### Explain how Connection Pooling is utilized in the Fabflix code.
    
    - #### Explain how Connection Pooling works with two backend SQL.
    

- # Master/Slave
    - #### Include the filename/path of all code/configuration files in GitHub of routing queries to Master/Slave SQL.

    - #### How read/write requests were routed to Master/Slave SQL?
    

- # JMeter TS/TJ Time Logs
    - #### Instructions of how to use the `log_processing.*` script to process the JMeter logs.


- # JMeter TS/TJ Time Measurement Report

| **Single-instance Version Test Plan**          | **Graph Results Screenshot** | **Average Query Time(ms)** | **Average Search Servlet Time(ms)** | **Average JDBC Time(ms)** | **Analysis** |
|------------------------------------------------|------------------------------|----------------------------|-------------------------------------|---------------------------|--------------|
| Case 1: HTTP/1 thread                          | ![](path to image in img/)   | 84                         | 5.747701847090664                   | 2.3387073125845736                        | http 1 thread was the fastest because no security checks and only 1 threead
          |
| Case 2: HTTP/10 threads                        | ![](path to image in img/)   | 88                         |12.552260782209775                                   | 6.173559137145065                         | http 10 thread was the second fastest because no security checks and 10 threads        
| Case 3: HTTPS/10 threads                       | ![](path to image in img/)   | 99                         | 16.1597442281233                    | 8.309941412289058         | https 10 thread was the second slowest because of security checks and 10 threads           |
| Case 4: HTTP/10 threads/No connection pooling  | ![](path to image in img/)   | 81                         | 28.656177413885313                                   | 13.84901096068139                         |  10 thread was the slowest because of 10 threads with no pooling.           |

| **Scaled Version Test Plan**                   | **Graph Results Screenshot** | **Average Query Time(ms)** | **Average Search Servlet Time(ms)** | **Average JDBC Time(ms)** | **Analysis** |
|------------------------------------------------|------------------------------|----------------------------|-------------------------------------|---------------------------|--------------|
| Case 1: HTTP/1 thread                          | ![](path to image in img/)   |57                         | 5.8616052763448785                                   | 2.4043350058953576                         | http 1 thread was the fastest because no security checks and only 1 threead           |
| Case 2: HTTP/10 threads                        | ![](path to image in img/)   | 69                         | 18.809006826648762                  | 9.656157788625418         | http 10 thread was the second slowest because no security checks and 10 threads         |
| Case 3: HTTP/10 threads/No connection pooling  | ![](path to image in img/)   | 83                        | 36.258958175847281                                 | 23.837247490048755                        | http 10 thread was the  slowest because no security checks and 10 threads with no pooling.           |
## Project 4 Demo Video Link
https://youtu.be/9IWJLtTTQfw

## Project 4 Contributions
For Project 4: The file to add the fulltext index is fulltext.sql. It adds two, for Services table columns `title` and `description`.

### Jeremy Chang (jeremsc2):
- project management
- helped work on android app

### Thomas Tran (thomatt6):
- added autocomplete
- added fulltext searching
- made android app

---

## Optimization Strategies
- 1. Bulk insert query
- 2. Filter inconsistencies during parse
- 3. StringBuilder instead of default Strings

## Project 3 Demo Video Link
https://youtu.be/_cSuMhNcO2c

## Project 3 Contributions

### Jeremy Chang (jeremsc2):
- Created Dashboard front-end
- Added backend to employee login
- Worked on HTML and CSS
- Created parsers and XML data

### Thomas Tran (thomatt6):
- Created Dashboard backend
- Added captchas
- Added HTTPS certificates
- Changed statements to prepared statements
- Created stored procedure for dashboard
- Added parser connection to server
- Helped create dashboard front-end
- Edited the password hashing scripts

---

## Substring Matching Design
Used LIKE clause on SQL query and concatenated the keyword with % symbols at the beginning and end.

## Project 2 Demo Video Link
https://youtu.be/KlhUqMi92s4

## Project 2 Contributions

### Jeremy Chang (jeremsc2):
- Helped setup shopping cart backend + pages
- Worked on servlet for shopping cart
- Updated UI on every page
- Coded Navbar for every page
- Wrote html and css

### Thomas Tran (thomatt6):
- Made changes to database schema and instance
- Created Main page Search and Browse
- Added session jump functionality
- Added pagination and sorting
- Helped implement shopping cart and payment

---

## Project 1 Demo Video Link
https://youtu.be/GpSWLhu4Kg8

## Project 1 Contributions

### Jeremy Chang (jeremsc2):
- Helped setup sql database and schema
- Worked on servlet for services and single seller
- Updated listing.js/html
- Coded Single-Seller.html and Single-Seller.js
- Wrote html and css

### Thomas Tran (thomatt6):
- Worked on Java SingleServiceServlet and AbstractServlet
- Created the database schema (createtable.sql)
- Generated and scraped data for the database instance (populate-data.sql)
- Wrote front-end for Single Service (HTML/JS)
- Managed the AWS deployment
