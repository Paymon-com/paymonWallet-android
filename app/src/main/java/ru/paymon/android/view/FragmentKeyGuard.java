package ru.paymon.android.view;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.nightonke.blurlockview.BlurLockView;
import com.nightonke.blurlockview.Directions.HideType;
import com.nightonke.blurlockview.Directions.ShowType;
import com.nightonke.blurlockview.Eases.EaseType;
import com.nightonke.blurlockview.Password;

import ru.paymon.android.R;
import ru.paymon.android.User;

public class FragmentKeyGuard extends Fragment implements View.OnClickListener, BlurLockView.OnPasswordInputListener {
    private BlurLockView blurLockView;

    public static synchronized FragmentKeyGuard newInstance() {
        return new FragmentKeyGuard();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_key_guard, container, false);

        blurLockView = (BlurLockView) view.findViewById(R.id.blurlockview);

        if (User.CLIENT_SECURITY_PASSWORD_VALUE != null) {
            blurLockView.setTitle("Введите пароль");
            blurLockView.setTypeface(Typeface.DEFAULT);
            blurLockView.setType(Password.NUMBER, false);
            blurLockView.setLeftButton("Очистить");
            blurLockView.setRightButton("Подсказка");//TODO:
            blurLockView.setOnRightButtonClickListener(() -> blurLockView.showHint());
        }else{
            blurLockView.setTitle("Введите желаемый пароль");
            blurLockView.setPasswordLength(10);
            blurLockView.setTypeface(Typeface.DEFAULT);
            blurLockView.setType(Password.NUMBER, false);
            blurLockView.setRightButton("OK");
            blurLockView.setLeftButton("Очистить");
        }

        blurLockView.setOnPasswordInputListener(this);

        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.keyGuardImage:
                blurLockView.show(
                        getActivity().getIntent().getIntExtra("SHOW_DURATION", 300),
                        getShowType(getActivity().getIntent().getIntExtra("SHOW_DIRECTION", 0)),
                        getEaseType(getActivity().getIntent().getIntExtra("SHOW_EASE_TYPE", 30)));
                break;
        }
    }


    @Override
    public void correct(String inputPassword) {
        blurLockView.hide(
                getActivity().getIntent().getIntExtra("HIDE_DURATION", 300),
                getHideType(getActivity().getIntent().getIntExtra("HIDE_DIRECTION", 0)),
                getEaseType(getActivity().getIntent().getIntExtra("HIDE_EASE_TYPE", 30)));

        getActivity().finish();
    }

    @Override
    public void incorrect(String inputPassword) {
        Toast.makeText(getActivity(), "Пароль не верный!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void input(String inputPassword) {
    }

    private ShowType getShowType(int p) {
        ShowType showType = ShowType.FROM_TOP_TO_BOTTOM;
        switch (p) {
            case 0:
                showType = ShowType.FROM_TOP_TO_BOTTOM;
                break;
            case 1:
                showType = ShowType.FROM_RIGHT_TO_LEFT;
                break;
            case 2:
                showType = ShowType.FROM_BOTTOM_TO_TOP;
                break;
            case 3:
                showType = ShowType.FROM_LEFT_TO_RIGHT;
                break;
            case 4:
                showType = ShowType.FADE_IN;
                break;
        }
        return showType;
    }

    private HideType getHideType(int p) {
        HideType hideType = HideType.FROM_TOP_TO_BOTTOM;
        switch (p) {
            case 0:
                hideType = HideType.FROM_TOP_TO_BOTTOM;
                break;
            case 1:
                hideType = HideType.FROM_RIGHT_TO_LEFT;
                break;
            case 2:
                hideType = HideType.FROM_BOTTOM_TO_TOP;
                break;
            case 3:
                hideType = HideType.FROM_LEFT_TO_RIGHT;
                break;
            case 4:
                hideType = HideType.FADE_OUT;
                break;
        }
        return hideType;
    }

    private EaseType getEaseType(int p) {
        EaseType easeType = EaseType.Linear;
        switch (p) {
            case 0:
                easeType = EaseType.EaseInSine;
                break;
            case 1:
                easeType = EaseType.EaseOutSine;
                break;
            case 2:
                easeType = EaseType.EaseInOutSine;
                break;
            case 3:
                easeType = EaseType.EaseInQuad;
                break;
            case 4:
                easeType = EaseType.EaseOutQuad;
                break;
            case 5:
                easeType = EaseType.EaseInOutQuad;
                break;
            case 6:
                easeType = EaseType.EaseInCubic;
                break;
            case 7:
                easeType = EaseType.EaseOutCubic;
                break;
            case 8:
                easeType = EaseType.EaseInOutCubic;
                break;
            case 9:
                easeType = EaseType.EaseInQuart;
                break;
            case 10:
                easeType = EaseType.EaseOutQuart;
                break;
            case 11:
                easeType = EaseType.EaseInOutQuart;
                break;
            case 12:
                easeType = EaseType.EaseInQuint;
                break;
            case 13:
                easeType = EaseType.EaseOutQuint;
                break;
            case 14:
                easeType = EaseType.EaseInOutQuint;
                break;
            case 15:
                easeType = EaseType.EaseInExpo;
                break;
            case 16:
                easeType = EaseType.EaseOutExpo;
                break;
            case 17:
                easeType = EaseType.EaseInOutExpo;
                break;
            case 18:
                easeType = EaseType.EaseInCirc;
                break;
            case 19:
                easeType = EaseType.EaseOutCirc;
                break;
            case 20:
                easeType = EaseType.EaseInOutCirc;
                break;
            case 21:
                easeType = EaseType.EaseInBack;
                break;
            case 22:
                easeType = EaseType.EaseOutBack;
                break;
            case 23:
                easeType = EaseType.EaseInOutBack;
                break;
            case 24:
                easeType = EaseType.EaseInElastic;
                break;
            case 25:
                easeType = EaseType.EaseOutElastic;
                break;
            case 26:
                easeType = EaseType.EaseInOutElastic;
                break;
            case 27:
                easeType = EaseType.EaseInBounce;
                break;
            case 28:
                easeType = EaseType.EaseOutBounce;
                break;
            case 29:
                easeType = EaseType.EaseInOutBounce;
                break;
            case 30:
                easeType = EaseType.Linear;
                break;
        }
        return easeType;
    }
}