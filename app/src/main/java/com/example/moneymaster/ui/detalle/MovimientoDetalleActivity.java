package com.example.moneymaster.ui.detalle;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.moneymaster.R;
import com.example.moneymaster.data.database.AppDatabase;
import com.example.moneymaster.data.model.CategoriaGasto;
import com.example.moneymaster.data.model.CategoriaIngreso;
import com.example.moneymaster.data.model.GastoPersonal;
import com.example.moneymaster.data.model.IngresoPersonal;
import com.example.moneymaster.data.model.MovimientoReciente;
import com.example.moneymaster.databinding.ActivityMovimientoDetalleBinding;
import com.example.moneymaster.ui.categories.CategoryAdapter;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MovimientoDetalleActivity extends AppCompatActivity {

    private static final String EXTRA_ID   = "extra_id";
    private static final String EXTRA_TIPO = "extra_tipo";

    private ActivityMovimientoDetalleBinding binding;

    private final NumberFormat     currencyFormat =
            NumberFormat.getCurrencyInstance(Locale.getDefault());
    private final SimpleDateFormat dateFormat     =
            new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    public static void start(Context context, int id, MovimientoReciente.Tipo tipo) {
        Intent intent = new Intent(context, MovimientoDetalleActivity.class);
        intent.putExtra(EXTRA_ID, id);
        intent.putExtra(EXTRA_TIPO, tipo.name());
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMovimientoDetalleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        int id = getIntent().getIntExtra(EXTRA_ID, -1);
        String tipoStr = getIntent().getStringExtra(EXTRA_TIPO);

        if (id == -1 || tipoStr == null) {
            finish();
            return;
        }

        MovimientoReciente.Tipo tipo = MovimientoReciente.Tipo.valueOf(tipoStr);
        cargarDetalle(id, tipo);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    private void cargarDetalle(int id, MovimientoReciente.Tipo tipo) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            if (tipo == MovimientoReciente.Tipo.GASTO) {
                GastoPersonal gasto = AppDatabase.getDatabase(this)
                        .gastoPersonalDao().getByIdSync(id);
                runOnUiThread(() -> mostrarGasto(gasto));
            } else {
                IngresoPersonal ingreso = AppDatabase.getDatabase(this)
                        .ingresoPersonalDao().getByIdSync(id);
                runOnUiThread(() -> mostrarIngreso(ingreso));
            }
        });
    }

    private void mostrarGasto(GastoPersonal gasto) {
        if (gasto == null) { finish(); return; }

        binding.toolbar.setTitle(getString(R.string.detalle_gasto));
        binding.tvImporte.setText("-" + currencyFormat.format(gasto.monto));
        binding.tvImporte.setTextColor(getColor(R.color.expense_red));
        binding.tvFecha.setText(dateFormat.format(new Date(gasto.fecha)));
        binding.tvTipo.setText(getString(R.string.gasto));
        binding.tvTipo.setTextColor(getColor(R.color.expense_red));

        if (gasto.descripcion != null && !gasto.descripcion.isEmpty()) {
            binding.tvDescripcion.setText(gasto.descripcion);
            binding.layoutDescripcion.setVisibility(View.VISIBLE);
        }

        cargarCategoriaGasto(gasto.categoria_id);
    }

    private void mostrarIngreso(IngresoPersonal ingreso) {
        if (ingreso == null) { finish(); return; }

        binding.toolbar.setTitle(getString(R.string.detalle_ingreso));
        binding.tvImporte.setText("+" + currencyFormat.format(ingreso.monto));
        binding.tvImporte.setTextColor(getColor(R.color.income_green));
        binding.tvFecha.setText(dateFormat.format(new Date(ingreso.fecha)));
        binding.tvTipo.setText(getString(R.string.ingreso));
        binding.tvTipo.setTextColor(getColor(R.color.income_green));

        if (ingreso.descripcion != null && !ingreso.descripcion.isEmpty()) {
            binding.tvDescripcion.setText(ingreso.descripcion);
            binding.layoutDescripcion.setVisibility(View.VISIBLE);
        }

        cargarCategoriaIngreso(ingreso.categoria_id);
    }

    private void cargarCategoriaGasto(Integer categoriaId) {
        if (categoriaId == null) {
            binding.tvCategoria.setText(getString(R.string.sin_categoria));
            binding.ivIconoCategoria.setImageResource(R.drawable.ic_category_default);
            return;
        }
        AppDatabase.databaseWriteExecutor.execute(() -> {
            CategoriaGasto cat = AppDatabase.getDatabase(this)
                    .categoriaGastoDao().getByIdSync(categoriaId);
            runOnUiThread(() -> {
                if (cat == null) return;
                // FIX: resolver clave al nombre traducido
                binding.tvCategoria.setText(
                        CategoryAdapter.resolverNombre(this, cat.nombre));
                aplicarColorIcono(cat.color, cat.icono);
            });
        });
    }

    private void cargarCategoriaIngreso(Integer categoriaId) {
        if (categoriaId == null) {
            binding.tvCategoria.setText(getString(R.string.sin_categoria));
            binding.ivIconoCategoria.setImageResource(R.drawable.ic_category_default);
            return;
        }
        AppDatabase.databaseWriteExecutor.execute(() -> {
            CategoriaIngreso cat = AppDatabase.getDatabase(this)
                    .categoriaIngresoDao().getByIdSync(categoriaId);
            runOnUiThread(() -> {
                if (cat == null) return;
                // FIX: resolver clave al nombre traducido
                binding.tvCategoria.setText(
                        CategoryAdapter.resolverNombre(this, cat.nombre));
                aplicarColorIcono(cat.color, cat.icono);
            });
        });
    }

    private void aplicarColorIcono(String color, String icono) {
        try {
            int c = Color.parseColor(color);
            int alpha30 = Color.argb(77,
                    Color.red(c), Color.green(c), Color.blue(c));
            binding.viewIconoBg.setBackgroundTintList(
                    ColorStateList.valueOf(alpha30));
            binding.ivIconoCategoria.setColorFilter(c);
        } catch (Exception ignored) { }

        int resId = 0;
        if (icono != null && !icono.isEmpty()) {
            resId = getResources().getIdentifier(
                    icono, "drawable", getPackageName());
        }
        binding.ivIconoCategoria.setImageResource(
                resId != 0 ? resId : R.drawable.ic_category_default);
    }
}