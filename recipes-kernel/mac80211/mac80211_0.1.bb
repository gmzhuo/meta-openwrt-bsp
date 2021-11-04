SUMMARY = "Example of how to build an external Linux kernel module"
DESCRIPTION = "${SUMMARY}"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=6bc538ed5bd9a7fc9398086aedcd7e46"

inherit module
DEPENDS += "flex-native"

SRC_URI = "https://cdn.kernel.org/pub/linux/kernel/projects/backports/stable/v5.15-rc6/backports-5.15-rc6-1.tar.xz \
		  file://test.cfg \
          "

include include/build.inc include/subsys.inc include/ath.inc include/ath5k.inc  include/ath9k.inc \
	include/ath10k.inc include/ath11k.inc include/rt2x00.inc include/brcm.inc include/mwl.inc

SRC_URI[sha256sum] = "9282612c4c02ef9fc9d74405303033f6b53914cd63d631eef0f43155fcd38932"

S = "${WORKDIR}/backports-5.15-rc6-1"

# The inherit of module.bbclass will automatically name module packages with
# "kernel-module-" prefix as required by the oe-core build environment.

#make  -C "/opt/work/yocto/openwrt/openwrt/build_dir/target-mipsel_24kc_musl/linux-ramips_mt7620/backports-5.15-rc6-1" \
#KCFLAGS="-fmacro-prefix-map=/opt/work/yocto/openwrt/openwrt/build_dir/target-mipsel_24kc_musl=target-mipsel_24kc_musl" \
#HOSTCFLAGS="-O2 -I/opt/work/yocto/openwrt/openwrt/staging_dir/host/include -I/opt/work/yocto/openwrt/openwrt/staging_dir/hostpkg/include -I/opt/work/yocto/openwrt/openwrt/staging_dir/target-mipsel_24kc_musl/host/include -Wall -Wmissing-prototypes -Wstrict-prototypes" \
#CROSS_COMPILE="mipsel-openwrt-linux-musl-" ARCH="mips" KBUILD_HAVE_NLS=no KBUILD_BUILD_USER="" KBUILD_BUILD_HOST="" \
#KBUILD_BUILD_TIMESTAMP="Fri Oct 29 18:36:13 2021" KBUILD_BUILD_VERSION="0" HOST_LOADLIBES="-L/opt/work/yocto/openwrt/openwrt/staging_dir/host/lib" \
#KBUILD_HOSTLDLIBS="-L/opt/work/yocto/openwrt/openwrt/staging_dir/host/lib" CONFIG_SHELL="bash" V=''  cmd_syscalls= \
#KBUILD_EXTRA_SYMBOLS="/opt/work/yocto/openwrt/openwrt/build_dir/target-mipsel_24kc_musl/linux-ramips_mt7620/symvers/gpio-button-hotplug.symvers /opt/work/yocto/openwrt/openwrt/build_dir/target-mipsel_24kc_musl/linux-ramips_mt7620/symvers/mac80211.symvers /opt/work/yocto/openwrt/openwrt/build_dir/target-mipsel_24kc_musl/linux-ramips_mt7620/symvers/mt76.symvers" \
#KERNELRELEASE=5.10.75 \
#EXTRA_CFLAGS="-I/opt/work/yocto/openwrt/openwrt/build_dir/target-mipsel_24kc_musl/linux-ramips_mt7620/backports-5.15-rc6-1/include -fmacro-prefix-map=/opt/work/yocto/openwrt/openwrt/build_dir/target-mipsel_24kc_musl/linux-ramips_mt7620/backports-5.15-rc6-1=backports-5.15-rc6-1" \
#KLIB_BUILD="/opt/work/yocto/openwrt/openwrt/build_dir/target-mipsel_24kc_musl/linux-ramips_mt7620/linux-5.10.75" \
#MODPROBE=true KLIB=/lib/modules/5.10.75 KERNEL_SUBLEVEL=10 KBUILD_LDFLAGS_MODULE_PREREQ= modules

module_do_compile() {
	unset CFLAGS CPPFLAGS CXXFLAGS LDFLAGS

	oe_runmake CROSS_COMPILE="${CROSS_COMPILE}" ARCH="mips" KBUILD_HAVE_NLS=no KBUILD_BUILD_USER="" KBUILD_BUILD_HOST="" \
		EXTRA_CFLAGS="-I${S}/include" KERNELRELEASE=${KERNEL_VERSION} \
		KLIB_BUILD="${STAGING_KERNEL_BUILDDIR}" \
		MODPROBE=true KLIB="${STAGING_KERNEL_BUILDDIR}" KERNEL_SUBLEVEL=10 KBUILD_LDFLAGS_MODULE_PREREQ= V=1 modules
}

module_do_install() {
	unset CFLAGS CPPFLAGS CXXFLAGS LDFLAGS
	oe_runmake CROSS_COMPILE="${CROSS_COMPILE}" ARCH="mips" KBUILD_HAVE_NLS=no KBUILD_BUILD_USER="" KBUILD_BUILD_HOST="" \
		EXTRA_CFLAGS="-I${S}/include" KERNELRELEASE=${KERNEL_VERSION} \
		KLIB_BUILD="${STAGING_KERNEL_BUILDDIR}" \
		CC="${KERNEL_CC}" LD="${KERNEL_LD}" \
		O=${STAGING_KERNEL_BUILDDIR} \
		MODPROBE=true KLIB="${D}" KERNEL_SUBLEVEL=10 KBUILD_LDFLAGS_MODULE_PREREQ= modules_install V=1

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
}

inherit cml1

do_configure() {
	export PATH_BACK=${PATH}
	export PATH=$PATH:/usr/bin/

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
	touch .config
	bbnote ${@" ".join(find_cfgs(d))}
	merge_config.sh -m .config ${@" ".join(find_cfgs(d))}
	cat .config
	make CROSS_COMPILE="${CROSS_COMPILE}" ARCH="mips" KBUILD_HAVE_NLS=no KBUILD_BUILD_USER="" KBUILD_BUILD_HOST="" \
		EXTRA_CFLAGS="-I${S}/include" KERNELRELEASE=${KERNEL_VERSION}\
		KLIB_BUILD="${STAGING_KERNEL_BUILDDIR}" \
		CC="${KERNEL_CC}" LD="${KERNEL_LD}" \
		O=${STAGING_KERNEL_BUILDDIR} \
		MODPROBE=true KLIB="${STAGING_KERNEL_BUILDDIR}" KERNEL_SUBLEVEL=10 KBUILD_LDFLAGS_MODULE_PREREQ= allnoconfig CC=cc
	export PATH=${PATH_BACK}
}

RPROVIDES:${PN} += "kernel-module-mac80211"
