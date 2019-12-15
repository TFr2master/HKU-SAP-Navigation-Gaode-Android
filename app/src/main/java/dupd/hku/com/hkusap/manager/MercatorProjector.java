package dupd.hku.com.hkusap.manager;

import dupd.hku.com.hkusap.model.Point;
import com.amap.api.maps.model.LatLng;

import java.util.Locale;


class PixelBoundingBox {
    public Point northEast;
    public Point northWest;
    public Point southEast;
    public Point southWest;
}

class TileBoundingBox {
    public LatLng northEast;
    public LatLng northWest;
    public LatLng southEast;
    public LatLng southWest;

    public TileBoundingBox() {

    }

    public TileBoundingBox(LatLng northEast,
                           LatLng northWest,
                           LatLng southEast,
                           LatLng southWest) {
        this.northEast = northEast;
        this.northWest = northWest;
        this.southEast = southEast;
        this.southWest = southWest;
    }
}

public class MercatorProjector {
    long tileSize;
    double initialResolution;
    double originShift;
    final double PI = 3.1415926;
    public MercatorProjector() {
        this(256);
    }

    public MercatorProjector(long size) {
        this.tileSize = size;
        initialResolution = 2 * PI * 6378137 / size;
        originShift = 2 * PI * 6378137 / 2.0;
    }

    public Point coordinateToMeters(LatLng coordinate) {
        double x = coordinate.longitude * originShift / 180.0;
        double y = Math.log(Math.tan((90 + coordinate.latitude) * PI / 360.0)) /(PI / 180.0);
        y = y * originShift / 180.0;
        return new Point(x, y);
    }

    public LatLng metersToCoordinate(Point meters) {
        double longitude = (meters.x /originShift) * 180.0;
        double latitude = (meters.y /originShift) * 180;
        latitude = 180 / PI * (2 * Math.atan(Math.exp(latitude * PI / 180)) - PI /2.0);
        LatLng coordinate = new LatLng(latitude, longitude);
        return coordinate;
    }

    public Point metersToPixels(Point meters, long zoom) {
        double resolution = resolutionInZoom(zoom);
        double x = (meters.x + originShift) / resolution;
        double y = (meters.y + originShift) / resolution;
        return new Point(x, y);
    }

    public Point pixelToMeters(Point pixel, long zoom) {
        double resolution = resolutionInZoom(zoom);
        double x = pixel.x * resolution - originShift;
        double y = pixel.y * resolution - originShift;
        return new Point(x, y);
    }

    public Point pixelToTileXY(Point pixel) {
        long x = (int)Math.ceil(pixel.x / tileSize) - 1;
        long y = (int)Math.ceil(pixel.y / tileSize) - 1;
        return new Point(x, y);
    }

    public double resolutionInZoom(long zoom) {
        return (2 * PI * 6378137) / (tileSize * Math.pow(2, zoom));
    }

    public PixelBoundingBox pixelBoundingBoxWithXY(long x, long y) {
        PixelBoundingBox box = new PixelBoundingBox();
        box.northWest = new Point(x * tileSize, y * tileSize);
        box.northEast = new Point((x + 1) * tileSize, y * tileSize);
        box.southWest = new Point(x * tileSize, (y + 1) * tileSize);
        box.southEast = new Point((x + 1) * tileSize, (y + 1)* tileSize);
        return box;
    }

    public TileBoundingBox tileBoundingBoxWithZoom(long zoom, PixelBoundingBox mercator) {
        TileBoundingBox box = new TileBoundingBox();
        box.northWest = metersToCoordinate(pixelToMeters(mercator.northWest, zoom));
        box.northEast = metersToCoordinate(pixelToMeters(mercator.northEast, zoom));
        box.southWest = metersToCoordinate(pixelToMeters(mercator.southWest, zoom));
        box.southEast = metersToCoordinate(pixelToMeters(mercator.southEast, zoom));
        return box;
    }

    public TileBoundingBox tileBoundBoxWithZoom(long zoom, long x, long y) {
        PixelBoundingBox pixelBox = pixelBoundingBoxWithXY(x, y);
        return tileBoundingBoxWithZoom(zoom, pixelBox);
    }

    public Point tileXYWithZoom(long zoom, LatLng coordinate) {
        Point meters = coordinateToMeters(coordinate);
        Point pixel = metersToPixels(meters, zoom);
        Point tileXY = pixelToTileXY(pixel);
        tileXY.y = convert((long)tileXY.y, zoom);
        return tileXY;
    }

    public TileRegion tileWithZoom(long zoom, LatLng coordinate) {
        Point tileXY = tileXYWithZoom(zoom, coordinate);
        return tileWithZoom(zoom, (long)tileXY.x, (long)tileXY.y);
    }

    public String tileCodeWithZoom(long zoom, long x, long y) {
        return String.format(Locale.getDefault(), "%dl/%dl/%dl", x, y, zoom);
    }

    public TileRegion tileWithZoom(long zoom, long x, long y) {
        x = availableTileX(zoom, x);
        long yGoogle = y;
        long yTMS = convert(yGoogle, zoom);
        TileBoundingBox box = tileBoundBoxWithZoom(zoom, x, yTMS);
        String tileCode = tileCodeWithZoom(zoom, x, yGoogle);
        return new TileRegion(tileSize, zoom, x, yGoogle, tileCode, box);
    }

    public long convert(long y, long zoom) {
        return (long)Math.pow(2, zoom) - 1 - y;
    }

    public long availableTileX(long zoom, long x) {
        long max = (long)Math.pow(2, zoom) - 1;
        if (x < 0) {
            return x + max;
        }
        if (x > max) {
            return x - max;
        }
        return x;
    }

    public long tileYRangeInZoom(long zoom) {
        return (long)Math.pow(2, zoom) -1 ;
    }

    public boolean tileYIsValid(long zoom, long y) {
        return y >=0 && y <= ((long)Math.pow(2, zoom) - 1);
    }
}
