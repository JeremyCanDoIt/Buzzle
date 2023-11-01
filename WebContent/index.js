//Fetch browsing specializations

function handleResult(obj) {
    let specsObj = $("#browsing_specs");
    for (let i = 0; i < obj.length; i++) {
        let str = obj[i]["category"];
        specsObj.append("<a href='listing.html?spec-exact=" + str + "'><p>" + str + "</p></a>")
    }
}

$.ajax({
    url: "api/index",
    method: "GET",
    success: (obj) => handleResult(obj)
})


//Programmatically filling up the A-Z 0-9 * column
function populateTitleLinks() {
    let container = $("#browsing_titles");
    for (let c = 'A'.charCodeAt(0); c <= 'Z'.charCodeAt(0); c++) {
        container.append("<a href='listing.html?starts-with=" + String.fromCharCode(c) + "'><p>" + String.fromCharCode(c) + "</p></a>")
    }
    for (let i = 0; i <= 9; i++) {
        container.append("<a href='listing.html?starts-with=" + i + "'><p>" + i + "</p></a>")
    }

    //Special * case
    //TODO: HACKY WORKAROUND
    container.append("<a href='listing.html?starts-with-special=%25'><p>*</p></a>")
}
populateTitleLinks();


// ########################
// # AUTOCOMPLETE SECTION #
// ########################

//First we bind the text box to the front end autocompleter
$('#autocomplete').autocomplete({
    // documentation of the lookup function can be found under the "Custom lookup function" section
    lookup: function (query, doneCallback) {
        handleACLookup(query, doneCallback)
    },
    onSelect: function(suggestion) {
        handleACSelectSuggestion(suggestion)
    },
    // set delay time
    deferRequestBy: 300,
    minChars: 3,

    // there are some other parameters that you might want to use to satisfy all the requirements
    // TODO: add other parameters
});

const ENTER_KEY = 13; //code for enter key
$('#autocomplete').keypress(function(event) {
    if (event.keyCode === ENTER_KEY) {
        // pass the value of the input box to the handler function
        handleACNormalSearch($('#autocomplete').val())
    }
});

//queries the backend database for services matching what the user has typed
function handleACLookup(query, doneCallback) {
    console.log("Autocomplete: Performing lookup search")

    //Checks if present in cache and returns those results instead
    if (sessionStorage.getItem(query) !== null) {
        console.log("Autocomplete: Retrieved results from cache");
        handleACLookupAjaxSuccess(JSON.parse(sessionStorage.getItem(query)), query, doneCallback, false);
        return;
    }

    console.log("Autocomplete: Cache miss, sending AJAX request");
    // sending the HTTP GET request to the Java Servlet endpoint hero-suggestion
    // with the query data
    jQuery.ajax({
        "method": "POST",
        // generate the request url from the query.
        // escape the query string to avoid errors caused by special characters
        "url": "api/index",
        "data": {
            //NOTE: the "query" here is the search query (dont confuse with SQL query)
            "ac-query": query
        },
        "success": function(data) {
            // pass the data, query, and doneCallback function into the success handler
            handleACLookupAjaxSuccess(data, query, doneCallback, true)
        },
        "error": function(errorData) {
            console.log("lookup ajax error")
            console.log(errorData)
        }
    });
}

function handleACLookupAjaxSuccess(data, query, doneCallback, overwriteCache) {
    //Caches the result
    if (overwriteCache === true) {
        sessionStorage.setItem(query, JSON.stringify(data));
    }

    //log the data
    console.log(JSON.stringify(data));

    // call the callback function provided by the autocomplete library
    // add "{suggestions: jsonData}" to satisfy the library response format according to
    //   the "Response Format" section in documentation
    doneCallback( { suggestions: data } );
}

function handleACSelectSuggestion(suggestion) {
    window.location.href = `single-service.html?id=${suggestion["data"]}`;
    // console.log("Autocomplete: You selected " + suggestion["value"] + " with ID " + suggestion["data"]);
}

function handleACNormalSearch(query) {
    window.location.href = `listing.html?full-text=${query}`;
    // console.log("AC: doing normal search with query: " + query);
}
