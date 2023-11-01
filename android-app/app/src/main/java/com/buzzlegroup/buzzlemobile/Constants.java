package com.buzzlegroup.buzzlemobile;

/**
 * Class for holding global static constants that are relevant in multiple parts of the app
 * @author Thomas Tran
 */
public class Constants {

    /*
      In Android, localhost is the address of the device or the emulator.
      To connect to your machine, you need to use the below IP address
     */

    //AWS VERSION
    public static final String host = "54.193.74.176"; //NOTE: this changes every reboot
    public static final String port = "8443";
    public static final String domain = "buzzle-project";
    public static final String baseURL = "https://" + host + ":" + port + "/" + domain;


    //LOCAL VERSION
//    public static final String host = "10.0.2.2";
//    public static final String port = "8080";
//    public static final String domain = "buzzle_project_war";
//    public static final String baseURL = "http://" + host + ":" + port + "/" + domain;
}
