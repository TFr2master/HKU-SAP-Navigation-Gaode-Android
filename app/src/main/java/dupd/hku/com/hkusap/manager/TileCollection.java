package dupd.hku.com.hkusap.manager;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Stream;

class TileCollectionRange {
    long zoom;
    long xStart;
    long xRange;
    long yStart;
    long yRange;
    public TileCollectionRange(long zoom, long xStart, long xRange, long yStart, long yRange) {
        this.zoom = zoom;
        this.xStart = xStart;
        this.xRange = xRange;
        this.yStart = yStart;
        this.yRange = yRange;
    }

    public boolean isEqual(TileCollectionRange range) {
        return ((this.zoom == range.zoom) &&
                (this.xStart == range.xStart) &&
                (this.xRange == range.xRange) &&
                (this.yStart == range.yStart) &&
                (this.yRange == range.yRange));
    }
}
interface TileAt {
    public TileRegion tileAt(long x, long y);
}

interface Intersect {
    public List<TileRegion> intersect(TileCollection input);
}

interface Minus {
    public List<TileRegion> minus(List<TileRegion> input);
}

public class TileCollection {
    TileCollectionRange range;
    List<TileRegion> tiles;

    public TileCollection(TileCollectionRange range, List<TileRegion> tiles) {
        this.range = range;
        this.tiles = tiles;
    }

    public boolean cotainsTileWithTileCode(String tileCode) {
        Stream<TileRegion> stream = tiles.stream();
        return stream.anyMatch(tile -> tile.tileCode.equals(tileCode));
    }

    public boolean isEqual(TileCollection collection) {
        return range.isEqual(collection.range);
    }

    public TileAt tileAt() {
        return (x, y) -> {
            if (x < range.xStart || x > range.xStart + range.xRange ||
                    y < range.yStart || y > range.yStart + range.yRange) {
                return null;
            } else {
                long column = x - range.xStart;
                long row = y - range.yStart;
                return tiles.get((int)(column + row * range.xRange));
            }
        };
    }

    public Intersect intersect() {
        return (input -> {
            Set<TileRegion> left = new HashSet<TileRegion>(tiles);
            left.retainAll(input.tiles);
            return new ArrayList<TileRegion>(left);
        });
    }

    public Minus minus() {
        return (input -> {
            Set<TileRegion> left = new HashSet<TileRegion>(tiles);
            left.removeAll(input);
            return new ArrayList<TileRegion>(left);
        });
    }

}
