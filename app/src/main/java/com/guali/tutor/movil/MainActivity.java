package com.guali.tutor.movil;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * Pantalla principal de Guali Tutor (front end móvil). Reúne los cinco elementos de la
 * "Interfaz Principal": barra de menú, título de lección activa, panel de lección activa,
 * editor de código y panel de consola, más los botones Compilar y Ejecutar.
 *
 * Por ahora la compilación y la ejecución son simuladas: esta fase solo construye la interfaz.
 */
public class MainActivity extends AppCompatActivity {

    private TextView tvTituloLeccion;
    private TextView tvDescripcion;
    private TextView tvConsola;
    private ScrollView svConsola;
    private EditText etCodigo;
    private MaterialButton btnCompilar;
    private MaterialButton btnEjecutar;

    /** Ruta del JDK escrita en el diálogo "Configurar JDK" (por ahora solo se guarda en memoria). */
    private String rutaJdk = "";

    /** true si la compilación más reciente fue exitosa: solo entonces se habilita Ejecutar. */
    private boolean compilacionExitosa = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Deja el contenido fuera de las barras del sistema, de la cámara frontal (notch) y del teclado.
        View raiz = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(raiz, (vista, insets) -> {
            Insets margenes = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                            | WindowInsetsCompat.Type.displayCutout()
                            | WindowInsetsCompat.Type.ime());
            vista.setPadding(margenes.left, margenes.top, margenes.right, margenes.bottom);
            return insets;
        });

        enlazarVistas();
        configurarMenu();
        configurarBotones();
        configurarEditor();

        // Solo la primera vez: al rotar la pantalla Android restaura los textos guardados.
        if (savedInstanceState == null) {
            registrarConsola(getString(R.string.consola_inicial));
            abrirLeccionDemo();
        }
    }

    // ------------------------------------------------------------------
    // Configuración inicial
    // ------------------------------------------------------------------

    private void enlazarVistas() {
        tvTituloLeccion = findViewById(R.id.tvTituloLeccion);
        tvDescripcion = findViewById(R.id.tvDescripcion);
        tvConsola = findViewById(R.id.tvConsola);
        svConsola = findViewById(R.id.svConsola);
        etCodigo = findViewById(R.id.etCodigo);
        btnCompilar = findViewById(R.id.btnCompilar);
        btnEjecutar = findViewById(R.id.btnEjecutar);
    }

    private void configurarMenu() {
        MaterialToolbar barra = findViewById(R.id.toolbar);
        barra.inflateMenu(R.menu.menu_principal);
        barra.setOnMenuItemClickListener(this::alSeleccionarMenu);
    }

    private void configurarBotones() {
        btnCompilar.setOnClickListener(vista -> alPulsarCompilar());
        btnEjecutar.setOnClickListener(vista -> alPulsarEjecutar());
    }

    private void configurarEditor() {
        // Si el estudiante modifica el código, la compilación anterior deja de valer.
        etCodigo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (compilacionExitosa) {
                    compilacionExitosa = false;
                    actualizarBotonEjecutar();
                }
            }
        });
    }

    // ------------------------------------------------------------------
    // Menú
    // ------------------------------------------------------------------

    private boolean alSeleccionarMenu(MenuItem opcion) {
        int id = opcion.getItemId();
        if (id == R.id.accion_abrir_leccion) {
            abrirLeccionDemo();
            return true;
        } else if (id == R.id.accion_cerrar_leccion) {
            cerrarLeccion();
            return true;
        } else if (id == R.id.accion_configurar_jdk) {
            mostrarDialogoJdk();
            return true;
        } else if (id == R.id.accion_ver_historial) {
            mostrarHistorial();
            return true;
        } else if (id == R.id.accion_ayuda) {
            mostrarAyuda();
            return true;
        }
        return false;
    }

    // ------------------------------------------------------------------
    // Lecciones
    // ------------------------------------------------------------------

    private boolean hayLeccionAbierta() {
        return !getString(R.string.sin_leccion).contentEquals(tvTituloLeccion.getText());
    }

    private void abrirLeccionDemo() {
        String nombre = getString(R.string.leccion_demo_nombre);
        tvTituloLeccion.setText(nombre);
        tvDescripcion.setText(R.string.leccion_demo_descripcion);
        etCodigo.setText("");
        registrarConsola(getString(R.string.log_leccion_abierta, nombre));
    }

    private void cerrarLeccion() {
        tvTituloLeccion.setText(R.string.sin_leccion);
        tvDescripcion.setText("");
        etCodigo.setText("");
        registrarConsola(getString(R.string.log_leccion_cerrada));
    }

    // ------------------------------------------------------------------
    // Botones de acción (simulados)
    // ------------------------------------------------------------------

    private void alPulsarCompilar() {
        compilacionExitosa = false;
        if (!hayLeccionAbierta()) {
            registrarConsola(getString(R.string.log_sin_leccion));
        } else if (etCodigo.getText().toString().trim().isEmpty()) {
            registrarConsola(getString(R.string.log_editor_vacio));
        } else {
            compilacionExitosa = true;
            registrarConsola(getString(R.string.log_compilacion_ok));
        }
        actualizarBotonEjecutar();
    }

    private void alPulsarEjecutar() {
        if (compilacionExitosa) {
            registrarConsola(getString(R.string.log_ejecucion_ok));
        }
    }

    private void actualizarBotonEjecutar() {
        btnEjecutar.setEnabled(compilacionExitosa);
    }

    // ------------------------------------------------------------------
    // Diálogos
    // ------------------------------------------------------------------

    private void mostrarDialogoJdk() {
        final EditText campo = new EditText(this);
        campo.setSingleLine(true);
        campo.setHint(R.string.dialogo_jdk_hint);
        campo.setText(rutaJdk);

        // Contenedor con márgenes para que el campo no quede pegado a los bordes del diálogo.
        int margen = (int) (20 * getResources().getDisplayMetrics().density);
        FrameLayout contenedor = new FrameLayout(this);
        FrameLayout.LayoutParams parametros = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        parametros.setMargins(margen, margen / 2, margen, 0);
        campo.setLayoutParams(parametros);
        contenedor.addView(campo);

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialogo_jdk_titulo)
                .setView(contenedor)
                .setPositiveButton(R.string.aceptar, (dialogo, boton) -> {
                    rutaJdk = campo.getText().toString().trim();
                    if (rutaJdk.isEmpty()) {
                        registrarConsola(getString(R.string.log_jdk_vacio));
                    } else {
                        registrarConsola(getString(R.string.log_jdk_guardado, rutaJdk));
                    }
                })
                .setNegativeButton(R.string.cancelar, null)
                .show();
    }

    private void mostrarHistorial() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.historial_titulo)
                .setMessage(R.string.historial_ejemplo)
                .setPositiveButton(R.string.cerrar, null)
                .show();
    }

    private void mostrarAyuda() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.ayuda_titulo)
                .setMessage(R.string.ayuda_texto)
                .setPositiveButton(R.string.cerrar, null)
                .show();
    }

    // ------------------------------------------------------------------
    // Consola
    // ------------------------------------------------------------------

    private void registrarConsola(String mensaje) {
        tvConsola.append(mensaje + "\n");
        // Desplaza la consola hasta la última línea.
        svConsola.post(() -> svConsola.fullScroll(View.FOCUS_DOWN));
    }
}
