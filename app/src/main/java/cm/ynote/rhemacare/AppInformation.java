package cm.ynote.rhemacare;
import static cm.ynote.rhemacare.BuildConfig.API_BASE_URL;
import static cm.ynote.rhemacare.BuildConfig.RAR_PASSWORD;
import static cm.ynote.rhemacare.BuildConfig.SHOW_CONTROL_NUMBER_MENU;

 public class AppInformation {
     public static class DomainInfo {

//         URL address of openIMIS demo server => please update with your own Web Services base URL/IP
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
         private static boolean _showControlNumberMenu = SHOW_CONTROL_NUMBER_MENU;

         public static boolean getShowControlNumberMenu() {
              return _showControlNumberMenu;
         }
     }
}
