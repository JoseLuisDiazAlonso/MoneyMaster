package com.example.moneymaster.ui.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.moneymaster.R;
import com.example.moneymaster.data.model.CategoriaIngreso;
import com.example.moneymaster.ui.categories.CategoryAdapter;

import java.util.ArrayList;
import java.util.List;

public class IncomeDropdownAdapter extends ArrayAdapter<CategoriaIngreso> {

    private final List<CategoriaIngreso> allCategories = new ArrayList<>();
    private final LayoutInflater         inflater;

    private final Filter noOpFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            results.values = allCategories;
            results.count  = allCategories.size();
            return results;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void publishResults(CharSequence constraint, FilterResults results) {
            clear();
            if (results.values != null) {
                addAll((List<CategoriaIngreso>) results.values);
            }
            notifyDataSetChanged();
        }

        @Override
        public CharSequence convertResultToString(Object resultValue) {
            if (resultValue instanceof CategoriaIngreso) {
                // FIX: resolver clave al nombre traducido
                return CategoryAdapter.resolverNombre(
                        getContext(), ((CategoriaIngreso) resultValue).nombre);
            }
            return super.convertResultToString(resultValue);
        }
    };

    public IncomeDropdownAdapter(@NonNull Context context) {
        super(context, R.layout.item_category_dropdown, new ArrayList<>());
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return noOpFilter;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return buildView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView,
                                @NonNull ViewGroup parent) {
        return buildView(position, convertView, parent);
    }

    private View buildView(int position, @Nullable View convertView,
                           @NonNull ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView       = inflater.inflate(R.layout.item_category_dropdown, parent, false);
            holder            = new ViewHolder();
            holder.iconFrame  = convertView.findViewById(R.id.view_icon_bg);
            holder.icon       = convertView.findViewById(R.id.img_category_icon);
            holder.name       = convertView.findViewById(R.id.tv_category_name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        CategoriaIngreso cat = getItem(position);
        if (cat == null) return convertView;

        // FIX: resolver clave al nombre traducido
        holder.name.setText(CategoryAdapter.resolverNombre(getContext(), cat.nombre));

        int color = ContextCompat.getColor(getContext(), R.color.income_green);
        holder.iconFrame.setBackgroundTintList(ColorStateList.valueOf(color));

        if (cat.icono != null && !cat.icono.isEmpty()) {
            int resId = getContext().getResources().getIdentifier(
                    cat.icono, "drawable", getContext().getPackageName());
            if (resId != 0) {
                Drawable drawable = ContextCompat.getDrawable(getContext(), resId);
                holder.icon.setImageDrawable(drawable);
                holder.icon.setColorFilter(
                        ContextCompat.getColor(getContext(), android.R.color.white));
            } else {
                holder.icon.setImageResource(R.drawable.ic_category_default);
                holder.icon.setColorFilter(
                        ContextCompat.getColor(getContext(), android.R.color.white));
            }
        }

        return convertView;
    }

    public void updateCategories(@NonNull List<CategoriaIngreso> newCategories) {
        allCategories.clear();
        allCategories.addAll(newCategories);
        clear();
        addAll(newCategories);
        notifyDataSetChanged();
    }

    private static class ViewHolder {
        View      iconFrame;
        ImageView icon;
        TextView  name;
    }
}