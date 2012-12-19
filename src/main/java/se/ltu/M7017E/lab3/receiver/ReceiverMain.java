package se.ltu.M7017E.lab3.receiver;

import org.gstreamer.Gst;

import se.ltu.M7017E.lab3.audio.Receiver;

public class ReceiverMain {

	public static void main(String[] args) {
		Gst.init("Receiver", new String[] { "--gst-debug-level=3",
				"--gst-debug=liveadder:2", "--gst-debug=basesrc:2",
				"--gst-debug-no-color" });
		Receiver rec = new Receiver("receiverCat", "senderCat");

		rec.play();
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("the game");

		while (true)
			;
	}
}
