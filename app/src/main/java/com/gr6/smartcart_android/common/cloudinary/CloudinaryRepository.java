package com.gr6.smartcart_android.common.cloudinary;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.gr6.smartcart_android.common.utils.Constants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CloudinaryRepository {

    private final CloudinaryApiService apiService;

    public CloudinaryRepository() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.cloudinary.com/")
                .client(new OkHttpClient.Builder().build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(CloudinaryApiService.class);
    }

    public void uploadImage(
            Context context,
            Uri imageUri,
            UploadCallback callback
    ) {
        try {
            File imageFile = createTempFileFromUri(context, imageUri);

            if (imageFile == null || !imageFile.exists()) {
                callback.onError("Không đọc được ảnh đã chọn");
                return;
            }

            String mimeType = context.getContentResolver().getType(imageUri);
            if (mimeType == null || mimeType.trim().isEmpty()) {
                mimeType = "image/*";
            }

            RequestBody fileBody = RequestBody.create(
                    MediaType.parse(mimeType),
                    imageFile
            );

            MultipartBody.Part filePart = MultipartBody.Part.createFormData(
                    "file",
                    imageFile.getName(),
                    fileBody
            );

            RequestBody presetBody = RequestBody.create(
                    MediaType.parse("text/plain"),
                    Constants.CLOUDINARY_UPLOAD_PRESET
            );

            apiService.uploadImage(
                    Constants.CLOUDINARY_CLOUD_NAME,
                    filePart,
                    presetBody
            ).enqueue(new Callback<CloudinaryUploadResponse>() {
                @Override
                public void onResponse(
                        @NonNull Call<CloudinaryUploadResponse> call,
                        @NonNull Response<CloudinaryUploadResponse> response
                ) {
                    imageFile.delete();

                    if (!response.isSuccessful()) {
                        callback.onError("Upload ảnh thất bại. Mã lỗi: " + response.code());
                        return;
                    }

                    CloudinaryUploadResponse body = response.body();

                    if (body == null || body.getSecureUrl() == null || body.getSecureUrl().trim().isEmpty()) {
                        callback.onError("Cloudinary không trả link ảnh");
                        return;
                    }

                    callback.onSuccess(body.getSecureUrl());
                }

                @Override
                public void onFailure(
                        @NonNull Call<CloudinaryUploadResponse> call,
                        @NonNull Throwable t
                ) {
                    imageFile.delete();
                    callback.onError("Không upload được ảnh: " + t.getMessage());
                }
            });

        } catch (Exception e) {
            callback.onError("Lỗi xử lý ảnh: " + e.getMessage());
        }
    }

    private File createTempFileFromUri(Context context, Uri uri) throws Exception {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);

        if (inputStream == null) {
            return null;
        }

        File tempFile = new File(
                context.getCacheDir(),
                "avatar_" + System.currentTimeMillis() + ".jpg"
        );

        FileOutputStream outputStream = new FileOutputStream(tempFile);

        byte[] buffer = new byte[4096];
        int length;

        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }

        outputStream.flush();
        outputStream.close();
        inputStream.close();

        return tempFile;
    }

    public interface UploadCallback {
        void onSuccess(String imageUrl);

        void onError(String message);
    }
}