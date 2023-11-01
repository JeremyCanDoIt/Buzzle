//PERFORM ON-LOAD AJAX CALL
function handleLoad(returnObj) {
    //Populate the schema information
    let schemaHolderObj = $("#schema_holder");
    let schema= returnObj["schema"];

    for (let i = 0; i < schema.length; i++) {
        //base template + table name
        let str = `<div class="service-card"><h3>${schema[i]["name"]}</h3><ul>`;

        let cols = schema[i]["columns"];
        //populate in the column names and types
        for (let c = 0; c < cols.length; c++) {
            str += `<li>${cols[c]["column_name"]} : ${cols[c]["data_type"]}</li>`;
        }

        str += "</ul></div>";
        schemaHolderObj.append(str);
    }

    //Populate the general location information
    let locationHolderObj = $("#location");
    let loc = returnObj["location_data"];
    for (let i = 0; i < loc.length; i++) {
        locationHolderObj.append(`<option value=\"${loc[i]["location_id"]}\">${loc[i]["city"]}, ${loc[i]["state"]}</option>`);
    }
}

$.ajax(
    {
        url: "api/_dashboard",
        method: "GET",
        success: (returnObj) => handleLoad(returnObj)
    }
)

//FORM SUBMIT FOR NEW SELLER
$("#create-seller").submit((event) => {
    event.preventDefault();

    $.ajax({
        url: "api/_dashboard",
        method: "POST",
        data: $("#create-seller").serialize() + "&action=add_seller",
        success: (retObj) => {
            if (retObj["success"] === false) {
                $("#seller-insert-status").html(retObj["errorMessage"]);
            }
        }
    });
});

//FORM SUBMIT FOR NEW SERVICE
$("#create-service").submit((event) => {
    event.preventDefault();

    $.ajax({
        url: "api/_dashboard",
        method: "POST",
        data: $("#create-service").serialize() + "&action=add_service",
        success: (retObj) => {
            if (retObj["success"] === false) {
                $("#service-insert-status").html(retObj["errorMessage"]);
            }
        }
    });
});

