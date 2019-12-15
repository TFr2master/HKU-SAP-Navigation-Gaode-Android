package dupd.hku.com.hkusap.manager;

import com.amap.api.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import dupd.hku.com.hkusap.model.GLKVector.GLKVector2;
import dupd.hku.com.hkusap.model.GLKVector.GLKVector3;
import dupd.hku.com.hkusap.model.IDMatchingModel;
import dupd.hku.com.hkusap.model.IDPointModel;
import dupd.hku.com.hkusap.model.IDRouteModel;
import dupd.hku.com.hkusap.model.SPNavigationRouteModel;
import dupd.hku.com.hkusap.model.SPPlateModel;
import dupd.hku.com.hkusap.utils.GeoUtils;

import static dupd.hku.com.hkusap.manager.PedometerManager.*;
import static dupd.hku.com.hkusap.model.IEnum.IDPointPosition.IDPointPositionMyLocation;
import static dupd.hku.com.hkusap.model.IEnum.IDPointPosition.IDPointPositionOthers;
import static dupd.hku.com.hkusap.model.IEnum.SPWeightType.SPWeightTypeOthers;
import static dupd.hku.com.hkusap.utils.GeoUtils.degreeToMeter;
import static dupd.hku.com.hkusap.utils.GeoUtils.degreeToRadius;
import static dupd.hku.com.hkusap.utils.GeoUtils.meterToDegree;
import static dupd.hku.com.hkusap.utils.GeoUtils.radiusToDegree;

public class DeadReckoningManager implements PedometerManagerDelegate {

    public static final float DISTANCE_FACTOR = 40.0f;
    public static final float HEADING_FACTOR = 10.0f;
    private final PedometerManager pedometerManager;
    public boolean beacon_location = false;

    public boolean started;                    //是否开启
    public boolean mapMatchingEnabled;         //是否开启地图匹配
    public List<IDRouteModel> navigationRoutes;          //通路集合
    public List<IDRouteModel> routes;                    //计算用通路
    public String levelCode;                 //楼层代码
    public LatLng lastMapMachtedCoordinate;   //计算出的地图匹配坐标
    public LatLng lastCoordinate;             //计算出的非地图匹配坐标
    public boolean pause;
    private float lastHeading;               //计算用方向角
    public IDRouteModel lastRoute;                 //目前投影到的路
    public LatLng lastPlateCoordinate;        //获得的plate坐标

    private static DeadReckoningManager INSTANCE;
    private List<DeadReckoningProtocol> observerArray = new ArrayList<>();

    public static DeadReckoningManager getInstance() {
        if (INSTANCE == null) INSTANCE = new DeadReckoningManager();
        return INSTANCE;
    }

    private DeadReckoningManager() {
        pedometerManager = new PedometerManager(this);
        mapMatchingEnabled = true;
        levelCode = "100";
    }
    public boolean checkDeadReckoning(){
        if(MapIOManager.useCheck==false)
            return true;
        if(lastCoordinate!=null||lastMapMachtedCoordinate!=null)
            return true;
        return false;
    }

    public void registerNavigationRoutes(List<SPNavigationRouteModel> routes) {
        this.routes = new ArrayList<>();
        for (SPNavigationRouteModel route : routes) {
            this.routes.add(route.mutableConverted());
        }
    }

    public void updateHeading(float heading) {
        lastHeading = heading;
    }

    public void startDeadReckoningWithMapMatchingEnabled(boolean enabled) {
        started = true;
        mapMatchingEnabled = enabled;
        pedometerManager.startPedometerUpdates();
    }

    public void switchToMapMatchingMode(boolean matching, List<IDRouteModel> routes) {
        synchronized (this) {
            mapMatchingEnabled = matching;
            if (!matching) {
                lastMapMachtedCoordinate = null;
                navigationRoutes = null;
            } else {
                navigationRoutes = routes;
            }
        }
    }

    public void stopDeadReckoning() {
        started = false;
        pedometerManager.stopPedometerUpdates();
        clear();
    }

    public void clear() {
        lastRoute = null;
        lastHeading = 0;
        lastCoordinate = null;
        lastMapMachtedCoordinate = null;
        lastPlateCoordinate = null;
    }

    public void approachPlate(SPPlateModel model) {
        LatLng coordinate = model.getCoordinate();
        levelCode = model.levelCode;
        processDeadReckoningCoordinate(coordinate, true);
    }

    @Override
    public void pedometerManagerDidRecognizedStep(int type, float step) {

        if (!started || lastCoordinate == null || lastHeading == 0) {
            return;
        }

        LatLng origin = lastCoordinate;
        float distance = 0.5f;
        float heading = lastHeading;

        double x = degreeToMeter(origin.longitude);
        double y = degreeToMeter(origin.latitude);
        double angle = degreeToRadius(heading);

        double latitude = meterToDegree(y + distance * Math.cos(angle));
        double longitude = meterToDegree(x + distance * Math.sin(angle));

        LatLng coordinate = new LatLng(latitude, longitude);

        processDeadReckoningCoordinate(coordinate, false);
    }

    public void processDeadReckoningCoordinate(LatLng coordinate, boolean forPlate) {
        lastCoordinate = coordinate;
        if (forPlate) {
            lastPlateCoordinate = coordinate;
        }


        if (!mapMatchingEnabled) {
            lastRoute = null;
            DataIOManager.getInstance().mSubject.onNext("processDeadReckoningCoordinate: 不匹配模式" + "\n");

            boolean within = false;
            if (lastPlateCoordinate != null) {
                within = GeoUtils.distance(lastPlateCoordinate, coordinate) < 8;
            }
            IDMatchingModel matching = new IDMatchingModel(coordinate, levelCode, within, false);
            //IDMatchingModel matching = new IDMatchingModel(coordinate, levelCode, beacon_location, false);

            for (DeadReckoningProtocol observer : observerArray) {
                observer.didCalculatedDeadReckoningModel(matching, lastRoute);
            }
        } else {

            DataIOManager.getInstance().mSubject.onNext("processDeadReckoningCoordinate: 匹配模式" + "\n");

            IDPointModel point = new IDPointModel(coordinate, SPWeightTypeOthers, levelCode, IDPointPositionOthers);
            IDRouteModel route = getNearestRouteFromPointWithMixedWeight(point);
            LatLng intersectCoordinate = intersectionCoordinateFrom(coordinate, route);

            if (lastRoute != route) {
                IDRouteModel from = lastRoute;

                for (DeadReckoningProtocol observer : observerArray) {
                    observer.didChangedRouteFrom(from, route);
                }
            }
            lastRoute = route;
            lastMapMachtedCoordinate = intersectCoordinate;

            boolean within = false;
            if (lastPlateCoordinate != null) {
                within = GeoUtils.distance(lastPlateCoordinate, intersectCoordinate) < 8;
            }

            IDMatchingModel matching = new IDMatchingModel(intersectCoordinate, levelCode, within, true);
            //IDMatchingModel matching = new IDMatchingModel(intersectCoordinate, levelCode, beacon_location, true);

            for (DeadReckoningProtocol observer : observerArray) {
                observer.didCalculatedDeadReckoningModel(matching, lastRoute);
            }

            checkCoordinatesInConfidenceRange();
        }
    }

    public void checkCoordinatesInConfidenceRange() {
        if (mapMatchingEnabled) {
            return;
        }
        if (lastCoordinate == null || lastMapMachtedCoordinate == null) {
            return;
        }
        double distance = 30;
        double difference = GeoUtils.distance(lastCoordinate, lastMapMachtedCoordinate);

        if (difference > distance) {

            for (DeadReckoningProtocol observer : observerArray) {
                IDPointModel point = new IDPointModel(lastMapMachtedCoordinate, SPWeightTypeOthers, levelCode, IDPointPositionMyLocation);
                observer.didExitConfidenceRangeFromPoint(point);
            }
        }
    }

    //根据两点计算出方向角，知道就好，我就不解释了
    public double headingFrom(LatLng from, LatLng to) {
        double fromLatitude = radiusToDegree(from.latitude);
        double fromLongitude = radiusToDegree(from.longitude);
        double toLatitude = radiusToDegree(to.latitude);
        double toLongitude = radiusToDegree(to.longitude);

        double longitudeDiff = toLongitude - fromLongitude;
        double latitudeDiff = toLatitude - fromLatitude;

        //land on quadrant cases
        if (longitudeDiff == 0) {
            if (latitudeDiff > 0) {
                return 0.0;
            } else {
                return 180.0;
            }
        }

        if (latitudeDiff == 0) {
            if (longitudeDiff > 0) {
                return 90;
            } else {
                return 270;
            }
        }

        //else
        double angle = radiusToDegree(Math.atan((toLongitude - fromLongitude) / (toLatitude - fromLatitude)));
        if (longitudeDiff >= 0 && latitudeDiff > 0) {
            //quadrant 1
            return angle;
        } else if (longitudeDiff > 0 && latitudeDiff < 0) {
            //quadrant 2
            return angle + 180;
        } else if (longitudeDiff < 0 && latitudeDiff < 0) {
            //quadrant 3
            return angle + 180;
        } else {
            //quadrant 4
            return angle + 360;
        }
    }

    public LatLng intersectionCoordinateFrom(LatLng coordinate, IDRouteModel route) {
        if (route == null || route.start == null || route.end == null) {
            return coordinate;
        }
        return intersectionCoordinateFrom(coordinate, route.start.coordinate, route.end.coordinate);
    }

    public IDRouteModel getNearestRouteFromPointWithMixedWeight(IDPointModel point) {
        double lastWeight = 0;

        IDRouteModel nearestRoute = null;

        //过滤与点同一层的道路
        List<IDRouteModel> routeList = findForLevelCode(point.levelCode, navigationRoutes);
        //遍历所有道路
        for (IDRouteModel route : routeList) {
            double distanceWeight = distanceWeightFromPoint(point, route);
            double headingWeight = headingWeightToRoute(route, lastHeading);
            double total = distanceWeight + headingWeight;
            if (lastWeight < total) {
                lastWeight = total;
                nearestRoute = route;
            }
        }

        return nearestRoute;
    }

    public IDRouteModel getNearestRouteFromPoint(IDPointModel point) {
        return getNearestRouteFromPoint(point, routes);
    }

    public IDRouteModel getNearestRouteFromPoint(IDPointModel point, List<IDRouteModel> routeList) {

        double lastDistance = Double.MAX_VALUE;
        IDRouteModel nearestRoute = null;

        //过滤与点同一层的道路
        List<IDRouteModel> routes = findForLevelCode(point.levelCode, routeList);

        //遍历所有道路
        for (IDRouteModel route : routes) {
            //计算点到路的距离
            double distance = distanceFromCoordinate(point.coordinate, route);
            if (lastDistance > distance) {
                //如果与最短距离短，则更新长度和路
                lastDistance = distance;
                nearestRoute = route;
            }
        }
        return nearestRoute;
    }

    public double distanceFrom(IDPointModel point, IDRouteModel route) {
        if (!point.levelCode.equals(route.levelCode)) {
            return Double.MAX_VALUE;
        }

        return distanceFromCoordinate(point.coordinate, route);
    }

    public double distanceFromCoordinate(LatLng coordinate, IDRouteModel route) {
        LatLng intersection = intersectionCoordinateFrom(coordinate, route);
        return GeoUtils.distance(coordinate, intersection);
    }

    //计算点与道路的垂点
    public LatLng intersectionCoordinateFrom(LatLng latLng, LatLng start, LatLng end) {
        //https://blog.csdn.net/changbaolong/article/details/7414796
        double a = (start.latitude - end.latitude);
        double b = (end.longitude - start.longitude);
        double c = (start.latitude * (start.longitude - end.longitude) - (start.longitude * (start.latitude - end.latitude)));

        GLKVector3 vector = new GLKVector3(a, b, c);

        double longitude = (Math.pow(vector.y, 2) * latLng.longitude - vector.x * vector.y * latLng.latitude - vector.x * vector.z) / (Math.pow(vector.x, 2) + Math.pow(vector.y, 2));
        double latitude = (-vector.x * vector.y * latLng.longitude + Math.pow(vector.x, 2) * latLng.latitude - vector.y * vector.z) / (Math.pow(vector.x, 2) + Math.pow(vector.y, 2));
        LatLng intersect = new LatLng(latitude, longitude);

        GLKVector2 vector1 = new GLKVector2(start.longitude - longitude, start.latitude - latitude);
        GLKVector2 vector2 = new GLKVector2(longitude - end.longitude, latitude - end.latitude);

        //check if the point is landing on extension cord
        double m = vector1.x * vector2.x + vector1.y * vector2.y;
        if (m < 0) {
            //if point landing on extension cord, then find the nearest instead
            double startDistance = GeoUtils.distance(intersect, start);
            double endDistance = GeoUtils.distance(intersect, end);
            return startDistance < endDistance ? start : end;
        } else {
            return intersect;
        }
    }

    public double headingWeightToRoute(IDRouteModel route, double heading) {
        if (heading == 0) return 0.0;
        return vectorBasedWeightToRoute(route, heading);
    }

    public double slopeBasedWeightToRoute(IDRouteModel route, double heading) {
        double trueHeading = heading;
        double routeHeading = headingFrom(route.start.coordinate, route.end.coordinate);

        double headingSlope = slopeForDegree(trueHeading);
        double routeSlope = slopeForDegree(routeHeading);

        //vertical case
        GLKVector2 trueVector = new GLKVector2(Math.sin(degreeToRadius(trueHeading)), Math.cos(degreeToRadius(trueHeading)));
        GLKVector2 routeVector = new GLKVector2(Math.sin(degreeToRadius(routeHeading)), Math.cos(degreeToRadius(routeHeading)));
        GLKVector2 result = new GLKVector2(trueVector, routeVector);
        if (result.x + result.y == 0) {
            return 0.0;
        }

        //parallex case
        result = new GLKVector2(trueVector, routeVector);
        if (result.x == result.y) {
            return HEADING_FACTOR;
        }

        double radius = Math.atan(Math.abs((headingSlope - routeSlope) / (1 + headingSlope * routeSlope)));
        double weight = HEADING_FACTOR * Math.cos(radius);

        return weight;
    }

    public double slopeForDegree(double degree) {
        return -Math.cos(degreeToRadius(degree)) / Math.sin(degreeToRadius(degree));
    }


    public List<IDRouteModel> findForLevelCode(String levelCode, List<IDRouteModel> routes) {
        List<IDRouteModel> routelList = new ArrayList<>();
        for (IDRouteModel model : routes) {
            if (levelCode.equals(model.levelCode)) {
                routelList.add(model);
            }
        }
        return routelList;
    }

    public double vectorBasedWeightToRoute(IDRouteModel route, double heading) {
        double routeHeading = headingFrom(route.start.coordinate, route.end.coordinate);

        GLKVector2 trueVector = new GLKVector2(Math.sin(degreeToRadius(heading)), Math.cos(degreeToRadius(heading)));
        GLKVector2 routeVector = new GLKVector2(Math.sin(degreeToRadius(routeHeading)), Math.cos(degreeToRadius(routeHeading)));
        GLKVector2 vector = new GLKVector2(trueVector, routeVector);
        double length = vector.length();
        double len2 = Math.pow(length, 2);                  // 0 - 4, 2 is in the middle
        double adjusted = len2 > 2 ? 4 - len2 : len2;  // 0 - 2
        double factor = (2 - adjusted) / 2.0;          // 0 - 1, 0 means vertical

        return factor * HEADING_FACTOR;
    }

    public double distanceWeightFromPoint(IDPointModel point, IDRouteModel route) {
        double distance = distanceFromCoordinate(point.coordinate, route);
        double factor;
        if (distance > 160) {
            factor = 0.0f;
        } else {
            factor = (80 - distance) / 80.0f;
        }

        return factor * DISTANCE_FACTOR;
    }

    public void addNotifyObserver(DeadReckoningProtocol observer) {
        synchronized (this) {
            if (observerArray.contains(observer)) {
                return;
            }
            observerArray.add(observer);
        }
    }

    public void removeNotifyObserver(DeadReckoningProtocol observer) {
        synchronized (this) {
            if (!observerArray.contains(observer)) {
                return;
            }
            observerArray.remove(observer);
        }
    }

    public interface DeadReckoningProtocol {
        default void didCalculatedDeadReckoningModel(IDMatchingModel model, IDRouteModel route) {
        }

        default void didChangedRouteFrom(IDRouteModel from, IDRouteModel to) {
        }

        default void didExitConfidenceRangeFromPoint(IDPointModel point) {
        }

    }
}
