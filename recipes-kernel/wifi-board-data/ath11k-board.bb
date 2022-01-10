SUMMARY = "Board data for ath11k"
HOMEPAGE = "https://www.kernel.org/"
DESCRIPTION = "Mediatek-firmware is firmware for mt76"
SECTION = "kernel"

LICENSE = "CLOSED"

LIC_FILES_CHKSUM = ""

SRC_URI = "git://source.codeaurora.org/quic/qsdk/oss/ath11k-bdf;protocol=https;branch=NHSS.QSDK.11.4.0.5"
#SRC_URI[sha256sum] = "71c2389913cd990a23000f24e1c7f2626fbff9fa374657b7368d09fec2a27940"

S = "${WORKDIR}/git"

PV = "1.0+git${SRCPV}"
SRCREV = "0bc83743aab26006c084963760fa7dd7232ef123"

PACKAGES += " ${PN}-IPQ6018 ${PN}-IPQ8074 ${PN}-QCN9074"

FILES:${PN}-IPQ6018 = "/lib/firmware/ath11k/IPQ6018/hw1.0/*"
FILES:${PN}-IPQ8074 = "/lib/firmware/ath11k/IPQ8074/hw2.0/*"
FILES:${PN}-QCN9074 = "/lib/firmware/ath11k/QCN9074/hw1.0/*"

do_configure() {
}

do_install() {
	install -d -m 755 ${D}/lib/firmware/ath11k/IPQ6018/hw1.0/
	install -d -m 755 ${D}/lib/firmware/ath11k/IPQ8074/hw2.0/
	install -d -m 755 ${D}/lib/firmware/ath11k/QCN9074/hw1.0/

	install -m 644 ${S}/IPQ6018/hw1.0/WLAN.HK.2.5.0.1/WLAN.HK.2.5.0.1-01192-QCAHKSWPL_SILICONZ-1/board-2.bin  \
		${D}/lib/firmware/ath11k/IPQ6018/hw1.0/
	install -m 644 ${S}/IPQ8074/hw2.0/WLAN.HK.2.5.0.1/WLAN.HK.2.5.0.1-01192-QCAHKSWPL_SILICONZ-1/board-2.bin \
		${D}/lib/firmware/ath11k/IPQ8074/hw2.0/
	install -m 644 ${S}/QCN9074/hw1.0/WLAN.HK.2.5.0.1/WLAN.HK.2.5.0.1-01192-QCAHKSWPL_SILICONZ-1/board-2.bin \
		${D}/lib/firmware/ath11k/QCN9074/hw1.0/
}

do_compile() {
}

# Firmware files are generally not ran on the CPU, so they can be
# allarch despite being architecture specific
INSANE_SKIP = "arch"