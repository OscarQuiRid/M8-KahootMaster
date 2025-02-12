package com.example.kahootmaster;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.InputFilter;
import android.text.Spanned;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText inputID;
    private TextView textViewError;
    private Spinner spinnerDificultad;
    private Button buttonComezar;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar vistas
        TextInputLayout inputLayout = findViewById(R.id.inputID);
        inputID = (TextInputEditText) inputLayout.getEditText();
        textViewError = findViewById(R.id.textViewError);
        spinnerDificultad = findViewById(R.id.spinnerDificultad);
        buttonComezar = findViewById(R.id.buttonComezar);

        // Inicializar referencia a la base de datos
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference();

        // Cargar niveles de dificultad
        loadDificultadLevels();
        // Configurar validación de ID
        setupIDValidation();

        // Establecer filtro de entrada para permitir solo letras y números
        inputID.setFilters(new InputFilter[]{new AlphanumericInputFilter()});

        // Configurar el botón para comenzar el juego
        buttonComezar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("MainActivity", "Start Game button clicked");
                createGame();
            }
        });
    }

    // Cargar niveles de dificultad desde la base de datos
    private void loadDificultadLevels() {
        databaseReference.child("preguntas").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Set<String> levels = new HashSet<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    levels.add(snapshot.getKey());
                }
                Log.d("MainActivity", "Dificultad levels: " + levels);
                ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, R.layout.spinner_item, new ArrayList<>(levels));
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerDificultad.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("MainActivity", "Error loading dificultad levels", databaseError.toException());
            }
        });
    }

    // Configurar validación de ID
    private void setupIDValidation() {
        inputID.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkIDAvailability(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // Verificar disponibilidad del ID
    private void checkIDAvailability(final String id) {
        if (id.isEmpty()) {
            textViewError.setVisibility(View.GONE);
            return;
        }

        databaseReference.child("salas").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(id)) {
                    textViewError.setText("Nombre no disponible");
                    textViewError.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    textViewError.setVisibility(View.VISIBLE);
                } else {
                    textViewError.setText("Nombre disponible");
                    textViewError.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    textViewError.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("MainActivity", "Error checking ID availability", databaseError.toException());
            }
        });
    }

    // Crear un nuevo juego
    private void createGame() {
        String id = inputID.getText().toString();
        String dificultad = (spinnerDificultad.getSelectedItem() != null) ? spinnerDificultad.getSelectedItem().toString() : "";

        Log.d("MainActivity", "Creating game with ID: " + id + " and Dificultad: " + dificultad);

        if (!id.isEmpty() && !dificultad.isEmpty() && textViewError.getCurrentTextColor() == getResources().getColor(android.R.color.holo_green_dark)) {
            Map<String, Object> partida = new HashMap<>();
            partida.put("id", id);
            partida.put("dificultad", dificultad);

            // Initialize jugadores with a default value
            Map<String, Object> jugadores = new HashMap<>();
            jugadores.put("defaultPlayer", 0);
            partida.put("jugadores", jugadores);

            databaseReference.child("salas").child(id).setValue(partida).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d("MainActivity", "Game created successfully");
                    Intent intent = new Intent(MainActivity.this, JuegoActivity.class);
                    intent.putExtra("partidaId", id);
                    intent.putExtra("dificultad", dificultad);
                    startActivity(intent);
                } else {
                    Log.e("MainActivity", "Error creating game", task.getException());
                    textViewError.setText("Error al crear la partida");
                    textViewError.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    textViewError.setVisibility(View.VISIBLE);
                }
            });
        } else {
            textViewError.setText("ID or Dificultad is empty or invalid");
            textViewError.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            textViewError.setVisibility(View.VISIBLE);
        }
    }

    // Filtro de entrada para permitir solo letras y números
    private class AlphanumericInputFilter implements InputFilter {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            for (int i = start; i < end; i++) {
                if (!Character.isLetterOrDigit(source.charAt(i))) {
                    return "";
                }
            }
            return null;
        }
    }
}