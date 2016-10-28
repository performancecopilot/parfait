public class Sleep implements Runnable {
    public void run() {
        try {
            System.out.println("G'day World!");
            synchronized(this) {
                while (true) {
                    this.wait();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
