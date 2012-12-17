package se.ltu.M7017E.lab3.sip;

import java.text.ParseException;
import java.util.Date;
import java.util.Properties;

import javax.sdp.SdpFactory;
import javax.sdp.SdpParseException;
import javax.sdp.SessionDescription;
import javax.sip.Dialog;
import javax.sip.DialogState;
import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.InvalidArgumentException;
import javax.sip.ListeningPoint;
import javax.sip.PeerUnavailableException;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.SipFactory;
import javax.sip.SipListener;
import javax.sip.SipProvider;
import javax.sip.SipStack;
import javax.sip.Transaction;
import javax.sip.TransactionState;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.header.ContactHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.ToHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

/**
 * This class is a UAS template.
 * 
 * @author M. Ranganathan
 */
public class MySipListener implements SipListener {

	private static AddressFactory addressFactory;

	private static MessageFactory messageFactory;

	private static HeaderFactory headerFactory;

	private static SdpFactory sdpFactory;

	private static SipStack sipStack;

	private static final String myAddress = "130.240.52.7";

	private static final int myPort = 5070;

	protected ServerTransaction inviteTid;

	private Request inviteRequest;

	private Dialog dialog;

	public void processRequest(RequestEvent requestEvent) {
		Request request = requestEvent.getRequest();
		ServerTransaction serverTransactionId = requestEvent
				.getServerTransaction();

		System.out.println("\n\nRequest " + request.getMethod()
				+ " with server transaction id " + serverTransactionId);

		if (request.getMethod().equals(Request.INVITE)) {
			processInvite(requestEvent, serverTransactionId);
		} else if (request.getMethod().equals(Request.ACK)) {
			processAck(requestEvent, serverTransactionId);
		} else if (request.getMethod().equals(Request.BYE)) {
			processBye(requestEvent, serverTransactionId);
		} else if (request.getMethod().equals(Request.CANCEL)) {
			processCancel(requestEvent, serverTransactionId);
		} else {
			try {
				serverTransactionId.sendResponse(messageFactory.createResponse(
						202, request));

				// send one back
				SipProvider prov = (SipProvider) requestEvent.getSource();
				Request refer = requestEvent.getDialog().createRequest("REFER");
				requestEvent.getDialog().sendRequest(
						prov.getNewClientTransaction(refer));

			} catch (SipException e) {
				e.printStackTrace();
			} catch (InvalidArgumentException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

	}

	public void processResponse(ResponseEvent responseEvent) {
		// this is a UAS (server, !=UAC), no response should be received
	}

	/**
	 * Process the ACK request.
	 */
	public void processAck(RequestEvent requestEvent,
			ServerTransaction serverTransaction) {
		System.out.println("got an ACK! ");
		System.out.println("Dialog State = " + dialog.getState());
	}

	/**
	 * Process the invite request.
	 */
	public void processInvite(RequestEvent requestEvent,
			ServerTransaction serverTransaction) {
		SipProvider sipProvider = (SipProvider) requestEvent.getSource();
		Request request = requestEvent.getRequest();

		SessionDescription clientSdp = null;
		try {
			clientSdp = sdpFactory.createSessionDescription(new String(request
					.getRawContent()));
			// TODO refuse with good http error msg
		} catch (SdpParseException e) {
			System.err.println("Content wasn't SDP or malformed SDP");
			e.printStackTrace();
		}
		try {
			System.out.println("got an Invite and SDP:\n" + clientSdp);

			ServerTransaction st = requestEvent.getServerTransaction();

			if (st == null) {
				st = sipProvider.getNewServerTransaction(request);
			}
			this.inviteTid = st;
			dialog = st.getDialog();

			try {
				if (inviteTid.getState() != TransactionState.COMPLETED) {
					System.out.println("shootme: Dialog state before 200: "
							+ inviteTid.getDialog().getState());

					// create sdp offer
					SessionDescription serverSdp = sdpFactory
							.createSessionDescription("v=0\n"// protocol version
									+ "o=Voicemail "
									+ new Date().getTime()
									+ " "
									+ new Date().getTime()
									+ " IN IP4 "
									+ myAddress
									+ "\n"
									+ "c= IN IP4 "
									+ myAddress
									+ "\n"
									+ // originator
									"s=Voicemail\n"
									+ // session name
									"t= 0 0\n"
									+ "m=audio 5000 RTP/AVP 97\n"
									+ "a=rtpmap:97 speex/16000\n"
									+ "a=fmtp:97 mode=\"10,any\"");

					Response response = messageFactory.createResponse(
							Response.OK, request);
					Address address = addressFactory
							.createAddress("Voicemail <sip:" + myAddress + ":"
									+ myPort + ">");
					ContactHeader contactHeader = headerFactory
							.createContactHeader(address);
					response.addHeader(contactHeader);
					ToHeader toHeader = (ToHeader) response
							.getHeader(ToHeader.NAME);
					toHeader.setTag("4321"); // Application is supposed to set.
					response.setContent(serverSdp.toString().getBytes(),
							headerFactory.createContentTypeHeader(
									"application", "sdp"));
					this.inviteRequest = request;

					st.sendResponse(response);

					System.out.println("shootme: Dialog state after 200: "
							+ inviteTid.getDialog().getState());
				}
			} catch (SipException ex) {
				ex.printStackTrace();
			} catch (InvalidArgumentException ex) {
				ex.printStackTrace();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);
		}
	}

	/**
	 * Process the bye request.
	 */
	public void processBye(RequestEvent requestEvent,
			ServerTransaction serverTransactionId) {
		SipProvider sipProvider = (SipProvider) requestEvent.getSource();
		Request request = requestEvent.getRequest();
		Dialog dialog = requestEvent.getDialog();
		System.out.println("local party = " + dialog.getLocalParty());
		try {
			System.out.println("shootme:  got a bye sending OK.");
			Response response = messageFactory.createResponse(200, request);
			serverTransactionId.sendResponse(response);
			System.out.println("Dialog State is "
					+ serverTransactionId.getDialog().getState());

		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);

		}
	}

	public void processCancel(RequestEvent requestEvent,
			ServerTransaction serverTransactionId) {
		SipProvider sipProvider = (SipProvider) requestEvent.getSource();
		Request request = requestEvent.getRequest();
		try {
			System.out.println("shootme:  got a cancel.");
			if (serverTransactionId == null) {
				System.out.println("shootme:  null tid.");
				return;
			}
			Response response = messageFactory.createResponse(200, request);
			serverTransactionId.sendResponse(response);
			if (dialog.getState() != DialogState.CONFIRMED) {
				response = messageFactory.createResponse(
						Response.REQUEST_TERMINATED, inviteRequest);
				inviteTid.sendResponse(response);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);

		}
	}

	public void processTimeout(javax.sip.TimeoutEvent timeoutEvent) {
		Transaction transaction;
		if (timeoutEvent.isServerTransaction()) {
			transaction = timeoutEvent.getServerTransaction();
		} else {
			transaction = timeoutEvent.getClientTransaction();
		}
		System.out.println("state = " + transaction.getState());
		System.out.println("dialog = " + transaction.getDialog());
		System.out.println("dialogState = "
				+ transaction.getDialog().getState());
		System.out.println("Transaction Time out");
	}

	public void processIOException(IOExceptionEvent exceptionEvent) {
		System.out.println("IOException");

	}

	public void init() {
		SipFactory sipFactory = null;
		sipStack = null;
		sipFactory = SipFactory.getInstance();
		sipFactory.setPathName("gov.nist");
		Properties properties = new Properties();
		properties.setProperty("javax.sip.STACK_NAME", "voicemail");
		// You need 16 for logging traces. 32 for debug + traces.
		// Your code will limp at 32 but it is best for debugging.
		properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");
		properties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
				"voicemaildebug.txt");
		properties.setProperty("gov.nist.javax.sip.SERVER_LOG",
				"voicemaillog.txt");

		try {
			// Create SipStack object
			sipStack = sipFactory.createSipStack(properties);
			System.out.println("sipStack = " + sipStack);
		} catch (PeerUnavailableException e) {
			// could not find
			// gov.nist.jain.protocol.ip.sip.SipStackImpl
			// in the classpath
			e.printStackTrace();
			System.err.println(e.getMessage());
			if (e.getCause() != null)
				e.getCause().printStackTrace();
			System.exit(0);
		}

		try {
			headerFactory = sipFactory.createHeaderFactory();
			addressFactory = sipFactory.createAddressFactory();
			messageFactory = sipFactory.createMessageFactory();
			sdpFactory = SdpFactory.getInstance();
			ListeningPoint lp = sipStack.createListeningPoint(myAddress,
					myPort, "udp");

			MySipListener listener = this;

			SipProvider sipProvider = sipStack.createSipProvider(lp);
			System.out.println("udp provider " + sipProvider);
			sipProvider.addSipListener(listener);

		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			ex.printStackTrace();
		}

	}

	public void processTransactionTerminated(
			TransactionTerminatedEvent transactionTerminatedEvent) {
		if (transactionTerminatedEvent.isServerTransaction())
			System.out.println("Transaction terminated event recieved"
					+ transactionTerminatedEvent.getServerTransaction());
		else
			System.out.println("Transaction terminated "
					+ transactionTerminatedEvent.getClientTransaction());

	}

	public void processDialogTerminated(
			DialogTerminatedEvent dialogTerminatedEvent) {
		System.out.println("Dialog terminated event recieved");
		Dialog d = dialogTerminatedEvent.getDialog();
		System.out.println("Local Party = " + d.getLocalParty());

	}

}
