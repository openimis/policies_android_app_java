$(document).ready(function () {
    Android.SetUrl('Home.html');
    document.title = Android.getString('Families');
    LoadFamilies();

    var FamilyUuid = '';
    $('#btnAddNew').click(function () {
        var url = 'Enrollment.html?f=' + FamilyUuid;
        Android.SetUrl(url);
        window.open("Family.html?f=", "_self");
    });


    $('.ulList li').click(function () {
        FamilyUuid = $(this).find('#hfFamilyId').val();
        //<!--window.open('FamilyAndInsurees.html?f=' + FamilyId, '_self');-->

    });

    AssignDotClass();
    contextMenu.createContextMenu([Android.getString('Edit'), Android.getString('Delete')], function () {
        var clicked = $(this).text();
        if (clicked == Android.getString('Edit')) {
            var url = 'Enrollment.html?f=' + FamilyUuid;
            Android.SetUrl(url);
            window.open('FamilyAndInsurees.html?f=' + FamilyUuid, '_self');
        }
        else if (clicked == Android.getString('Delete')) {
            var isOffline = Android.getFamilyStat(FamilyUuid);
            if (isOffline == 0 || isOffline == 2) {
                $('#msgAlert').text(Android.getString('DeleteFamilyOnlyOffline'));
            } else {
                $('#msgAlert').text(Android.getString('DeleteFamily'));
            }
            var isOffline = Android.getFamilyStat(FamilyUuid);
            var deletedSuccess = 0;
            $("#dialog-confirm").dialog({
                resizable: false,
                height: "auto",
                width: 350,
                modal: true,
                buttons: [
                    {
                        text: Android.getString("Ok"),
                        click: function () {

                        if (isOffline == 0 || isOffline == 2) {
                            //deletedSuccess = parseInt(Android.DeleteOnlineData(FamilyId, 'F'));
                            var resul = Android.DeleteOnlineDataF(FamilyUuid);
                            if (resul == 1) {
                                window.open('Enrollment.html', '_self');
                                Android.ShowDialog(Android.getString('FamilyDeleted'));
                                //Android.informUser();
                            }
                        } else {
                            deletedSuccess = parseInt(Android.DeleteFamily(FamilyUuid));
                            LoadFamilies();
                        }
                        if (deletedSuccess == 1) {

                            Android.ShowDialog(Android.getString('FamilyDeleted'));
                            window.open('Enrollment.html', '_self');
                        } else if (deletedSuccess == -1) {
                            Android.ShowDialog(Android.getString('LoginToDeleteOnlineData'));
                        } else if (deletedSuccess == 3) {
                            var resul = Android.DeleteOnlineDataF(FamilyUuid);
                            if (resul == 1) {
                                window.open('Enrollment.html', '_self');
                                Android.informUser();
                            }
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

function LoadFamilies() {
    var Families = Android.getAllFamilies();
    var ctls = ["hfFamilyId", "InsuranceNumber", "InsureeName", "Region", "District", "Ward", "Village", "spFamilyId", "hfIsOffline"];
    var Columns = ["FamilyUUID", "CHFID", "InsureeName", "RegionName", "DistrictName", "WardName", "VillageName", "FamilyUUID", "isOffline"];
    LoadList(Families, '.ulList', ctls, Columns);
}

function AssignDotClass() {
    var $lis = $(".ulList li");
    $lis.addClass("dot-side-menu");
}
