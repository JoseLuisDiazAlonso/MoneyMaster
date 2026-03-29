package com.example.moneymaster.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.moneymaster.R;
import com.example.moneymaster.data.database.AppDatabase;
import com.example.moneymaster.data.model.User;
import com.example.moneymaster.ui.auth.LoginActivity;
import com.example.moneymaster.utils.BackupManager;
import com.example.moneymaster.utils.CurrencyUtils;
import com.example.moneymaster.utils.ResetManager;
import com.example.moneymaster.utils.SecurityUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Arrays;

public class PerfilFragment extends Fragment {

    // ── SharedPreferences ─────────────────────────────────────────────────────
    private static final String PREFS_NAME       = "MoneyMasterPrefs";
    private static final String KEY_USER_ID      = "userId";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_REMEMBER_ME  = "rememberMe";

    private static final String PREFS_CONFIG = "moneymaster_config";
    private static final String KEY_IDIOMA   = "idioma_nombre";

    // ── Vistas ────────────────────────────────────────────────────────────────
    private TextView tvNombreActual;
    private TextView tvEmailActual;
    private TextView tvMonedaActual;
    private TextView tvIdiomaActual;
    private TextView tvVersionActual;

    // ── Launcher SAF para restore ─────────────────────────────────────────────
    private final ActivityResultLauncher<String[]> pickFileLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.OpenDocument(),
                    uri -> { if (uri != null) confirmarRestore(uri); });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);
        cargarDatosUsuario();
        cargarPreferencias();
        cargarVersion();
        configurarClickListeners(view);
    }

    // ── Binding ───────────────────────────────────────────────────────────────
    private void bindViews(View view) {
        tvNombreActual  = view.findViewById(R.id.tvNombreActual);
        tvEmailActual   = view.findViewById(R.id.tvEmailActual);
        tvMonedaActual  = view.findViewById(R.id.tvMonedaActual);
        tvIdiomaActual  = view.findViewById(R.id.tvIdiomaActual);
        tvVersionActual = view.findViewById(R.id.tvVersionActual);
    }

    // ── Carga de datos ────────────────────────────────────────────────────────
    private void cargarDatosUsuario() {
        int userId = obtenerUsuarioId();
        if (userId == -1) return;

        AppDatabase.databaseWriteExecutor.execute(() -> {
            User user = AppDatabase.getDatabase(requireContext())
                    .usuarioDao().getById(userId);
            if (user != null && isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    tvNombreActual.setText(user.fullName);
                    tvEmailActual.setText(user.email);
                });
            }
        });
    }

    private void cargarPreferencias() {
        String simbolo = CurrencyUtils.getCurrencySymbol(requireContext());
        String nombre  = CurrencyUtils.getCurrencyName(requireContext());
        tvMonedaActual.setText(simbolo + " " + nombre);

        String idioma = requireContext()
                .getSharedPreferences(PREFS_CONFIG, 0)
                .getString(KEY_IDIOMA, "Español");
        tvIdiomaActual.setText(idioma);
    }

    private void cargarVersion() {
        try {
            PackageInfo pInfo = requireContext().getPackageManager()
                    .getPackageInfo(requireContext().getPackageName(), 0);
            tvVersionActual.setText(pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            tvVersionActual.setText("1.0.0");
        }
    }

    // ── Click listeners ───────────────────────────────────────────────────────
    private void configurarClickListeners(View view) {
        view.findViewById(R.id.itemNombre)
                .setOnClickListener(v -> mostrarDialogEditarNombre());
        view.findViewById(R.id.itemCambiarPassword)
                .setOnClickListener(v -> mostrarDialogCambiarPassword());
        view.findViewById(R.id.itemMoneda)
                .setOnClickListener(v -> mostrarDialogMoneda());
        view.findViewById(R.id.itemIdioma)
                .setOnClickListener(v -> mostrarDialogIdioma());
        view.findViewById(R.id.itemCrearBackup)
                .setOnClickListener(v -> ejecutarBackup());
        view.findViewById(R.id.itemRestaurarBackup)
                .setOnClickListener(v -> abrirSelectorArchivo());
        view.findViewById(R.id.itemEliminarDatos)
                .setOnClickListener(v -> mostrarDialogEliminarDatos());
        view.findViewById(R.id.itemCerrarSesion)
                .setOnClickListener(v -> mostrarDialogCerrarSesion());
        view.findViewById(R.id.itemContacto)
                .setOnClickListener(v -> abrirContacto());
    }

    // ── Backup ────────────────────────────────────────────────────────────────
    private void ejecutarBackup() {
        Toast.makeText(requireContext(), "Creando backup...", Toast.LENGTH_SHORT).show();
        AppDatabase.databaseWriteExecutor.execute(() -> {
            String ruta = BackupManager.createBackup(requireContext());
            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                if (ruta != null) {
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Backup creado")
                            .setMessage("Copia guardada en:\n" + ruta)
                            .setPositiveButton("Aceptar", null)
                            .show();
                } else {
                    Toast.makeText(requireContext(),
                            "Error al crear el backup", Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    // ── Restore ───────────────────────────────────────────────────────────────
    private void abrirSelectorArchivo() {
        pickFileLauncher.launch(new String[]{"application/octet-stream", "*/*"});
    }

    private void confirmarRestore(Uri uri) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Restaurar backup")
                .setMessage("Se sobrescribirá toda la base de datos actual y la app se reiniciará. ¿Continuar?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Restaurar", (d, w) -> ejecutarRestore(uri))
                .show();
    }

    private void ejecutarRestore(Uri uri) {
        Toast.makeText(requireContext(), "Restaurando backup...", Toast.LENGTH_SHORT).show();
        AppDatabase.databaseWriteExecutor.execute(() -> {
            boolean ok = BackupManager.restoreBackup(requireContext(), uri);
            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                if (ok) reiniciarApp();
                else Toast.makeText(requireContext(),
                        "Error al restaurar el backup", Toast.LENGTH_LONG).show();
            });
        });
    }

    // ── Eliminar todos los datos (Card #53) ───────────────────────────────────
    private void mostrarDialogEliminarDatos() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_confirmar_eliminar, null);

        CheckBox cbConfirmar = dialogView.findViewById(R.id.cbConfirmarEliminar);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Eliminar todos los datos")
                .setView(dialogView)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Eliminar todo", null)   // null: evita cierre automático
                .create();

        dialog.setOnShowListener(d -> {
            Button btnEliminar = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

            // El botón empieza desactivado hasta que se marque el checkbox
            btnEliminar.setEnabled(false);
            btnEliminar.setAlpha(0.4f);

            cbConfirmar.setOnCheckedChangeListener((btn, checked) -> {
                btnEliminar.setEnabled(checked);
                btnEliminar.setAlpha(checked ? 1f : 0.4f);
            });

            btnEliminar.setOnClickListener(v -> {
                dialog.dismiss();
                ejecutarResetCompleto();
            });
        });

        dialog.show();
    }

    private void ejecutarResetCompleto() {
        Toast.makeText(requireContext(),
                "Eliminando datos...", Toast.LENGTH_SHORT).show();

        AppDatabase.databaseWriteExecutor.execute(() -> {
            boolean ok = ResetManager.resetApp(requireContext());
            if (!isAdded()) return;

            requireActivity().runOnUiThread(() -> {
                if (ok) {
                    // Limpiar sesión y volver a LoginActivity
                    limpiarSesion();
                    Intent intent = new Intent(requireContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                } else {
                    Toast.makeText(requireContext(),
                            "Error al eliminar los datos", Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    // ── Diálogo: editar nombre ────────────────────────────────────────────────
    private void mostrarDialogEditarNombre() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_editar_nombre, null);
        TextInputEditText etNombre  = dialogView.findViewById(R.id.etNuevoNombre);
        TextInputLayout   tilNombre = dialogView.findViewById(R.id.tilNuevoNombre);
        etNombre.setText(tvNombreActual.getText());

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Editar nombre")
                .setView(dialogView)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Guardar", (d, w) -> {
                    String nuevoNombre = etNombre.getText() != null
                            ? etNombre.getText().toString().trim() : "";
                    if (nuevoNombre.isEmpty()) {
                        tilNombre.setError("El nombre no puede estar vacío");
                        return;
                    }
                    guardarNombre(nuevoNombre);
                })
                .show();
    }

    private void guardarNombre(String nuevoNombre) {
        int userId = obtenerUsuarioId();
        if (userId == -1) return;
        AppDatabase.databaseWriteExecutor.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(requireContext());
            User user = db.usuarioDao().getById(userId);
            if (user != null) {
                user.fullName = nuevoNombre;
                db.usuarioDao().updateUser(user);
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        tvNombreActual.setText(nuevoNombre);
                        Toast.makeText(requireContext(),
                                "Nombre actualizado", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    // ── Diálogo: cambiar contraseña ───────────────────────────────────────────
    private void mostrarDialogCambiarPassword() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_cambiar_password, null);

        TextInputEditText etActual     = dialogView.findViewById(R.id.etPasswordActual);
        TextInputEditText etNueva      = dialogView.findViewById(R.id.etPasswordNueva);
        TextInputEditText etConfirmar  = dialogView.findViewById(R.id.etPasswordConfirmar);
        TextInputLayout   tilActual    = dialogView.findViewById(R.id.tilPasswordActual);
        TextInputLayout   tilNueva     = dialogView.findViewById(R.id.tilPasswordNueva);
        TextInputLayout   tilConfirmar = dialogView.findViewById(R.id.tilPasswordConfirmar);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Cambiar contraseña")
                .setView(dialogView)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Guardar", null)
                .create();

        dialog.setOnShowListener(d ->
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                    tilActual.setError(null);
                    tilNueva.setError(null);
                    tilConfirmar.setError(null);

                    String actual    = etActual.getText()    != null ? etActual.getText().toString()    : "";
                    String nueva     = etNueva.getText()     != null ? etNueva.getText().toString()     : "";
                    String confirmar = etConfirmar.getText() != null ? etConfirmar.getText().toString() : "";

                    if (actual.isEmpty())         { tilActual.setError("Introduce la contraseña actual"); return; }
                    if (nueva.length() < 6)       { tilNueva.setError("Mínimo 6 caracteres"); return; }
                    if (!nueva.equals(confirmar)) { tilConfirmar.setError("Las contraseñas no coinciden"); return; }

                    cambiarPassword(actual, nueva, dialog, tilActual);
                })
        );
        dialog.show();
    }

    private void cambiarPassword(String actual, String nueva,
                                 AlertDialog dialog, TextInputLayout tilActual) {
        int userId = obtenerUsuarioId();
        if (userId == -1) return;
        AppDatabase.databaseWriteExecutor.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(requireContext());
            User user = db.usuarioDao().getById(userId);
            if (user == null) return;
            boolean valida = SecurityUtils.verifyPassword(actual, user.passwordHash);
            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                if (!valida) { tilActual.setError("Contraseña actual incorrecta"); return; }
                AppDatabase.databaseWriteExecutor.execute(() -> {
                    db.usuarioDao().updatePasswordByEmail(user.email, SecurityUtils.hashPassword(nueva));
                    if (isAdded()) requireActivity().runOnUiThread(() -> {
                        dialog.dismiss();
                        Toast.makeText(requireContext(),
                                "Contraseña actualizada", Toast.LENGTH_SHORT).show();
                    });
                });
            });
        });
    }

    // ── Diálogo: seleccionar moneda ───────────────────────────────────────────
    private void mostrarDialogMoneda() {
        int seleccionado    = CurrencyUtils.getCurrentIndex(requireContext());
        final int[] elegido = {seleccionado};
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Seleccionar moneda")
                .setSingleChoiceItems(CurrencyUtils.NOMBRES, seleccionado,
                        (d, which) -> elegido[0] = which)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Aceptar", (d, w) -> {
                    CurrencyUtils.saveCurrency(requireContext(),
                            CurrencyUtils.SIMBOLOS[elegido[0]],
                            CurrencyUtils.NOMBRES[elegido[0]]);
                    tvMonedaActual.setText(CurrencyUtils.SIMBOLOS[elegido[0]]
                            + " " + CurrencyUtils.NOMBRES[elegido[0]]);
                    Toast.makeText(requireContext(),
                            "Moneda actualizada", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    // ── Diálogo: seleccionar idioma ───────────────────────────────────────────
    private void mostrarDialogIdioma() {
        final String[] idiomas = {"Español", "English", "Français",
                "Deutsch", "Italiano", "Português"};
        String actual       = requireContext().getSharedPreferences(PREFS_CONFIG, 0)
                .getString(KEY_IDIOMA, "Español");
        int    seleccionado = Math.max(Arrays.asList(idiomas).indexOf(actual), 0);
        final int[] elegido = {seleccionado};
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Seleccionar idioma")
                .setSingleChoiceItems(idiomas, seleccionado, (d, which) -> elegido[0] = which)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Aceptar", (d, w) -> {
                    requireContext().getSharedPreferences(PREFS_CONFIG, 0).edit()
                            .putString(KEY_IDIOMA, idiomas[elegido[0]]).apply();
                    tvIdiomaActual.setText(idiomas[elegido[0]]);
                    Toast.makeText(requireContext(),
                            "Preferencia guardada", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    // ── Diálogo: cerrar sesión ────────────────────────────────────────────────
    private void mostrarDialogCerrarSesion() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Cerrar sesión")
                .setMessage("¿Seguro que quieres cerrar sesión?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Cerrar sesión", (d, w) -> cerrarSesion())
                .show();
    }

    private void cerrarSesion() {
        limpiarSesion();
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    // ── Contacto / soporte ────────────────────────────────────────────────────
    private void abrirContacto() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:soporte@moneymaster.app"));
        intent.putExtra(Intent.EXTRA_SUBJECT, "Soporte MoneyMaster");
        if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(requireContext(),
                    "No hay cliente de email disponible", Toast.LENGTH_SHORT).show();
        }
    }

    // ── Reinicio de app (tras restore) ────────────────────────────────────────
    private void reiniciarApp() {
        Intent intent = requireContext().getPackageManager()
                .getLaunchIntentForPackage(requireContext().getPackageName());
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            requireContext().startActivity(intent);
        }
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    // ── Utilidades ────────────────────────────────────────────────────────────
    private int obtenerUsuarioId() {
        return requireContext()
                .getSharedPreferences(PREFS_NAME, 0)
                .getInt(KEY_USER_ID, -1);
    }

    /** Limpia la sesión activa de SharedPreferences. */
    private void limpiarSesion() {
        requireContext().getSharedPreferences(PREFS_NAME, 0).edit()
                .putBoolean(KEY_IS_LOGGED_IN, false)
                .putBoolean(KEY_REMEMBER_ME, false)
                .remove(KEY_USER_ID)
                .apply();
    }
}