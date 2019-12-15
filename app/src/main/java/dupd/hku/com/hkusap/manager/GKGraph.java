package dupd.hku.com.hkusap.manager;

import com.orhanobut.logger.Logger;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.WeightedMultigraph;

import java.util.List;

import dupd.hku.com.hkusap.model.IDPointModel;
import dupd.hku.com.hkusap.model.IDRouteModel;
import dupd.hku.com.hkusap.model.IEnum;
import dupd.hku.com.hkusap.utils.GeoUtils;

public class GKGraph {

    private List<IDRouteModel> routes;

    /**
     * @param routes 道路集合
     */
    public GKGraph(List<IDRouteModel> routes) {
        this.routes = routes;
        Logger.d(routes.size());
    }

    /**
     * @param from 起点
     * @param to   终点
     * @return 最短路径
     */
    public List<IDPointModel> findPathFromNode(IDPointModel from, IDPointModel to) {

        WeightedMultigraph<IDPointModel, DefaultWeightedEdge> graph = new WeightedMultigraph<>(DefaultWeightedEdge.class);

        for (IDRouteModel route : routes) {
            IDPointModel start = route.start;
            IDPointModel end = route.end;

            graph.addVertex(start);
            graph.addVertex(end);

            graph.addEdge(start, end, new RouteWeightedEdge(start, end));
        }

        DijkstraShortestPath<IDPointModel, DefaultWeightedEdge> dijkstraPath = new DijkstraShortestPath<>(graph);

        ShortestPathAlgorithm.SingleSourcePaths<IDPointModel, DefaultWeightedEdge> iPaths = dijkstraPath.getPaths(from);

        GraphPath<IDPointModel, DefaultWeightedEdge> result = iPaths.getPath(to);

        return result.getVertexList();
    }

    public static class RouteWeightedEdge extends DefaultWeightedEdge {
        private IDPointModel start;
        private IDPointModel end;

        public RouteWeightedEdge(IDPointModel start, IDPointModel end) {
            this.start = start;
            this.end = end;
        }

        @Override
        protected Object getSource() {
            return start;
        }

        @Override
        protected Object getTarget() {
            return end;
        }

        @Override
        protected double getWeight() {
            if(start.type ==IEnum.SPWeightType.SPWeightTypeEscalator&&end.type==IEnum.SPWeightType.SPWeightTypeEscalator)
                return 2;
            return GeoUtils.distance(start.coordinate, end.coordinate);
        }
    }
}
