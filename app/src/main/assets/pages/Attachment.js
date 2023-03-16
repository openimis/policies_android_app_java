$(document).ready(function () {

    $('#btnAddNew').click(function () {
            var url = 'Enrollment.html?f=' + FamilyId;
            Android.SetUrl(url);
            window.open("Family.html?f=0", "_self");
    });

    $('#btnSave').click(function () {
            $("#divProgress").show();

            getImage();

            var passed = isFormValidated();

            if (passed == true) {

                Android.clearInsuranceNo();
                var jsonInsuree = sessionStorage.getItem("InsureeData");

                if (sessionStorage.getItem("FamilyData") !== null) {

                    var FamilyId = Android.SaveFamily(sessionStorage.getItem("FamilyData"), jsonInsuree);

                    if (FamilyId > 0) {
                        sessionStorage.removeItem("FamilyData");
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
                    if (PolicyId > 0 && TotalIns >= MemberCount) {
                        $("#divProgress").hide();
                    } else {
                        $("#divProgress").hide();
                        window.open("FamilyAndInsurees.html?f=" + FamilyId, "_self");
                    }
                }
            } else {
                $("#divProgress").hide();
                Android.ShowDialog(Android.getString('FieldRequired'));
            }

        });
}