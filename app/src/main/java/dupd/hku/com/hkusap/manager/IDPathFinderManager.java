package dupd.hku.com.hkusap.manager;

import com.amap.api.maps.model.LatLng;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import dupd.hku.com.hkusap.R;
import dupd.hku.com.hkusap.model.IDPathResultModel;
import dupd.hku.com.hkusap.model.IDPointModel;
import dupd.hku.com.hkusap.model.IDRouteModel;
import dupd.hku.com.hkusap.model.SPNavigationRouteModel;

public class IDPathFinderManager {

    public List<IDRouteModel> routes;
    private static IDPathFinderManager INSTANCE;

    public static IDPathFinderManager getInstance() {
        if (INSTANCE == null) INSTANCE = new IDPathFinderManager();
        return INSTANCE;
    }

    //注册导航路线
    public void registerNavigationRoutes(List<SPNavigationRouteModel> routes) {
        this.routes = new ArrayList<>();
        //遍历所有SPNavigationRouteModel
        for (SPNavigationRouteModel route : routes) {
            //将SPNavigationRouteModel转化为IDRouteModel方便后面处理
            this.routes.add(route.mutableConverted());
        }
    }

    public IDPathResultModel shortestPathFrom(IDPointModel from, IDPointModel to) {

        DeadReckoningManager manager = DeadReckoningManager.getInstance();
        //获取起始点from最近的线路startRoute
        IDRouteModel startRoute = manager.getNearestRouteFromPoint(from);
        //获得from到startRoute两个端点的线段组合
        List<IDRouteModel> seperatedStartRoutes = seperateRoute(startRoute, from);

        //获得终点to最近的线路endRoute
        IDRouteModel endRoute = manager.getNearestRouteFromPoint(to);

        //获得end到endRoute两个端点的线段组合
        List<IDRouteModel> seperatedEndRoutes = seperateRoute(endRoute, to);

        List<IDRouteModel> routes = filterIntervalLevelRoutes(from.levelCode, to.levelCode);

        //从道路集合里去掉startRoute和endRoute
        routes.remove(startRoute);
        routes.remove(endRoute);

        //把生成的seperatedStartRoutes和seperatedEndRoutes集合添加进道路集合里
        routes.addAll(seperatedStartRoutes);
        routes.addAll(seperatedEndRoutes);

        //生成graphy
        GKGraph graphView = new GKGraph(routes);

        //获取最短路径并扔进IDPathResultModel处理
        return new IDPathResultModel(graphView.findPathFromNode(from, to));
    }


    //用点分离道路
    private List<IDRouteModel> seperateRoute(IDRouteModel route, IDPointModel point) {
        List<IDRouteModel> routeList = new ArrayList<>();

        //取得点point落在route上的坐标projection
        LatLng projection = DeadReckoningManager.getInstance().intersectionCoordinateFrom(point.coordinate, route);
        /*
        if (projection.equals(route.start.coordinate)) {  //如果点坐落在目标道路的起始点上
            //返回由目标点point和route终点组成的新道路
            IDRouteModel result = new IDRouteModel(point, route.end, route.levelCode, 0);
            routeList.add(result);
        } else if (projection.equals(route.end.coordinate)) {//如果点坐落在目标道路的终点上
            //返回由目标点point和route起点组成的新道路
            IDRouteModel result = new IDRouteModel(route.start, point, route.levelCode, 0);
            routeList.add(result);*/

        if (projection.equals(route.start.coordinate)||projection.equals(route.end.coordinate)) {//如果点坐落在目标道路的终点上
            //返回由目标点point和route起点组成的新道路
            IDRouteModel result = new IDRouteModel(route.start, point, route.levelCode, 0);
            if(point.coordinate.equals(route.start.coordinate))
                result = new IDRouteModel(point, route.end, route.levelCode, 0);
            routeList.add(result);
        } else { //如果落在道路中间

            //生成route起点与目标点point组成的路
            IDRouteModel upperRoute = new IDRouteModel(route.start, point, route.levelCode, 0);

            //生成route终点与目标点point组成的路
            IDRouteModel lowerRoute = new IDRouteModel(point, route.end, route.levelCode, 1);

            routeList.add(upperRoute);
            routeList.add(lowerRoute);
        }
        return routeList;
    }

    private List<IDRouteModel> filterIntervalLevelRoutes(String fromLevel, String toLevel) {
        List<IDRouteModel> routeModels = new ArrayList<>();

        //如果fromLevel 大于 toLevel
        boolean flashback = fromLevel.compareTo(toLevel) > 0;

        for (IDRouteModel route : routes) {

            if (flashback) {//取 toLevel 到 fromLevel 之间的Route
                if (route.levelCode.compareTo(toLevel) < 0 || route.levelCode.compareTo(fromLevel) > 0) {
                    continue;
                }
                routeModels.add(route);
            } else {//取 fromLevel 到 toLevel 之间的Route
                if (route.levelCode.compareTo(fromLevel) < 0 || route.levelCode.compareTo(toLevel) > 0) {
                    continue;
                }
                routeModels.add(route);
            }
        }

        return routeModels;
    }

    public boolean compareLevel(String level, String nextLevel) {
        return level.compareTo(nextLevel) > 0;
    }
}
