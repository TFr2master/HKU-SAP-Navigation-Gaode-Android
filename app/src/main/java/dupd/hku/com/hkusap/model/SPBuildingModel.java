package dupd.hku.com.hkusap.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dupd.hku.com.hkusap.manager.DataIOManager;

public class SPBuildingModel {

    @SerializedName("postalBuildingCode")
    public String postalCode;
    @SerializedName("lat")
    public double latitude;
    @SerializedName("lon")
    public double longitude;
    public String address;
    public String name;


    public List<SPLevelModel> getLevels() {
        List<SPLevelModel> levelList = new ArrayList<>();
        for (SPLevelModel item : DataIOManager.getInstance().sapdb.level) {
            if (postalCode.equals(item.postalCode)) {
                levelList.add(item);
            }
        }
        Collections.sort(levelList, new Sapdb.LevelComparator());
        return levelList;
    }


    public List<SPPlateModel> getPlates(String levelCode) {

        List<SPPlateModel> plateList = new ArrayList<>();
        for (SPPlateModel plate : DataIOManager.getInstance().sapdb.plate) {
            if (postalCode.equals(plate.postalCode) && plate.levelCode.equals(levelCode) && plate.searchable == 1) {
                plateList.add(plate);
            }
        }

        return plateList;
    }
}
