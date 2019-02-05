package com.fess.loggertest;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Пробуем положить логи с разных потоков через цикл for
        Thread thread1 = new Thread(this::putLogs);
        Thread thread2 = new Thread(this::putLogs);
        Thread thread3 = new Thread(this::putLogs);
        Thread thread4 = new Thread(this::putLogs);

        thread1.start();
        thread2.start();
        thread3.start();
        thread4.start();
    }

    private void putLogs(){
        for (int i = 0; i < 180; i++){
            LoggerManager.put(Thread.currentThread().getName() , generateString());
        }
    }

    private static String generateString() {
        return UUID.randomUUID().toString();
    }
}
