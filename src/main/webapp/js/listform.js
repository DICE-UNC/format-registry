$(":input").keyup(function() {
    var anytext = false;
    var alltext = true;
    $("input:text").each(function(index, element) {
        if (element.value) {
            anytext = true;
        } else {
            alltext = false;
        }
    });
    $("#add_new").attr("disabled", !alltext);
    $("#clear").attr("disabled", !anytext);
});

$("#add_new").click(function() {
    $(":button").attr("disabled", true);
    var formData = {class: CLASS_URI, entity_name: $("#entity_name").val(), entity_id: $("#entity_id").val()};

    $.ajax({
        type: "POST",
        url: POST_URL,
        data: {formData: JSON.stringify(formData)},
        success: function(data) {
            if (data.created) {
                $("input:text").val("");

                var newlink = $(document.createElement("a"));
                newlink.addClass("new-item");
                newlink.attr("href", "/edit?entity=" + encodeURIComponent(data.id));
                newlink.append(document.createTextNode(data.name));

                var list_item = $(document.createElement("li"));
                list_item.append(newlink);
                $(".item-list").append(list_item);
            } else {
                $(":button").attr("disabled", false);
                alert("Trouble creating item");
            }
        },
        dataType:"json"
    });
});

$("#clear").click(function() {
    $(":button").attr("disabled", true);
    $("input:text").val("");
});
