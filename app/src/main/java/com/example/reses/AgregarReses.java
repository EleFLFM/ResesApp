package com.example.reses;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AgregarReses extends AppCompatActivity {
    private EditText Chapeta;
    private EditText Nombre;
    private EditText Padre;
    private EditText Madre;
    private RadioButton Macho;
    private EditText FechaNacimiento;
    private String cedula, tiporesesaux;

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_agregar_reses);

        // Inicializar Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("reses");

        Intent intent = getIntent();
        cedula = intent.getStringExtra("cedula");
        tiporesesaux = intent.getStringExtra("tiporeses");

        Chapeta = findViewById(R.id.chapetaEditText);
        Nombre = findViewById(R.id.nombreEditText);
        Padre = findViewById(R.id.padreEditText);
        Madre = findViewById(R.id.madreEditText);
        Macho = findViewById(R.id.machoButton);
        FechaNacimiento = findViewById(R.id.fechaEditText);

        FechaNacimiento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        v.getContext(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                                String fecha = selectedYear + "/" + String.format("%02d", (selectedMonth + 1)) + "/" + String.format("%02d", selectedDay);
                                FechaNacimiento.setText(fecha);
                            }
                        },
                        year, month, day
                );
                datePickerDialog.show();
            }
        });
    }

    public void GoBackListaBovino(View view) {
        Intent intent = new Intent(AgregarReses.this, Reses.class);
        intent.putExtra("cedula", cedula);
        intent.putExtra("tiporeses", tiporesesaux);
        startActivity(intent);
        finish();
    }

    public void RegistrarReses(View view) {
        // Validar campos obligatorios
        if (Nombre.getText().toString().isEmpty() ||
                Padre.getText().toString().isEmpty() ||
                Madre.getText().toString().isEmpty() ||
                FechaNacimiento.getText().toString().isEmpty()) {

            Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verificar fecha válida
        String fechaActual = java.time.LocalDate.now().toString().replace("-", "/");
        if (fechaActual.compareTo(FechaNacimiento.getText().toString()) < 0) {
            Toast.makeText(this, "Error, la fecha ingresada supera el día actual.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtener valores de los campos
        String chapeta = Chapeta.getText().toString().trim();
        String nombre = Nombre.getText().toString().trim();
        String padre = Padre.getText().toString().trim();
        String madre = Madre.getText().toString().trim();
        String sexo = Macho.isChecked() ? "Macho" : "Hembra";
        String fechaNacimiento = FechaNacimiento.getText().toString().trim();

        // Verificar si la chapeta ya existe para este usuario
        databaseReference.orderByChild("chapeta").equalTo(chapeta)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // La chapeta ya existe
                            Toast.makeText(AgregarReses.this, "La chapeta ya fue registrada.", Toast.LENGTH_SHORT).show();
                        } else {
                            // Crear un nuevo registro de res
                            Map<String, Object> resData = new HashMap<>();
                            resData.put("chapeta", chapeta);
                            resData.put("nombre", nombre);
                            resData.put("padre", padre);
                            resData.put("madre", madre);
                            resData.put("sexo", sexo);
                            resData.put("fechaNacimiento", fechaNacimiento);
                            resData.put("usuarioFk", cedula);
                            resData.put("tipoRes", tiporesesaux);

                            // Guardar en Firebase
                            databaseReference.push().setValue(resData)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(AgregarReses.this, "Bovino registrado exitosamente", Toast.LENGTH_SHORT).show();
                                        limpiarCampos();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(AgregarReses.this, "Error en el registro: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(AgregarReses.this, "Error de conexión: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void limpiarCampos() {
        Chapeta.setText("");
        Nombre.setText("");
        Padre.setText("");
        Madre.setText("");
        FechaNacimiento.setText("");
        Chapeta.requestFocus();
        Macho.setChecked(true);
    }
}