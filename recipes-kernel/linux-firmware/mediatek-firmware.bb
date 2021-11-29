SUMMARY = "Firmware files for use with Linux kernel"
HOMEPAGE = "https://www.kernel.org/"
DESCRIPTION = "Mediatek-firmware is firmware for mt76"
SECTION = "kernel"

LICENSE = "CLOSED"

LIC_FILES_CHKSUM = ""

SRC_URI = "git://github.com/openwrt/mt76.git"

S = "${WORKDIR}/git"

PV = "1.0+git${SRCPV}"
SRCREV = "${AUTOREV}"

PACKAGES += " ${PN}-mt76x2"

FILES:${PN}-mt76x2 = "/lib/firmware/mt7662_rom_patch.bin /lib/firmware/mt7662.bin /usr/share/usb_modeswitch/0e8d\:2870"

do_install() {
	install -d -m 755 ${D}/lib/firmware
	install -d -m 755 ${D}/usr/share/usb_modeswitch/
	install -m 644 ${S}/firmware/mt7662_patch_e3_hdr_v0.0.2_P69.bin ${D}/lib/firmware/mt7662_rom_patch.bin
	install -m 644 ${S}/firmware/mt7662_firmware_e3_v1.9.bin ${D}/lib/firmware/mt7662.bin

cat <<EOF >"${D}/usr/share/usb_modeswitch/0e8d\:2870"
# COMFAST CF-WU782AC WiFi Dongle
TargetVendor=0x0e8d
TargetProduct=0x7612
StandardEject=1
EOF
}

do_compile() {
}