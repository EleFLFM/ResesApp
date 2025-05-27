package com.example.reses;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class GestiondeReses extends AppCompatActivity {
    private EditText Chapeta, Nombre, Padre, Madre, FechaNacimiento;
    private RadioButton Macho, Hembra;
    private String cedula, tiporesesaux, chapetaaux, id;
    private DatabaseReference databaseReference;
    private ImageView imageViewRes; // Agrega esta variable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_gestionde_reses);

        // Inicializar Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("reses");

        // Obtener datos del Intent
        Intent intent = getIntent();
        cedula = intent.getStringExtra("cedula");
        tiporesesaux = intent.getStringExtra("tiporeses");
        id = intent.getStringExtra("id");

        // Inicializar vistas
        Chapeta = findViewById(R.id.chapetaEditText);
        Nombre = findViewById(R.id.nombreEditText);
        Padre = findViewById(R.id.padreEditText);
        Madre = findViewById(R.id.madreEditText);
        Macho = findViewById(R.id.machoButton);
        Hembra = findViewById(R.id.hembraButton);
        FechaNacimiento = findViewById(R.id.fechaEditText);
        imageViewRes = findViewById(R.id.imageViewRes); // Inicializa el ImageView

        // Configurar date picker
        FechaNacimiento.setOnClickListener(v -> showDatePicker());

        // Cargar datos de la res
        loadResData();
    }
    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, day) -> {
                    String fecha = year + "/" + String.format("%02d", (month + 1)) + "/" + String.format("%02d", day);
                    FechaNacimiento.setText(fecha);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void loadResData() {
        databaseReference.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Chapeta.setText(snapshot.child("chapeta").getValue(String.class));
                    Nombre.setText(snapshot.child("nombre").getValue(String.class));
                    Padre.setText(snapshot.child("padre").getValue(String.class));
                    Madre.setText(snapshot.child("madre").getValue(String.class));
                    String sexo = snapshot.child("sexo").getValue(String.class);
                    Macho.setChecked("Macho".equals(sexo));
                    Hembra.setChecked("Hembra".equals(sexo));
                    FechaNacimiento.setText(snapshot.child("fechaNacimiento").getValue(String.class));
                    chapetaaux = Chapeta.getText().toString();

                    // Cargar la imagen si existe
                    String imageUrl = snapshot.child("imageUrl").getValue(String.class);
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        // Reemplazar las barras invertidas escapadas
                        String cleanUrl = imageUrl.replace("\\/", "/");
                        Glide.with(GestiondeReses.this)
                                .load(cleanUrl)
                                .placeholder(R.drawable.calavera)
                                .error(R.drawable.calavera)
                                .into(imageViewRes);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(GestiondeReses.this, "Error al cargar datos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void GoBackListaBovinoDesdeGestion(View view) {
        Intent intent = new Intent(this, Reses.class);
        intent.putExtra("cedula", cedula);
        intent.putExtra("tiporeses", tiporesesaux);
        startActivity(intent);
        finish();
    }

    public void DeleteReses(View view) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmación")
                .setMessage("¿Está seguro de querer eliminar este bovino?")
                .setPositiveButton("Sí", (dialog, which) -> deleteResFromFirebase())
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void deleteResFromFirebase() {
        databaseReference.child(id).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Bovino eliminado exitosamente", Toast.LENGTH_SHORT).show();
                    navigateBackToList();
                    //finish();

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al eliminar: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    public void ActualizarReses(View view) {
        if (validarCampos()) {
            String fechaActual = java.time.LocalDate.now().toString().replace("-", "/");
            if (fechaActual.compareTo(FechaNacimiento.getText().toString()) >= 0) {
                checkChapetaAndUpdate();
            } else {
                Toast.makeText(this, "Error, la fecha ingresada supera el día actual.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean validarCampos() {
        if (Nombre.getText().toString().isEmpty() ||
                Padre.getText().toString().isEmpty() ||
                Madre.getText().toString().isEmpty() ||
                FechaNacimiento.getText().toString().isEmpty()) {

            Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void checkChapetaAndUpdate() {
        String nuevaChapeta = Chapeta.getText().toString().trim();

        if (chapetaaux.equalsIgnoreCase(nuevaChapeta)) {
            updateResInFirebase();
        } else {
            databaseReference.orderByChild("chapeta").equalTo(nuevaChapeta)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                Toast.makeText(GestiondeReses.this, "La chapeta ya fue registrada.", Toast.LENGTH_SHORT).show();
                            } else {
                                updateResInFirebase();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            Toast.makeText(GestiondeReses.this, "Error al verificar chapeta", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void updateResInFirebase() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("chapeta", Chapeta.getText().toString().trim());
        updates.put("nombre", Nombre.getText().toString().trim());
        updates.put("padre", Padre.getText().toString().trim());
        updates.put("madre", Madre.getText().toString().trim());
        updates.put("sexo", Macho.isChecked() ? "Macho" : "Hembra");
        updates.put("fechaNacimiento", FechaNacimiento.getText().toString().trim());

        databaseReference.child(id).updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Bovino actualizado exitosamente", Toast.LENGTH_SHORT).show();
                    navigateBackToList();
//                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al actualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
    private void navigateBackToList() {
        Intent intent = new Intent(this, Reses.class);
        intent.putExtra("cedula", cedula);
        intent.putExtra("tiporeses", tiporesesaux);
        startActivity(intent);
        finish();
    }
}