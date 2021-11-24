package org.openimis.imispolicies;

import java.text.SimpleDateFormat;
import java.util.Locale;

import static org.openimis.imispolicies.BuildConfig.API_BASE_URL;
import static org.openimis.imispolicies.BuildConfig.RAR_PASSWORD;
import static org.openimis.imispolicies.BuildConfig.API_VERSION;
import static org.openimis.imispolicies.BuildConfig.SHOW_PAYMENT_MENU;
import static org.openimis.imispolicies.BuildConfig.SHOW_BULK_CN_MENU;

public final class AppInformation {
    public final static class DomainInfo {
        public static String getDomain() {
            return API_BASE_URL;
        }

        public static String getDefaultRarPassword() {
            return RAR_PASSWORD;
        }

        public static String getApiVersion() {
            return API_VERSION;
        }

        private DomainInfo() {
        }
    }

    public final static class MenuInfo {
        public static boolean getShowPaymentNumberMenu() {
            return SHOW_PAYMENT_MENU;
        }

        public static boolean getShowBulkCNMenu() {
            return SHOW_BULK_CN_MENU;
        }

        private MenuInfo() {
        }
    }

    public final static class DateTimeInfo {
        public static String getDateFormat() {
            return "yyyy-MM-dd";
        }

        public static String getISODatetimeFormat() {
            return "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSXXX";
        }

        public static String getFileDatetimeFormat() {
            return "yyyy-MM-dd'T'HH-mm-ss";
        }

        public static SimpleDateFormat getDefaultDateFormatter() {
            return new SimpleDateFormat(getDateFormat(), Locale.US);
        }

        public static SimpleDateFormat getDefaultIsoDatetimeFormatter() {
            return new SimpleDateFormat(getISODatetimeFormat(), Locale.US);
        }

        public static SimpleDateFormat getDefaultFileDatetimeFormatter() {
            return new SimpleDateFormat(getFileDatetimeFormat(), Locale.US);
        }

        private DateTimeInfo() {
        }
    }

    private AppInformation() {
    }
}
