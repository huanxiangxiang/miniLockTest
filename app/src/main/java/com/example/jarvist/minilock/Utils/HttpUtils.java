package com.example.jarvist.minilock.Utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.baidu.mapapi.search.core.SearchResult;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Jarvist on 2017/11/13.
 */



public class HttpUtils {

    private static final String URL_TYPE = "/datapoints?type=3";
    private static final String APIKEY = "UNEdJ7p30oSi2epJFxomDwi2b38=";
    private static final String API_KEY = "api-key";
    private static OkHttpClient mclient = new OkHttpClient();
    private Context mContext;


    public static void get(final Context context,String url, final String deviceId){
        Request request = new Request.Builder()
                .addHeader(API_KEY,APIKEY)
                .url(url+deviceId+URL_TYPE)
                .build();
        Call call = mclient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Toast.makeText(context,"success!",Toast.LENGTH_SHORT).show();
                }
            });
    }

    public static void post(Context context,String url, final String id, JSONObject object){
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(JSON,object.toString());
        String mUrl = url + id + URL_TYPE;
        Request request = new Request.Builder()
                .url(mUrl)
                .addHeader(API_KEY,APIKEY)
                .post(requestBody)
                .build();
        Call call = mclient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.i("post","Success post");
                }
            });
    }
}
