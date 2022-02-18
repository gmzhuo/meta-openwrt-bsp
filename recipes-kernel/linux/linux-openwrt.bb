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
    	git://github.com/gregkh/linux.git;protocol=https;branch=${KBRANCH} \
	git://github.com/gmzhuo/openwrt-kernel-cache.git;protocol=https;type=kmeta;name=meta;branch=main;destsuffix=${KMETA} \
	${OPENWRT_SRC_URI};type=openwrt;name=openwrt;destsuffix=openwrt \
	file://configs/config-5.10.cfg;name=config-general \
	file://configs/${KERNEL_FEATURE_BSP_CONFIG};name=config-machine \
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

do_openwrt_image() {
	vmlinux_path="vmlinux"
	cd ${B}

	bbnote "${KERNEL_IMAGETYPE}"

	[ -n "${vmlinux_path}" ] && {
		for type in ${KERNEL_IMAGETYPE} ;
		do
			bbnote handle type ${type}
			case "${type}" in
			"uImage")
				${OBJCOPY} -O binary -R .note -R .comment -S "${vmlinux_path}" linux.bin.append
				[ -f ${B}/arch/${ARCH}/boot/dts/${OPENWRT_KERNEL_DTS_PATH}/${MACHINE_DEFAULT_DTB} ] && {
					cat ${B}/arch/${ARCH}/boot/dts/${OPENWRT_KERNEL_DTS_PATH}/${MACHINE_DEFAULT_DTB} >>linux.bin.append
				}
				[ -f ${DEPLOY_DIR_IMAGE}/devicetree/${MACHINE_DEFAULT_DTB} ] && {
					cat ${DEPLOY_DIR_IMAGE}/devicetree/${MACHINE_DEFAULT_DTB} >>linux.bin.append
				}
				/usr/bin/lzma-alone e -lc1 -lp2 -pb2 linux.bin.append linux.bin.append.lzma
				ENTRYPOINT=${UBOOT_ENTRYPOINT}
				if [ -n "${UBOOT_ENTRYSYMBOL}" ]; then
					ENTRYPOINT=`${HOST_PREFIX}nm ${B}/vmlinux | \
						awk '$3=="${UBOOT_ENTRYSYMBOL}" {print "0x"$1;exit}'`
				fi

				uboot-mkimage -A ${UBOOT_ARCH} -O linux -T kernel -C "lzma" -a ${UBOOT_LOADADDRESS} -e $ENTRYPOINT -n "${DISTRO_NAME}/${PV}/${MACHINE}" -d linux.bin.append.lzma ${B}/arch/${ARCH}/boot/uImage.dtb.lzma
			;;
			*)
				bbnote "not defined ${type}"
			;;
			esac
		done
	}
}

addtask do_openwrt_patch after do_patch before do_configure
addtask do_openwrt_image after do_bundle_initramfs before do_deploy
do_openwrt_image[depends] += "device-tree:do_deploy"

kernel_do_deploy:append() {
	# Update deploy directory
	[ -f ${B}/arch/${ARCH}/boot/uImage.dtb.lzma ] && \
		install -m 0644 ${B}/arch/${ARCH}/boot/uImage.dtb.lzma \
		"$deployDir/uImage-${INITRAMFS_IMAGE_NAME}-dtb.lzma"
}

COMPATIBLE_MACHINE += "|ipq807x|hc5761|bananapi_bpi-r64|ax3600|ax9000|ap143"
