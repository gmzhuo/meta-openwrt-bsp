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

LINUX_VERSION ?= "5.10"
KERNEL_VERSION_SANITY_SKIP = "1"
KBRANCH = "linux-5.10.y "
KERNEL_FEATURE_BSP_CONFIG ?= "config-5.10"
OPENWRT_KERNEL_DTS_PATH ?= ""

SRCREV_meta ?= "59dcdb2de6e073544ef766ca8e5b436fd82f7ffb"
KMETA = "kernel-meta"


SRC_URI = " \
    git://mirrors.tuna.tsinghua.edu.cn/git/linux-stable.git;protocol=https;branch=${KBRANCH} \
	git://github.com/gmzhuo/openwrt-kernel-cache.git;protocol=https;type=kmeta;name=meta;branch=main;destsuffix=${KMETA} \
	${OPENWRT_SRC_URI};type=openwrt;name=openwrt;destsuffix=openwrt \
	file://configs/config-5.10.cfg;name=config-general \
	file://configs/config-mt7620-5.10.cfg;name=config-machine \
    "

SRC_URI += "file://${MACHINE_EXTERNAL_PATCH}"

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

	patch -p1 <${WORKDIR}/${MACHINE_EXTERNAL_PATCH}

	bbnote "config adjust done"
}


addtask do_openwrt_patch after do_patch before do_configure

COMPATIBLE_MACHINE += "|hc5761"
