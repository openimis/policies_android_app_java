$(document).ready(function () {
    document.title = Android.getString('About');

    let appVersion = document.getElementById("VersionValue");
    var version = Android.getVersion();
    appVersion.textContent = version;
});
