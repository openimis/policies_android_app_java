$(document).ready(function () {
    document.title = Android.getString('FamilyAndPolicies');

    if (!Android.IsBulkCNUsed()) {
        $('#ControlNumberSection').hide();
    }

    var FamilyUUID = queryString("f");
    var LocationId = parseInt(queryString("l"));
    var RegionId = parseInt(queryString("r"));
    var DistrictId = parseInt(queryString("d"));

    var url = 'FamilyAndInsurees.html?f=' + FamilyUUID;
    Android.SetUrl(url);

    var Action = 'none';
    var PolicyUUID = null;

    LoadFamilyPolicies(FamilyUUID);

    $(".plusButton").click(function () {
        var url = 'FamilyPolicies.html?f=' + FamilyUUID + '&l=' + LocationId + '&r=' + RegionId + '&d=' + DistrictId;
        Android.SetUrl(url);
        window.open('Policy.html?f=' + FamilyUUID + '&l=' + LocationId + '&p=' + '&r=' + RegionId + '&d=' + DistrictId, '_self');

    });

    $('.ulList li').click(function () {
        PolicyUUID = $(this).find('#hfPolicyId').val();

    });

    AssignDotClass();

    contextMenuHandler = function () {
        var clicked = $(this).text();
        if (clicked == Android.getString('Edit')) {
            var url = 'FamilyPolicies.html?f=' + FamilyUUID + '&l=' + LocationId + '&r=' + RegionId + '&d=' + DistrictId;
            Android.SetUrl(url);
            window.open("Policy.html?p=" + PolicyUUID + "&f=" + FamilyUUID + "&l=" + LocationId + '&r=' + RegionId + '&d=' + DistrictId, "_self");
        }
        else if (clicked == Android.getString('Payment')) {
            var url = 'FamilyPolicies.html?f=' + FamilyUUID + '&l=' + LocationId + '&r=' + RegionId + '&d=' + DistrictId;
            Android.SetUrl(url);

            window.open("PolicyPremium.html?p=" + PolicyUUID + "&f=" + FamilyUUID + "&l=" + LocationId + '&r=' + RegionId + '&d=' + DistrictId, "_self");

        }
        else if (clicked == Android.getString('Delete')) {
            var isOffline = $('#hfIsOffline').val();
            var PolicyDeleted = -1;
            $('#msgAlert').text(Android.getString('DeletePolicyPremium'));
            var isOffline = $('#hfIsOffline').val();
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

                            if (isOffline == 0 || isOffline == 2) {

                                PolicyDeleted = parseInt(Android.DeleteOnlineData(PolicyUUID, 'PO'));
                            }
                            else {
                                PolicyDeleted = Android.DeletePolicy(PolicyUUID);
                            }
                            if (PolicyDeleted == 1) {
                                Android.ShowDialog(Android.getString('PolicyDeleted'));
                                window.open('FamilyPolicies.html?f=' + FamilyUUID + "&l=" + LocationId + '&r=' + RegionId + '&d=' + DistrictId, "_self");
                            }
                            else if (PolicyDeleted == -1) {
                                Android.ShowDialog(Android.getString('LoginToDeleteOnlineData'));
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
    }

    contextMenuList = [Android.getString('Edit'), Android.getString('Delete')];
    if(Android.getRule('ShowPaymentOption', true)) {
        contextMenuList.push(Android.getString('Payment'));
    }

    contextMenu.createContextMenu(contextMenuList, contextMenuHandler);
});

function LoadFamilyPolicies(FamilyUUID) {
    var Policies = Android.getFamilyPolicies(FamilyUUID);
    var ctls = ["ProductCode", "ProductName", "StartDate", "ExpireDate", "PolicyValue", "PolicyStatus", "EffectiveDate", "hfPolicyId", "PolicyUUID", "hfIsOffline", "ControlNumber"];
    var Columns = ["ProductCode", "ProductName", "StartDate", "ExpiryDate", "PolicyValue", "PolicyStatus", "EffectiveDate", "PolicyUUID", "PolicyUUID", "isOffline", "ControlNumber"];
    LoadList(Policies, '.ulList', ctls, Columns);
}

function AssignDotClass() {
    var $lis = $(".ulList li");
    $lis.addClass("dot-side-menu");
}
