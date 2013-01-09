#!/bin/sh

if [ $1 ]
then
	SEND_TO_ADDR=$1
else
	echo "Gimme an IP to send to"
	exit
fi;

if [ $2 ]
then
        SEND_TO_RTP_PORT=$2
	SEND_TO_RTCP_PORT=$(($2 + 1))
else
        echo "Gimme a port to send to"
        exit
fi;

ENCODER=speexenc
PAYLOADER=rtpspeexpay

SPEEX_PARAMS="quality=6 vad=false dtx=false"
SPEEX_CAPS="audio/x-raw-int,rate=16000"

ENCODER_PARAMS=${SPEEX_PARAMS}
RTP_PARAMS="latency=100"
AUDIO_CAPS=${SPEEX_CAPS}

gst-launch -v --gst-debug=3 gstrtpbin name=rtpbin ${RTP_PARAMS} \
filesrc location=../answerphone.ogg ! decodebin ! audioconvert \
! queue ! audioresample ! ${AUDIO_CAPS} ! ${ENCODER} ${ENCODER_PARAMS} ! ${PAYLOADER} ! "application/x-rtp,payload=(int)96" \
! rtpbin.send_rtp_sink_0 \
rtpbin.send_rtp_src_0 ! udpsink port=${SEND_TO_RTP_PORT} host=${SEND_TO_ADDR} \
rtpbin.send_rtcp_src_0 ! udpsink port=${SEND_TO_RTCP_PORT} host=${SEND_TO_ADDR} sync=false async=false
