BBCLASSEXTEND = "native nativesdk"
SRC_URI = " file://mkits.sh file://functions.sh file://sysupgrade-tar.sh file://ubinize-image.sh"
LICENSE = "GPL2.0"
LIC_FILES_CHKSUM = "file://mkits.sh;beginline=2;endline=14;md5=63f8459f12db32fe68adabea257758ef"

S = "${WORKDIR}"
do_install () {
	# Specify install commands here
	install -d ${D}${bindir}
	install -m 0755 ${WORKDIR}/mkits.sh ${D}${bindir}
	install -m 0755 ${WORKDIR}/functions.sh ${D}${bindir}
	install -m 0755 ${WORKDIR}/sysupgrade-tar.sh ${D}${bindir}
	install -m 0755 ${WORKDIR}/ubinize-image.sh ${D}${bindir}
}
