package com.gr6.smartcart_android.common.cloudinary;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.gr6.smartcart_android.common.utils.Constants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

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
        uploadMedia(context, imageUri, false, callback);
    }

    public void uploadVideo(
            Context context,
            Uri videoUri,
            UploadCallback callback
    ) {
        uploadMedia(context, videoUri, true, callback);
    }

    /**
     * Quan trọng:
     * Copy file từ Uri sang cache được chạy trong background thread.
     * Trước đó bạn copy ngay trong hàm uploadMedia(), dễ làm lag main thread khi chọn ảnh/video lớn.
     */
    private void uploadMedia(
            Context context,
            Uri uri,
            boolean isVideo,
            UploadCallback callback
    ) {
        if (context == null || uri == null) {
            postError(callback, isVideo ? "Video không hợp lệ" : "Ảnh không hợp lệ");
            return;
        }

        Context appContext = context.getApplicationContext();

        EXECUTOR.execute(() -> {
            File mediaFile = null;

            try {
                mediaFile = createTempFileFromUri(
                        appContext,
                        uri,
                        isVideo ? "review_video_" : "review_image_"
                );

                if (mediaFile == null || !mediaFile.exists()) {
                    postError(callback, isVideo ? "Không đọc được video đã chọn" : "Không đọc được ảnh đã chọn");
                    return;
                }

                String mimeType = appContext.getContentResolver().getType(uri);

                if (mimeType == null || mimeType.trim().isEmpty()) {
                    mimeType = isVideo ? "video/*" : "image/*";
                }

                RequestBody fileBody = RequestBody.create(
                        MediaType.parse(mimeType),
                        mediaFile
                );

                MultipartBody.Part filePart = MultipartBody.Part.createFormData(
                        "file",
                        mediaFile.getName(),
                        fileBody
                );

                RequestBody presetBody = RequestBody.create(
                        MediaType.parse("text/plain"),
                        Constants.CLOUDINARY_UPLOAD_PRESET
                );

                Call<CloudinaryUploadResponse> call = isVideo
                        ? apiService.uploadVideo(Constants.CLOUDINARY_CLOUD_NAME, filePart, presetBody)
                        : apiService.uploadImage(Constants.CLOUDINARY_CLOUD_NAME, filePart, presetBody);

                File finalMediaFile = mediaFile;

                call.enqueue(new Callback<CloudinaryUploadResponse>() {
                    @Override
                    public void onResponse(
                            @NonNull Call<CloudinaryUploadResponse> call,
                            @NonNull Response<CloudinaryUploadResponse> response
                    ) {
                        safeDelete(finalMediaFile);

                        if (!response.isSuccessful()) {
                            postError(
                                    callback,
                                    (isVideo ? "Upload video thất bại. Mã lỗi: " : "Upload ảnh thất bại. Mã lỗi: ")
                                            + response.code()
                            );
                            return;
                        }

                        CloudinaryUploadResponse body = response.body();

                        if (body == null
                                || body.getSecureUrl() == null
                                || body.getSecureUrl().trim().isEmpty()) {
                            postError(
                                    callback,
                                    isVideo ? "Cloudinary không trả link video" : "Cloudinary không trả link ảnh"
                            );
                            return;
                        }

                        postSuccess(callback, body.getSecureUrl());
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<CloudinaryUploadResponse> call,
                            @NonNull Throwable t
                    ) {
                        safeDelete(finalMediaFile);

                        postError(
                                callback,
                                (isVideo ? "Không upload được video: " : "Không upload được ảnh: ")
                                        + t.getMessage()
                        );
                    }
                });

            } catch (Exception e) {
                safeDelete(mediaFile);
                postError(callback, (isVideo ? "Lỗi xử lý video: " : "Lỗi xử lý ảnh: ") + e.getMessage());
            }
        });
    }

    private File createTempFileFromUri(
            Context context,
            Uri uri,
            String prefix
    ) throws Exception {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);

        if (inputStream == null) {
            return null;
        }

        File tempFile = new File(
                context.getCacheDir(),
                prefix + System.currentTimeMillis()
        );

        FileOutputStream outputStream = new FileOutputStream(tempFile);

        byte[] buffer = new byte[8192];
        int length;

        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }

        outputStream.flush();
        outputStream.close();
        inputStream.close();

        return tempFile;
    }

    private void safeDelete(File file) {
        try {
            if (file != null && file.exists()) {
                file.delete();
            }
        } catch (Exception ignored) {
        }
    }

    private void postSuccess(UploadCallback callback, String url) {
        if (callback == null) return;

        MAIN_HANDLER.post(() -> callback.onSuccess(url));
    }

    private void postError(UploadCallback callback, String message) {
        if (callback == null) return;

        MAIN_HANDLER.post(() -> callback.onError(message));
    }

    public interface UploadCallback {
        void onSuccess(String mediaUrl);

        void onError(String message);
    }
}