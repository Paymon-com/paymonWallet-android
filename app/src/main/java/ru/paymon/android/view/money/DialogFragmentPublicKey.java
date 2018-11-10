package ru.paymon.android.view.money;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.ShareCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import ru.paymon.android.R;
import ru.paymon.android.WalletApplication;
import ru.paymon.android.utils.Qr;
import ru.paymon.android.utils.Utils;

import static ru.paymon.android.view.money.FragmentMoney.CURRENCY_KEY;
import static ru.paymon.android.view.money.bitcoin.FragmentBitcoinWallet.BTC_CURRENCY_VALUE;
import static ru.paymon.android.view.money.ethereum.FragmentEthereumWallet.ETH_CURRENCY_VALUE;
import static ru.paymon.android.view.money.pmnt.FragmentPaymonWallet.PMNT_CURRENCY_VALUE;

public class DialogFragmentPublicKey extends DialogFragment {
    private String currency;

    public DialogFragmentPublicKey setArgs(Bundle bundle) {
        this.setArguments(bundle);
        return this;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null) {
            if (!bundle.containsKey(CURRENCY_KEY)) return;
            currency = bundle.getString(CURRENCY_KEY);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_fragment_public_key, null);

        TextView publicKey = (TextView) view.findViewById(R.id.dialog_fragment_public_key_address);
        ConstraintLayout button = (ConstraintLayout) view.findViewById(R.id.dialog_fragment_public_key_button);
        ImageView qr = (ImageView) view.findViewById(R.id.dialog_fragment_public_key_image);

        WalletApplication application = ((WalletApplication) getActivity().getApplication());

        String publicKeyStr = "";
        switch (currency) {
            case ETH_CURRENCY_VALUE:
                publicKeyStr = application.getEthereumWallet().publicAddress;
                break;
            case BTC_CURRENCY_VALUE:
                publicKeyStr = application.getBitcoinPublicAddress();
                break;
            case PMNT_CURRENCY_VALUE:
                publicKeyStr = application.getPaymonWallet().publicAddress;
                break;
        }

        final String pubKey = publicKeyStr;
        publicKey.setText(pubKey);
        publicKey.setOnClickListener((view1) -> Utils.copyText(pubKey, getActivity()));
        Bitmap qrBmp = Qr.bitmap(pubKey);
        if (qrBmp != null) {
            int width = qr.getLayoutParams().width;
            int height = qr.getLayoutParams().height;
            Bitmap scaledQrBmp = Bitmap.createScaledBitmap(qrBmp, width, height, false);
            qr.setImageBitmap(scaledQrBmp);
        }

        button.setOnClickListener(view1 -> {
            final ShareCompat.IntentBuilder builder = ShareCompat.IntentBuilder.from(getActivity());
            builder.setType("text/plain");
            builder.setText(pubKey);
            builder.setChooserTitle(R.string.other_share);
            builder.startChooser();
        });

        return view;
    }
}