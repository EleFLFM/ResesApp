package com.example.reses;
import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class GestiondeReses extends AppCompatActivity {
    private EditText Chapeta, Nombre, Padre, Madre, FechaNacimiento;
    private RadioButton Macho, Hembra;
    private String cedula, tiporesesaux, chapetaaux, id;
    private DatabaseReference databaseReference;
    private ImageView imageViewRes;
    private Uri imageUri;
    private Bitmap selectedBitmap;

    private static final int REQUEST_IMAGE_GALLERY = 1;
    private static final int REQUEST_IMAGE_CAMERA = 2;
    private static final int REQUEST_PERMISSION = 100;

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
        imageViewRes = findViewById(R.id.imageViewRes);

        // Configurar date picker
        FechaNacimiento.setOnClickListener(v -> showDatePicker());

        // Configurar botones de imagen
        findViewById(R.id.btnSeleccionarImagen).setOnClickListener(v -> seleccionarImagenGaleria());
        findViewById(R.id.btnTomarFoto).setOnClickListener(v -> tomarFotoCamara());

        // Verificar permisos
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, REQUEST_PERMISSION);
        }

        // Cargar datos de la res
        loadResData();
    }

    private void seleccionarImagenGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_GALLERY);
    }

    private void tomarFotoCamara() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_IMAGE_CAMERA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_GALLERY && data != null) {
                imageUri = data.getData();
                try {
                    selectedBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    imageViewRes.setImageBitmap(selectedBitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == REQUEST_IMAGE_CAMERA && data != null) {
                selectedBitmap = (Bitmap) data.getExtras().get("data");
                imageViewRes.setImageBitmap(selectedBitmap);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permisos concedidos
            } else {
                Toast.makeText(this, "Se necesitan permisos para acceder a la cámara y galería", Toast.LENGTH_SHORT).show();
            }
        }
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
                if (selectedBitmap != null) {
                    // Si hay una nueva imagen seleccionada, subirla primero
                    subirImagenYActualizar();
                } else {
                    // Si no hay nueva imagen, actualizar solo los datos
                    checkChapetaAndUpdate();
                }
            } else {
                Toast.makeText(this, "Error, la fecha ingresada supera el día actual.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void subirImagenYActualizar() {
        String imageName = Chapeta.getText().toString().trim() + "_" + System.currentTimeMillis();
        ImgBBUploader.uploadImage(selectedBitmap, imageName, new ImgBBUploader.UploadCallback() {
            @Override
            public void onSuccess(String imageUrl) {
                runOnUiThread(() -> {
                    // Guardar la nueva URL de la imagen y actualizar los datos
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("imageUrl", imageUrl);
                    databaseReference.child(id).updateChildren(updates)
                            .addOnSuccessListener(aVoid -> {
                                // Después de actualizar la imagen, actualizar el resto de los datos
                                checkChapetaAndUpdate();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(GestiondeReses.this, "Error al actualizar la imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(GestiondeReses.this, "Error al subir la imagen: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
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