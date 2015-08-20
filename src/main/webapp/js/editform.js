var initial = null;
var loadInitial = function (callback) {
    if (initial) {
        callback(initial);
        return;
    }

    $.ajax(document.URL + "&json=true").done(function (data) {
        initial = data;
        callback(data);
    });
};

var isEmpty = function(obj) {
    for (var key in obj) {
        if (Object.prototype.hasOwnProperty.call(obj, key)) {
            return false;
        }
    }
    return true;
};

var getValues = function(fieldContainer) {
    var ret = [];
    $(".field-value", fieldContainer).not(".short-text").each(function(index, element) {
        if (element.options) {
            var value = element.options[element.selectedIndex].value;
            if (value !== "none") {
                ret.push(value);
            }
        } else {
            if (element.value) {
                ret.push(element.value);
            }
        }
    });
    return ret;
};

var stopEdit = function () {
    //Update all the "p" fields
    $(".view-mode").not(".btn").each(function(index, p) {
        $(".field-value", $(p).parent()).not(".short-text").each(function(index, valueElement) {
            $(p).empty();
            if (valueElement.options) {
                var value = valueElement.options[valueElement.selectedIndex].value;
                if (value !== "none") {
                    p.appendChild(document.createTextNode(valueElement.options[valueElement.selectedIndex].label));
                }
            } else {
                if (valueElement.value) {
                    p.innerHTML = valueElement.value.replace(/\n/g, '<br/>');
                }
            }
        });
    });

    //Hide empty field containers
    $(".field-container").each(function (index, element) {
        var values = getValues(element);
        if (!values.length) {
            $(element).hide();
        }
    });

    //Toggle the appropriate controls
    $(".edit-mode").hide();
    $(".view-mode").show();
};

var startEdit = function () {
    //Show all field containers
    $(".field-container").show();

    //Toggle the appropriate controls
    $(".edit-mode").show();
    $(".view-mode").hide();

    //Pick the correct type of text field
    $(".short-text").each(function(index, element) {
        flipTextInputs(element.getAttribute("id"), undefined);
    });
};

var isShort = function (value) {
    if (! value || !value.length) {
        return true;
    }
    return value.length < 40 && value.indexOf("\n") === -1;
};

var resetForm = function (state) {
    var top = $("#form-container");
    top.empty();

    var header = $(document.createElement("h1"));
    header.append(document.createTextNode(state.name));
    top.append(header);

    var formDiv = $(document.createElement("div"));
    formDiv.addClass("form-horizontal");
    top.append(formDiv);

    var lockedFields = $(document.createElement("fieldset"));
    lockedFields.addClass("well");
    formDiv.append(lockedFields);

    var classGroup = $(document.createElement("div"));
    classGroup.addClass("control-group");
    lockedFields.append(classGroup);

    var classLabel = $(document.createElement("label"));
    classLabel.addClass("control-label");
    classLabel.css("font-weight","bold");
    classLabel.append(document.createTextNode("Class"));
    classGroup.append(classLabel);

    var classControls = $(document.createElement("div"));
    classControls.addClass("controls");
    classGroup.append(classControls);

    var classValue = $(document.createElement("p"));
    classValue.addClass("help-inline");
    classValue.append(document.createTextNode(state.classDescriptor.label));
    classControls.append(classValue);

    var titleGroup = $(document.createElement("div"));
    titleGroup.addClass("control-group");
    lockedFields.append(titleGroup);

    var titleLabel = $(document.createElement("label"));
    titleLabel.addClass("control-label");
    titleLabel.css("font-weight", "bold");
    titleLabel.append(document.createTextNode("Title"));
    titleGroup.append(titleLabel);

    var titleControls = $(document.createElement("div"));
    titleControls.addClass("controls");
    titleGroup.append(titleControls);

    var titleValue = $(document.createElement("p"));
    titleValue.addClass("help-inline");
    titleValue.append(document.createTextNode(state.name));
    titleControls.append(titleValue);

    var idGroup = $(document.createElement("div"));
    idGroup.addClass("control-group");
    lockedFields.append(idGroup);

    var idLabel = $(document.createElement("label"));
    idLabel.addClass("control-label");
    idLabel.css("font-weight", "bold");
    idLabel.append(document.createTextNode("Identifier"));
    idGroup.append(idLabel);

    var idControls = $(document.createElement("div"));
    idControls.addClass("controls");
    idGroup.append(idControls);

    var idValue = $(document.createElement("p"));
    idValue.addClass("help-inline");
    idValue.append(document.createTextNode(state.id));
    idControls.append(idValue);

    var fieldGroup = $(document.createElement("fieldset"));
    fieldGroup.addClass("well");
    formDiv.append(fieldGroup);

    var i, len;
    for (i = 0, len = state.fields.length; i < len; i++) {
        var fieldDesc = state.fields[i];
        fieldGroup.append(createField(i, fieldDesc.label, fieldDesc.uri, fieldDesc.functional, fieldDesc.datatype, fieldDesc.values, fieldDesc.allowedValues, fieldDesc.comment));
    }

    var actionDiv = $(document.createElement("div"));
    actionDiv.addClass("form-actions");
    fieldGroup.append(actionDiv);

    var submitButton = $(document.createElement("button"));
    submitButton.addClass("btn");
    submitButton.addClass("btn-primary");
    submitButton.addClass("edit-mode");
    submitButton.attr("id", "btn-submit");
    submitButton.attr("type", "submit");
    submitButton.append(document.createTextNode("Save Changes"));

    var saveImage = $(document.createElement("i"));
    saveImage.addClass("icon-file");
    submitButton.append(saveImage);

    actionDiv.append(submitButton);

    actionDiv.append(document.createTextNode(" "));

    var cancelButton = $(document.createElement("button"));
    cancelButton.addClass("btn");
    cancelButton.addClass("edit-mode");
    cancelButton.append(document.createTextNode("Cancel"));
    cancelButton.attr("id", "btn-cancel");
    actionDiv.append(cancelButton);

    var editButton = $(document.createElement("button"));
    editButton.addClass("btn");
    editButton.addClass("view-mode");
    editButton.append(document.createTextNode("Edit"));
    editButton.attr("id", "btn-edit");
    actionDiv.append(editButton);

    submitButton.on("click", function (event) {
        submitForm();
    });
    cancelButton.on("click", function (event) {
        resetForm(state);
    });
    editButton.on("click", function (event) {
        startEdit();
    });
    stopEdit();
};

var submitForm = function () {
    var formBody = {
        entityUri: initial.id
    };
    var fieldData = {};
    $(".field-container").each(function (index, element) {
        var values = getValues(element);
        if (values.length) {
            fieldData[element.id.substr(4)] = values;
        }
    });
    formBody["fields"] = fieldData;

    var formData = new FormData();
    formData.append("formData", JSON.stringify(formBody));
    $(":file").each(function(index, element) {
        if (element.files[0]) {
            var filename = $("#" + escapeIdentifier(element.id + "name")).attr("value");
            formData.append("file:" + filename, element.files[0]);
        }
    });

    $(":button").attr("disabled", true);
    $.ajax({
        type:"POST",
        url: document.URL,
        data: formData,
        success:function (data) {
            $(":button").attr("disabled", false);
            stopEdit();
        },
        dataType: "json",
        processData: false,
        contentType: false
    });
};

var escapeIdentifier = function(id) {
    return ("" + id).replace(/([\\\/#:\.])/g, "\\$1");
};

var flipTextInputs = function(id, changeLarge) {
    id = escapeIdentifier(id);
    var small = $("#" + id);
    var large = $("#" + id + "-long");
    var value = changeLarge ? large.attr("value") : small.attr("value");
    if (changeLarge === true) {
        small.attr("value", value);
    } else if (changeLarge === false) {
        large.attr("value", value);
    }
    if (isShort(value)) {
        small.show();
        if (changeLarge === true) {
            var tmp = doGetCaretPosition(large[0]);
            small.focus();
            setCaretPosition(small[0], tmp);
        }
        large.hide();
    } else {
        large.show();
        if (changeLarge === false) {
            var tmp = doGetCaretPosition(small[0]);
            large.focus();
            setCaretPosition(large[0], tmp);
        }
        small.hide();
    }
};

var doGetCaretPosition = function(ctrl) {
    var pos = 0;
    if (document.selection) {
        //IE support
        ctrl.focus ();
        var Sel = document.selection.createRange ();
        Sel.moveStart ("character", -ctrl.value.length);
        pos = Sel.text.length;
    } else if (ctrl.selectionStart || ctrl.selectionStart == "0") {
        // Firefox support
        pos = ctrl.selectionStart;
    }
    return (pos);
};

var setCaretPosition = function (ctrl, pos) {
    if(ctrl.setSelectionRange) {
        ctrl.focus();
        ctrl.setSelectionRange(pos,pos);
    } else if (ctrl.createTextRange) {
        var range = ctrl.createTextRange();
        range.collapse(true);
        range.moveEnd("character", pos);
        range.moveStart("character", pos);
        range.select();
    }
};

var deleteFunctional = function(id) {
    $("#" + escapeIdentifier(id + "-p")).hide("fast", function() {
        $(this).remove();
    });
};

var appendRawTextInput = function(id, value, parent) {
    var p = $(document.createElement("p"));
    p.addClass("view-mode");
    p.hide();

    //Text input field
    var input = $(document.createElement("input"));
    input.addClass("input-large");
    input.addClass("field-value");
    input.addClass("edit-mode");
    input.addClass("short-text");
    input.on("keyup", function() {flipTextInputs(id + "-input", false);});
    input.attr("id", id + "-input");
    input.attr("name", id + "-input");

    //Textarea field
    var textarea = $(document.createElement("textarea"));
    textarea.addClass("input-large");
    textarea.addClass("field-value");
    textarea.addClass("edit-mode");
    textarea.attr("rows", 5);
    textarea.hide();
    textarea.on("keyup", function() { flipTextInputs(id + "-input", true);});
    textarea.attr("id", id + "-input-long");
    textarea.attr("name", id + "-input-long");

    if (value.length) {
        textarea.attr("value", value);
        input.attr("value", value);
        p.innerHTML = value.replace(/\n/g, '<br/>');
    }
    parent.append(textarea);
    parent.append(input);
    parent.append(p);
};

var appendFileInput = function(id, value, parent) {
    var a = $(document.createElement("a"));
    a.addClass("view-mode");
    a.attr("href", "/download?entityUri=" + initial.id + "&filename=" + value);
    a.attr("id", id + "-filelink");
    a.append(document.createTextNode(value));
    a.hide();

    //Text input field
    var fileInput = $(document.createElement("input"));
    fileInput.hide();
    fileInput.attr("type", "file");
    fileInput.attr("id", id + "-file");
    fileInput.attr("name", id + "-file");
    fileInput.change(function () {
        var tokens = $(this).val().split(/[\\/]/);
        var filename = tokens[tokens.length -1];
        $("#" + escapeIdentifier(id) + "-filename").val(filename);
        $("#" + escapeIdentifier(id) + "-filelink").each(function(idx, elm) {
            var jElm = $(elm);
            jElm.empty();
            jElm.append(document.createTextNode(filename));
            jElm.attr("href", "/download?entityUri=" + initial.id + "&filename=" + filename);
        });
    });

    var dummyFileInput = $(document.createElement("input"));
    dummyFileInput.addClass("input");
    dummyFileInput.addClass("disabled");
    dummyFileInput.addClass("edit-mode");
    dummyFileInput.addClass("field-value");
    dummyFileInput.attr("type", "text");
    dummyFileInput.attr("name", id + "-filename");
    dummyFileInput.attr("id", id + "-filename");
    dummyFileInput.attr("readonly", true);

    var fileUploadButton = $(document.createElement("a"));
    fileUploadButton.attr("id", id + "-upload");
    fileUploadButton.addClass("btn");
    fileUploadButton.addClass("edit-mode");
    fileUploadButton.append(document.createTextNode("Select File"));
    fileUploadButton.on("click", function() {
        $("#" + escapeIdentifier(id) + "-file").trigger("click");
    });

    if (value.length) {
        dummyFileInput.attr("value", value);
        a.append(document.createTextNode(value));
    }

    parent.append(fileInput);
    parent.append(dummyFileInput);
    parent.append($(document.createTextNode(" ")));
    parent.append(fileUploadButton);
    parent.append(a);
};

var appendSelectInput = function(id, value, allowedValues, parent) {
    //Select
    var p = $(document.createElement("p"));
    p.addClass("view-mode");
    p.hide();

    var select = $(document.createElement("select"));
    select.addClass("large");
    select.addClass("field-value");
    select.addClass("edit-mode");
    select.attr("id", id + "-select");
    select.attr("name", id + "-select");

    var defaultOption = $(document.createElement("option"));
    defaultOption.attr({value:"none"});
    defaultOption.append(document.createTextNode("[Select a Value]"));

    select.append(defaultOption);
    var optValue, label;
    for (optValue in allowedValues) {
        if (!Object.prototype.hasOwnProperty.call(allowedValues, optValue)) continue;
        label = allowedValues[optValue];
        var option = $(document.createElement("option"));
        if (value === optValue) {
            option.attr("selected", true);
            p.append(document.createTextNode(label));
        }
        option.attr("value", optValue);
        option.append(document.createTextNode(label));
        select.append(option);
    }
    parent.append(select);
    parent.append(p);
};

var createRawInput = function (index, id, functional, type, value, allowedValues) {
    id = id + "-" + index;

    var inputControl = $(document.createElement("p"));
    inputControl.attr("id", id + "-p");
    inputControl.attr("style", "display:none"); //TODO: does this do anything useful??

    if (isEmpty(allowedValues)) {
        if (type === "http://www.w3.org/2001/XMLSchema#base64Binary") {
            appendFileInput(id, value, inputControl);
        } else if (type === "something else") {
            //Other input types here
        } else {
            appendRawTextInput(id, value, inputControl);
        }
    } else {
        appendSelectInput(id, value, allowedValues, inputControl);
    }

    var span = $(document.createElement("span"));
    span.append(document.createTextNode(" "));
    inputControl.append(span);

    if (!functional) {
        // delete button
        var delButton = $(document.createElement("button"));
        delButton.addClass("btn");
        delButton.addClass("btn-small");
        delButton.addClass("edit-mode");
        delButton.attr("id", id + "-delete");
        var delI = $(document.createElement("i"));
        delI.addClass("icon-trash");
        delButton.append(delI);
        delButton.on("click", function() {deleteFunctional(id);});
        inputControl.append(delButton);
    }

    return inputControl;
};

var createInput = function (id, functional, type, values, allowedValues) {
    var inputControl = [];

    if (values.length == 0) {
        if (functional) {
            var p = createRawInput(0, id, functional, type, "", allowedValues);
            inputControl.push(p);
            p.show("fast");
        }
    } else {
        for (var i = 0; i < values.length; i++) {
            var p = createRawInput(i, id, functional, type, values[i], allowedValues);
            inputControl.push(p);
            p.show("fast");
        }
    }

    // Adding "Add New" Button
    if (functional == false) {
        var addButton = $(document.createElement("input"));
        addButton.attr("value", "Add New");
        addButton.addClass("btn");
        addButton.addClass("edit-mode");
        addButton.attr("type", "button");
        addButton.attr("id", id + "-add");
        addButton.attr("createinputrow", values.length);
        addButton.on("click", function() {addButtonClicked(id);});
        inputControl.push(addButton);
    }

    return inputControl;
};

var addButtonClicked = function (id) {
    var desc = null;
    for (var i = 0; i < initial.fields.length; i++) {
        var fieldDesc = initial.fields[i];
        if (fieldDesc.uri === id) {
            desc = fieldDesc;
            break;
        }
    }
    if (desc === null) {
        alert("trouble");
        return;
    }
    var addButton = $("#" + escapeIdentifier(id + "-add"));
    var inputIndex = parseInt(addButton.attr("createinputrow")) + 1;
    addButton.attr("createinputrow", inputIndex);
    var inputControls = createRawInput(inputIndex, desc.uri, desc.functional, desc.datatype, "", desc.allowedValues);
    addButton.before(inputControls);
    $(inputControls).show("fast");
};

var createField = function (index, label, id, functional, type, values, allowedValues, help) {
    var fieldGroup = $(document.createElement("div"));
    fieldGroup.addClass("control-group");
    fieldGroup.addClass("field-container");
    fieldGroup.attr("id", "div-" + id);

    var labelElement = $(document.createElement("label"));
    labelElement.addClass("control-label");
    labelElement.css("font-weight","bold");
    labelElement.attr("for", id);
    labelElement.append(document.createTextNode(label));
    fieldGroup.append(labelElement);

    var controlGroup = $(document.createElement("div"));
    controlGroup.addClass("controls");
    fieldGroup.append(controlGroup);

    var controlGroupParagraph = $(document.createElement("p"));
    controlGroup.append(controlGroupParagraph);

    var inputs = createInput(id, functional, type, values, allowedValues);

    for (var i = 0; i < inputs.length; i++) {
        controlGroup.append(inputs[i]);
    }

    if (help) {
        var helpBlock = $(document.createElement("p"));
        helpBlock.addClass("help-inline");
        helpBlock.append(document.createTextNode(help));
        controlGroup.append(helpBlock);
    }

    return fieldGroup;
};

$(window).load(function () {
    loadInitial(resetForm);
});