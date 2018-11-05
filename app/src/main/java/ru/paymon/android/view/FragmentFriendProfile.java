package ru.paymon.android.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import ru.paymon.android.components.CircularImageView;

import androidx.navigation.Navigation;
import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.R;
import ru.paymon.android.UsersManager;
import ru.paymon.android.components.DialogProgress;
import ru.paymon.android.net.NetworkManager;
import ru.paymon.android.net.RPC;
import ru.paymon.android.utils.Utils;

import static ru.paymon.android.view.FragmentChat.CHAT_ID_KEY;

public class FragmentFriendProfile extends Fragment {
    private int userId;
    private ImageButton floatMenu;
    private ImageButton chatButton;
//    private ImageButton blockButton;
    private ImageButton showBitcoin;
    private ImageButton showEthereum;
    private DialogProgress dialogProgress;
    private boolean isFABOpen = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle bundle = getArguments();
        if (bundle != null)
            if (bundle.containsKey(CHAT_ID_KEY))
                userId = bundle.getInt(CHAT_ID_KEY);

        dialogProgress = new DialogProgress(getContext());
        dialogProgress.setCancelable(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend_profile, container, false);

        ImageView backToolbar = (ImageView) view.findViewById(R.id.toolbar_back_btn);

        backToolbar.setOnClickListener(view1 -> Navigation.findNavController(getActivity(), R.id.nav_host_fragment).popBackStack());

        chatButton = (ImageButton) view.findViewById(R.id.friend_profile_chat_button);
//        blockButton = (ImageButton) view.findViewById(R.id.friend_profile_block_button);
        showBitcoin = (ImageButton) view.findViewById(R.id.friend_profile_bitcoin_button);
        showEthereum = (ImageButton) view.findViewById(R.id.friend_profile_ethereum_button);

        showEthereum.setVisibility(View.GONE);
        showBitcoin.setVisibility(View.GONE);

        floatMenu = (ImageButton) view.findViewById(R.id.friend_profile_menu_button);
        TextView friendProfileCity = (TextView) view.findViewById(R.id.friend_profile_city_text_view);
        TextView friendProfileLogin = (TextView) view.findViewById(R.id.status_friend_profile_text_view);
        TextView friendProfileName = (TextView) view.findViewById(R.id.name_friend_profile_text_view);
        CircularImageView avatar = (CircularImageView) view.findViewById(R.id.friend_profile_avatar_image_view);

        floatMenu.setOnClickListener(view1 -> {
            if (!isFABOpen) {
                showFABMenu();
            } else {
                closeFABMenu();
            }
        });

        Utils.netQueue.postRunnable(() -> {
            ApplicationLoader.applicationHandler.post(dialogProgress::show);
            RPC.PM_getUserInfo userInfo = new RPC.PM_getUserInfo();
            userInfo.user_id = userId;

            final long requestID = NetworkManager.getInstance().sendRequest(userInfo, (response, error) -> {
                if (response != null) {
                    final RPC.PM_userFull user = (RPC.PM_userFull) response;
                    ApplicationLoader.applicationHandler.post(() -> {
                        if (dialogProgress != null && dialogProgress.isShowing())
                            dialogProgress.dismiss();

                        UsersManager.getInstance().putUser(user);

                        friendProfileName.setText(Utils.formatUserName(user));
                        friendProfileLogin.setText("@" + user.login);

                        if (user.email != null && !user.email.isEmpty())
                            friendProfileCity.setText(user.email);
                        else
                            friendProfileCity.setText(R.string.not_specified);

                        if (!user.photoURL.url.isEmpty())
                            Utils.loadPhoto(user.photoURL.url, avatar);

                        chatButton.setOnClickListener(view12 -> {
                            final Bundle bundle = new Bundle();
                            bundle.putInt(CHAT_ID_KEY, userId);
                            Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(R.id.fragmentChat, bundle);
                        });

//                        blockButton.setOnClickListener(view15 -> {
//                            //TODO:сделать реализацию кнопки для блокировки пользователя
//                        });
                    });
                }
            });
            ApplicationLoader.applicationHandler.post(() -> dialogProgress.setOnDismissListener((dialog) -> NetworkManager.getInstance().cancelRequest(requestID, false)));

            RPC.PM_BTC_getWalletKey getWalletKeyBtc = new RPC.PM_BTC_getWalletKey();
            getWalletKeyBtc.uid = userId;
            final long requestIDBtc = NetworkManager.getInstance().sendRequest(getWalletKeyBtc, (response, error) -> {
                if (response != null) {
                    final RPC.PM_BTC_setWalletKey walletKeyBtc = (RPC.PM_BTC_setWalletKey) response;
                    ApplicationLoader.applicationHandler.post(() -> {
                        if (walletKeyBtc.walletKey != null && !walletKeyBtc.walletKey.isEmpty()) {
                            showBitcoin.setVisibility(View.VISIBLE);
                            showBitcoin.setOnClickListener(view13 -> {
                                //TODO:сделать часть с переходом на его биток
                            });
                        }
                    });
                }
            });
            ApplicationLoader.applicationHandler.post(() -> dialogProgress.setOnDismissListener((dialog) -> NetworkManager.getInstance().cancelRequest(requestIDBtc, false)));

            RPC.PM_ETH_getWalletKey getWalletKeyEth = new RPC.PM_ETH_getWalletKey();
            getWalletKeyEth.uid = userId;
            final long requestIDEth = NetworkManager.getInstance().sendRequest(getWalletKeyEth, (response, error) -> {
                if (response != null) {
                    final RPC.PM_ETH_setWalletKey walletKeyEth = (RPC.PM_ETH_setWalletKey) response;
                    ApplicationLoader.applicationHandler.post(() -> {
                        if (walletKeyEth.walletKey != null && !walletKeyEth.walletKey.isEmpty()) {
                            showEthereum.setVisibility(View.VISIBLE);
                            showEthereum.setOnClickListener(view14 -> {
                                //TODO:сделать часть перехода на его эфир
                            });
                        }
                    });
                }
            });
            ApplicationLoader.applicationHandler.post(() -> dialogProgress.setOnDismissListener((dialog) -> NetworkManager.getInstance().cancelRequest(requestIDEth, false)));
        });

        return view;
    }

    private void showFABMenu() {
        isFABOpen = true;
        floatMenu.animate().rotation(180);
        chatButton.animate().translationY(getResources().getDimension(R.dimen.standard_55));
//        blockButton.animate().translationY(getResources().getDimension(R.dimen.standard_105));
        showBitcoin.animate().translationY(getResources().getDimension(R.dimen.standard_105));
        showEthereum.animate().translationY(getResources().getDimension(R.dimen.standard_155));
    }

    private void closeFABMenu() {
        isFABOpen = false;
        floatMenu.animate().rotation(0);
        chatButton.animate().translationY(0);
        showBitcoin.animate().translationY(0);
        showEthereum.animate().translationY(0);
//        blockButton.animate().translationY(0);
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.hideBottomBar(getActivity());
    }
}
