$(document).ready(function () {
    document.title = Android.getString('AddEditPolicy');
    fillDropdowns();

    if(!Android.IsBulkCNUsed()) {
        $('#ControlNumber').hide();
    }

    if(!Android.getRule("isVisibleSigningDate")){
        $('#SigningDate').hide();
    }

    $("#dialog-confirm").attr("title", Android.getString('Confirm'));

    var LocationId = parseInt(queryString("l"));
    var FamilyId = parseInt(queryString("f"));
    var strOfficerLocation = Android.getOfficerLocation();
    var $Locations = $.parseJSON(strOfficerLocation);
    var RegionId = parseInt($Locations[0]["RegionId"]);
    var DistrictId = parseInt($Locations[0]["DistrictId"]);
    var policyId = parseInt(queryString("p"));

    var HasCycle = null;
    var fStartDate = null;
    $("#txtEffectiveDate").prop('disabled', true);
    $("#txtExpiryDate").prop('disabled', true);

    $("#Officer").hide();
    var OfficerId = Android.getOfficerId();
    var OfficerCode = Android.getOfficerCode();
    $("#ddlOfficer").val(parseInt(OfficerId));

    //LoadOfficers(LocationId, null);
    //LoadProduct(RegionId, DistrictId, null);
    LoadContributionPlan(null);

    if (policyId != 0) {
        var strPolicy = Android.getPolicy(policyId);
        var $Policy = $.parseJSON(strPolicy);
        //$("#ddlProduct").val($Policy[0]["ProdId"]);
        $("#ddlContributionPlan").val($Policy[0]["ContributionPlanId"]);
         $("#ddlPeriodicity").val($Policy[0]["Periodicity"]);
        $("#ddlOfficer").val($Policy[0]["OfficerId"]);
        $("#ddlPaymentDay").val($Policy[0]["PaymentDay"]);
        var PolicyStage = $Policy[0]["PolicyStage"];
        var StartDate = $Policy[0]["StartDate"];
        var EnrolmentDate = $Policy[0]["EnrollDate"];
        var ExpiryDate = $Policy[0]["ExpiryDate"];
        var SigningDate = $Policy[0]["SigningDate"];
        var ProdId = parseInt($Policy[0]["ProdId"]);
        var CPId = parseInt($Policy[0]["ContributionPlanId"]);
        var CurrentPolicyValue = $Policy[0]["PolicyValue"];
        var isOffline = parseInt($Policy[0]["isOffline"]);

        bindDataFromDatafield(strPolicy);

        $('#txtStartDate').val((StartDate));
        $('#txtExpiryDate').val(ExpiryDate);
        $('#txtSigningDate').val(SigningDate);

        if(Android.IsBulkCNUsed()) {
            if($Policy[0]["ControlNumber"]) {
                $('#AssignedControlNumber').val($Policy[0]["ControlNumber"]);
            } else {
                $('#AssignedControlNumber').val('');
            }
        }

        var HSCycle = false;
        if ($('#hfHasCycle').val()) HSCycle = true;

        //var NewPolicyValue = Android.getPolicyValue(EnrolmentDate, ProdId, FamilyId, $('#hffStartDate').val(), HSCycle, parseInt(policyId), PolicyStage, isOffline);
        var PolicyStatusValue = $("#hfPolicyStatus").val();

        /*if (NewPolicyValue != CurrentPolicyValue) {
            var Vdate = new Date(EnrolmentDate);  //or your date here
            var NewDate = ((Vdate.getMonth() + 1) + '/' + Vdate.getDate() + '/' + Vdate.getFullYear());
            Android.ShowDialog(Android.getString('PolicyValueChange') + NewDate + ' ' + Android.getString('Changed'));

        }*/

        $("#txtEnrolmentDate").prop('disabled', false);
        $("#ddlProduct").prop('disabled', false);
        $("#ddlContributionPlan").prop('disabled', false);
        $("#txtStartDate").prop('disabled', true);
        if (PolicyStatusValue == 1) {
            $("#txtExpiryDate").prop('disabled', false);
        } else {
            $("#txtExpiryDate").prop('disabled', true);
        }
    }

    $('#txtEnrolmentDate').change(function () {
        var EnrolmentDate = $('#txtEnrolmentDate').val();
        LoadProduct(RegionId, DistrictId, EnrolmentDate);
        $("#txtSigningDate").prop('min',EnrolmentDate);
        //LoadOfficers(LocationId, EnrolmentDate);
    });

    $('#ddlContributionPlan').change(function() {
        var ContributionPlanCode = Android.getCPCode($('#ddlContributionPlan').val());
        if(ContributionPlanCode == "AMOG" || ContributionPlanCode == "AMOE" || ContributionPlanCode == "AMOS"){
            $('#ddlPeriodicity').val("M")
        } else if(ContributionPlanCode == "AMOS1" || ContributionPlanCode == "AMOS2" || ContributionPlanCode == "AMOS3" || ContributionPlanCode == "AMOS4"){
            $('#ddlPeriodicity').val("Q")
        } else if(ContributionPlanCode == "AMS"){
            $('#ddlPeriodicity').val("Y")
        }
    });

    $('#ddlPeriodicity').change(function() {
        if($('#ddlContributionPlan').val() != "0"){
            var ContributionPlanCode = Android.getCPCode($('#ddlContributionPlan').val());
            var periodicity = $('#ddlPeriodicity').val();
            var ans = Android.isValidPeriodicity(ContributionPlanCode, periodicity);
            if (ans != true) {
                $('#ddlPeriodicity').val("");
               $('#ddlPeriodicity').focus();
            }
        }
    })

    $('#txtEnrolmentDate, #ddlContributionPlan, #ddlPeriodicity').change(function () {
        var EnrolmentDate = $('#txtEnrolmentDate').val();
        var ProdId = $('#ddlProduct').val();
        var ContributionPlanId = $('#ddlContributionPlan').val();
        getPolicyPeriod(EnrolmentDate, ContributionPlanId, parseInt(FamilyId), parseInt(policyId));
    });

    $('#ddlProduct').change(function () {
        if(Android.IsBulkCNUsed()) {
            var productId = $('#ddlProduct').val();
            if(productId == '0') {
                $('#AssignedControlNumber').val('');
                return;
            }
            var controlNumber = Android.GetNextBulkCn(productId);
            if(typeof controlNumber === 'undefined') {
                Android.ShowDialog(Android.getString('noBulkCNAvailable'));
                $('#AssignedControlNumber').val('');
            } else {
                $('#AssignedControlNumber').val(controlNumber);
            }
        }
    });

    /*$('#ddlContributionPlan').change(function () {
           var CPId = $('#ddlContributionPlan').val();
           var policyValue = Android.GetContributionPlanValue(parseInt(FamilyId),CPId);
           var finalValue;

           var fun = JSON.parse(policyValue);
           with(fun) {
                   // prints "foo"
                   finalValue = eval(remoteFunction);
           }

           var periodicity = $('#ddlPeriodicity').val();

           $('#spPolicyValue').text(finalValue);
           $('#hfPolicyValue').val(finalValue);
    });*/

    function savePolicy() {
        var jsonPolicy = createJSONString();
        var PPolicyId = Android.SavePolicy(jsonPolicy, parseInt(FamilyId), parseInt(policyId));
        window.open('FamilyPolicies.html?f=' + FamilyId + '&l=' + LocationId + '&r=' + RegionId + '&d=' + DistrictId, "_self");
        $('#btnSave').attr("disabled", "disabled")
    }

    $('#btnSave').click(function () {
        var passed = isFormValidated();

        if (passed == true) {
            if(Android.IsBulkCNUsed() && !$('#AssignedControlNumber').val()) {
                Android.ShowDialog(Android.getString('noBulkCNAssigned'));
                $('#AssignedControlNumber').val('');
                return;
            }

            if(Android.IsBulkCNUsed() && !Android.isFetchedControlNumber($('#AssignedControlNumber').val())) {
                $("#msgAlert").text(Android.getStringWithArgument('ConfirmControlNumber', $('#AssignedControlNumber').val()));
                $("#dialog-confirm").dialog({
                    resizable: false,
                    height: "auto",
                    width: 300,
                    modal: true,
                    buttons: [
                    {
                        text: Android.getString("Yes"),
                        click: function () {
                            savePolicy();
                            $(this).dialog("close");
                        }
                    },
                    {
                        text: Android.getString("No"),
                        click: function () {
                            $(this).dialog("close");
                        }
                    }
                    ]
                });
            }
            else {
                savePolicy()
            }
        }
        else {
            Android.ShowDialog(Android.getString('FieldRequired'));
        }
    });

    $('#txtStartDate').change(function () {
        var txtStartDate = $('#txtStartDate').val();
        var CPId = $('#ddlContributionPlan').val();
        getPolicyPeriod(txtStartDate, CPId, parseInt(FamilyId), parseInt(policyId));
    });

    $('#txtSigningDate').on('change', function() {
        var signingDate = $(this).val();
        if (signingDate) {
            $('#ddlPaymentDay').attr('required', true);
        } else {
            $('#ddlPaymentDay').removeAttr('required');
        }
    });

});

function getPolicyPeriod(EnrolmentDate, CpId, FamilyId, policyId) {
    if (EnrolmentDate.length == 0 || CpId == 0)
        return false;

    var ProdId = Android.getContributionPlanProduct(CpId);
    var Periodicity = $('#ddlPeriodicity').val()
    var Period = $.parseJSON(Android.getPolicyPeriod(parseInt(ProdId), EnrolmentDate, Periodicity));

    var StartDate = new Date(Period[0]["StartDate"]);
    var ExpiryDate = new Date(Period[0]["ExpiryDate"]);
    HasCycle = Period[0]["HasCycle"];

    $('#txtStartDate').val(getDateForJS(StartDate));
    $('#txtExpiryDate').val(getDateForJS(ExpiryDate));


    $("#txtStartDate").prop('disabled', HasCycle);

    fStartDate = getDateForJS(StartDate)
    //fStartDate = moment(fStartDate).toDate();
    var isOffline = $('#hfOffline').val();
    //var PolicyValue = Android.getPolicyValue(EnrolmentDate, ProdId, FamilyId, fStartDate, HasCycle, 0, "N", isOffline);
    var PolicyValue = Android.GetContributionPlanValue(parseInt(FamilyId),CpId)
    var finalValue;
    var fun = JSON.parse(PolicyValue);
    with(fun) {
        finalValue = eval(remoteFunction);
    }

    var periodicity = $('#ddlPeriodicity').val();
    if(periodicity == "M") {
        finalValue = finalValue * 1;
    } else if (periodicity == "Q") {
        finalValue = finalValue * 3;
    } else if (periodicity == "S") {
        finalValue = finalValue * 6;
    } else if (periodicity == "Y") {
        finalValue = finalValue * 12;
    }

    $('#spPolicyValue').text(finalValue);
    $('#hfPolicyValue').val(finalValue);

    $('#hfHasCycle').val(HasCycle);
    $('#hffStartDate').val(fStartDate);
}
function LoadOfficers(LocationId, EnrolmentDate) {
    var OfficerId = Android.getOfficerId();

    var $Officers = Android.getOfficers(LocationId, EnrolmentDate);
    bindDropdown('ddlOfficer', $Officers, 'OfficerId', 'Code', 0, Android.getString('SelectOfficer'));

    $("#ddlOfficer").val(parseInt(OfficerId));
    $("#ddlOfficer").prop('disabled', true);
    $("#ddlOfficer").css('display', 'none');

}
function LoadProduct(RegionId, DistrictId, EnrolmentDate) {
    var $Products = Android.getProducts(parseInt(RegionId), parseInt(DistrictId), EnrolmentDate);
    bindDropdown('ddlProduct', $Products, 'ProdId', 'ProductNameCombined', 0, Android.getString('SelectProduct'));
}

function LoadContributionPlan(EnrolmentDate) {
    var $ContributionPlans = Android.getContributionPlans(EnrolmentDate);
    bindDropdown('ddlContributionPlan', $ContributionPlans, 'CpId', 'CombinedName', 0, Android.getString('SelectContribution'));
}

function createJSONString() {
    var jsonPolicy = getControlsValuesJSON('li');
    return jsonPolicy;
}

function fillDropdowns() {
    getPaymentDayValue();
    getPeriodicityValue();
}

function getPeriodicityValue()  {
    $textLanguage = "Name";
    if (Android.getSelectedLanguage() != "en") {
        $textLanguage = "AltLanguage";
    }
    var $Period= Android.getPeriodicity();
    bindDropdown('ddlPeriodicity', $Period, 'Code', $textLanguage, Android.getString('Periodicity'));
}

function getPaymentDayValue() {
    var PaymentDay = Android.getPaymentDay();
    bindDropdown('ddlPaymentDay', PaymentDay, 'Value', 'Label', null);
}
