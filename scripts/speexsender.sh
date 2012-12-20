#!/bin/sh

SEND_TO_ADDR=130.240.95.200 #53.169
SEND_TO_RTP_PORT=40000
SEND_TO_RTCP_PORT=40001

ENCODER=speexenc
PAYLOADER=rtpspeexpay

SPEEX_PARAMS="quality=6 vad=false dtx=false"
SPEEX_CAPS="audio/x-raw-int,rate=16000"

ENCODER_PARAMS=${SPEEX_PARAMS}
RTP_PARAMS="latency=100"
AUDIO_CAPS=${SPEEX_CAPS}

gst-launch -v --gst-debug=3 gstrtpbin name=rtpbin ${RTP_PARAMS} \
audiotestsrc \
! queue ! audioresample ! ${AUDIO_CAPS} ! ${ENCODER} ${ENCODER_PARAMS} ! ${PAYLOADER} ! "application/x-rtp,payload=(int)103" \
! rtpbin.send_rtp_sink_0 \
rtpbin.send_rtp_src_0 ! udpsink port=${SEND_TO_RTP_PORT} host=${SEND_TO_ADDR} \
rtpbin.send_rtcp_src_0 ! udpsink port=${SEND_TO_RTCP_PORT} host=${SEND_TO_ADDR} sync=false async=false
