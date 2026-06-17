package com.bagus.toko_baju_uas.util;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import com.bagus.toko_baju_uas.R;

public class AnimationUtil {
    public static void animateButtonClick(View view) {
        Animation anim = AnimationUtils.loadAnimation(view.getContext(), R.anim.button_click);
        view.startAnimation(anim);
    }
}