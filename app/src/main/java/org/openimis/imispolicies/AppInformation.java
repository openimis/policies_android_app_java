package org.openimis.imispolicies;

 public class AppInformation {
     public static class DomainInfo {

//         URL address of openIMIS demo server => please update with your own Web Services base URL/IP
         private static String _Domain = "http://demo.openimis.org/";

         private static final String DEFAULT_RAR_PASSWORD = ")(#$1HsD";

         public static String getDomain(){
             return _Domain;
         }

         public static String getDefaultRarPassword() {
             return DEFAULT_RAR_PASSWORD;
         }
     }
}
