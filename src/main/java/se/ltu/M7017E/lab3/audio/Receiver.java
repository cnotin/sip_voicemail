package se.ltu.M7017E.lab3.audio;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.gstreamer.Caps;
import org.gstreamer.Element;
import org.gstreamer.ElementFactory;
import org.gstreamer.Pad;
import org.gstreamer.PadLinkReturn;
import org.gstreamer.Pipeline;

import se.ltu.M7017E.lab3.tools.Tool;

public class Receiver extends Pipeline {
	private Pipeline me = this;

	/**
	 * Save audio from udp into a a file
	 * 
	 * @param receiverName
	 * @param senderName
	 */
	public Receiver(String receiverName, String senderName) {
		// create a date
		SimpleDateFormat filenameFormatter = new SimpleDateFormat(
				"yyyy-MM-dd-HH-mm-ss");
		Date date = new Date();
		String stringDate = filenameFormatter.format(date);
		final Element udpSource = ElementFactory.make("udpsrc", null);
		udpSource.set("port", 0); // ask for a port
		Tool.successOrDie("caps",
				udpSource.getStaticPad("src").setCaps(
						Caps.fromString("application/x-rtp,"
								+ "media=(string)audio,"
								+ "clock-rate=(int)16000,"
								+ "encoding-name=(string)SPEEX, "
								+ "encoding-params=(string)1, "
								+ "payload=(int)110")));

		final Element rtpDepay = ElementFactory.make("rtpspeexdepay", null);
		Element rtpBin = ElementFactory.make("gstrtpbin", null);
		final Element filesink = ElementFactory.make("filesink", null);
		filesink.set("location", receiverName + "/" + senderName + "-"
				+ stringDate + ".ogg");
		final Element oggmux = ElementFactory.make("oggmux", null);
		final Element speexdec = ElementFactory.make("speexdec", null);
		final Element speexenc = ElementFactory.make("speexenc", null);

		// ############## ADD THEM TO PIPELINE ####################
		addMany(udpSource, rtpBin, speexdec, speexenc, oggmux, filesink);

		// ####################### CONNECT EVENTS ######################"
		rtpBin.connect(new Element.PAD_ADDED() {
			public void padAdded(Element element, Pad pad) {
				System.out.println("Pad added: " + pad);
				if (pad.getName().startsWith("recv_rtp_src")) {

					System.out.println("\nGot new input pad: " + pad);
					me.add(rtpDepay);
					rtpDepay.syncStateWithParent();

					Tool.successOrDie("rtpDepay-speexdec",
							Element.linkMany(rtpDepay, speexdec));
					Tool.successOrDie("speexdec-speexenc",
							Element.linkMany(speexdec, speexenc));
					Tool.successOrDie("speexenc-oggmux",
							Element.linkMany(speexenc, oggmux));
					Tool.successOrDie(
							"bin-decoder",
							pad.link(rtpDepay.getStaticPad("sink")).equals(
									PadLinkReturn.OK));
				}
			}
		});

		// ###################### LINK THEM ##########################

		Pad pad = rtpBin.getRequestPad("recv_rtp_sink_0");

		Tool.successOrDie("oggmux-sink", Element.linkMany(oggmux, filesink));
		Tool.successOrDie("udpSource-rtpbin", udpSource.getStaticPad("src")
				.link(pad).equals(PadLinkReturn.OK));

	}
}
