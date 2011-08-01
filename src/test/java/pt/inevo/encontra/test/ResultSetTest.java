package pt.inevo.encontra.test;

import junit.framework.TestCase;
import org.junit.Test;
import pt.inevo.encontra.common.Result;
import pt.inevo.encontra.common.ResultSet;
import pt.inevo.encontra.common.ResultSetDefaultImpl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Tests some of the ResultSet common operations.
 */
public class ResultSetTest extends TestCase {

    private ResultSet<String> resultSet;

    public ResultSetTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        resultSet = new ResultSetDefaultImpl<String>();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

     @Test
     public void testAdd() {
         boolean result = resultSet.add(new Result<String>("TestAdd"));
         assertTrue(result);
         assertTrue(resultSet.getSize() == 1);
     }

    @Test
    public void testAddAll() {
        List<Result<String>> results = new ArrayList<Result<String>>();
        for (int i = 0 ; i < 10 ; i++) {
            results.add(new Result<String>("TestAddAll" + i));
        }

        boolean result = resultSet.addAll(results);
        assertTrue(result);
        assertTrue(resultSet.getSize() == 10);
    }

    @Test
    public void testClear() {
        resultSet.add(new Result<String>("TestClear"));

        assertTrue(resultSet.getSize() != 0);
        resultSet.clear();
        assertTrue(resultSet.getSize() == 0);
    }

    @Test
    public void testContains() {
        Result<String> r = new Result<String>("TestContains");
        resultSet.add(r);
        assertTrue(resultSet.contains(r));
    }

    @Test
    public void testNoContains() {
        Result<String> r = new Result<String>("TestContains");
        assertFalse(resultSet.contains(r));
    }

    @Test
    public void testContainsResultObject() {
        String object = "Object";

        Result<String> r = new Result<String>(object);
        resultSet.add(r);

        assertTrue(resultSet.containsResultObject(object));
        assertFalse(resultSet.containsResultObject("Not in the result set!"));
    }

    @Test
    public void testGetScore() {
        String object = "Object";
        Result<String> r = new Result<String>(object);
        r.setScore(2);
        resultSet.add(r);

        assertEquals(resultSet.getScore(object), 2.0);
    }

    @Test
    public void testRemove() {
        Result<String> r = new Result<String>("TestRemove");
        resultSet.add(r);

        boolean result = resultSet.remove(r);
        assertTrue(result);
        assertTrue(resultSet.getSize() == 0);
    }

    @Test
    public void testGetFirstResults() {
       List<Result<String>> results = new ArrayList<Result<String>>();
        for (int i = 0 ; i < 10 ; i++) {
            Result<String> r = new Result<String>("TestAddAll" + i);
            r.setScore(i);
            results.add(r);
        }

        resultSet.addAll(results);
        resultSet.normalizeScores();
        resultSet.invertScores();

        ResultSet<String> subResultSet = resultSet.getFirstResults(5);

        assertEquals(subResultSet.getSize(), 5);
        Iterator<Result<String>> it = subResultSet.iterator();
        for (int i = 9 ; i >= 5 ; i--) {
            assertEquals(it.next(), results.get(i));
        }
    }
}
