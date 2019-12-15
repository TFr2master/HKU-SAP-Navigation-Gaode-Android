package dupd.hku.com.hkusap.model;

import com.google.gson.annotations.SerializedName;

public class SPRoomModel {

    @SerializedName("postalBuildingCode")
    public String postalCode;
    public String roomCode;
    public String levelCode;
    @SerializedName("lat")
    public double latitude;
    @SerializedName("lon")
    public double longitude;
    public String name;

    public String roomID() {
        return postalCode + levelCode + roomCode;
    }
}
