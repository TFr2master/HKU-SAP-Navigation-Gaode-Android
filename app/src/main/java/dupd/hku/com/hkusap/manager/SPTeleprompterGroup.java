package dupd.hku.com.hkusap.manager;

import com.amap.api.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import dupd.hku.com.hkusap.model.IDRouteModel;
import dupd.hku.com.hkusap.model.IEnum.SPWeightType;
import dupd.hku.com.hkusap.utils.GeoUtils;

import static dupd.hku.com.hkusap.model.IEnum.SPWeightType.SPWeightTypeLift;
import static dupd.hku.com.hkusap.model.IEnum.SPWeightType.SPWeightTypeOthers;

/**
 * @author: 13060393903@163.com
 * @created on: 2018/12/13 11:37
 * @description:
 */
public class SPTeleprompterGroup {

    public List<IDRouteModel> routes;
    public SPWeightType type=SPWeightTypeOthers;

    public SPTeleprompterGroup() {
        routes = new ArrayList<>();
    }

    public SPTeleprompterGroup(SPWeightType type) {
        this();
        this.type = type;
    }

    public void appendRoute(IDRouteModel route) {
        if (!containsRoute(route)) {
            routes.add(route);
            if (route.start.type != SPWeightTypeOthers) {
                type = route.start.type;
            }

            if (type == SPWeightTypeLift) {
                type = route.end.type;
            } else {
                if (route.end.type != SPWeightTypeOthers) {
                    type = route.end.type;
                }
            }
        }
    }

    public boolean containsRoute(IDRouteModel route) {
        return routes.contains(route);
    }

    public List<IDRouteModel> routes() {
        return new ArrayList<>(routes);
    }

    public int routeCount() {
        return routes.size();
    }

    public SPWeightType groupType() {
        return type;
    }

    public IDRouteModel firstRoute() {
        if (routes.isEmpty()) {
            return null;
        }
        return routes.get(0);
    }

    public IDRouteModel lastRoute() {
        if (routes.isEmpty()) {
            return null;
        }
        return routes.get(routes.size() - 1);
    }

    public double groupLength() {
        double distance = 0.0;
        for (IDRouteModel route : routes) {
            distance += route.length();
        }

        return distance;
    }

    public boolean groupInLift() {
        IDRouteModel route = firstRoute();
        if (route == null) {
            return false;
        }
        return route.liftRoute();
    }

    public double distanceLeftFromCoordinate(LatLng coordinate, IDRouteModel route) {
        if (!containsRoute(route)) return Double.MAX_VALUE;

        int index = routes.indexOf(route);
        double distance = 0.0;

        if (index != routes.size() - 1) {
            for (int i = index + 1; i < routes.size(); i++) {
                IDRouteModel internal = routes.get(i);
                distance += internal.length();
            }
        }

        distance += GeoUtils.distance(coordinate, route.end.coordinate);
        return distance;
    }

}
