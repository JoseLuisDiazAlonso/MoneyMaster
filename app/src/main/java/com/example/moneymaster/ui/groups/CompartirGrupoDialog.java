package com.example.moneymaster.ui.groups;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.moneymaster.data.database.AppDatabase;
import com.example.moneymaster.data.model.BalanceGrupo;
import com.example.moneymaster.data.model.GastoGrupo;
import com.example.moneymaster.data.model.Grupo;
import com.example.moneymaster.data.model.MiembroGrupo;
import com.example.moneymaster.databinding.DialogCompartirGrupoBinding;
import com.example.moneymaster.utils.ResumenTextGenerator;
import com.example.moneymaster.utils.ShareUtils;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CompartirGrupoDialog extends BottomSheetDialogFragment {

    public static final String TAG = "CompartirGrupoDialog";

    private static final String ARG_GRUPO_ID     = "grupoId";
    private static final String ARG_GRUPO_NOMBRE = "grupoNombre";

    private DialogCompartirGrupoBinding binding;

    private final ExecutorService executor    = Executors.newSingleThreadExecutor();
    private final Handler         mainHandler = new Handler(Looper.getMainLooper());

    //Factory

    public static CompartirGrupoDialog newInstance(int grupoId, String grupoNombre) {
        CompartirGrupoDialog dialog = new CompartirGrupoDialog();
        Bundle args = new Bundle();
        args.putInt(ARG_GRUPO_ID, grupoId);
        args.putString(ARG_GRUPO_NOMBRE, grupoNombre != null ? grupoNombre : "");
        dialog.setArguments(args);
        return dialog;
    }

    //Ciclo de vida

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DialogCompartirGrupoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int grupoId = requireArguments().getInt(ARG_GRUPO_ID, -1);
        if (grupoId == -1) {
            Toast.makeText(requireContext(), "Error: grupo no encontrado",
                    Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }

        binding.optionTexto.setOnClickListener(v -> compartirTexto(grupoId));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        executor.shutdownNow();
    }

    //Compartir texto

    private void compartirTexto(int grupoId) {
        mostrarLoading(true);

        executor.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(requireContext());

            Grupo              grupo    = db.grupoDao().getGrupoByIdSync(grupoId);
            List<MiembroGrupo> miembros = db.miembroGrupoDao().getMiembrosByGrupoSync(grupoId);
            List<GastoGrupo>   gastos   = db.gastoGrupoDao().getGastosByGrupoSync(grupoId);
            List<BalanceGrupo> balances = db.balanceGrupoDao().getBalancesByGrupoSync(grupoId);

            if (miembros == null) miembros = new ArrayList<>();
            if (gastos   == null) gastos   = new ArrayList<>();
            if (balances == null) balances = new ArrayList<>();

            Map<Integer, String> mapaIdNombre = construirMapaNombres(miembros, gastos);

            final Grupo              grupoFinal    = grupo != null ? grupo
                    : crearGrupoPlaceholder(grupoId,
                    requireArguments().getString(ARG_GRUPO_NOMBRE, "Grupo"));
            final List<MiembroGrupo> miembrosFinal = miembros;
            final List<GastoGrupo>   gastosFinal   = gastos;
            final List<BalanceGrupo> balancesFinal = balances;
            final Map<Integer,String> mapaFinal    = mapaIdNombre;

            mainHandler.post(() -> {
                if (!isAdded() || binding == null) return;

                mostrarLoading(false);

                ResumenTextGenerator.DatosGrupo datos = new ResumenTextGenerator.DatosGrupo(
                        grupoFinal, miembrosFinal, gastosFinal, balancesFinal, mapaFinal);

                String texto = ResumenTextGenerator.generar(datos);
                ShareUtils.shareText(requireContext(), texto,
                        "Resumen de " + grupoFinal.nombre, "Compartir resumen");
                dismiss();
            });
        });
    }

    //UI helpers

    private void mostrarLoading(boolean cargando) {
        if (binding == null) return;
        binding.optionTexto.setVisibility(cargando ? View.GONE : View.VISIBLE);
        binding.layoutLoading.setVisibility(cargando ? View.VISIBLE : View.GONE);
        if (getDialog() != null) getDialog().setCancelable(!cargando);
    }

    //Helpers de datos

    private Map<Integer, String> construirMapaNombres(List<MiembroGrupo> miembros,
                                                      List<GastoGrupo> gastos) {
        Map<Integer, String> mapa = new HashMap<>();

        for (MiembroGrupo m : miembros) {
            String nombre = (m.nombre != null && !m.nombre.isEmpty())
                    ? m.nombre
                    : "Miembro " + m.id;
            mapa.put(m.id, nombre);
        }

        for (GastoGrupo g : gastos) {
            if (g.pagadoPorNombre != null && !g.pagadoPorNombre.isEmpty()
                    && g.pagadoPorId != 0) {
                String actual = mapa.get(g.pagadoPorId);
                if (actual == null || actual.startsWith("Miembro ")) {
                    mapa.put(g.pagadoPorId, g.pagadoPorNombre);
                }
            }
        }

        return mapa;
    }

    private Grupo crearGrupoPlaceholder(int id, String nombre) {
        Grupo g = new Grupo();
        g.id     = id;
        g.nombre = nombre != null ? nombre : "Grupo";
        return g;
    }
}