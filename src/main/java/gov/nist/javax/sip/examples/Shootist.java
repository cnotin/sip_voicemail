package gov.nist.javax.sip.examples;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Properties;

import javax.sip.ClientTransaction;
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
import javax.sip.TransactionTerminatedEvent;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.Header;
import javax.sip.header.HeaderFactory;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

/**
 * This class is a UAC template.
 * 
 * @author M. Ranganathan
 */
public class Shootist implements SipListener {

	private static SipProvider sipProvider;

	private static AddressFactory addressFactory;

	private static MessageFactory messageFactory;

	private static HeaderFactory headerFactory;

	private static SipStack sipStack;

	private ContactHeader contactHeader;

	private ListeningPoint tcpListeningPoint;

	private ClientTransaction inviteTid;

	private Dialog dialog;

	private boolean byeTaskRunning;

	private static final String usageString = "java "
			+ "examples.shootist.Shootist \n"
			+ ">>>> is your class path set to the root?";

	private static void usage() {
		System.out.println(usageString);
		System.exit(0);

	}

	public void processRequest(RequestEvent requestReceivedEvent) {
		Request request = requestReceivedEvent.getRequest();
		ServerTransaction serverTransactionId = requestReceivedEvent
				.getServerTransaction();

		System.out.println("\n\nRequest " + request.getMethod()
				+ " received at " + sipStack.getStackName()
				+ " with server transaction id " + serverTransactionId);

		// We are the UAC so the only request we get is the BYE.
		if (request.getMethod().equals(Request.BYE))
			processBye(request, serverTransactionId);
		else {
			try {
				serverTransactionId.sendResponse(messageFactory.createResponse(
						202, request));
			} catch (SipException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public void processBye(Request request,
			ServerTransaction serverTransactionId) {
		try {
			System.out.println("shootist:  got a bye .");
			if (serverTransactionId == null) {
				System.out.println("shootist:  null TID.");
				return;
			}
			Dialog dialog = serverTransactionId.getDialog();
			System.out.println("Dialog State = " + dialog.getState());
			Response response = messageFactory.createResponse(200, request);
			serverTransactionId.sendResponse(response);
			System.out.println("shootist:  Sending OK.");
			System.out.println("Dialog State = " + dialog.getState());

		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);

		}
	}

	// Save the created ACK request, to respond to retransmitted 2xx
	private Request ackRequest;

	public void processResponse(ResponseEvent responseReceivedEvent) {
		System.out.println("Got a response");
		Response response = (Response) responseReceivedEvent.getResponse();
		ClientTransaction tid = responseReceivedEvent.getClientTransaction();
		CSeqHeader cseq = (CSeqHeader) response.getHeader(CSeqHeader.NAME);

		System.out.println("Response received : Status Code = "
				+ response.getStatusCode() + " " + cseq);

		if (tid == null) {

			// RFC3261: MUST respond to every 2xx
			if (ackRequest != null && dialog != null) {
				System.out.println("re-sending ACK");
				try {
					dialog.sendAck(ackRequest);
				} catch (SipException se) {
					se.printStackTrace();
				}
			}
			return;
		}

		System.out.println("transaction state is " + tid.getState());
		System.out.println("Dialog = " + tid.getDialog());
		System.out.println("Dialog State is " + tid.getDialog().getState());

		try {
			if (response.getStatusCode() == Response.OK) {
				if (cseq.getMethod().equals(Request.INVITE)) {
					System.out.println("Dialog after 200 OK  " + dialog);
					System.out.println("Dialog State after 200 OK  "
							+ dialog.getState());
					ackRequest = dialog.createAck(((CSeqHeader) response
							.getHeader(CSeqHeader.NAME)).getSeqNumber());
					System.out.println("Sending ACK");
					dialog.sendAck(ackRequest);

					// JvB: test REFER, reported bug in tag handling
					// dialog.sendRequest( sipProvider.getNewClientTransaction(
					// dialog.createRequest("REFER") ));

				} else if (cseq.getMethod().equals(Request.CANCEL)) {
					if (dialog.getState() == DialogState.CONFIRMED) {
						// oops cancel went in too late. Need to hang up the
						// dialog.
						System.out
								.println("Sending BYE -- cancel went in too late !!");
						Request byeRequest = dialog.createRequest(Request.BYE);
						ClientTransaction ct = sipProvider
								.getNewClientTransaction(byeRequest);
						dialog.sendRequest(ct);

					}

				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);
		}

	}

	public void processTimeout(javax.sip.TimeoutEvent timeoutEvent) {

		System.out.println("Transaction Time out");
	}

	public void sendCancel() {
		try {
			System.out.println("Sending cancel");
			Request cancelRequest = inviteTid.createCancel();
			ClientTransaction cancelTid = sipProvider
					.getNewClientTransaction(cancelRequest);
			cancelTid.sendRequest();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void init() {
		SipFactory sipFactory = null;
		sipStack = null;
		sipFactory = SipFactory.getInstance();
		sipFactory.setPathName("gov.nist");
		Properties properties = new Properties();
		// If you want to try TCP transport change the following to
		String transport = "tcp";
		String peerHostPort = "127.0.0.1:5070";
		// String peerHostPort = "230.0.0.1:5070";
		properties.setProperty("javax.sip.OUTBOUND_PROXY", peerHostPort + "/"
				+ transport);
		// If you want to use UDP then uncomment this.
		properties.setProperty("javax.sip.STACK_NAME", "shootist");

		// The following properties are specific to nist-sip
		// and are not necessarily part of any other jain-sip
		// implementation.
		// You can set a max message size for tcp transport to
		// guard against denial of service attack.
		properties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
				"shootistdebug.txt");
		properties.setProperty("gov.nist.javax.sip.SERVER_LOG",
				"shootistlog.txt");

		// Drop the client connection after we are done with the transaction.
		properties.setProperty("gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS",
				"false");
		// Set to 0 (or NONE) in your production code for max speed.
		// You need 16 (or TRACE) for logging traces. 32 (or DEBUG) for debug +
		// traces.
		// Your code will limp at 32 but it is best for debugging.
		properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "DEBUG");

		try {
			// Create SipStack object
			sipStack = sipFactory.createSipStack(properties);
			System.out.println("createSipStack " + sipStack);
		} catch (PeerUnavailableException e) {
			// could not find
			// gov.nist.jain.protocol.ip.sip.SipStackImpl
			// in the classpath
			e.printStackTrace();
			System.err.println(e.getMessage());
			System.exit(0);
		}

		try {
			headerFactory = sipFactory.createHeaderFactory();
			addressFactory = sipFactory.createAddressFactory();
			messageFactory = sipFactory.createMessageFactory();
			tcpListeningPoint = sipStack.createListeningPoint("127.0.0.1",
					5060, "tcp");
			System.out.println("listeningPoint = " + tcpListeningPoint);
			sipProvider = sipStack.createSipProvider(tcpListeningPoint);
			System.out.println("SipProvider = " + sipProvider);
			Shootist listener = this;
			sipProvider.addSipListener(listener);

			String fromName = "BigGuy";
			String fromSipAddress = "here.com";
			String fromDisplayName = "The Master Blaster";

			String toSipAddress = "there.com";
			String toUser = "LittleGuy";
			String toDisplayName = "The Little Blister";

			// create >From Header
			SipURI fromAddress = addressFactory.createSipURI(fromName,
					fromSipAddress);

			Address fromNameAddress = addressFactory.createAddress(fromAddress);
			fromNameAddress.setDisplayName(fromDisplayName);
			FromHeader fromHeader = headerFactory.createFromHeader(
					fromNameAddress, "12345");

			// create To Header
			SipURI toAddress = addressFactory
					.createSipURI(toUser, toSipAddress);
			Address toNameAddress = addressFactory.createAddress(toAddress);
			toNameAddress.setDisplayName(toDisplayName);
			ToHeader toHeader = headerFactory.createToHeader(toNameAddress,
					null);

			// create Request URI
			SipURI requestURI = addressFactory.createSipURI(toUser,
					peerHostPort);

			// Create ViaHeaders

			ArrayList<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
			String ipAddress = tcpListeningPoint.getIPAddress();
			ViaHeader viaHeader = headerFactory.createViaHeader(ipAddress,
					sipProvider.getListeningPoint(transport).getPort(),
					transport, null);

			// add via headers
			viaHeaders.add(viaHeader);

			// Create ContentTypeHeader
			ContentTypeHeader contentTypeHeader = headerFactory
					.createContentTypeHeader("application", "sdp");

			// Create a new CallId header
			CallIdHeader callIdHeader = sipProvider.getNewCallId();

			// Create a new Cseq header
			CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(1L,
					Request.INVITE);

			// Create a new MaxForwardsHeader
			MaxForwardsHeader maxForwards = headerFactory
					.createMaxForwardsHeader(70);

			// Create the request.
			Request request = messageFactory.createRequest(requestURI,
					Request.INVITE, callIdHeader, cSeqHeader, fromHeader,
					toHeader, viaHeaders, maxForwards);
			// Create contact headers
			String host = "127.0.0.1";

			SipURI contactUrl = addressFactory.createSipURI(fromName, host);
			contactUrl.setPort(tcpListeningPoint.getPort());
			contactUrl.setLrParam();

			// Create the contact name address.
			SipURI contactURI = addressFactory.createSipURI(fromName, host);
			contactURI.setPort(sipProvider.getListeningPoint(transport)
					.getPort());

			Address contactAddress = addressFactory.createAddress(contactURI);

			// Add the contact address.
			contactAddress.setDisplayName(fromName);

			contactHeader = headerFactory.createContactHeader(contactAddress);
			request.addHeader(contactHeader);

			// You can add extension headers of your own making
			// to the outgoing SIP request.
			// Add the extension header.
			Header extensionHeader = headerFactory.createHeader("My-Header",
					"my header value");
			request.addHeader(extensionHeader);

			String sdpData = "v=0\r\n"
					+ "o=4855 13760799956958020 13760799956958020"
					+ " IN IP4  129.6.55.78\r\n" + "s=mysession session\r\n"
					+ "p=+46 8 52018010\r\n" + "c=IN IP4  129.6.55.78\r\n"
					+ "t=0 0\r\n" + "m=audio 6022 RTP/AVP 0 4 18\r\n"
					+ "a=rtpmap:0 PCMU/8000\r\n" + "a=rtpmap:4 G723/8000\r\n"
					+ "a=rtpmap:18 G729A/8000\r\n" + "a=ptime:20\r\n";
			byte[] contents = sdpData.getBytes();

			request.setContent(contents, contentTypeHeader);
			// You can add as many extension headers as you
			// want.

			extensionHeader = headerFactory.createHeader("My-Other-Header",
					"my new header value ");
			request.addHeader(extensionHeader);

			Header callInfoHeader = headerFactory.createHeader("Call-Info",
					"<http://www.antd.nist.gov>");
			request.addHeader(callInfoHeader);

			// Create the client transaction.
			inviteTid = sipProvider.getNewClientTransaction(request);

			System.out.println("inviteTid = " + inviteTid);

			// send the request out.

			inviteTid.sendRequest();

			dialog = inviteTid.getDialog();

		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			ex.printStackTrace();
			usage();
		}
	}

	public static void main(String args[]) {
		Shootist shootist = new Shootist();
		shootist.init();
		System.out.println("Bye ?");
		new java.util.Scanner(System.in).nextLine();
		try {
			Request byeRequest = shootist.dialog.createRequest(Request.BYE);
			ClientTransaction ct = sipProvider
					.getNewClientTransaction(byeRequest);
			shootist.dialog.sendRequest(ct);
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);
		}
		System.out.println("Sent bye.");
	}

	public void processIOException(IOExceptionEvent exceptionEvent) {
		System.out.println("IOException happened for "
				+ exceptionEvent.getHost() + " port = "
				+ exceptionEvent.getPort());

	}

	public void processTransactionTerminated(
			TransactionTerminatedEvent transactionTerminatedEvent) {
		System.out.println("Transaction terminated event recieved");
	}

	public void processDialogTerminated(
			DialogTerminatedEvent dialogTerminatedEvent) {
		System.out.println("dialogTerminatedEvent");

	}
}
