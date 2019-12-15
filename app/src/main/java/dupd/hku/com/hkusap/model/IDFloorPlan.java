package dupd.hku.com.hkusap.model;

import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;

import dupd.hku.com.hkusap.utils.GeoUtils;
import dupd.hku.com.hkusap.utils.Utils;

public class IDFloorPlan implements IDFloorPlanProtocol {

    public String levelCode;
    public SPFloorPlanBase base;

    public IDFloorPlan(String levelCode) {
        this.levelCode = levelCode;
        base = new SPFloorPlanBase();
    }

    public GroundOverlayOptions groundOverlay() {
        LatLng southWest = bottomLeftCoordinate();
        LatLng northEast = topRightCoordinate();

        LatLngBounds.Builder bounds = new LatLngBounds.Builder();
        bounds.include(southWest);
        bounds.include(northEast);

        int resID = Utils.getResourcesDrawable(imageName());

        GroundOverlayOptions overlay = new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(resID))
                .positionFromBounds(bounds.build());
        return overlay;
    }

    @Override
    public String imageName() {
        return "level_" + levelCode;
    }

    @Override
    public float bearing() {
        return 0;
    }

    @Override
    public LatLng topLeftCoordinate() {
        return GeoUtils.convertCoordinate(new LatLng(base.topLeft.latitude, base.topLeft.longitude));
    }

    @Override
    public LatLng topRightCoordinate() {
        return GeoUtils.convertCoordinate(new LatLng(base.topRight.latitude, base.topRight.longitude));
    }

    @Override
    public LatLng bottomLeftCoordinate() {
        return GeoUtils.convertCoordinate(new LatLng(base.bottomLeft.latitude, base.bottomLeft.longitude));
    }

    @Override
    public LatLng bottomRightCoordinate() {
        return GeoUtils.convertCoordinate(new LatLng(base.bottomRight.latitude, base.bottomRight.longitude));
    }

    @Override
    public LatLng centerCoordinate() {
        return GeoUtils.convertCoordinate(new LatLng(base.center.latitude, base.center.longitude));
    }
}
