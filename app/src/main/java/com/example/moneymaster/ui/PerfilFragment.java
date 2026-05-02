package com.example.moneymaster.ui;

import android.content.Context;
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
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.moneymaster.R;
import com.example.moneymaster.data.database.AppDatabase;
import com.example.moneymaster.data.model.User;
import com.example.moneymaster.ui.auth.LoginActivity;
import com.example.moneymaster.utils.BackupManager;
import com.example.moneymaster.utils.ResetManager;
import com.example.moneymaster.utils.SecurityUtils;
import com.example.moneymaster.utils.SessionManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class PerfilFragment extends Fragment {

    private TextView tvNombreActual;
    private TextView tvEmailActual;
    private TextView tvVersionActual;

    private SessionManager sessionManager;

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
        sessionManager = new SessionManager(requireContext());
        bindViews(view);
        cargarDatosUsuario();
        cargarVersion();
        configurarSwitchDarkMode(view);
        configurarClickListeners(view);
    }

    private void bindViews(View view) {
        tvNombreActual  = view.findViewById(R.id.tvNombreActual);
        tvEmailActual   = view.findViewById(R.id.tvEmailActual);
        tvVersionActual = view.findViewById(R.id.tvVersionActual);
    }

    // Modo oscuro

    private void configurarSwitchDarkMode(View view) {
        SharedPreferences prefs = requireContext()
                .getSharedPreferences("MoneyMasterPrefs", Context.MODE_PRIVATE);

        SwitchMaterial switchDark = view.findViewById(R.id.switchDarkMode);

        // Marcar el switch según la preferencia guardada
        switchDark.setChecked(prefs.getBoolean("dark_mode", false));

        // Reaccionar al cambio
        switchDark.setOnCheckedChangeListener((btn, isChecked) -> {
            prefs.edit().putBoolean("dark_mode", isChecked).apply();
            AppCompatDelegate.setDefaultNightMode(
                    isChecked ? AppCompatDelegate.MODE_NIGHT_YES
                            : AppCompatDelegate.MODE_NIGHT_NO
            );
        });
    }

    // Datos de usuario

    private void cargarDatosUsuario() {
        long userId = sessionManager.getUserId();
        if (userId == -1) return;

        AppDatabase.databaseWriteExecutor.execute(() -> {
            User user = AppDatabase.getDatabase(requireContext())
                    .usuarioDao().getById((int) userId);
            if (user != null && isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    tvNombreActual.setText(user.fullName);
                    tvEmailActual.setText(user.email);
                });
            }
        });
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

    // Click listeners

    private void configurarClickListeners(View view) {
        view.findViewById(R.id.itemNombre)
                .setOnClickListener(v -> mostrarDialogEditarNombre());
        view.findViewById(R.id.itemCambiarPassword)
                .setOnClickListener(v -> mostrarDialogCambiarPassword());
        view.findViewById(R.id.itemEliminarDatos)
                .setOnClickListener(v -> mostrarDialogEliminarDatos());
        view.findViewById(R.id.itemCerrarSesion)
                .setOnClickListener(v -> mostrarDialogCerrarSesion());
    }

    // Backup / Restore

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

    // Eliminar datos

    private void mostrarDialogEliminarDatos() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_confirmar_eliminar, null);

        CheckBox cbConfirmar = dialogView.findViewById(R.id.cbConfirmarEliminar);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Eliminar todos los datos")
                .setView(dialogView)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Eliminar todo", null)
                .create();

        dialog.setOnShowListener(d -> {
            Button btnEliminar = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
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
                    sessionManager.clearSession();
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

    //Editar nombre

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
        long userId = sessionManager.getUserId();
        if (userId == -1) return;
        AppDatabase.databaseWriteExecutor.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(requireContext());
            User user = db.usuarioDao().getById((int) userId);
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

    // Cambiar contraseña

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
        long userId = sessionManager.getUserId();
        if (userId == -1) return;
        AppDatabase.databaseWriteExecutor.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(requireContext());
            User user = db.usuarioDao().getById((int) userId);
            if (user == null) return;
            boolean valida = SecurityUtils.verifyPassword(actual, user.passwordHash);
            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                if (!valida) { tilActual.setError("Contraseña actual incorrecta"); return; }
                AppDatabase.databaseWriteExecutor.execute(() -> {
                    db.usuarioDao().updatePasswordByEmail(
                            user.email, SecurityUtils.hashPassword(nueva));
                    if (isAdded()) requireActivity().runOnUiThread(() -> {
                        dialog.dismiss();
                        Toast.makeText(requireContext(),
                                "Contraseña actualizada", Toast.LENGTH_SHORT).show();
                    });
                });
            });
        });
    }

    //  Cerrar sesión

    private void mostrarDialogCerrarSesion() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Cerrar sesión")
                .setMessage("¿Seguro que quieres cerrar sesión?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Cerrar sesión", (d, w) -> cerrarSesion())
                .show();
    }

    private void cerrarSesion() {
        sessionManager.clearSession();
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    // Reiniciar app

    private void reiniciarApp() {
        Intent intent = requireContext().getPackageManager()
                .getLaunchIntentForPackage(requireContext().getPackageName());
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            requireContext().startActivity(intent);
        }
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}