SUMMARY = "Firmware files for use with Linux kernel"
HOMEPAGE = "https://www.kernel.org/"
DESCRIPTION = "Mediatek-firmware is firmware for mt76"
SECTION = "kernel"

LICENSE = "CLOSED"

LIC_FILES_CHKSUM = ""

SRC_URI = "git://github.com/quic/upstream-wifi-fw.git;protocol=https;branch=main"

S = "${WORKDIR}/git"

PV = "1.0+git${SRCPV}"
SRCREV = "4abc7856e26618a9b7a70f24d7f61e844f160e17"

PACKAGES += " ${PN}-IPQ6018 ${PN}-IPQ8074 ${PN}-QCN9074"

FILES:${PN}-IPQ6018 = "/lib/firmware/IPQ6018/*"
FILES:${PN}-IPQ8074 = "/lib/firmware/IPQ8074/*"
FILES:${PN}-QCN9074 = "/lib/firmware/ath11k/QCN9074/hw1.0/*"

do_configure() {
}

do_install() {
	install -d -m 755 ${D}/lib/firmware/IPQ6018
	install -d -m 755 ${D}/lib/firmware/IPQ8074
	install -d -m 755 ${D}/lib/firmware/ath11k/QCN9074/hw1.0

	install -m 644 ${S}/ath11k-firmware/IPQ6018/hw1.0/2.5.0.1/WLAN.HK.2.5.0.1-01208-QCAHKSWPL_SILICONZ-1/* \
		${D}/lib/firmware/IPQ6018/
	install -m 644 ${S}/ath11k-firmware/IPQ8074/hw2.0/2.5.0.1/WLAN.HK.2.5.0.1-01208-QCAHKSWPL_SILICONZ-1/* \
		${D}/lib/firmware/IPQ8074/
	install -m 644 ${S}/ath11k-firmware/QCN9074/hw1.0/2.5.0.1/WLAN.HK.2.5.0.1-01208-QCAHKSWPL_SILICONZ-1/* \
		${D}/lib/firmware/ath11k/QCN9074/hw1.0/
}

do_compile() {
}

# Firmware files are generally not ran on the CPU, so they can be
# allarch despite being architecture specific
INSANE_SKIP = "arch"