SUMMARY = "Firmware files for use with Linux kernel"
HOMEPAGE = "https://www.kernel.org/"
DESCRIPTION = "Mediatek-firmware is firmware for mt76"
SECTION = "kernel"

LICENSE = "CLOSED"

LIC_FILES_CHKSUM = ""

SRC_URI = "https://www.candelatech.com/downloads/${QCA988X_FIRMWARE_FILE_CT};sha256sum=${QCA988X_FIRMWARE_FILE_CT_SHA256}"

PACKAGES += " firmware-qca988x-ct"

QCA988X_FIRMWARE_FILE_CT = "firmware-2-ct-full-community-22.bin.lede.022"
QCA988X_FIRMWARE_FILE_CT_SHA256 = "398e4380e7e55105f3da0f78af29d1e437404ed3a82597aa4b6daaa7dce1a38e"

FILES:firmware-qca988x-ct = "/lib/firmware/ath10k/QCA988X/hw2.0/*"

do_configure() {
}

do_install() {
	install -d -m 755 ${D}/lib/firmware/ath10k/QCA988X/hw2.0/

	install -m 644 ${WORKDIR}/${QCA988X_FIRMWARE_FILE_CT} \
		${D}/lib/firmware/ath10k/QCA988X/hw2.0/firmware-2.bin
}

do_compile() {
}

# Firmware files are generally not ran on the CPU, so they can be
# allarch despite being architecture specific
INSANE_SKIP = "arch"