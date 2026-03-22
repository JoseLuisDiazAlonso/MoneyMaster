package com.example.moneymaster.ui.groups;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Card #34 — Fragment Tablón de Fotos del grupo.
 * Card #35 — abrirViewer() actualizado a ImageViewerActivity.
 */
public class PhotoBoardFragment extends Fragment {

    public static final String ARG_GRUPO_ID = "grupoId";

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
                // Click normal → ver foto ampliada
                foto -> {
                    if (adapter.isSelectionMode()) {
                        toggleSeleccion(foto);
                    } else {
                        abrirViewer(foto); // Card #35
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

    // ─── Viewer — Card #35 ────────────────────────────────────────────────────

    /**
     * Abre ImageViewerActivity pasando el ID y la ruta de la foto.
     * Sustituye FotoViewerDialog del Card #34.
     */
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

    // ─── Compartir ────────────────────────────────────────────────────────────

    private void compartirSeleccionadas() {
        List<FotoRecibo> seleccionadas = adapter.getSeleccionadas();
        if (seleccionadas.isEmpty()) return;

        ArrayList<Uri> uris = new ArrayList<>();
        String authority = requireContext().getPackageName() + ".fileprovider";

        for (FotoRecibo foto : seleccionadas) {
            File file = new File(foto.rutaArchivo);
            if (file.exists()) {
                try {
                    Uri uri = FileProvider.getUriForFile(
                            requireContext(), authority, file);
                    uris.add(uri);
                } catch (IllegalArgumentException e) {
                    // Ignorar archivos que FileProvider no puede exponer
                }
            }
        }

        if (uris.isEmpty()) return;

        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        intent.setType("image/jpeg");
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, "Compartir fotos"));

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
