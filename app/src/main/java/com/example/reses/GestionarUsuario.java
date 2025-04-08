package com.example.reses;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.Map;

public class GestionarUsuario extends AppCompatActivity {
    EditText identificacion;
    EditText Nombre;
    EditText Apellidos;
    EditText Usuario;
    EditText Clave;
    private DatabaseHelper dbHelper;
    String cedula;
    String user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_actualizar_usuario);
        Intent intent=getIntent();
        cedula=intent.getStringExtra("cedula");
        dbHelper = new DatabaseHelper(this);
        identificacion=findViewById(R.id.identificacionEditText);
        Nombre=findViewById(R.id.nombresEditText);
        Apellidos=findViewById(R.id.apellidosEditText);
        Usuario=findViewById(R.id.usuarioEditText);
        Clave=findViewById(R.id.claveEditText);
        List<Map<String, String>> resultado=dbHelper.SearchSQL("select * from Usuario where Identificacion='"+cedula+"'");
        identificacion.setText(resultado.get(0).get("Identificacion"));
        Nombre.setText(resultado.get(0).get("Nombre"));
        Apellidos.setText(resultado.get(0).get("Apellidos"));
        Usuario.setText(resultado.get(0).get("Usuario"));
        user=resultado.get(0).get("Usuario");
        Clave.setText(resultado.get(0).get("Clave"));
    }

    public void GoBackMenu(View view){
        Intent intent=new Intent(GestionarUsuario.this, Menu.class);
        intent.putExtra("cedula", cedula);
        startActivity(intent);
        finish();
    }

    public void ActualizarUsuario(View view){
        if (Clave.getText().toString().isEmpty() || Usuario.getText().toString().isEmpty() || Nombre.getText().toString().isEmpty() ||
                Apellidos.getText().toString().isEmpty() || identificacion == null || identificacion.getText().toString().isEmpty()) {
            Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show();
        }
        if(dbHelper.SearchSQL("select * from Usuario where Usuario='"+Usuario.getText()+"'  COLLATE NOCASE ").isEmpty() || user.equalsIgnoreCase(Usuario.getText().toString())) {
                long newRowId = dbHelper.updateUsuario(identificacion.getText().toString(), Nombre.getText().toString(),
                        Apellidos.getText().toString(), Usuario.getText().toString(), Clave.getText().toString());

                if (newRowId == -1) {
                    Toast.makeText(this, "Error en el registro, datos no válidos", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Usuario actualizado exitosamente", Toast.LENGTH_SHORT).show();
                }
        }else{
            Toast.makeText(this, "El nombre de usuario ya esta ocupado por otra persona.", Toast.LENGTH_SHORT).show();
        }
    }
    public void DeleteAllData(View view){
        new AlertDialog.Builder(this)
                .setTitle("Confirmación")
                .setMessage("¿Está seguro de querer borrar su cuenta y los registros de sus reses?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    if(dbHelper.borrarDatos(cedula,"Reses", "UsuarioFk") ||
                            dbHelper.SearchSQL("select * from Reses where UsuarioFk='"+cedula+"'").isEmpty()) {
                        if (dbHelper.borrarDatos(cedula, "Usuario", "Identificacion")) {
                            Toast.makeText(this, "Usuario eliminado exitosamente", Toast.LENGTH_SHORT).show();
                            Intent intent=new Intent(GestionarUsuario.this,MainActivity.class);
                            startActivity(intent);
                        }else{
                            Toast.makeText(this, "Error al borrar el usuario", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(this, "Error al borrar las reses", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

}