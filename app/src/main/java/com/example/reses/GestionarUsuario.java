package com.example.reses;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class GestionarUsuario extends AppCompatActivity {
    EditText identificacion;
    EditText Nombre;
    EditText Apellidos;
    EditText Usuario;
    EditText Clave;
    private DatabaseReference databaseReference;
    String cedula;
    String user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_actualizar_usuario);

        Intent intent = getIntent();
        cedula = intent.getStringExtra("cedula");

        // Inicializar Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("usuarios");

        // Inicializar vistas
        identificacion = findViewById(R.id.identificacionEditText);
        Nombre = findViewById(R.id.nombresEditText);
        Apellidos = findViewById(R.id.apellidosEditText);
        Usuario = findViewById(R.id.usuarioEditText);
        Clave = findViewById(R.id.claveEditText);

        // Cargar datos del usuario
        loadUserData();
    }

    private void loadUserData() {
        databaseReference.child(cedula).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    identificacion.setText(snapshot.child("identificacion").getValue(String.class));
                    Nombre.setText(snapshot.child("nombre").getValue(String.class));
                    Apellidos.setText(snapshot.child("apellidos").getValue(String.class));
                    Usuario.setText(snapshot.child("usuario").getValue(String.class));
                    user = snapshot.child("usuario").getValue(String.class);
                    Clave.setText(snapshot.child("clave").getValue(String.class));
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(GestionarUsuario.this, "Error al cargar datos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void GoBackMenu(View view) {
        Intent intent = new Intent(this, Menu.class);
        intent.putExtra("cedula", cedula);
        startActivity(intent);
        finish();
    }
      public void GoBackMenu() {
        Intent intent = new Intent(this, Menu.class);
        intent.putExtra("cedula", cedula);
        startActivity(intent);
        finish();
    }

    public void ActualizarUsuario(View view) {
        // Validar campos
        if (Clave.getText().toString().isEmpty() || Usuario.getText().toString().isEmpty() ||
                Nombre.getText().toString().isEmpty() || Apellidos.getText().toString().isEmpty() ||
                identificacion.getText().toString().isEmpty()) {
            Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verificar si el usuario ya existe
        checkUsernameAndUpdate();
    }

    private void checkUsernameAndUpdate() {
        final String nuevoUsuario = Usuario.getText().toString().trim();

        if (user.equalsIgnoreCase(nuevoUsuario)) {
            // Si el nombre de usuario no cambió, actualizar directamente
            updateUserInFirebase();
        } else {
            // Verificar si el nuevo nombre de usuario ya está ocupado
            databaseReference.orderByChild("usuario").equalTo(nuevoUsuario)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                Toast.makeText(GestionarUsuario.this, "El nombre de usuario ya está ocupado por otra persona.", Toast.LENGTH_SHORT).show();
                            } else {
                                updateUserInFirebase();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            Toast.makeText(GestionarUsuario.this, "Error al verificar usuario", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void updateUserInFirebase() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("identificacion", identificacion.getText().toString().trim());
        updates.put("nombre", Nombre.getText().toString().trim());
        updates.put("apellidos", Apellidos.getText().toString().trim());
        updates.put("usuario", Usuario.getText().toString().trim());
        updates.put("clave", Clave.getText().toString().trim());

        databaseReference.child(cedula).updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(GestionarUsuario.this, "Usuario actualizado exitosamente", Toast.LENGTH_SHORT).show();
                    GoBackMenu();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(GestionarUsuario.this, "Error en el registro, datos no válidos", Toast.LENGTH_SHORT).show();
                });
    }

    public void DeleteAllData(View view) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmación")
                .setMessage("¿Está seguro de querer borrar su cuenta y los registros de sus reses?")
                .setPositiveButton("Sí", (dialog, which) -> deleteUserAndReses())
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void deleteUserAndReses() {
        // Primero eliminar las reses asociadas al usuario
        DatabaseReference resesRef = FirebaseDatabase.getInstance().getReference("reses");
        resesRef.orderByChild("usuarioFk").equalTo(cedula)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        // Eliminar cada res asociada al usuario
                        for (DataSnapshot resesSnapshot : snapshot.getChildren()) {
                            resesSnapshot.getRef().removeValue();
                        }

                        // Después de eliminar las reses, eliminar el usuario
                        deleteUser();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(GestionarUsuario.this, "Error al borrar las reses", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteUser() {
        databaseReference.child(cedula).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(GestionarUsuario.this, "Usuario eliminado exitosamente", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(GestionarUsuario.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(GestionarUsuario.this, "Error al borrar el usuario", Toast.LENGTH_SHORT).show();
                });
    }
}