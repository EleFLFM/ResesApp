package com.example.reses;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    private EditText usuario, contrasena;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Inicializar Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("usuarios");

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        usuario = findViewById(R.id.etUsername);
        contrasena = findViewById(R.id.etPassword);
    }

    public void Login(View view) {
        String usuarioaux = usuario.getText().toString().trim();
        String claveaux = contrasena.getText().toString().trim();

        if (usuarioaux.isEmpty() || claveaux.isEmpty()) {
            Toast.makeText(this, "Ingrese usuario y contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        // Consulta para verificar credenciales
        Query query = databaseReference.orderByChild("usuario").equalTo(usuarioaux);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String claveDB = snapshot.child("clave").getValue(String.class);
                        if (claveDB != null && claveDB.equals(claveaux)) {
                            // Credenciales correctas
                            String cedula = snapshot.child("identificacion").getValue(String.class);
                            Intent intent = new Intent(MainActivity.this, Menu.class);
                            intent.putExtra("cedula", cedula);
                            startActivity(intent);
                            finish();
                            return;
                        }
                    }
                }
                // Si llega aquí es porque no encontró coincidencias
                Toast.makeText(MainActivity.this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Error de conexión: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void VistaRegistro(View view) {
        Intent intent = new Intent(MainActivity.this, RegistroUsuario.class);
        startActivity(intent);
        finish();
    }
}