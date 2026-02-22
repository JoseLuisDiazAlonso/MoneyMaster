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

    //Referencia a las vistas

    private ImageView iconContainer;
    private TextView tvTitle;
    private TextView tvDescription;

    //Constructor vacío

    public fragment_onboarding2() {
        // Required empty public constructor
    }


  //Método para crear una nueva instancia del fragment

  public static fragment_onboarding1 newInstance() {
        return new fragment_onboarding1();
  }

  public View OnCreateView (@NonNull LayoutInflater inflter, @NonNull ViewGroup container, @NonNull Bundle savedInstanceState) {
        //Inflar el layout del fragment
        return inflter.inflate(R.layout.fragment_onboarding2, container, false);
  }

  @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
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