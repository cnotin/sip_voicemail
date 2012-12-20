package se.ltu.M7017E.lab3.sip;

import java.text.ParseException;
import java.util.Date;
import java.util.Properties;

import javax.sdp.SessionDescription;
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

import se.ltu.M7017E.lab3.App;

/**
 * Main SIP listener class, inspired by an example found in the NIST
 * implementation of JAIN-SIP.
 */
public class MySipListener implements SipListener {
	private AddressFactory addressFactory;
	private MessageFactory messageFactory;
	private HeaderFactory headerFactory;
	private SdpTool sdpTool;

	private SipStack sipStack;

	private String myAddress;
	private int myPort;
	private App app;

	/**
	 * Create a SIP listener and launch the associated SIP stack.
	 * 
	 * @param myAddress
	 *            my IP address, will be sent to clients so it's better to use a
	 *            public and reachable address for them
	 * @param myPort
	 *            listening port for this SIP server, recommended for SIP is UDP
	 *            5060
	 * @param app
	 */
	public MySipListener(String myAddress, int myPort, App app) {
		this.myAddress = myAddress;
		this.myPort = myPort;
		this.app = app;

		System.out.println("Listening on " + myAddress + ":" + myPort);
		sipInit();
	}

	public void processRequest(RequestEvent requestEvent) {
		Request request = requestEvent.getRequest();
		ServerTransaction serverTransaction = requestEvent
				.getServerTransaction();

		if (serverTransaction == null) {
			System.out.println("Request " + request.getMethod()
					+ "\twith no server transaction yet");
		} else {
			System.out.println("Request " + request.getMethod()
					+ "\twith server transaction id "
					+ serverTransaction.getBranchId() + " and dialog id "
					+ serverTransaction.getDialog().getDialogId());
		}

		if (request.getMethod().equals(Request.INVITE)) {
			processInvite(requestEvent, serverTransaction);
		} else if (request.getMethod().equals(Request.ACK)) {
			processAck(requestEvent, serverTransaction);
		} else if (request.getMethod().equals(Request.BYE)) {
			processBye(requestEvent, serverTransaction);
		} else if (request.getMethod().equals(Request.CANCEL)) {
			processCancel(requestEvent, serverTransaction);
		} else {
			try {
				serverTransaction.sendResponse(messageFactory.createResponse(
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
	private void processAck(RequestEvent requestEvent,
			ServerTransaction serverTransaction) {
	}

	/**
	 * Process the invite request.
	 */
	private void processInvite(RequestEvent requestEvent,
			ServerTransaction serverTransaction) {
		SipProvider sipProvider = (SipProvider) requestEvent.getSource();
		Request request = requestEvent.getRequest();

		try {
			ServerTransaction st = requestEvent.getServerTransaction();

			if (st == null) {
				st = sipProvider.getNewServerTransaction(request);
			}

			try {
				if (st.getState() != TransactionState.COMPLETED) {
					// get info about client's SDP offer
					SessionDescription clientSdp = null;
					clientSdp = sdpTool.fromString(new String(request
							.getRawContent()));

					String clientAddr = sdpTool.getIpAddress(clientSdp);

					System.out.println("Client SDP medias "
							+ clientSdp.getMediaDescriptions(false).toString());

					int myPort = -1;

					int port = sdpTool.getAudioMediaPort(clientSdp);
					if (port != -1) {
						System.out.println("Client wants sound @ " + clientAddr
								+ ":" + port);
						myPort = app.doAnswerPhone(clientAddr, port);
					} else {
						System.err
								.println("Client didn't give any port for audio stream");
						// TODO cancel everything with appropriate message
					}

					// create my SDP offer
					SessionDescription serverSdp = sdpTool.fromString("v=0\n"// protocol
																				// version
							+ "o=Voicemail "
							+ new Date().getTime()
							+ " "
							+ new Date().getTime()
							+ " IN IP4 "
							+ myAddress
							+ "\n" + "c= IN IP4 "
							+ myAddress
							+ "\n"
							+ // originator
							"s=Voicemail\n"
							+ // session name
							"t=0 0\n" + "m=audio "
							+ myPort
							+ " RTP/AVP 96\n"
							+ "a=rtcp:"
							+ (myPort + 1)
							+ "\n"
							+ "a=rtpmap:96 speex/16000");
					// + "a=fmtp:100 mode=\"10,any\"");

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

					st.sendResponse(response);
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
	private void processBye(RequestEvent requestEvent,
			ServerTransaction serverTransactionId) {
		Request request = requestEvent.getRequest();
		try {
			Response response = messageFactory.createResponse(200, request);
			serverTransactionId.sendResponse(response);

		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);

		}
	}

	private void processCancel(RequestEvent requestEvent,
			ServerTransaction serverTransactionId) {
		Request request = requestEvent.getRequest();
		try {
			if (serverTransactionId == null) {
				System.err
						.println("Got a cancel for an unexisting transaction");
				return;
			}
			Response response = messageFactory.createResponse(200, request);
			serverTransactionId.sendResponse(response);
			if (requestEvent.getDialog().getState() != DialogState.CONFIRMED) {
				response = messageFactory.createResponse(
						Response.REQUEST_TERMINATED, request);
				requestEvent.getServerTransaction().sendResponse(response);
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
		System.out.println("Transaction timeout, state = "
				+ transaction.getState() + ", dialog = "
				+ transaction.getDialog() + ", dialogState = "
				+ transaction.getDialog().getState());
	}

	public void processIOException(IOExceptionEvent exceptionEvent) {
		System.err.println("IOException");
	}

	private void sipInit() {
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
			sdpTool = new SdpTool();
			ListeningPoint lp = sipStack.createListeningPoint(myAddress,
					myPort, "udp");

			MySipListener listener = this;

			SipProvider sipProvider = sipStack.createSipProvider(lp);
			sipProvider.addSipListener(listener);

		} catch (Exception ex) {
			System.err.println(ex.getMessage());
			ex.printStackTrace();
		}
		System.out.println("SIP init completed");
	}

	public void processTransactionTerminated(
			TransactionTerminatedEvent transactionTerminatedEvent) {
		if (transactionTerminatedEvent.isServerTransaction())
			System.out.println("Transaction terminated event received "
					+ transactionTerminatedEvent.getServerTransaction()
							.getBranchId());
		else
			System.out.println("Transaction terminated "
					+ transactionTerminatedEvent.getClientTransaction()
							.getBranchId());

	}

	public void processDialogTerminated(
			DialogTerminatedEvent dialogTerminatedEvent) {
		System.out.println("Dialog terminated event recieved");
	}
}
