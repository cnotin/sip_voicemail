package se.ltu.M7017.lab.audio.answerphone;

import org.gstreamer.Bus;
import org.gstreamer.Gst;
import org.gstreamer.GstObject;

import se.ltu.M7017E.lab3.audio.Receiver;
import se.ltu.M7017E.lab3.audio.Sender;

public class AnswerPhone {

	public void doAnswerPhone(String ip, int port) {
		Gst.init("Receiver", new String[] { "--gst-debug-level=3",
				"--gst-debug=liveadder:2", "--gst-debug=basesrc:2",
				"--gst-debug-no-color" });

		final Sender sender = new Sender(ip, port);
		System.out.println("1234");
		sender.getBus().connect(new Bus.EOS() {

			public void endOfStream(GstObject source) {
				System.out.println("travail terminééééééé");

				sender.stop();
				Receiver receiver = new Receiver("ReceiverCat", "SenderCat");
				receiver.play();
				// and then the user can talk for his message

			}
		});
		sender.play();
		System.out.println("12345");
		Gst.main();
	}
}
