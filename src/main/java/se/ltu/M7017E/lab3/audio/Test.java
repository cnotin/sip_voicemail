package se.ltu.M7017E.lab3.audio;

import org.gstreamer.Element;
import org.gstreamer.ElementFactory;
import org.gstreamer.Pipeline;

/**
 * GStreamer pipeline for the receiving part. Can manage several multicast (for
 * rooms) and one unicast channel at the same time.
 */
public class Test extends Pipeline {

	// private final Element sink = ElementFactory.make("autoaudiosink", null);

	private Pipeline me = this;

	// THE UnicastReceiver to talk with someone

	public Test() {

		Element audiotestsrc = ElementFactory.make("alsasrc", null);
		Element speexenc = ElementFactory.make("speexenc", null);
		Element rtpspeexpay = ElementFactory.make("rtpspeexpay", null);
		Element rtpspeexdepay = ElementFactory.make("rtpspeexdepay", null);
		Element rtpsink = ElementFactory.make("udpsink", "rtpsink");
		rtpsink.set("port", 5000);
		rtpsink.set("host", "130.240.53.166");
		rtpsink.set("auto-multicast", false);
		Element rtcpsink = ElementFactory.make("udpsink", "rtcpsink");
		rtcpsink.set("port", 5001);
		Element udpsrc = ElementFactory.make("udpsrc", null);
		udpsrc.set("port", 5000);

		Element speexdec = ElementFactory.make("speexdec", null);
		Element speexxenc = ElementFactory.make("speexenc", null);
		Element oggmux = ElementFactory.make("oggmux", null);
		Element filesink = ElementFactory.make("filesink", null);

		filesink.set("location", "lolwut.ogg");

		this.addMany(audiotestsrc, speexenc, rtpspeexpay, rtpsink, udpsrc,
				rtpspeexdepay, speexdec, speexxenc, oggmux, filesink);
		this.linkMany(audiotestsrc, speexenc, rtpspeexpay, rtpsink, udpsrc,
				rtpspeexdepay, speexdec, speexxenc, oggmux, filesink);
	}

	private static void successOrDie(String message, boolean result) {
		if (!result) {
			System.err.println("Die because of " + message);
			System.exit(-1);
		}
	}

}