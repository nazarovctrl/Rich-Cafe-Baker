package com.example.utill;

public class IdCheckUtil {
    public static boolean check(String text){
        try {
            Integer.valueOf(text);
        }catch (RuntimeException e){
            return false;
        }
        return true;
    }
}
