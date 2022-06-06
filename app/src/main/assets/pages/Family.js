$(document).ready(function () {

    document.title = Android.getString('AddNewFamily');
    var FamilyId = queryString('f');

    if (parseInt(FamilyId) != 0)
        $('#btnNext').val(Android.getString("Save"));

    fillDropdowns();
    setControls();

    // Display of LanguageOfSMS is same as for ApprovalOfSMS
    $("#LanguageOfSMS").css("display", $("#ApprovalOfSMS").css("display"));

    $('#ddlRegion').change(function () {
        getDistricts($(this).val());
    });

    $('#ddlDistrict').change(function () {
        getWards($(this).val());
    });

    $('#ddlWard').change(function () {
        getVillages($(this).val());
    });

    $('#btnNext').click(function () {

        var passed = isFormValidated();

        if (passed == true) {
            if (FamilyId == 0 || FamilyId == null || FamilyId == undefined) {
                saveFamilyLocally();
                window.open("Insuree.html", "_self");
            } else {
                var familyData = createJSONString();
                Android.SaveFamily(familyData, '');
                window.open("FamilyAndInsurees.html?f=" + FamilyId, "_self");
            }
        }
        else
            Android.ShowDialog(Android.getString('FieldRequired'));
    });

    //if FamilyId > 0; Load family

    if (parseInt(FamilyId) != 0) {
        var strFamily = Android.getFamily(parseInt(FamilyId));

        var $Family = $.parseJSON(strFamily);
        $("#ddlRegion").val($Family[0]["RegionId"]).trigger("change");
        $("#ddlDistrict").val($Family[0]["DistrictId"]).trigger("change");
        $("#ddlWard").val($Family[0]["WardId"]).trigger("change");
        $("#ddlVillage").val($Family[0]["VillageId"]);

        var isOffline = $Family[0]["isOffline"];
        if (isOffline == 0 || isOffline == false || isOffline == "false") {
            $("#ddlRegion").attr("disabled", "disabled");
            $("#ddlDistrict").attr("disabled", "disabled");
            $("#ddlWard").attr("disabled", "disabled");
            $("#ddlVillage").attr("disabled", "disabled");
        }

        if ($Family[0].hasOwnProperty("familySMS")) {
            $("#ddlApprovalOfSMS").val($Family[0]["familySMS"]["ApprovalOfSMS"]).trigger("change");
            $("#ddlLanguageOfSMS").val($Family[0]["familySMS"]["LanguageOfSMS"]).trigger("change");
        }
        //Load remaining fields
        bindDataFromDatafield(strFamily);

    }

});

function createJSONString() {
    var jsonFamily = getControlsValuesJSON('li');
    return jsonFamily;
}

function fillDropdowns() {
    getRegions();
    getPovertyStatus();
    getConfirmationTypes();
    getFamilyTypes();
    getApprovalOfSMS();
    getLanguageOfSMS();
}

function saveFamilyLocally() {
    var jsonFamily = getControlsValuesJSON('li');
    sessionStorage.setItem("FamilyData", jsonFamily);
}

function getRegions() {
    var $Regions = Android.getRegions();
    var SelectText = Android.getString('SelectRegion');
    var rows = JSON.parse($Regions).length;
    if (rows == 1) SelectText = null;
    bindDropdown('ddlRegion', $Regions, 'LocationId', 'LocationName', 0, SelectText);
    if (rows == 1) getDistricts($('#ddlRegion').val());
}

function getDistricts(RegionId) {
    var SelectText = Android.getString('SelectDistrict');
    var $Districts = Android.getDistricts(parseInt(RegionId));
    var rows = JSON.parse($Districts).length;
    if (rows == 1) SelectText = null;
    bindDropdown('ddlDistrict', $Districts, 'LocationId', 'LocationName', 0, SelectText);
    if (rows == 1) getWards($('#ddlDistrict').val());
}

function getWards(DistrictId) {
    var SelectText = Android.getString('SelectWard');
    var OfficerCode = Android.getOfficerCode();
    //var $Wards = Android.getWardsOfficer(OfficerCode);
    var $Wards = Android.getWards(parseInt(DistrictId));
    var rows = JSON.parse($Wards).length;
    if (rows == 1) SelectText = null;
    bindDropdown('ddlWard', $Wards, 'LocationId', 'LocationName', 0, SelectText);
    if (rows == 1) getVillages($('#ddlWard').val());

}

function getVillages(WardId) {
    var SelectText = Android.getString('SelectVillage');
    //var $Villages = Android.getVillagesOfficer(WardId.toString());
    var $Villages = Android.getVillages(parseInt(WardId.toString()));
    var rows = JSON.parse($Villages).length;
    if (rows == 1) SelectText = null;
    bindDropdown('ddlVillage', $Villages, 'LocationId', 'LocationName', 0, SelectText);
}

function getPovertyStatus() {
    var $YesNo = Android.getYesNo();
    bindDropdown('ddlPovertyStatus', $YesNo, 'value', 'key', null, Android.getString('SelectPovertyStatus'));
}

function getConfirmationTypes() {
    $textLanguage = "ConfirmationType";
    if (Android.getSelectedLanguage() != "en") {
        $textLanguage = "AltLanguage";
    }
    var $ConfirmationTypes = Android.getConfirmationTypes();
    bindDropdown('ddlConfirmationType', $ConfirmationTypes, 'ConfirmationTypeCode', $textLanguage, "", Android.getString('SelectConfirmationType'));
}

function getFamilyTypes() {
    $textLanguage = "FamilyType";
    if (Android.getSelectedLanguage() != "en") {
        $textLanguage = "AltLanguage";
    }
    var $FamilyTypes = Android.getGroupTypes();
    bindDropdown('ddlGroupType', $FamilyTypes, 'FamilyTypeCode', $textLanguage, 0, Android.getString('SelectFamilyType'));
}

function getApprovalOfSMS() {
    var $ApprovalTypes = Android.getApprovalOfSMS();
    bindDropdown('ddlApprovalOfSMS', $ApprovalTypes, 'value', 'key', "", Android.getString('approvalOfSMS'));
}

function getLanguageOfSMS() {
    $textLanguage = "LanguageName";
    var $Languages = Android.getLanguagesOfSMS();
    bindDropdown('ddlLanguageOfSMS', $Languages, 'LanguageCode', $textLanguage, "", Android.getString('languageOfSMS'));
}
