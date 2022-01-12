FILESEXTRAPATHS:prepend := "${THISDIR}/files:"
SRC_URI += " file://boot"

do_install:append () {
	install -Dm 0755 ${WORKDIR}/boot ${D}/etc/init.d/boot
}
