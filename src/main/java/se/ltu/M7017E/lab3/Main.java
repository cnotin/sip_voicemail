package se.ltu.M7017E.lab3;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;

public class Main {
	public static void main(String[] args) {
		System.out.println("Welcome");

		String myIp = getMyIp();
		if (myIp == null) {
			System.err
					.println("Unable to find an IPv4 address for any interface");
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
	 * Get my IPv4 address for the first interface which name begins with one of
	 * the defined prefixes ("eth", "wlan", or ?)
	 * 
	 * @return the IP, eg "130.240.52.7", or null if not found
	 */
	private static String getMyIp() {
		List<NetworkInterface> interfaces = null;
		try {
			interfaces = Collections.list(NetworkInterface
					.getNetworkInterfaces());
		} catch (SocketException e) {
			System.err
					.println("Got a problem while trying to get my IP address, leaving");
			e.printStackTrace();
			System.exit(-1);
		}

		// try to find an interface with IPv4 for different prefixes
		String[] prefixes = { "eth", "wlan" };
		for (String prefix : prefixes) {
			for (NetworkInterface netint : interfaces) {
				if (netint.getName().startsWith(prefix)) {
					for (InetAddress addr : Collections.list(netint
							.getInetAddresses())) {
						if (addr instanceof Inet4Address) {
							return addr.getHostAddress();
						}
					}
				}
			}
		}

		return null;
	}
}
