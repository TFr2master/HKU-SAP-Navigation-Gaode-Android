package dupd.hku.com.hkusap.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;

import com.amap.api.maps.model.LatLng;

public class GeoUtils {

    private static int EARTH_RADIUS = 6371 * 1000;
    private static boolean CONVERT_LOCATION_FLAG = false;

    /**
     * Computes the distance in kilometers between two points on Earth.
     */
    public static double distance(LatLng start, LatLng end) {
        double lat1Rad = Math.toRadians(start.latitude);
        double lat2Rad = Math.toRadians(end.latitude);
        double deltaLonRad = Math.toRadians(end.longitude - start.longitude);

        return Math.acos(Math.sin(lat1Rad) * Math.sin(lat2Rad) + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                * Math.cos(deltaLonRad))
                * EARTH_RADIUS;
    }


    /**
     * Computes the bearing in degrees between two points on Earth.
     */
    public static double bearing(LatLng start, LatLng end) {
        double lat1Rad = Math.toRadians(start.latitude);
        double lat2Rad = Math.toRadians(end.latitude);
        double deltaLonRad = Math.toRadians(end.longitude - start.longitude);

        double y = Math.sin(deltaLonRad) * Math.cos(lat2Rad);
        double x = Math.cos(lat1Rad) * Math.sin(lat2Rad) - Math.sin(lat1Rad) * Math.cos(lat2Rad)
                * Math.cos(deltaLonRad);
        return radToBearing(Math.atan2(y, x));
    }

    /**
     * Converts an angle in radians to degrees
     */
    public static double radToBearing(double rad) {
        return (Math.toDegrees(rad) + 360) % 360;
    }


    /**
     * 计算方位角,正北向为0度，以顺时针方向递增
     *
     * @return 方位角
     */
    public static double computeAzimuth(double lat1, double lon1, double lat2, double lon2) {
        double result = 0.0;
        int ilat1 = (int) (0.50 + lat1 * 360000.0);
        int ilat2 = (int) (0.50 + lat2 * 360000.0);
        int ilon1 = (int) (0.50 + lon1 * 360000.0);
        int ilon2 = (int) (0.50 + lon2 * 360000.0);
        lat1 = Math.toRadians(lat1);
        lon1 = Math.toRadians(lon1);
        lat2 = Math.toRadians(lat2);
        lon2 = Math.toRadians(lon2);
        if ((ilat1 == ilat2) && (ilon1 == ilon2)) {
            return result;
        } else if (ilon1 == ilon2) {
            if (ilat1 > ilat2)
                result = 180.0;
        } else {
            double c = Math.acos(Math.sin(lat2) * Math.sin(lat1) + Math.cos(lat2)
                    * Math.cos(lat1) * Math.cos((lon2 - lon1)));
            double A = Math.asin(Math.cos(lat2) * Math.sin((lon2 - lon1))
                    / Math.sin(c));
            result = Math.toDegrees(A);
            if ((ilat2 > ilat1) && (ilon2 > ilon1)) {
            } else if ((ilat2 < ilat1) && (ilon2 < ilon1)) {
                result = 180.0 - result;
            } else if ((ilat2 < ilat1) && (ilon2 > ilon1)) {
                result = 180.0 - result;
            } else if ((ilat2 > ilat1) && (ilon2 < ilon1)) {
                result += 360.0;
            }
        }
        return result;
    }


    public static double radiusToDegree(double radius) {
        return radius / Math.PI * 180.0;
    }

    public static double degreeToMeter(double degree) {
        return degree * 3600 * 30.0;
    }

    public static double degreeToRadius(double degree) {
        return degree / 180.0 * Math.PI;
    }

    public static double meterToDegree(double km) {
        return (km) / (3600 * 30.0);
    }

    public static void convertCoordinate(Location location) {
        if (CONVERT_LOCATION_FLAG) {
            LocationConverter.LatLng latLng = new LocationConverter.LatLng(location.getLatitude(), location.getLongitude());
            LocationConverter.LatLng convertedLatLng = LocationConverter.wgs84ToGcj02(latLng);
            location.setLatitude(convertedLatLng.latitude);
            location.setLongitude(convertedLatLng.longitude);
        }
    }

    public static LatLng convertCoordinate(LatLng coordinate) {
        if (CONVERT_LOCATION_FLAG) {
            LocationConverter.LatLng latLng = new LocationConverter.LatLng(coordinate.latitude, coordinate.longitude);
            LocationConverter.LatLng convertedLatLng = LocationConverter.wgs84ToGcj02(latLng);
            LatLng newCoordinate = new LatLng(convertedLatLng.latitude, convertedLatLng.longitude);
            return newCoordinate;
        } else {
            return coordinate;
        }
    }


    /**
     * 判断GPS是否开启，GPS或者AGPS开启一个就认为是开启的
     *
     * @param context
     * @return true 表示开启
     */
    public static boolean isOpenGPS(final Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (gps || network) {
            return true;
        }
        return false;
    }

    public static int getZoomCode(int zoom) {
        int zoomLevel;
        if (zoom < 17) {
            zoomLevel = 0;
        } else if (zoom < 18) {
            zoomLevel = 1;
        } else if (zoom < 19) {
            zoomLevel = 2;
        } else {
            zoomLevel = 3;
        }
        return zoomLevel;
    }
}
