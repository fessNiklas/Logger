package com.fess.logger;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class LoggerManager {

    private final static String TAG = LoggerManager.class.getSimpleName();

    private final static int SEND_LIMIT_KB = 100;
    private final static int REWRITE_LIMIT_KB = 300;

    private static int sCurrentLogSizeKb;

//    private static StringBuffer sBuffer = new StringBuffer("[]");
//    private static boolean bufferIsClear = true;
    private static ThreadLocal<Pair<String, String>> sLastLog = new ThreadLocal<>();
    /**
     * Выбрал CopyOnWriteArrayList потому что:
     * - Будем часто итерироваться по списку
     * - Вставок в середину не предусмотрено
     * - Потокобезопасный (можно спамить с разных потоков)
     * - Можно модифицировать список, когда другой поток итерируется по ней. В общем нет ConcurrentModificationException
     */
    private static List<Pair<String, String>> sLogs = new CopyOnWriteArrayList<>();

    private LoggerManager() {
    }

    public static void put(String key, String value) {
        validateParameters(key, value);
        //String.format не использовал, по причине его дороговизны/медлительности
        Pair<String, String> pair = new Pair<>(key, value);
        sLastLog.set(pair);
//        if (bufferIsClear){
//            sBuffer.insert(1, ", ");
//            bufferIsClear = false;
//        }
//        sBuffer.insert(sBuffer.length() - 2, pair);
        sLogs.add(sLastLog.get());
        calculateSize();
    }

    private static void send() {
        if (NetworkManager.sharedInstance().isConnected()) {
            NetworkManager.sharedInstance().SendLogRequest(logsAsString());
            // if resetFields successful callback ->
            resetFields();
        }
    }

    private static void resetFields(){
//        sBuffer = new StringBuffer("[]");
//        bufferIsClear = true;
        sLogs = new CopyOnWriteArrayList<>();
        sLastLog = new ThreadLocal<>();
    }

    private static void calculateSize() {
        sCurrentLogSizeKb += logsAsString().getBytes(/*"UTF-8"*/).length / 1024;
        while (sCurrentLogSizeKb >= REWRITE_LIMIT_KB){
            trimSize();
        }
        if (sCurrentLogSizeKb >= SEND_LIMIT_KB && sCurrentLogSizeKb < REWRITE_LIMIT_KB) {
            send();
        }
    }

    private static void trimSize() {
        while (sCurrentLogSizeKb > REWRITE_LIMIT_KB) {
            sLogs.remove(0);
            calculateSize();
        }
    }

    private static String logsAsString(){
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (Pair<String, String> pair : sLogs){
            sb.append(pair);
            sb.append(", ");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.deleteCharAt(sb.length() - 1);
        sb.append("]");
        return sb.toString();
    }

    private static void validateParameters(String key, String value) {
        if (key == null || key.isEmpty()) {
            throw new RuntimeException("Key cannot be null or empty.");
        }
        if (value == null || value.isEmpty()) {
            throw new RuntimeException("Value cannot be null or empty.");
        }
    }
}
