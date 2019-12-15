package dupd.hku.com.hkusap.model;

import java.util.Comparator;
import java.util.List;

public class Sapdb {

    public List<SPBuildingModel> building;
    public List<SPLevelModel> level;
    public List<SPRoomModel> room;
    public List<SPPlateModel> plate;

    public static class LevelComparator implements Comparator<SPLevelModel> {

        @Override
        public int compare(SPLevelModel o1, SPLevelModel o2) {
            return Integer.valueOf(o2.levelCode) - Integer.valueOf(o1.levelCode);
        }
    }

    public static class LevelCodeComparator implements Comparator<String> {

        @Override
        public int compare(String o1, String o2) {
            return Integer.valueOf(o2) - Integer.valueOf(o1);
        }
    }
}
