package dupd.hku.com.hkusap.model;

import dupd.hku.com.hkusap.model.IEnum.SPWeightType;

import static dupd.hku.com.hkusap.model.IEnum.IDPointUsage.SPPointUsageRoute;


public class SPNavigationRouteModel {

    public String segmentID;
    public String levelCode;
    public SPPointModel from;
    public SPPointModel to;

    public SPNavigationRouteModel(String SegmentID, String from, String to) {
        segmentID = SegmentID;
        levelCode = segmentID.substring(0, 3);
        this.from = pointWithRouteDataString(from, 0);
        this.to = pointWithRouteDataString(to, 1);
    }

    public SPPointModel pointWithRouteDataString(String text, int index) {
        String[] split = text.split(",");
        double longitude = Double.valueOf(split[0]);
        double latitude = Double.valueOf(split[1]);

        SPWeightType type = SPWeightType.init(Integer.valueOf(split[2]));
        String levelCode = split[3];

        SPPointModel point = new SPPointModel(latitude, longitude);
        point.type = type;
        point.usage = SPPointUsageRoute;
        point.pointID = segmentID + index;
        point.index = Integer.valueOf(point.pointID);
        point.levelCode = levelCode;

        return point;
    }


    public IDRouteModel mutableConverted() {
        IDPointModel startPoint = from.mutableConverted();
        IDPointModel endPoint = to.mutableConverted();
        int index = Integer.valueOf(segmentID);
        return new IDRouteModel(startPoint, endPoint, levelCode, index);
    }

    public String primaryKey() {
        return segmentID;
    }
}
