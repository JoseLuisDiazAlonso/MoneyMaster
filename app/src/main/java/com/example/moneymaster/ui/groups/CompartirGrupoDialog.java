package com.example.moneymaster.ui.groups;

import android.net.Uri;
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
import com.example.moneymaster.utils.ResumenImageGenerator;
import com.example.moneymaster.utils.ResumenTextGenerator;
import com.example.moneymaster.utils.ShareUtils;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * CompartirGrupoDialog — BottomSheetDialogFragment del Card #39.
 *
 * Recibe solo el grupoId y carga todos los datos necesarios desde
 * AppDatabase directamente en un hilo de fondo, sin depender del
 * GrupoViewModel (que aún está pendiente de implementar).
 *
 * Tres opciones:
 *   - Texto:  ResumenTextGenerator → ShareUtils.shareText()
 *   - Imagen: ResumenImageGenerator (hilo de fondo) → ShareUtils.shareImage()
 *   - Copiar: ResumenTextGenerator → portapapeles
 *
 * Uso:
 *   CompartirGrupoDialog.newInstance(grupoId, grupoNombre)
 *       .show(getSupportFragmentManager(), CompartirGrupoDialog.TAG);
 */
public class CompartirGrupoDialog extends BottomSheetDialogFragment {

    public static final String TAG = "CompartirGrupoDialog";

    private static final String ARG_GRUPO_ID     = "grupoId";
    private static final String ARG_GRUPO_NOMBRE = "grupoNombre";

    private DialogCompartirGrupoBinding binding;

    private final ExecutorService executor   = Executors.newSingleThreadExecutor();
    private final Handler         mainHandler = new Handler(Looper.getMainLooper());

    // ─── Factory ─────────────────────────────────────────────────────────────

    public static CompartirGrupoDialog newInstance(int grupoId, String grupoNombre) {
        CompartirGrupoDialog dialog = new CompartirGrupoDialog();
        Bundle args = new Bundle();
        args.putInt(ARG_GRUPO_ID, grupoId);
        args.putString(ARG_GRUPO_NOMBRE, grupoNombre != null ? grupoNombre : "");
        dialog.setArguments(args);
        return dialog;
    }

    // ─── Ciclo de vida ────────────────────────────────────────────────────────

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
            Toast.makeText(requireContext(), "Error: grupo no encontrado", Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }

        binding.optionTexto.setOnClickListener(v  -> ejecutarEnSegundoPlano(grupoId, Accion.TEXTO));
        binding.optionImagen.setOnClickListener(v -> ejecutarEnSegundoPlano(grupoId, Accion.IMAGEN));
        binding.optionCopiar.setOnClickListener(v -> ejecutarEnSegundoPlano(grupoId, Accion.COPIAR));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        executor.shutdownNow();
    }

    // ─── Enum de acción ───────────────────────────────────────────────────────

    private enum Accion { TEXTO, IMAGEN, COPIAR }

    // ─── Núcleo: cargar datos + ejecutar acción ───────────────────────────────

    /**
     * Carga todos los datos del grupo desde Room en un hilo de fondo y luego
     * ejecuta la acción elegida de vuelta en el hilo principal.
     *
     * Flujo:
     *   1. Mostrar loading, bloquear opciones
     *   2. executor: consultar AppDatabase (sync queries)
     *   3. mainHandler: construir DTOs y ejecutar la acción
     */
    private void ejecutarEnSegundoPlano(int grupoId, Accion accion) {
        mostrarLoading(true);

        executor.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());

            // ── Cargar datos síncronos desde Room ─────────────────────────────
            // Estos métodos deben existir en los DAOs (ver nota al final del archivo)
            Grupo              grupo    = db.grupoDao().getGrupoById(grupoId).getValue();
            List<MiembroGrupo> miembros = db.miembroGrupoDao().getMiembrosByGrupoSync(grupoId);
            List<GastoGrupo>   gastos   = db.gastoGrupoDao().getGastosByGrupoSync(grupoId);
            List<BalanceGrupo> balances = db.balanceGrupoDao().getBalancesByGrupo(grupoId).getValue();

            // Valores seguros si alguna consulta devuelve null
            if (miembros == null) miembros = new ArrayList<>();
            if (gastos   == null) gastos   = new ArrayList<>();
            if (balances == null) balances = new ArrayList<>();

            // Construir mapa id → nombre a partir de MiembroGrupo
            // MiembroGrupo no tiene campo nombre propio; el nombre viene de GastoGrupo.pagadoPorNombre
            // Usamos un mapa de usuarioId → pagadoPorNombre extrayendo los datos de gastos
            Map<Integer, String> mapaIdNombre = construirMapaNombres(miembros, gastos);

            // Snapshot final para el lambda (variables efectivamente finales)
            final Grupo              grupoFinal    = grupo    != null ? grupo    : crearGrupoPlaceholder(grupoId, requireArguments().getString(ARG_GRUPO_NOMBRE, "Grupo"));
            final List<MiembroGrupo> miembrosFinal = miembros;
            final List<GastoGrupo>   gastosFinal   = gastos;
            final List<BalanceGrupo> balancesFinal = balances;
            final Map<Integer,String> mapaFinal    = mapaIdNombre;

            // ── Generar imagen si es necesario (en este mismo hilo de fondo) ──
            File archivoPng = null;
            if (accion == Accion.IMAGEN) {
                ResumenImageGenerator.DatosGrupo datosImg =
                        ResumenImageGenerator.construirDatos(grupoFinal, mapaFinal, gastosFinal, balancesFinal);
                archivoPng = ResumenImageGenerator.generarFile(requireContext(), datosImg);
            }
            final File archivoPngFinal = archivoPng;

            // ── Volver al hilo principal para interactuar con la UI ───────────
            mainHandler.post(() -> {
                if (!isAdded() || binding == null) return;

                mostrarLoading(false);

                // Construir DatosGrupo para el generador de texto
                ResumenTextGenerator.DatosGrupo datosTexto = new ResumenTextGenerator.DatosGrupo(
                        grupoFinal, miembrosFinal, gastosFinal, balancesFinal, mapaFinal);

                switch (accion) {

                    case TEXTO:
                        String texto = ResumenTextGenerator.generar(datosTexto);
                        ShareUtils.shareText(requireContext(), texto,
                                "Resumen de " + grupoFinal.nombre, "Compartir resumen");
                        dismiss();
                        break;

                    case IMAGEN:
                        if (archivoPngFinal != null && archivoPngFinal.exists()) {
                            ShareUtils.shareImage(requireContext(), archivoPngFinal,
                                    "image/png", "Resumen de " + grupoFinal.nombre,
                                    "Compartir imagen");
                            dismiss();
                        } else {
                            Toast.makeText(requireContext(),
                                    "No se pudo generar la imagen", Toast.LENGTH_LONG).show();
                        }
                        break;

                    case COPIAR:
                        String textoCopiar = ResumenTextGenerator.generar(datosTexto);
                        ResumenTextGenerator.copiarAlPortapapeles(requireContext(), textoCopiar);
                        dismiss();
                        break;
                }
            });
        });
    }

    // ─── UI helpers ───────────────────────────────────────────────────────────

    private void mostrarLoading(boolean cargando) {
        if (binding == null) return;
        int opacidad = cargando ? View.GONE : View.VISIBLE;
        binding.optionTexto.setVisibility(opacidad);
        binding.optionImagen.setVisibility(opacidad);
        binding.optionCopiar.setVisibility(opacidad);
        binding.layoutLoading.setVisibility(cargando ? View.VISIBLE : View.GONE);
        if (getDialog() != null) getDialog().setCancelable(!cargando);
    }

    // ─── Helpers de datos ─────────────────────────────────────────────────────

    /**
     * Construye el mapa usuarioId → nombre.
     *
     * Como MiembroGrupo.usuarioId es el ID y GastoGrupo almacena el nombre
     * del pagador en pagadoPorNombre (texto plano), intentamos cruzar los datos.
     * Si no hay coincidencia, usamos "Miembro N" como fallback.
     */
    private Map<Integer, String> construirMapaNombres(List<MiembroGrupo> miembros,
                                                      List<GastoGrupo> gastos) {
        Map<Integer, String> mapa = new HashMap<>();

        // Recopilar nombres únicos de pagadores desde gastos
        // (GastoGrupo.pagadoPorNombre es el nombre en texto plano)
        // No podemos hacer un JOIN directo, así que usamos el índice posicional:
        // el miembro 0 → primer nombre encontrado en gastos, etc.
        // En implementaciones futuras (cuando MiembroGrupo tenga campo nombre)
        // simplificar a: mapa.put(m.usuarioId, m.nombre)
        for (MiembroGrupo m : miembros) {
            // Buscar si algún gasto tiene pagadoPorNombre que coincida con algún
            // nombre ya conocido — si no, dejar como placeholder
            mapa.put(m.usuarioId, "Miembro " + m.usuarioId);
        }

        // Intentar enriquecer el mapa con pagadoPorNombre de GastoGrupo
        // Si en tu entidad MiembroGrupo ya tienes campo nombre, usa:
        //   mapa.put(m.usuarioId, m.nombre);
        // y elimina este bloque.
        for (GastoGrupo g : gastos) {
            if (g.pagadoPorNombre != null && !g.pagadoPorNombre.isEmpty()
                    && g.pagadoPorId != 0) {
                // Sobrescribir el placeholder con el nombre real si tenemos el ID
                mapa.put(g.pagadoPorId, g.pagadoPorNombre);
            }
        }

        return mapa;
    }

    /**
     * Crea un objeto Grupo mínimo si la consulta a Room devuelve null
     * (caso extremo: el grupo fue eliminado justo antes de compartir).
     */
    private Grupo crearGrupoPlaceholder(int id, String nombre) {
        Grupo g = new Grupo();
        g.id     = id;
        g.nombre = nombre != null ? nombre : "Grupo";
        return g;
    }

    /*
     * ─── NOTA: Métodos síncronos requeridos en los DAOs ──────────────────────
     *
     * Este Dialog llama a métodos síncronos (no LiveData) porque se ejecuta
     * dentro de un executor. Verifica que estos métodos existan en tus DAOs:
     *
     * GrupoDao:
     *   @Query("SELECT * FROM grupos WHERE id = :id")
     *   Grupo getGrupoByIdSync(int id);
     *
     * MiembroGrupoDao:
     *   @Query("SELECT * FROM miembros_grupo WHERE grupo_id = :grupoId AND activo = 1")
     *   List<MiembroGrupo> getMiembrosByGrupoSync(int grupoId);
     *
     * GastoGrupoDao:
     *   @Query("SELECT * FROM gastos_grupo WHERE grupo_id = :grupoId ORDER BY fecha DESC")
     *   List<GastoGrupo> getGastosByGrupoSync(int grupoId);
     *
     * BalanceGrupoDao:
     *   @Query("SELECT * FROM balance_grupo WHERE grupo_id = :grupoId")
     *   List<BalanceGrupo> getBalancesByGrupoSync(int grupoId);
     *
     * Si no existen, añádelos — son idénticos a los que ya tienes para LiveData
     * pero sin el wrapper LiveData<> en el tipo de retorno.
     */
}
