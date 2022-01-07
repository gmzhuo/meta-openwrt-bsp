SUMMARY = "Example of how to build an external Linux kernel module"
DESCRIPTION = "${SUMMARY}"
LICENSE = "CLOSE"
LIC_FILES_CHKSUM = "file://ChangeLog;md5=26274509bf48473d672d111c90379232"

KERNEL_SPLIT_MODULES = "0"
inherit module
DEPENDS = "virtual/kernel"

DEPENDS += "flex-native"

SRC_URI = "file://qca-ssdk-2021-04-28-c9bc3bc3.tar.xz \
			file://0001-SSDK-config-add-kernel-5.10.patch file://0003-add-aquantia-phy-id-113CB0-0x31c31C12.patch file://0005-SSDK-dts-fix-of_get_mac_address.patch  file://0007-qca8081-convert-to-5.11-IRQ-model.patch \
			file://0002-SSDK-replace-ioremap_nocache-with-ioremap.patch  file://0004-platform-use-of_mdio_find_bus-to-get-MDIO-bus.patch  file://0006-SSDK-config-add-kernel-5.15.patch \
          "

# Modify these as desired
PV = "1.0"

S = "${WORKDIR}/qca-ssdk-2021-04-28-c9bc3bc3"

# The inherit of module.bbclass will automatically name module packages with
# "kernel-module-" prefix as required by the oe-core build environment.

#PACKAGES += "kernel-module-ssdk"


module_do_compile() {
	unset CFLAGS CPPFLAGS CXXFLAGS LDFLAGS


	oe_runmake CROSS_COMPILE="${CROSS_COMPILE}" ARCH="${ARCH}"  \
		KLIB_BUILD="${STAGING_KERNEL_BUILDDIR}" \
		MODPROBE=true KLIB="${D}" -j1 \
	AR="aarch64-poky-linux-musl-gcc-ar" AS="aarch64-poky-linux-musl-gcc -c -Os -pipe -mcpu=cortex-a53 -fno-caller-saves -fno-plt -fhonour-copts -Wno-error=unused-but-set-variable -Wno-error=unused-result -fmacro-prefix-map=/opt/work/yocto/openwrt/openwrt/build_dir/target-aarch64_cortex-a53_musl/linux-ipq807x_generic/qca-ssdk-2021-04-28-c9bc3bc3=qca-ssdk-2021-04-28-c9bc3bc3 -Wformat -Werror=format-security -fstack-protector -D_FORTIFY_SOURCE=1 -Wl,-z,now -Wl,-z,relro" \
	LD=aarch64-poky-linux-musl-ld NM="aarch64-poky-linux-musl-gcc-nm" CC="aarch64-poky-linux-musl-gcc" GCC="aarch64-poky-linux-musl-gcc" CXX="aarch64-poky-linux-musl-g++" \
	RANLIB="aarch64-openwrt-linux-musl-gcc-ranlib" STRIP=aarch64-openwrt-linux-musl-strip OBJCOPY=aarch64-openwrt-linux-musl-objcopy OBJDUMP=aarch64-poky-linux-musl-objdump \
	SIZE=aarch64-poky-linux-musl-size CROSS="aarch64-poky-linux-musl-" ARCH="aarch64" \
	TARGET_NAME= TOOL_PATH=/opt/work/yocto/openwrt/openwrt/staging_dir/toolchain-aarch64_cortex-a53_gcc-11.2.0_musl/bin \
	SYS_PATH=/opt/work/yocto/repo/build/tmp/work/ax9000-poky-linux-musl/linux-openwrt/1.0-r0/linux-ax9000-standard-build \
	KERNEL_SRC=/opt/work/yocto/repo/build/tmp/work/ax9000-poky-linux-musl/linux-openwrt/1.0-r0/linux-ax9000-standard-build/source \
	TOOLPREFIX=aarch64-poky-linux-musl- KVER="${KERNEL_VERSION}" ARCH=arm64 TARGET_SUFFIX="musl" GCC_VERSION=110200 \
	EXTRA_CFLAGS="-fno-PIC -DHZ=250 -fno-stack-protector -DBITS_PER_LONG=64 -I/opt/work/yocto/repo/build/tmp/work/ax9000-poky-linux-musl/linux-openwrt/1.0-r0/linux-ax9000-standard-build/source/arch/arm64/include/asm/mach -I/opt/work/yocto/repo/build/tmp/work/ax9000-poky-linux-musl/linux-openwrt/1.0-r0/linux-ax9000-standard-build/arch/arm64/include/generated/uapi/ -I/opt/work/yocto/repo/build/tmp/work/ax9000-poky-linux-musl/linux-openwrt/1.0-r0/linux-ax9000-standard-build/source/include/uapi/ -I/opt/work/yocto/repo/build/tmp/work/ax9000-poky-linux-musl/linux-openwrt/1.0-r0/linux-ax9000-standard-build/source/arch/arm64/include/uapi/ " \
	CROSS_COMPILE="aarch64-openwrt-linux-musl-" ARCH="arm64" KBUILD_HAVE_NLS=no KBUILD_BUILD_USER="" KBUILD_BUILD_HOST="" \
	KBUILD_BUILD_TIMESTAMP="Wed Dec 15 20:50:37 2021" KBUILD_BUILD_VERSION="0" \
	CONFIG_SHELL="bash" V=''  \
	cmd_syscalls= KBUILD_EXTRA_SYMBOLS="/opt/work/yocto/openwrt/openwrt/build_dir/target-aarch64_cortex-a53_musl/linux-ipq807x_generic/symvers/gpio-button-hotplug.symvers /opt/work/yocto/openwrt/openwrt/build_dir/target-aarch64_cortex-a53_musl/linux-ipq807x_generic/symvers/ath10k-ct.symvers /opt/work/yocto/openwrt/openwrt/build_dir/target-aarch64_cortex-a53_musl/linux-ipq807x_generic/symvers/mac80211.symvers /opt/work/yocto/openwrt/openwrt/build_dir/target-aarch64_cortex-a53_musl/linux-ipq807x_generic/symvers/qca-nss-dp.symvers /opt/work/yocto/openwrt/openwrt/build_dir/target-aarch64_cortex-a53_musl/linux-ipq807x_generic/symvers/qca-ssdk.symvers" \
	KERNELRELEASE="${KERNEL_VERSION}" PTP_FEATURE=disable SWCONFIG_FEATURE=disable CHIP_TYPE=HPPE ;
}

module_do_install() {
	install -d ${D}${includedir}/qca-ssdk
	install -d ${D}${includedir}/qca-ssdk/api
	install -d ${D}${includedir}/qca-ssdk/ref
	install -d ${D}${includedir}/qca-ssdk/fal
	install -d ${D}${includedir}/qca-ssdk/sal
	install -d ${D}${includedir}/qca-ssdk/init
	install -m 0644 ${S}/include/api/sw_ioctl.h ${D}${includedir}/qca-ssdk/api/sw_ioctl.h
	if [ -f ${S}/include/ref/ref_vsi.h ]; then \
	install -m 0644 ${S}/include/ref/ref_vsi.h ${D}${includedir}/qca-ssdk/ref/ref_vsi.h
	fi
	if [ -f ${S}/include/ref/ref_fdb.h ]; then \
	install -m 0644 ${S}/include/ref/ref_fdb.h ${D}${includedir}/qca-ssdk/ref/ref_fdb.h
	fi
	if [ -f ${S}/include/ref/ref_port_ctrl.h ]; then \
	install -m 0644 ${S}/include/ref/ref_port_ctrl.h ${D}${includedir}/qca-ssdk/ref/ref_port_ctrl.h
	fi
	if [ -f ${S}/include/init/ssdk_init.h ]; then \
	install -m 0644 ${S}/include/init/ssdk_init.h ${D}${includedir}/qca-ssdk/init/ssdk_init.h
	fi
	install -m 0644 ${S}/include/fal/* ${D}${includedir}/qca-ssdk/fal
	install -m 0644 ${S}/include/common/*.h ${D}${includedir}/qca-ssdk
	install -m 0644 ${S}/include/sal/os/linux/*.h ${D}${includedir}/qca-ssdk
	install -m 0644 ${S}/include/sal/os/*.h ${D}${includedir}/qca-ssdk	

	install -d ${D}${includedir}/${BPN}/
	install -Dm0644 ${S}/Module.symvers ${D}${includedir}/${BPN}/Module.symvers

	install -d ${D}/lib/modules/${KERNEL_VERSION}/updates/
	install -m 0644 ${S}/build/bin/*.ko ${D}/lib/modules/${KERNEL_VERSION}/updates/
}

inherit cml1

do_configure() {
}

RPROVIDES:${PN} += "kernel-module-qca-ssdk"

