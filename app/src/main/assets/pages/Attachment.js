$(document).ready(function () {

    var InsureeId = queryString("i");
    var FamilyId = queryString("f");
    var AttachmentTitle = "";
    var AttachmentName = "";
    var AttachmentId = 0;

    if (sessionStorage.getItem("FamilyData") !== null) {
             var Attachments = Android.getInsureeAttachments(parseInt(queryString('f')));
             var ctls = ["AttachmentTitle", "AttachmentFile", "AttachmentId"];
             var Columns = ["Title", "Filename", "Id"];
             LoadList(Attachments, '.ulList', ctls, Columns);
    }else{
        LoadAttachments();
    }


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

            Android.addAttachment(parseInt(FamilyId),title, file);
            window.location.reload();

        } else
            Android.ShowDialog(Android.getString('FieldRequired'));

        $('#txtTitleAttachment').val("");
        $('#txtFileAttachment').val("");
    });

    $('#btnSave').click(function () {
        $("#divProgress").show();

        if (Android.getInsureeAttachments(parseInt(queryString('f'))) === "[]") {
            $("#divProgress").hide();
            Android.ShowDialog(Android.getString('AttachmentRequired'));
        } else {
            var jsonInsuree = sessionStorage.getItem("InsureeData");
            if (sessionStorage.getItem("FamilyData") !== null) {
                var FamilyId = Android.SaveFamily(sessionStorage.getItem("FamilyData"), jsonInsuree);
                Android.SaveInsureeAttachments(FamilyId);

                if (FamilyId > 0) {
                    sessionStorage.removeItem("FamilyData");
                    sessionStorage.removeItem("InsureeData");
                    $(this).attr("disabled", "disabled");

                    window.open("FamilyAndInsurees.html?f=" + FamilyId, "_self");

                }

            } else {
                var FamilyId = parseInt(queryString('f'));
                var FamilyPolicy = Android.getFamilyPolicy(FamilyId);
                var $Policy = $.parseJSON(FamilyPolicy);
                var MemberCount = parseInt($Policy[0]["MemberCount"]);
                var Threshold = parseInt($Policy[0]["Threshold"]);
                var TotalIns = parseInt($Policy[0]["Ins"]);
                var PolicyId = parseInt($Policy[0]["PolicyId"]);
                var IsNewIns = parseInt($("#hfInsureeId").val());
                var MemberDialog = -1;
                var ExceedThreshold = -1;

                if (PolicyId > 0 && IsNewIns == 0) {
                    if (TotalIns >= MemberCount) {
                        ExceedThreshold = 0;
                        Android.ShowDialog(Android.getString('ExceedMemberCount'));
                    } else if (TotalIns >= Threshold) {
                        ExceedThreshold = 1;
                    } else {
                        ExceedThreshold = 0;
                    }

                }
                var InsureeId = Android.SaveInsuree(jsonInsuree, FamilyId, 0, parseInt(ExceedThreshold), PolicyId);
                Android.SaveInsureeAttachments(FamilyId);
                if (PolicyId > 0 && TotalIns >= MemberCount) {
                    $("#divProgress").hide();
                } else {
                    $("#divProgress").hide();
                    window.open("FamilyAndInsurees.html?f=" + FamilyId, "_self");
                }
            }
        }
    });

    $('#txtFileAttachment').click(function () {
        Android.showAttachmentDialog();
    });

    AssignDotClass();
    contextMenu.createContextMenu([Android.getString(Android.getString('Delete'))], function () {
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
                            AttachmentDeleted = parseInt(Android.DeleteAttachment(parseInt(FamilyId),AttachmentId,AttachmentTitle, AttachmentName));
                            LoadAttachments();
                            if (AttachmentDeleted == 1) {
                                Android.ShowDialog(Android.getString('AttachmentDeleted'));
                                window.open('Attachment.html?f=' + FamilyId, "_self");
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
    var Attachments = Android.getInsureeAttachments(parseInt(queryString('f')));
    var ctls = ["AttachmentTitle", "AttachmentFile", "AttachmentId"];
    var Columns = ["Title", "Filename", "Id"];
    LoadList(Attachments, '.ulList', ctls, Columns);
}

function AssignDotClass() {
    var $lis = $(".ulList li");
    $lis.addClass("dot-side-menu");
}