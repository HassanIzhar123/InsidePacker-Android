package com.wireguard.insidepacker_android.ViewModels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.wireguard.insidepacker_android.models.BasicInformation.BasicInformation;
import com.wireguard.insidepacker_android.repository.SignInRepo;
import com.wireguard.insidepacker_android.utils.StateLiveData;

public class SignInViewModel extends AndroidViewModel {
    StateLiveData<?> signInLiveData;
    SignInRepo mainHomeRepo;

    public SignInViewModel(@NonNull Application application) {
        super(application);
        mainHomeRepo = new SignInRepo();
    }

    public StateLiveData<?> getAccessToken(BasicInformation basicInformation) {
        signInLiveData = mainHomeRepo.getAccessToken(basicInformation);
        return signInLiveData;
    }
}
