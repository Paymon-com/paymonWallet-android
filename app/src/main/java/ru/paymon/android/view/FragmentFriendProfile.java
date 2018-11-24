package ru.paymon.android.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.bitcoinj.wallet.Wallet;

import androidx.navigation.Navigation;
import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.R;
import ru.paymon.android.User;
import ru.paymon.android.UsersManager;
import ru.paymon.android.WalletApplication;
import ru.paymon.android.components.CircularImageView;
import ru.paymon.android.components.DialogProgress;
import ru.paymon.android.models.EthereumWallet;
import ru.paymon.android.models.PaymonWallet;
import ru.paymon.android.net.NetworkManager;
import ru.paymon.android.net.RPC;
import ru.paymon.android.utils.Utils;

import static ru.paymon.android.view.FragmentChat.CHAT_ID_KEY;

public class FragmentFriendProfile extends Fragment {
    private int userId;
    private ImageButton floatMenu;
    private ImageButton chatButton;
    //    private ImageButton blockButton;
    private ImageButton btcButton;
    private ImageButton ethButton;
    private ImageButton pmntButton;
    private DialogProgress dialogProgress;
    private boolean isFABOpen = false;
    private RPC.PM_userFull user;
    private WalletApplication application;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle bundle = getArguments();
        if (bundle != null)
            if (bundle.containsKey(CHAT_ID_KEY))
                userId = bundle.getInt(CHAT_ID_KEY);

        dialogProgress = new DialogProgress(getContext());
        dialogProgress.setCancelable(true);
        application = (WalletApplication) getActivity().getApplication();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend_profile, container, false);

        ImageView backToolbar = (ImageView) view.findViewById(R.id.toolbar_back_btn);

        backToolbar.setOnClickListener(view1 -> Navigation.findNavController(getActivity(), R.id.nav_host_fragment).popBackStack());

        chatButton = (ImageButton) view.findViewById(R.id.friend_profile_chat_button);
//        blockButton = (ImageButton) view.findViewById(R.id.friend_profile_block_button);
        btcButton = (ImageButton) view.findViewById(R.id.friend_profile_bitcoin_button);
        ethButton = (ImageButton) view.findViewById(R.id.friend_profile_ethereum_button);
        pmntButton = (ImageButton) view.findViewById(R.id.friend_profile_pmnt_button);

        floatMenu = (ImageButton) view.findViewById(R.id.friend_profile_menu_button);
        TextView friendProfileCity = (TextView) view.findViewById(R.id.friend_profile_city_text_view);
        TextView friendProfileLogin = (TextView) view.findViewById(R.id.status_friend_profile_text_view);
        TextView friendProfileName = (TextView) view.findViewById(R.id.name_friend_profile_text_view);
        CircularImageView avatar = (CircularImageView) view.findViewById(R.id.friend_profile_avatar_image_view);


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
                        this.user = user;

                        friendProfileName.setText(Utils.formatUserName(user));
                        friendProfileLogin.setText("@" + user.login);

                        if (user.email != null && !user.email.isEmpty())
                            friendProfileCity.setText(user.email);
                        else
                            friendProfileCity.setText(R.string.user_profile_not_specified);

                        if (!user.photoURL.url.isEmpty())
                            Utils.loadPhoto(user.photoURL.url, avatar);

                        chatButton.setOnClickListener(v -> {
                            final Bundle bundle = new Bundle();
                            bundle.putInt(CHAT_ID_KEY, userId);
                            Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(R.id.fragmentChat, bundle);
                        });
                        Log.e("AAA", user.btcAddress + " QQ " + user.ethAddress + " QQ " + user.pmntAddress);
                        btcButton.setOnClickListener(v -> {
                            if (User.CLIENT_MONEY_BITCOIN_WALLET_PASSWORD == null) {
                                Toast.makeText(ApplicationLoader.applicationContext, "Необходим BTC кошелек!", Toast.LENGTH_LONG).show();
                                return;
                            }

                            final Wallet bitcoinWallet = application.getBitcoinWallet();

                            if (bitcoinWallet != null) {
                                final Bundle bundle = new Bundle();
                                bundle.putString("address", user.btcAddress);
                                Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(R.id.fragmentBitcoinWalletTransfer, bundle);
                            } else {
                                Toast.makeText(ApplicationLoader.applicationContext, "Необходим BTC кошелек!", Toast.LENGTH_LONG).show();
                            }
                        });

                        ethButton.setOnClickListener(v -> {
                            if (User.CLIENT_MONEY_ETHEREUM_WALLET_PASSWORD == null) {
                                Toast.makeText(ApplicationLoader.applicationContext, "Необходим ETH кошелек!", Toast.LENGTH_LONG).show();
                                return;
                            }

                            final EthereumWallet ethereumWallet = application.getEthereumWallet();

                            if (ethereumWallet != null) {
                                final Bundle bundle = new Bundle();
                                bundle.putString("address", user.ethAddress);
                                Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(R.id.fragmentEthereumWalletTransfer, bundle);
                            } else {
                                Toast.makeText(ApplicationLoader.applicationContext, "Необходим ETH кошелек!", Toast.LENGTH_LONG).show();
                            }
                        });

                        pmntButton.setOnClickListener(v -> {
                            if (User.CLIENT_MONEY_PAYMON_WALLET_PASSWORD == null) {
                                Toast.makeText(ApplicationLoader.applicationContext, "Необходим PMNT кошелек!", Toast.LENGTH_LONG).show();
                                return;
                            }

                            final PaymonWallet paymonWallet = application.getPaymonWallet();

                            if (paymonWallet != null) {
                                final Bundle bundle = new Bundle();
                                bundle.putString("address", user.pmntAddress);
                                Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(R.id.fragmentPaymonWalletTransfer, bundle);
                            } else {
                                Toast.makeText(ApplicationLoader.applicationContext, "Необходим PMNT кошелек!", Toast.LENGTH_LONG).show();
                            }
                        });

                        floatMenu.setOnClickListener(view1 -> showFABMenu(!isFABOpen));

//                        blockButton.setOnClickListener(view15 -> {
//                            //TODO:сделать реализацию кнопки для блокировки пользователя
//                        });
                    });
                }
            });

            ApplicationLoader.applicationHandler.post(() -> dialogProgress.setOnDismissListener((dialog) -> NetworkManager.getInstance().cancelRequest(requestID, false)));
        });

        return view;
    }

    private void showFABMenu(boolean flag) {
        isFABOpen = flag;
        if (flag) {
            btcButton.setVisibility(!user.btcAddress.isEmpty() ? View.VISIBLE : View.GONE);
            ethButton.setVisibility(!user.ethAddress.isEmpty() ? View.VISIBLE : View.GONE);
            pmntButton.setVisibility(!user.pmntAddress.isEmpty() ? View.VISIBLE : View.GONE);
            floatMenu.animate().rotation(180);
            float dp55 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 55f, getResources().getDisplayMetrics());
            float currentDP = dp55;
            chatButton.animate().translationY(currentDP);
            if (!user.btcAddress.isEmpty()) {
                currentDP += dp55;
                btcButton.animate().translationY(currentDP);
            }
            if (!user.ethAddress.isEmpty()) {
                currentDP += dp55;
                ethButton.animate().translationY(currentDP);
            }
            if (!user.pmntAddress.isEmpty()) {
                currentDP += dp55;
                pmntButton.animate().translationY(currentDP);
            }
//        blockButton.animate().translationY(getResources().getDimension(R.dimen.standard_220));
        } else {
            floatMenu.animate().rotation(0);
            chatButton.animate().translationY(0);
            btcButton.animate().translationY(0);
            ethButton.animate().translationY(0);
            pmntButton.animate().translationY(0);
            //        blockButton.animate().translationY(0);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.hideBottomBar(getActivity());
    }
}
