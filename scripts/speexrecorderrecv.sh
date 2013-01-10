#!/bin/sh

if [ $1 ]
then
	RECV_FROM_RTP_PORT=$1
else
	RECV_FROM_RTP_PORT=5000
fi;

echo "Receiving on port" ${RECV_FROM_RTP_PORT}
AUDIORATE=16000

gst-launch-0.10 -v --gst-debug=3 gstrtpbin name=rtpbin \
udpsrc caps="application/x-rtp, media=(string)audio, clock-rate=(int)${AUDIORATE}, encoding-name=(string)SPEEX, encoding-params=(string)1, payload=(int)96" port=${RECV_FROM_RTP_PORT} \
! rtpbin.recv_rtp_sink_1 \
rtpbin. ! rtpspeexdepay ! speexdec ! audioresample ! audioconvert ! speexenc ! oggmux ! filesink location=tmp.ogg
