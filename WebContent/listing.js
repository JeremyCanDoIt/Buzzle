
let maxPageIsValid = false;
let maxPages = -1;

/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs two steps:
 *      1. Use jQuery to talk to backend API to get the json data.
 *      2. Populate the data to correct html elements.
 */


/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param result jsonObject
 */
function handleResult(result) {
    //Backend returned saved state --> restore and reload
    if (result.hasOwnProperty("restored_state_string")) {
        window.location.search = result["restored_state_string"];
        return;
    }

    let $cardContainer = jQuery("#service_table_body");
    let resultData = result["values"];

    //Update max pages
    maxPageIsValid = true;
    maxPages = result["max-pages"];

    //Show/hide page PREV and NEXT indicators based on page number
    let getParams= new URLSearchParams(window.location.search);
    let currentPage = (getParams.has("page")) ? parseInt(getParams.get("page")) : 1;
    let buttonHolder = $("#page-navigation-buttons");

    if (currentPage > 1) {
        buttonHolder.append("<button id=\"prev-page\" onClick=\"changePage(-1)\">Previous Page</button>")
    }
    if (currentPage < maxPages) {
        buttonHolder.append("<button id=\"next-page\" onclick=\"changePage(1)\">Next Page</button>");
    }

    //If nothing was returned (no data)
    if (resultData.length === 0) {
        console.log("No data returned from API call");
        $cardContainer.append("<div class='service-card'><p class='empty-data'>No results found.</p></div>")
        return;
    }

    // Populate the service table
    // Find the empty table body by id
    console.log("handleResult: populating service table from resultData");
    // Iterate through resultData, no more than 100 entries
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
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/services" + window.location.search, // Setting request url, which is mapped by its corresponding Java servlet
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the servlet
});

function changePageSize() {
    let params= new URLSearchParams(window.location.search);

    params.set("page-size", $("#page-size").val());
    params.set("page", "1"); //changing page size will reset page to 1

    window.location.search = params.toString();
}

function changePage(ch) {
    let params= new URLSearchParams(window.location.search);

    let page = (params.has("page")) ? parseInt(params.get("page")) : 1;

    //TODO: this is inefficient because I'm clamping twice but I am also running on like 4 hours of sleep rn, fix this later

    //Clamp value
    page = Math.max(page, 1);
    if (maxPageIsValid) {
        page = Math.min(page, maxPages);
    }

    page += ch;

    //Clamp value
    page = Math.max(page, 1);
    if (maxPageIsValid) {
        page = Math.min(page, maxPages);
    }

    params.set("page", page);
    window.location.search = params.toString();
}

function fixDropdowns() {
    let params= new URLSearchParams(window.location.search);

    if (params.has("page-size")) {
        $("#page-size").val(params.get("page-size"));
    }
    if (params.has("sort-order")) {
        $("#sort-order").val(params.get("sort-order"));
    }
}
fixDropdowns();

function changeSortOrder() {
    let params= new URLSearchParams(window.location.search);
    params.set("sort-order", $("#sort-order").val());
    window.location.search = params.toString();
}
function handleAddEvent(serviceID){
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