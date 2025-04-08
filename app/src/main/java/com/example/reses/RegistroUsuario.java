package com.example.reses;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class RegistroUsuario extends AppCompatActivity {
    EditText identificacion;
    EditText Nombre;
    EditText Apellidos;
    EditText Usuario;
    EditText Clave;

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro_usuario);

        // Inicializar Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("usuarios");

        identificacion = findViewById(R.id.identificacionEditText);
        Nombre = findViewById(R.id.nombresEditText);
        Apellidos = findViewById(R.id.apellidosEditText);
        Usuario = findViewById(R.id.usuarioEditText);
        Clave = findViewById(R.id.claveEditText);
    }

    public void GoBack(View view) {
        Intent intent = new Intent(RegistroUsuario.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void Registrarse(View view) {
        if (!validarCampos()) {
            return;
        }

        final String id = identificacion.getText().toString().trim();
        final String usuario = Usuario.getText().toString().trim();
        final String clave = Clave.getText().toString().trim();
        final String nombre = Nombre.getText().toString().trim();
        final String apellidos = Apellidos.getText().toString().trim();

        // Primero verificamos si la identificación ya existe
        databaseReference.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Toast.makeText(RegistroUsuario.this, "La identificación ya está registrada", Toast.LENGTH_SHORT).show();
                } else {
                    // Si la identificación no existe, verificamos el nombre de usuario
                    verificarUsuarioUnico(usuario, id, nombre, apellidos, clave);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(RegistroUsuario.this, "Error de conexión: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void verificarUsuarioUnico(final String usuario, final String id,
                                       final String nombre, final String apellidos,
                                       final String clave) {
        Query query = databaseReference.orderByChild("usuario").equalTo(usuario);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Toast.makeText(RegistroUsuario.this, "El nombre de usuario ya está en uso", Toast.LENGTH_SHORT).show();
                } else {
                    registrarUsuario(id, nombre, apellidos, usuario, clave);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(RegistroUsuario.this, "Error al verificar usuario: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void registrarUsuario(String id, String nombre, String apellidos,
                                  String usuario, String clave) {
        Map<String, Object> usuarioMap = new HashMap<>();
        usuarioMap.put("identificacion", id);
        usuarioMap.put("nombre", nombre);
        usuarioMap.put("apellidos", apellidos);
        usuarioMap.put("usuario", usuario);
        usuarioMap.put("clave", clave);

        databaseReference.child(id).setValue(usuarioMap)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(RegistroUsuario.this, "Registro exitoso!", Toast.LENGTH_SHORT).show();
                    limpiarCampos();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(RegistroUsuario.this, "Error al registrar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private boolean validarCampos() {
        if (identificacion.getText().toString().trim().isEmpty() ||
                Nombre.getText().toString().trim().isEmpty() ||
                Apellidos.getText().toString().trim().isEmpty() ||
                Usuario.getText().toString().trim().isEmpty() ||
                Clave.getText().toString().trim().isEmpty()) {

            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void limpiarCampos() {
        identificacion.setText("");
        Nombre.setText("");
        Apellidos.setText("");
        Usuario.setText("");
        Clave.setText("");
        identificacion.requestFocus();
    }
}