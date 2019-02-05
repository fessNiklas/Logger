package com.fess.loggertest;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public final class LoggerManager {

    private final static String TAG = LoggerManager.class.getSimpleName();

    private final static int SEND_LIMIT_KB = 100;
    private final static int REWRITE_LIMIT_KB = 300;

    private static AtomicInteger sCurrentLogSizeKb = new AtomicInteger(0);

    private static StringBuffer sStringLogs = new StringBuffer();

    private volatile static boolean sendingLogsInProgress = false;

    /**
     * Основная коллекция с логами
     * Выбрал CopyOnWriteArrayList потому что:
     * - Будем часто итерироваться по списку
     * - Вставок в середину не предусмотрено
     * - Потокобезопасный (можно спамить с разных потоков)
     * - Можно модифицировать список, когда другой поток итерируется по ней. В общем нет ConcurrentModificationException
     */
    private final static List<Pair<String, String>> sLogs = new CopyOnWriteArrayList<>();
    /**
     * Буффер логов.
     * Если данные отправляются, пишем логи в буффер. После отправки перекидываем логи с буффера в
     * основную коллекцию.
     */
    private final static List<Pair<String, String>> sBufferLogs = new CopyOnWriteArrayList<>();

    private LoggerManager() {
    }

    static void put(String key, String value) {
        Log.d(TAG, key + "put value: " + value);
        validateParameters(key, value);
        //String.format не использовал, по причине его дороговизны/медлительности
        Pair<String, String> pair = new Pair<>(key, value);
        sCurrentLogSizeKb.set(getKilobytesFromList(sLogs));
        if (sendingLogsInProgress) {
            sBufferLogs.add(pair);
        } else {
            sLogs.add(pair);
        }
        checkLogSize();
    }

    private static void checkLogSize() {
        Log.d(TAG, Thread.currentThread() + " checkLogSize. O size: " + sCurrentLogSizeKb.get());
        Log.d(TAG, Thread.currentThread() + " checkLogSize. B size: " + getKilobytesFromList(sBufferLogs));
        while (sCurrentLogSizeKb.get() >= REWRITE_LIMIT_KB) {
            trimSize();
        }
        if (NetworkManager.sharedInstance().isConnected() && !sendingLogsInProgress) {
            if (sCurrentLogSizeKb.get() >= SEND_LIMIT_KB && sCurrentLogSizeKb.get() < REWRITE_LIMIT_KB) {
                sendingLogsInProgress = true;
                send();
            }
        }
    }

    private static void send() {
        Log.d(TAG, Thread.currentThread() + "Send " + getLogs());
        NetworkManager.sharedInstance().SendLogRequest(logsAsString(), successful -> {
            Log.d(TAG, "All data is sending.");
            resetFields();
            sendingLogsInProgress = false;
            checkLogSize();
        });
    }

    private static void resetFields() {
        Log.d(TAG, "resetFields");
        sStringLogs.setLength(0);
        sLogs.clear();
        sLogs.addAll(sBufferLogs);
        sBufferLogs.clear();
        sCurrentLogSizeKb.set(getKilobytesFromList(sLogs));
    }

    private static void trimSize() {
        while (sCurrentLogSizeKb.get() >= REWRITE_LIMIT_KB) {
            sLogs.remove(0);
            sCurrentLogSizeKb.set(getKilobytesFromList(sLogs));
        }
        Log.d(TAG, "trimSize. size: " + sCurrentLogSizeKb.get());
    }

    private static String logsAsString() {
        sStringLogs.append("[");
        int listSize = sLogs.size();
        for (int i = 0; i < listSize; i++) {
            sStringLogs.append(sLogs.get(i));
            if (i != listSize - 1) {
                sStringLogs.append(", ");
            }
        }
        sStringLogs.append("]");
        return sStringLogs.toString();
    }

    private static void validateParameters(String key, String value) {
        if (key == null || key.isEmpty()) {
            throw new RuntimeException("Key cannot be null or empty.");
        }
        if (value == null || value.isEmpty()) {
            throw new RuntimeException("Value cannot be null or empty.");
        }
    }

    static String getLogs() {
        return logsAsString();
    }

    private static int getKilobytesFromList(List list) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out;
        try {
            out = new ObjectOutputStream(baos);
            out.writeObject(list);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return baos.toByteArray().length / 1024;
    }
}