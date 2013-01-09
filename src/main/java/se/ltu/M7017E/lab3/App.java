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
	 * @param callee
	 *            SIP name of the person who was called
	 * @param caller
	 *            SIP name of the person who called
	 * @return automatically attributed port for incoming stream
	 */
	public int doAnswerPhone(String ip, int port, String callee, String caller) {
		final Receiver receiver = new Receiver(callee, caller);

		final Sender sender = new Sender(ip, port);
		sender.getBus().connect(new Bus.EOS() {
			public void endOfStream(GstObject source) {
				sender.stop();
				receiver.play();
				// and then the user can talk for his message
			}
		});
		sender.play();

		return receiver.getPort();
	}
}
