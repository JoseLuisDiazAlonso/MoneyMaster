package com.example.moneymaster;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.moneymaster.databinding.ActivityMainBinding;
import com.example.moneymaster.ui.dialogs.ExportDialogFragment;
import com.example.moneymaster.ui.estadisticas.EstadisticasFragment;
import com.example.moneymaster.ui.groups.GroupsFragment;
import com.example.moneymaster.HomeFragment;
import com.example.moneymaster.ProfileFragment;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    // Fragment actual visible
    private Fragment currentFragment;

    // Tags para identificar cada fragment en el back stack
    private static final String TAG_HOME    = "HOME";
    private static final String TAG_GROUPS  = "GROUPS";
    private static final String TAG_STATS   = "STATS";
    private static final String TAG_PROFILE = "PROFILE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ViewBinding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        setupBottomNavigation();
        setupFab();

        // Carga el fragment de Inicio por defecto (solo si es primera vez)
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment(), TAG_HOME);
            binding.bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
    }

    // ─── Toolbar ─────────────────────────────────────────────────────────────

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
    }

    /** Permite que los fragments actualicen el título de la toolbar. */
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

        if (id == R.id.action_notifications) {
            // TODO: navegar a NotificationsActivity
            return true;

        } else if (id == R.id.action_export) {
            // Card #49 — abrir dialog de exportación
            ExportDialogFragment.newInstance()
                    .show(getSupportFragmentManager(), ExportDialogFragment.TAG);
            return true;

        } else if (id == R.id.action_settings) {
            // TODO: navegar a SettingsActivity
            return true;

        } else if (id == R.id.action_logout) {
            // TODO: lógica de logout con SessionManager
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // ─── BottomNavigation ─────────────────────────────────────────────────────

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
                loadFragment(new ProfileFragment(), TAG_PROFILE);
                hideFab();
                return true;
            }

            return false;
        });
    }

    /**
     * Carga un fragment en el contenedor principal.
     * Reutiliza la instancia si ya existe en el back stack (evita recrear la UI).
     */
    private void loadFragment(Fragment fragment, String tag) {
        Fragment existingFragment = getSupportFragmentManager().findFragmentByTag(tag);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer,
                        existingFragment != null ? existingFragment : fragment,
                        tag)
                .commit();

        currentFragment = fragment;
    }

    // ─── FAB ─────────────────────────────────────────────────────────────────

    private void setupFab() {
        binding.fabAddExpense.setOnClickListener(v -> {
            // TODO: abrir AddExpenseActivity o BottomSheet de gasto rápido
        });
    }

    /** Muestra el FAB con animación. */
    public void showFab() {
        binding.fabAddExpense.show();
    }

    /** Oculta el FAB con animación. */
    public void hideFab() {
        binding.fabAddExpense.hide();
    }

    // ─── Ciclo de vida ────────────────────────────────────────────────────────

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}