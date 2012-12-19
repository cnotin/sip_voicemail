package se.ltu.M7017E.lab3;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;

public class Main {
	public static void main(String[] args) {
		System.out.println("Welcome");

		String myIp = getMyIp();
		if (myIp == null) {
			System.err
					.println("Unable to find my IPv4 address for any interface which names begins with 'eth'");
			System.exit(-1);
		}
		// launch App
		new App(myIp);

		System.out.println("Bye ?");
		new java.util.Scanner(System.in).nextLine();
		System.out.println("See you");
		System.exit(0);
	}

	/**
	 * Get my IPv4 address for the first interface which name begins with "eth"
	 * 
	 * @return the IP, eg "130.240.52.7", or null if not found
	 */
	private static String getMyIp() {
		Enumeration<NetworkInterface> interfaces = null;
		try {
			interfaces = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
			System.err
					.println("Got a problem while trying to get my IP address, leaving");
			e.printStackTrace();
			System.exit(-1);
		}
		for (NetworkInterface netint : Collections.list(interfaces)) {
			if (netint.getName().startsWith("eth")) {
				for (InetAddress addr : Collections.list(netint
						.getInetAddresses())) {
					if (addr instanceof Inet4Address) {
						return addr.getHostAddress();
					}
				}
			}
		}

		return null;
	}
}
