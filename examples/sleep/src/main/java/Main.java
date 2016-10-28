public class Main {
    public static void main (String args[]) {
        Thread t = new Thread(new Sleep());
        t.start();
    }
}
