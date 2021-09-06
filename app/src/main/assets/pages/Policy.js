$(document).ready(function () {
    document.title = Android.getString('AddEditPolicy');

    if(!Android.IsBulkCNUsed()) {
        $('#ControlNumber').hide();
    }

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

    $("#Officer").hide();
    var OfficerId = Android.getOfficerId();
    var OfficerCode = Android.getOfficerCode();
    $("#ddlOfficer").val(parseInt(OfficerId));

    //LoadOfficers(LocationId, null);
    LoadProduct(RegionId, DistrictId, null);

    if (policyId != 0) {
        var strPolicy = Android.getPolicy(policyId);
        var $Policy = $.parseJSON(strPolicy);
        $("#ddlProduct").val($Policy[0]["ProdId"]);
        $("#ddlOfficer").val($Policy[0]["OfficerId"]);
        var PolicyStage = $Policy[0]["PolicyStage"];
        var StartDate = $Policy[0]["StartDate"];
        var EnrolmentDate = $Policy[0]["EnrollDate"];
        var ExpiryDate = $Policy[0]["ExpiryDate"];
        var ProdId = parseInt($Policy[0]["ProdId"]);
        var CurrentPolicyValue = $Policy[0]["PolicyValue"];
        var isOffline = parseInt($Policy[0]["isOffline"]);

        bindDataFromDatafield(strPolicy);

        $('#txtStartDate').val((StartDate));
        $('#txtExpiryDate').val(ExpiryDate);

        if(Android.IsBulkCNUsed()) {
            console.log(typeof $Policy[0]["ControlNumber"]);
            if($Policy[0]["ControlNumber"]) {
                $('#AssignedControlNumber').val($Policy[0]["ControlNumber"]).prop('readonly', true);
            } else {
                $('#AssignedControlNumber').val('').prop('readonly', false);
            }
        }

        var HSCycle = false;
        if ($('#hfHasCycle').val()) HSCycle = true;

        var NewPolicyValue = Android.getPolicyValue(EnrolmentDate, ProdId, FamilyId, $('#hffStartDate').val(), HSCycle, parseInt(policyId), PolicyStage, isOffline);
        var PolicyStatusValue = $("#hfPolicyStatus").val();

        if (NewPolicyValue != CurrentPolicyValue) {
            var Vdate = new Date(EnrolmentDate);  //or your date here
            var NewDate = ((Vdate.getMonth() + 1) + '/' + Vdate.getDate() + '/' + Vdate.getFullYear());
            Android.ShowDialog(Android.getString('PolicyValueChange') + NewDate + ' ' + Android.getString('Changed'));

        }

        $("#txtEnrolmentDate").prop('disabled', false);
        $("#ddlProduct").prop('disabled', false);
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
        //LoadOfficers(LocationId, EnrolmentDate);

    });

    $('#txtEnrolmentDate, #ddlProduct').change(function () {
        var EnrolmentDate = $('#txtEnrolmentDate').val();
        var ProdId = $('#ddlProduct').val();
        getPolicyPeriod(EnrolmentDate, parseInt(ProdId), parseInt(FamilyId), parseInt(policyId));

    });

    $('#ddlProduct').change(function () {
        if(Android.IsBulkCNUsed()) {
            var productId = $('#ddlProduct').val();
            if(productId == '0') {
                $('#AssignedControlNumber').val('').prop('readonly', false);
                return;
            }
            var controlNumber = Android.GetNextBulkCn(productId);
            if(typeof controlNumber === 'undefined') {
                Android.ShowDialog(Android.getString('noBulkCNAvailable'));
                $('#AssignedControlNumber').val('').prop('readonly', false);
            } else {
                $('#AssignedControlNumber').val(controlNumber).prop('readonly', true);
            }
        }
    });

    $('#btnSave').click(function () {
        var passed = isFormValidated();
        var jsonPolicy = createJSONString();

        if (passed == true) {
            if(Android.IsBulkCNUsed() && !$('#AssignedControlNumber').val()) {
                Android.ShowDialog(Android.getString('noBulkCNAssigned'));
                $('#AssignedControlNumber').val('').prop('readonly', false);
                return;
            }

            var PPolicyId = Android.SavePolicy(jsonPolicy, parseInt(FamilyId), parseInt(policyId));
            window.open('FamilyPolicies.html?f=' + FamilyId + '&l=' + LocationId + '&r=' + RegionId + '&d=' + DistrictId, "_self");
            $('#btnSave').attr("disabled", "disabled")
        }
        else {
            Android.ShowDialog(Android.getString('FieldRequired'));
        }
    });

    $('#txtStartDate').change(function () {
        var txtStartDate = $('#txtStartDate').val();
        var ProdId = $('#ddlProduct').val();
        getPolicyPeriod(txtStartDate, parseInt(ProdId), parseInt(FamilyId), parseInt(policyId));
    });
});

function getPolicyPeriod(EnrolmentDate, ProdId, FamilyId, policyId) {
    if (EnrolmentDate.length == 0 || ProdId == 0)
        return false;

    var Period = $.parseJSON(Android.getPolicyPeriod(parseInt(ProdId), EnrolmentDate));

    var StartDate = new Date(Period[0]["StartDate"]);
    var ExpiryDate = new Date(Period[0]["ExpiryDate"]);
    HasCycle = Period[0]["HasCycle"];

    $('#txtStartDate').val(getDateForJS(StartDate));
    $('#txtExpiryDate').val(getDateForJS(ExpiryDate));


    $("#txtStartDate").prop('disabled', HasCycle);

    fStartDate = getDateForJS(StartDate)
    //fStartDate = moment(fStartDate).toDate();
    var isOffline = $('#hfOffline').val();
    var PolicyValue = Android.getPolicyValue(EnrolmentDate, parseInt(ProdId), FamilyId, fStartDate, HasCycle, 0, "N", isOffline);

    $('#spPolicyValue').text(PolicyValue);
    $('#hfPolicyValue').val(PolicyValue);

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
    bindDropdown('ddlProduct', $Products, 'ProdId', 'ProductCode', 0, Android.getString('SelectProduct'));
}

function createJSONString() {
    var jsonPolicy = getControlsValuesJSON('li');
    return jsonPolicy;
}
