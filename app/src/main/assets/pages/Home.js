$(document).ready(function () {
    document.title = Android.getString('Home');

    var Families = Android.getTotalFamily();
    var Insuree = Android.getTotalInsuree();
    var Policy = Android.getTotalPolicy();
    var Premium = Android.getTotalPremium();
    var SumPremium = Android.getSumPremium();
    var Attachment = Android.getTotalAttachments();

    var FamiliesOnline = Android.getTotalFamilyOnline();
    var InsureeOnline = Android.getTotalInsureeOnline();
    var PolicyOnline = Android.getTotalPolicyOnline();
    var PremiumOnline = Android.getTotalPremiumOnline();


    $('#TotalFamilies').text(Families);
    $('#TotalInsuree').text(Insuree);
    $('#TotalPolicies').text(Policy);
    $('#TotalPremium').text(Premium);
    $('#PremiumAmount').text(SumPremium);
    $("#TotalAttachments").text(Attachment);
    $('#TotalFamiliesOnline').text(FamiliesOnline);
    $('#TotalInsureeOnline').text(InsureeOnline);
    //$('#TotalPoliciesOnline').text(PolicyOnline);
    //$('#TotalPremiumOnline').text(PremiumOnline);
});
