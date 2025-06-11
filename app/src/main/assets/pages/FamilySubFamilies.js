$(document).ready(function () {
    document.title = Android.getString('FamilyAndSubFamilies');

    if (!Android.IsBulkCNUsed()) {
        $('#ControlNumberSection').hide();
    }

    var FamilyId = queryString("f");
    var LocationId = parseInt(queryString("l"));
    var RegionId = parseInt(queryString("r"));
    var DistrictId = parseInt(queryString("d"));

    var url = 'FamilyPolygamy.html?f=' + FamilyId;
    Android.SetUrl(url);

    var Action = 'none';
    var SubFamilyId = null;

    LoadFamilySubFamilies(parseInt(FamilyId));

    $(".plusButton").click(function () {
        var url = 'FamilySubFamilies.html?f=' + FamilyId + '&l=' + LocationId + '&r=' + RegionId + '&d=' + DistrictId;
        Android.SetUrl(url);
        window.open('SubFamily.html?f=' + FamilyId + '&sf=0', '_self');
    });

    $('.ulList li').click(function () {
        SubFamilyId = parseInt($(this).find('#hfFamilyId').val());
        //window.open("PolicyPremium.html?p=" + PolicyId + "&f=" + FamilyId + "&l=" + LocationId + '&r=' + RegionId + '&d=' + DistrictId, "_self");
    });

    AssignDotClass();

    contextMenu.createContextMenu([Android.getString('Edit'), Android.getString('Detach'), Android.getString('Attachment'), Android.getString('Delete')], function () {
        var clicked = $(this).text();
        if (clicked == Android.getString('Edit')) {
            var url = 'FamilySubFamilies.html?f=' + FamilyId + '&l=' + LocationId + '&r=' + RegionId + '&d=' + DistrictId;
            Android.SetUrl(url);
            window.open('FamilyAndInsurees.html?f=' + SubFamilyId + '&p=' + FamilyId + '&isSubfamily=1', '_self');
        }
        if (clicked == Android.getString('Detach')) {
            var detachSuccess = 0;
            $('#msgAlert').text(Android.getString('DetachFamily'));
            $("#dialog-confirm").dialog({
                resizable: false,
                height: "auto",
                width: 350,
                modal: true,
                buttons: [
                    {
                        text: Android.getString("Ok"),
                        click: function () {
                            detachSuccess = parseInt(Android.DetachFamily(SubFamilyId));
                            if (detachSuccess == 1) {
                                Android.ShowDialog(Android.getString('SubFamilyDetached'));
                                window.open('FamilySubFamilies.html?f=' + FamilyId, '_self');
                            } else if (detachSuccess == -1) {
                                Android.ShowDialog(Android.getString('ErrorDetach'));
                            }
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
        else if(clicked == Android.getString('Attachment')){
            var url = 'FamilySubFamilies.html?f=' + FamilyId + '&l=' + LocationId + '&r=' + RegionId + '&d=' + DistrictId;
            Android.SetUrl(url);
            window.open("Attachment.html?f=" + FamilyId + '&s=' + SubFamilyId + '&isSubfamily=1', "_self");
        }
        else if (clicked == Android.getString('Delete')) {
            var isOffline = Android.getFamilyStat(SubFamilyId);
            if (isOffline == 0 || isOffline == 2) {
                $('#msgAlert').text(Android.getString('DeleteFamilyOnlyOffline'));
            } else {
                $('#msgAlert').text(Android.getString('DeleteFamily'));
            }
            var isOffline = Android.getFamilyStat(SubFamilyId);
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
                                var resul = Android.DeleteOnlineDataF(SubFamilyId);
                                if (resul == 1) {
                                    window.open('FamilySubFamilies.html?f=' + FamilyId, '_self');
                                    Android.ShowDialog(Android.getString('SubFamilyDeleted'));
                                    //Android.informUser();
                                }
                            } else {
                                deletedSuccess = parseInt(Android.DeleteFamily(SubFamilyId));
                                LoadFamilySubFamilies();
                            }
                            if (deletedSuccess == 1) {

                                Android.ShowDialog(Android.getString('SubFamilyDeleted'));
                                window.open('FamilySubFamilies.html?f=' + FamilyId, '_self');
                            } else if (deletedSuccess == -1) {
                                Android.ShowDialog(Android.getString('LoginToDeleteOnlineData'));
                            } else if (deletedSuccess == 3) {
                                var resul = Android.DeleteOnlineDataF(SubFamilyId);
                                if (resul == 1) {
                                    window.open('FamilySubFamilies.html?f=' + FamilyId, '_self');
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
        };
    })
});

function LoadFamilySubFamilies(FamilyId) {
    var SubFamilies = Android.getAllSubFamilies(FamilyId);
    var ctls = ["hfFamilyId", "InsuranceNumber", "InsureeName", "Region", "District", "Ward", "Village", "FamilyId", "spFamilyId", "hfIsOffline", "hfFamilyType"];
    var Columns = ["FamilyId", "CHFID", "InsureeName", "RegionName", "DistrictName", "WardName", "VillageName", "FamilyId", "FamilyId", "isOffline", "FamilyType"];
    LoadList(SubFamilies, '.ulList', ctls, Columns);
}

function AssignDotClass() {
    var $lis = $(".ulList li");
    $lis.addClass("dot-side-menu");
}
