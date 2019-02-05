package com.fess.loggertest;

import java.util.UUID;

public class LoggerUsageExample {

    public static void main(String[] args){
        for (int i = 0; i < 10000; i++){
            LoggerManager.put(generateString(), generateString());
        }
    }

    private static String generateString() {
        String uuid = UUID.randomUUID().toString();
        return "uuid = " + uuid;
    }

}
