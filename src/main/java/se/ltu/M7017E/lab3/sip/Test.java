package se.ltu.M7017E.lab3.sip;

public class Test {

	public static void main(String args[]) {
		System.out.println("hello");
		new MySipListener().init();
		System.out.println("SIP init completed.");
	}
}
