$(document).ready(function () {
    document.title = Android.getString('Login');
    var page = parseInt(queryString("s"));
    if (Android.isLoggedIn()) {
        sessionStorage.removeItem("UserData");
        sessionStorage.setItem("user", "out");
        Android.Logout();
        window.open("Home.html", "_self");
    }
    var officer_code = Android.getOfficerCode();
    $("#txtLoginName").val(officer_code);

    $('#spPleaseWait').text(Android.getString('PleaseWait'));
    $('#btnLogin').click(function () {
        var internetAvailable = Android.CheckInternetAvailable();
        if (!internetAvailable) {
            return;
        }
        var passed = isFormValidated();
        if (passed) {
            var Username = $('#txtLoginName').val();
            var Password = $('#txtPassword').val();
            sessionStorage.setItem("user", "in");
            $("#divProgress").show();
            setTimeout(function () {
                var LoggedIn = Android.LoginJI(Username, Password);
                if (LoggedIn > 0) {
                    SaveUserLoggedIn();
                    if (page == 0) window.open('Sync.html', '_self');
                    else if (page == 1) window.open('Search.html', '_self');
                    else if (page == 2) window.open('Enrollment.html', '_self');
                    else if (page == 4) {
                        window.open('Home.html', '_self');
                        Android.launchActivity("Reports");
                    }
                    else if (page == 5) {
                        window.open('Home.html', '_self');
                        Android.launchActivity("Enquire");
                    }
                    else window.open('Home.html', '_self');

                }
                else
                    Android.ShowDialog(Android.getString('LoginFail'));
                $("#divProgress").hide();
            }, 500)
        }
        else
            Android.ShowDialog(Android.getString('FieldRequired'));
    });

});
function SaveUserLoggedIn() {
    var jsonUser = getControlsValuesJSON('li');
    sessionStorage.setItem("UserData", jsonUser);
}
