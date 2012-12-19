package tools;

public class Tool {
	public static void successOrDie(String message, boolean result) {
		if (!result) {
			System.err.println("Die because of " + message);
			System.exit(-1);
		} else
			System.out.println(message + " ok");
	}
}
