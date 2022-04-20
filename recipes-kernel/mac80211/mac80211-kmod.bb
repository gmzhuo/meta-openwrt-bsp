SUMMARY = "Example of how to build an external Linux kernel module"
DESCRIPTION = "${SUMMARY}"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=6bc538ed5bd9a7fc9398086aedcd7e46"

KERNEL_SPLIT_MODULES = "1"
inherit module
DEPENDS += "flex-native ncurses-native"
MAC80211_PACKAGE_CONFIGS ?= ""

SRC_URI = "https://cdn.kernel.org/pub/linux/kernel/projects/backports/stable/v5.15.8/backports-5.15.8-1.tar.xz \
		  file://test.cfg \
          "

include include/build.inc include/subsys.inc include/ath.inc include/ath5k.inc  include/ath9k.inc \
	include/ath10k.inc include/ath11k.inc include/rt2x00.inc include/brcm.inc include/mwl.inc

SRC_URI += " file://patches/0901-add-nl80211-debug.patch file://patches/0902-adust-rx-tx.patch"

SRC_URI[sha256sum] = "9f71b659c034f19d156532ec780fcb606cee3c4ccc42e2f8ef18dd3e9f1b6820"

S = "${WORKDIR}/backports-5.15.8-1"

FILES:${PN}-dev += " /usr/include/*"


# The inherit of module.bbclass will automatically name module packages with
# "kernel-module-" prefix as required by the oe-core build environment.

module_do_compile() {
	unset CFLAGS CPPFLAGS CXXFLAGS LDFLAGS

	oe_runmake CROSS_COMPILE="${CROSS_COMPILE}" ARCH="${ARCH}"  \
		KLIB_BUILD="${STAGING_KERNEL_BUILDDIR}" \
		MODPROBE=true KLIB="${D}" modules
}

module_do_install() {
	unset CFLAGS CPPFLAGS CXXFLAGS LDFLAGS

	oe_runmake CROSS_COMPILE="${CROSS_COMPILE}" ARCH="${ARCH}"  \
		KLIB_BUILD="${STAGING_KERNEL_BUILDDIR}" \
		MODPROBE=true KLIB="${D}" modules_install

	if [ ! -e "${B}/${MODULES_MODULE_SYMVERS_LOCATION}/Module.symvers" ] ; then
		bbwarn "Module.symvers not found in ${B}/${MODULES_MODULE_SYMVERS_LOCATION}"
		bbwarn "Please consider setting MODULES_MODULE_SYMVERS_LOCATION to a"
		bbwarn "directory below B to get correct inter-module dependencies"
	else
		install -Dm0644 "${B}/${MODULES_MODULE_SYMVERS_LOCATION}"/Module.symvers ${D}${includedir}/${BPN}/Module.symvers
		# Module.symvers contains absolute path to the build directory.
		# While it doesn't actually seem to matter which path is specified,
		# clear them out to avoid confusion
		sed -e 's:${B}/::g' -i ${D}${includedir}/${BPN}/Module.symvers
	fi

	mkdir -p \
		${D}/usr/include/mac80211 \
		${D}/usr/include/mac80211-backport \
		${D}/usr/include/mac80211/ath \
		${D}/usr/include/net/mac80211

	cp ${S}/net/mac80211/*.h ${S}/include/* ${D}/usr/include/mac80211/ -rf
	cp ${S}/backport-include/* ${D}/usr/include/mac80211-backport/ -rf
	cp ${S}/net/mac80211/rate.h ${D}/usr/include/net/mac80211/ -rf
	cp ${S}/drivers/net/wireless/ath/*.h ${D}/usr/include/mac80211/ath/ -rf
	rm -f ${D}/usr/include/mac80211-backport/linux/module.h
}

inherit cml1

do_configure() {
	rm -rf \
		include/linux/ssb \
		include/linux/bcma \
		include/net/bluetooth

	rm -f \
		include/linux/cordic.h \
		include/linux/crc8.h \
		include/linux/eeprom_93cx6.h \
		include/linux/wl12xx.h \
		include/linux/spi/libertas_spi.h \
		include/net/ieee80211.h \
		backport-include/linux/bcm47xx_nvram.h

	set -x
	>.config

	for config in ${MAC80211_PACKAGE_CONFIGS}; do
		echo $config >> .config
	done
	cp .config CONFIG.SEC

	#merge_config.sh -m .config ${@" ".join(find_cfgs(d))}

	oe_runmake CROSS_COMPILE="${CROSS_COMPILE}" ARCH="${ARCH}"  \
		KLIB_BUILD="${STAGING_KERNEL_BUILDDIR}" \
		MODPROBE=true KLIB="${D}" CC=gcc LEX=flex allnoconfig 
}

RPROVIDES:${PN} += "kernel-module-mac80211"
