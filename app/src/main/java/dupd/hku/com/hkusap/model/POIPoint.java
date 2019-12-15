package dupd.hku.com.hkusap.model;

import org.json.JSONObject;

public class POIPoint {
    public String pointID;
    public String levelCode;
    public int type;
    public int mapScale;
    public String title;
    public double latitude;
    public double longitude;
    public String building_name;
    public String poi_type;

    public POIPoint() {
    }

    public POIPoint(JSONObject jsonObject) {
        pointID = jsonObject.optString("Id");
        latitude = jsonObject.optDouble("latitude");
        longitude = jsonObject.optDouble("longitude");
        levelCode = jsonObject.optString("level");
        if(levelCode.length()<3)
            levelCode="0"+levelCode;
        title = jsonObject.optString("title");
        type = jsonObject.optInt("type");
        mapScale = jsonObject.optInt("map_scale");
    }
}
