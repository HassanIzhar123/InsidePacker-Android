package com.wireguard.insidepacker_android.view_models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.wireguard.insidepacker_android.MyApp;
import com.wireguard.insidepacker_android.utils.AppUrls;

import org.json.JSONException;
import org.json.JSONObject;

public class SignInViewModel extends ViewModel {

    private MutableLiveData<String> responseData;

    public LiveData<String> getData() {
        if (responseData == null) {
            responseData = new MutableLiveData<>();
            loadData();
        }
        return responseData;
    }

    private void loadData() {
        String accessTokenUrl = new AppUrls().getAccessTokenUrl();
        RequestQueue queue = Volley.newRequestQueue(MyApp.getAppContext());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, accessTokenUrl, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String data = response.getString("data");
                            responseData.setValue(data);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error
                        responseData.setValue("Error occurred " + error.getMessage());
                    }
                });

        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);
    }
}
