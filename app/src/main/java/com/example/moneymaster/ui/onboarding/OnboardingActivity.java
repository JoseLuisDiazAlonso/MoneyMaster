package com.example.moneymaster.ui.onboarding;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class OnboardingActivity extends AppCompatActivity {
   @Override
   protected void onCreate (Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       TextView textView = new TextView(this);
       textView.setText("Onboarding Activity");
       textView.setTextSize(24);
       textView.setPadding(50, 50, 50,50);
         setContentView(textView);
   }

}
