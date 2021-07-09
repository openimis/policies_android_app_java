$(document).ready(function () {

    document.title = Android.getString('FamilyAndInsurees');

    var FamilyId = queryString("f");
    var LocationId = null;
    var RegionId = null;
    var DistrictId = null;
    var InsureeId = null;
    var Action = null;

    var url = 'Enrollment.html?f=' + FamilyId;
    Android.SetUrl(url);

    if (FamilyId != 0) {
        LoadFamilyHeader(parseInt(FamilyId))
        LoadInsurees(parseInt(FamilyId));
    }

    $(".family-location").click(function () {
        window.open('Family.html?f=' + FamilyId);
    });

    $('#btnNewInsuree').click(function () {
        var url = 'FamilyAndInsurees.html?f=' + FamilyId;
        Android.SetUrl(url);
        window.open('Insuree.html?f=' + FamilyId, '_self');
    });


    $('.ulList li').click(function () {
        InsureeId = parseInt($(this).find('#hfInsureeId').val());
        //window.open("Insuree.html?i=" + InsureeId + "&f=" + FamilyId, "_self");

    });

    $('.Policy-label').click(function () {
        LocationId = $('#hfLocationId').val();
        RegionId = $('#hfRegionId').val();
        DistrictId = $('#hfDistrictId').val();

        window.open('FamilyPolicies.html?f=' + FamilyId + '&l=' + LocationId + '&r=' + RegionId + '&d=' + DistrictId, '_self');
    });


    AssignDotClass();
    contextMenu.createContextMenu([Android.getString('Edit'), Android.getString('Delete')], function () {
        var clicked = $(this).text();
        if (clicked == Android.getString('Edit')) {
            var url = 'FamilyAndInsurees.html?f=' + FamilyId;
            Android.SetUrl(url);
            window.open("Insuree.html?i=" + InsureeId + "&f=" + FamilyId, "_self");
        }
        else if (clicked == Android.getString('Delete')) {
            //$("#divProgress").show();
            var isOffline = $('#hfIsOffline').val();
            var deletedSuccess = null;
            $('#spPleaseWait').text(Android.getString('Deleting'));
            if (isOffline == 0 || isOffline == 2) {

                $("#divProgress").show();

                deletedSuccess = parseInt(Android.DeleteOnlineData(InsureeId, 'I'));


            }
            else {

                deletedSuccess = Android.DeleteInsuree(InsureeId);
            }

            if (deletedSuccess == 1) {

                $("#divProgress").hide();
                //Android.ShowDialog(Android.getString('InsureeDeleted'));
                window.open('FamilyAndInsurees.html?f=' + FamilyId + '&l=' + LocationId + '&r=' + RegionId + '&d=' + DistrictId, '_self');
            }
            else if (deletedSuccess == 2) {
                $("#divProgress").hide();
                Android.ShowDialog(Android.getString('IsHeadDelete'));
            } else if (deletedSuccess == -1) {
                $("#divProgress").hide();
                Android.ShowDialog(Android.getString('LoginToDeleteOnlineData'));
            } else {
                $("#divProgress").hide();

                Android.ShowDialog(Android.getString('InsureeNotDeleted'));
            }

        }
    });

});

function LoadInsurees(FamilyId) {
    var Insurees = Android.getInsureesForFamily(FamilyId);
    var ctls = ["CHFID", "InsureeName", "hfInsureeId", "spDOB", "spGender", "hfIsHead", "InsureeId", "hfIsOffline"];
    var Columns = ["CHFID", "InsureeName", "InsureeId", "DOB", "Gender", "isHead", "InsureeId", "isOffline"];
    LoadList(Insurees, '.ulList', ctls, Columns);

    HighlightHOF();

}
function LoadFamilyHeader(FamilyId) {
    var FamilyHeader = Android.getFamilyHeader(FamilyId);
    bindDataFromDatafield(FamilyHeader);
}
function AssignDotClass() {
    var $lis = $(".ulList li");
    $lis.addClass("dot-side-menu");
}


function HighlightHOF() {
    var $li = $(".ulList li");

    $.each($li, function (index, obj) {
        var $childObj = ($(obj).children());
        $.each($childObj, function (index, cObj) {
            if ($(cObj).attr("id") == "hfIsHead" && $(cObj).val() == "1") {
                $(obj).css({ "border": "1px solid blue" });
            }
        });
    })
}
