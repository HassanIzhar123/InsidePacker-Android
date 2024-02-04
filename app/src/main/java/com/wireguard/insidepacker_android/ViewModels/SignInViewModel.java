package com.wireguard.insidepacker_android.ViewModels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.wireguard.insidepacker_android.models.AccessToken.AccessToken;
import com.wireguard.insidepacker_android.models.BasicInformation.BasicInformation;
import com.wireguard.insidepacker_android.repository.SignInRepo;

public class SignInViewModel extends AndroidViewModel {
    LiveData<AccessToken> signInLiveData;
    SignInRepo mainHomeRepo;

    public SignInViewModel(@NonNull Application application) {
        super(application);
        mainHomeRepo = new SignInRepo();
    }

    public LiveData<AccessToken> getAccessToken(BasicInformation basicInformation) {
        signInLiveData = mainHomeRepo.getAccessToken(basicInformation);
        return signInLiveData;
    }
}
