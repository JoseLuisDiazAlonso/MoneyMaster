package com.example.moneymaster.ui.auth;

import android.text.TextWatcher;

public abstract class SimpleTextWatcher implements TextWatcher {
    @Override
    public void beforeTextChanged (CharSequence s, int start, int count, int after) {
        // Vacio intencionado
    }
    @Override
    public void onTextChanged (CharSequence s, int start, int before, int count) {
        // Vacio intenionado
    }
    @Override
    public void afterTextChanged (android.text.Editable s) {
        // No-op
    }
}
