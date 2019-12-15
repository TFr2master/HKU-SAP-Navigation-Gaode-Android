package dupd.hku.com.hkusap.manager;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.WeightedMultigraph;
import org.junit.Before;
import org.junit.Test;

import dupd.hku.com.hkusap.model.IDPointModel;
import dupd.hku.com.hkusap.model.IDRouteModel;
import dupd.hku.com.hkusap.utils.GeoUtils;

public class GKGraphTest {


    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void findPathFromNode() {
        DataIOManager results = DataIOManager.getInstance();

        System.out.println(results.routes.size());


        IDPointModel from = results.routes.get(0).mutableConverted().start;

        IDPointModel to = results.routes.get(results.routes.size() - 1).mutableConverted().end;


        WeightedMultigraph<IDPointModel, PointDefaultWeightedEdge> graph = new WeightedMultigraph<>(PointDefaultWeightedEdge.class);

        for (int i = 0; i < results.routes.size(); i++) {

            IDRouteModel route = results.routes.get(i).mutableConverted();
            IDPointModel start = route.start;
            IDPointModel end = route.end;

            graph.addVertex(start);
            graph.addVertex(end);

            graph.addEdge(start, end, new PointDefaultWeightedEdge(start, end));
        }

        DijkstraShortestPath<IDPointModel, PointDefaultWeightedEdge> dijkstraPath = new DijkstraShortestPath<>(graph);

        ShortestPathAlgorithm.SingleSourcePaths<IDPointModel, PointDefaultWeightedEdge> iPaths = dijkstraPath.getPaths(from);

        GraphPath<IDPointModel, PointDefaultWeightedEdge> result = iPaths.getPath(to);

        System.out.println(result.getVertexList().size());
    }

    public static class PointDefaultWeightedEdge extends DefaultWeightedEdge {
        IDPointModel source;
        IDPointModel target;

        public PointDefaultWeightedEdge(IDPointModel source, IDPointModel target) {
            this.source = source;
            this.target = target;
        }

        @Override
        protected Object getSource() {
            return source;
        }

        @Override
        protected Object getTarget() {
            return target;
        }

        @Override
        protected double getWeight() {
            return GeoUtils.distance(source.coordinate, target.coordinate);
        }
    }
}