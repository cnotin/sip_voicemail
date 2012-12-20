package se.ltu.M7017E.lab3;

import lombok.Getter;

import org.gstreamer.Bus;
import org.gstreamer.Gst;
import org.gstreamer.GstObject;

import se.ltu.M7017E.lab3.audio.Receiver;
import se.ltu.M7017E.lab3.audio.Sender;
import se.ltu.M7017E.lab3.sip.MySipListener;

@Getter
public class App {
	public App(String ipAddr) {
		// ############ GSTREAMER STUFF ###############
		Gst.init("SIP Voicemail", new String[] { "--gst-debug-level=3",
				"--gst-debug-no-color" });

		new MySipListener(ipAddr, Config.LISTENING_PORT, this);
	}

	/**
	 * Answer the phone for a contact.
	 * 
	 * @param ip
	 *            contact's remote IP to send stream to
	 * @param port
	 *            contact's remote port to send stream to
	 */
	public void doAnswerPhone(String ip, int port) {
		final Sender sender = new Sender(ip, port);
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
	}
}
