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
                var Result = Android.ModifyFamily(InsuranceNumber);
                //alert(Result);
                if (Result == 1) {
                    //Android.ShowDialog(Android.getString('DataDownloadedSuccess'));
                    window.open("Enrollment.html", "_self");
                }
                else if (Result == 2) {
                    Android.ShowDialog(Android.getString('FamilyExists'));
                    //window.open("Enrollment.html", "_self");
                }
                else if (Result == 0) {
                    Android.ShowDialog(Android.getString('InsuranceNumberNotFound'));
                }
                else if (Result == 3) {
                    Android.ShowDialog(Android.getString('NoInternet'));
                }
                $("#divProgress").hide();
            }, 500)
        }
        else {
            Android.ShowDialog(Android.getString('InsuranceNumberRequired'));
        }
    });
});
