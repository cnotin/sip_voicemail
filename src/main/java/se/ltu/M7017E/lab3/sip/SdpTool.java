package se.ltu.M7017E.lab3.sip;

import javax.sdp.Media;
import javax.sdp.MediaDescription;
import javax.sdp.SdpException;
import javax.sdp.SdpFactory;
import javax.sdp.SdpParseException;
import javax.sdp.SessionDescription;

/**
 * Collection of methods useful to deal with SDP messages.
 */
public class SdpTool {
	SdpFactory sdpFactory = SdpFactory.getInstance();

	public int getAudioMediaPort(SessionDescription sdp) {
		int port = -1;
		try {
			for (Object iter : sdp.getMediaDescriptions(false)) {
				Media media = ((MediaDescription) iter).getMedia();
				if (media.getMediaType().equals("audio")) {
					port = media.getMediaPort();
					break;
				}
			}
		} catch (SdpException e) {
			e.printStackTrace();
		}
		return port;
	}

	public String getIpAddress(SessionDescription sdp) {
		String ret = null;
		try {
			ret = sdp.getOrigin().getAddress();
		} catch (SdpParseException e) {
			e.printStackTrace();
		}
		return ret;
	}

	public SessionDescription fromString(String sdp) {
		SessionDescription ret = null;
		try {
			ret = sdpFactory.createSessionDescription(sdp);
		} catch (SdpParseException e) {
			e.printStackTrace();
		}
		return ret;
	}
}
