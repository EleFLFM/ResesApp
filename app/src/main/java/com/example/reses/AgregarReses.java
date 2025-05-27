package com.example.reses;
import android.graphics.Bitmap;
import okhttp3.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AgregarReses extends AppCompatActivity {
    private EditText Chapeta, Nombre, Padre, Madre, FechaNacimiento;
    private RadioButton Macho;
    private ImageView imageViewRes;

    private static final int REQUEST_IMAGE_GALLERY = 1;
    private static final int REQUEST_IMAGE_CAMERA = 2;
    private static final int REQUEST_PERMISSION = 100;

    private Uri imageUri;

    private String cedula, tiporesesaux;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_agregar_reses);

        // Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("reses");

        // Obtener extras
        Intent intent = getIntent();
        cedula = intent.getStringExtra("cedula");
        tiporesesaux = intent.getStringExtra("tiporeses");

        // Inicializar vistas
        Chapeta = findViewById(R.id.chapetaEditText);
        Nombre = findViewById(R.id.nombreEditText);
        Padre = findViewById(R.id.padreEditText);
        Madre = findViewById(R.id.madreEditText);
        Macho = findViewById(R.id.machoButton);
        FechaNacimiento = findViewById(R.id.fechaEditText);
        imageViewRes = findViewById(R.id.imageViewRes);

        FechaNacimiento.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    v.getContext(),
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String fecha = selectedYear + "/" + String.format("%02d", (selectedMonth + 1)) + "/" + String.format("%02d", selectedDay);
                        FechaNacimiento.setText(fecha);
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });

        // Solicitar permisos si es necesario
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE
            }, REQUEST_PERMISSION);
        }

        // Botón seleccionar imagen de galería
        findViewById(R.id.btnSeleccionarImagen).setOnClickListener(v -> {
            Intent intentGal = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intentGal, REQUEST_IMAGE_GALLERY);
        });

        // Botón tomar foto
        findViewById(R.id.btnTomarFoto).setOnClickListener(v -> {
            Intent intentCam = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intentCam, REQUEST_IMAGE_CAMERA);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_GALLERY && data != null) {
                imageUri = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    imageViewRes.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (requestCode == REQUEST_IMAGE_CAMERA && data != null) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                imageViewRes.setImageBitmap(photo);
            }
        }
    }

    public void GoBackListaBovino(View view) {
        Intent intent = new Intent(AgregarReses.this, Reses.class);
        intent.putExtra("cedula", cedula);
        intent.putExtra("tiporeses", tiporesesaux);
        startActivity(intent);
        finish();
    }

    public void RegistrarReses(View view) {
        if (Nombre.getText().toString().isEmpty() ||
                Padre.getText().toString().isEmpty() ||
                Madre.getText().toString().isEmpty() ||
                FechaNacimiento.getText().toString().isEmpty()) {

            Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        String fechaActual = java.time.LocalDate.now().toString().replace("-", "/");
        if (fechaActual.compareTo(FechaNacimiento.getText().toString()) < 0) {
            Toast.makeText(this, "Error, la fecha ingresada supera el día actual.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtener el Bitmap de la ImageView
        imageViewRes.setDrawingCacheEnabled(true);
        imageViewRes.buildDrawingCache();
        Bitmap bitmap = imageViewRes.getDrawingCache();

        if (bitmap == null) {
            // Si no hay imagen, guardar solo los datos
            guardarDatosEnFirebase(null);
        } else {
            // Subir la imagen a ImgBB
            String imageName = Chapeta.getText().toString().trim() + "_" + System.currentTimeMillis();
            ImgBBUploader.uploadImage(bitmap, imageName, new ImgBBUploader.UploadCallback() {
                @Override
                public void onSuccess(String imageUrl) {
                    // Cuando la imagen se sube correctamente, guardar los datos con la URL
                    runOnUiThread(() -> {
                        guardarDatosEnFirebase(imageUrl);
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        Toast.makeText(AgregarReses.this, "Error al subir la imagen: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            });
        }
    }
    private void guardarDatosEnFirebase(String imageUrl) {
        String chapeta = Chapeta.getText().toString().trim();
        String nombre = Nombre.getText().toString().trim();
        String padre = Padre.getText().toString().trim();
        String madre = Madre.getText().toString().trim();
        String sexo = Macho.isChecked() ? "Macho" : "Hembra";
        String fechaNacimiento = FechaNacimiento.getText().toString().trim();

        databaseReference.orderByChild("chapeta").equalTo(chapeta)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Toast.makeText(AgregarReses.this, "La chapeta ya fue registrada.", Toast.LENGTH_SHORT).show();
                        } else {
                            Map<String, Object> resData = new HashMap<>();
                            resData.put("chapeta", chapeta);
                            resData.put("nombre", nombre);
                            resData.put("padre", padre);
                            resData.put("madre", madre);
                            resData.put("sexo", sexo);
                            resData.put("fechaNacimiento", fechaNacimiento);
                            resData.put("usuarioFk", cedula);
                            resData.put("tipoRes", tiporesesaux);

                            // Agregar la URL de la imagen si existe
                            if (imageUrl != null) {
                                resData.put("imageUrl", imageUrl);
                            }

                            databaseReference.push().setValue(resData)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(AgregarReses.this, "Bovino registrado exitosamente", Toast.LENGTH_SHORT).show();
                                        limpiarCampos();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(AgregarReses.this, "Error en el registro: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(AgregarReses.this, "Error de conexión: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void limpiarCampos() {
        Chapeta.setText("");
        Nombre.setText("");
        Padre.setText("");
        Madre.setText("");
        FechaNacimiento.setText("");
        imageViewRes.setImageResource(0); // Limpiar imagen
        Chapeta.requestFocus();
        Macho.setChecked(true);
    }
}
