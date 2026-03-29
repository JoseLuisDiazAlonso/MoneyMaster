package com.example.moneymaster.ui.search;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.widget.SearchView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;

import com.example.moneymaster.R;

/**
 * SearchBarHelper — Card #58
 *
 * Utilidad para añadir un SearchView a un MaterialToolbar y conectarlo
 * con BusquedaViewModel de forma sencilla.
 *
 * Uso en Fragment:
 *
 *   SearchBarHelper searchHelper = new SearchBarHelper(
 *       binding.toolbar,
 *       query -> busquedaViewModel.setQuery(query)
 *   );
 *   searchHelper.setup();
 *
 *   // Para mostrar badge con número de filtros activos:
 *   busquedaViewModel.getFiltroActual().observe(getViewLifecycleOwner(), filtro ->
 *       searchHelper.actualizarBadgeFiltros(filtro.contarFiltrosActivos()));
 *
 *   // Para limpiar desde código:
 *   searchHelper.limpiar();
 */
public class SearchBarHelper {

    public interface OnQueryChangedListener {
        void onQueryChanged(String query);
    }

    private final MaterialToolbar        toolbar;
    private final OnQueryChangedListener queryListener;
    private SearchView                   searchView;

    public SearchBarHelper(MaterialToolbar toolbar, OnQueryChangedListener listener) {
        this.toolbar       = toolbar;
        this.queryListener = listener;
    }

    /** Configura el SearchView en el toolbar. Llamar en onViewCreated(). */
    public void setup() {
        // Inflar el SearchView desde el menu o añadirlo directamente
        toolbar.inflateMenu(R.menu.menu_search);

        android.view.MenuItem searchItem = toolbar.getMenu().findItem(R.id.action_search);
        if (searchItem == null) return;

        searchView = (SearchView) searchItem.getActionView();
        if (searchView == null) return;

        searchView.setQueryHint("Buscar gasto...");
        searchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        searchView.setMaxWidth(Integer.MAX_VALUE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (queryListener != null) queryListener.onQueryChanged(query.trim());
                ocultarTeclado();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (queryListener != null) queryListener.onQueryChanged(newText.trim());
                return true;
            }
        });

        // Limpiar query al cerrar el SearchView
        searchItem.setOnActionExpandListener(new android.view.MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(android.view.MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(android.view.MenuItem item) {
                if (queryListener != null) queryListener.onQueryChanged("");
                return true;
            }
        });
    }

    /** Actualiza el badge numérico en el icono de filtros del toolbar. */
    public void actualizarBadgeFiltros(int count) {
        android.view.MenuItem filterItem = toolbar.getMenu().findItem(R.id.action_filter);
        if (filterItem == null) return;

        // BadgeDrawable requiere API 21+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            BadgeDrawable badge = BadgeDrawable.create(toolbar.getContext());
            if (count > 0) {
                badge.setNumber(count);
                badge.setVisible(true);
                BadgeUtils.attachBadgeDrawable(badge, toolbar, filterItem.getItemId());
            } else {
                badge.setVisible(false);
                BadgeUtils.detachBadgeDrawable(badge, toolbar, filterItem.getItemId());
            }
        }
    }

    /** Limpia el texto del SearchView y notifica query vacío. */
    public void limpiar() {
        if (searchView != null) {
            searchView.setQuery("", false);
        }
    }

    private void ocultarTeclado() {
        if (searchView == null) return;
        InputMethodManager imm = (InputMethodManager)
                toolbar.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
        }
    }
}