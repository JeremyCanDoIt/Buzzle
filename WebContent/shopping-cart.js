




function handleSessionData(resultDataString) {
    let resultDataJson = resultDataString;

    console.log("handle session response");
    console.log(resultDataJson);
    console.log(resultDataJson["sessionID"]);

    // show the session information
    $("#sessionID").text("Session ID: " + resultDataJson["sessionID"]);
    $("#lastAccessTime").text("Last access time: " + resultDataJson["lastAccessTime"]);

    // show cart information
    handleCartArray(resultDataJson["cart"]);
}


/**
 * Handle the items in item list
 * @param resultArray jsonObject, needs to be parsed to html
 */
function handleCartArray(resultData) {
    // console.log(resultArray);
    // let item_list = $("#item_list");
    // // change it to html list
    // let res = "<ul>";
    // for (let i = 0; i < resultArray.length; i++) {
    //     // each item will be in a bullet point
    //     res += "<li>" + resultArray[i] + "</li>";
    // }
    // res += "</ul>";
    //
    // // clear the old array and show the new array in the frontend
    // item_list.html("");
    // item_list.append(res);
    // Populate the service table
    // Find the empty table body by id
    let $cardContainer = jQuery("#service_table_body");
    console.log("handleResult: populating service table from resultData");
    for (let i = 0; i < resultData.length; i++) {

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

        rowHTML  +="<tr>" +'<td>'+
            resultData[i]["service_price"] +'</td>'+
            "</tr>";
        rowHTML  +="<tr>" +'<td>'+
            resultData[i]["service_status"] +'</td>'+
            "</tr>";
        rowHTML  +="<tr>" +'<td>'+
            resultData[i]["service_posted_date"] +'</td>'+
            "</tr>";
        //Quantity and +/- buttons
        rowHTML += "&nbsp;<tr><td>Quantity: " + resultData[i]["quantity"];
        rowHTML += `<button onClick=\"changeQty(${resultData[i]["service_id"]}, -1)\">-</button>`;
        rowHTML += `<button onClick=\"changeQty(${resultData[i]["service_id"]}, 1)\">+</button>`;
        rowHTML += "</td></tr>";

        //Add remove button
        rowHTML += `<tr><td><button onClick=\"remove(${resultData[i]["service_id"]})\">Remove</button></td></tr>`;

        // rowHTML += "<th>$" + resultData[i]["service_price"] + "</th>";
        // rowHTML += "<th>" + resultData[i]["service_date"] + "</th>";
        // rowHTML += "<th>" + resultData[i]["service_status"] + "</th>";
        rowHTML += "</tbody></table></div>";
        // Append the row created to the table body, which will refresh the page
        console.log(rowHTML);
        $cardContainer.append(rowHTML);
    }
}


jQuery.ajax({
    method: "GET",// Setting request method
    url: "api/shopping-cart", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleSessionData(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});


function changeQty(id, delta) {
    let dataObj = {
        "action": "change_qty",
        "service_id" : id,
        "qty_change": delta,
    }

    $.ajax({
        method: "POST",
        data: dataObj,
        url: "api/shopping-cart",
        success: (resultData) => window.location.reload()
    });
}

function remove(id) {
    let dataObj = {
        "action": "remove",
        "service_id": id,
    };

    $.ajax(
        {
            method: "POST",
            data: dataObj,
            url: "api/shopping-cart",
            success: (resultData) => window.location.reload()
        }
    )
}