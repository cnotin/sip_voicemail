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

	/**
	 * Retrieve first (if several) port advertised in SDP offer on which the
	 * client wants to receive audio media.
	 * 
	 * @param sdp
	 *            the SDP offer to search within
	 * @return the port, or -1 if not found
	 */
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

	/**
	 * Retrieve IP address from SDP offer.
	 * 
	 * @param sdp
	 *            the SDP offer to search within
	 * @return the IP address, as a string, or null if not found
	 */
	public String getIpAddress(SessionDescription sdp) {
		String ret = null;
		try {
			ret = sdp.getOrigin().getAddress();
		} catch (SdpParseException e) {
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * Create a SDP payload populated from a string. For format, please refer to
	 * {@link SdpFactory#createSessionDescription(String)}
	 * 
	 * @param sdp
	 *            SDP message as a String
	 * @return the SDP message populated, or null if a parsing problem happens
	 */
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
