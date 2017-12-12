import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Jarvist on 2017/11/13.
 */

public class HttpUtils {
    private OkHttpClient mclient;

    public HttpUtils (OkHttpClient client){
        mclient = client;
    }

    public void get(final String url, final okhttp3.Callback callback){
        Request request = new Request.Builder()
                .addHeader("api-key","UNEdJ7p30oSi2epJFxomDwi2b38=")
                .url(url)
                .build();
        Call call = mclient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                }
            });
    }

    public void post(final String url,final okhttp3.Callback callback){

    }
}
