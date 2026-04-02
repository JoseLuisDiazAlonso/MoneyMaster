package com.example.moneymaster.ui.groups;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.moneymaster.R;
import com.example.moneymaster.databinding.ActivityGroupDetailBinding;
import com.google.android.material.tabs.TabLayoutMediator;

public class GroupDetailActivity extends AppCompatActivity {

    private static final String EXTRA_GRUPO_ID     = "grupoId";
    private static final String EXTRA_GRUPO_NOMBRE = "grupoNombre";

    private ActivityGroupDetailBinding binding;

    private int    grupoId;
    private String grupoNombre;

    // ─── Factory ─────────────────────────────────────────────────────────────

    public static void start(Context context, int grupoId, String grupoNombre) {
        Intent intent = new Intent(context, GroupDetailActivity.class);
        intent.putExtra(EXTRA_GRUPO_ID, grupoId);
        intent.putExtra(EXTRA_GRUPO_NOMBRE, grupoNombre);
        context.startActivity(intent);
    }

    // ─── Ciclo de vida ────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        grupoId     = getIntent().getIntExtra(EXTRA_GRUPO_ID, -1);
        grupoNombre = getIntent().getStringExtra(EXTRA_GRUPO_NOMBRE);

        configurarToolbar(grupoNombre);
        configurarViewPager(grupoId);
    }

    private void configurarToolbar(String nombre) {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(nombre != null ? nombre : "Grupo");
        }
    }

    private void configurarViewPager(int id) {
        GroupPagerAdapter adapter = new GroupPagerAdapter(this, id);
        binding.viewPager.setAdapter(adapter);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0: tab.setText("Gastos");  break;
                        case 1: tab.setText("Balance"); break;
                    }
                }).attach();
    }

    // ─── Menú Toolbar ─────────────────────────────────────────────────────────

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_group_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        if (item.getItemId() == R.id.action_compartir) {
            mostrarDialogCompartir();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ─── Compartir ────────────────────────────────────────────────────────────

    private void mostrarDialogCompartir() {
        CompartirGrupoDialog dialog = CompartirGrupoDialog.newInstance(grupoId, grupoNombre);
        dialog.show(getSupportFragmentManager(), CompartirGrupoDialog.TAG);
    }

    // ─── Adapter de pestañas ──────────────────────────────────────────────────

    private static class GroupPagerAdapter extends FragmentStateAdapter {

        private final int grupoId;

        GroupPagerAdapter(FragmentActivity activity, int grupoId) {
            super(activity);
            this.grupoId = grupoId;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:  return GroupExpensesFragment.newInstance(grupoId);
                case 1:  return GroupBalanceFragment.newInstance(grupoId);
                default: return GroupExpensesFragment.newInstance(grupoId);
            }
        }

        @Override
        public int getItemCount() { return 2; }
    }
}