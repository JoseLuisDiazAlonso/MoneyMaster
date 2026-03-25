package com.example.moneymaster.ui.groups;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.moneymaster.R;
import com.example.moneymaster.data.model.FotoRecibo;
import com.example.moneymaster.databinding.FragmentPhotoBoardBinding;
import com.example.moneymaster.ui.groups.adapter.PhotoBoardAdapter;
import com.example.moneymaster.ui.viewer.ImageViewerActivity;
import com.example.moneymaster.utils.ShareUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * PhotoBoardFragment — Tablón de fotos del grupo en cuadrícula 3 columnas.
 *
 * Funcionalidades:
 *   - Click normal: abrir foto ampliada (ImageViewerActivity)
 *   - Long-press: activar modo selección múltiple
 *   - Toolbar contextual: compartir seleccionadas / eliminar seleccionadas
 *
 * Card #40 — actualiza compartirSeleccionadas() para:
 *   1. Construir ArrayList<Uri> con FileProvider
 *   2. Intentar WhatsApp directamente si está instalado (1 foto → shareImageViaWhatsApp,
 *      varias fotos → ACTION_SEND_MULTIPLE con setPackage WhatsApp)
 *   3. Fallback al chooser genérico del sistema si WhatsApp no está disponible
 *
 * Depende de: ShareUtils (Card #36)
 */
public class PhotoBoardFragment extends Fragment {

    public static final String ARG_GRUPO_ID = "grupoId";

    // Package names de WhatsApp para la preselección
    private static final String WHATSAPP_PACKAGE          = "com.whatsapp";
    private static final String WHATSAPP_BUSINESS_PACKAGE = "com.whatsapp.w4b";

    private FragmentPhotoBoardBinding binding;
    private PhotoBoardViewModel       viewModel;
    private PhotoBoardAdapter         adapter;
    private ActionMode                actionMode;

    // ─── Factory ─────────────────────────────────────────────────────────────

    public static PhotoBoardFragment newInstance(int grupoId) {
        PhotoBoardFragment f = new PhotoBoardFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_GRUPO_ID, grupoId);
        f.setArguments(args);
        return f;
    }

    // ─── Ciclo de vida ────────────────────────────────────────────────────────

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPhotoBoardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int grupoId = getArguments() != null
                ? getArguments().getInt(ARG_GRUPO_ID, -1) : -1;
        if (grupoId == -1) return;

        viewModel = new ViewModelProvider(this).get(PhotoBoardViewModel.class);
        viewModel.init(grupoId);

        configurarGrid();
        observarFotos();
    }

    // ─── Grid ─────────────────────────────────────────────────────────────────

    private void configurarGrid() {
        adapter = new PhotoBoardAdapter(
                // Click normal → ver foto ampliada o toggle selección
                foto -> {
                    if (adapter.isSelectionMode()) {
                        toggleSeleccion(foto);
                    } else {
                        abrirViewer(foto);
                    }
                },
                // Long-press → activar selección múltiple
                foto -> {
                    if (!adapter.isSelectionMode()) {
                        activarModoSeleccion();
                    }
                    toggleSeleccion(foto);
                    return true;
                }
        );

        binding.recyclerViewFotos.setLayoutManager(
                new GridLayoutManager(requireContext(), 3));
        binding.recyclerViewFotos.setAdapter(adapter);
    }

    // ─── Observadores ─────────────────────────────────────────────────────────

    private void observarFotos() {
        viewModel.getFotos().observe(getViewLifecycleOwner(), fotos -> {
            adapter.submitList(fotos);
            boolean vacio = fotos == null || fotos.isEmpty();
            binding.layoutEmptyFotos.setVisibility(vacio ? View.VISIBLE : View.GONE);
            binding.recyclerViewFotos.setVisibility(vacio ? View.GONE : View.VISIBLE);
        });
    }

    // ─── Viewer ──────────────────────────────────────────────────────────────

    private void abrirViewer(FotoRecibo foto) {
        ImageViewerActivity.start(requireContext(), foto.id, foto.rutaArchivo);
    }

    // ─── Selección múltiple ───────────────────────────────────────────────────

    private void toggleSeleccion(FotoRecibo foto) {
        adapter.toggleSeleccion(foto.id);
        int count = adapter.getSeleccionCount();

        if (count == 0) {
            finalizarModoSeleccion();
            return;
        }

        if (actionMode != null) {
            actionMode.setTitle(count + " seleccionada" + (count == 1 ? "" : "s"));
        }
    }

    private void activarModoSeleccion() {
        actionMode = requireActivity().startActionMode(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.menu_photo_selection, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                if (item.getItemId() == R.id.action_compartir) {
                    compartirSeleccionadas();
                    return true;
                } else if (item.getItemId() == R.id.action_eliminar) {
                    confirmarEliminar();
                    return true;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                finalizarModoSeleccion();
            }
        });

        adapter.setSelectionMode(true);
        actionMode.setTitle("0 seleccionadas");
    }

    private void finalizarModoSeleccion() {
        adapter.setSelectionMode(false);
        adapter.limpiarSeleccion();
        if (actionMode != null) {
            actionMode.finish();
            actionMode = null;
        }
    }

    // ─── Compartir — Card #40 ─────────────────────────────────────────────────

    /**
     * Comparte las fotos seleccionadas intentando WhatsApp primero.
     *
     * Flujo:
     *   1. Convierte rutas locales en Uris seguras con FileProvider.
     *   2. Si hay exactamente 1 foto: usa ShareUtils.shareImageViaWhatsApp()
     *      (que cae en chooser genérico si WhatsApp no está instalado).
     *   3. Si hay varias fotos: intenta WhatsApp directamente con setPackage().
     *      Si WhatsApp no está instalado, abre el chooser genérico del sistema.
     *   4. Siempre finaliza el modo selección al terminar.
     */
    private void compartirSeleccionadas() {
        List<FotoRecibo> seleccionadas = adapter.getSeleccionadas();
        if (seleccionadas.isEmpty()) return;

        // ── Paso 1: construir ArrayList<Uri> con FileProvider ─────────────────
        ArrayList<Uri> uris = new ArrayList<>();
        for (FotoRecibo foto : seleccionadas) {
            File file = new File(foto.rutaArchivo);
            if (file.exists()) {
                try {
                    Uri uri = FileProvider.getUriForFile(
                            requireContext(),
                            requireContext().getPackageName() + ".fileprovider",
                            file);
                    uris.add(uri);
                } catch (IllegalArgumentException e) {
                    // Archivo fuera del directorio autorizado — ignorar
                    e.printStackTrace();
                }
            }
        }

        if (uris.isEmpty()) return;

        // ── Paso 2: una sola foto → ShareUtils con preselección de WhatsApp ──
        if (uris.size() == 1) {
            File archivoUnico = new File(seleccionadas.get(0).rutaArchivo);
            // shareImageViaWhatsApp() ya maneja el fallback al chooser si
            // WhatsApp no está instalado (internamente usa isAppInstalled)
            ShareUtils.shareImageViaWhatsApp(requireContext(), archivoUnico);
            finalizarModoSeleccion();
            return;
        }

        // ── Paso 3: varias fotos → ACTION_SEND_MULTIPLE con WhatsApp ─────────
        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        intent.setType("image/jpeg");
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // Preselección de WhatsApp si está instalado
        if (isAppInstalada(WHATSAPP_PACKAGE)) {
            // Lanzar directamente en WhatsApp sin chooser
            intent.setPackage(WHATSAPP_PACKAGE);
            startActivity(intent);
        } else if (isAppInstalada(WHATSAPP_BUSINESS_PACKAGE)) {
            // Fallback a WhatsApp Business
            intent.setPackage(WHATSAPP_BUSINESS_PACKAGE);
            startActivity(intent);
        } else {
            // Ninguna versión de WhatsApp instalada → chooser genérico
            startActivity(Intent.createChooser(intent, "Compartir fotos"));
        }

        finalizarModoSeleccion();
    }

    // ─── Eliminar ─────────────────────────────────────────────────────────────

    private void confirmarEliminar() {
        int count = adapter.getSeleccionCount();
        new AlertDialog.Builder(requireContext())
                .setTitle("Eliminar fotos")
                .setMessage("¿Eliminar " + count
                        + " foto" + (count == 1 ? "" : "s") + "?\n"
                        + "Los gastos se mantendrán.")
                .setPositiveButton("Eliminar", (d, w) -> {
                    viewModel.eliminarFotos(adapter.getSeleccionadas());
                    finalizarModoSeleccion();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    /**
     * Comprueba si una app está instalada en el dispositivo.
     * En Android 11+ requiere declarar el paquete en <queries> del Manifest.
     */
    private boolean isAppInstalada(String packageName) {
        try {
            requireContext().getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (android.content.pm.PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    // ─── Destrucción ─────────────────────────────────────────────────────────

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (actionMode != null) {
            actionMode.finish();
            actionMode = null;
        }
        binding = null;
    }
}
