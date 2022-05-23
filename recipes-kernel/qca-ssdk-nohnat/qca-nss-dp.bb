DESCRIPTION = "NSS Dataplane"
LICENSE = "ISC"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/${LICENSE};md5=f3b90e78ea0cffb20bf5cca7947a896d"

KERNEL_SPLIT_MODULES = "1"
inherit module

DEPENDS = "virtual/kernel qca-ssdk"


SRC_URI = "git://git.codelinaro.org/clo/qsdk/oss/lklm/nss-dp.git;protocol=https;branch=NHSS.QSDK.12.0 \
		file://patch-nss-dp/0001-edma_tx_rx-support-newer-kernels-time-stamping-API.patch \
		file://patch-nss-dp/0002-nss_dp_main-make-phy-mode-code-compatible-with-newer.patch \
		file://patch-nss-dp/0003-Drop-_nocache-variants-of-ioremap.patch \
		file://patch-nss-dp/0004-EDMA-Fix-NAPI-packet-counting.patch \
		file://patch-nss-dp/0005-EDMA-Use-NAPI_POLL_WEIGHT-as-NAPI-weight.patch \
		file://patch-nss-dp/0006-NSS-DP-fix-of_get_mac_address.patch \
		file://patch-nss-dp/0007-NSS-DP-implement-ethernet-IOCTL-s.patch \
		file://patch-nss-dp/0008-switchdev-remove-the-transaction-structure.patch \
		file://patch-nss-dp/0009-switchdev-use-new-switchdev-flags.patch \
		file://patch-nss-dp/0010-switchdev-fix-FDB-roaming.patch \
		file://patch-nss-dp/0011-treewide-fix-confusing-printing-of-registered-netdev.patch \
		file://patch-nss-dp/0012-gmac-syn-xgmac-silence-debug-log-on-probe.patch \
	   "

SRCREV = "72e9ec4187414461cbcf6ccff100e8b5ebe5f40b"

S = "${WORKDIR}/git"


do_ls_arch_file () {
	ln ${S}/hal/soc_ops/${SOC_TYPE}/nss_${SOC_TYPE}.h \
		${S}/exports/nss_dp_arch.h
}

addtask do_ls_arch_file after do_patch before do_configure
 
do_configure() {
}

do_compile() {
	#make -C /opt/work/yocto/repo/build/tmp-ax9000/work-shared/ax9000/kernel-source M=${S} V=1 \
	make -C "${STAGING_KERNEL_BUILDDIR}"/source M=${S} V=1 \
		CROSS_COMPILE='${TARGET_PREFIX}' \
		ARCH='arm64' \
		SUBDIRS="${S}" \
		EXTRA_CFLAGS="-I${STAGING_INCDIR}/qca-ssdk" \
		SoC='${SOC_TYPE}' \
		KBUILD_EXTRA_SYMBOLS=${STAGING_INCDIR}/qca-ssdk/Module.symvers
}

#KERNEL_MODULE_AUTOLOAD += " qca-nss-dp"
do_install() {
	install -d ${D}${base_libdir}/modules/${KERNEL_VERSION}/kernel/drivers/${PN}
	install -m 0644 qca-nss-dp${KERNEL_OBJECT_SUFFIX} ${D}${base_libdir}/modules/${KERNEL_VERSION}/kernel/drivers/${PN}
	install -d ${D}${includedir}/qca-nss-dp
	install -m 0644 exports/* ${D}/${includedir}/qca-nss-dp/
}

RPROVIDES:${PN} += "kernel-module-qca-nss-dp"

