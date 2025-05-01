package com.example.frases.api;

import android.content.Context;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class QuoteApi {
    private static final String BASE_URL = "https://dummyjson.com/quotes/random";
    private RequestQueue requestQueue;

    public QuoteApi(Context context) {
        requestQueue = Volley.newRequestQueue(context.getApplicationContext());
    }

    public void getRandomQuote(Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, BASE_URL, null, listener, errorListener) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("User-Agent", "Mozilla/5.0 (Android)");
                return headers;
            }
        };
        requestQueue.add(request);
    }
}

