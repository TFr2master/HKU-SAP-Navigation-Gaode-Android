package dupd.hku.com.hkusap.model;

import com.amap.api.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;

import dupd.hku.com.hkusap.utils.AssetsUtil;

import static dupd.hku.com.hkusap.model.IEnum.IDPointUsage.SPPointUsageBase;
import static dupd.hku.com.hkusap.model.IEnum.SPWeightType.SPWeightTypeOthers;

public class SPFloorPlanBase {
    public SPPointModel topLeft;
    public SPPointModel topRight;
    public SPPointModel bottomLeft;
    public SPPointModel bottomRight;
    public SPPointModel center;

    public SPFloorPlanBase() {
        String json = AssetsUtil.readAssetJson("floorplans.json");
        Type type = new TypeToken<Map<String, LatLng>>() {
        }.getType();
        Map<String, LatLng> latLngMap = new Gson().fromJson(json, type);

        topLeft = pointWithData(latLngMap.get("TL"), "TL", 0);
        topRight = pointWithData(latLngMap.get("TR"), "TR", 1);
        bottomRight = pointWithData(latLngMap.get("BR"), "BR", 2);
        bottomLeft = pointWithData(latLngMap.get("BL"), "BL", 3);
        center = pointWithData(latLngMap.get("CT"), "CT", 4);
    }

    public SPPointModel pointWithData(LatLng latLng, String name, int index) {
        SPPointModel point = new SPPointModel(latLng);
        point.name = name;
        point.index = index;
        point.usage = SPPointUsageBase;
        point.type = SPWeightTypeOthers;
        point.pointID = String.valueOf(index);

        return point;
    }
}
