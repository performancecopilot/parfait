
public class Acme {
    private ProductBuilder rockets = new ProductBuilder("Rockets");
    private ProductBuilder anvils = new ProductBuilder("Anvils");
    private ProductBuilder gbrs = new ProductBuilder("Giant_Rubber_Bands");

    Acme() {
        rockets.difficulty(4500);
        anvils.difficulty(150);
        gbrs.difficulty(25);
    }

    public void start() {
        rockets.start();
        anvils.start();
        gbrs.start();
        try {
            gbrs.join();	// blocks forever
        } catch (Exception e) {
            System.out.println("Shutdown");
        }
    }
}
