package dupd.hku.com.hkusap.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.amap.api.maps.model.LatLng;
import com.google.gson.annotations.JsonAdapter;

import dupd.hku.com.hkusap.manager.DataIOManager;
import dupd.hku.com.hkusap.model.IEnum.SPWeightType;

@JsonAdapter(SPPlateModelAdapter.class)
public class SPPlateModel implements Parcelable {

    public String plateID;
    public double mPower;
    public SPWeightType type;
    public String name;
    public String speakOut;
    public int searchable;

    public String UUID;
    public String postalCode;
    public String levelCode;
    public String roomID;
    public double latitude;
    public double longitude;
    public String building_name;
    public String poi_type;


    public void init() {
        UUID = getUUIDFromString(plateID);
        plateID = getPlateIDFromString(plateID);
        postalCode = getPostalCode(plateID);
        levelCode = getLevelCode(plateID);
        roomID = getRoomCode(plateID);
    }

    public String getRoomCode(String uuid) {
        return uuid.substring(0, 24);
    }

    public String getLevelCode(String uuid) {
        return uuid.substring(13, 16);
    }

    public String getPostalCode(String uuid) {
        return uuid.substring(0, 13);
    }

    public String getPlateIDFromString(String uuid) {
        String start = uuid.substring(0, 2);
        String end = uuid.substring(4, uuid.length());
        return start + convertPositionCode(uuid) + end;
    }

    public String convertPositionCode(String uuid) {
        String positionCode = uuid.substring(2, 4);
        int index = Integer.valueOf(positionCode);
        switch (index) {
            case 1:
                return "NW";
            case 2:
                return "NE";
            case 3:
                return "SW";
            case 4:
            default:
                return "SE";
        }
    }

    //11030713-5148-0103-a000-a000a020a053
    //11SW0713 5148 0098 a000 a000b005a254
    public String getUUIDFromString(String uuid) {
        StringBuilder builder = new StringBuilder();
        /*
        builder.append(uuid.substring(0, 8))
                .append("-")
                .append(uuid.substring(8, 12))
                .append("-")
                .append(uuid.substring(12, 16))
                .append("-")
                .append(uuid.substring(16, 20))
                .append("-")
                .append(uuid.substring(20));
        return builder.toString();
         */
        return uuid;
    }

    public LatLng getCoordinate() {
        /*
        SPBuildingModel result = findPlateBuilding();
        if (result == null) return null;
        LatLng origin = new LatLng(result.latitude, result.longitude);
        LatLng plateCoordinate = coordinateWithOrigin(origin, plateID);
        double latitude = plateCoordinate.latitude;
        double longitude = plateCoordinate.longitude;*/
        return new LatLng(latitude, longitude);
    }

    private SPBuildingModel findPlateBuilding() {
        for (SPBuildingModel b : DataIOManager.getInstance().sapdb.building) {
            if (b.postalCode.equals(postalCode))
                return b;
        }
        return null;
    }

    public LatLng coordinateWithOrigin(LatLng origin, String plateID) {
        String roomOffset = plateID.substring(16, 24);
        String beaconOffset = plateID.substring(24);
        LatLng roomCoordinate = nextCoordinate(origin, roomOffset);
        return nextCoordinate(roomCoordinate, beaconOffset);
    }

    private LatLng nextCoordinate(LatLng coordinate, String offset) {
        String longitudeOffset = offset.substring(0, 4);
        String latitudeOffset = offset.substring(4);

        double longitude = coordinate.longitude + degreeWithCode(longitudeOffset);
        double latitude = coordinate.latitude + degreeWithCode(latitudeOffset);

        return new LatLng(latitude, longitude);
    }

    private double degreeWithCode(String offsetCode) {
        int symbol = offsetCode.substring(0, 1).equals("a") ? 1 : -1;
        double seconds = Integer.valueOf(offsetCode.substring(1)) / 100.0;
        return (seconds / 3600.0) * symbol;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.plateID);
        dest.writeDouble(this.mPower);
        dest.writeDouble(this.latitude);
        dest.writeDouble(this.longitude);
        dest.writeInt(this.type == null ? -1 : this.type.ordinal());
        dest.writeString(this.name);
        dest.writeString(this.speakOut);
        dest.writeInt(this.searchable);
        dest.writeString(this.UUID);
        dest.writeString(this.postalCode);
        dest.writeString(this.levelCode);
        dest.writeString(this.roomID);
    }

    public SPPlateModel() {
    }

    protected SPPlateModel(Parcel in) {
        this.plateID = in.readString();
        this.mPower = in.readDouble();
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
        int tmpType = in.readInt();
        this.type = tmpType == -1 ? null : SPWeightType.values()[tmpType];
        this.name = in.readString();
        this.speakOut = in.readString();
        this.searchable = in.readInt();
        this.UUID = in.readString();
        this.postalCode = in.readString();
        this.levelCode = in.readString();
        this.roomID = in.readString();
    }

    public static final Creator<SPPlateModel> CREATOR = new Creator<SPPlateModel>() {
        @Override
        public SPPlateModel createFromParcel(Parcel source) {
            return new SPPlateModel(source);
        }

        @Override
        public SPPlateModel[] newArray(int size) {
            return new SPPlateModel[size];
        }
    };

    @Override
    public String toString() {
        return "name='" + name +
                "\nUUID='" + UUID +
                "\ntype=" + type +
                "\nlevelCode='" + levelCode +
                ",mPower=" + mPower
                ;
    }
}
