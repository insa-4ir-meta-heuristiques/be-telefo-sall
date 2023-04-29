package jobshop.solvers;

import jobshop.Instance;
import jobshop.encodings.Schedule;
import jobshop.solvers.neighborhood.Neighborhood;
import jobshop.solvers.neighborhood.Nowicki;

import java.util.Optional;

/** Common interface that must implemented by all solvers. */
public interface Solver {

    /** Look for a solution until blocked or a deadline has been met.
     *
     * @param instance Jobshop instance that should be solved.
     * @param deadline Absolute time at which the solver should have returned a solution.
     *                 This time is in milliseconds and can be compared with System.currentTimeMilliseconds()
     * @return An optional schedule that will be non empty if a solution was found.
     */
    Optional<Schedule> solve(Instance instance, long deadline);

    /** Static factory method to create a new solver based on its name. */
    static Solver getSolver(String name) {
        GreedySolver solver=new GreedySolver(GreedySolver.Priority.EST_SPT);
        Nowicki nowicki=new Nowicki();
        //paramétres des tabous
        int maxIter=50;
        int timeTabouInit=5;

        switch (name) {
            case "basic": return new BasicSolver();
            case "spt": return new GreedySolver(GreedySolver.Priority.SPT);
            case "lrpt":return new GreedySolver(GreedySolver.Priority.LRPT);
            case "est_lrpt":return new GreedySolver(GreedySolver.Priority.EST_LRPT);
            case "est_spt":return new GreedySolver(GreedySolver.Priority.EST_SPT);
            case "est_lpt_random":return new GreedySolver(GreedySolver.Priority.EST_LPT_RANDOM);
            case "est_lrpt_random": return new GreedySolver(GreedySolver.Priority.EST_LRPT_RANDOM);
            case "descent_est_spt":return new DescentSolver(nowicki,new GreedySolver(GreedySolver.Priority.EST_SPT));
            case "descent_est_lrpt":return new DescentSolver(nowicki,new GreedySolver(GreedySolver.Priority.EST_LRPT));
            case "tabou_est_spt":return new TabooSolver(nowicki,new GreedySolver(GreedySolver.Priority.EST_SPT),maxIter,2,timeTabouInit);
            case "tabou_est_lrpt":return new TabooSolver(nowicki,new GreedySolver(GreedySolver.Priority.EST_LRPT),maxIter,2,timeTabouInit);
            //case "dct": return new DescentSolver();
            default: throw new RuntimeException("Unknown solver: "+ name);
        }
    }

}
