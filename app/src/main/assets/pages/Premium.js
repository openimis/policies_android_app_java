$(document).ready(function () {

    document.title = Android.getString('AddEditPremium');

    var adj = Android.getSpecificControlHtml("TotalAmount");
    if (adj == "M" || adj == "R") {
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

    $("#dialog-confirm").attr("title", Android.getString('Confirm'));

    var photoValue = 1;
    var LocationId = queryString("l");
    var FamilyUUID = queryString("f");
    var policyUUID = queryString("p");


    var RegionId = parseInt(queryString("r"));
    var DistrictId = parseInt(queryString("d"));
    var PreviousAmount = 0;

    var Paydate = null;
    var premiumUUID = $.trim(queryString("pr"));

    var IdlePolicy = 1;
    var ActivePolicy = 2;
    var SuspendedPolicy = 4;
    var ExpiredPolicy = 8;
    var ReadyPolicy = 16;

    getPolicyValue(policyUUID);

    LoadPayers(RegionId, DistrictId);

    if (premiumUUID.length != 0) {
        var strPremium = Android.getPremium(premiumUUID);
        var $Premium = $.parseJSON(strPremium);
        $("#ddlPayer").val($Premium[0]["PayerId"]);
        $("#ddlPayType").val($Premium[0]["PayType"]);
        $("#ddlPhotoFee").val($Premium[0]["IsPhotoFee"]);
        PreviousAmount = parseInt($Premium[0]["Amount"]);
        var policyValue = Android.getPolicyVal(policyUUID);
        bindDataFromDatafield(strPremium);
        var Balance = parseInt($('#spBalance').text());

        var Contribution = parseInt($('#spContribution').text())

        var policyValue = Android.getPolicyVal(policyUUID);
        var prevAmount = parseInt(Android.getSumPrem(policyUUID));
        var currentBalance = policyValue - prevAmount;

        var currentContribution = Contribution - PreviousAmount;
        var isOffline = parseInt($Premium[0]["isOffline"]);

        $("#hfBalance").val(currentBalance);
        $("#hfContribution").val(currentContribution);

    } else {
        var policyValue = Android.getPolicyVal(policyUUID);
        var prevAmount = parseInt(Android.getSumPrem(policyUUID));
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
                buttons: [
                {
                    text: Android.getString("Yes"),
                    click: function () {
                        $('#div-details').show();
                        $('.footer').show();
                        $(this).dialog("close");
                    }
                },
                {
                    text: Android.getString("No"),
                    click: function () {
                        window.open('PolicyPremium.html?p=' + policyUUID + '&l=' + LocationId + '&f=' + FamilyUUID + '&r=' + RegionId + '&d=' + DistrictId, "_self");
                        $(this).dialog("close");
                    }
                }
                ]
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
            if (premiumUUID.length != 0) {
                var policyValue = Android.getPolicyVal(policyUUID);
                var prevAmount = parseInt(Android.getSumPrem(policyUUID));
                var balance = policyValue - prevAmount;
                var newAmt = balance + PreviousAmount;
                var newBalance = newAmt - Amount;
            } else {
                var policyValue = Android.getPolicyVal(policyUUID);
                var prevAmount = parseInt(Android.getSumPrem(policyUUID));
                var currentBalance = policyValue - prevAmount;
                var newBalance = currentBalance - Amount;
            }

            var newContribution = Amount + ogContribution;

            $('#spBalance').text(newBalance);
            $('#spContribution').text(newContribution);
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

            var IsReceiptUnique = Android.IsReceiptNumberUnique(ReceiptNo, FamilyUUID);
            var PolicyBalance = $('#spBalance').text();
            if (IsReceiptUnique == true || premiumUUID.length != 0) {

                if ($('#ddlPhotoFee').val() == 'true') {
                    var PremiumUUID = Android.SavePremiums(jsonPremium, policyUUID, premiumUUID, FamilyUUID);
                    window.open('PolicyPremium.html?p=' + policyUUID + '&l=' + LocationId + '&f=' + FamilyUUID + '&r=' + RegionId + '&d=' + DistrictId, "_self");
                } else {
                    if ((parseInt($.trim(queryString("pr")))) <= 0 && PolicyBalance > 0) {

                        var maxInstallments = Android.getMaxInstallments(policyUUID);
                        var totalPremiums = Android.getCountPremiums(policyUUID);

                        if (PolicyBalance > 0 && totalPremiums < maxInstallments) {

                            if (totalPremiums == (maxInstallments - 1) && $("#ddlPhotoFee").val() == 'false') {

                                $("#msgAlert").text(Android.getString('MaxInstallment'));
                                $("#dialog-confirm").dialog({
                                    resizable: false,
                                    height: "auto",
                                    width: 300,
                                    modal: true,
                                    buttons: [
										{
											text: Android.getString("Wait"),
											click: function () {
												policystatus = IdlePolicy;
												var PremiumUUID = Android.SavePremiums(jsonPremium, policyUUID, premiumUUID, FamilyUUID);
												Paydate = $('#txtPayDate').val();
												Android.UpdatePolicy(policyUUID, Paydate, policystatus);
												Android.UpdateInsureePolicy(policyUUID);
												window.open('PolicyPremium.html?p=' + policyUUID + '&l=' + LocationId + '&f=' + FamilyUUID + '&r=' + RegionId + '&d=' + DistrictId, "_self");
												$(this).dialog("close");
											}
										},
										{
											text: Android.getString("Suspend"),
											click: function () {
												policystatus = SuspendedPolicy;
												var PremiumUUID = Android.SavePremiums(jsonPremium, policyUUID, premiumUUID, FamilyUUID);
												Paydate = $('#txtPayDate').val();
												Android.UpdatePolicy(policyUUID, Paydate, policystatus);
												Android.UpdateInsureePolicy(policyUUID);
												window.open('PolicyPremium.html?p=' + policyUUID + '&l=' + LocationId + '&f=' + FamilyUUID + '&r=' + RegionId + '&d=' + DistrictId, "_self");
												$(this).dialog("close");
											}
										},
										{
											text: Android.getString("Enforce"),
											click: function () {
												policystatus = ActivePolicy;
												var PremiumUUID = Android.SavePremiums(jsonPremium, policyUUID, premiumUUID, FamilyUUID);
												Paydate = $('#txtPayDate').val();
												Android.UpdatePolicy(policyUUID, Paydate, policystatus);
												Android.UpdateInsureePolicy(policyUUID);
												window.open('PolicyPremium.html?p=' + policyUUID + '&l=' + LocationId + '&f=' + FamilyUUID + '&r=' + RegionId + '&d=' + DistrictId, "_self");
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
                            } else {
                                $("#msgAlert").text(Android.getString('PriceBelow'));
                                $("#dialog-confirm").dialog({
                                    resizable: false,
                                    height: "auto",
                                    width: 300,
                                    modal: true,
                                    buttons: [
                                    {
                                        text: Android.getString("Ok"),
                                        click: function () {
                                            policystatus = IdlePolicy;
                                            var PremiumUUID = Android.SavePremiums(jsonPremium, policyUUID, premiumUUID, FamilyUUID);
                                            Paydate = $('#txtPayDate').val();
                                            Android.UpdatePolicy(policyUUID, Paydate, policystatus);
                                            Android.UpdateInsureePolicy(policyUUID);
                                            window.open('PolicyPremium.html?p=' + policyUUID + '&l=' + LocationId + '&f=' + FamilyUUID + '&r=' + RegionId + '&d=' + DistrictId, "_self");
                                            $(this).dialog("close");
                                        }
                                    },
                                    {
                                        text: Android.getString("Enforce"),                                        
                                        click: function () {
                                            policystatus = ActivePolicy;
                                            var PremiumUUID = Android.SavePremiums(jsonPremium, policyUUID, premiumUUID, FamilyUUID);
                                            $('#btnSave').attr("disabled", "disabled");
                                            Paydate = $('#txtPayDate').val();
                                            Android.UpdatePolicy(policyUUID, Paydate, policystatus);
                                            Android.UpdateInsureePolicy(policyUUID);
                                            window.open('PolicyPremium.html?p=' + policyUUID + '&l=' + LocationId + '&f=' + FamilyUUID + '&r=' + RegionId + '&d=' + DistrictId, "_self");
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

                        } else {
                            if (PolicyBalance <= 0) {
                                policystatus = ActivePolicy;
                                Paydate = $('#txtPayDate').val();
                                Android.UpdatePolicy(policyUUID, Paydate, policystatus);
                                Android.UpdateInsureePolicy(policyUUID);
                                window.open('PolicyPremium.html?p=' + policyUUID + '&l=' + LocationId + '&f=' + FamilyUUID + '&r=' + RegionId + '&d=' + DistrictId, "_self");
                            } else if (PolicyBalance > 0) {
                                $("#msgAlert").text(Android.getString('PriceBelow'));
                                $("#dialog-confirm").dialog({
                                    resizable: false,
                                    height: "auto",
                                    width: 300,
                                    modal: true,
                                    buttons: [
                                    {
                                        text: Android.getString("Ok"),
                                        click: function () {
                                            policystatus = IdlePolicy;
                                            var PremiumUUID = Android.SavePremiums(jsonPremium, policyUUID, premiumUUID, FamilyUUID);
                                            Paydate = $('#txtPayDate').val();
                                            Android.UpdatePolicy(policyUUID, Paydate, policystatus);
                                            Android.UpdateInsureePolicy(policyUUID);
                                            window.open('PolicyPremium.html?p=' + policyUUID + '&l=' + LocationId + '&f=' + FamilyUUID + '&r=' + RegionId + '&d=' + DistrictId, "_self");
                                            $(this).dialog("close");
                                        }
                                    },
                                    {
                                        text: Android.getString("Enforce"),
                                        click: function () {
                                            policystatus = ActivePolicy;
                                            var PremiumUUID = Android.SavePremiums(jsonPremium, policyUUID, premiumUUID, FamilyUUID);
                                            $('#btnSave').attr("disabled", "disabled");
                                            Paydate = $('#txtPayDate').val();
                                            Android.UpdatePolicy(policyUUID, Paydate, policystatus);
                                            Android.UpdateInsureePolicy(policyUUID);
                                            window.open('PolicyPremium.html?p=' + policyUUID + '&l=' + LocationId + '&f=' + FamilyUUID + '&r=' + RegionId + '&d=' + DistrictId, "_self");
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
                            } else if (results != false) {
                                window.open('PolicyPremium.html?p=' + policyUUID + '&l=' + LocationId + '&f=' + FamilyUUID + '&r=' + RegionId + '&d=' + DistrictId, "_self");
                            }
                        }

                    } else if (PolicyBalance < 0) {
                        $("#msgAlert").text(Android.getString('ExceedsPolicy'));
                        $("#dialog-confirm").dialog({
                            resizable: false,
                            height: "auto",
                            width: 300,
                            modal: true,
                            buttons: [
                                {
                                    text: Android.getString("Ok"),
                                    click: function () {
                                        var PremiumUUID = Android.SavePremiums(jsonPremium, policyUUID, premiumUUID, FamilyUUID);
                                        policystatus = ActivePolicy;
                                        Paydate = $('#txtPayDate').val();
                                        Android.UpdatePolicy(policyUUID, Paydate, policystatus);
                                        Android.UpdateInsureePolicy(policyUUID);
                                        window.open('PolicyPremium.html?p=' + policyUUID + '&l=' + LocationId + '&f=' + FamilyUUID + '&r=' + RegionId + '&d=' + DistrictId, "_self");
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
                    } else if (results == true) {
                        var PremiumUUID = Android.SavePremiums(jsonPremium, policyUUID, premiumUUID, FamilyUUID);
                        policystatus = PolicyBalance > 0 ? IdlePolicy : ActivePolicy;
                        Paydate = $('#txtPayDate').val();
                        Android.UpdatePolicy(policyUUID, Paydate, policystatus);
                        Android.UpdateInsureePolicy(policyUUID);
                        window.open('PolicyPremium.html?p=' + policyUUID + '&l=' + LocationId + '&f=' + FamilyUUID + '&r=' + RegionId + '&d=' + DistrictId, "_self");
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

function getPolicyValue(policyUUID) {
    var strPolicy = Android.getPolicy(policyUUID);
    var $Policy = $.parseJSON(strPolicy);
    bindDataFromDatafield(strPolicy);
}
