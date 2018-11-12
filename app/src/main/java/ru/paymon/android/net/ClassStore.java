package ru.paymon.android.net;

import android.util.Log;
import android.util.SparseArray;

import ru.paymon.android.Config;
import ru.paymon.android.utils.SerializedBuffer;


public class ClassStore {
    private SparseArray<Class> classStore;

    private ClassStore() {
        classStore = new SparseArray<>();

        classStore.put(RPC.PM_DHParams.svuid, RPC.PM_DHParams.class);
        classStore.put(RPC.PM_requestDHParams.svuid, RPC.PM_requestDHParams.class);
        classStore.put(RPC.PM_serverDHdata.svuid, RPC.PM_serverDHdata.class);
        classStore.put(RPC.PM_clientDHdata.svuid, RPC.PM_clientDHdata.class);
        classStore.put(RPC.PM_DHresult.svuid, RPC.PM_DHresult.class);
        classStore.put(RPC.PM_postConnectionData.svuid, RPC.PM_postConnectionData.class);
        classStore.put(RPC.PM_auth.svuid, RPC.PM_auth.class);
        classStore.put(RPC.PM_authToken.svuid, RPC.PM_authToken.class);
        classStore.put(RPC.PM_keepAlive.svuid, RPC.PM_keepAlive.class);
        classStore.put(RPC.PM_message.svuid, RPC.PM_message.class);
        classStore.put(RPC.PM_messageItem.svuid, RPC.PM_messageItem.class);
        classStore.put(RPC.PM_error.svuid, RPC.PM_error.class);
        classStore.put(RPC.PM_user.svuid, RPC.PM_user.class);
        classStore.put(RPC.PM_userFull.svuid, RPC.PM_userFull.class);
        classStore.put(RPC.PM_userSelf.svuid, RPC.PM_userSelf.class);
        classStore.put(RPC.Group.svuid, RPC.Group.class);
        classStore.put(RPC.PM_chat_messages.svuid, RPC.PM_chat_messages.class);
        classStore.put(RPC.PM_chatsAndMessages.svuid, RPC.PM_chatsAndMessages.class);
        classStore.put(RPC.PM_register.svuid, RPC.PM_register.class);
        classStore.put(RPC.PM_searchContact.svuid, RPC.PM_searchContact.class);
        classStore.put(RPC.PM_users.svuid, RPC.PM_users.class);
        classStore.put(RPC.PM_addFriend.svuid, RPC.PM_addFriend.class);
        classStore.put(RPC.PM_updateMessageID.svuid, RPC.PM_updateMessageID.class);
        classStore.put(RPC.PM_photoURL.svuid, RPC.PM_photoURL.class);
        classStore.put(RPC.PM_requestPhoto.svuid, RPC.PM_requestPhoto.class);
        classStore.put(RPC.PM_photo.svuid, RPC.PM_photo.class);
        classStore.put(RPC.PM_file.svuid, RPC.PM_file.class);
        classStore.put(RPC.PM_filePart.svuid, RPC.PM_filePart.class);
        classStore.put(RPC.PM_boolTrue.svuid, RPC.PM_boolTrue.class);
        classStore.put(RPC.PM_boolFalse.svuid, RPC.PM_boolFalse.class);
        classStore.put(RPC.PM_getChatMessages.svuid, RPC.PM_getChatMessages.class);
        classStore.put(RPC.PM_getStickerPack.svuid, RPC.PM_getStickerPack.class);
        classStore.put(RPC.PM_stickerPack.svuid, RPC.PM_stickerPack.class);
        classStore.put(RPC.PM_sticker.svuid, RPC.PM_sticker.class);
        classStore.put(RPC.PM_BTC_getWalletKey.svuid, RPC.PM_BTC_getWalletKey.class);
        classStore.put(RPC.PM_BTC_setWalletKey.svuid, RPC.PM_BTC_setWalletKey.class);
        classStore.put(RPC.PM_resendEmail.svuid, RPC.PM_resendEmail.class);
        classStore.put(RPC.PM_createGroup.svuid, RPC.PM_createGroup.class);
        classStore.put(RPC.PM_group_addParticipants.svuid, RPC.PM_group_addParticipants.class);
        classStore.put(RPC.PM_group_removeParticipant.svuid, RPC.PM_group_removeParticipant.class);
        classStore.put(RPC.PM_group_setSettings.svuid, RPC.PM_group_setSettings.class);
        classStore.put(RPC.PM_group_setPhoto.svuid, RPC.PM_group_setPhoto.class);
        classStore.put(RPC.PM_ETH_getWalletKey.svuid, RPC.PM_ETH_getWalletKey.class);
        classStore.put(RPC.PM_ETH_setWalletKey.svuid, RPC.PM_ETH_setWalletKey.class);
        classStore.put(RPC.PM_ETH_balanceInfo.svuid, RPC.PM_ETH_balanceInfo.class);
        classStore.put(RPC.PM_ETH_createWallet.svuid, RPC.PM_ETH_createWallet.class);
        classStore.put(RPC.PM_ETH_fiatInfo.svuid, RPC.PM_ETH_fiatInfo.class);
        classStore.put(RPC.PM_ETH_getBalance.svuid, RPC.PM_ETH_getBalance.class);
        classStore.put(RPC.PM_ETH_getPublicFromPrivate.svuid, RPC.PM_ETH_getPublicFromPrivate.class);
        classStore.put(RPC.PM_ETH_getTxInfo.svuid, RPC.PM_ETH_getTxInfo.class);
        classStore.put(RPC.PM_ETH_publicFromPrivateInfo.svuid, RPC.PM_ETH_publicFromPrivateInfo.class);
        classStore.put(RPC.PM_ETH_send.svuid, RPC.PM_ETH_send.class);
        classStore.put(RPC.PM_ETH_sendInfo.svuid, RPC.PM_ETH_sendInfo.class);
        classStore.put(RPC.PM_ETH_toFiat.svuid, RPC.PM_ETH_toFiat.class);
        classStore.put(RPC.PM_ETH_txInfo.svuid, RPC.PM_ETH_txInfo.class);
        classStore.put(RPC.PM_ETH_walletInfo.svuid, RPC.PM_ETH_walletInfo.class);
        classStore.put(RPC.PM_postReferal.svuid, RPC.PM_postReferal.class);
        classStore.put(RPC.PM_deleteChat.svuid, RPC.PM_deleteChat.class);
        classStore.put(RPC.PM_leaveChat.svuid, RPC.PM_leaveChat.class);
        classStore.put(RPC.PM_deleteChatMessages.svuid, RPC.PM_deleteChatMessages.class);
        classStore.put(RPC.PM_deleteDialogMessages.svuid, RPC.PM_deleteDialogMessages.class);
        classStore.put(RPC.PM_deleteGroupMessages.svuid, RPC.PM_deleteGroupMessages.class);
        classStore.put(RPC.PM_getUserInfo.svuid, RPC.PM_getUserInfo.class);
        classStore.put(RPC.PM_getInvitedUsersCount.svuid, RPC.PM_getInvitedUsersCount.class);
        classStore.put(RPC.PM_invitedUsersCount.svuid, RPC.PM_invitedUsersCount.class);
        classStore.put(RPC.PM_restorePassword.svuid, RPC.PM_restorePassword.class);
        classStore.put(RPC.PM_restorePasswordRequestCode.svuid, RPC.PM_restorePasswordRequestCode.class);
        classStore.put(RPC.PM_getPhotosURL.svuid, RPC.PM_getPhotosURL.class);
        classStore.put(RPC.PM_photosURL.svuid, RPC.PM_photosURL.class);
        classStore.put(RPC.PM_deleteProfilePhoto.svuid, RPC.PM_deleteProfilePhoto.class);
    }

    private static ClassStore store = null;

    public static ClassStore Instance() {
        if (store == null) {
            store = new ClassStore();
        }
        return store;
    }

    public Packet TLdeserialize(SerializedBuffer stream, int constructor, boolean exception) {
        Class objClass = classStore.get(constructor);
        if (objClass != null) {
            Packet response;
            try {
                response = (Packet) objClass.newInstance();
            } catch (Throwable e) {
                Log.e(Config.TAG, e.getMessage());
                return null;
            }
            response.readParams(stream, exception);
            return response;
        }
        return null;
    }
}