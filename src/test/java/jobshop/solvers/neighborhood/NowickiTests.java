package jobshop.solvers.neighborhood;
import jobshop.Instance;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class NowickiTests {
    @Test
    public void blocksOfCriticalPathTest() throws IOException {
        Instance instance = Instance.fromFile(Paths.get("instances/aaa1"));
        ResourceOrder manualRO = new ResourceOrder(instance);
        //manualRO.addTaskToMachine(..., new Task(..., ...));
        manualRO.addTaskToMachine(0,new Task(0,0));
        manualRO.addTaskToMachine(0,new Task(1,1));
        manualRO.addTaskToMachine(1,new Task(1,0));
        manualRO.addTaskToMachine(1,new Task(0,1));
        manualRO.addTaskToMachine(2,new Task(1,2));
        manualRO.addTaskToMachine(2,new Task(0,2));
        Nowicki now=new Nowicki();
        List<Nowicki.Block> blocks1=now.blocksOfCriticalPath(manualRO);
        //liste de blocks qu'on devrait trouver
        Nowicki.Block block1=new Nowicki.Block(0,0,1);
        Nowicki.Block block2=new Nowicki.Block(2,0,1);
        assert blocks1.get(0).compareTo(block1)==0;
        assert blocks1.get(1).compareTo(block2)==0;




    }

}
