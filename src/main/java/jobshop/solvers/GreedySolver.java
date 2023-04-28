package jobshop.solvers;

import jobshop.Instance;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Schedule;
import jobshop.encodings.Task;

import java.util.*;

/** An empty shell to implement a greedy solver. */
public class GreedySolver implements Solver {

    /** All possible priorities for the greedy solver. */
    public enum Priority {
        SPT, LPT, SRPT, LRPT, EST_SPT, EST_LPT, EST_SRPT, EST_LRPT,
        EST_LPT_RANDOM,EST_LRPT_RANDOM
    }

    /** Priority that the solver should use. */
    final Priority priority;

    /** Creates a new greedy solver that will use the given priority. */
    public GreedySolver(Priority p) {
        this.priority = p;
    }

    @Override
    public Optional<Schedule> solve(Instance instance, long deadline) {

        //throw new UnsupportedOperationException();
        int numMachine = instance.numMachines;/*nombre de machines*/
        int numJob = instance.numJobs;
        int nbreChoixNonRandom=0;//c'est pour le pourcentage de choix par random
        Random rand=new Random();
        ArrayList<Integer> candidatesTime =  new ArrayList<>();
        ArrayList<Integer> candidatesMachine =  new ArrayList<>();
        ArrayList<Integer> jobs =  new ArrayList<>();
        ArrayList<Integer> task =  new ArrayList<>();
        ResourceOrder resourceOrder = new ResourceOrder(instance);
        for (int i = 0; i < numJob; i++){
            candidatesMachine.add(instance.machine(i,0));
            candidatesTime.add(instance.duration(i,0));
            jobs.add(i);
            task.add(0);
        }
        //pour les EST
        ArrayList<Integer> bestStartTimeJobs = new ArrayList<>();/*bestStartTimeJobs[i] correspond à la date de debut au plus tot(l'est) du job i*/
        ArrayList<Integer> task_done = new ArrayList<>();/*task_done[i] correspond au nombre de taches déja effectuées pour le job i*/
        ArrayList<Integer> machinesDates = new ArrayList<>();/*machinesDates[i] correspond à la date de début au plus tot de la machine i*/
        /*initialiser les dates au plus tot des jobs  ainsi le nombre de taches effectuées pour chaque job à 0*/
        for (int i = 0; i < numJob; i++) {
            bestStartTimeJobs.add(0);
            task_done.add(0);
        }
        /*initialiser les dates au plus tot des machines à 0*/
        for (int i = 0; i < numMachine; i++) {
            machinesDates.add(0);
        }

        switch (priority) {
            case SPT:
                while (!candidatesMachine.isEmpty()) {
                    int index = candidatesTime.indexOf(Collections.min(candidatesTime));
                    resourceOrder.addTaskToMachine(candidatesMachine.get(index), new Task(jobs.get(index), task.get(index)));
                    task.set(index, task.get(index) + 1);
                    if (task.get(index) == numMachine) {
                        candidatesMachine.remove(index);
                        candidatesTime.remove(index);
                        jobs.remove(index);
                        task.remove(index);
                    } else {
                        candidatesMachine.set(index, instance.machine(jobs.get(index), task.get(index)));
                        candidatesTime.set(index, instance.duration(jobs.get(index), task.get(index)));
                    }
                }
                break;
            case LPT:
                while (!candidatesMachine.isEmpty()) {
                    int index = candidatesTime.indexOf(Collections.max(candidatesTime));
                    resourceOrder.addTaskToMachine(candidatesMachine.get(index), new Task(jobs.get(index), task.get(index)));
                    task.set(index, task.get(index) + 1);
                    if (task.get(index) == numMachine) {
                        candidatesMachine.remove(index);
                        candidatesTime.remove(index);
                        jobs.remove(index);
                        task.remove(index);
                    } else {
                        candidatesMachine.set(index, instance.machine(jobs.get(index), task.get(index)));
                        candidatesTime.set(index, instance.duration(jobs.get(index), task.get(index)));
                    }
                }
                break;
            case LRPT:
                ArrayList<Integer> remainingTimes = new ArrayList<>();
                ArrayList<Integer> tasks = new ArrayList<>();
                for (int i = 0; i < numJob; i++) {
                    int sum = 0;
                    for (int j = 0; j < numMachine; j++) {
                        sum += instance.duration(i, j);
                    }
                    remainingTimes.add(sum);
                    tasks.add(0);
                }
                int maxRemainTime = Collections.max(remainingTimes);
                while (maxRemainTime > 0) {
                    int index = remainingTimes.indexOf(maxRemainTime);
                    resourceOrder.addTaskToMachine(instance.machine(index, tasks.get(index)), new Task(index, tasks.get(index)));
                    remainingTimes.set(index, maxRemainTime - instance.duration(index, tasks.get(index)));
                    tasks.set(index, tasks.get(index) + 1);
                    maxRemainTime = Collections.max(remainingTimes);
                }
                break;
            case EST_SPT:

                while (Collections.min(task_done) != numMachine) { /*pour s'arreter quand toutes les taches sont effectuées*/
                    System.out.println("best start time " + bestStartTimeJobs);
                    System.out.println("task done " + task_done);
                    System.out.println("machine date " + machinesDates);
                    int minStartTime = Collections.min(bestStartTimeJobs);
                    int index = bestStartTimeJobs.indexOf(minStartTime);/*le num du job qui a le min de best */
                    System.out.println("index "+index);
                    List<Integer> indexes = new ArrayList<>();
                    for (int i = 0; i < bestStartTimeJobs.size(); i++) {
                        if (bestStartTimeJobs.get(i).equals(minStartTime)) {
                            indexes.add(i);
                        }
                    }
                    if (indexes.size() == 1) {
                        Task task_t = new Task(index, task_done.get(index));
                        System.out.println("Tache choisie:"+task_t);
                        resourceOrder.addTaskToMachine(instance.machine(task_t), task_t);
                        task_done.set(index, task_done.get(index) + 1);

                        if(task_done.get(index) == numMachine){//si on effectue toutes les taches du job index, on met le best start time de ce job à l'infini pour ne plus le selectionner
                            int machineOfActualTask = instance.machine(task_t);
                            machinesDates.set(machineOfActualTask, bestStartTimeJobs.get(index) + instance.duration(task_t));
                            bestStartTimeJobs.set(index,Integer.MAX_VALUE);
                        }
                        else{
                            int machineOfNextTask = instance.machine(new Task(index, task_done.get(index)));   // machine sur laquelle va s'executer la prochaine tache du job selectionné
                            int machineOfActualTask = instance.machine(task_t);
                            machinesDates.set(machineOfActualTask, bestStartTimeJobs.get(index) + instance.duration(task_t));
                            //mettre à jour le bestStartTime du job choisi
                            bestStartTimeJobs.set(index, Integer.max(bestStartTimeJobs.get(index) + instance.duration(task_t), machinesDates.get(machineOfNextTask)));
                        }
                        //mettre à jour les bestStartTimes des autres jobs dont leur prochaine tache doit s'executer sur la machine de la tache qui vient d'étre choisi
                        for(int i=0;i<numJob;i++){

                            if(i!=index && task_done.get(i)!=numMachine){
                                int machineOfThisTask = instance.machine(new Task(i, task_done.get(i)));
                                bestStartTimeJobs.set(i, Integer.max(bestStartTimeJobs.get(i), machinesDates.get(machineOfThisTask)));
                            }
                        }
                    } else {
                        List<Integer> indexesTime = new ArrayList<>();
                        int minTime = Integer.MAX_VALUE;
                        int indexOfMinTime = -1;
                        for (int elt : indexes) {
                            int eltTime = instance.duration(new Task(elt, task_done.get(elt)));
                            if (eltTime < minTime) {
                                minTime = eltTime;
                                indexOfMinTime = elt;
                            }
                        }
                        System.out.println("index of min time:" + indexOfMinTime);
                        Task task_t = new Task(indexOfMinTime, task_done.get(indexOfMinTime));
                        System.out.println("Tache choisie:"+task_t);
                        resourceOrder.addTaskToMachine(instance.machine(task_t), task_t);
                        task_done.set(indexOfMinTime, task_done.get(indexOfMinTime) + 1);
                        if(task_done.get(indexOfMinTime) == numMachine){
                            int machineOfActualTask = instance.machine(task_t);
                            machinesDates.set(machineOfActualTask, bestStartTimeJobs.get(indexOfMinTime) + instance.duration(task_t));
                            bestStartTimeJobs.set(indexOfMinTime,Integer.MAX_VALUE);
                        }
                        else {
                            int machineOfNextTask = instance.machine(new Task(indexOfMinTime, task_done.get(indexOfMinTime)));   // machine sur laquelle va s'executer la prochaine tache du job selectionné
                            int machineOfActualTask = instance.machine(task_t);
                            machinesDates.set(machineOfActualTask, bestStartTimeJobs.get(indexOfMinTime) + instance.duration(task_t));
                            bestStartTimeJobs.set(indexOfMinTime, Integer.max(bestStartTimeJobs.get(indexOfMinTime) + instance.duration(task_t), machinesDates.get(machineOfNextTask)));
                        }
                        for(int i=0;i<numJob;i++){
                            if(i!=indexOfMinTime && task_done.get(i)!=numMachine){
                                int machineOfThisTask = instance.machine(new Task(i, task_done.get(i)));
                                bestStartTimeJobs.set(i, Integer.max(bestStartTimeJobs.get(i), machinesDates.get(machineOfThisTask)));
                            }
                        }
                    }

                }
                break;
            case EST_LRPT:

                while (Collections.min(task_done) != numMachine) {
                    System.out.println("best start time " + bestStartTimeJobs);
                    System.out.println("task done " + task_done);
                    System.out.println("machine date " + machinesDates);
                    int minStartTime = Collections.min(bestStartTimeJobs);
                    int index = bestStartTimeJobs.indexOf(minStartTime);
                    System.out.println("index "+index);
                    List<Integer> indexes = new ArrayList<>();
                    for (int i = 0; i < bestStartTimeJobs.size(); i++) {
                        if (bestStartTimeJobs.get(i).equals(minStartTime)) {
                            indexes.add(i);
                        }
                    }
                    if (indexes.size() == 1) {
                        Task task_t = new Task(index, task_done.get(index));
                        System.out.println("Tache choisie:"+task_t);
                        resourceOrder.addTaskToMachine(instance.machine(task_t), task_t);
                        task_done.set(index, task_done.get(index) + 1);
                        if(task_done.get(index) == numMachine){
                            //
                            int machineOfActualTask = instance.machine(task_t);
                            machinesDates.set(machineOfActualTask, bestStartTimeJobs.get(index) + instance.duration(task_t));
                            bestStartTimeJobs.set(index,Integer.MAX_VALUE);
                        }
                        else{
                            int machineOfNextTask = instance.machine(new Task(index, task_done.get(index)));   // machine sur laquelle va s'executer la prochaine tache du job selectionné
                            int machineOfActualTask = instance.machine(task_t);
                            machinesDates.set(machineOfActualTask, bestStartTimeJobs.get(index) + instance.duration(task_t));
                            bestStartTimeJobs.set(index, Integer.max(bestStartTimeJobs.get(index) + instance.duration(task_t), machinesDates.get(machineOfNextTask)));
                        }
                        for(int i=0;i<numJob;i++){

                            if(i!=index && task_done.get(i)!=numMachine){
                                int machineOfThisTask = instance.machine(new Task(i, task_done.get(i)));
                                bestStartTimeJobs.set(i, Integer.max(bestStartTimeJobs.get(i), machinesDates.get(machineOfThisTask)));
                            }
                        }
                    } else {
                        List<Integer> indexesTime = new ArrayList<>();
                        int maxTime = Integer.MIN_VALUE;
                        int indexOfMaxTime = -1;
                        for (int elt : indexes) {
                            int eltTime=0;
                            for(int j=task_done.get(elt);j<numMachine;j++) {
                                eltTime += instance.duration(new Task(elt, j));
                            }
                            if (eltTime > maxTime) {
                                maxTime = eltTime;
                                indexOfMaxTime = elt;
                            }
                        }
                        System.out.println("index of max time" + indexOfMaxTime);
                        Task task_t = new Task(indexOfMaxTime, task_done.get(indexOfMaxTime));
                        System.out.println("Tache choisie:"+task_t);
                        resourceOrder.addTaskToMachine(instance.machine(task_t), task_t);
                        task_done.set(indexOfMaxTime, task_done.get(indexOfMaxTime) + 1);
                        if(task_done.get(indexOfMaxTime) == numMachine){
                            //
                            int machineOfActualTask = instance.machine(task_t);
                            machinesDates.set(machineOfActualTask, bestStartTimeJobs.get(indexOfMaxTime) + instance.duration(task_t));
                            bestStartTimeJobs.set(indexOfMaxTime,Integer.MAX_VALUE);
                        }
                        else {
                            int machineOfNextTask = instance.machine(new Task(indexOfMaxTime, task_done.get(indexOfMaxTime)));   // machine sur laquelle va s'executer la prochaine tache du job selectionné
                            int machineOfActualTask = instance.machine(task_t);
                            machinesDates.set(machineOfActualTask, bestStartTimeJobs.get(indexOfMaxTime) + instance.duration(task_t));
                            bestStartTimeJobs.set(indexOfMaxTime, Integer.max(bestStartTimeJobs.get(indexOfMaxTime) + instance.duration(task_t), machinesDates.get(machineOfNextTask)));
                        }
                        for(int i=0;i<numJob;i++){

                            if(i!=indexOfMaxTime && task_done.get(i)!=numMachine){
                                int machineOfThisTask = instance.machine(new Task(i, task_done.get(i)));
                                bestStartTimeJobs.set(i, Integer.max(bestStartTimeJobs.get(i), machinesDates.get(machineOfThisTask)));
                            }
                        }
                    }

                }
                break;
            case EST_LPT_RANDOM:

                while (Collections.min(task_done) != numMachine) { /*pour s'arreter quand toutes les taches sont effectuées*/
                    System.out.println("best start time " + bestStartTimeJobs);
                    System.out.println("task done " + task_done);
                    System.out.println("machine date " + machinesDates);
                    int minStartTime = Collections.min(bestStartTimeJobs);
                    int index = bestStartTimeJobs.indexOf(minStartTime);/*le num du job qui a le min de best */
                    System.out.println("index "+index);
                    List<Integer> indexes = new ArrayList<>();
                    for (int i = 0; i < bestStartTimeJobs.size(); i++) {
                        if (bestStartTimeJobs.get(i).equals(minStartTime)) {
                            indexes.add(i);
                        }
                    }
                    if (indexes.size() == 1) {
                        Task task_t = new Task(index, task_done.get(index));
                        System.out.println("Tache choisie:"+task_t);
                        resourceOrder.addTaskToMachine(instance.machine(task_t), task_t);
                        task_done.set(index, task_done.get(index) + 1);

                        if(task_done.get(index) == numMachine){//si on effectue toutes les taches du job index, on met le best start time de ce job à l'infini pour ne plus le selectionner
                            int machineOfActualTask = instance.machine(task_t);
                            machinesDates.set(machineOfActualTask, bestStartTimeJobs.get(index) + instance.duration(task_t));
                            bestStartTimeJobs.set(index,Integer.MAX_VALUE);
                        }
                        else{
                            int machineOfNextTask = instance.machine(new Task(index, task_done.get(index)));   // machine sur laquelle va s'executer la prochaine tache du job selectionné
                            int machineOfActualTask = instance.machine(task_t);
                            machinesDates.set(machineOfActualTask, bestStartTimeJobs.get(index) + instance.duration(task_t));
                            //mettre à jour le bestStartTime du job choisi
                            bestStartTimeJobs.set(index, Integer.max(bestStartTimeJobs.get(index) + instance.duration(task_t), machinesDates.get(machineOfNextTask)));
                        }
                        //mettre à jour les bestStartTimes des autres jobs dont leur prochaine tache doit s'executer sur la machine de la tache qui vient d'étre choisi
                        for(int i=0;i<numJob;i++){

                            if(i!=index && task_done.get(i)!=numMachine){
                                int machineOfThisTask = instance.machine(new Task(i, task_done.get(i)));
                                bestStartTimeJobs.set(i, Integer.max(bestStartTimeJobs.get(i), machinesDates.get(machineOfThisTask)));
                            }
                        }
                    } else {
                        List<Integer> indexesTime = new ArrayList<>();
                        int minTime = Integer.MAX_VALUE;
                        int indexOfMinTime = -1;
                        //Pour faire 1 choix random sur 20(5% des cas)
                        if(nbreChoixNonRandom%20==0){
                            indexOfMinTime=indexes.get(rand.nextInt(indexes.size()));
                        }
                        else {
                            for (int elt : indexes) {
                                int eltTime = instance.duration(new Task(elt, task_done.get(elt)));
                                if (eltTime < minTime) {
                                    minTime = eltTime;
                                    indexOfMinTime = elt;
                                }
                            }
                            nbreChoixNonRandom++;
                        }
                        System.out.println("index of min time:" + indexOfMinTime);
                        Task task_t = new Task(indexOfMinTime, task_done.get(indexOfMinTime));
                        System.out.println("Tache choisie:"+task_t);
                        resourceOrder.addTaskToMachine(instance.machine(task_t), task_t);
                        task_done.set(indexOfMinTime, task_done.get(indexOfMinTime) + 1);
                        if(task_done.get(indexOfMinTime) == numMachine){
                            int machineOfActualTask = instance.machine(task_t);
                            machinesDates.set(machineOfActualTask, bestStartTimeJobs.get(indexOfMinTime) + instance.duration(task_t));
                            bestStartTimeJobs.set(indexOfMinTime,Integer.MAX_VALUE);
                        }
                        else {
                            int machineOfNextTask = instance.machine(new Task(indexOfMinTime, task_done.get(indexOfMinTime)));   // machine sur laquelle va s'executer la prochaine tache du job selectionné
                            int machineOfActualTask = instance.machine(task_t);
                            machinesDates.set(machineOfActualTask, bestStartTimeJobs.get(indexOfMinTime) + instance.duration(task_t));
                            bestStartTimeJobs.set(indexOfMinTime, Integer.max(bestStartTimeJobs.get(indexOfMinTime) + instance.duration(task_t), machinesDates.get(machineOfNextTask)));
                        }
                        for(int i=0;i<numJob;i++){
                            if(i!=indexOfMinTime && task_done.get(i)!=numMachine){
                                int machineOfThisTask = instance.machine(new Task(i, task_done.get(i)));
                                bestStartTimeJobs.set(i, Integer.max(bestStartTimeJobs.get(i), machinesDates.get(machineOfThisTask)));
                            }
                        }
                    }

                }
                break;
            case EST_LRPT_RANDOM:
                while (Collections.min(task_done) != numMachine) {
                    System.out.println("best start time " + bestStartTimeJobs);
                    System.out.println("task done " + task_done);
                    System.out.println("machine date " + machinesDates);
                    int minStartTime = Collections.min(bestStartTimeJobs);
                    int index = bestStartTimeJobs.indexOf(minStartTime);
                    System.out.println("index "+index);
                    List<Integer> indexes = new ArrayList<>();
                    for (int i = 0; i < bestStartTimeJobs.size(); i++) {
                        if (bestStartTimeJobs.get(i).equals(minStartTime)) {
                            indexes.add(i);
                        }
                    }
                    if (indexes.size() == 1) {
                        Task task_t = new Task(index, task_done.get(index));
                        System.out.println("Tache choisie:"+task_t);
                        resourceOrder.addTaskToMachine(instance.machine(task_t), task_t);
                        task_done.set(index, task_done.get(index) + 1);
                        if(task_done.get(index) == numMachine){
                            //
                            int machineOfActualTask = instance.machine(task_t);
                            machinesDates.set(machineOfActualTask, bestStartTimeJobs.get(index) + instance.duration(task_t));
                            bestStartTimeJobs.set(index,Integer.MAX_VALUE);
                        }
                        else{
                            int machineOfNextTask = instance.machine(new Task(index, task_done.get(index)));   // machine sur laquelle va s'executer la prochaine tache du job selectionné
                            int machineOfActualTask = instance.machine(task_t);
                            machinesDates.set(machineOfActualTask, bestStartTimeJobs.get(index) + instance.duration(task_t));
                            bestStartTimeJobs.set(index, Integer.max(bestStartTimeJobs.get(index) + instance.duration(task_t), machinesDates.get(machineOfNextTask)));
                        }
                        for(int i=0;i<numJob;i++){

                            if(i!=index && task_done.get(i)!=numMachine){
                                int machineOfThisTask = instance.machine(new Task(i, task_done.get(i)));
                                bestStartTimeJobs.set(i, Integer.max(bestStartTimeJobs.get(i), machinesDates.get(machineOfThisTask)));
                            }
                        }
                    } else {
                        List<Integer> indexesTime = new ArrayList<>();
                        int maxTime = Integer.MIN_VALUE;
                        int indexOfMaxTime = -1;
                        if(nbreChoixNonRandom%20==0){
                            indexOfMaxTime=indexes.get(rand.nextInt(indexes.size()));
                        }
                        else{
                            for (int elt : indexes) {
                                int eltTime=0;
                                for(int j=task_done.get(elt);j<numMachine;j++) {
                                    eltTime += instance.duration(new Task(elt, j));
                                }
                                if (eltTime > maxTime) {
                                    maxTime = eltTime;
                                    indexOfMaxTime = elt;
                                }
                            }
                            nbreChoixNonRandom++;

                        }

                        System.out.println("index of max time" + indexOfMaxTime);
                        Task task_t = new Task(indexOfMaxTime, task_done.get(indexOfMaxTime));
                        System.out.println("Tache choisie:"+task_t);
                        resourceOrder.addTaskToMachine(instance.machine(task_t), task_t);
                        task_done.set(indexOfMaxTime, task_done.get(indexOfMaxTime) + 1);
                        if(task_done.get(indexOfMaxTime) == numMachine){
                            //
                            int machineOfActualTask = instance.machine(task_t);
                            machinesDates.set(machineOfActualTask, bestStartTimeJobs.get(indexOfMaxTime) + instance.duration(task_t));
                            bestStartTimeJobs.set(indexOfMaxTime,Integer.MAX_VALUE);
                        }
                        else {
                            int machineOfNextTask = instance.machine(new Task(indexOfMaxTime, task_done.get(indexOfMaxTime)));   // machine sur laquelle va s'executer la prochaine tache du job selectionné
                            int machineOfActualTask = instance.machine(task_t);
                            machinesDates.set(machineOfActualTask, bestStartTimeJobs.get(indexOfMaxTime) + instance.duration(task_t));
                            bestStartTimeJobs.set(indexOfMaxTime, Integer.max(bestStartTimeJobs.get(indexOfMaxTime) + instance.duration(task_t), machinesDates.get(machineOfNextTask)));
                        }
                        for(int i=0;i<numJob;i++){

                            if(i!=indexOfMaxTime && task_done.get(i)!=numMachine){
                                int machineOfThisTask = instance.machine(new Task(i, task_done.get(i)));
                                bestStartTimeJobs.set(i, Integer.max(bestStartTimeJobs.get(i), machinesDates.get(machineOfThisTask)));
                            }
                        }
                    }

                }
                break;

        }

        return  resourceOrder.toSchedule();
    }
}

