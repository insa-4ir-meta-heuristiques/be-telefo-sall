package jobshop.solvers;

import jobshop.Instance;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Schedule;
import jobshop.solvers.neighborhood.Neighborhood;
import jobshop.solvers.neighborhood.Nowicki;

import java.util.List;
import java.util.Optional;

/** An empty shell to implement a descent solver. */
public class DescentSolver implements Solver {

    final Neighborhood neighborhood;
    final Solver baseSolver;

    /** Creates a new descent solver with a given neighborhood and a solver for the initial solution.
     *
     * @param neighborhood Neighborhood object that should be used to generates neighbor solutions to the current candidate.
     * @param baseSolver A solver to provide the initial solution.
     */
    public DescentSolver(Neighborhood neighborhood, Solver baseSolver) {
        this.neighborhood = neighborhood;
        this.baseSolver = baseSolver;
    }

    @Override
    public Optional<Schedule> solve(Instance instance, long deadline) {
        ResourceOrder resourceOrder1=new ResourceOrder(baseSolver.solve(instance,deadline).get());
        ResourceOrder resourceOrder0;
        int Min,min;
        long startTime = System.currentTimeMillis();
        long endTime;

        do {
            Min=resourceOrder1.toSchedule().get().makespan();
            //System.out.println("makespan="+Min);
            List<ResourceOrder> candidates=neighborhood.generateNeighbors(resourceOrder1);
            int j=0;
            //recuperer la 1ere resource order valide parmi celles de resourceOrder1
            while(j< candidates.size() && candidates.get(j).toSchedule().isEmpty()){
                j++;
            }
            ResourceOrder resMin = candidates.get(j);
            //On selectionne le meilleur voisin: celui qui a le + petit makespan
            min = resMin.toSchedule().get().makespan();
            for (int i = j; i < candidates.size(); i++) {

                if (!candidates.get(i).toSchedule().isEmpty() && candidates.get(i).toSchedule().get().isValid() && candidates.get(i).toSchedule().get().makespan() < min) {
                    resMin = candidates.get(i);
                    min = resMin.toSchedule().get().makespan();
                }
            }
            resourceOrder0=resourceOrder1.copy();
            resourceOrder1=resMin.copy();
            endTime = System.currentTimeMillis();
        }
        while(min<Min && endTime-startTime<=deadline);
        return resourceOrder0.toSchedule();
    }

}
