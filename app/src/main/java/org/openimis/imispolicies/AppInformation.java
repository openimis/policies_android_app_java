package org.openimis.imispolicies;

import static org.openimis.imispolicies.BuildConfig.API_BASE_URL;
import static org.openimis.imispolicies.BuildConfig.RAR_PASSWORD;
import static org.openimis.imispolicies.BuildConfig.API_VERSION;
import static org.openimis.imispolicies.BuildConfig.SHOW_PAYMENT_MENU;
import static org.openimis.imispolicies.BuildConfig.SHOW_BULK_CN_MENU;

public class AppInformation {
    public static class DomainInfo {
        public static String getDomain() {
            return API_BASE_URL;
        }

        public static String getDefaultRarPassword() {
            return RAR_PASSWORD;
        }

        public static String getApiVersion() {
            return API_VERSION;
        }
    }

    public static class MenuInfo {
        public static boolean getShowPaymentNumberMenu() {
            return SHOW_PAYMENT_MENU;
        }

        public static boolean getShowBulkCNMenu() {
            return SHOW_BULK_CN_MENU;
        }
    }
}
