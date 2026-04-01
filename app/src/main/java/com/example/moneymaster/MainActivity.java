package com.example.moneymaster;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.moneymaster.databinding.ActivityMainBinding;
import com.example.moneymaster.ui.estadisticas.EstadisticasFragment;
import com.example.moneymaster.ui.expenses.AddExpenseActivity;
import com.example.moneymaster.ui.groups.CreateGroupActivity;
import com.example.moneymaster.ui.groups.GroupsFragment;
import com.example.moneymaster.ui.PerfilFragment;
import com.example.moneymaster.utils.SessionManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private static final String TAG_HOME    = "HOME";
    private static final String TAG_GROUPS  = "GROUPS";
    private static final String TAG_STATS   = "STATS";
    private static final String TAG_PROFILE = "PROFILE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        setupBottomNavigation();

        if (savedInstanceState == null) {
            navigateTo(TAG_HOME);
            binding.bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
    }

    // ── Toolbar ───────────────────────────────────────────────────────────────

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
    }

    public void setToolbarTitle(String title) {
        binding.toolbar.setTitle(title);
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            mostrarDialogCerrarSesion();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void mostrarDialogCerrarSesion() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Cerrar sesión")
                .setMessage("¿Seguro que quieres cerrar sesión?")
                .setPositiveButton("Cerrar sesión", (dialog, which) -> {
                    new SessionManager(this).clearSession();
                    Intent intent = new Intent(this,
                            com.example.moneymaster.ui.auth.LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // ── BottomNavigation ──────────────────────────────────────────────────────

    private void setupBottomNavigation() {
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if      (id == R.id.nav_home)    navigateTo(TAG_HOME);
            else if (id == R.id.nav_groups)  navigateTo(TAG_GROUPS);
            else if (id == R.id.nav_stats)   navigateTo(TAG_STATS);
            else if (id == R.id.nav_profile) navigateTo(TAG_PROFILE);
            else return false;
            return true;
        });
    }

    private void navigateTo(String tag) {
        Fragment fragment;
        switch (tag) {
            case TAG_GROUPS:  fragment = new GroupsFragment();      break;
            case TAG_STATS:   fragment = new EstadisticasFragment(); break;
            case TAG_PROFILE: fragment = new PerfilFragment();       break;
            default:          fragment = new HomeFragment();         break;
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment, tag)
                .commit();

        switch (tag) {
            case TAG_HOME:
                binding.fabAddExpense.setVisibility(View.VISIBLE);
                binding.fabAddExpense.setOnClickListener(v -> mostrarDialogTipoMovimiento());
                break;
            case TAG_GROUPS:
                binding.fabAddExpense.setVisibility(View.VISIBLE);
                binding.fabAddExpense.setOnClickListener(v ->
                        startActivity(new Intent(this, CreateGroupActivity.class)));
                break;
            default:
                binding.fabAddExpense.setVisibility(View.GONE);
                break;
        }
    }

    // ── FAB ───────────────────────────────────────────────────────────────────

    private void mostrarDialogTipoMovimiento() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Añadir movimiento")
                .setItems(new String[]{"Gasto", "Ingreso"}, (dialog, which) -> {
                    if (which == 0) {
                        startActivity(new Intent(this, AddExpenseActivity.class));
                    } else {
                        startActivity(new Intent(this,
                                com.example.moneymaster.ui.income.AddIncomeActivity.class));
                    }
                })
                .show();
    }

    public void showFab() {
        binding.fabAddExpense.setVisibility(View.VISIBLE);
    }

    public void hideFab() {
        binding.fabAddExpense.setVisibility(View.GONE);
    }

    // ── Ciclo de vida ─────────────────────────────────────────────────────────

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}