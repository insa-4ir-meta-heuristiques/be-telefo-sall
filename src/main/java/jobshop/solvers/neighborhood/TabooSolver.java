package jobshop.solvers.neighborhood;

import jobshop.Instance;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Schedule;
import jobshop.encodings.Task;
import jobshop.solvers.Solver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

//import static java.util.stream.Nodes.collect;

public class TabooSolver implements Solver{

    final Neighborhood neighborhood;
    final Solver baseSolver;
    final int maxIter;
    final int version;
    final int timeTabouInit;

    /** Creates a new TabooSolver with a given neighborhood and a solver for the initial solution.
     *
     * @param neighborhood Neighborhood object that should be used to generates neighbor solutions to the current candidate.
     * @param baseSolver A solver to provide the initial solution.
     */
    public TabooSolver(Neighborhood neighborhood, Solver baseSolver,int maxIter, int version, int timeTabouInit) {
        this.neighborhood = neighborhood;
        this.baseSolver = baseSolver;
        this.maxIter = maxIter;
        this.version = version;
        this.timeTabouInit=timeTabouInit;
    }

    @Override
    public Optional<Schedule> solve(Instance instance, long deadline) {
        ResourceOrder s=new ResourceOrder(baseSolver.solve(instance,deadline).get());
        ResourceOrder sOpt = s;//represente le s*
        int sMakespan;
        int sPrimeMakespan;
        int sOptmakespan = sOpt.toSchedule().get().makespan();
        int k = 0;
        long t0 = System.currentTimeMillis();
        long t;
        ArrayList<Nowicki.Swap> sTabou = new ArrayList<>();
        HashSet<ArrayList<Task>> sTabouHashSet=new HashSet<>();

        ArrayList<Integer> timeTabou = new ArrayList<>();
        switch(version) {
            case 1:
                do {
                    sMakespan = s.toSchedule().get().makespan();
                    k++;
                    System.out.println("makespan=" + sMakespan);
                    List<ResourceOrder> candidates = neighborhood.generateNeighbors(s);
                    System.out.println("neighbors:" + candidates);
                    int j = 0;
                    //choisir le 1er candidat valide
                    while (j< candidates.size() && candidates.get(j).toSchedule().isEmpty()) {
                        j++;
                    }
                    //trouver le s'
                    ResourceOrder sPrime = candidates.get(j);
                    sPrimeMakespan = sPrime.toSchedule().get().makespan();
                    for (int i = j; i < candidates.size(); i++) {
                        if (candidates.get(i).toSchedule().get().isValid() && candidates.get(i).toSchedule().get().makespan() < sPrimeMakespan) {
                            sPrime = candidates.get(i);
                            sPrimeMakespan = sPrime.toSchedule().get().makespan();
                        }
                    }
                    System.out.println("makespan:"+sPrimeMakespan);
                    if (sPrimeMakespan < sOptmakespan) {
                        sOpt = sPrime;
                        sOptmakespan = sPrimeMakespan;
                    }
                    s = sPrime;
                    t = System.currentTimeMillis();
                }
                while (k <= this.maxIter && (t - t0) < deadline);
                break;
            case 2:
                do {
                    sMakespan = s.toSchedule().get().makespan();
                    k++;
                    System.out.println("makespan=" + sMakespan);
                    ResourceOrder sPrime= getMinSwap(s, sTabou).generateFrom(s);
                    sPrimeMakespan=sPrime.toSchedule().get().makespan();
                    //faudra gérér le updatesTabou avec un hashSet
                    updatesTabou(sTabou,timeTabou);
                    sTabou.add(getMinSwap(s, sTabou));
                    timeTabou.add(timeTabouInit);
                    System.out.println("makespan:"+sPrimeMakespan);
                    if (sPrimeMakespan < sOptmakespan) {
                        sOpt = sPrime;
                        sOptmakespan = sPrimeMakespan;
                    }
                    s = sPrime;
                    t = System.currentTimeMillis();
                }
                while (k <= this.maxIter && (t - t0) < deadline);
                break;
            case 3:
                do {
                    sMakespan = s.toSchedule().get().makespan();
                    k++;
                    System.out.println("makespan=" + sMakespan);
                    List<ResourceOrder> candidates = neighborhood.generateNeighbors(s);
                    System.out.println("neighbors:" + candidates);
                    int j = 0;
                    while (j< candidates.size() && sTabou.contains(candidates.get(j)) &&candidates.get(j).toSchedule().isEmpty()) {
                        j++;
                    }
                    ResourceOrder sPrime = candidates.get(j);

                    sPrimeMakespan = sPrime.toSchedule().get().makespan();
                    for (int i = j; i < candidates.size(); i++) {
                        //ResourceOrder ro = cndidate.generateNeigbor();
                        if (!(sTabou.contains(candidates.get(i))) && candidates.get(i).toSchedule().get().isValid() && candidates.get(i).toSchedule().get().makespan() < sPrimeMakespan) {
                            sPrime = candidates.get(i);
                            sPrimeMakespan = sPrime.toSchedule().get().makespan();
                        }
                    }
                    updatesTabou(sTabou,timeTabou);
                    //sTabou.add(sPrime);
                    timeTabou.add(timeTabouInit);
                    System.out.println("makespan:"+sPrimeMakespan);
                    if (sPrimeMakespan < sOptmakespan) {
                        sOpt = sPrime;
                        sOptmakespan = sPrimeMakespan;
                        s = sPrime;
                    }

                    t = System.currentTimeMillis();
                }
                while (k <= this.maxIter && (t - t0) < deadline);
                break;
        }
        return sOpt.toSchedule();
    }
    //fonction qui met à jour les timeTabou et supprime ceux ayant expirés
    public void updatesTabou(ArrayList<Nowicki.Swap> sTabou,  ArrayList<Integer> timeTabou){
        for (int i = 0; i< sTabou.size(); i++){
            timeTabou.set(i,timeTabou.get(i)-1);
            if(timeTabou.get(i) ==0){
                timeTabou.remove(i);
                sTabou.remove(i);
            }
        }
    }
    /*cette fonction retourne le swap avec le make span minimal parmi ceux qui ne sont pas interdis */
    public Nowicki.Swap getMinSwap(ResourceOrder father, ArrayList<Nowicki.Swap> swapListForbiden){
        Nowicki nowicki = new Nowicki();
        List<Nowicki.Swap> swapList = nowicki.allSwaps(father);
        int j = 0;

        Nowicki.Swap minSwap=swapList.get(j);
        ResourceOrder minResOrder=minSwap.generateFrom(father);
        while (j<swapList.size() && swapListForbiden.contains(swapList.get(j)) && minResOrder.toSchedule().isEmpty()) {
            j++;
        }
        minSwap=swapList.get(j);
        minResOrder=minSwap.generateFrom(father);
        for (int i = j; i < swapList.size(); i++) {
            ResourceOrder resOrderCorrespond=swapList.get(i).generateFrom(father);
            if (!(swapListForbiden.contains(swapList.get(i))) && resOrderCorrespond.toSchedule().get().isValid() && resOrderCorrespond.toSchedule().get().makespan() < minResOrder.toSchedule().get().makespan()) {
                minSwap = swapList.get(i);
                minResOrder=minSwap.generateFrom(father);
            }
        }
        return minSwap;
    }


}
