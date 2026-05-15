package com.gr6.smartcart_android.buyer.address;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.gr6.smartcart_android.buyer.address.repository.AddressRepository;
import com.gr6.smartcart_android.buyer.address.request.AddressRequest;
import com.gr6.smartcart_android.buyer.address.response.AddressResponse;

import java.util.List;

public class AddressViewModel extends AndroidViewModel {

    private final AddressRepository repository;

    private final MutableLiveData<AddressListState> addressListState = new MutableLiveData<>();
    private final MutableLiveData<AddressActionState> actionState = new MutableLiveData<>();

    public AddressViewModel(@NonNull Application application) {
        super(application);
        repository = new AddressRepository(application);
    }

    public LiveData<AddressListState> getAddressListState() {
        return addressListState;
    }

    public LiveData<AddressActionState> getActionState() {
        return actionState;
    }

    public void loadAddresses() {
        addressListState.setValue(AddressListState.loading());

        repository.getMyAddresses(new AddressRepository.AddressListCallback() {
            @Override
            public void onSuccess(List<AddressResponse> addresses) {
                addressListState.postValue(AddressListState.success(addresses));
            }

            @Override
            public void onError(String message) {
                addressListState.postValue(AddressListState.error(message));
            }
        });
    }

    public void createAddress(AddressRequest request) {
        actionState.setValue(AddressActionState.loading());

        repository.createAddress(request, new AddressRepository.AddressActionCallback() {
            @Override
            public void onSuccess(AddressResponse address, String message) {
                actionState.postValue(AddressActionState.success(message));
            }

            @Override
            public void onError(String message) {
                actionState.postValue(AddressActionState.error(message));
            }
        });
    }

    public void updateAddress(Long addressId, AddressRequest request) {
        actionState.setValue(AddressActionState.loading());

        repository.updateAddress(addressId, request, new AddressRepository.AddressActionCallback() {
            @Override
            public void onSuccess(AddressResponse address, String message) {
                actionState.postValue(AddressActionState.success(message));
            }

            @Override
            public void onError(String message) {
                actionState.postValue(AddressActionState.error(message));
            }
        });
    }

    public void deleteAddress(Long addressId) {
        actionState.setValue(AddressActionState.loading());

        repository.deleteAddress(addressId, new AddressRepository.SimpleCallback() {
            @Override
            public void onSuccess(String message) {
                actionState.postValue(AddressActionState.success(message));
            }

            @Override
            public void onError(String message) {
                actionState.postValue(AddressActionState.error(message));
            }
        });
    }

    public void setDefaultAddress(Long addressId) {
        actionState.setValue(AddressActionState.loading());

        repository.setDefaultAddress(addressId, new AddressRepository.SimpleCallback() {
            @Override
            public void onSuccess(String message) {
                actionState.postValue(AddressActionState.success(message));
            }

            @Override
            public void onError(String message) {
                actionState.postValue(AddressActionState.error(message));
            }
        });
    }
}