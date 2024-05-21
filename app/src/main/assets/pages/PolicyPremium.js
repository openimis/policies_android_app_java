$(document).ready(function () {
    document.title = Android.getString('Premiums');

    var policyUUID = queryString("p");
    var LocationId = parseInt(queryString("l"));
    var PremiumUUID = '';
    var RegionId = parseInt(queryString("r"));
    var DistrictId = parseInt(queryString("d"));
    var FamilyUUID = queryString("f");

    var url = 'FamilyPolicies.html?f=' + FamilyUUID + '&l=' + LocationId + '&r=' + RegionId + '&d=' + DistrictId;
    Android.SetUrl(url);

    LoadPremiums(policyUUID);

    $(".plusButton").click(function () {
        window.open('Premium.html?p=' + policyUUID + '&f=' + FamilyUUID + '&l=' + LocationId + '&pr=' + PremiumUUID + '&r=' + RegionId + '&d=' + DistrictId, '_self');
    });


    $('.ulList li').click(function () {
        PremiumUUID = $(this).find('#hfPremiumId').val();
        });

    AssignDotClass();
    contextMenu.createContextMenu([Android.getString('Edit'), Android.getString('Delete')], function () {
        var clicked = $(this).text();

        if (clicked == Android.getString('Edit')) {
            var url = 'PolicyPremium.html?p=' + policyUUID + '&f=' + FamilyUUID + '&l=' + LocationId + '&r=' + RegionId + '&d=' + DistrictId;
            Android.SetUrl(url);
            window.open('Premium.html?p=' + policyUUID + '&f=' + FamilyUUID + '&l=' + LocationId + '&pr=' + PremiumUUID + '&r=' + RegionId + '&d=' + DistrictId, '_self');
        }
        else if (clicked == Android.getString('Delete')) {
            var isOffline = $('#hfIsOffline').val();
            var deletedSuccess = -1;
            if (isOffline == 0 || isOffline == 2) {
                deletedSuccess = parseInt(Android.DeleteOnlineData(PremiumUUID, 'PR'));
            }
            else {
                deletedSuccess = parseInt(Android.DeletePremium(PremiumUUID, policyUUID));
            }

            if (deletedSuccess == 1) {
                Android.ShowDialog(Android.getString('PremiumDeleted'));
                window.open("PolicyPremium.html?p=" + policyUUID + "&f=" + FamilyUUID + "&l=" + LocationId + '&r=' + RegionId + '&d=' + DistrictId, "_self");
            }
            else if (deletedSuccess == -1) {
                Android.ShowDialog(Android.getString('LoginToDeleteOnlineData'));
            }
        }
    });
});

function LoadPremiums(PolicyUUID) {
    var Premiums = Android.getPremiums(PolicyUUID);
    var ctls = ["hfPremiumId", "Amount", "PayDate", "PayType", "IsOffline", "IsPhotoFee", "Receipt", "PremiumUIUD", "hfIsOffline"];
    var Columns = ["PremiumUUID", "Amount", "PayDate", "PayType", "isOffline", "IsPhotoFee", "Receipt", "PremiumUUID", "isOffline"];
    LoadList(Premiums, '.ulList', ctls, Columns);
}

function AssignDotClass() {
    var $lis = $(".ulList li");
    $lis.addClass("dot-side-menu");
}
