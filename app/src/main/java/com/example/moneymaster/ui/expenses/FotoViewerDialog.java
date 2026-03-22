package com.example.moneymaster.ui.expenses;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.example.moneymaster.R;
import com.example.moneymaster.databinding.DialogFotoViewerBinding;

/**
 * Card #32 — Dialog de vista ampliada de foto de recibo.
 *
 * Muestra la foto a pantalla completa con dos acciones:
 *   - Cerrar (X) — esquina superior derecha
 *   - Eliminar foto — FAB rojo esquina inferior derecha
 *
 * El gasto se mantiene al eliminar la foto.
 *
 * Uso desde un Fragment:
 *   FotoViewerDialog dialog = FotoViewerDialog.newInstance(rutaFoto, gastoId);
 *   dialog.setOnEliminarFotoListener(gastoId -> viewModel.eliminarFoto(gastoId));
 *   dialog.show(getChildFragmentManager(), "FotoViewer");
 *
 * Uso desde una Activity:
 *   dialog.show(getSupportFragmentManager(), "FotoViewer");
 */
public class FotoViewerDialog extends DialogFragment {

    private static final String ARG_RUTA     = "ruta_foto";
    private static final String ARG_GASTO_ID = "gasto_id";

    private DialogFotoViewerBinding binding;
    private OnEliminarFotoListener  eliminarListener;

    // ─── Interfaz callback ────────────────────────────────────────────────────

    public interface OnEliminarFotoListener {
        /**
         * Se llama cuando el usuario confirma eliminar la foto.
         * @param gastoId ID del gasto al que pertenece la foto.
         */
        void onEliminarFoto(int gastoId);
    }

    // ─── Factory method ───────────────────────────────────────────────────────

    /**
     * @param rutaFoto Ruta absoluta de la foto en el almacenamiento interno.
     * @param gastoId  ID del gasto propietario. Pasa -1 si solo quieres ver sin eliminar.
     */
    public static FotoViewerDialog newInstance(String rutaFoto, int gastoId) {
        FotoViewerDialog dialog = new FotoViewerDialog();
        Bundle args = new Bundle();
        args.putString(ARG_RUTA, rutaFoto);
        args.putInt(ARG_GASTO_ID, gastoId);
        dialog.setArguments(args);
        return dialog;
    }

    public void setOnEliminarFotoListener(OnEliminarFotoListener listener) {
        this.eliminarListener = listener;
    }

    // ─── Ciclo de vida ────────────────────────────────────────────────────────

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog.getWindow() != null) {
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DialogFotoViewerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String rutaFoto = getArguments() != null ? getArguments().getString(ARG_RUTA) : null;
        int    gastoId  = getArguments() != null ? getArguments().getInt(ARG_GASTO_ID, -1) : -1;

        cargarFoto(rutaFoto);
        configurarBtnCerrar();
        configurarBtnEliminar(gastoId);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Expandir el dialog a pantalla completa
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            getDialog().getWindow().setBackgroundDrawableResource(
                    android.R.color.black);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // ─── Métodos privados ─────────────────────────────────────────────────────

    private void cargarFoto(String rutaFoto) {
        if (rutaFoto != null) {
            Glide.with(this)
                    .load(rutaFoto)
                    .placeholder(R.drawable.ic_receipt_long)
                    .error(R.drawable.ic_receipt_long)
                    .into(binding.ivFotoAmpliada);
        } else {
            binding.ivFotoAmpliada.setImageResource(R.drawable.ic_receipt_long);
        }
    }

    private void configurarBtnCerrar() {
        binding.btnCerrarViewer.setOnClickListener(v -> dismiss());
    }

    private void configurarBtnEliminar(int gastoId) {
        // Si no hay gastoId válido, ocultamos el botón eliminar
        if (gastoId == -1) {
            binding.btnEliminarFotoViewer.setVisibility(View.GONE);
            return;
        }

        binding.btnEliminarFotoViewer.setOnClickListener(v ->
                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle("Eliminar foto")
                        .setMessage("¿Eliminar la foto del recibo?\nEl gasto se mantendrá.")
                        .setPositiveButton("Eliminar", (d, w) -> {
                            if (eliminarListener != null) {
                                eliminarListener.onEliminarFoto(gastoId);
                            }
                            dismiss();
                        })
                        .setNegativeButton("Cancelar", null)
                        .show()
        );
    }
}
