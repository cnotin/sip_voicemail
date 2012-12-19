package se.ltu.M7017E.lab3.sip;

public class Test {

	public static void main(String args[]) {
		System.out.println("hello");
		new MySipListener("130.240.52.7", 5060);
		System.out.println("SIP init completed.");
	}
}
