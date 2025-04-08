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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Reses extends AppCompatActivity {

    private ListView resesListView;
    private Button agregarButton;
    private ArrayList<GettersReses> resesList;
    private ResesAdapter adapter;
    private EditText izquierdoaux, derechoaux;
    private String tiporesesaux, cedula;

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
                startActivity(intent);
                finish();
            }
        });

        // Sincronizar cantidades
        SincronizarCantidades();

        // Listener para búsqueda
        derechoaux.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadReses();
                SincronizarCantidades();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    public void SincronizarCantidades() {
        Query query;
        if (tiporesesaux.equalsIgnoreCase("Todos")) {
            query = databaseReference.orderByChild("usuarioFk").equalTo(cedula);
        } else {
            query = databaseReference.orderByChild("usuarioFk_tipoRes").equalTo(cedula + "_" + tiporesesaux);
        }

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long count = dataSnapshot.getChildrenCount();
                izquierdoaux.setText("Bovinos: " + count);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(Reses.this, "Error al contar reses: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
//    private void loadReses() {
//        Query query;
//        String searchText = derechoaux.getText().toString().trim();
//
//        // Simplificamos la consulta ya que no necesitamos filtrar por tipo
//        query = databaseReference.orderByChild("usuarioFk").equalTo(cedula);
//
//        query.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                resesList.clear();
//                String searchText = derechoaux.getText().toString().trim().toLowerCase();
//
//                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                    String nombre = snapshot.child("nombre").getValue(String.class);
//                    String sexo = snapshot.child("sexo").getValue(String.class);
//                    String id = snapshot.getKey();
//
//                    // Filtramos solo por texto de búsqueda
//                    if (searchText.isEmpty() || nombre.toLowerCase().contains(searchText)) {
//                        GettersReses res = new GettersReses(id, nombre, sexo);
//                        resesList.add(res);
//                    }
//                }
//                adapter.notifyDataSetChanged();
//                SincronizarCantidades();
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                Toast.makeText(Reses.this, "Error al cargar reses: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
    private void loadReses() {
        Query query;
        String searchText = derechoaux.getText().toString().trim();

        if (searchText.isEmpty()) {
            // Consulta sin filtro de búsqueda
            if (tiporesesaux.equalsIgnoreCase("Todos")) {
                query = databaseReference.orderByChild("usuarioFk").equalTo(cedula);
            } else {
                query = databaseReference.orderByChild("usuarioFk_tipoRes").equalTo(cedula + "_" + tiporesesaux);
            }
        } else {
            // Consulta con filtro de búsqueda por nombre
            if (tiporesesaux.equalsIgnoreCase("Todos")) {
                query = databaseReference.orderByChild("usuarioFk").equalTo(cedula);
            } else {
                query = databaseReference.orderByChild("usuarioFk_tipoRes").equalTo(cedula + "_" + tiporesesaux);
            }
        }

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                resesList.clear();
                String searchText = derechoaux.getText().toString().trim().toLowerCase();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String nombre = snapshot.child("nombre").getValue(String.class);
                    String tipoRes = snapshot.child("tipoRes").getValue(String.class);
                    String id = snapshot.getKey();

                    // Filtrar por texto de búsqueda si es necesario
                    if (searchText.isEmpty() || nombre.toLowerCase().contains(searchText)) {
                        if (tiporesesaux.equalsIgnoreCase("Todos") ||
                                tiporesesaux.equalsIgnoreCase(tipoRes)) {

                            GettersReses res = new GettersReses(id, nombre, tipoRes);
                            resesList.add(res);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
                SincronizarCantidades();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(Reses.this, "Error al cargar reses: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}