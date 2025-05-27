package com.example.reses;

import android.graphics.Bitmap;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ImgBBUploader {
    private static final String IMGBB_API_KEY = "7c1edcfe9e8137b58af246b4b36ed9f5";
    private static final String IMGBB_UPLOAD_URL = "https://api.imgbb.com/1/upload";

    public interface UploadCallback {
        void onSuccess(String imageUrl);
        void onError(String error);
    }

    public static void uploadImage(Bitmap bitmap, String imageName, UploadCallback callback) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);

        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new FormBody.Builder()
                .add("key", IMGBB_API_KEY)
                .add("image", encodedImage)
                .add("name", imageName)
                .build();

        Request request = new Request.Builder()
                .url(IMGBB_UPLOAD_URL)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    // Parsear la respuesta para obtener la URL de la imagen
                    // Esto es un ejemplo simplificado, deberías usar un JSON parser real
                    String imageUrl = responseData.split("\"url\":\"")[1].split("\"")[0];
                    callback.onSuccess(imageUrl);
                } else {
                    callback.onError("Error en la respuesta: " + response.code());
                }
            }
        });
    }
}