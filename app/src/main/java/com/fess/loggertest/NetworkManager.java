package com.fess.loggertest;

class NetworkManager {

    public interface OnRequestComplete{
        void onComplete(boolean successful);
    }

    private NetworkManager() {}

    static NetworkManager sharedInstance(){
        return NetworkManagerHolder.INSTANCE;
    }

    void SendLogRequest(String log, OnRequestComplete listener){
        if (parameterAreValid(log)) {
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (listener != null){
                    listener.onComplete(true);
                }
            }).start();
        }
    }

    boolean isConnected(){
        return true;
    }

    private boolean parameterAreValid(String log){
        if (log == null || log.isEmpty()){
            throw new RuntimeException("Nothing to send. Logs cannot be null or empty.");
        }
        return true;
    }

    private static class NetworkManagerHolder{
        private static final NetworkManager INSTANCE = new NetworkManager();
    }
}
