import static org.junit.Assert.assertNotNull;
import org.junit.Test;

public class AcmeTest {
    @Test
    public void constructor() {
        Acme factory = new Acme();
        assertNotNull(factory);

        ProductBuilder product = new ProductBuilder("test");
        assertNotNull(product);
    }
}
