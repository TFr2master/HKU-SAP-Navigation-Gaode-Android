package dupd.hku.com.hkusap.manager;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dupd.hku.com.hkusap.HKUApplication;
import dupd.hku.com.hkusap.model.POI;
import dupd.hku.com.hkusap.model.POIImage;
import dupd.hku.com.hkusap.model.POILayer;
import dupd.hku.com.hkusap.model.POIOpeningDetail;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class POIManager {
    POIStorage storage;
    POILayerDownloader downloader;
    Map<String, Long> monitoredKeysAndVersions;
    private static POIManager instance =  new POIManager();
    public POIManager() {
        storage = new POIStorage(diskStorePath());
        downloader = new POILayerDownloader();
        monitoredKeysAndVersions = new HashMap<String, Long>();
    }

    public static POIManager getInstance() {
        return instance;
    }

    public String diskStorePath() {
        File outFile = HKUApplication.sAPP.getBaseContext().getDatabasePath("data.db");
        return outFile.getPath();
    }

    public List<String> expiredKeysForKeys(List<String> keys, Map<String, Long> versions) {
        List<String> list = new ArrayList<String>();
        keys.forEach(item -> {
            long oldVersion = monitoredKeysAndVersions.get(item);
            long newVersion = versions.get(item);
            if (oldVersion != newVersion) {
                list.add(item);
            }
        });
        return list;
    }

    public void downloadPOILayersForTiles(List<String> tileCodes) {
        String json = downloader.downloadPOILayersForTiles(tileCodes);
        storePOIJson(json);
    }

    public List<POILayer> retrievePOILayersForTiles(List<String> codes) {
        List<POILayer> layers = new ArrayList<POILayer>();
        List<String> downloadCodes = new ArrayList<String>();
        for (String code: codes) {
            if (storage.containsPOILayerForKey(code)) {
                POILayer layer = storage.getPOILayer(code);
                layers.add(layer);
                monitoredKeysAndVersions.put(layer.tile_code, layer.version);
            } else {
                downloadCodes.add(code);
            }
        }
        List<POILayer> downloadedLayers = new ArrayList<POILayer>();
        String json = downloader.downloadPOILayersForTiles(downloadCodes);
        return storePOIJson(json);
    }

    public List<POILayer> storePOIJson(String json)  {
        List<POILayer> layers = new ArrayList<POILayer>();
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray tiles = jsonObject.getJSONArray("tiles");
            for (int i = 0; i < tiles.length(); i++) {
                JSONObject tile = (JSONObject) tiles.get(i);
                POILayer layer = new POILayer();
                layer.tile_code = tile.getString("tile_code");
                layer.version = tile.getLong("tile_version");
                List<POI> pois = new ArrayList<POI>();
                JSONArray poiArray =tile.getJSONArray("pois");
                for (int j = 0; j < poiArray.length(); j++) {
                    JSONObject poiObject = (JSONObject) poiArray.get(j);
                    POI poi = new POI();
                    poi.title = poiObject.getString("title");
                    poi.level_code = poiObject.getString("level_code");
                    poi.name = poiObject.getString("name");
                    poi.map_scale = poiObject.getInt("map_scale");
                    poi.staff = poiObject.getString("staff");
                    //poi.visitor = poiObject.getBoolean("visitor");
                    poi.tile_code = poiObject.getString("tile_code");
                    //poi.interest_type = poiObject.getInt("interest_type");
                    //poi.parking_field = poiObject.getBoolean("parking_field");
                    JSONObject introduction = poiObject.getJSONObject("introduction");
                    poi.building_name = poiObject.getString("building_name");
                    poi.descriptions_title = introduction.getString("title");
                    poi.descriptions_text = introduction.getString("text");
                    poi.website = poiObject.getString("website");
                    poi.tel = poiObject.getString("tel");
                    poi.address = poiObject.getString("address");
                    poi.latitude = poiObject.getDouble("latitude");
                    poi.longitude = poiObject.getDouble("longitude");
                    poi.poi_type = poiObject.getString("poi_type");
                    List<POIImage> images = new ArrayList<POIImage>();
                    JSONArray imageArray = poiObject.getJSONArray("images");
                    for (int k = 0; k < imageArray.length(); k++) {
                        POIImage image = new POIImage();
                        JSONObject imageObject = (JSONObject) imageArray.get(k);
                        image.url = imageObject.getString("url");
                        images.add(image);
                    }
                    poi.images = images;
                    List<POIOpeningDetail> details = new ArrayList<POIOpeningDetail>();
                    JSONArray detailArray = poiObject.getJSONArray("openning");
                    for (int k = 0; k < detailArray.length(); k++) {
                        JSONObject detailObject = (JSONObject) detailArray.get(k);
                        POIOpeningDetail detail = new POIOpeningDetail();
                        detail.detail_index = detailObject.getString("index");
                        detail.days = detailObject.getString("days");
                        detail.hours = detailObject.getString("hours");
                        details.add(detail);
                    }
                    poi.openingDetails = details;
                    pois.add(poi);
                }
                layer.pois = pois;
                updatePOILayer(layer);
                monitoredKeysAndVersions.put(layer.tile_code, layer.version);
                layers.add(layer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return layers;
        }
    }

    public void storePOILayer(POILayer layer) {
        storage.storePOILayer(layer);
    }

    public void updatePOILayer(POILayer layer) {
        removePOILayerForTileCode(layer.tile_code);
        storePOILayer(layer);
    }

    public boolean containsPOILayerForTileCode(String tileCode) {
        return storage.containsPOILayerForKey(tileCode);
    }

    public void removePOILayerForTileCode(String tileCode) {
        storage.removePOILayer(tileCode);
    }
}
