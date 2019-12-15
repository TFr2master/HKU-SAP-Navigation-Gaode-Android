package dupd.hku.com.hkusap.model;

import java.util.Objects;

import dupd.hku.com.hkusap.model.IEnum.SPWeightType;
import dupd.hku.com.hkusap.utils.GeoUtils;

import static dupd.hku.com.hkusap.model.IEnum.SPWeightType.SPWeightTypeLift;
import static dupd.hku.com.hkusap.model.IEnum.SPWeightType.SPWeightTypeEscalator;

public class IDRouteModel {

    public IDPointModel start;
    public IDPointModel end;
    public String levelCode;
    public int index;

    public IDRouteModel(IDPointModel start, IDPointModel end, String levelCode, int index) {
        this.start = start;
        this.end = end;
        this.levelCode = levelCode;
        this.index = index;
    }

    public double length() {
        return GeoUtils.distance(start.coordinate, end.coordinate);
    }

    public boolean liftRoute() {
        return start.type == SPWeightTypeLift && end.type == SPWeightTypeLift;
    }
    public boolean escalatorRoute() {
        return start.type == SPWeightTypeEscalator && end.type == SPWeightTypeEscalator;
    }


    public SPWeightType routeWeight() {
        return SPWeightType.MIN(start.type, end.type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IDRouteModel that = (IDRouteModel) o;
        return index == that.index &&
                Objects.equals(start, that.start) &&
                Objects.equals(end, that.end) &&
                Objects.equals(levelCode, that.levelCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end, levelCode, index);
    }
}
