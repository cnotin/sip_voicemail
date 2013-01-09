package se.ltu.M7017E.lab3.audio;

import org.gstreamer.Caps;
import org.gstreamer.Element;
import org.gstreamer.ElementFactory;
import org.gstreamer.Pad;
import org.gstreamer.PadLinkReturn;
import org.gstreamer.Pipeline;
import org.gstreamer.elements.good.RTPBin;

import se.ltu.M7017E.lab3.tools.Tool;

public class Sender extends Pipeline {

	public Sender(String ip, int port) {
		super();

		Element src = ElementFactory.make("filesrc", null);
		src.set("location", "answerphone.ogg");
		Element decodebin = ElementFactory.make("decodebin", null);
		final Element audioconvert = ElementFactory.make("audioconvert", null);
		final Element audioresample = ElementFactory
				.make("audioresample", null);
		Element capsRateFilter = ElementFactory.make("capsfilter", null);
		capsRateFilter.setCaps(Caps.fromString("audio/x-raw-int,rate=16000"));
		final Element encoder = ElementFactory.make("speexenc", null);
		encoder.set("quality", 6); // quality in [0,10]
		encoder.set("vad", true); // voice activity detection
		encoder.set("dtx", true); // discontinuous transmission
		Element rtpPay = ElementFactory.make("rtpspeexpay", null);
		Element capsRtpFilter = ElementFactory.make("capsfilter", null);
		capsRtpFilter.setCaps(Caps.fromString("application/x-rtp,"
				+ "payload=(int)96"));
		RTPBin rtpBin = new RTPBin((String) null);

		// asking this put the gstrtpbin plugin in sender mode
		Pad rtpSink0 = rtpBin.getRequestPad("send_rtp_sink_0");

		Element udpSink = ElementFactory.make("udpsink", null);
		udpSink.set("host", ip);
		udpSink.set("port", port);
		udpSink.set("async", false);

		// ############## ADD THEM TO PIPELINE ####################
		addMany(src, decodebin, audioconvert, audioresample, capsRateFilter,
				encoder, rtpPay, capsRtpFilter, rtpBin, udpSink);

		// ####################### CONNECT EVENT ######################"
		decodebin.connect(new Element.PAD_ADDED() {
			public void padAdded(Element element, Pad pad) {
				System.out.println("\nGot new input pad: " + pad);
				Tool.successOrDie(
						"decodebin-audioconvert",
						pad.link(audioconvert.getStaticPad("sink")).equals(
								PadLinkReturn.OK));
			}
		});

		// ###################### LINK THEM ##########################
		Tool.successOrDie("src,decodebin", linkMany(src, decodebin));

		Tool.successOrDie(
				"audioconvert,audioresample,capsRateFilter,encoder,rtppay,capsFilter",
				linkMany(audioconvert, audioresample, capsRateFilter, encoder,
						rtpPay, capsRtpFilter));
		Tool.successOrDie("capsfilter-rtpbin", capsRtpFilter
				.getStaticPad("src").link(rtpSink0).equals(PadLinkReturn.OK));
		Tool.successOrDie(
				"rtpbin-udpSink",
				rtpBin.getStaticPad("send_rtp_src_0")
						.link(udpSink.getStaticPad("sink"))
						.equals(PadLinkReturn.OK));
	}

}
