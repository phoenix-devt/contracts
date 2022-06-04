package fr.lezoo.contracts.utils;

import java.util.Locale;

public class ContractsUtils {



    public static String ymlName(String str) {
        return str.toLowerCase().replace("_","-").replace(" ","-");
    }
    public static String enumName(String str) {
        return str.toUpperCase().replace("-","_");
    }
}
