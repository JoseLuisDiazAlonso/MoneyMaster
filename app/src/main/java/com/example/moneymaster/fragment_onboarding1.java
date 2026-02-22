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

import com.google.android.material.card.MaterialCardView;

/*Fragment que muestar la primera pantalla del onboarding.**/

public class fragment_onboarding1 extends Fragment {

    private ImageView iconContainer;
    private TextView tvTitle;
    private TextView tvDescription;

  // Constructor vacío requerido para Android

  public fragment_onboarding1 () {

  }

  //Método para crear una nueva instancia del fragment

    public static fragment_onboarding1 newInstance() {
      return new fragment_onboarding1();
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflar el layout del fragment
        return inflater.inflate(R.layout.fragment_onboarding1, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Inicializar las vistas
        initViews (view);
    }

    private void initViews(View view) {
      iconContainer = view.findViewById(R.id.iconContainer);
      tvTitle = view.findViewById(R.id.tvTitle);
      tvDescription = view.findViewById(R.id.description);
    }


}