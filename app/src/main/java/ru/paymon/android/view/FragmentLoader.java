package ru.paymon.android.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import ru.paymon.android.NotificationManager;
import ru.paymon.android.R;
import ru.paymon.android.User;
import ru.paymon.android.utils.Utils;

public class FragmentLoader extends Fragment {
    private static FragmentLoader instance;
    private String text;

    public static synchronized FragmentLoader newInstance() {
        instance = new FragmentLoader();
        return instance;
    }

    public static synchronized FragmentLoader newInstance(String text) {
        instance = new FragmentLoader();
        instance.text = text;
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_loader, container, false);

        TextView name = view.findViewById(R.id.fragment_loader_name_text_view);
        ImageView logo = view.findViewById(R.id.fragment_loader_logo_image_view);

        if (text == null) {
            if (User.currentUser != null)
                name.setText(String.format("%s, %s", getString(R.string.hi), Utils.formatUserName(User.currentUser)));
            else
                name.setText(String.format("%s", getString(R.string.hi)));
        } else {
            name.setText(text);
        }

        name.setVisibility(View.GONE);

        Animation fadeInLogo = new AlphaAnimation(0, 1);
        fadeInLogo.setInterpolator(new DecelerateInterpolator());
        fadeInLogo.setDuration(3000);

        Animation fadeInLogo2 = AnimationUtils.loadAnimation(getActivity(), R.anim.icon_anim_fade_out);
        fadeInLogo.setInterpolator(new AnticipateOvershootInterpolator());
        fadeInLogo.setDuration(3000);

        AnimationSet logoSet = new AnimationSet(true);
        logoSet.addAnimation(fadeInLogo);
        logoSet.addAnimation(fadeInLogo2);

        Animation fadeInName = new AlphaAnimation(0, 1);
        fadeInName.setInterpolator(new DecelerateInterpolator());
        fadeInName.setDuration(5000);

        Animation fadeOutName = new AlphaAnimation(1, 0);
        fadeOutName.setInterpolator(new DecelerateInterpolator());
        fadeOutName.setDuration(2500);

        Animation slide = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_text);
        slide.setDuration(5000);

        AnimationSet set = new AnimationSet(true);
        set.addAnimation(fadeInName);
        set.addAnimation(slide);

        Animation fadeOutPoint = new AlphaAnimation(1, 0);

        fadeOutPoint.setInterpolator(new DecelerateInterpolator());
        fadeOutPoint.setDuration(2500);

        fadeInLogo.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                animation.reset();
                logo.startAnimation(fadeInLogo2);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        fadeInLogo2.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                animation.reset();
                logo.startAnimation(fadeInLogo);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        name.setVisibility(View.VISIBLE);
        name.startAnimation(set);
        logo.startAnimation(fadeInLogo);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.hideActionBar(getActivity());
        Utils.hideBottomBar(getActivity());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
