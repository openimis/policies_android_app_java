$(document).ready(function () {
    document.title = Android.getString('Search');
    $('#spPleaseWait').text(Android.getString('DownloadFamily'));

    if (!Android.isLoggedIn()) {
        window.open("Login.html?s=1", "_self");
    }

    $('#btnSearch').click(function () {
        var InsuranceNumber = $('#txtInsuranceNumber').val();
        if (InsuranceNumber.length > 0) {
            $("#divProgress").show();
            setTimeout(function () {
                if (Android.ModifyFamily(InsuranceNumber) == 1) {
                    window.open("Enrollment.html", "_self");
                }
                $("#divProgress").hide();
            }, 500)
        }
        else {
            Android.ShowDialog(Android.getString('InsuranceNumberRequired'));
        }
    });
});
