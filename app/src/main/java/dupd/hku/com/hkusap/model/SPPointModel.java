package dupd.hku.com.hkusap.model;

import com.amap.api.maps.model.LatLng;

import dupd.hku.com.hkusap.model.IEnum.IDPointUsage;
import dupd.hku.com.hkusap.utils.GeoUtils;

import static dupd.hku.com.hkusap.model.IEnum.IDPointPosition.IDPointPositionOthers;
import static dupd.hku.com.hkusap.model.IEnum.SPWeightType;
import static dupd.hku.com.hkusap.model.IEnum.SPWeightType.SPWeightTypeOthers;

public class SPPointModel {

    public String pointID;
    public String postalCode;
    public String levelCode;
    public IDPointUsage usage;
    public SPWeightType type;
    public String name;
    public int index;
    public double latitude;
    public double longitude;


    public SPPointModel(LatLng latLng) {
        this(latLng.latitude,latLng.longitude);
    }

    public SPPointModel(double latitude, double longitude) {
        this(latitude, longitude, SPWeightTypeOthers);
    }

    public SPPointModel(double latitude, double longitude, SPWeightType type) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.type = type;
    }

    public IDPointModel mutableConverted() {
        LatLng coordinate = GeoUtils.convertCoordinate(new LatLng(latitude, longitude));
        return new IDPointModel(coordinate, type, levelCode, IDPointPositionOthers);
    }
}
