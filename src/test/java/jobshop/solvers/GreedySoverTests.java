package jobshop.solvers;
import jobshop.Instance;
import jobshop.encodings.Schedule;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;

public class GreedySoverTests {
    @Test
    public void testSPT() throws IOException {
        Instance instance = Instance.fromFile(Paths.get("instances/aaa3"));
        GreedySolver greedySolver=new GreedySolver(GreedySolver.Priority.SPT);
        Schedule schedule = greedySolver.solve(instance,10000).get();
        assert schedule.makespan() == 53;

    }
    @Test
    public void testLPT() throws IOException {
        Instance instance = Instance.fromFile(Paths.get("instances/aaa3"));
        GreedySolver greedySolver=new GreedySolver(GreedySolver.Priority.LPT);
        Schedule schedule = greedySolver.solve(instance,10000).get();
        assert schedule.makespan() == 92;

    }
    @Test
    public void testLRPT() throws IOException {
        Instance instance = Instance.fromFile(Paths.get("instances/aaa3"));
        GreedySolver greedySolver=new GreedySolver(GreedySolver.Priority.LRPT);
        Schedule schedule = greedySolver.solve(instance,10000).get();
        assert schedule.makespan() == 54;

    }
    @Test
    public void testEST_SPT() throws IOException {
        Instance instance = Instance.fromFile(Paths.get("instances/aaa3"));
        GreedySolver greedySolver=new GreedySolver(GreedySolver.Priority.EST_SPT);
        Schedule schedule = greedySolver.solve(instance,10000).get();
        String gant = schedule.asciiGantt();
        System.out.println(gant);
        assert schedule.makespan() == 48;

    }
    @Test
    public void testEST_LRPT() throws IOException {
        Instance instance = Instance.fromFile(Paths.get("instances/aaa3"));
        GreedySolver greedySolver=new GreedySolver(GreedySolver.Priority.EST_LRPT);
        Schedule schedule = greedySolver.solve(instance,10000).get();
        String gant = schedule.asciiGantt();
        System.out.println(gant);
        System.out.println("make span = " + schedule.makespan());
        assert schedule.makespan() == 56;

    }

}
