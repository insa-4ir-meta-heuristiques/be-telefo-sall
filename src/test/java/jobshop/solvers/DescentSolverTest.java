package jobshop.solvers;
import jobshop.Instance;
import jobshop.solvers.neighborhood.Neighborhood;
import jobshop.solvers.neighborhood.Nowicki;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;

public class DescentSolverTest {
    @Test
    public void descentSolverTest() throws IOException {
        GreedySolver solver=new GreedySolver(GreedySolver.Priority.EST_SPT);
        Nowicki nowicki=new Nowicki();
        DescentSolver descentSolver=new DescentSolver(nowicki,solver);
        Instance instance = Instance.fromFile(Paths.get("instances/aaa1"));
        descentSolver.solve(instance,10000);


    }
}
