package com.gr6.smartcart_android.seller.cloudinary;

import android.content.Context;
import android.net.Uri;

import com.gr6.smartcart_android.common.cloudinary.CloudinaryRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Uploader dùng cho seller.
 *
 * Không dùng Cloudinary Android SDK nữa để tránh lỗi:
 * package com.cloudinary.android does not exist
 *
 * File này bọc lại common.cloudinary.CloudinaryRepository,
 * upload từng ảnh bằng Retrofit lên Cloudinary.
 */
public class CloudinaryUploader {

    private CloudinaryUploader() {
    }

    public static void uploadMany(
            Context context,
            List<Uri> uris,
            UploadManyCallback callback
    ) {
        if (callback == null) {
            return;
        }

        if (context == null) {
            callback.onError("Context không hợp lệ");
            return;
        }

        if (uris == null || uris.isEmpty()) {
            callback.onSuccess(new ArrayList<>());
            return;
        }

        CloudinaryRepository repository = new CloudinaryRepository();
        List<String> uploadedUrls = new ArrayList<>();

        uploadAt(
                context.getApplicationContext(),
                repository,
                uris,
                0,
                uploadedUrls,
                callback
        );
    }

    private static void uploadAt(
            Context context,
            CloudinaryRepository repository,
            List<Uri> uris,
            int index,
            List<String> uploadedUrls,
            UploadManyCallback callback
    ) {
        if (index >= uris.size()) {
            callback.onSuccess(uploadedUrls);
            return;
        }

        Uri currentUri = uris.get(index);

        if (currentUri == null) {
            uploadAt(
                    context,
                    repository,
                    uris,
                    index + 1,
                    uploadedUrls,
                    callback
            );
            return;
        }

        callback.onProgress(index + 1, uris.size());

        repository.uploadImage(context, currentUri, new CloudinaryRepository.UploadCallback() {
            @Override
            public void onSuccess(String imageUrl) {
                if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                    uploadedUrls.add(imageUrl.trim());
                }

                uploadAt(
                        context,
                        repository,
                        uris,
                        index + 1,
                        uploadedUrls,
                        callback
                );
            }

            @Override
            public void onError(String message) {
                callback.onError(message == null ? "Upload ảnh thất bại" : message);
            }
        });
    }

    public interface UploadManyCallback {
        void onProgress(int current, int total);

        void onSuccess(List<String> urls);

        void onError(String message);
    }
}


