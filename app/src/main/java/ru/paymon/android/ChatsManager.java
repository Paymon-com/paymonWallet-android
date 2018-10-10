package ru.paymon.android;

import ru.paymon.android.models.ChatsItem;

public class ChatsManager {
    private static volatile ChatsManager Instance = null;

    public static ChatsManager getInstance() {
        ChatsManager localInstance = Instance;
        if (localInstance == null) {
            synchronized (ChatsManager.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new ChatsManager();
                }
            }
        }
        return localInstance;
    }

    public void putChat(ChatsItem chatsItem){
        ApplicationLoader.db.chatDao().insert(chatsItem);
    }

    public void removeChat(ChatsItem chatsItem){
        ApplicationLoader.db.chatDao().delete(chatsItem);
    }
}
