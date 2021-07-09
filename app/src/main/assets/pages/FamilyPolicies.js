$(document).ready(function () {
    document.title = Android.getString('FamilyAndPolicies');

    var FamilyId = queryString("f");
    var LocationId = parseInt(queryString("l"));
    var RegionId = parseInt(queryString("r"));
    var DistrictId = parseInt(queryString("d"));

    var url = 'FamilyAndInsurees.html?f=' + FamilyId;
    Android.SetUrl(url);

    var Action = 'none';
    var PolicyId = null;

    LoadFamilyPolicies(parseInt(FamilyId));

    $(".plusButton").click(function () {
        var url = 'FamilyPolicies.html?f=' + FamilyId + '&l=' + LocationId + '&r=' + RegionId + '&d=' + DistrictId;
        Android.SetUrl(url);
        window.open('Policy.html?f=' + FamilyId + '&l=' + LocationId + '&p=' + 0 + '&r=' + RegionId + '&d=' + DistrictId, '_self');

    });


    $('.ulList li').click(function () {
        PolicyId = parseInt($(this).find('#hfPolicyId').val());
        //window.open("PolicyPremium.html?p=" + PolicyId + "&f=" + FamilyId + "&l=" + LocationId + '&r=' + RegionId + '&d=' + DistrictId, "_self");
    });



    AssignDotClass();
    contextMenu.createContextMenu([Android.getString('Edit'), Android.getString('Delete'), Android.getString('Payment')], function () {
        var clicked = $(this).text();
        if (clicked == Android.getString('Edit')) {
            var url = 'FamilyPolicies.html?f=' + FamilyId + '&l=' + LocationId + '&r=' + RegionId + '&d=' + DistrictId;
            Android.SetUrl(url);
            window.open("Policy.html?p=" + PolicyId + "&f=" + FamilyId + "&l=" + LocationId + '&r=' + RegionId + '&d=' + DistrictId, "_self");
        }
        else if (clicked == Android.getString('Payment')) {
            var url = 'FamilyPolicies.html?f=' + FamilyId + '&l=' + LocationId + '&r=' + RegionId + '&d=' + DistrictId;
            Android.SetUrl(url);

            window.open("PolicyPremium.html?p=" + PolicyId + "&f=" + FamilyId + "&l=" + LocationId + '&r=' + RegionId + '&d=' + DistrictId, "_self");

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
                buttons: {
                    OK: function () {

                        if (isOffline == 0 || isOffline == 2) {

                            PolicyDeleted = parseInt(Android.DeleteOnlineData(PolicyId, 'PO'));
                        }
                        else {
                            PolicyDeleted = Android.DeletePolicy(PolicyId);
                        }
                        if (PolicyDeleted == 1) {
                            Android.ShowDialog(Android.getString('PolicyDeleted'));
                            window.open('FamilyPolicies.html?f=' + FamilyId + "&l=" + LocationId + '&r=' + RegionId + '&d=' + DistrictId, "_self");
                        }
                        else if (PolicyDeleted == -1) {
                            Android.ShowDialog(Android.getString('LoginToDeleteOnlineData'));
                        }
                        $(this).dialog("close");
                    },
                    Cancel: function () {
                        $(this).dialog("close");
                    }
                }
            });
        }
    });


});

function LoadFamilyPolicies(FamilyId) {
    var Policies = Android.getFamilyPolicies(FamilyId);
    var ctls = ["ProductCode", "ProductName", "StartDate", "ExpireDate", "PolicyValue", "PolicyStatus", "EffectiveDate", "hfPolicyId", "PolicyId", "hfIsOffline"];
    var Columns = ["ProductCode", "ProductName", "StartDate", "ExpiryDate", "PolicyValue", "PolicyStatus", "EffectiveDate", "PolicyId", "PolicyId", "isOffline"];
    LoadList(Policies, '.ulList', ctls, Columns);
}

function AssignDotClass() {
    var $lis = $(".ulList li");
    $lis.addClass("dot-side-menu");
}
