# SPDX-License-Identifier: Apache-2.0
#
# Copyright (c) 2020 Arm Limited
#

DESCRIPTION = "Linux Android Common Kernel"
SECTION = "kernel"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://${S}/COPYING;md5=6bc538ed5bd9a7fc9398086aedcd7e46"

require recipes-kernel/linux/linux-yocto.inc
DEPENDS += " lzma-native u-boot-tools-native"

COMPATIBLE_MACHINE ?= "invalid"

S = "${WORKDIR}/git"

LINUX_VERSION ?= "5.10"
KERNEL_VERSION_SANITY_SKIP = "1"
KBRANCH = "linux-5.10.y "
KERNEL_FEATURE_BSP_CONFIG ?= "config-5.10"
OPENWRT_KERNEL_DTS_PATH ?= ""

SRCREV_meta ?= "${AUTOREV}"
KMETA = "kernel-meta"

KERNEL_DEVICETREE:bananapi_bpi-r64 += "mediatek/mt7622-bananapi-bpi-r64.dtb"

KERNEL_FEATURES:remove=" features/debug/printk.scc"

#git://mirrors.tuna.tsinghua.edu.cn/git/linux-stable.git;protocol=https;branch=${KBRANCH}


SRC_URI = " \
	git://git.kernel.org/pub/scm/linux/kernel/git/stable/linux.git;protocol=https;name=first;branch=${KBRANCH} \
	git://github.com/gmzhuo/openwrt-kernel-cache.git;protocol=https;type=kmeta;name=meta;branch=main;destsuffix=${KMETA} \
	${OPENWRT_SRC_URI};type=openwrt;name=openwrt;destsuffix=openwrt \
	file://configs/config-5.10.cfg;name=config-general \
	file://configs/${KERNEL_FEATURE_BSP_CONFIG};name=config-machine \
	file://patches/a00-packet-rx.patch \
    "

SRC_URI += "file://${MACHINE_EXTERNAL_PATCH}"

SRCREV_first = "49b60cb9c8b8eb094c5910b66a099f538f69ddf6"
SRCREV = "${AUTOREV}"

do_openwrt_patch() {


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
		[ -d ${WORKDIR}/openwrt/target/linux/${KERNEL_FEATURE_BSP_PATH}/files/ ] && cp ${WORKDIR}/openwrt/target/linux/${KERNEL_FEATURE_BSP_PATH}/files/* ${S}/ -rfd
		[ -d ${WORKDIR}/openwrt/target/linux/${KERNEL_FEATURE_BSP_PATH}/files-5.10/ ] && cp ${WORKDIR}/openwrt/target/linux/${KERNEL_FEATURE_BSP_PATH}/files-5.10/* ${S}/ -rfd
		for patchpath in ${KERNEL_FEATURE_BSP_PATCH_PATHS}
		do
			patches=$(ls ${WORKDIR}/openwrt/target/linux/${KERNEL_FEATURE_BSP_PATH}/${patchpath}/*)
			for patche in ${patches}
			do
				patch -p1 <${patche}
			done
		done
	fi

	[ -d ${WORKDIR}/openwrt/target/linux/${KERNEL_FEATURE_BSP_PATH}/dts -a -d ${S}/arch/${ARCH}/boot/dts/${OPENWRT_KERNEL_DTS_PATH}/ ] && {
		cp ${WORKDIR}/openwrt/target/linux/${KERNEL_FEATURE_BSP_PATH}/dts/* ${S}/arch/${ARCH}/boot/dts/${OPENWRT_KERNEL_DTS_PATH}/ -rfd
		echo "dtb-y += ${MACHINE_DEFAULT_DTB}" >>${S}/arch/${ARCH}/boot/dts/${OPENWRT_KERNEL_DTS_PATH}/Makefile
	}

	echo "EXTRA_CFLAGS += -I\${srctree}/include/linux -I\${srctree}/include/linux/lzma" >>${S}/lib/lzma/Makefile

	patch -p1 <${WORKDIR}/${MACHINE_EXTERNAL_PATCH}

	bbnote "config adjust done"
}

addtask do_openwrt_patch after do_patch before do_configure


COMPATIBLE_MACHINE += "|ipq807x|hc5761|bananapi_bpi-r64|ax3600|ax9000|ap143|ap1700v2"
