package dupd.hku.com.hkusap.manager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dupd.hku.com.hkusap.HKUApplication;
import dupd.hku.com.hkusap.model.Beacon;
import dupd.hku.com.hkusap.model.BeaconLayer;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class BeaconManager {
    BeaconStorage storage;
    BeaconLayerDownloader downloader;
    Map<String, Long> monitoredKeysAndVersions;
    private static BeaconManager instance =  new BeaconManager();
    public BeaconManager() {
        storage = new BeaconStorage(diskStorePath());
        downloader = new BeaconLayerDownloader();
        monitoredKeysAndVersions = new HashMap<String, Long>();
    }

    public static BeaconManager getInstance() {
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

    public void downloadBeaconLayersForTiles(List<String> tileCodes) {
        String json = downloader.downloadBeaconLayersForTiles(tileCodes);
        storeBeaconJson(json);
    }

    public List<BeaconLayer> retrieveBeaconLayersForTiles(List<String> codes) {
        List<BeaconLayer> layers = new ArrayList<BeaconLayer>();
        List<String> downloadCodes = new ArrayList<String>();
        for (String code: codes) {
            if (storage.containsBeaconLayerForKey(code)) {
                BeaconLayer layer = storage.getBeaconLayer(code);
                layers.add(layer);
                monitoredKeysAndVersions.put(layer.tile_code, layer.version);
            } else {
                downloadCodes.add(code);
            }
        }
        List<BeaconLayer> downloadedLayers = new ArrayList<BeaconLayer>();
        String json = downloader.downloadBeaconLayersForTiles(downloadCodes);
        return storeBeaconJson(json);
    }

    public List<BeaconLayer> storeBeaconJson(String json) {
        List<BeaconLayer> layers = new ArrayList<BeaconLayer>();
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray tiles = jsonObject.getJSONArray("tiles");
            for (int i = 0; i < tiles.length(); i++) {
                JSONObject tile = (JSONObject) tiles.get(i);
                BeaconLayer layer = new BeaconLayer();
                layer.tile_code = tile.getString("tile_code");
                layer.version = tile.getLong("tile_version");
                JSONArray beaconArray = tile.getJSONArray("beacons");
                List<Beacon> beacons = new ArrayList<Beacon>();
                for (int j = 0; j < beaconArray.length(); j++) {
                    JSONObject beaconObject = (JSONObject)beaconArray.get(j);
                    Beacon beacon = new Beacon();
                    beacon.uuid = Beacon.formatUUID(beaconObject.getString("uuid"));
                    beacon.plate_id = beaconObject.getString("plate_code");
                    beacon.tile_code = beaconObject.getString("tile_code");
                    beacon.level_code = beaconObject.getString("level_code");
                    beacon.name = beaconObject.getString("name");
                    beacon.searchable = (beaconObject.getLong("searchable") == 1);
                    beacon.speakout = beaconObject.getString("speakout");
                    beacon.room_id = beaconObject.getString("room_code");
                    beacon.room_name = beaconObject.getString("room_name");
                    beacon.building_id = beaconObject.getString("building_id");
                    beacon.building_name = beaconObject.getString("building_name");
                    beacon.postal_code = beaconObject.getString("postal_code");
                    beacon.type = beaconObject.getLong("beacon_type");
                    beacon.m_power = beaconObject.getLong("m_power");
                    beacon.latitude = beaconObject.getDouble("latitude");
                    beacon.longitude = beaconObject.getDouble("longitude");
                    beacon.indoor = (beaconObject.getLong("indoor") == 1);
                    beacons.add(beacon);
                }
                layer.beacons = beacons;
                storeBeaconLayer(layer);
                monitoredKeysAndVersions.put(layer.tile_code, layer.version);
                layers.add(layer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return layers;
        }
    }

    public void storeBeaconLayer(BeaconLayer layer) {
        storage.storeBeaconLayer(layer);
    }

    public void updateBeaconLayer(BeaconLayer layer) {
        removeBeaconLayerForTileCode(layer.tile_code);
        storeBeaconLayer(layer);
    }

    public boolean containsBeaconLayerForTileCode(String tileCode) {
        return storage.containsBeaconLayerForKey(tileCode);
    }

    public void removeBeaconLayerForTileCode(String tileCode) {
        storage.removeBeaconLayer(tileCode);
    }

    public Beacon beaconForUuid(String uuid) {
        return storage.getBeaconByUuid(uuid);
    }
}
