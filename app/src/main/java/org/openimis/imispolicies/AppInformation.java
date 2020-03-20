package org.openimis.imispolicies;
import static org.openimis.imispolicies.BuildConfig.API_BASE_URL;
import static org.openimis.imispolicies.BuildConfig.SHOW_CONTROL_NUMBER_MENU;

 public class AppInformation {
     public static class DomainInfo {
          private static String _Domain = API_BASE_URL;

          public static String getDomain() {
              return _Domain;
         }

     }
     public static class MenuInfo {
         private static boolean _showControlNumberMenu = SHOW_CONTROL_NUMBER_MENU;

         public static boolean getShowControlNumberMenu() {
              return _showControlNumberMenu;
         }
     }
}
