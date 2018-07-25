package ru.paymon.android.net;

public class TimeHandler extends Thread implements Runnable {
    long lastKeepAliveTime;
    boolean keepAliveSent = false;
    boolean running = true;

    @Override
    public void run() {
        final NetworkManager networkManager = NetworkManager.getInstance();
        if(networkManager.connectorService == null) return;
        networkManager.connectorService.lastKeepAlive = System.currentTimeMillis() / 1000L;
        lastKeepAliveTime = System.currentTimeMillis() / 1000L;

        while (running) {
            long curtime = System.currentTimeMillis() / 1000L;

            if (networkManager.isConnected() && networkManager.connectorService != null) {
                if (curtime - lastKeepAliveTime >= 10 && !keepAliveSent) {
                    lastKeepAliveTime = System.currentTimeMillis() / 1000L;
                    networkManager.sendRequest(new RPC.PM_keepAlive(), (response, error) -> {
                        networkManager.connectorService.lastKeepAlive = System.currentTimeMillis() / 1000L;
                        keepAliveSent = false;
                    });
                    keepAliveSent = true;
                }
                if (networkManager.connectorService != null && (curtime - networkManager.connectorService.lastKeepAlive > 30)) {
                    networkManager.reconnect();
                    keepAliveSent = false;
                }
            }
            try {
                Thread.sleep(750);
            } catch (InterruptedException e) {

            }
        }
    }

    public void halt() {
        running = false;
    }
}
