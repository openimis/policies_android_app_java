package tz.co.exact.imis;

 public class AppInformation {
     public static class DomainInfo {

         // IP address of openIMIS demo server => please update with your own Web Services base URL/IP
         private static String _Domain = "http://132.148.151.32/";

         public static String getDomain(){
             return _Domain;
         }
     }
}
