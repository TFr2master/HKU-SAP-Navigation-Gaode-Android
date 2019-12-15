package dupd.hku.com.hkusap.model;

import static dupd.hku.com.hkusap.model.IEnum.IDPointUsage.SPPointUsageBuildingBoundary;
import static dupd.hku.com.hkusap.model.IEnum.SPWeightType.SPWeightTypeOthers;

public class SPBuildingBoundaryLineModel {

    public String segmentID;
    public String postalCode;
    public String levelCode;
    public int index;
    public SPPointModel from;
    public SPPointModel to;

    public SPBuildingBoundaryLineModel(String SegmentID, String buildingID, String SegmentPointsList) {

        postalCode = getPostalCodeFrom(buildingID);
        segmentID = SegmentID;
        levelCode = segmentID.substring(0, 3);
        index = Integer.valueOf(segmentID);

        String[] components = SegmentPointsList.split(",");

        double fromLongitude = Double.valueOf(components[0]);
        double fromLatitude = Double.valueOf(components[1]);
        double toLongitude = Double.valueOf(components[2]);
        double toLatitude = Double.valueOf(components[3]);

        SPPointModel from = new SPPointModel(fromLatitude, fromLongitude);

        from.usage = SPPointUsageBuildingBoundary;
        from.type = SPWeightTypeOthers;
        from.postalCode = postalCode;
        from.levelCode = levelCode;
        from.pointID = segmentPointID("0");
        from.index = Integer.valueOf(from.pointID);
        this.from = from;

        SPPointModel to = new SPPointModel(toLatitude, toLongitude);
        to.usage = SPPointUsageBuildingBoundary;
        to.type = SPWeightTypeOthers;
        to.postalCode = postalCode;
        to.levelCode = levelCode;
        to.pointID = segmentPointID("1");
        to.index = Integer.valueOf(to.pointID);
        this.to = to;
    }

    public String convertPositionCode(String code) {
        int index = Integer.valueOf(code);
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

    public String getPostalCodeFrom(String buildingID) {
        String positionCode = buildingID.substring(2, 4);
        String convertedCode = convertPositionCode(positionCode);

        String prefix = buildingID.substring(0, 2);
        String sufix = buildingID.substring(4);
        return String.format("%s%s%s", prefix, convertedCode, sufix);
    }

    public String segmentPointID(String indentifier) {
        return String.format("%s%s", segmentID, indentifier);
    }
}
