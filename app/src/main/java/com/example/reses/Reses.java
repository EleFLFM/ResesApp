package com.example.reses;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Reses extends AppCompatActivity {

    private ListView resesListView;
    private Button agregarButton;
    private ArrayList<GettersReses> resesList;
    private ResesAdapter adapter;
    private EditText izquierdoaux, derechoaux;
    private String tiporesesaux, cedula;
    private boolean isSpecificView;
    private String imagenUrl;  // o el nombre que uses para la imagen

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reses);

        // Inicializar Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("reses");

        // Obtener datos del intent
        Intent intent = getIntent();
        tiporesesaux = intent.getStringExtra("tiporeses");
        cedula = intent.getStringExtra("cedula");
        isSpecificView = intent.getBooleanExtra("isSpecificView", false);
        // Inicializar vistas
        resesListView = findViewById(R.id.resesListView);
        izquierdoaux = findViewById(R.id.editTextIzquierdo);
        derechoaux = findViewById(R.id.editTextDerecho);
        agregarButton = findViewById(R.id.agregarButton);
        resesList = new ArrayList<>();

        // Configurar adaptador
        adapter = new ResesAdapter(this, resesList, tiporesesaux, cedula);
        resesListView.setAdapter(adapter);

        // Cargar datos
        loadReses();

        // Botón agregar
        agregarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Reses.this, AgregarReses.class);
                intent.putExtra("cedula", cedula);
                intent.putExtra("tiporeses", tiporesesaux);
                intent.putExtra("isSpecificView", isSpecificView); // Pasar el parámetro
                startActivity(intent);
                finish();
            }
        });

        // Listener para búsqueda
        derechoaux.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadReses();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void loadReses() {
        Query query = databaseReference.orderByChild("usuarioFk").equalTo(cedula);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                resesList.clear();
                String searchText = derechoaux.getText().toString().trim().toLowerCase();
                int count = 0;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    GettersReses res = createResesFromSnapshot(snapshot);

                    // Filtro principal por SEXO (no por tipoRes)
                    boolean matchesType = tiporesesaux.equalsIgnoreCase("Todos") ||
                            res.getSexo().equalsIgnoreCase(tiporesesaux);

                    // Filtro secundario por búsqueda
                    boolean matchesSearch = searchText.isEmpty() ||
                            res.getNombre().toLowerCase().contains(searchText);

                    if (matchesType && matchesSearch) {
                        resesList.add(res);
                        count++;
                    }
                }

                adapter.notifyDataSetChanged();
                izquierdoaux.setText("Bovinos: " + count);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Reses.this, "Error al cargar reses: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private GettersReses createResesFromSnapshot(DataSnapshot snapshot) {
        String id = snapshot.getKey();
        String nombre = snapshot.child("nombre").getValue(String.class);
        String sexo = snapshot.child("sexo").getValue(String.class);
        String tipoRes = snapshot.child("tipoRes").getValue(String.class);
        String fechaNacimiento = snapshot.child("fechaNacimiento").getValue(String.class);
        String madre = snapshot.child("madre").getValue(String.class);
        String padre = snapshot.child("padre").getValue(String.class);
        String imagenUrl = snapshot.child("imagenUrl").getValue(String.class);
        return new GettersReses(id, nombre, sexo, tipoRes, fechaNacimiento, madre, padre, imagenUrl);

    }

    public void GoBackMenu(View view) {
        Intent intent = new Intent(Reses.this, Menu.class);
        intent.putExtra("cedula", cedula);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadReses();
    }
}