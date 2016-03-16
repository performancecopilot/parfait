import java.util.Random;

public class AgentDemo {
	private Random random = new Random();
	
	public AgentDemo() {
	}

	public void doSleep() {
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
		}
	}

	private void doTask() {
		try {
			Thread.sleep(random.nextInt(1000));
		} catch (InterruptedException e) {
		}
	}

	public void doWork() {
		for (int i = 0 ; i < random.nextInt(10) ; i++) {
			doTask();
		}
	}

	public static void main(String[] args) {
		System.out.println("G'day world");
		AgentDemo test = new AgentDemo();
		while (true) {
			test.doWork();
			System.out.println("Sleep");
			test.doSleep();
			System.out.println("Awake");
		}
	}
}
