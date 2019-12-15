package dupd.hku.com.hkusap.model;

import com.amap.api.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import dupd.hku.com.hkusap.manager.DataIOManager;
import dupd.hku.com.hkusap.utils.GeoUtils;

import static dupd.hku.com.hkusap.model.IEnum.IDPointUsage.SPPointUsageBuildingBoundary;

public class SPBuildingBoundary {
    public String postalCode;
    public String name;
    public String levelCode;

    public SPBuildingBoundary(String postalCode, String name, String levelCode) {
        this.postalCode = postalCode;
        this.name = name;
        this.levelCode = levelCode;
    }

    public List<LatLng> boundaryPath() {
        List<LatLng> path = new ArrayList<>();
        List<SPBuildingBoundaryLineModel> lines = boundaryLines();

        for (SPBuildingBoundaryLineModel line : lines) {
            LatLng coordinate = GeoUtils.convertCoordinate(new LatLng(line.from.latitude, line.from.longitude));
            path.add(coordinate);
        }

        SPBuildingBoundaryLineModel last = lines.get(lines.size() - 1);
        LatLng coordinate = GeoUtils.convertCoordinate(new LatLng(last.to.latitude, last.to.longitude));
        path.add(coordinate);

        return path;
    }

    public List<SPBuildingBoundaryLineModel> boundaryLines() {
        List<SPBuildingBoundaryLineModel> boundaryLines = new ArrayList<>();
        for (SPBuildingBoundaryLineModel line : DataIOManager.getInstance().boundaryLines) {
            if (postalCode.equals(line.postalCode) && levelCode.equals(line.levelCode)) {
                boundaryLines.add(line);
            }
        }
        return boundaryLines;
    }

    public List<SPPointModel> boundaryPoints() {
        List<SPPointModel> pointList = new ArrayList<>();
        for (SPBuildingBoundaryLineModel line : DataIOManager.getInstance().boundaryLines) {

            SPPointModel point = point(line.from);
            if (point != null) {
                pointList.add(point);
            }

            point = point(line.to);
            if (point != null) {
                pointList.add(point);
            }
        }
        return pointList;
    }

    public SPPointModel point(SPPointModel point) {
        if (point == null) return null;
        if (postalCode.equals(point.postalCode) && levelCode.equals(point.levelCode) && (point.usage == SPPointUsageBuildingBoundary)) {
            return point;
        }
        return null;
    }
}
