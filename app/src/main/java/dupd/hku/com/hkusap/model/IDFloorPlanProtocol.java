package dupd.hku.com.hkusap.model;

import com.amap.api.maps.model.LatLng;

public interface IDFloorPlanProtocol {

    String imageName();

    float bearing();

    LatLng topLeftCoordinate();

    LatLng topRightCoordinate();

    LatLng bottomLeftCoordinate();

    LatLng bottomRightCoordinate();

    LatLng centerCoordinate();
}
