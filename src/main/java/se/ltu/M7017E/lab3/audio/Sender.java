package se.ltu.M7017E.lab3.audio;

import org.gstreamer.Bin;
import org.gstreamer.Closure;
import org.gstreamer.Element;
import org.gstreamer.ElementFactory;
import org.gstreamer.Gst;
import org.gstreamer.Pad;
import org.gstreamer.Pipeline;
import org.gstreamer.elements.BaseSrc;

public class Sender {
	public static void play() {

		String[] params = { "--gst-debug=3", "--gst-debug-no-color" };
		Gst.init("Sender", params);

		Pipeline pipeline = new Pipeline("pipeline");

		BaseSrc source = (BaseSrc) ElementFactory.make("alsasrc", null);
		source.setLive(true);

		Element convert = ElementFactory.make("audioconvert", "convert");
		Element rtpPayload = ElementFactory.make("rtppcmupay", "rtpPayload");
		Element encoder = ElementFactory.make("mulawenc", "mulawenc");
		Bin rtpBin = (Bin) ElementFactory.make("gstrtpbin", "rtpbin");
		rtpBin.connect(new Element.PAD_ADDED() {
			@Override
			public void padAdded(Element element, Pad pad) {
				System.out.println("Pad added: " + pad);
			}
		});
		rtpBin.getRequestPad("send_rtp_sink_0");
		rtpBin.connect("on-ssrc-validated", new Closure() {
			@SuppressWarnings("unused")
			public void invoke() {
				System.out.println("lol");
			}
		});

		System.out.println("Caps (check out SSRC) "
				+ rtpBin.getElementByName("rtpsession0").getSinkPads().get(0)
						.getCaps());

		Element rtpsink = ElementFactory.make("udpsink", "rtpsink");
		rtpsink.set("port", 5000);
		rtpsink.set("host", "localhost");
		rtpsink.set("auto-multicast", false);
		Element rtcpsink = ElementFactory.make("udpsink", "rtcpsink");
		rtcpsink.set("port", 5001);

		pipeline.addMany(source, convert, rtpPayload, encoder, rtpBin, rtpsink,
				rtcpsink);
		System.out.println("link "
				+ Element.linkMany(source, convert, encoder, rtpPayload));

		System.out.println(Element.linkPads(rtpPayload, null, rtpBin,
				"send_rtp_sink_0"));
		System.out.println(Element.linkPads(rtpBin, "send_rtp_src_0", rtpsink,
				"sink"));

		System.out.println(Element.linkPads(rtpBin, "send_rtcp_src_0",
				rtcpsink, null));

		pipeline.play();

		System.out.println(rtpBin.getSrcPads().get(1).getCaps());

		System.out.println("Src caps = "
				+ source.getSrcPads().get(0).getNegotiatedCaps());

		Gst.main();
	}
}
