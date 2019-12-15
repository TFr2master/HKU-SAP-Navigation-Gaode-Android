package dupd.hku.com.hkusap.manager;

import java.io.IOException;
import java.util.List;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class POILayerDownloader {
    public String downloadPOILayersForTiles(List<String> tileCodes){
        OkHttpClient client = new OkHttpClient();
        String tileCodeString = String.join(";", (String[])tileCodes.toArray(new String[0]));
        RequestBody body = new FormBody.Builder()
                .add("tile_code", tileCodeString)
                .build();
        Request request = new Request.Builder()
                .url("http://118.31.57.210/apis/poi")
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful())
                throw new IOException("Unexpected code " + response);
            return response.body().string();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String checkoutPOIVersionsForTiles(List<String> tileCodes) {
        OkHttpClient client = new OkHttpClient();
        String tileCodeString = String.join(";", (String[]) tileCodes.toArray());
        RequestBody body = new FormBody.Builder()
                .add("tile_code", tileCodeString)
                .build();
        Request request = new Request.Builder()
                .url("http://118.31.57.210/apis/poi_version")
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful())
                throw new IOException("Unexpected code " + response);
            return response.body().string();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
