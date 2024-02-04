package com.wireguard.insidepacker_android.Api;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.wireguard.insidepacker_android.DataStructure.StaticData;
import com.wireguard.insidepacker_android.Interfaces.VolleyCallback;
import com.wireguard.insidepacker_android.models.BasicInformation.BasicInformation;
import org.json.JSONObject;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class ApiClient {

    private static final String BASE_URL = StaticData.baseUrl;
    private static ApiClient instance;
    private final RequestQueue requestQueue;
    private final Context ctx;

    private ApiClient(Context context) {
        ctx = context;
        requestQueue = getRequestQueue();
    }

    public static synchronized ApiClient getInstance(Context context) {
        if (instance == null) {
            instance = new ApiClient(context);
        }
        return instance;
    }

    @SuppressLint("CustomX509TrustManager")
    private RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            SSLContext sslContext = null;
            try {
                sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, new TrustManager[]{new X509TrustManager() {
                    @SuppressLint("TrustAllX509TrustManager")
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    @SuppressLint("TrustAllX509TrustManager")
                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }}, null);
            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                e.printStackTrace();
            }
            assert sslContext != null;
            MyHurlStack hurlStack = new MyHurlStack(null, sslContext.getSocketFactory());
            return Volley.newRequestQueue(ctx.getApplicationContext(), hurlStack);
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    public void getAccessToken(BasicInformation basicInformation, final VolleyCallback callback) {
        try {
            String url = BASE_URL + "get_token";
            String json = basicInformation.toJson();
            JSONObject object = new JSONObject(json);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, object, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    callback.onSuccess(response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    callback.onError(error.getMessage());
                }
            }) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };
            addToRequestQueue(request);
        } catch (Exception e) {
            e.printStackTrace();
            callback.onError(e.getMessage());
        }
    }


}