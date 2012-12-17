package se.ltu.M7017E.lab3.client;

import org.gstreamer.Gst;

import se.ltu.M7017E.lab3.audio.Sender;

/** Main class for the client. */
public class Main {

	public static void main(String[] args) {
		Gst.init("Receiver", new String[] { "--gst-debug-level=3",
				"--gst-debug=liveadder:2", "--gst-debug=basesrc:2",
				"--gst-debug-no-color" });
		Sender sender = new Sender();
		sender.play();
		System.out.println("test");
		while (true)
			;
	}
}
