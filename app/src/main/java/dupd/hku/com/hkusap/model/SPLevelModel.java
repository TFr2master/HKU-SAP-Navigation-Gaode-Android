package dupd.hku.com.hkusap.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by liuwei on 2018/9/10.
 */

public class SPLevelModel {

    @SerializedName("postalBuildingCode")
    public String postalCode;
    public String levelID;
    public String levelCode;
    public int level;
    @SerializedName("longName")
    public String name;
    @SerializedName("name")
    public String abbreviation;
}
