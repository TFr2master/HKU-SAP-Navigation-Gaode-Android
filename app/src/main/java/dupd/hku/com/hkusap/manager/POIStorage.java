package dupd.hku.com.hkusap.manager;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import dupd.hku.com.hkusap.model.POI;
import dupd.hku.com.hkusap.model.POIImage;
import dupd.hku.com.hkusap.model.POILayer;
import dupd.hku.com.hkusap.model.POIOpeningDetail;

public class POIStorage {
    SQLiteDatabase database;
    public POIStorage() {
        this("");
    }

    public POIStorage(String path) {
        database = SQLiteDatabase.openOrCreateDatabase(path, null);
        database.execSQL("create table if not exists poi_layer(tile_code text unique, " +
                "version long);");
        database.execSQL("create table if not exists poi(id integer primary key autoincrement, title text, " +
                "level_code text, name text, map_scale int, staff text, visitor int, " +
                "tile_code text, interest_type int, parking_field int," +
                "descriptions_title text, descriptions_text text, website text, " +
                "opening_text text, tel text, address text, latitude double, longitude double," +
                "poi_type text, building_name text);");
        database.execSQL("create table if not exists poi_image(poi_id int, url text);");
        database.execSQL("create table if not exists poi_openingdetail(poi_id int, " +
                "detail_index text, days text, hours text);");
    }

    public boolean containsPOILayerForKey(String key) {
        Cursor cursor = database.query("poi_layer",new String[]{"tile_code", "version"},
                "tile_code like ?", new String[]{key},null, null,
                null);
        return cursor.getCount() > 0;
    }

    public void storePOILayer(POILayer layer) {
        ContentValues cv = new ContentValues();
        cv.put("tile_code", layer.tile_code);
        cv.put("version", layer.version);

        long id = database.insertWithOnConflict("poi_layer", null,
                cv, SQLiteDatabase.CONFLICT_REPLACE);
        if (id != -1) {
            List<POI> pois = layer.pois;
            database.delete("poi", "tile_code like ?",
                    new String[]{layer.tile_code});
            pois.stream().forEach(item -> {
                ContentValues poiCV = new ContentValues();
                poiCV.put("title", item.title);
                poiCV.put("level_code", item.level_code);
                poiCV.put("name", item.name);
                poiCV.put("map_scale", item.map_scale);
                poiCV.put("staff", item.staff);
                poiCV.put("visitor", item.visitor);
                poiCV.put("tile_code", item.tile_code);
                poiCV.put("interest_type", item.interest_type);
                poiCV.put("parking_field", item.parking_field);
                poiCV.put("building_name", item.building_name);
                poiCV.put("descriptions_title", item.descriptions_title);
                poiCV.put("descriptions_text", item.descriptions_text);
                poiCV.put("website", item.website);
                poiCV.put("opening_text", item.opening_text);
                poiCV.put("tel", item.tel);
                poiCV.put("address", item.address);
                poiCV.put("latitude", item.latitude);
                poiCV.put("longitude", item.longitude);
                poiCV.put("poi_type", item.poi_type);
                long poi_id = database.insert("poi", null, poiCV);
                item.images.stream().forEach(imageItem -> {
                    ContentValues poiImageCV = new ContentValues();
                    poiImageCV.put("poi_id", poi_id);
                    poiImageCV.put("url", imageItem.url);
                    database.insert("poi_image", null, poiImageCV);
                });
                item.openingDetails.stream().forEach(detailItem -> {
                    ContentValues detailCV = new ContentValues();
                    detailCV.put("poi_id", poi_id);
                    detailCV.put("detail_index", detailItem.detail_index);
                    detailCV.put("days", detailItem.days);
                    detailCV.put("hours", detailItem.hours);
                    database.insert("poi_openingdetail", null, detailCV);
                });
            });
        }
    }

    public void removePOILayer(String tile_code) {
        database.delete("poi_layer", "tile_code like ?", new String[]{tile_code});
        Cursor cursor = database.query("poi", new String[]{"id"}, "tile_code like ?",
                new String[]{tile_code}, null, null, null);
        while (cursor.moveToNext()) {
            int poi_id = cursor.getInt(0);
            database.delete("poi_image", "poi_id = ?",
                    new String[]{Integer.toString(poi_id)});
            database.delete("poi_openingdetail", "poi_id = ?",
                    new String[]{Integer.toString(poi_id)});
        }
        database.delete("poi", "tile_code like ?", new String[]{tile_code});
    }

    public POILayer getPOILayer(String tile_code) {
        Cursor cursor = database.query("poi", new String[]{"tile_code", "version"},
                "tile_code like ?", new String[]{tile_code}, null, null,
                null);
        if (cursor.moveToNext()) {
            POILayer poiLayer = new POILayer();
            int i;
            i = cursor.getColumnIndex("tile_code");
            poiLayer.tile_code = cursor.getString(i);
            i = cursor.getColumnIndex("version");
            poiLayer.version = cursor.getLong(i);
            List<POI> pois= new ArrayList<POI>();
            Cursor poiCursor = database.query("poi", new String[]{"id", "title", "level_code",
                    "name", "map_scale", "staff", "visitor", "tile_code", "interest_type",
                    "parking_field", "descriptions_title", "descriptions_text", "website",
                    "opening_text", "address", "latitude", "longitude","poi_type", "building_name"},
                    "tile_code like ?", new String[]{tile_code}, null,
                    null, null);
            while (poiCursor.moveToNext()) {
                POI poi = new POI();
                int j;
                j = poiCursor.getColumnIndex("id");
                int curPoiId = poiCursor.getInt(j);

                j = poiCursor.getColumnIndex("title");
                poi.title = poiCursor.getString(j);
                j = poiCursor.getColumnIndex("level_code");
                poi.level_code = poiCursor.getString(j);
                j = poiCursor.getColumnIndex("name");
                poi.name = poiCursor.getString(j);
                j = poiCursor.getColumnIndex("map_scale");
                poi.map_scale = poiCursor.getInt(j);
                j = poiCursor.getColumnIndex("staff");
                poi.staff = poiCursor.getString(j);
                j = poiCursor.getColumnIndex("visitor");
                poi.visitor = poiCursor.getInt(j) != 0;
                j = poiCursor.getColumnIndex("tile_code");
                poi.tile_code = poiCursor.getString(j);
                j = poiCursor.getColumnIndex("interest_type");
                poi.interest_type = poiCursor.getInt(j);
                j = poiCursor.getColumnIndex("parking_field");
                poi.parking_field = poiCursor.getInt(j) != 0;
                j = poiCursor.getColumnIndex("descriptions_title");
                poi.descriptions_title = poiCursor.getString(j);
                j = poiCursor.getColumnIndex("descriptions_text");
                poi.descriptions_text = poiCursor.getString(j);
                j = poiCursor.getColumnIndex("building_name");
                poi.descriptions_text = poiCursor.getString(j);
                j = poiCursor.getColumnIndex("website");
                poi.website = poiCursor.getString(j);
                j = poiCursor.getColumnIndex("opening_text");
                poi.opening_text = poiCursor.getString(j);
                j = poiCursor.getColumnIndex("tel");
                poi.tel = poiCursor.getString(j);
                j = poiCursor.getColumnIndex("address");
                poi.address = poiCursor.getString(j);
                j = poiCursor.getColumnIndex("latitude");
                poi.latitude = poiCursor.getDouble(j);
                j = poiCursor.getColumnIndex("longitude");
                poi.longitude = poiCursor.getDouble(j);
                j = poiCursor.getColumnIndex("poi_type");
                poi.poi_type = poiCursor.getString(j);
                Cursor imageCursor = database.query("poi_image", new String[] {"url"},
                        "poi_id = ?", new String[]{Integer.toString(curPoiId)},
                        null, null, null);
                List<POIImage> images = new ArrayList<POIImage>();
                while (imageCursor.moveToNext()) {
                    POIImage image = new POIImage();
                    int k = imageCursor.getColumnIndex("url");
                    image.url = imageCursor.getString(k);
                    images.add(image);
                }
                poi.images = images;
                Cursor detailCursor = database.query("poi_openingdetail", new String[] {"detail_index", "days", "hours"},
                        "poi_id = ?", new String[]{Integer.toString(curPoiId)},
                        null, null, null);
                List<POIOpeningDetail> details = new ArrayList<POIOpeningDetail>();
                while (detailCursor.moveToNext()) {
                    POIOpeningDetail detail = new POIOpeningDetail();
                    int k;
                    k = detailCursor.getColumnIndex("detail_index");
                    detail.detail_index = detailCursor.getString(k);
                    k = detailCursor.getColumnIndex("days");
                    detail.days = detailCursor.getString(k);
                    k = detailCursor.getColumnIndex("hours");
                    detail.hours = detailCursor.getString(k);
                    details.add(detail);
                }
                poi.openingDetails = details;
                pois.add(poi);
            }
            poiLayer.pois = pois;
            return poiLayer;
        } else {
            return null;
        }
    }


}
