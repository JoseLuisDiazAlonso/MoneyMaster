package com.example.moneymaster;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.moneymaster.databinding.ActivityMainBinding;
import com.example.moneymaster.ui.categories.CategoriesFragment;
import com.example.moneymaster.ui.expenses.AddExpenseActivity;
import com.example.moneymaster.ui.groups.GroupsFragment;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private Fragment currentFragment;

    private static final String TAG_HOME       = "HOME";
    private static final String TAG_CATEGORIES = "CATEGORIES";
    private static final String TAG_GROUPS     = "GROUPS";
    private static final String TAG_STATS      = "STATS";
    private static final String TAG_PROFILE    = "PROFILE";

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

    // ── Toolbar ──────────────────────────────────────────────────────────────

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
    }

    public void setToolbarTitle(String title) {
        binding.toolbar.setTitle(title);
    }

    // ── BottomNavigation ─────────────────────────────────────────────────────

    private void setupBottomNavigation() {
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                loadFragment(new HomeFragment(), TAG_HOME);
                showFab();
                return true;

            } else if (id == R.id.nav_categories) {
                loadFragment(new CategoriesFragment(), TAG_CATEGORIES);
                hideFab();
                return true;

            } else if (id == R.id.nav_groups) {
                loadFragment(new GroupsFragment(), TAG_GROUPS);
                hideFab();
                return true;

            } else if (id == R.id.nav_stats) {
                loadFragment(new StatsFragment(), TAG_STATS);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_logout) {
            return true;
        } else if (id == R.id.action_notifications) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ── Fragment loader ──────────────────────────────────────────────────────

    private void loadFragment(Fragment fragment, String tag) {
        Fragment existingFragment = getSupportFragmentManager().findFragmentByTag(tag);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer,
                        existingFragment != null ? existingFragment : fragment, tag)
                .commit();

        currentFragment = fragment;
    }

    // ── FAB ──────────────────────────────────────────────────────────────────

    private void setupFab() {
        binding.fabAddExpense.setOnClickListener(view -> {
            startActivity(new Intent(this, AddExpenseActivity.class));
        });
    }

    public void showFab() {
        binding.fabAddExpense.show();
    }

    public void hideFab() {
        binding.fabAddExpense.hide();
    }

    // ── Ciclo de vida ────────────────────────────────────────────────────────

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}