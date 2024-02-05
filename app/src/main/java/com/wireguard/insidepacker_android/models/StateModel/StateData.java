package com.wireguard.insidepacker_android.models.StateModel;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class StateData<T> {

    @NonNull
    private DataStatus status;

    @Nullable
    private T data;

    @Nullable
    private String error;

    public StateData() {
        this.status = DataStatus.CREATED;
        this.data = null;
        this.error = null;
    }

    public StateData<T> loading() {
        this.status = DataStatus.LOADING;
        this.data = null;
        this.error = null;
        return this;
    }

    public StateData<T> success(@NonNull T data) {
        this.status = DataStatus.SUCCESS;
        this.data = data;
        this.error = null;
        return this;
    }

    public StateData<T> error(@NonNull String error) {
        this.status = DataStatus.ERROR;
        this.data = null;
        this.error = error;
        return this;
    }

    public StateData<T> complete() {
        this.status = DataStatus.COMPLETE;
        return this;
    }

    @NonNull
    public DataStatus getStatus() {
        return status;
    }

    @Nullable
    public T getData() {
        return data;
    }

    @Nullable
    public String getError() {
        return error;
    }

    public enum DataStatus {
        CREATED, SUCCESS, ERROR, LOADING, COMPLETE
    }
}