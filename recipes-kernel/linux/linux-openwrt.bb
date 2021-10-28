# SPDX-License-Identifier: Apache-2.0
#
# Copyright (c) 2020 Arm Limited
#

DESCRIPTION = "Linux Android Common Kernel"
SECTION = "kernel"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://${S}/COPYING;md5=6bc538ed5bd9a7fc9398086aedcd7e46"

require recipes-kernel/linux/linux-yocto.inc

COMPATIBLE_MACHINE ?= "invalid"

S = "${WORKDIR}/git"

LINUX_VERSION ?= "${PV}"
KERNEL_VERSION_SANITY_SKIP = "1"
KBRANCH = ""
KERNEL_FEATURE_BSP_CONFIG ?= "config-5.10"
OPENWRT_KERNEL_DTS_PATH ?= ""




SRC_URI = " \
    git://mirrors.tuna.tsinghua.edu.cn/git/linux-stable.git;protocol=https;branch=linux-5.10.y \
	${OPENWRT_SRC_URI};type=openwrt;name=openwrt;destsuffix=openwrt \
	file://${MACHINE_EXTERNAL_PATCH} \
    "

SRCREV = "${AUTOREV}"


do_kernel_metadata() {
}

do_kernel_configme() {
	cd ${S}
	sccs_from_src_uri="${@" ".join(find_sccs(d))}"
	patches="${@" ".join(find_patches(d,''))}"
	bbnote "do_kernel_configme: sccs_from_src_uri ${sccs_from_src_uri} patches ${patches}"
	config_flags=""
	configs="${STAGING_KERNEL_DIR}/.kernel-meta/cfg/config-5.10.cfg ${STAGING_KERNEL_DIR}/.kernel-meta/cfg/config-machine-5.10.cfg"
	CFLAGS="${CFLAGS} ${TOOLCHAIN_OPTIONS}" HOSTCC="${BUILD_CC} ${BUILD_CFLAGS} ${BUILD_LDFLAGS}" HOSTCPP="${BUILD_CPP}" CC="${KERNEL_CC}" LD="${KERNEL_LD}" \
		ARCH=${ARCH} merge_config.sh -O ${B} ${config_flags} ${configs} > ${STAGING_KERNEL_DIR}/.kernel-meta/cfg/merge_config_build.log 2>&1
}

do_kernel_configcheck() {
}

do_patch() {


	cd ${S}
	bbnote "$PWD"

	cp ${WORKDIR}/openwrt/target/linux/generic/files/* ${S}/ -rfd
	patches=$(ls ${WORKDIR}/openwrt/target/linux/generic/back*5.10/*)
	for patche in ${patches}
	do
		bbnote "to apply $patche in $PWD"
		patch -p1 <${patche}
	done

	patches=$(ls ${WORKDIR}/openwrt/target/linux/generic/pen*5.10/*)
	for patche in ${patches}
	do
		bbnote "to apply $patche in $PWD"
		patch -p1 <${patche}
	done

	patches=$(ls ${WORKDIR}/openwrt/target/linux/generic/hack*5.10/*)
	for patche in ${patches}
	do
		bbnote "to apply $patche in $PWD"
		patch -p1 <${patche}
	done

	if [ ! -z ${KERNEL_FEATURE_BSP_PATH} ]; then
		cp ${WORKDIR}/openwrt/target/linux/${KERNEL_FEATURE_BSP_PATH}/files/* ${S}/ -rfd
		for patchpath in ${KERNEL_FEATURE_BSP_PATCH_PATHS}
		do
			patches=$(ls ${WORKDIR}/openwrt/target/linux/${KERNEL_FEATURE_BSP_PATH}/${patchpath}/*)
			for patche in ${patches}
			do
				patch -p1 <${patche}
			done
		done
	fi

	[ -d ${WORKDIR}/openwrt/target/linux/${KERNEL_FEATURE_BSP_PATH}/dts ] && {
		cp ${WORKDIR}/openwrt/target/linux/${KERNEL_FEATURE_BSP_PATH}/dts/* ${S}/arch/${ARCH}/boot/dts/${OPENWRT_KERNEL_DTS_PATH}/ -rfd
	}

	echo "EXTRA_CFLAGS += -I\${srctree}/include/linux -I\${srctree}/include/linux/lzma" >>${S}/lib/lzma/Makefile
	echo "dtb-y += ${MACHINE_DEFAULT_DTB}" >>${S}/arch/${ARCH}/boot/dts/${OPENWRT_KERNEL_DTS_PATH}/Makefile

	mkdir -p ${STAGING_KERNEL_DIR}/.kernel-meta/cfg/
	cp ${WORKDIR}/openwrt/target/linux/generic/config-5.10 ${STAGING_KERNEL_DIR}/.kernel-meta/cfg/config-5.10.cfg
	cp ${WORKDIR}/openwrt/target/linux/${KERNEL_FEATURE_BSP_PATH}/${KERNEL_FEATURE_BSP_CONFIG} ${STAGING_KERNEL_DIR}/.kernel-meta/cfg/config-machine-5.10.cfg

	echo "CONFIG_KERNEL_DTB=y" >>${STAGING_KERNEL_DIR}/.kernel-meta/cfg/config-machine-5.10.cfg
	echo "CONFIG_BUILTIN_DTB=y" >>${STAGING_KERNEL_DIR}/.kernel-meta/cfg/config-machine-5.10.cfg
	echo "# CONFIG_MIPS_NO_APPENDED_DTB is not set" >>${STAGING_KERNEL_DIR}/.kernel-meta/cfg/config-machine-5.10.cfg
	echo "CONFIG_MIPS_ELF_APPENDED_DTB=y" >>${STAGING_KERNEL_DIR}/.kernel-meta/cfg/config-machine-5.10.cfg
	echo "# CONFIG_MIPS_RAW_APPENDED_DTB is not set" >>${STAGING_KERNEL_DIR}/.kernel-meta/cfg/config-machine-5.10.cfg
	echo "CONFIG_APPENDED_DTB=${OPENWRT_KERNEL_DTS_PATH}/${MACHINE_DEFAULT_DTB}" >>${STAGING_KERNEL_DIR}/.kernel-meta/cfg/config-machine-5.10.cfg

	patch -p1 <${WORKDIR}/${MACHINE_EXTERNAL_PATCH}

	bbnote "config adjust done"
}


COMPATIBLE_MACHINE = "qemuarm|qemuarmv5|qemuarm64|qemux86|qemuppc|qemuppc64|qemumips|qemumips64|qemux86-64|qemuriscv64|qemuriscv32"
