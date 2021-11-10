import java.util.Scanner;

import com.offbynull.coroutines.user.Continuation;
import com.offbynull.coroutines.user.Coroutine;
import com.offbynull.coroutines.user.CoroutineRunner;

public class Test {
	public static void main(String[] args) throws InterruptedException {
		// client says "i'm doing this thing"
		// at this point our server checks the validity of the message
		// and decides if we should handle this event or if it's a spoofed event
		Scanner sc = new Scanner(System.in);
		TestRoutine routine = new TestRoutine();
		CoroutineRunner r = new CoroutineRunner(routine);
		
		// we will call execute at least once during the lifetime of this routine. each execute call represents 1 tick
		r.execute();
		
		(new Thread() {
			@Override
			public void run() {
				// hasNext will block, so off to another thread. shared mutable state tsktsk
				while (sc.hasNext()) {
					String next = sc.next();
					
					switch (next) {
						case "c":
							routine.responded = true;
							break;
						case "s":
							sc.close(); // tell the compiler to stop yelling at me
							System.exit(0);
							break;
					}
				}
			}
		}).start();
		
		while (true) {
			r.execute();
			Thread.sleep(2000); // let's extend the tick's time a bit to not spam the console lol
		}
	}
	
	public static final class TestRoutine implements Coroutine {
		public boolean responded = false;
		
		@Override
		public void run(Continuation c) throws Exception {
			System.out.println("Inside test routine, about to send message.");
			messagebox("test message to your boy", c);
			messagebox("thanks for responding", c);
			System.out.println("No more stuff to do..");
		}
		
		public void messagebox(String message, Continuation c) {
			System.out.println("sending: " + message);
			
			while (!responded) {
				System.out.println("No response yet...");
				c.suspend();
			}
			
			responded = false;
		}
	}
}
