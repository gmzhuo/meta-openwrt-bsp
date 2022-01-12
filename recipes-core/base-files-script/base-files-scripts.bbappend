FILESEXTRAPATHS:prepend := "${THISDIR}/files:"
SRC_URI += " file://functions.sh"

do_install:append () {
	install -Dm 0644 ${WORKDIR}/functions.sh ${D}/lib/functions.sh
}