$(document).ready(function () {

    document.title = Android.getString('FamilyAndAttachment');

    var FamilyId = queryString("f");
    var subFamilyId = queryString("s");
    var isSubfamily = queryString("isSubfamily")
    var AttachmentTitle = "";
    var AttachmentName = "";
    var AttachmentId = 0;

    LoadAttachments();


    $('.ulList li').click(function () {
          AttachmentTitle = $(this).find('#AttachmentTitle').text();
          AttachmentName = $(this).find('#AttachmentFile').text();
          AttachmentId = parseInt($(this).find('#AttachmentId').val());
    });


    $('#btnAddNew').click(function () {
        var passed = isFormValidated();

        if (passed == true) {
            var title = $('#txtTitleAttachment').val();
            var file = $('#txtFileAttachment').val();

            if(isSubfamily == 1){
                Android.addAttachment(parseInt(subFamilyId),title, file);
                window.location.reload();
            } else {
                Android.addAttachment(parseInt(FamilyId),title, file);
                window.location.reload();
            }
        } else
            Android.ShowDialog(Android.getString('FieldRequired'));

        $('#txtTitleAttachment').val("");
        $('#txtFileAttachment').val("");
    });

    $('#btnSave').click(function () {
        $("#divProgress").show();

        if(isSubfamily == 1){
            if ( Android.getFamilyAttachments(parseInt(queryString('s'))) === "[]"){
                $("#divProgress").hide();
                Android.ShowDialog(Android.getString('AttachmentNotFound'));
            } else {
                Android.SaveFamilyAttachments(subFamilyId);
                window.open("FamilySubFamilies.html?f=" + FamilyId, "_self");
            }
        } else {
            if ( Android.getFamilyAttachments(parseInt(queryString('f'))) === "[]"){
                $("#divProgress").hide();
                Android.ShowDialog(Android.getString('AttachmentNotFound'));
            } else {
                Android.SaveFamilyAttachments(FamilyId);
                window.open("Enrollment.html", "_self");
            }
        }
    });

    $('#txtFileAttachment').click(function () {
        Android.showAttachmentDialog();
    });

    AssignDotClass();
    contextMenu.createContextMenu([Android.getString('Delete')], function () {
        var clicked = $(this).text();

        if (clicked == Android.getString('Delete')) {
            var AttachmentDeleted = -1;
            $('#msgAlert').text(Android.getString('DeleteAttachment'));
            var deletedSuccess = -1;
            $("#dialog-confirm").dialog({
                resizable: false,
                height: "auto",
                width: 350,
                modal: true,
                buttons: [
                    {
                        text: Android.getString("Ok"),
                        click: function () {
                            AttachmentDeleted = parseInt(Android.DeleteAttachment(parseInt(subFamilyId),AttachmentId,AttachmentTitle, AttachmentName));

                            if (AttachmentDeleted == 1) {
                                Android.ShowDialog(Android.getString('AttachmentDeleted'));
                                location.reload()
                            }
                            $(this).dialog("close");
                        }
                    },
                    {
                        text: Android.getString("Cancel"),
                        click: function () {
                            $(this).dialog("close");
                        }
                    }
                ]
            });
        }
    });


});

// called from java after attachment was selected by the user
function selectAttachmentCallback(attachName) {
    $('#txtFileAttachment').val(attachName);
}

function LoadAttachments() {
    var Attachments;
    if(queryString('isSubfamily') == 1){
        Attachments = Android.getFamilyAttachments(parseInt(queryString('s')));
    } else {
        Attachments = Android.getFamilyAttachments(parseInt(queryString('f')));
    }
    var ctls = ["AttachmentTitle", "AttachmentFile", "AttachmentId"];
    var Columns = ["Title", "Filename", "Id"];
    LoadList(Attachments, '.ulList', ctls, Columns);
}

function AssignDotClass() {
    var $lis = $(".ulList li");
    $lis.addClass("dot-side-menu");
}