package ru.paymon.android.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import ru.paymon.android.NotificationManager;
import ru.paymon.android.net.NetworkManager;
import ru.paymon.android.utils.Utils;

public class MainViewModel extends ViewModel implements NotificationManager.IListener {
    private MutableLiveData<Boolean> isNetworkConnected = new MutableLiveData<>();
    private MutableLiveData<Boolean> isServerConnected = new MutableLiveData<>();
    private MutableLiveData<Boolean> isAuthorized = new MutableLiveData<>();

    public LiveData<Boolean> getNetworkConnectionState(){
        if(isNetworkConnected.getValue() == null)
            isNetworkConnected.postValue(NetworkManager.getInstance().networkState == NetworkManager.NetworkState.CONNECTED);
        return isNetworkConnected;
    }

    public LiveData<Boolean> getServerConnectionState(){
        return isServerConnected;
    }

    public LiveData<Boolean> getAuthorizationState(){
        return isAuthorized;
    }

    public MainViewModel() {
        super();
        NotificationManager.getInstance().addObserver(this, NotificationManager.NotificationEvent.userAuthorized);
        NotificationManager.getInstance().addObserver(this, NotificationManager.NotificationEvent.didEstablishedSecuredConnection);
        NotificationManager.getInstance().addObserver(this, NotificationManager.NotificationEvent.didDisconnectedFromTheServer);
        NotificationManager.getInstance().addObserver(this, NotificationManager.NotificationEvent.NETWORK_STATE_CONNECTED);
        NotificationManager.getInstance().addObserver(this, NotificationManager.NotificationEvent.NETWORK_STATE_DISCONNECTED);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        NotificationManager.getInstance().removeObserver(this, NotificationManager.NotificationEvent.userAuthorized);
        NotificationManager.getInstance().removeObserver(this, NotificationManager.NotificationEvent.didEstablishedSecuredConnection);
        NotificationManager.getInstance().removeObserver(this, NotificationManager.NotificationEvent.didDisconnectedFromTheServer);
        NotificationManager.getInstance().removeObserver(this, NotificationManager.NotificationEvent.NETWORK_STATE_CONNECTED);
        NotificationManager.getInstance().removeObserver(this, NotificationManager.NotificationEvent.NETWORK_STATE_DISCONNECTED);
    }

    @Override
    public void didReceivedNotification(NotificationManager.NotificationEvent event, Object... args) {
        Utils.netQueue.postRunnable(() -> {
            if (event == NotificationManager.NotificationEvent.userAuthorized) {
                isAuthorized.postValue(true);
            } else if (event == NotificationManager.NotificationEvent.didEstablishedSecuredConnection) {
                isServerConnected.postValue(true);
            } else if (event == NotificationManager.NotificationEvent.didDisconnectedFromTheServer) {
                isServerConnected.postValue(false);
            } else if (event == NotificationManager.NotificationEvent.NETWORK_STATE_DISCONNECTED) {
                isNetworkConnected.postValue(false);
            } else if (event == NotificationManager.NotificationEvent.NETWORK_STATE_CONNECTED) {
                isNetworkConnected.postValue(true);
            }
        });
    }
}
