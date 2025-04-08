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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class GestiondeReses extends AppCompatActivity {

    private EditText Chapeta;
    private EditText Nombre;
    private EditText Padre;
    private EditText Madre;
    private RadioButton Macho;
    String cedula,tiporesesaux;
    private DatabaseHelper dbHelper;
    private RadioButton Hembra;
    String chapetaaux;
    private EditText FechaNacimiento;
    String id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_gestionde_reses);
        dbHelper = new DatabaseHelper(this);
        Intent intent = getIntent();
        cedula = intent.getStringExtra("cedula");
        tiporesesaux = intent.getStringExtra("tiporeses");
        id = intent.getStringExtra("id");
        Chapeta=findViewById(R.id.chapetaEditText);
        Nombre=findViewById(R.id.nombreEditText);
        Padre=findViewById(R.id.padreEditText);
        Madre=findViewById(R.id.madreEditText);
        Macho=findViewById(R.id.machoButton);
        Hembra=findViewById(R.id.hembraButton);
        FechaNacimiento=findViewById(R.id.fechaEditText);

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

        List<Map<String, String>> resultado=dbHelper.SearchSQL("select * from Reses where Id='"+id+"'");
        Chapeta.setText(resultado.get(0).get("Chapeta"));
        Nombre.setText(resultado.get(0).get("Nombre"));
        Padre.setText(resultado.get(0).get("Padre"));
        Madre.setText(resultado.get(0).get("Madre"));
        Macho.setChecked(resultado.get(0).get("TipoBovino").toString().equals("Macho"));
        Hembra.setChecked(resultado.get(0).get("TipoBovino").toString().equals("Hembra"));
        FechaNacimiento.setText(resultado.get(0).get("FechaNacimiento"));
        chapetaaux=Chapeta.getText().toString();
    }

    public void GoBackListaBovinoDesdeGestion(View view){
        Intent intent=new Intent(GestiondeReses.this, Reses.class);
        intent.putExtra("cedula",cedula);
        intent.putExtra("tiporeses",tiporesesaux);
        startActivity(intent);
        finish();
    }
    public void DeleteReses(View view){
        new AlertDialog.Builder(this)
                .setTitle("Confirmación")
                .setMessage("¿Está seguro de querer borrar su cuenta y los registros de sus reses?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    if(dbHelper.borrarDatos(id,"Reses", "Id")) {
                        Toast.makeText(this, "Bovino eliminado exitosamente", Toast.LENGTH_SHORT).show();
                        GoBackListaBovinoDesdeGestion(view);
                    }else{
                        Toast.makeText(this, "Error al borrar las reses", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }
    public void ActualizarReses(View view) {
        if (Nombre.getText().toString().isEmpty() || Padre.getText().toString().isEmpty() || Madre.getText().toString().isEmpty() || FechaNacimiento.getText().toString().isEmpty()) {
            Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show();
        } else {
            String fechaactual=java.time.LocalDate.now().toString().replace("-", "/");
            if (fechaactual.compareTo(FechaNacimiento.getText().toString()) >= 0) {
                if (dbHelper.SearchSQL("select * from Reses where Chapeta='" + Chapeta.getText().toString() + "' COLLATE NOCASE and UsuarioFk='" + cedula + "'").isEmpty() ||
                        chapetaaux.equalsIgnoreCase(Chapeta.getText().toString())) {
                    long newRowId = dbHelper.updateReses(id, Chapeta.getText().toString(), Nombre.getText().toString(),
                            Padre.getText().toString(), Madre.getText().toString(), (Macho.isChecked() ? "Macho" : "Hembra"), FechaNacimiento.getText().toString(), cedula);

                    if (newRowId == -1) {
                        Toast.makeText(this, "Error en el registro, datos no válidos", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Bovino actualizado exitosamente", Toast.LENGTH_SHORT).show();
                        GoBackListaBovinoDesdeGestion(view);
                    }
                } else {
                    Toast.makeText(this, "La chapeta ya fue registrada.", Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(this, "Error, la fecha ingresada supera el dia actual.", Toast.LENGTH_SHORT).show();
            }
        }
    }


}