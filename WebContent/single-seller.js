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
    console.log("SingleSeller: Handling Single Seller result");

    //Populate common data
    $("#display_name").append(resultData["single_seller"]["name"]);
    $("#username").append("@" + resultData["single_seller"]["username"]);
    $("#seller_type").append(resultData["seller_type"]);

    //Populate specializations
    let specData = resultData["single_seller"]["specializations"];
    if (specData !== null && specData.length > 0) {
        let specListObj = $("#specializations");
        specListObj.append("<h3>Specializations</h3>");

        let list = "<ul>";

        for (let i = 0; i < specData.length; i++) {
            list += "<li>" + specData[i]["category"] + "</li>";
        }
        list += "</ul>";

        specListObj.append(list);
    }

    //Populate description if it exists, or display empty desc. message
    let descriptionData = resultData["single_seller"]["profile_description"];
    if (descriptionData !== null) {
        $("#description").append(descriptionData);
    }
    else {
        $("#description").append("<i>No description provided.</i>");
    }

    //Populate Contact Box Generic Info
    let contactBoxObj = $("#contact-info");
    let website = resultData["single_seller"]["website"];
    if (website !== null) {
        contactBoxObj.append("<a href=\"" + website + "\">" + website + "</a>\n");
    }

    //Company-specific contact info
    if (resultData["seller_type"] === "company") {
        //No need to null check -- these are guaranteed not null by database
        contactBoxObj.append("<p>" + resultData["single_seller"]["phone_number"] + "</p>");
        contactBoxObj.append("<p>" + resultData["single_seller"]["address"] + "</p>");
    }

    populateServiceTable(resultData["related_services"]);
}

function populateServiceTable(resultData) {

    // Populate the service table
    // Find the empty table body by id
    let $cardContainer = jQuery("#service_table_body");
    console.log("handleResult: populating service table from resultData");
    for (let i = 0; i < Math.min(100, resultData.length); i++) {

        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "<div class=\"service-card\">" +
            "<table><tbody>";

        rowHTML +="<tr>" +'<td>'+'<a class= \"gradient-text\" href="single-service.html?id=' + resultData[i]['service_id'] + '">'
            + resultData[i]["service_title"] +     // display service title for the link text
            '</a>' +'</td>'+
            "</tr>";

        //Seller Name
        rowHTML  +="<tr>" +'<td>'+
            // Add a link to seller's profile page with id passed with GET url parameter
            '<a class= \"gradient-text\" href="single-seller.html?id=' + resultData[i]['seller_id'] + '">'
            + resultData[i]["seller_name"] +     // display seller's name for the link text
            '</a>' +'</td>'+
            "</tr>";
        // add row html for extra column

        rowHTML  +="<tr>" +'<td>'+"$"+
            resultData[i]["service_price"] +'</td>'+
            "</tr>";
        rowHTML  +="<tr>" +'<td>'+
            resultData[i]["service_status"] +'</td>'+
            "</tr>";
        rowHTML  +="<tr>" +'<td>'+
            resultData[i]["service_date"] +'</td>'+
            "</tr>";
        rowHTML  +="<tr>"+'<td>'+"<div class='bottom-button'>"+
            "<button onclick='handleAddEvent("+resultData[i]["service_id"]+")' class='add-button'>Add</button></div>"+
            "<p id='error-message-display'></p>"+ "</div>"+'</td>'+ "</tr>";

        // rowHTML += "<th>$" + resultData[i]["service_price"] + "</th>";
        // rowHTML += "<th>" + resultData[i]["service_date"] + "</th>";
        // rowHTML += "<th>" + resultData[i]["service_status"] + "</th>";
        rowHTML += "</tbody></table></div>";
        // Append the row created to the table body, which will refresh the page
        console.log(rowHTML);
        $cardContainer.append(rowHTML);
    }
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let sellerId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-seller?id=" + sellerId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});

function handleAddEvent(serviceID) {
    console.log("submit item");
    /**
     * When users click the submit button, calls this function
     * creates a dictionary of all the items to be saved
     * event handler when the event is triggered.
     */
    let serviceData = {
        "service_id": serviceID,
        "action": "add_item"
    };
    jQuery.ajax("api/shopping-cart", {
        method: "POST",
        data: serviceData,
        success: resultDataJson => {
            if (resultDataJson["success"] === true) {
                window.location.href = "./shopping-cart.html"
            } else {
                //Display the error message
                $("#error-message-display").append(resultDataJson["errorMessage"]);
            }

            // handleCartArray(resultDataJson["previousItems"]);
        }
    });
}