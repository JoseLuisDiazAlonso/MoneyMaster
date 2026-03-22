package com.example.moneymaster.ui.groups;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.moneymaster.data.database.AppDatabase;
import com.example.moneymaster.data.model.Grupo;
import com.example.moneymaster.data.model.MiembroGrupo;
import com.example.moneymaster.databinding.ActivityCreateGroupBinding;
import com.example.moneymaster.ui.adapter.MemberInputAdapter;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class CreateGroupActivity extends AppCompatActivity {

    private ActivityCreateGroupBinding binding;
    private MemberInputAdapter memberAdapter;

    private static final String[] MEMBER_COLORS = {
            "#F44336", "#E91E63", "#9C27B0", "#3F51B5",
            "#2196F3", "#009688", "#4CAF50", "#FF9800",
            "#795548", "#607D8B"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateGroupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupToolbar();
        setupRecyclerView();
        setupButtons();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Nuevo grupo");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupRecyclerView() {
        memberAdapter = new MemberInputAdapter(position -> {
            memberAdapter.removeMember(position);
            updateMemberCount();
        });
        binding.recyclerViewMembers.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewMembers.setAdapter(memberAdapter);
        binding.recyclerViewMembers.setNestedScrollingEnabled(false);
    }

    private void setupButtons() {
        binding.buttonAddMember.setOnClickListener(v -> {
            int nextIndex = memberAdapter.getItemCount();
            String color = MEMBER_COLORS[nextIndex % MEMBER_COLORS.length];
            memberAdapter.addMember(color);
            updateMemberCount();
            binding.recyclerViewMembers.post(() ->
                    binding.recyclerViewMembers.smoothScrollToPosition(
                            memberAdapter.getItemCount() - 1));
        });
        binding.buttonCreateGroup.setOnClickListener(v -> validateAndCreate());
    }

    private void validateAndCreate() {
        String nombre = binding.editTextGroupName.getText() != null
                ? binding.editTextGroupName.getText().toString().trim() : "";
        String descripcion = binding.editTextGroupDescription.getText() != null
                ? binding.editTextGroupDescription.getText().toString().trim() : "";

        if (TextUtils.isEmpty(nombre)) {
            binding.inputLayoutGroupName.setError("El nombre del grupo es obligatorio");
            binding.editTextGroupName.requestFocus();
            return;
        }
        binding.inputLayoutGroupName.setError(null);

        List<String> nombresMiembros = memberAdapter.getMemberNames();
        List<String> coloresMiembros = memberAdapter.getMemberColors();
        List<String> nombresValidos = new ArrayList<>();
        List<String> coloresValidos = new ArrayList<>();

        for (int i = 0; i < nombresMiembros.size(); i++) {
            String nm = nombresMiembros.get(i).trim();
            if (!TextUtils.isEmpty(nm)) {
                nombresValidos.add(nm);
                coloresValidos.add(coloresMiembros.get(i));
            }
        }

        if (nombresValidos.size() < 2) {
            Snackbar.make(binding.getRoot(),
                    "Añade al menos 2 miembros al grupo",
                    Snackbar.LENGTH_LONG).show();
            return;
        }

        binding.buttonCreateGroup.setEnabled(false);
        binding.progressBar.setVisibility(View.VISIBLE);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(this);

            // 1. Insertar grupo
            Grupo grupo = new Grupo();
            grupo.nombre = nombre;
            grupo.descripcion = TextUtils.isEmpty(descripcion) ? null : descripcion;
            grupo.fechaCreacion = System.currentTimeMillis();
            long grupoId = db.grupoDao().insertGrupo(grupo);

            // 2. Insertar miembros
            for (int i = 0; i < nombresValidos.size(); i++) {
                MiembroGrupo miembro = new MiembroGrupo();
                miembro.grupoId = (int) grupoId;
                miembro.nombre = nombresValidos.get(i);
                miembro.color = coloresValidos.get(i);
                miembro.fechaUnion = System.currentTimeMillis();
                db.miembroGrupoDao().insertar(miembro);
            }

            // 3. Volver al hilo principal
            runOnUiThread(() -> {
                binding.progressBar.setVisibility(View.GONE);
                setResult(RESULT_OK);
                finish();
            });
        });
    }

    private void updateMemberCount() {
        int count = memberAdapter.getItemCount();
        String label = count == 0 ? "Sin miembros"
                : count + (count == 1 ? " miembro" : " miembros");
        binding.textViewMemberCount.setText(label);
        binding.textViewMemberWarning.setVisibility(count < 2 ? View.VISIBLE : View.GONE);
    }
}