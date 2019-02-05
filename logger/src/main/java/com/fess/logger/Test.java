package com.fess.logger;

import java.util.ArrayList;
import java.util.List;

public class Test {

    public static void main(String[] args){
//        StringBuffer s = new StringBuffer("[key,1 : value 1, key2 : value2]");
        List<Pair<String, String>> logs = new ArrayList<>();
        logs.add(new Pair<>("key 1", "value 1"));
        logs.add(new Pair<>("key2", "value2"));
        System.out.println(format(logs));
        logs.remove(0);
        System.out.println(format(logs));
    }

    private static String format(List<Pair<String, String>> logs){
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (Pair<String, String> pair : logs){
            sb.append(pair);
            sb.append(", ");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.deleteCharAt(sb.length() - 1);
        sb.append("]");
        return sb.toString();
    }
}
