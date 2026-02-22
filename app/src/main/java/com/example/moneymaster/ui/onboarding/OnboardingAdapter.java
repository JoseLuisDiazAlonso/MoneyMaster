package com.example.moneymaster.ui.onboarding;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.moneymaster.fragment_onboarding1;
import com.example.moneymaster.fragment_onboarding2;
import com.example.moneymaster.fragment_onboarding3;

public class OnboardingAdapter extends FragmentStateAdapter {

    public OnboardingAdapter (@NonNull OnboardingActivity OnboardingActivity) {
        super(OnboardingActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new fragment_onboarding1();
            case 1:
                return new fragment_onboarding2();
            case 2:
                return new fragment_onboarding3();
            default:
                return new fragment_onboarding1();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }

}
