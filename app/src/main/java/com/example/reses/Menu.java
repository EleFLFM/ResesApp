package com.example.reses;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class Menu extends AppCompatActivity {
    String cedula;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu);
        Intent intent=getIntent();
        cedula=intent.getStringExtra("cedula");
    }
    public void ViewActualizarUsuario(View view){
        Intent intent=new Intent(Menu.this, GestionarUsuario.class);
        intent.putExtra("cedula",cedula);
        startActivity(intent);
        finish();
    }
    public void ViewLogin(View view){
        Intent intent=new Intent(Menu.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    public void ViewMachos(View view) {
        Intent intent = new Intent(Menu.this, Reses.class);
        intent.putExtra("tiporeses", "Macho");
        intent.putExtra("cedula", cedula);
        intent.putExtra("isSpecificView", true); // Nuevo parámetro
        startActivity(intent);
        finish();
    }
    public void ViewHembras(View view) {
        Intent intent = new Intent(Menu.this, Reses.class);
        intent.putExtra("tiporeses", "Hembra");
        intent.putExtra("cedula", cedula);
        intent.putExtra("isSpecificView", true); // Nuevo parámetro
        startActivity(intent);
        finish();
    }

    public void ViewAll(View view) {
        Intent intent = new Intent(Menu.this, Reses.class);
        intent.putExtra("tiporeses", "Todos");
        intent.putExtra("cedula", cedula);
        intent.putExtra("isSpecificView", false); // Nuevo parámetro
        startActivity(intent);
        finish();
    }
}