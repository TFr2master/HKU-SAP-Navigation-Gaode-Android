package dupd.hku.com.hkusap.manager;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class IDPathFinderManagerTest {

    IDPathFinderManager manager;

    @Before
    public void setUp() throws Exception {
        manager = IDPathFinderManager.getInstance();
    }

    @Test
    public void compareLevel() {

        System.out.println(manager.compareLevel("098", "099"));
        System.out.println(manager.compareLevel("100", "099"));
        System.out.println(manager.compareLevel("101", "112"));
        System.out.println(manager.compareLevel("114", "099"));
        System.out.println(manager.compareLevel("098", "098"));
    }
}