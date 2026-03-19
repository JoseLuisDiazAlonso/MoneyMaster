package com.example.moneymaster;

import android.os.Bundle;
import android.view.MenuItem;


import androidx.appcompat.app.AppCompatActivity;

import androidx.fragment.app.Fragment;

import com.example.moneymaster.databinding.ActivityMainBinding;


public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    //Fragment actualmente visible
    private Fragment currentFragment;

    //Tags para identificar cada fragment en el back stack
    private static final String TAG_HOME = "HOME";
    private static final String TAG_GROUPS = "GROUPS";
    private static final String TAG_STATS = "STATS";
    private static final String TAG_PROFILE = "PROFILE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //ViewBinding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        setupBottomNavigation();
        setupFab();

        //Cargamos el fragmento de inicio por defecto
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment(), TAG_HOME);
            binding.bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
    }

    //Toolbar

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        /**El título se establece desde el XML (app:title) o se puede cambiar dinamicamente
         * desde cada fragment llamando a (MainActivity) getActivity().setToolbarTitle("...")*/
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }

    }

    //Permite que los fragments actualicen el título del toolbar
    public void setToolbarTitle(String title) {
        binding.toolbar.setTitle(title);
    }

    //BottomNavigation

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
            //Abrir pantalla de configuración
            return true;

        } else if (id == R.id.action_logout) {
            //Cerrar sesión
            return true;
        } else if (id == R.id.action_notifications) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    /**Cargamos un fragmente en el contenedor principal.**/

    private void loadFragment(Fragment fragment, String tag) {
        //Comprobamos si ya existe una instancia en el back stack
        Fragment existingFragment = getSupportFragmentManager().findFragmentByTag(tag);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, existingFragment != null ? existingFragment : fragment, tag)
                .commit();

        currentFragment = fragment;

    }
    //FAB

    private void setupFab() {
        binding.fabAddExpense.setOnClickListener(view -> {
            //Abrir pantalla para agregar nuevo gasto
            //Ejemplo: startActivity(new Intent(this, AddExpenseActivity.class));
        });
    }

    private void showFab() {
        binding.fabAddExpense.show();
    }

    /*Ocultamos el fab con animación**/
    public void hideFab() {
        binding.fabAddExpense.hide();
    }
    //Ciclo de vida

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null; //Liberamos la referencia al binding para evitar fugas de memoria
    }
}