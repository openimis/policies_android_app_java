$(document).ready(function () {

    document.title = Android.getString('AddEditPremium');

    var adj = Android.getSpecificControlHtml("TotalAmount");
    if (adj == "R") {
        $('#txtAmount').attr('readonly', 'readonly');
    }

    if ($("#ddlPhotoFee").is(":visible")) {
        $("#txtAmount").keyup(function () {
            var cat = $("#ddlPhotoFee").val();
            if (cat == "") {
                $(this).val("");
            }
        });
    };

    var photoValue = 1;
    var LocationId = queryString("l");
    var FamilyId = queryString("f");
    var policyId = queryString("p");


    var RegionId = parseInt(queryString("r"));
    var DistrictId = parseInt(queryString("d"));
    var PreviousAmount = 0;

    var Paydate = null;
    var premiumId = parseInt($.trim(queryString("pr")));

    var IdlePolicy = 1;
    var ActivePolicy = 2;
    var SuspendedPolicy = 4;
    var ExpiredPolicy = 8;
    var ReadyPolicy = 16;

    getPolicyValue(policyId);

    LoadPayers(RegionId, DistrictId);

    if (premiumId != 0) {
        var strPremium = Android.getPremium(premiumId);
        var $Premium = $.parseJSON(strPremium);
        $("#ddlPayer").val($Premium[0]["PayerId"]);
        $("#ddlPayType").val($Premium[0]["PayType"]);
        $("#ddlPhotoFee").val($Premium[0]["IsPhotoFee"]);
        PreviousAmount = parseInt($Premium[0]["Amount"]);
        var policyValue = Android.getPolicyVal(policyId);
        bindDataFromDatafield(strPremium);
        var Balance = parseInt($('#spBalance').text());

        var Contribution = parseInt($('#spContribution').text())

        var policyValue = Android.getPolicyVal(policyId);
        var prevAmount = parseInt(Android.getSumPrem(policyId));
        var currentBalance = policyValue - prevAmount;

        var currentContribution = Contribution - PreviousAmount;
        var isOffline = parseInt($Premium[0]["isOffline"]);

        $("#hfBalance").val(currentBalance);
        $("#hfContribution").val(currentContribution);

    } else {
        var policyValue = Android.getPolicyVal(policyId);
        var prevAmount = parseInt(Android.getSumPrem(policyId));
        var currentBalance = policyValue - prevAmount;

        if (currentBalance <= 0) {
            $('#div-details').hide();
            $('.footer').hide();
            $("#msgAlert").text(Android.getString('PolicyCovered'));
            $("#dialog-confirm").dialog({
                resizable: false,
                height: "auto",
                width: 300,
                modal: true,
                buttons: {
                    Yes: function () {
                        $('#div-details').show();
                        $('.footer').show();
                        $(this).dialog("close");
                    },
                    No: function () {
                        window.open('PolicyPremium.html?p=' + policyId + '&l=' + LocationId + '&f=' + FamilyId + '&r=' + RegionId + '&d=' + DistrictId, "_self");
                        $(this).dialog("close");
                    }
                }
            });
        }
    }
    $('#txtAmount').change(function () {
        if ($('#ddlPhotoFee').val() == 'true') {
            $('#spBalance').text($('#hfBalance').val());
            $('#spContribution').text($('#hfContribution').val());
        }
        else {
            var ogBalance = parseInt($('#hfContribution').val());
            var ogContribution = parseInt($('#hfContribution').val());
            var Amount = parseInt($('#txtAmount').val() || 0);
            var policyValue = parseInt($('#txtPolicyValue').text());
            if (premiumId != 0) {
                var policyValue = Android.getPolicyVal(policyId);
                var prevAmount = parseInt(Android.getSumPrem(policyId));
                var balance = policyValue - prevAmount;
                var newAmt = balance + PreviousAmount;
                var newBalance = newAmt - Amount;
            } else {
                var policyValue = Android.getPolicyVal(policyId);
                var prevAmount = parseInt(Android.getSumPrem(policyId));
                var currentBalance = policyValue - prevAmount;
                var newBalance = currentBalance - Amount;

            }

            var newContribution = Amount + ogContribution;

            $('#spBalance').text(policyValue - Amount);
            $('#spContribution').text(Amount);
        }
    });

    $('#txtPayDate').change(function () {
        Paydate = ($(this).val());
    });


    var policystatus = IdlePolicy;

    $('#btnSave').click(function () {
        var results = true;
        var ReceiptNo = $('#txtReceipt').val();
        var passed = isFormValidated();
        var jsonPremium = createJSONString();

        if (passed == true) {

            var IsReceiptUnique = Android.IsReceiptNumberUnique(ReceiptNo, parseInt(FamilyId));
            var PolicyBalance = $('#spBalance').text();
            if (IsReceiptUnique == true || premiumId != 0) {

                if ($('#ddlPhotoFee').val() == 'true') {
                    var PremiumId = Android.SavePremiums(jsonPremium, parseInt(policyId), parseInt(premiumId), parseInt(FamilyId));
                    window.open('PolicyPremium.html?p=' + policyId + '&l=' + LocationId + '&f=' + FamilyId + '&r=' + RegionId + '&d=' + DistrictId, "_self");
                } else {
                    if ((parseInt($.trim(queryString("pr")))) <= 0 && PolicyBalance > 0) {

                        var maxInstallments = Android.getMaxInstallments(policyId);
                        var totalPremiums = Android.getCountPremiums(policyId);

                        if (PolicyBalance > 0 && totalPremiums < maxInstallments) {

                            if (totalPremiums == (maxInstallments - 1) && $("#ddlPhotoFee").val() == 'false') {

                                $("#msgAlert").text(Android.getString('MaxInstallment'));
                                $("#dialog-confirm").dialog({
                                    resizable: false,
                                    height: "auto",
                                    width: 300,
                                    modal: true,
                                    buttons: {
                                        Wait: function () {
                                            policystatus = IdlePolicy;
                                            var PremiumId = Android.SavePremiums(jsonPremium, parseInt(policyId), parseInt(premiumId), parseInt(FamilyId));
                                            Paydate = $('#txtPayDate').val();
                                            Android.UpdatePolicy(parseInt(policyId), Paydate, policystatus);
                                            Android.UpdateInsureePolicy(parseInt(policyId));
                                            window.open('PolicyPremium.html?p=' + policyId + '&l=' + LocationId + '&f=' + FamilyId + '&r=' + RegionId + '&d=' + DistrictId, "_self");
                                            $(this).dialog("close");
                                        },
                                        Suspend: function () {
                                            policystatus = SuspendedPolicy;
                                            var PremiumId = Android.SavePremiums(jsonPremium, parseInt(policyId), parseInt(premiumId), parseInt(FamilyId));
                                            Paydate = $('#txtPayDate').val();
                                            Android.UpdatePolicy(parseInt(policyId), Paydate, policystatus);
                                            Android.UpdateInsureePolicy(parseInt(policyId));
                                            window.open('PolicyPremium.html?p=' + policyId + '&l=' + LocationId + '&f=' + FamilyId + '&r=' + RegionId + '&d=' + DistrictId, "_self");
                                            $(this).dialog("close");
                                        },
                                        Enforce: function () {
                                            policystatus = ActivePolicy;
                                            var PremiumId = Android.SavePremiums(jsonPremium, parseInt(policyId), parseInt(premiumId), parseInt(FamilyId));
                                            Paydate = $('#txtPayDate').val();
                                            Android.UpdatePolicy(parseInt(policyId), Paydate, policystatus);
                                            Android.UpdateInsureePolicy(parseInt(policyId));
                                            window.open('PolicyPremium.html?p=' + policyId + '&l=' + LocationId + '&f=' + FamilyId + '&r=' + RegionId + '&d=' + DistrictId, "_self");
                                            $(this).dialog("close");
                                        },
                                        No: function () {
                                            $(this).dialog("close");
                                        }
                                    }
                                });
                            } else {
                                $("#msgAlert").text(Android.getString('PriceBelow'));
                                $("#dialog-confirm").dialog({
                                    resizable: false,
                                    height: "auto",
                                    width: 300,
                                    modal: true,
                                    buttons: {
                                        OK: function () {
                                            policystatus = IdlePolicy;
                                            var PremiumId = Android.SavePremiums(jsonPremium, parseInt(policyId), parseInt(premiumId), parseInt(FamilyId));
                                            Paydate = $('#txtPayDate').val();
                                            Android.UpdatePolicy(parseInt(policyId), Paydate, policystatus);
                                            Android.UpdateInsureePolicy(parseInt(policyId));
                                            window.open('PolicyPremium.html?p=' + policyId + '&l=' + LocationId + '&f=' + FamilyId + '&r=' + RegionId + '&d=' + DistrictId, "_self");
                                            $(this).dialog("close");
                                        },
                                        Enforce: function () {
                                            policystatus = ActivePolicy;
                                            var PremiumId = Android.SavePremiums(jsonPremium, parseInt(policyId), parseInt(premiumId), parseInt(FamilyId));
                                            $('#btnSave').attr("disabled", "disabled");
                                            Paydate = $('#txtPayDate').val();
                                            Android.UpdatePolicy(parseInt(policyId), Paydate, policystatus);
                                            Android.UpdateInsureePolicy(parseInt(policyId));
                                            window.open('PolicyPremium.html?p=' + policyId + '&l=' + LocationId + '&f=' + FamilyId + '&r=' + RegionId + '&d=' + DistrictId, "_self");
                                            $(this).dialog("close");
                                        },
                                        No: function () {
                                            $(this).dialog("close");
                                        }
                                    }
                                });
                            }

                        } else {
                            if (PolicyBalance <= 0) {
                                policystatus = ActivePolicy;
                                Paydate = $('#txtPayDate').val();
                                Android.UpdatePolicy(parseInt(policyId), Paydate, policystatus);
                                Android.UpdateInsureePolicy(parseInt(policyId));
                                window.open('PolicyPremium.html?p=' + policyId + '&l=' + LocationId + '&f=' + FamilyId + '&r=' + RegionId + '&d=' + DistrictId, "_self");
                            } else if (PolicyBalance > 0) {
                                $("#msgAlert").text(Android.getString('PriceBelow'));
                                $("#dialog-confirm").dialog({
                                    resizable: false,
                                    height: "auto",
                                    width: 300,
                                    modal: true,
                                    buttons: {
                                        OK: function () {
                                            policystatus = IdlePolicy;
                                            var PremiumId = Android.SavePremiums(jsonPremium, parseInt(policyId), parseInt(premiumId), parseInt(FamilyId));
                                            Paydate = $('#txtPayDate').val();
                                            Android.UpdatePolicy(parseInt(policyId), Paydate, policystatus);
                                            Android.UpdateInsureePolicy(parseInt(policyId));
                                            window.open('PolicyPremium.html?p=' + policyId + '&l=' + LocationId + '&f=' + FamilyId + '&r=' + RegionId + '&d=' + DistrictId, "_self");
                                            $(this).dialog("close");
                                        },
                                        Enforce: function () {
                                            policystatus = ActivePolicy;
                                            var PremiumId = Android.SavePremiums(jsonPremium, parseInt(policyId), parseInt(premiumId), parseInt(FamilyId));
                                            $('#btnSave').attr("disabled", "disabled");
                                            Paydate = $('#txtPayDate').val();
                                            Android.UpdatePolicy(parseInt(policyId), Paydate, policystatus);
                                            Android.UpdateInsureePolicy(parseInt(policyId));
                                            window.open('PolicyPremium.html?p=' + policyId + '&l=' + LocationId + '&f=' + FamilyId + '&r=' + RegionId + '&d=' + DistrictId, "_self");
                                            $(this).dialog("close");
                                        },
                                        No: function () {
                                            $(this).dialog("close");
                                        }
                                    }
                                });
                            } else if (results != false) {
                                window.open('PolicyPremium.html?p=' + policyId + '&l=' + LocationId + '&f=' + FamilyId + '&r=' + RegionId + '&d=' + DistrictId, "_self");
                            }
                        }

                    } else if (PolicyBalance < 0) {
                        if (!confirm(Android.getString('ExceedsPolicy'))) {
                            results = false;
                            return false;
                        }
                        if (results == true) {
                            var PremiumId = Android.SavePremiums(jsonPremium, parseInt(policyId), parseInt(premiumId), parseInt(FamilyId));
                            policystatus = ActivePolicy;
                            Paydate = $('#txtPayDate').val();
                            Android.UpdatePolicy(parseInt(policyId), Paydate, policystatus);
                            Android.UpdateInsureePolicy(parseInt(policyId));
                            window.open('PolicyPremium.html?p=' + policyId + '&l=' + LocationId + '&f=' + FamilyId + '&r=' + RegionId + '&d=' + DistrictId, "_self");
                        }
                    } else if (results == true) {
                        var PremiumId = Android.SavePremiums(jsonPremium, parseInt(policyId), parseInt(premiumId), parseInt(FamilyId));
                        policystatus = ActivePolicy;
                        Paydate = $('#txtPayDate').val();
                        Android.UpdatePolicy(parseInt(policyId), Paydate, policystatus);
                        Android.UpdateInsureePolicy(parseInt(policyId));
                        window.open('PolicyPremium.html?p=' + policyId + '&l=' + LocationId + '&f=' + FamilyId + '&r=' + RegionId + '&d=' + DistrictId, "_self");
                    }
                }


            } else {
                Android.ShowDialog(Android.getString('ReceiptNotUnique'));
            }
        } else {
            Android.ShowDialog(Android.getString('FieldRequired'));
        }
    });

    $('#ddlPhotoFee').change(function () {
        var Balance = parseInt($('#hfBalance').val());
        var Contribution = parseInt($('#hfContribution').val());
        if ($('#ddlPhotoFee').val() == 'true') {
            photoValue = 0;
            $('#spContribution').text(0);
        } else {
            if (Balance < 0 || currentBalance < 0) {
                $('#txtAmount').val(0);
                Contribution = 0;
            } else {
                $('#txtAmount').val(currentBalance);
                Contribution = currentBalance;
            }
            $('#spContribution').text(Contribution);
            $('#txtAmount').change();
        }
    });

    $('#spBalance').text(currentBalance);

    if ($("#ddlPhotoFee").is(":hidden")) {
        $('#ddlPhotoFee').val('false').trigger('change');
    }
});

function confirmPremium() {

}

function LoadPayers(RegionId, DistrictId) {
    var $Payers = Android.getPayers(RegionId, DistrictId);
    bindDropdown('ddlPayer', $Payers, 'PayerId', 'PayerName', 0, Android.getString('SelectPayer'));
}
function createJSONString() {
    var jsonPremium = getControlsValuesJSON('li');
    return jsonPremium;
}

function getPolicyValue(policyId) {
    var strPolicy = Android.getPolicy(parseInt(policyId));
    var $Policy = $.parseJSON(strPolicy);
    bindDataFromDatafield(strPolicy);
}
