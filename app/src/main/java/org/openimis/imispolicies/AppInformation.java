package org.openimis.imispolicies;

/**
 * Created by Hiren on 3/16/2018.
 */

 public class AppInformation {
     public static class DomainInfo {
          private static String _Domain = "http://imis-mv.swisstph-mis.ch/";
         public static String getDomain(){
             return _Domain;
         }
     }
}
