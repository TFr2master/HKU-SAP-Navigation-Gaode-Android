package dupd.hku.com.hkusap.manager;

import android.widget.LinearLayout;

import java.util.List;
import java.util.ArrayList;
import com.amap.api.maps.model.LatLng;
import dupd.hku.com.hkusap.model.Point;

public class TileManager {
    MercatorProjector projector;
    private static TileManager instance = new TileManager(false);

    public TileManager() {
        this(false);
    }

    public TileManager(boolean isHighDPI) {
        long tileSize = isHighDPI ? 512 : 256;
        projector = new MercatorProjector(tileSize);
    }

    public static TileManager getInstance() {
        return instance;
    }

    public Point coordinateToMeters(LatLng coordinate) {
        return projector.coordinateToMeters(coordinate);
    }

    public LatLng metersToCoordinate(Point meters) {
        return projector.metersToCoordinate(meters);
    }

    public String getTileCode(long zoom, LatLng coordinate) {
        Point tileXY = projector.tileXYWithZoom(zoom, coordinate);
        return projector.tileCodeWithZoom(zoom, (long)tileXY.x, (long)tileXY.y);
    }

    public TileRegion tileWithZoom(long zoom, LatLng coordinate) {
        return projector.tileWithZoom(zoom, coordinate);
    }

    public String getTileCode(long zoom, long x, long y) {
        return projector.tileCodeWithZoom(zoom, x, y);
    }

    public TileRegion tileWithZoom(long zoom, long x, long y) {
        return projector.tileWithZoom(zoom, x, y);
    }

    public TileCollection tileCollectionWithZoom(long zoom, LatLng coordinate, long dimension) {
        TileCollectionRange range = tileCollectionRangeWithZoom(zoom, coordinate, dimension);
        return tileCollectionWithRange(range);
    }

    public TileCollectionRange tileCollectionRangeWithZoom(long zoom, LatLng coordinate, long dimension) {
        Point tileXY = projector.tileXYWithZoom(zoom, coordinate);
        long length = (dimension - 1) / 2;
        long xOrigin = (long)tileXY.x - length;
        long xMax = xOrigin + dimension - 1;
        long yOrigin = (long)tileXY.y - length;
        long yMax = yOrigin + dimension - 1;

        long availableRange = projector.tileYRangeInZoom(zoom);
        if (yOrigin < 0) {
            yOrigin = 0;
        }
        if (yMax > availableRange) {
            yMax = availableRange;
        }

        long xStart = xOrigin;
        long xRange = xMax - xOrigin;
        long yStart = yOrigin;
        long yRange = yMax - yOrigin;
        return new TileCollectionRange(zoom, xStart, xRange, yStart, yRange);
    }

    public TileCollection tileCollectionWithRange(TileCollectionRange range) {
        List<TileRegion> tiles = new ArrayList<TileRegion>();
        for (long x = range.xStart; x <= range.xStart + range.xRange; x++) {
            for (long y = range.yStart; y <= range.yStart + range.yRange; y++) {
                TileRegion tile = projector.tileWithZoom(range.zoom, x, y);
                tiles.add(tile);
            }
        }
        return new TileCollection(range, tiles);
    }

    public List<TileRegion> tilesFrom(Point fromXY, Point toXY, long zoom) {
        List<TileRegion> tiles = new ArrayList<TileRegion>();
        long fromX = (long)Math.min(fromXY.x, toXY.x);
        long toX = (long)Math.max(fromXY.x, toXY.x);
        long fromY = (long)Math.min(fromXY.y, toXY.y);
        long toY = (long)Math.max(fromXY.y, toXY.y);

        for (long x = fromX; x <= toX; x++) {
            for (long y = fromY; y <= toY; y++) {
                TileRegion tile = tileWithZoom(zoom, x, y);
                tiles.add(tile);
            }
        }
        return tiles;
    }

    public List<TileRegion> tilesFromCoordinate(LatLng from, LatLng to, long zoom) {
        Point fromXY = projector.tileXYWithZoom(zoom, from);
        Point toXY = projector.tileXYWithZoom(zoom, to);
        return tilesFrom(fromXY, toXY, zoom);
    }

    public List<String> tilesCodesFromCoordinate(LatLng from, LatLng to, long zoom) {
        Point fromXY = projector.tileXYWithZoom(zoom, from);
        Point toXY = projector.tileXYWithZoom(zoom, to);

        List<String> codes = new ArrayList<String>();
        long fromX = (long)Math.min(fromXY.x, toXY.x);
        long toX = (long)Math.max(fromXY.x, toXY.x);
        long fromY = (long)Math.min(fromXY.y, toXY.y);
        long toY = (long)Math.max(fromXY.y, toXY.y);
        for (long x = fromX; x <= toX; x++) {
            for (long y = fromY; y <= toY; y++) {
                String code = projector.tileCodeWithZoom(zoom, x, y);
                codes.add(code);
            }
        }
        return codes;
    }
}
