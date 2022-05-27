//On document ready
$(document).ready(function () {
    //initial page load to initialize localStorage variables
    if (localStorage.getItem("maxURLs") == null) {
        localStorage.setItem("maxURLs", "0");
    }
    if (localStorage.getItem("useMultithreading") == null) {
        localStorage.setItem("useMultithreading", "true");
    }
    if (document.title !== "Settings") return;

    //Setting values on setting page using localStorage variables
    let slider = $("input[type=range]");
    slider.val(localStorage.getItem("maxURLs"))
    $("#range-value").html(slider.val())
    $("input[type=checkbox]").prop("checked", localStorage.getItem("useMultithreading") === "true")
})

//Updating value for range slider and localStorage variables
$("input[type=range]").on("change input", function () {
    $("#range-value").html($(this).val())
    localStorage.setItem("maxURLs", $(this).val())
})
$("input[type=checkbox]").click(function () {
    localStorage.setItem("useMultithreading", $(this).is(":checked"))
})

//On URL Search
$("button[type=submit]").click(function (e) {
    e.preventDefault()
    let url = $("input[type=search]").val()

    //Checking if URL is valid
    if (url.length === 0 || !(url.includes("http") || url.includes("ftp"))) return;

    $("#scrape-info-images").text("Loading images...")
    $("button[type=submit]").attr("disabled", "")
    $("#image-list").empty()

    let maxURLs = localStorage.getItem("maxURLs")
    let useMultithreading = localStorage.getItem("useMultithreading")

    makeApiCall('/main?url=' + encodeURIComponent(url) + "&maxURLs=" + maxURLs + "&useMultithreading=" + useMultithreading, 'POST', null, updateImages);
})

function createImage(url) {
    let linkElement = $('<a href="' + url + '" target="_blank">')
    let imageContainer = $('<div class="image-container rounded">')
    let imageExtensionDiv = $('<div class="image-extension">' + getURLExtension(url) + '</div>')

    //Appending image to link
    linkElement.append($('<img class="img-thumbnail" src="' + url + '" style="width: 100px; height: 100px; object-fit: contain">'))
    //Appending link and extension to image container
    imageContainer.append(linkElement)
    imageContainer.append(imageExtensionDiv)

    //Adding image to list
    $("#image-list").append(imageContainer)
}

function updateImages(response) {
    for (let i = 0; i < response.length; i++) {
        createImage(response[i])
    }

    //After all images added, update text and re-enable button
    $("#scrape-info-images").text(response.length + " images found")
    $("button[type=submit]").removeAttr("disabled")
}

function makeApiCall(url, method, obj, callback) {
    let xhr = new XMLHttpRequest();
    xhr.open(method, url);
    xhr.onreadystatechange = apiCallBack.bind(null, xhr, callback);
    xhr.send(obj ? obj instanceof FormData || obj.constructor == String ? obj : JSON.stringify(obj) : null);
}

function apiCallBack(xhr, callback) {
    if (xhr.readyState == XMLHttpRequest.DONE) {
        if (xhr.status != 200) {
            let message = xhr.status + ":" + xhr.statusText + ":" + xhr.responseText;
            alert(message);
            throw 'API call returned bad code: ' + xhr.status;
        }
        let response = xhr.responseText ? JSON.parse(xhr.responseText) : null;
        if (callback) {
            callback(response);
        }
    }
}

function getURLExtension(url) {
    let endOfUrl = url.split(/[#?]/)[0].split('.').pop().trim().toUpperCase()
    try {
        if (endOfUrl.substring(0, 4) === "JPEG") {
            return "JPEG";
        } else {
            let ext = endOfUrl.substring(0, 3)
            if (ext === "PNG" || ext === "JPG" || ext === "GIF" || ext === "SVG") {
                return ext;
            } else {
                return "???";
            }
        }
    } catch (e) {
        return "???";
    }
    //TODO add encrypted image handling
    //fetch("https://encrypted-tbn1.gstatic.com/images?q=tbn:ANd9GcTA_Rg2GwJVJEmOGGoYFev_eTSZAjkp_stpi4cUXpjWbE6Wh7gSpCvldg", {method:"HEAD"})
    //.then(response => response.headers.get("Content-Type"))
    //.then(type => console.log(`.${type.replace(/.+\/|;.+/g, "")}`));
}


