package com.wireguard.insidepacker_android.ViewModels.HomeViewModel;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.wireguard.insidepacker_android.models.ConfigModel.ConfigModel;
import com.wireguard.insidepacker_android.models.BasicInformation.BasicInformation;
import com.wireguard.insidepacker_android.Interfaces.ViewModelCallBacks;
import com.wireguard.insidepacker_android.models.ConfigModel.ConfigModel;
import com.wireguard.insidepacker_android.repository.HomeRepo;

import org.json.JSONObject;

public class HomeViewModel extends AndroidViewModel {
    MutableLiveData<ConfigModel> configMutableLiveData;
    MutableLiveData<String> errorMutableLiveData;
    HomeRepo mainHomeRepo;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        mainHomeRepo = new HomeRepo();
        configMutableLiveData = new MutableLiveData<>();
        errorMutableLiveData = new MutableLiveData<>();
    }

    public MutableLiveData<ConfigModel> getConfigMutableLiveData() {
        return configMutableLiveData;
    }

    public MutableLiveData<String> getErrorMutableLiveData() {
        return errorMutableLiveData;
    }

    public void getConfig(Context context) {
        mainHomeRepo.getConfiguration(context, new ViewModelCallBacks() {
            @Override
            public void onSuccess(JSONObject result) {
                configMutableLiveData.postValue(new Gson().fromJson(result.toString(), ConfigModel.class));
            }

            @Override
            public void onError(String message) {
                errorMutableLiveData.postValue(message);
            }
        });
    }
}
