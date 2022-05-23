SUMMARY = "Example of how to build an external Linux kernel module"
DESCRIPTION = "${SUMMARY}"
LICENSE = "CLOSE"
LIC_FILES_CHKSUM = "file://ChangeLog;md5=26274509bf48473d672d111c90379232"

KERNEL_SPLIT_MODULES = "1"
inherit module
DEPENDS = "virtual/kernel"

DEPENDS += "flex-native"

SRC_URI = "git://git.codelinaro.org/clo/qsdk/oss/lklm/qca-ssdk.git;protocol=https;branch=NHSS.QSDK.12.0 \
			file://patch-ssdk/0001-SSDK-config-add-kernel-5.10.patch \
			file://patch-ssdk/0002-SSDK-replace-ioremap_nocache-with-ioremap.patch \
			file://patch-ssdk/0004-platform-use-of_mdio_find_bus-to-get-MDIO-bus.patch \
			file://patch-ssdk/0005-SSDK-dts-fix-of_get_mac_address.patch \
			file://patch-ssdk/0006-SSDK-config-add-kernel-5.15.patch \
			file://patch-ssdk/0007-qca8081-convert-to-5.11-IRQ-model.patch \
			file://patch-ssdk/0008-qca807x-add-a-LED-quirk-for-Xiaomi-AX9000.patch \
			file://patch-ssdk/0009-qca807x-add-a-LED-quirk-for-Xiaomi-AX3600.patch \
			file://patch-ssdk/0012-include-fix-compilation-error-for-parse_uci_option.patch \
          "

SRCREV = "62955de3c838028d3f72313eef99c57fa4babb71"
# Modify these as desired
PV = "1.0"

S = "${WORKDIR}/git"

# The inherit of module.bbclass will automatically name module packages with
# "kernel-module-" prefix as required by the oe-core build environment.


module_do_compile() {
	unset CFLAGS CPPFLAGS CXXFLAGS LDFLAGS


	oe_runmake CROSS_COMPILE="${CROSS_COMPILE}" ARCH="${ARCH}"  \
		KLIB_BUILD="${STAGING_KERNEL_BUILDDIR}" \
		MODPROBE=true KLIB="${D}" -j1 \
		CROSS="{CROSS_COMPILE}" \
		TARGET_NAME= TOOL_PATH="${STAGING_BINDIR_TOOLCHAIN}" \
		SYS_PATH="${STAGING_KERNEL_BUILDDIR}" \
		KERNEL_SRC="${STAGING_KERNEL_BUILDDIR}"/source \
		TOOLPREFIX=${CROSS_COMPILE} KVER="${KERNEL_VERSION}" \
		EXTRA_CFLAGS="-fno-PIC -I${STAGING_KERNEL_BUILDDIR}/arch/arm64/include/generated/uapi/ -I${STAGING_KERNEL_BUILDDIR}/source/include/uapi/ -I${STAGING_KERNEL_BUILDDIR}/source/arch/arm64/include/uapi/ " \
		KBUILD_HAVE_NLS=no KBUILD_BUILD_USER="" KBUILD_BUILD_HOST="" \
		PTP_FEATURE=disable SWCONFIG_FEATURE=disable CHIP_TYPE=HPPE
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

#inherit cml1

do_configure() {
}

RPROVIDES:${PN} += "kernel-module-qca-ssdk"

