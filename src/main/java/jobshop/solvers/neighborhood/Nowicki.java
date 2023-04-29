package jobshop.solvers.neighborhood;

import jobshop.Instance;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/** Implementation of the Nowicki and Smutnicki neighborhood.
 *
 * It works on the ResourceOrder encoding by generating two neighbors for each block
 * of the critical path.
 * For each block, two neighbors should be generated that respectively swap the first two and
 * last two tasks of the block.
 */
public class Nowicki extends Neighborhood {

    /** A block represents a subsequence of the critical path such that all tasks in it execute on the same machine.
     * This class identifies a block in a ResourceOrder representation.
     *
     * Consider the solution in ResourceOrder representation
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (0,2) (2,1) (1,1)
     * machine 2 : ...
     *
     * The block with : machine = 1, firstTask= 0 and lastTask = 1
     * Represent the task sequence : [(0,2) (2,1)]
     *
     * */
    public static class Block {
        /** machine on which the block is identified */
        public final int machine;
        /** index of the first task of the block */
        public final int firstTask;
        /** index of the last task of the block */
        public final int lastTask;

        /** Creates a new block. */
        Block(int machine, int firstTask, int lastTask) {
            this.machine = machine;
            this.firstTask = firstTask;
            this.lastTask = lastTask;
        }
        public String toString(){
            return "("+this.machine+","+this.firstTask+","+this.lastTask+")";
        }
        public int compareTo(Block a){
             int result = Integer.compare(this.machine,a.machine);
             if(result == 0){
                 result = Integer.compare(this.firstTask,a.firstTask);
             }
             if(result == 0){
                 result = Integer.compare(this.lastTask,a.lastTask);
             }
             return result;
        }
    }

    /**
     * Represents a swap of two tasks on the same machine in a ResourceOrder encoding.
     *
     * Consider the solution in ResourceOrder representation
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (0,2) (2,1) (1,1)
     * machine 2 : ...
     *
     * The swap with : machine = 1, t1= 0 and t2 = 1
     * Represent inversion of the two tasks : (0,2) and (2,1)
     * Applying this swap on the above resource order should result in the following one :
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (2,1) (0,2) (1,1)
     * machine 2 : ...
     */
    public static class Swap {
        /** machine on which to perform the swap */
        public final int machine;

        /** index of one task to be swapped (in the resource order encoding).
         * t1 should appear earlier than t2 in the resource order. */
        public final int t1;

        /** index of the other task to be swapped (in the resource order encoding) */
        public final int t2;

        /** Creates a new swap of two tasks. */
        Swap(int machine, int t1, int t2) {
            this.machine = machine;
            if (t1 < t2) {
                this.t1 = t1;
                this.t2 = t2;
            } else {
                this.t1 = t2;
                this.t2 = t1;
            }
        }


        /** Creates a new ResourceOrder order that is the result of performing the swap in the original ResourceOrder.
         *  The original ResourceOrder MUST NOT be modified by this operation.
         */
        public ResourceOrder generateFrom(ResourceOrder original) {
            ResourceOrder newResource=original.copy();
            newResource.swapTasks(machine,t1,t2);
            return newResource;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Swap swap = (Swap) o;
            return machine == swap.machine && t1 == swap.t1 && t2 == swap.t2;
        }

        @Override
        public int hashCode() {
            return Objects.hash(machine, t1, t2);
        }
    }


    @Override
    public List<ResourceOrder> generateNeighbors(ResourceOrder current) {
        // convert the list of swaps into a list of neighbors (function programming FTW)
        return allSwaps(current).stream().map(swap -> swap.generateFrom(current)).collect(Collectors.toList());

    }

    /** Generates all swaps of the given ResourceOrder.
     * This method can be used if one wants to access the inner fields of a neighbors. */
    public List<Swap> allSwaps(ResourceOrder current) {
        List<Swap> neighbors = new ArrayList<>();
        // iterate over all blocks of the critical path
        for(var block : blocksOfCriticalPath(current)) {
            // for this block, compute all neighbors and add them to the list of neighbors
            neighbors.addAll(neighbors(block));
        }
        return neighbors;
    }

    /** Returns a list of all the blocks of the critical path. */
    List<Block> blocksOfCriticalPath(ResourceOrder order) {
        List<Integer> machineVisited=new ArrayList<>();
        List<Task> path=order.toSchedule().get().criticalPath();
        System.out.println("critical path:"+path);
        List<Block> blocks=new ArrayList<>();
        for(Task task:path){
            int numMachine= order.instance.machine(task);
            int indexOfTask= order.getIndexOfTask(task);
            if (machineVisited.contains(numMachine)){
                int index=machineVisited.indexOf(numMachine);
                Block blockMachineVisited=blocks.get(index);
                if(blockMachineVisited.firstTask>indexOfTask){
                    Block newBlock=new Block(numMachine,indexOfTask,blockMachineVisited.lastTask);
                    blocks.set(index,newBlock);
                }
                else if(blockMachineVisited.lastTask<indexOfTask){
                    Block newBlock=new Block(numMachine,blockMachineVisited.firstTask,indexOfTask);
                    blocks.set(index,newBlock);
                }
            }
            else{
                machineVisited.add(numMachine);
                Block blockOfThisMachine=new Block(numMachine,indexOfTask,indexOfTask);
                blocks.add(blockOfThisMachine);
            }
        }
        return blocks;


    }


    /** For a given block, return the possible swaps for the Nowicki and Smutnicki neighborhood */
    List<Swap> neighbors(Block block) {
        int fistTask= block.firstTask;
        int lastTask= block.lastTask;
        List<Swap> swaps=new ArrayList<>();
        if(lastTask-fistTask<=1){
            swaps.add(new Swap(block.machine,fistTask,lastTask));
        }
        else{
            swaps.add(new Swap(block.machine,fistTask,fistTask+1));
            swaps.add(new Swap(block.machine,lastTask-1,lastTask));
        }
        return swaps;
    }

}
