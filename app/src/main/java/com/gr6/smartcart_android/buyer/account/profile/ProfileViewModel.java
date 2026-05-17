package com.gr6.smartcart_android.buyer.account.profile;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.gr6.smartcart_android.buyer.account.profile.repository.ProfileRepository;
import com.gr6.smartcart_android.buyer.account.profile.request.ProfileUpdateRequest;
import com.gr6.smartcart_android.buyer.account.profile.response.ProfileResponse;

public class ProfileViewModel extends AndroidViewModel {

    private final ProfileRepository repository;
    private final MutableLiveData<ProfileState> profileState = new MutableLiveData<>();
    private final MutableLiveData<ProfileState> updateState = new MutableLiveData<>();

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        repository = new ProfileRepository(application);
    }

    public LiveData<ProfileState> getProfileState() {
        return profileState;
    }

    public LiveData<ProfileState> getUpdateState() {
        return updateState;
    }

    public void loadProfile() {
        profileState.setValue(ProfileState.loading());

        repository.getProfile(new ProfileRepository.ProfileCallback() {
            @Override
            public void onSuccess(ProfileResponse data, String message) {
                profileState.postValue(ProfileState.success(data, message));
            }

            @Override
            public void onError(String message) {
                profileState.postValue(ProfileState.error(message));
            }
        });
    }

    public void updateProfile(
            String fullName,
            String phoneNumber,
            String avatarUrl
    ) {
        updateState.setValue(ProfileState.loading());

        ProfileUpdateRequest request = new ProfileUpdateRequest(
                fullName,
                phoneNumber,
                avatarUrl
        );

        repository.updateProfile(request, new ProfileRepository.ProfileCallback() {
            @Override
            public void onSuccess(ProfileResponse data, String message) {
                updateState.postValue(ProfileState.success(data, message));
            }

            @Override
            public void onError(String message) {
                updateState.postValue(ProfileState.error(message));
            }
        });
    }
}