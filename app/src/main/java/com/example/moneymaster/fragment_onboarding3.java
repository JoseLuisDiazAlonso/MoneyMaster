package com.example.moneymaster;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


public class fragment_onboarding3 extends Fragment {

    //Referencias a las vistas

    private ImageView iconContainer;
    private TextView tvTitle;
    private TextView tvDescription;

    //Constuctor vacío

    public fragment_onboarding3() {

    }

    //Método para crear una nueva instancia del fragment

    public static fragment_onboarding3 newInstance() {
        return new fragment_onboarding3();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Inflar el layout del fragment
        return inflater.inflate(R.layout.fragment_onboarding3, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Inicializar las vistas
        initViews(view);
    }

    //Inicializamos todas las referencias a las vistas del Layout
    private void initViews(View view) {
        iconContainer = view.findViewById(R.id.iconContainer);
        tvTitle = view.findViewById(R.id.tvTitle);
        tvDescription = view.findViewById(R.id.description);
    }




}