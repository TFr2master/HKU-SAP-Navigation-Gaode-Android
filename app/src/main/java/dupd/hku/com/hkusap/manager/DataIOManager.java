package dupd.hku.com.hkusap.manager;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import dupd.hku.com.hkusap.model.BeaconDebugForm.BeaconDebug;
import dupd.hku.com.hkusap.model.Often;
import dupd.hku.com.hkusap.model.POIPoint;
import dupd.hku.com.hkusap.model.SPBuildingBoundary;
import dupd.hku.com.hkusap.model.SPBuildingBoundaryLineModel;
import dupd.hku.com.hkusap.model.SPBuildingModel;
import dupd.hku.com.hkusap.model.SPLevelModel;
import dupd.hku.com.hkusap.model.SPNavigationRouteModel;
import dupd.hku.com.hkusap.model.SPPlateModel;
import dupd.hku.com.hkusap.model.SPRoomModel;
import dupd.hku.com.hkusap.model.Sapdb;
import dupd.hku.com.hkusap.utils.AssetsUtil;
import io.reactivex.subjects.PublishSubject;

public class DataIOManager {

    private static DataIOManager INSTANCE;
    public PublishSubject<String> mSubject = PublishSubject.create();
    public PublishSubject<Often> mOften = PublishSubject.create();
    public Sapdb sapdb;
    public List<SPNavigationRouteModel> routes = new ArrayList<>();
    public List<SPBuildingBoundaryLineModel> boundaryLines = new ArrayList<>();
    public List<SPBuildingBoundary> buildingBoundaries = new ArrayList<>();
    public StringBuilder mNormalDebug;
    public List<BeaconDebug> mBeaconDebugList;

    public List<POIPoint> POI;

    public static DataIOManager getInstance() {
        if (INSTANCE == null) INSTANCE = new DataIOManager();
        return INSTANCE;
    }

    private DataIOManager() {
        try {
            mNormalDebug = new StringBuilder();
            mBeaconDebugList = new ArrayList<>();
            Gson gson = new Gson();

            String json = AssetsUtil.readAssetJson("sapdb.json");
            sapdb = gson.fromJson(json, Sapdb.class);

            json = AssetsUtil.readAssetJson("NavigationRoute/navigationRoute.json");
            JSONObject navigationRoute = new JSONObject(json);
            String prefix = navigationRoute.optString("prefix");
            JSONArray levels = navigationRoute.optJSONArray("levels");
            for (int i = 0; i < levels.length(); i++) {
                String fileName = prefix + "_" + levels.optString(i) + ".xml";
                routes.addAll(readNavigationRouteModelList(fileName));
            }

            json = AssetsUtil.readAssetJson("Boundary/boundary.json");
            JSONArray boundaryArray = new JSONObject(json).optJSONArray("boundary");
            for (int i = 0; i < boundaryArray.length(); i++) {
                JSONObject boundary = boundaryArray.optJSONObject(i);
                prefix = boundary.optString("prefix");
                String name = boundary.optString("name");
                String postalCode = boundary.optString("postalCode");


                levels = boundary.optJSONArray("levels");
                for (int j = 0; j < levels.length(); j++) {
                    String level = levels.optString(j);
                    String fileName = prefix + "_" + level + ".xml";

                    List<SPBuildingBoundaryLineModel> boundaryLineList = readBuildingBoundaryLineModelList(fileName);
                    boundaryLines.addAll(boundaryLineList);

                    SPBuildingBoundary buildingBoundary = new SPBuildingBoundary(postalCode, name, level);
                    buildingBoundaries.add(buildingBoundary);
                }
            }
            POI = getPOIList("POI.plist");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public List<POIPoint> getPOIList(String fileName){
        List<POIPoint> POIList = new ArrayList<>();
        InputStream stream = AssetsUtil.readAssetInputStream("POI/" + fileName);
        if (stream == null) return POIList;
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = factory.newPullParser();
            xmlPullParser.setInput(stream, "UTF-8");
            int eventType = xmlPullParser.getEventType();

            String key = "array";
            String POIFileName;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String nodeName = xmlPullParser.getName();
                if (eventType==XmlPullParser.START_TAG) {
                    if (key.equals(nodeName)) {
                        key = "string";
                    }
                    if (key.equals(nodeName)) {
                        POIFileName="POI/"+"P_"+xmlPullParser.nextText()+".json";
                        POIList.addAll(getPOIPoint(POIFileName));
                    }
                }
                eventType = xmlPullParser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return POIList;
    }
    public List<POIPoint> getPOIPoint(String fileName){
        List<POIPoint> POIList = new ArrayList<>();
        String json = AssetsUtil.readAssetJson(fileName);
        try {
            JSONArray POIArray = new JSONArray(json);
            for(int i=0;i<POIArray.length();i++){
                JSONObject POIObject = POIArray.getJSONObject(i);
                POIPoint POIPoint=new POIPoint(POIObject);
                POIList.add(POIPoint);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return POIList;
    }
    public List<POIPoint> POIPointForLevel(String level) {
        List<POIPoint> POIList = new ArrayList<>();
        for (POIPoint point : POI) {
            if (point.levelCode.equals(level)) {
                POIList.add(point);
            }
        }
        return POIList;
    }
    public SPRoomModel plateToRoom(SPPlateModel plate) {
        if(sapdb==null){
            return null;
        }
        for (SPRoomModel room : sapdb.room) {
            if (room.roomID().equals(plate.roomID)) {
                return room;
            }
        }
        return null;
    }

    public String abbreviationForLevelCode(String level) {
        SPLevelModel model = null;
        for (SPLevelModel item : sapdb.level) {
            if (item.levelCode.equals(level)) {
                model = item;
            }
        }
        return model.abbreviation;
    }

    public List<SPBuildingBoundary> boundaryForLevel(String level) {
        List<SPBuildingBoundary> boundaries = new ArrayList<>();
        for (SPBuildingBoundary boundary : buildingBoundaries) {
            if (boundary.levelCode.equals(level)) {
                boundaries.add(boundary);
            }
        }
        return boundaries;
    }

    public SPBuildingModel postalCodeToBuilding(String postalCode) {
        List<SPBuildingModel> buildingList = DataIOManager.getInstance().sapdb.building;
        for (SPBuildingModel building : buildingList) {
            if (building.postalCode.equals(postalCode)) {
                return building;
            }
        }
        return null;
    }

    public SPBuildingModel BuildingNameToBuilding(String buildingName) {
        List<SPBuildingModel> buildingList = DataIOManager.getInstance().sapdb.building;
        for (SPBuildingModel building : buildingList) {
            if (building.name.equals(buildingName)) {
                return building;
            }
        }
        return null;
    }

    public static List<SPBuildingBoundaryLineModel> readBuildingBoundaryLineModelList(String fileName) {
        List<SPBuildingBoundaryLineModel> boundaryLineList = new ArrayList<>();
        InputStream stream = AssetsUtil.readAssetInputStream("Boundary/" + fileName);
        if (stream == null) return boundaryLineList;
        boundaryLineList.addAll(parseBuildingBoundaryLineModelList(stream));
        return boundaryLineList;
    }

    public static List<SPBuildingBoundaryLineModel> parseBuildingBoundaryLineModelList(InputStream in) {
        List<SPBuildingBoundaryLineModel> boundaryLineList = new ArrayList<>();
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = factory.newPullParser();
            xmlPullParser.setInput(in, "UTF-8");
            int eventType = xmlPullParser.getEventType();

            String buildingID = "";
            String SegmentID = "";
            String SegmentPointsList = "";
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String nodeName = xmlPullParser.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if ("buildingID".equals(nodeName)) {
                            buildingID = xmlPullParser.nextText();
                        } else if ("SegmentID".equals(nodeName)) {
                            SegmentID = xmlPullParser.nextText();
                        } else if ("SegmentPointsList".equals(nodeName)) {
                            SegmentPointsList = xmlPullParser.nextText();
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if ("SegmentInfo".equals(nodeName)) {
                            SPBuildingBoundaryLineModel model = new SPBuildingBoundaryLineModel(SegmentID, buildingID, SegmentPointsList);
                            boundaryLineList.add(model);
                        }
                        break;
                }
                eventType = xmlPullParser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return boundaryLineList;
    }

    public static List<SPNavigationRouteModel> readNavigationRouteModelList(String fileName) {
        List<SPNavigationRouteModel> routeList = new ArrayList<>();
        InputStream stream = AssetsUtil.readAssetInputStream("NavigationRoute/" + fileName);
        if (stream == null) return routeList;
        routeList.addAll(parseNavigationRouteModelList(stream));
        return routeList;
    }

    public static List<SPNavigationRouteModel> parseNavigationRouteModelList(InputStream stream) {
        List<SPNavigationRouteModel> routeList = new ArrayList<>();
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = factory.newPullParser();
            xmlPullParser.setInput(stream, "UTF-8");
            int eventType = xmlPullParser.getEventType();

            String SegmentID = "";
            String from = "";
            String to = "";
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String nodeName = xmlPullParser.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if ("SegmentID".equals(nodeName)) {
                            SegmentID = xmlPullParser.nextText();
                        } else if ("from".equals(nodeName)) {
                            from = xmlPullParser.nextText();
                        } else if ("to".equals(nodeName)) {
                            to = xmlPullParser.nextText();
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if ("SegmentInfo".equals(nodeName)) {
                            SPNavigationRouteModel route = new SPNavigationRouteModel(SegmentID, from, to);
                            routeList.add(route);
                        }
                        break;
                }
                eventType = xmlPullParser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return routeList;
    }
}
