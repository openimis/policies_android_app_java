package org.openimis.imispolicies;

 public class AppInformation {
     public static class DomainInfo {

         // URL address of openIMIS demo server => please update with your own Web Services base URL/IP
         private static String _Domain = "http://demo.openimis.org/";

         public static String getDomain(){
             return _Domain;
         }
     }
}
