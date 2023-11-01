/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs three steps:
 *      1. Get parameter from request URL so it know which id to look for
 *      2. Use jQuery to talk to backend API to get the json data.
 *      3. Populate the data to correct html elements.
 */

//maybe this doesn't work

/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {

    console.log("handleResult: populating single service info from resultData");

    // populate the star info h3
    // find the empty h3 body by id "star_info"
    // jQuery("#service_title").append("<h1>" + resultData[0]["service_title"] + "</h1>");

    let cols = [
        "service_title",
        "service_description",
        "service_price",
        "service_payment_type",
        "service_posted_date",
        "service_status",
        "seller_name",
        "seller_username",
        "seller_avg_rating",
    ];

    // Populate the star table
    // Find the empty table body by id "movie_table_body"

    cols.forEach((col) => jQuery("#".concat(col)).append(resultData[0][col]));

    jQuery("#seller_link").attr("href", "single-seller.html?id=" + resultData[0]["seller_id"]);
}





/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let serviceID = getParameterByName('id');



function handleAddEvent(){
    console.log("submit item");
    /**
     * When users click the submit button, calls this function
     * creates a dictionary of all the items to be saved
     * event handler when the event is triggered.
     */
    let serviceData={
        "service_id": serviceID,
        "action": "add_item"
    };
    jQuery.ajax("api/shopping-cart", {
        method: "POST",
        data: serviceData,
        success: resultDataJson => {
            if (resultDataJson["success"]=== true) {
                window.location.href="./shopping-cart.html"
            }
            else {
                //Display the error message
                $("#error-message-display").append(resultDataJson["errorMessage"]);
            }

            // handleCartArray(resultDataJson["previousItems"]);
        }
    });
}
// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-service?id=" + serviceID,
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});