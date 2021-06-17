package org.openimis.imispolicies;
import static org.openimis.imispolicies.BuildConfig.API_BASE_URL;
import static org.openimis.imispolicies.BuildConfig.RAR_PASSWORD;
import static org.openimis.imispolicies.BuildConfig.SHOW_PAYMENT_MENU;

 public class AppInformation {
     public static class DomainInfo {
         private static String _Domain = API_BASE_URL;

         private static final String DEFAULT_RAR_PASSWORD = RAR_PASSWORD;

         public static String getDomain(){
             return _Domain;
         }

         public static String getDefaultRarPassword() {
             return DEFAULT_RAR_PASSWORD;
         }
     }
	 public static class MenuInfo {
         private static boolean _showPaymentMenu = SHOW_PAYMENT_MENU;

         public static boolean getShowPaymentNumberMenu() {
              return _showPaymentMenu;
         }
     }
}
