package dupd.hku.com.hkusap.model;

import java.util.ArrayList;
import java.util.List;

public class IDPathResultModel {

    public List<IDPointModel> points;
    public List<IDRouteModel> routes;
    public IDPointModel startPoint;

    public IDPathResultModel(List<IDPointModel> points) {

        List<IDRouteModel> routes = new ArrayList<>();

        for (int i = 0; i < points.size() - 1; i++) {
            IDPointModel from = points.get(i);
            IDPointModel to = points.get(i + 1);

            IDRouteModel route = new IDRouteModel(from, to, from.levelCode, i);
            routes.add(route);
        }

        this.points = new ArrayList<>(points);
        this.routes = routes;
        this.startPoint = points.get(0);
    }

    public IDPathResultModel(List<IDPointModel> points, List<IDRouteModel> routes, IDPointModel startPoint) {
        this.points = points;
        this.routes = routes;
        this.startPoint = startPoint;
    }
}
