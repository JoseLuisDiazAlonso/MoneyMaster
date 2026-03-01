package com.example.moneymaster;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class fragment_onboarding2 extends Fragment {

    // Referencias a las vistas
    private ImageView iconContainer;
    private TextView  tvTitle;
    private TextView  tvDescription;

    // Constructor vacío obligatorio
    public fragment_onboarding2() {}

    // Método para crear una nueva instancia del fragment
    public static fragment_onboarding2 newInstance() {
        return new fragment_onboarding2(); // ← corregido: devuelve fragment_onboarding2, no 1
    }

    @Override // ← corregido: @Override + "on" en minúscula
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_onboarding2, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
    }

    // Inicializamos todas las referencias a las vistas del Layout
    private void initViews(View view) {
        iconContainer = view.findViewById(R.id.imageOnboarding); // ← corregido: ID real del XML
        tvTitle       = view.findViewById(R.id.tvTitle2);        // ← corregido: ID real del XML
        tvDescription = view.findViewById(R.id.tvDescription2);  // ← corregido: ID real del XML
    }
}