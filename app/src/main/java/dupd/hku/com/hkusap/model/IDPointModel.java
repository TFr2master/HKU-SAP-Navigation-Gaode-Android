package dupd.hku.com.hkusap.model;

import com.amap.api.maps.model.LatLng;

import java.util.Objects;

import dupd.hku.com.hkusap.model.IEnum.IDPointPosition;
import dupd.hku.com.hkusap.model.IEnum.SPWeightType;

public class IDPointModel implements Cloneable {

    public String postalCode;
    public String name;
    public String speakOut;
    public String levelCode;
    public LatLng coordinate;
    public SPWeightType type;
    public IDPointPosition position;


    public IDPointModel(LatLng coordinate, SPWeightType type, String levelCode, IDPointPosition position) {
        this.coordinate = coordinate;
        this.type = type;
        this.levelCode = levelCode;
        this.position = position;
    }


    public IDPointModel(SPPlateModel plate) {
        coordinate = plate.getCoordinate();
        type = plate.type;
        levelCode = plate.levelCode;
        name = plate.name;
        speakOut = plate.speakOut;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IDPointModel that = (IDPointModel) o;
        return Objects.equals(levelCode, that.levelCode) &&
                Objects.equals(coordinate, that.coordinate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(levelCode, coordinate);
    }

    @Override
    public IDPointModel clone() {
        try {
            return (IDPointModel) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }
}
