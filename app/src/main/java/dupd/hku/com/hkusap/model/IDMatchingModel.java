package dupd.hku.com.hkusap.model;

import com.amap.api.maps.model.LatLng;

import dupd.hku.com.hkusap.utils.GeoUtils;

public class IDMatchingModel implements Cloneable{

    public boolean inConfidenceRange;
    public LatLng coordinate;
    public boolean mapMatched;
    public String levelCode;

    public IDMatchingModel(LatLng coordinate, String levelCode, boolean inConfidenceRange, boolean mapMatched) {
        this.inConfidenceRange = inConfidenceRange;
//        LatLng cv= GeoUtils.convertCoordinate(coordinate);
//        this.coordinate = GeoUtils.convertCoordinate(coordinate);
        this.coordinate=coordinate;
        this.mapMatched = mapMatched;
        this.levelCode = levelCode;
    }

    @Override
    public IDMatchingModel clone() {
        try {
            return (IDMatchingModel) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }
}
