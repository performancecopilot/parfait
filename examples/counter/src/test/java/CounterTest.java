import static org.junit.Assert.assertNotNull;
import org.junit.Test;

public class CounterTest {
    @Test
    public void constructor() {
        Counter inc = new Counter();
        assertNotNull(inc);
    }
}
