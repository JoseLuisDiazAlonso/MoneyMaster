package com.example.moneymaster.ui.groups.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.moneymaster.R;
import com.example.moneymaster.data.model.FotoRecibo;
import com.example.moneymaster.databinding.ItemPhotoBoardBinding;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Card #34 — Adapter del tablón de fotos en cuadrícula 3 columnas.
 *
 * Soporta:
 *   - Click normal: abrir viewer
 *   - Long-press: activar selección múltiple
 *   - Selección visual con overlay y checkmark
 */
public class PhotoBoardAdapter
        extends ListAdapter<FotoRecibo, PhotoBoardAdapter.PhotoViewHolder> {

    // ─── Interfaces ───────────────────────────────────────────────────────────

    public interface OnFotoClickListener {
        void onFotoClick(FotoRecibo foto);
    }

    public interface OnFotoLongClickListener {
        boolean onFotoLongClick(FotoRecibo foto);
    }

    // ─── DiffUtil ─────────────────────────────────────────────────────────────

    private static final DiffUtil.ItemCallback<FotoRecibo> DIFF =
            new DiffUtil.ItemCallback<FotoRecibo>() {
                @Override
                public boolean areItemsTheSame(@NonNull FotoRecibo a, @NonNull FotoRecibo b) {
                    return a.id == b.id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull FotoRecibo a, @NonNull FotoRecibo b) {
                    return a.id == b.id
                            && strEq(a.rutaArchivo, b.rutaArchivo);
                }

                private boolean strEq(String a, String b) {
                    if (a == null && b == null) return true;
                    if (a == null || b == null) return false;
                    return a.equals(b);
                }
            };

    // ─── Estado ───────────────────────────────────────────────────────────────

    private final OnFotoClickListener     clickListener;
    private final OnFotoLongClickListener longClickListener;
    private boolean                       selectionMode = false;
    private final Set<Integer>            seleccionados = new HashSet<>();

    // ─── Constructor ─────────────────────────────────────────────────────────

    public PhotoBoardAdapter(OnFotoClickListener clickListener,
                             OnFotoLongClickListener longClickListener) {
        super(DIFF);
        this.clickListener     = clickListener;
        this.longClickListener = longClickListener;
    }

    // ─── API selección ────────────────────────────────────────────────────────

    public void setSelectionMode(boolean active) {
        this.selectionMode = active;
        if (!active) seleccionados.clear();
        notifyDataSetChanged();
    }

    public boolean isSelectionMode() { return selectionMode; }

    public void toggleSeleccion(int fotoId) {
        if (seleccionados.contains(fotoId)) {
            seleccionados.remove(fotoId);
        } else {
            seleccionados.add(fotoId);
        }
        notifyDataSetChanged();
    }

    public void limpiarSeleccion() {
        seleccionados.clear();
        notifyDataSetChanged();
    }

    public int getSeleccionCount() { return seleccionados.size(); }

    public List<FotoRecibo> getSeleccionadas() {
        List<FotoRecibo> result = new ArrayList<>();
        for (int i = 0; i < getCurrentList().size(); i++) {
            FotoRecibo f = getItem(i);
            if (seleccionados.contains(f.id)) result.add(f);
        }
        return result;
    }

    // ─── Ciclo de vida ────────────────────────────────────────────────────────

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PhotoViewHolder(ItemPhotoBoardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        holder.bind(getItem(position),
                clickListener, longClickListener,
                selectionMode, seleccionados.contains(getItem(position).id));
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ViewHolder
    // ═════════════════════════════════════════════════════════════════════════

    static class PhotoViewHolder extends RecyclerView.ViewHolder {

        private final ItemPhotoBoardBinding binding;

        PhotoViewHolder(ItemPhotoBoardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(FotoRecibo foto,
                  OnFotoClickListener clickListener,
                  OnFotoLongClickListener longClickListener,
                  boolean selectionMode,
                  boolean seleccionado) {

            // ── Cargar miniatura cuadrada con Glide ───────────────────────
            Glide.with(binding.ivFoto.getContext())
                    .load(foto.rutaArchivo)
                    .apply(new RequestOptions()
                            .transform(new CenterCrop(), new RoundedCorners(8))
                            .placeholder(R.drawable.ic_receipt_long)
                            .error(R.drawable.ic_receipt_long))
                    .into(binding.ivFoto);

            // ── Overlay de selección ──────────────────────────────────────
            binding.viewSelectionOverlay.setVisibility(
                    seleccionado ? View.VISIBLE : View.GONE);
            binding.ivCheckmark.setVisibility(
                    seleccionado ? View.VISIBLE : View.GONE);

            // ── Clicks ────────────────────────────────────────────────────
            binding.getRoot().setOnClickListener(v ->
                    clickListener.onFotoClick(foto));

            binding.getRoot().setOnLongClickListener(v ->
                    longClickListener.onFotoLongClick(foto));
        }
    }
}
