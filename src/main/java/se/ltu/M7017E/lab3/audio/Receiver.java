package se.ltu.M7017E.lab3.audio;

import java.text.SimpleDateFormat;
import java.util.Date;

import lombok.Getter;

import org.gstreamer.Caps;
import org.gstreamer.Element;
import org.gstreamer.ElementFactory;
import org.gstreamer.Pad;
import org.gstreamer.PadLinkReturn;
import org.gstreamer.Pipeline;

import se.ltu.M7017E.lab3.Config;
import se.ltu.M7017E.lab3.tools.Tool;

public class Receiver extends Pipeline {
	/** UDP port that has been automatically assigned from available ones */
	@Getter
	private int port = 0;

	/**
	 * Save audio from udp into a a file
	 * 
	 * @param receiverName
	 * @param senderName
	 */
	public Receiver(String receiverName, String senderName) {
		super();

		// create a date
		SimpleDateFormat filenameFormatter = new SimpleDateFormat(
				"yyyy-MM-dd-HH-mm-ss");
		Date date = new Date();
		String stringDate = filenameFormatter.format(date);
		final Element rtpSource = ElementFactory.make("udpsrc", null);
		rtpSource.set("port", 0); // ask for a port
		Tool.successOrDie("caps",
				rtpSource.getStaticPad("src").setCaps(
						Caps.fromString("application/x-rtp,"
								+ "media=(string)audio,"
								+ "clock-rate=(int)16000,"
								+ "encoding-name=(string)SPEEX, "
								+ "encoding-params=(string)1, "
								+ "payload=(int)96")));

		final Element rtpBin = ElementFactory.make("gstrtpbin", null);
		final Element rtpDepay = ElementFactory.make("rtpspeexdepay", null);
		final Element speexdec = ElementFactory.make("speexdec", null);
		final Element audioresample = ElementFactory
				.make("audioresample", null);
		final Element audioconvert = ElementFactory.make("audioconvert", null);
		final Element speexenc = ElementFactory.make("speexenc", null);
		final Element oggmux = ElementFactory.make("oggmux", null);
		final Element filesink = ElementFactory.make("filesink", null);
		filesink.set("location", Config.MESSAGE_FILES_ROOT + receiverName + "/"
				+ senderName + "-" + stringDate + ".ogg");

		// ############## ADD THEM TO PIPELINE ####################
		addMany(rtpSource, rtpBin, rtpDepay, speexdec, audioresample,
				audioconvert, speexenc, oggmux, filesink);

		// ####################### CONNECT EVENTS ######################"
		rtpBin.connect(new Element.PAD_ADDED() {
			public void padAdded(Element element, Pad pad) {
				System.out.println("Pad added: " + pad);
				if (pad.getName().startsWith("recv_rtp_src")) {
					Tool.successOrDie(
							"rtpBin-depay",
							pad.link(rtpDepay.getStaticPad("sink")).equals(
									PadLinkReturn.OK));
				}
			}
		});

		// ###################### LINK THEM ##########################

		Pad pad = rtpBin.getRequestPad("recv_rtp_sink_0");

		Tool.successOrDie("udpSource-rtpbin", rtpSource.getStaticPad("src")
				.link(pad).equals(PadLinkReturn.OK));
		Tool.successOrDie(
				"rtpDepay-speexdec-audioresample-speexenc-oggmux-sink", Element
						.linkMany(rtpDepay, speexdec, audioresample,
								audioconvert, speexenc, oggmux, filesink));

		pause();
		port = (Integer) rtpSource.get("port");

		// final Element rtcpSource = ElementFactory.make("udpsrc", null);
		// rtcpSource.set("port", port + 1);
		// addMany(rtcpSource);
		// Tool.successOrDie(
		// "rtcpSource-rtpbin",
		// rtcpSource.getStaticPad("src")
		// .link(rtpBin.getRequestPad("recv_rtcp_sink_0"))
		// .equals(PadLinkReturn.OK));
	}

}
