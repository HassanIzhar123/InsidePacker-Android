package com.wireguard.insidepacket_android.Api;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.wireguard.insidepacket_android.DataStructure.StaticData;
import com.wireguard.insidepacket_android.Interfaces.VolleyCallback;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
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

    public void postRequest(String url, JSONObject object, final VolleyCallback callback) {
        try {
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, object, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    callback.onSuccess(response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    String body;
                    if (error != null && error.networkResponse != null) {
                        if (error.networkResponse.data != null) {
                            body = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                            callback.onError(body);
                        }
                    } else {
                        callback.onError("Error");
                    }
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

    public void getRequest(String url, String tunnel, String username, String accessToken, VolleyCallback callback) {
        try {
            JsonObjectRequest request = new JsonObjectRequest(url, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject tenantListResult) {
                    try {
                        JSONArray items = tenantListResult.getJSONArray("items");
                        if (items.length() > 0) {
                            JSONObject item = new JSONObject(items.get(0).toString());
                            //Log.e("tenantListResult", "" + item);
                            String tunnelUrl = StaticData.getTunnelUrl(tunnel, username, item.optString("tunnel_id"));
                            JsonObjectRequest request = new JsonObjectRequest(tunnelUrl, new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject tunnelJson) {
                                    callback.onSuccess(tenantListResult, tunnelJson);
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    String body;
                                    if (error != null) {
                                        if (error.networkResponse.data != null) {
                                            body = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                                            callback.onError(body);
                                        }
                                    } else {
                                        callback.onError("Error");
                                    }
                                }
                            }) {
                                @Override
                                public Map<String, String> getHeaders() {
                                    Map<String, String> headers = new HashMap<>();
                                    headers.put("Content-Type", "application/json");
                                    headers.put("Cookie", "Authorization=" + accessToken.trim());
                                    return headers;
                                }
                            };
                            addToRequestQueue(request);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        callback.onError(e.getMessage());
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    String body;
                    if (error != null) {
                        if (error.networkResponse.data != null) {
                            body = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                            callback.onError(body);
                        }
                    } else {
                        callback.onError("Error");
                    }
                }
            }) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Cookie", "Authorization=" + accessToken.trim());
                    return headers;
                }
            };
            addToRequestQueue(request);
        } catch (Exception e) {
            e.printStackTrace();
            callback.onError(e.getMessage());
        }
    }

    public void getRequest(String url, String accessToken, VolleyCallback callback) {
        try {
            JsonObjectRequest request = new JsonObjectRequest (url, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject json) {
                    callback.onSuccess(json);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //Log.e("volleyError", ""+error);
                    String body;
                    if (error != null && error.networkResponse != null) {
                        if (error.networkResponse.data != null) {
                            body = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                            callback.onError(body);
                        }
                    } else {
                        callback.onError("Error");
                    }
                }
            }) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Cookie", "Authorization=" + accessToken.trim());
                    return headers;
                }
            };
            addToRequestQueue(request);
        } catch (Exception e) {
            e.printStackTrace();
            //Log.e("error", ""+e.getStackTrace().toString());
            callback.onError(e.getMessage());
        }
    }

    public void getRequest(String url, VolleyCallback volleyCallback) {
        try {
            JsonObjectRequest request = new JsonObjectRequest(url, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    volleyCallback.onSuccess(response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    String body;
                    if (error != null && error.networkResponse != null) {
                        if (error.networkResponse.data != null) {
                            body = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                            volleyCallback.onError(body);
                        }
                    } else {
                        volleyCallback.onError("Error");
                    }
                }
            });
            addToRequestQueue(request);
        } catch (Exception e) {
            e.printStackTrace();
            volleyCallback.onError(e.getMessage());
        }
    }
}