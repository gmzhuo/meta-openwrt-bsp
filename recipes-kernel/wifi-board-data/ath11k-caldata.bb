SUMMARY = "Board data for ath11k"
HOMEPAGE = "https://www.kernel.org/"
DESCRIPTION = "Mediatek-firmware is firmware for mt76"
SECTION = "kernel"

LICENSE = "CLOSED"

LIC_FILES_CHKSUM = ""

SRC_URI = "file://ath11k-caldata"

S = "${WORKDIR}/"

PV = "1.0"

FILES:${PN} = "/etc/hotplug.d/firmware/*"


do_configure() {
}

do_install() {
	install -d -m 755 ${D}/etc/hotplug.d/firmware/

	install -m 755 ${S}/ath11k-caldata ${D}/etc/hotplug.d/firmware/11-ath11k-caldata
}

do_compile() {
}

# Firmware files are generally not ran on the CPU, so they can be
# allarch despite being architecture specific
INSANE_SKIP = "arch"