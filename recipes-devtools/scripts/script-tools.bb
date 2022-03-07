BBCLASSEXTEND = "native nativesdk"
SRC_URI = " file://mkits.sh"
LICENSE = "GPL2.0"
LIC_FILES_CHKSUM = "file://mkits.sh;beginline=1;endline=14;md5=ae4c1e94869bfb29b7a5b740b5dd3b5f"

S = "${WORKDIR}"
do_install () {
	# Specify install commands here
	install -d ${D}/usr/bin
	install -m 0755 ${WORKDIR}/mkits.sh ${D}/usr/bin
}
