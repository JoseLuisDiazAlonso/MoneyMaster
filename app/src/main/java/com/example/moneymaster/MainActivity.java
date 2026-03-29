package com.example.moneymaster;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.moneymaster.databinding.ActivityMainBinding;
import com.example.moneymaster.ui.dialogs.ExportDialogFragment;
import com.example.moneymaster.ui.estadisticas.EstadisticasFragment;
import com.example.moneymaster.ui.expenses.AddExpenseActivity;
import com.example.moneymaster.ui.groups.GroupsFragment;
import com.example.moneymaster.ui.PerfilFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private Fragment currentFragment;

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
        setupFab();

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment(), TAG_HOME);
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
        int id = item.getItemId();

        if (id == R.id.action_export) {
            ExportDialogFragment.newInstance()
                    .show(getSupportFragmentManager(), ExportDialogFragment.TAG);
            return true;

        } else if (id == R.id.action_logout) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    // ── BottomNavigation ──────────────────────────────────────────────────────

    private void setupBottomNavigation() {
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                loadFragment(new HomeFragment(), TAG_HOME);
                showFab();
                return true;

            } else if (id == R.id.nav_groups) {
                loadFragment(new GroupsFragment(), TAG_GROUPS);
                hideFab();
                return true;

            } else if (id == R.id.nav_stats) {
                loadFragment(new EstadisticasFragment(), TAG_STATS);
                hideFab();
                return true;

            } else if (id == R.id.nav_profile) {
                loadFragment(new PerfilFragment(), TAG_PROFILE);
                hideFab();
                return true;
            }

            return false;
        });
    }

    private void loadFragment(Fragment fragment, String tag) {
        Fragment existingFragment =
                getSupportFragmentManager().findFragmentByTag(tag);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer,
                        existingFragment != null ? existingFragment : fragment,
                        tag)
                .commit();
        currentFragment = fragment;
    }

    // ── FAB ───────────────────────────────────────────────────────────────────

    private void setupFab() {
        binding.fabAddExpense.setOnClickListener(v -> mostrarDialogTipoMovimiento());
    }

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
        binding.fabAddExpense.show();
    }

    public void hideFab() {
        binding.fabAddExpense.hide();
    }

    // ── Ciclo de vida ─────────────────────────────────────────────────────────

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}