$(document).ready(function () {
    document.title = Android.getString('Premiums');

    var policyId = parseInt(queryString("p"));
    var LocationId = parseInt(queryString("l"));
    var PremiumId = 0;
    var RegionId = parseInt(queryString("r"));
    var DistrictId = parseInt(queryString("d"));
    var FamilyId = parseInt(queryString("f"));

    var url = 'FamilyPolicies.html?f=' + FamilyId + '&l=' + LocationId + '&r=' + RegionId + '&d=' + DistrictId;
    Android.SetUrl(url);

    LoadPremiums(policyId);

    $(".plusButton").click(function () {
        window.open('Premium.html?p=' + policyId + '&f=' + FamilyId + '&l=' + LocationId + '&pr=' + PremiumId + '&r=' + RegionId + '&d=' + DistrictId, '_self');
    });


    $('.ulList li').click(function () {
        PremiumId = parseInt($(this).find('#hfPremiumId').val());
        //window.open('Premium.html?p=' + policyId + '&l=' + LocationId + '&pr=' + PremiumId + '&r=' + RegionId + '&d=' + DistrictId, '_self');
        });

    AssignDotClass();
    contextMenu.createContextMenu([Android.getString('Edit'), Android.getString('Delete')], function () {
        var clicked = $(this).text();

        if (clicked == Android.getString('Edit')) {
            var url = 'PolicyPremium.html?p=' + policyId + '&f=' + FamilyId + '&l=' + LocationId + '&r=' + RegionId + '&d=' + DistrictId;
            Android.SetUrl(url);
            window.open('Premium.html?p=' + policyId + '&f=' + FamilyId + '&l=' + LocationId + '&pr=' + PremiumId + '&r=' + RegionId + '&d=' + DistrictId, '_self');
        }
        else if (clicked == Android.getString('Delete')) {
            var isOffline = $('#hfIsOffline').val();
            var deletedSuccess = -1;
            if (isOffline == 0 || isOffline == 2) {
                deletedSuccess = parseInt(Android.DeleteOnlineData(PremiumId, 'PR'));
            }
            else {
                deletedSuccess = parseInt(Android.DeletePremium(PremiumId, policyId));
            }

            if (deletedSuccess == 1) {
                Android.ShowDialog(Android.getString('PremiumDeleted'));
                window.open("PolicyPremium.html?p=" + policyId + "&f=" + FamilyId + "&l=" + LocationId + '&r=' + RegionId + '&d=' + DistrictId, "_self");
            }
            else if (deletedSuccess == -1) {
                Android.ShowDialog(Android.getString('LoginToDeleteOnlineData'));
            }
        }
    });
});

function LoadPremiums(PolicyId) {
    var Premiums = Android.getPremiums(PolicyId);
    var ctls = ["hfPremiumId", "Amount", "PayDate", "PayType", "IsOffline", "IsPhotoFee", "Receipt", "PremiumId", "hfIsOffline"];
    var Columns = ["PremiumId", "Amount", "PayDate", "PayType", "isOffline", "IsPhotoFee", "Receipt", "PremiumId", "isOffline"];
    LoadList(Premiums, '.ulList', ctls, Columns);
}

function AssignDotClass() {
    var $lis = $(".ulList li");
    $lis.addClass("dot-side-menu");
}
