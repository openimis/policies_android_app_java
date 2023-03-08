$(document).ready(function () {
    $('#btnSaveRarPassword').click(function () {
        try {
            var pass = $('#rarPassword').val();
            if (pass.length > 0) {
                var tmp = Android.SaveRarPassword(pass);
                Android.ShowDialog('Password has been changed');
                $('#rarPassword').val('');
            }
            else {
                Android.ShowDialog('Rar password required');
            }
        }
        catch (e) {
            Android.ShowDialog(e.message);
        }
    });

    $('#btnBackToDefault').click(function () {
        try {
            Android.BackToDefaultRarPassword();
            Android.ShowDialog('Password has been changed to the default rar password');
        }
        catch (e) {
            Android.ShowDialog(e.message);
        }
    });

    if (Android.isLoggingEnabled()) {
        $('#btnExportLogs').click(function () {
            Android.exportLogs()
        });

        $('#btnClearLogs').click(function () {
            Android.clearLogs()
        });
    } else {
        $('#logSection').hide();
    }
});
