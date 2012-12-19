

gst-launch audiotestsrc ! audio/x-raw-int,rate=16000 ! speexenc ! rtpspeexpay ! rtpspeexdepay ! speexdec ! speexenc ! oggmux ! filesink location=lol.ogg

