$(document).ready(function () {

     $('#PaymentMethod').hide();
     $("#Education").hide();

    //Hide the relationship if the insuree is the HOF
    if (sessionStorage.getItem("FamilyData") !== null || sessionStorage.getItem("SubFamilyData") !== null) {
        $("#Relationship").hide();
        $("#ddlRelationship").prop("required", false);
        $('#PaymentMethod').show();
        $("#txtPhoneNumber").prop("required", true);
    }

    if($('#txtInsuranceNumber').val() == ""){
        var insuranceNumber = Math.floor(Math.random() * 9000000000) + 1000000000;
        $('#txtInsuranceNumber').val(insuranceNumber);
    }


    document.title = Android.getString('AddEditInsuree');
    Android.shutDownProgress();
    $("#hfNewPhotoPath").val("");
    var FamilyId = queryString('f');

    var FSPDistrictCss = $('#FSPDistrict').css('display');
    if (FSPDistrictCss == 'none') $('#FSPRegion').css("display", "none");

    var CurrentDistrictCss = $('#CurrentDistrict').css('display');
    if (CurrentDistrictCss == 'none') $('#CurrentRegion').css("display", "none");

    fillDropdowns();
    $('#OtherHousehold').hide();
    $('#AccountDetails').hide();
    $('#Education').hide();

    $('#ddlCurrentRegion').change(function () {
        fillCurrentDistricts(parseInt($(this).val()));
    });

    $('#ddlFSPRegion').change(function () {
        fillFSPDistricts($(this).val());
    });

    $('#ddlCurrentDistrict').change(function () {
        fillCurrentWards(parseInt($(this).val()));
    });

    $('#ddlCurrentMunicipality').change(function () {
        fillCurrentVillages(parseInt($(this).val()));
    });

    $('#ddlFSPDistrict, #ddlFSPCategory').change(function () {
        var DistrictId = $('#ddlFSPDistrict').val();
        var FSPCategory = $('#ddlFSPCategory').val();
        fillFSP(DistrictId, FSPCategory);
    });

    $('#txtInsuranceNumber').change(function () {
        var Ins = $('#txtInsuranceNumber').val();
        var ans = Android.isValidInsuranceNumber(Ins);
        if (ans != true) {
            $('#txtInsuranceNumber').val("");
            $('#txtInsuranceNumber').focus();
        }
        var ImagePath = Android.GetListOfImagesContain(Ins);

        if (ImagePath.length > 0) {
            $('#imgInsuree').attr('src', 'file://' + ImagePath);
        } else {
            $('#imgInsuree').attr('src', '');
        }

        $("#hfNewPhotoPath").val("");
    });

    $('#txtBirthDate').change(function () {
        fillAge($(this).val());
    });

    $('#ddlRelationship').change(function () {
        var relationId = $('#ddlRelationship').val();
        if (relationId == 3) {
            $("#Education").show();
            $('#ddlEducation').prop("required", true);
        } else {
            $("#Education").hide();
            $("#ddlEducation").prop("required", false);
        }
    });

    $("#ddlMaritalStatus").change(function () {
        var maritalStatus = $("#ddlMaritalStatus").val();
        if (maritalStatus == "P") {
            $('#OtherHousehold').show();
        } else {
            $('#OtherHousehold').hide();
        }
    });

    $("#ddlPaymentMethod").change(function () {
        var paymentMethod = $("#ddlPaymentMethod").val();
        if (paymentMethod == "PB") {
            $('#AccountDetails').show();
        } else {
            $('#AccountDetails').hide();
        }
    });

    $("#txtIdentificationNumber").change(function () {
        var Ins = $('#txtIdentificationNumber').val();
        var ans = Android.isValidIdentificationNumber(Ins);
        if (ans != true) {
            $('#txtIdentificationNumber').val("");
            $('#txtIdentificationNumber').focus();
        }
    })


    $('#spPleaseWait').text(Android.getString('saving'));

    $('#btnSave').click(function () {
        $("#divProgress").show();

        getImage();

        var passed = isFormValidated();

        if (passed == true) {
            var jsonInsuree = createJSONString();

            if ($('#hfNewPhotoPath').val() != "" || $('#hfImagePath').val() != "") {

                if (sessionStorage.getItem("FamilyData") !== null) {
                    var FamilyId = Android.SaveFamily(sessionStorage.getItem("FamilyData"), jsonInsuree);
                    var FamilyType = sessionStorage.getItem("FamilyType");

                    if (FamilyId > 0) {
                        sessionStorage.removeItem("FamilyData");
                        $(this).attr("disabled", "disabled");
                        if (FamilyType == "P") {
                            window.open("FamilyPolygamy.html?f=" + FamilyId, "_self");
                        } else {
                            window.open("FamilyAndInsurees.html?f=" + FamilyId, "_self");
                        }
                    }

                } else if (sessionStorage.getItem("SubFamilyData") !== null) {
                    var FamilyId = queryString('f');
                    var SubFamilyId = Android.SaveSubFamily(sessionStorage.getItem("SubFamilyData"), jsonInsuree, parseInt(FamilyId));
                    if (SubFamilyId > 0) {
                        sessionStorage.removeItem("SubFamilyData");
                        $(this).attr("disabled", "disabled");
                        window.open("FamilyAndInsurees.html?f=" + SubFamilyId, "_self");
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
                        window.open("FamilyAndInsurees.html?f=" + FamilyId, "_self");
                    } else {
                        var isPolygamy = queryString("isPolygamy");
                        if(isPolygamy == 1){
                            $("#divProgress").hide();
                            window.open("FamilyPolygamy.html?f=" + FamilyId, "_self");
                        }else{
                            $("#divProgress").hide();
                            window.open("FamilyAndInsurees.html?f=" + FamilyId, "_self");
                        }
                    }
                }
            } else {
                $("#divProgress").hide();
                Android.ShowDialog(Android.getString('PhotoRequired'));
            }
        } else {
            $("#divProgress").hide();
            Android.ShowDialog(Android.getString('FieldRequired'));
        }

    });

    $(window).bind("onbeforeunload", function () {
        Android.showDialog('bye');
    });

    window.onunload = function () {
        sessionStorage.removeItem("FamilyData");
    }

    //if insureeid is passed load the insuree
    var InsureeId = queryString("i");
    var FamilyId = queryString("f");

    $('#btnScan').attr('src', '../images/scan.png');

    if (parseInt(InsureeId) > 0 || parseInt(InsureeId) < 0) {
        var Insuree = Android.getInsuree(parseInt(InsureeId));
        bindDataFromDatafield(Insuree);
        var PhotoPath = $.parseJSON(Insuree)[0]["PhotoPath"];
        var IsOffline = parseInt($.parseJSON(Insuree)[0]["isOffline"]);
        var marital = $.parseJSON(Insuree)[0]["Marital"];
        var relation = $.parseJSON(Insuree)[0]["Relationship"];
        var payment = $.parseJSON(Insuree)[0]["PaymentMethod"];
        var dob = $.parseJSON(Insuree)[0]["DOB"];
        var familyHeader = Android.getFamilyHeader(parseInt(FamilyId));
        if ($.parseJSON(Insuree)[0]["isHead"] == "true" || $.parseJSON(Insuree)[0]["isHead"] == "false") {
            if ($.parseJSON(Insuree)[0]["CHFID"] == $.parseJSON(familyHeader)[0]["InsureeChfId"]) {
                $("#Relationship").hide();
                $('#PaymentMethod').show();
                $("#ddlRelationship").prop("required", false);
                $("#txtPhoneNumber").prop("required", true);
            }else{
                $('#PaymentMethod').hide();
                $("#ddlRelationship").prop("required", true);
            }
        } else {
            //var head = parseInt($.parseJSON(Insuree)[0]["isHead"]);

            if ($.parseJSON(Insuree)[0]["CHFID"] == $.parseJSON(familyHeader)[0]["InsureeChfId"]) {
                $("#Relationship").hide();
                $('#PaymentMethod').show();
                $("#ddlRelationship").prop("required", false);
                $("#txtPhoneNumber").prop("required", true);
            }else{
                $('#PaymentMethod').hide();
                $("#ddlRelationship").prop("required", true);
                $("#txtPhoneNumber").prop("required", false);
            }
        }

        if($.parseJSON(Insuree)[0]["CardIssued"] == "true"){
            $('#ddlBeneficiaryCard').val(1)
        } else {
            $('#ddlBeneficiaryCard').val(2)
        }

        fillAge(dob);

        if(relation == 3){
            $("#Education").show();
        }

        if (marital == 'P') {
            $('#OtherHousehold').show();
        }

        if (payment == 'PB') {
            $('#AccountDetails').show();
        }

        $('#ddlPaymentMethod').val($.parseJSON(Insuree)[0]["PaymentMethod"]);

        var Ins = $('#txtInsuranceNumber').val();
        if (PhotoPath.length == 0) {
            PhotoPath = Android.GetListOfImagesContain(Ins);
        }

        if (PhotoPath.length > 0) {
            var photoFolder = Android.GetSystemImageFolder();
            if (PhotoPath.indexOf(photoFolder) == -1) {
                PhotoPath = photoFolder + PhotoPath;
            }
        }

        $("#imgInsuree").attr('src', PhotoPath);

        $('#hfIsOffline').val(IsOffline);
        $('#ddlCurrentRegion').val($.parseJSON(Insuree)[0]["CurRegion"]).trigger("change");
        $('#ddlCurrentDistrict').val($.parseJSON(Insuree)[0]["CurDistrict"]).trigger("change");
        $('#ddlCurrentMunicipality').val($.parseJSON(Insuree)[0]["CurWard"]).trigger("change");
        $('#ddlCurrentVillage').val($.parseJSON(Insuree)[0]["CurVillage"]);
        $('#ddlFSPRegion').val($.parseJSON(Insuree)[0]["FSPRegion"]).trigger("change");

        try {
            $('#ddlFSPDistrict').val($.parseJSON(Insuree)[0]["FSPDistrict"]).trigger("change");
        } catch (e) {
            console.log(e);
        }
        $('#ddlFSP').val($.parseJSON(Insuree)[0]["HFID"]);

    } else {
        $("#hfInsureeId").val(0);
    }

    $("#imgInsuree").click(function () {
        Android.selectPicture();
    });

    $('#btnScan').click(function () {
        Android.getScannedNumber();
    });

    if ($('#hfIsOffline').val() == "0") {
        $('#txtInsuranceNumber').attr("disabled", true);
        $('#btnScan').hide();
    }

});

function fillDropdowns() {
    fillRelationship();
    fillGender();
    fillMaritalStatus();
    fillBeneficiaryCard();
    fillCurrentRegion();
    //fillCurrentDistricts();
    fillProfessions();
    fillEducations();
    fillIdentificationTypes();
    fillFSPRegions();
    fillFSPDistricts();
    fillFSPCategory();
    fillVulnerability();
    fillPaymentMethods();
    fillIncomeLevels();
}

// called from java after the image was selected by the user
function selectImageCallback(imagePath) {
    if (imagePath != "") {
        $("#hfNewPhotoPath").val(imagePath);
        $("#imgInsuree").attr('src', imagePath);
    }
}

// called from java after the image was selected by the user
function scanQrCallback(insureeNumber) {
    var ans = Android.isValidInsuranceNumber(insureeNumber);
    if (ans) {
        $('#txtInsuranceNumber').val(insureeNumber);
        getImage();
    } else {
        $('#txtInsuranceNumber').val("");
        $('#txtInsuranceNumber').focus();
    }
}

function fillRelationship() {
    $textLanguage = "Relation";
    if (Android.getSelectedLanguage() != "en") {
        $textLanguage = "AltLanguage";
    }
    var $Relations = Android.getRelationships();
    bindDropdown('ddlRelationship', $Relations, 'RelationId', $textLanguage, null, null);
}

function fillGender() {
    $textLanguage = "Gender";
    if (Android.getSelectedLanguage() != "en") {
        $textLanguage = "AltLanguage";
    }
    var $Gender = Android.getGender();
    bindDropdown('ddlGender', $Gender, 'Code', $textLanguage, null, Android.getString('SelectGender'));
}

function fillMaritalStatus() {
    var $MaritalStatus = Android.getMaritalStatus();
    bindDropdown('ddlMaritalStatus', $MaritalStatus, 'Code', 'Status', null, null);
}

function fillBeneficiaryCard() {
    var $YesNo = Android.getYesNo();
    bindDropdown('ddlBeneficiaryCard', $YesNo, 'value', 'key', null, Android.getString('SelectBeneficiary'));
}

function fillCurrentRegion() {
    var $Regions = Android.getRegionsWO();
    bindDropdown('ddlCurrentRegion', $Regions, 'LocationId', 'LocationName', null, Android.getString('SelectRegion'));
}

function fillFSPRegions() {
    var $Regions = Android.getRegionsWO();
    bindDropdown('ddlFSPRegion', $Regions, 'LocationId', 'LocationName', null, Android.getString('SelectRegion'));
}

function fillCurrentDistricts(RegionId) {
    var $Districts = Android.getDistrictsWO(RegionId);
    bindDropdown('ddlCurrentDistrict', $Districts, 'LocationId', 'LocationName',null, Android.getString('SelectDistrict'));
}

function fillCurrentWards(DistrictId) {
    var $Wards = Android.getWards(parseInt(DistrictId));
    bindDropdown('ddlCurrentMunicipality', $Wards, 'LocationId', 'LocationName', null, Android.getString('SelectWard'));

}

function fillCurrentVillages(WardId) {
    var $Villages = Android.getVillages(parseInt(WardId));
    bindDropdown('ddlCurrentVillage', $Villages, 'LocationId', 'LocationName', null, Android.getString('SelectVillage'));
}

function fillProfessions() {
    $textLanguage = "Profession";
    if (Android.getSelectedLanguage() != "en") {
        $textLanguage = "AltLanguage";
    }
    var $Professions = Android.getProfessions();
    bindDropdown('ddlProfession', $Professions, 'ProfessionId', $textLanguage, null, null);
}

function fillEducations() {
    $textLanguage = "Education";
    if (Android.getSelectedLanguage() != "en") {
        $textLanguage = "AltLanguage";
    }
    var $Educations = Android.getEducations();
    bindDropdown('ddlEducation', $Educations, 'EducationId', $textLanguage, null, Android.getString('SelectEducation'));
}

function fillIdentificationTypes() {
    $textLanguage = "IdentificationTypes";
    if (Android.getSelectedLanguage() != "en") {
        $textLanguage = "AltLanguage";
    }
    var $idTypes = Android.getIdentificationTypes();
    bindDropdown('ddlIdentificationType', $idTypes, 'IdentificationCode', $textLanguage, "", Android.getString('SelectIdentificationType'));
}

function fillFSPDistricts(RegionId) {
    var $Districts = Android.getDistrictsWO(parseInt(RegionId));
    bindDropdown('ddlFSPDistrict', $Districts, 'LocationId', 'LocationName', null, Android.getString('SelectDistrict'));
}

function fillFSPCategory() {
    var $HFLevels = Android.getHFLevels();
    bindDropdown('ddlFSPCategory', $HFLevels, 'Code', 'HFLevel', null, null);
}

function fillFSP(DistrictId, HFLevel) {
    var $HF = Android.getHF(parseInt(DistrictId), HFLevel);
    bindDropdown('ddlFSP', $HF, 'HFID', 'HF', null, Android.getString('SelectHF'));
}

function fillVulnerability() {
    var $Vulnerability = Android.getVulnerability();
    bindDropdown('ddlVulnerability', $Vulnerability, 'value', 'key', "", Android.getString('SelectVulnerability'));
}

function fillPaymentMethods() {
    var $PaymentMethods = Android.getPaymentMethod();
    bindDropdown('ddlPaymentMethod', $PaymentMethods, 'Code', 'Method', null, null);
}

function fillIncomeLevels() {
    $textLanguage = "FirstLanguage";
    if (Android.getSelectedLanguage() != "en") {
        $textLanguage = "SecondLanguage";
    }
    var $IncomeLevels = Android.getIncomeLevels();
    bindDropdown('ddlIncomeLevel', $IncomeLevels, 'IncomeLevelID', $textLanguage, null, Android.getString('SelectIncomeLevel'));
}

function createJSONString() {
    var jsonInsuree = getControlsValuesJSON('li');
    return jsonInsuree;
}

function getImage() {
    var Ins = $('#txtInsuranceNumber').val();
    if($('#imgInsuree').attr('src') != ""){
        $("#hfImagePath").val($('#imgInsuree').attr('src'));
    } else{
        var ImagePath = Android.GetListOfImagesContain(Ins);
        if (ImagePath.length > 0) {
           $('#imgInsuree').attr('src', 'file://' + ImagePath);
        } else {
           $('#imgInsuree').attr('src', '');
        }
        $("#hfImagePath").val($('#imgInsuree').attr('src'));
    }
}

function fillAge(Birthday) {
    var today = new Date();
    var birthDate = new Date(Birthday)

    var age = today.getFullYear() - birthDate.getFullYear();
    var ageOfMajority = Android.getAgeOfMajority();

    if (age < ageOfMajority) {
        $("#Education").show();
        if($("#ddlRelationship").val() == 3){
            $("#ddlEducation").prop("required", true);
        }else{
            $("#ddlEducation").prop("required", false);
        }
    } else {
        $("#Education").hide();
        $("#ddlEducation").prop("required", false);
    }
}