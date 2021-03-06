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

LINUX_VERSION ?= "5.15"
KERNEL_VERSION_SANITY_SKIP = "1"
KBRANCH = "linux-5.15.y "
KERNEL_FEATURE_BSP_CONFIG ?= "config-5.15"
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
	file://configs/config-5.15.cfg;name=config-general \
	file://configs/${KERNEL_FEATURE_BSP_CONFIG};name=config-machine \
    "

SRC_URI += "file://patches/a00-packet-rx.patch "

SRC_URI += "file://${MACHINE_EXTERNAL_PATCH}"

SRCREV_first = "ee039006371a0b1d64d825a59f0eed8627bb3c91"
SRCREV_meta = "145a91de103ca471a4796bc4cb2937b075b6d9d3"
SRCREV_openwrt = "${OPENWRT_SRC_REV}"

do_openwrt_patch() {


	cd ${S}
	bbnote "$PWD"

	cp ${WORKDIR}/openwrt/target/linux/generic/files/* ${S}/ -rfd
	patches=$(ls ${WORKDIR}/openwrt/target/linux/generic/back*5.15/*)
	for patche in ${patches}
	do
		bbnote "to apply $patche in $PWD"
		patch -p1 <${patche}
	done

	patches=$(ls ${WORKDIR}/openwrt/target/linux/generic/pen*5.15/*)
	for patche in ${patches}
	do
		bbnote "to apply $patche in $PWD"
		patch -p1 <${patche}
	done

	patches=$(ls ${WORKDIR}/openwrt/target/linux/generic/hack*5.15/*)
	for patche in ${patches}
	do
		bbnote "to apply $patche in $PWD"
		patch -p1 <${patche}
	done

	if [ ! -z ${KERNEL_FEATURE_BSP_PATH} ]; then
		[ -d ${WORKDIR}/openwrt/target/linux/${KERNEL_FEATURE_BSP_PATH}/files/ ] && cp ${WORKDIR}/openwrt/target/linux/${KERNEL_FEATURE_BSP_PATH}/files/* ${S}/ -rfd
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
	if [ -f ${S}/drivers/net/phy/rtk/Makefile ]; then
		echo "ccflags-y += -I\${srctree}/drivers/net/phy/rtk/rtl8367c/include" >>${S}/drivers/net/phy/rtk/Makefile
		echo "ccflags-y += -I\${srctree}/include/linux/" >>${S}/drivers/net/phy/rtk/Makefile
	fi

	patch -p1 <${WORKDIR}/${MACHINE_EXTERNAL_PATCH}

	bbnote "config adjust done"
}

addtask do_openwrt_patch after do_patch before do_configure


COMPATIBLE_MACHINE += "|ipq807x|hc5761|bananapi_bpi-r64|ax3600|ax9000|ap143|ap1700v2"
