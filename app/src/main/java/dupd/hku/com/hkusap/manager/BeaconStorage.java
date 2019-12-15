package dupd.hku.com.hkusap.manager;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import dupd.hku.com.hkusap.model.Beacon;
import dupd.hku.com.hkusap.model.BeaconLayer;
//import com.tencent.wcdb.database.SqLiteDatabase;


public class BeaconStorage {

    SQLiteDatabase database;
    public BeaconStorage() {
        this("");
    }

    public BeaconStorage(String path) {
        database = SQLiteDatabase.openOrCreateDatabase(path, null);
        database.execSQL("create table if not exists beacon_layer(tile_code text unique, " +
                "version long);");
        database.execSQL("create table if not exists beacon(uuid text, plate_id text, " +
                "tile_code text, level_code text, name text, searchable int, speakout text, " +
                "room_id text, room_name text,building_id text, building_name text, type integer, postal_code text, " +
                "m_power integer, latitude double, longitude double, indoor int);");
    }

    public boolean containsBeaconLayerForKey(String key) {
        Cursor cursor = database.query("beacon_layer",new String[]{"tile_code", "version"},
                "tile_code like ?", new String[]{key},null, null,
                null);
        return cursor.getCount() > 0;
    }

    public void storeBeaconLayer(BeaconLayer layer) {
        ContentValues cv = new ContentValues();
        cv.put("tile_code", layer.tile_code);
        cv.put("version", layer.version);

        long id = database.insertWithOnConflict("beacon_layer", null,
                cv, SQLiteDatabase.CONFLICT_REPLACE);
        if (id != -1) {
            List<Beacon> beacons = layer.beacons;
            database.delete("beacon", "tile_code like ?",
                    new String[]{layer.tile_code});
            beacons.stream().forEach(item -> {
                ContentValues beaconCV = new ContentValues();
                beaconCV.put("uuid", item.uuid);
                beaconCV.put("plate_id", item.plate_id);
                beaconCV.put("tile_code", item.tile_code);
                beaconCV.put("level_code", item.level_code);
                beaconCV.put("name", item.name);
                beaconCV.put("searchable", item.searchable);
                beaconCV.put("speakout", item.speakout);
                beaconCV.put("room_id", item.room_id);
                beaconCV.put("room_name", item.room_name);
                beaconCV.put("building_id", item.building_id);
                beaconCV.put("building_name", item.building_name);
                beaconCV.put("postal_code", item.postal_code);
                beaconCV.put("type", item.type);
                beaconCV.put("m_power", item.m_power);
                beaconCV.put("latitude", item.latitude);
                beaconCV.put("longitude", item.longitude);
                beaconCV.put("indoor", item.indoor);
                database.insert("beacon", null, beaconCV);
            });
        }
    }

    public void removeBeaconLayer(String tile_code) {
        database.delete("beacon_layer", "tile_code like ?", new String[]{tile_code});
        database.delete("beacon", "tile_code like ?", new String[]{tile_code});
    }

    public BeaconLayer getBeaconLayer(String tile_code) {
        Cursor cursor = database.query("beacon_Layer", new String[]{"tile_code", "version"},
                "tile_code like ?", new String[]{tile_code}, null, null,
                null);
        if (cursor.moveToNext()) {
            BeaconLayer beaconLayer = new BeaconLayer();
            int i;
            i = cursor.getColumnIndex("tile_code");
            beaconLayer.tile_code = cursor.getString(i);
            i = cursor.getColumnIndex("version");
            beaconLayer.version = cursor.getLong(i);
            List<Beacon> beacons= new ArrayList<Beacon>();
            Cursor beaconCursor = database.query("beacon", new String[]{"uuid", "plate_id",
                            "tile_code", "level_code", "name", "searchable", "speakout", "room_id",
                            "room_name", "building_id", "building_name", "type", "m_power",
                            "latitude", "longitude", "indoor", "postal_code"},
                    "tile_code like ?", new String[]{tile_code}, null,
                    null, null);
            while (beaconCursor.moveToNext()) {
                Beacon beacon = new Beacon();
                int j;
                j = beaconCursor.getColumnIndex("uuid");
                beacon.uuid = beaconCursor.getString(j);
                j = beaconCursor.getColumnIndex("plate_id");
                beacon.plate_id = beaconCursor.getString(j);
                j = beaconCursor.getColumnIndex("tile_code");
                beacon.tile_code = beaconCursor.getString(j);
                j = beaconCursor.getColumnIndex("level_code");
                beacon.level_code = beaconCursor.getString(j);
                j = beaconCursor.getColumnIndex("name");
                beacon.name = beaconCursor.getString(j);
                j = beaconCursor.getColumnIndex("searchable");
                beacon.searchable = beaconCursor.getInt(j) != 0;
                j = beaconCursor.getColumnIndex("speakout");
                beacon.speakout = beaconCursor.getString(j);
                j = beaconCursor.getColumnIndex("room_id");
                beacon.room_id = beaconCursor.getString(j);
                j = beaconCursor.getColumnIndex("room_name");
                beacon.room_name = beaconCursor.getString(j);
                j = beaconCursor.getColumnIndex("building_id");
                beacon.building_id = beaconCursor.getString(j);
                j = beaconCursor.getColumnIndex("builidng_name");
                beacon.building_name = beaconCursor.getString(j);
                j = beaconCursor.getColumnIndex("postal_code");
                beacon.postal_code = beaconCursor.getString(j);
                j = beaconCursor.getColumnIndex("type");
                beacon.type = beaconCursor.getLong(j);
                j = beaconCursor.getColumnIndex("m_power");
                beacon.m_power = beaconCursor.getLong(j);
                j = beaconCursor.getColumnIndex("latitude");
                beacon.latitude = beaconCursor.getDouble(j);
                j = beaconCursor.getColumnIndex("longitude");
                beacon.longitude = beaconCursor.getDouble(j);
                j = beaconCursor.getColumnIndex("indoor");
                beacon.indoor = beaconCursor.getInt(j) != 0;
                beacons.add(beacon);
            }
            beaconLayer.beacons = beacons;
            return beaconLayer;
        } else {
            return null;
        }
    }

    public Beacon getBeaconByUuid(String uuid) {
        Cursor cursor = database.query("beacon", new String[]{"uuid", "plate_id",
                "tile_code", "level_code", "name", "searchable", "speakout", "room_id",
                "room_name", "building_id", "building_name", "type", "m_power",
                "latitude", "longitude", "indoor", "postal_code"},
                "uuid like ?", new String[]{uuid}, null,
                null, null);
        if (cursor.moveToNext()) {
            Beacon beacon = new Beacon();
            int j;
            j = cursor.getColumnIndex("uuid");
            beacon.uuid = cursor.getString(j);
            j = cursor.getColumnIndex("plate_id");
            beacon.plate_id = cursor.getString(j);
            j = cursor.getColumnIndex("tile_code");
            beacon.tile_code = cursor.getString(j);
            j = cursor.getColumnIndex("level_code");
            beacon.level_code = cursor.getString(j);
            j = cursor.getColumnIndex("name");
            beacon.name = cursor.getString(j);
            j = cursor.getColumnIndex("searchable");
            beacon.searchable = cursor.getInt(j) != 0;
            j = cursor.getColumnIndex("speakout");
            beacon.speakout = cursor.getString(j);
            j = cursor.getColumnIndex("room_id");
            beacon.room_id = cursor.getString(j);
            j = cursor.getColumnIndex("room_name");
            beacon.room_name = cursor.getString(j);
            j = cursor.getColumnIndex("building_id");
            beacon.building_id = cursor.getString(j);
            j = cursor.getColumnIndex("builidng_name");
            beacon.building_name = cursor.getString(j);
            j = cursor.getColumnIndex("postal_code");
            beacon.postal_code = cursor.getString(j);
            j = cursor.getColumnIndex("type");
            beacon.type = cursor.getLong(j);
            j = cursor.getColumnIndex("m_power");
            beacon.m_power = cursor.getLong(j);
            j = cursor.getColumnIndex("latitude");
            beacon.latitude = cursor.getDouble(j);
            j = cursor.getColumnIndex("longitude");
            beacon.longitude = cursor.getDouble(j);
            j = cursor.getColumnIndex("indoor");
            beacon.indoor = cursor.getInt(j) != 0;
            return beacon;
        } else {
            return null;
        }
    }
}
