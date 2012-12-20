#!/bin/sh

RECV_FROM_RTP_PORT=5000
AUDIORATE=16000

gst-launch-0.10 -v --gst-debug=3 gstrtpbin name=rtpbin \
udpsrc caps="application/x-rtp, media=(string)audio, clock-rate=(int)${AUDIORATE}, encoding-name=(string)SPEEX, encoding-params=(string)1, payload=(int)110" port=${RECV_FROM_RTP_PORT} \
! rtpbin.recv_rtp_sink_1 \
rtpbin. ! rtpspeexdepay ! decodebin ! autoaudiosink
