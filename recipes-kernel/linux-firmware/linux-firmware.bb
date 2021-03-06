SUMMARY = "Firmware files for use with Linux kernel"
HOMEPAGE = "https://www.kernel.org/"
DESCRIPTION = "Mediatek-firmware is firmware for mt76"
SECTION = "kernel"

LICENSE = "CLOSED"

LIC_FILES_CHKSUM = ""

SRC_URI = "https://cdn.kernel.org/pub/linux/kernel/firmware/linux-firmware-${PV}.tar.gz"
SRC_URI[sha256sum] = "c7a83c2f28807484d5c68566915fa632f880ad13ccb6b095484bb283dae94f91"

PV = "20220209"

PACKAGES += " firmware-ath10k-qca988x"
PACKAGES += " firmware-ath10k-qca9887"
PACKAGES += " board-ath10k-qca988x"
PACKAGES += " board-ath10k-qca9887"
PACKAGES += " firmware-rtl8153a-4"

FILES:firmware-ath10k-qca988x = "/lib/firmware/ath10k/QCA988X/hw2.0/firmware*"
FILES:board-ath10k-qca988x = "/lib/firmware/ath10k/QCA988X/hw2.0/board*"

FILES:firmware-ath10k-qca9887 = "/lib/firmware/ath10k/QCA9887/hw1.0/firmware*"
FILES:board-ath10k-qca9887 = "/lib/firmware/ath10k/QCA9887/hw1.0/board*"

FILES:firmware-rtl8153a-4 = "/lib/firmware/rtl_nic/rtl8153a-4.fw"

do_configure() {
}

do_install() {
	install -d -m 755 ${D}/lib/firmware/ath10k/QCA988X/hw2.0/

	install ${S}/ath10k/QCA988X/hw2.0/firmware-5.bin ${D}/lib/firmware/ath10k/QCA988X/hw2.0/
	install ${S}/ath10k/QCA988X/hw2.0/board.bin ${D}/lib/firmware/ath10k/QCA988X/hw2.0/

	install -d -m 755 ${D}/lib/firmware/ath10k/QCA9887/hw1.0/

	install ${S}/ath10k/QCA9887/hw1.0/firmware-5.bin ${D}/lib/firmware/ath10k/QCA9887/hw1.0/
	install ${S}/ath10k/QCA9887/hw1.0/board.bin ${D}/lib/firmware/ath10k/QCA9887/hw1.0/

	install -d -m 755 ${D}/lib/firmware/rtl_nic/
	install ${S}/rtl_nic/rtl8153a-4.fw ${D}/lib/firmware/rtl_nic/
}

do_compile() {
}

# Firmware files are generally not ran on the CPU, so they can be
# allarch despite being architecture specific
INSANE_SKIP = "arch"