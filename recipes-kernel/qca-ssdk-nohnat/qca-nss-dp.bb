DESCRIPTION = "NSS Dataplane"
LICENSE = "ISC"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/${LICENSE};md5=f3b90e78ea0cffb20bf5cca7947a896d"

KERNEL_SPLIT_MODULES = "0"
inherit module

DEPENDS = "virtual/kernel qca-ssdk"


SRC_URI = "file://qca-nss-dp-2021-03-26-e0c89348.tar.xz \
		file://patch-nss-dp/0001-edma_tx_rx-support-newer-kernels-time-stamping-API.patch    \
		file://patch-nss-dp/0003-Drop-_nocache-variants-of-ioremap.patch  \
		file://patch-nss-dp/0005-EDMA-Use-NAPI_POLL_WEIGHT-as-NAPI-weight.patch \
		file://patch-nss-dp/0002-nss_dp_main-make-phy-mode-code-compatible-with-newer.patch  \
		file://patch-nss-dp/0004-EDMA-Fix-NAPI-packet-counting.patch      \
		file://patch-nss-dp/0006-NSS-DP-fix-of_get_mac_address.patch \
	   "


S = "${WORKDIR}/qca-nss-dp-2021-03-26-e0c89348"


do_ls_arch_file () {
	ln -s ${S}/hal/arch/${SOC_TYPE}/nss_${SOC_TYPE}.h \
		${S}/exports/nss_dp_arch.h
}

addtask do_ls_arch_file after do_patch before do_configure
 
do_configure() {
}

do_compile() {
	make -C /opt/work/yocto/repo/build/tmp/work-shared/ax9000/kernel-source M=${S} V=1 \
		CROSS_COMPILE='${TARGET_PREFIX}' \
		ARCH='arm64' \
		SUBDIRS="${S}" \
		EXTRA_CFLAGS="-I${STAGING_INCDIR}/qca-ssdk" \
		SoC='${SOC_TYPE_AND_WIDTH}' \
		KBUILD_EXTRA_SYMBOLS=${STAGING_INCDIR}/qca-ssdk/Module.symvers
}

do_install() {
	install -d ${D}${base_libdir}/modules/${KERNEL_VERSION}/kernel/drivers/${PN}
	install -m 0644 qca-nss-dp${KERNEL_OBJECT_SUFFIX} ${D}${base_libdir}/modules/${KERNEL_VERSION}/kernel/drivers/${PN}
	install -d ${D}${includedir}/qca-nss-dp
	install -m 0644 exports/* ${D}/${includedir}/qca-nss-dp/
}

RPROVIDES:${PN} += "kernel-module-qca-nss-dp"

