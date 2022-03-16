SUMMARY = "Example of how to build an external Linux kernel module"
DESCRIPTION = "${SUMMARY}"
LICENSE = "CLOSED"
LIC_FILES_CHKSUM = ""

KERNEL_SPLIT_MODULES = "1"

inherit module
DEPENDS += "mac80211-kmod"

CT_KVER = "-5.15"

SRC_URI = "git://github.com/greearb/ath10k-ct.git;protocol=https \
		file://120-ath10k-fetch-calibration-data-via-nvmem-subsystem.patch \
		file://201-ath10k-add-LED-and-GPIO-controlling-support-for-various-chipsets.patch \
		file://202-ath10k-use-tpt-trigger-by-default.patch \
		file://300-ath10k-ct-Fix-spectral-scan-NULL-pointer.patch \
		file://960-0010-ath10k-limit-htt-rx-ring-size.patch \
		file://960-0011-ath10k-limit-pci-buffer-size.patch \
		"

SRCREV = "dc350bbf41d987c5b2db54405bcc9ef3cd66d5db"

S = "${WORKDIR}/git"

# The inherit of module.bbclass will automatically name module packages with
# "kernel-module-" prefix as required by the oe-core build environment.

RPROVIDES:${PN} += "kernel-module-ath10k-ct"
NOSTDINC_FLAGS = "\
		${KERNEL_NOSTDINC_FLAGS} \
		-I${S}/ \
		-I${S}/ath10k${CT_KVER} \
		-I${PKG_CONFIG_SYSROOT_DIR}/usr/include/mac80211-backport/uapi \
		-I${PKG_CONFIG_SYSROOT_DIR}/usr/include/mac80211-backport \
		-I${PKG_CONFIG_SYSROOT_DIR}/usr/include/mac80211/uapi \
		-I${PKG_CONFIG_SYSROOT_DIR}/usr/include/mac80211 \
		-include backport/autoconf.h \
		-include backport/backport.h "
NOSTDINC_FLAGS += " -DCONFIG_MAC80211_MESH"
NOSTDINC_FLAGS += " -DCONFIG_ATH10K_AHB"

NOSTDINC_FLAGS += " -DSTANDALONE_CT"
NOSTDINC_FLAGS += " -DCONFIG_MAC80211_DEBUGFS"
NOSTDINC_FLAGS += " -DCONFIG_ATH10K_DEBUGFS"
NOSTDINC_FLAGS += " -DCONFIG_ATH10K_DFS_CERTIFIED"
#NOSTDINC_FLAGS += " -DCONFIG_ATH10K_SPECTRAL"
NOSTDINC_FLAGS += " -DCONFIG_ATH10K_LEDS"
NOSTDINC_FLAGS += " -DCONFIG_ATH10K_SMALLBUFFERS"

CT_MAKEDEFS = " CONFIG_ATH10K=m CONFIG_ATH10K_PCI=m CONFIG_ATH10K_CE=y"
CT_MAKEDEFS += " CONFIG_ATH10K_AHB=m"
CT_MAKEDEFS += " CONFIG_ATH10K_DEBUGFS=y CONFIG_MAC80211_DEBUGFS=y"
#CT_MAKEDEFS += " CONFIG_ATH10K_SPECTRAL=y"
CT_MAKEDEFS += " CONFIG_ATH10K_LEDS=y"



module_do_compile() {
	unset CFLAGS CPPFLAGS CXXFLAGS LDFLAGS
	#/opt/work/yocto/openwrt/openwrt/build_dir/target-mips_24kc_musl/linux-ath79_generic/symvers/gpio-button-hotplug.symvers 

	
	oe_runmake ${CT_MAKEDEFS}  \
		-C "${STAGING_KERNEL_BUILDDIR}" \
		CROSS_COMPILE="${CROSS_COMPILE}" ARCH="${ARCH}" KBUILD_HAVE_NLS=no \
		KBUILD_EXTRA_SYMBOLS="${STAGING_INCDIR}/mac80211-kmod/Module.symvers" \
		KERNELRELEASE="${KERNEL_VERSION}" \
		M="${S}/ath10k-5.15" \
		NOSTDINC_FLAGS="${NOSTDINC_FLAGS}" \
		modules

	#oe_runmake ${CT_MAKEDEFS} -C ${STAGING_KERNEL_DIR} M=${S}/ath10k${CT_KVER} NOSTDINC_FLAGS="${NOSTDINC_FLAGS}"
}

module_do_install() {
	unset CFLAGS CPPFLAGS CXXFLAGS LDFLAGS

	cd ath10k${CT_KVER}
	oe_runmake -C "${STAGING_KERNEL_BUILDDIR}" \
		O=${STAGING_KERNEL_BUILDDIR} \
		M="${S}/ath10k-5.15" \
		KLIB_BUILD="${STAGING_KERNEL_BUILDDIR}" \
		MODPROBE=true MODLIB="${D}${nonarch_base_libdir}/modules/${KERNEL_VERSION}" \
		INSTALL_FW_PATH="${D}${nonarch_base_libdir}/firmware" modules_install

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


do_configure() {
    bbnote do configure
	cp ${PKG_CONFIG_SYSROOT_DIR}/usr/include/mac80211/ath/*.h ${WORKDIR}/git/
}

RPROVIDES:${PN} += "kernel-module-ath10k"
