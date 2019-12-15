package dupd.hku.com.hkusap.manager;

import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

import dupd.hku.com.hkusap.model.IDPointModel;
import dupd.hku.com.hkusap.model.IDRouteModel;

public class IDRouteCollection {
    public List<IDRouteModel> routes;

    public IDRouteCollection(List<IDRouteModel> routes) {
        this.routes = routes;
    }

    public PolylineOptions enabledRathsForLevel(String levelCode) {
        PolylineOptions enabledLines = new PolylineOptions();

        //过滤当前楼层的Route
        List<IDRouteModel> enabledRoutes = findForLevelCode(levelCode);

        LatLng endCoordinate = null;

        for (IDRouteModel route : enabledRoutes) {
            if (endCoordinate == null) {//首次循环 Route 起始-结束点 加入集合
                enabledLines.add(route.start.coordinate);
                enabledLines.add(route.end.coordinate);
            } else if (!route.start.coordinate.equals(endCoordinate)) { //如果Route 起始点 和 前一次的结束点不一样 直接循环下一次
                continue;
            } else {// Route 起始-结束点 加入集合
                enabledLines.add(route.start.coordinate);
                enabledLines.add(route.end.coordinate);
            }
            endCoordinate = route.end.coordinate;//当前Route的结束点临时存储,给下一次循环使用
        }
        return enabledLines;
    }

    public List<PolylineOptions> disabledPathsForLevel(String levelCode) {
        List<PolylineOptions> polylineOptionsList = new ArrayList<>();

        for (String level : shouldIndicateRouteLevelCodes()) {
            if (level.equals(levelCode)) continue;

            PolylineOptions enabledLines = enabledRathsForLevel(level);
            polylineOptionsList.add(enabledLines);
        }

        return polylineOptionsList;
    }

    public List<String> shouldIndicateRouteLevelCodes() {
        List<String> levels = new ArrayList<>();
        for (IDRouteModel item : routes) {
            String levelCode = item.levelCode;
            if (!levels.contains(levelCode)) {
                levels.add(levelCode);
            }
        }
        return levels;
    }

    public IDPointModel destinationPoint() {
        return routes.get(routes.size() - 1).end;
    }

    public List<IDRouteModel> findForLevelCode(String levelCode) {
        List<IDRouteModel> routelList = new ArrayList<>();
        for (IDRouteModel model : routes) {
            if (levelCode.equals(model.levelCode)) {
                routelList.add(model);
            }
        }
        return routelList;
    }
}
