# This file Copyright (C) 2015 Khem Raj <raj.khem@gmail.com> and
# Copyright (C) 2018 Daniel Dickinson <cshored@thecshore.com>
#
# It is released under the MIT license.  See COPYING.MIT
# for the terms.

SUMMARY = "OpenWrt Minimal Complete Image"

LICENSE = "MIT"
export IMAGE_BASENAME = "${MLPREFIX}openwrt-initramfs"

inherit core-image openwrt openwrt-kmods openwrt-services

CORE_IMAGE_BASE_INSTALL = '\
    packagegroup-core-boot \
    packagegroup-openwrt-minimal-base \
    \
    ${MACHINE_EXTRA_RDEPENDS} \
    ${CORE_IMAGE_EXTRA_INSTALL} \
	initramfs-dev \
     '

IMAGE_INSTALL ?= "${CORE_IMAGE_BASE_INSTALL} swconfig "

IMAGE_FSTYPES += "ext4"

do_rootfs_clean() {
	rm ${WORKDIR}/rootfs/boot/* -rfd
}

OPENWRT_IMAGE_CMDLINES = "kernel_bin , add_dtb , lzma , uImage lzma aa , add_rootfs"

do_openwrt_firmware_kernel_bin() {
	bbnote kernel bin ${1}

	${OBJCOPY} -O binary -R .note -R .comment -S \
		${WORKDIR}/linux-vmlinux/tmp/firmare.bin \
		${WORKDIR}/linux-vmlinux/tmp/firmare.tmp

	mv ${WORKDIR}/linux-vmlinux/tmp/firmare.tmp \
		${WORKDIR}/linux-vmlinux/tmp/firmare.bin
}

do_openwrt_firmware_add_dtb() {
	bbnote append dtb ${1}

	cat ${WORKDIR}/linux-vmlinux/tmp/firmare.bin >${WORKDIR}/linux-vmlinux/tmp/firmare.tmp
	[ -f ${DEPLOY_DIR_IMAGE}/devicetree/${MACHINE_DEFAULT_DTB} ] && {
		cat ${DEPLOY_DIR_IMAGE}/devicetree/${MACHINE_DEFAULT_DTB} \
			>>${WORKDIR}/linux-vmlinux/tmp/firmare.tmp
	}

	mv ${WORKDIR}/linux-vmlinux/tmp/firmare.tmp \
		${WORKDIR}/linux-vmlinux/tmp/firmare.bin
}

do_openwrt_firmware_lzma() {
	bbnote lzma ${1} ${2}

	/usr/bin/lzma-alone e -lc1 -lp2 -pb2 \
		${WORKDIR}/linux-vmlinux/tmp/firmare.bin \
		${WORKDIR}/linux-vmlinux/tmp/firmare.tmp

	mv ${WORKDIR}/linux-vmlinux/tmp/firmare.tmp \
		${WORKDIR}/linux-vmlinux/tmp/firmare.bin
}

do_openwrt_firmware_uImage() {
	bbnote uImage ${1}

	ENTRYPOINT=${UBOOT_ENTRYPOINT}
	if [ -n "${UBOOT_ENTRYSYMBOL}" ]; then
		ENTRYPOINT=`${HOST_PREFIX}nm ${vmlinux_path} | \
			awk '$3=="${UBOOT_ENTRYSYMBOL}" {print "0x"$1;exit}'`
	fi

	uboot-mkimage -A ${UBOOT_ARCH} -O linux -T kernel -C "lzma" \
		-a ${UBOOT_LOADADDRESS} -e $ENTRYPOINT -n \
		"${DISTRO_NAME}/${PV}/${MACHINE}" \
		-d ${WORKDIR}/linux-vmlinux/tmp/firmare.bin \
		${WORKDIR}/linux-vmlinux/tmp/firmare.tmp

	mv ${WORKDIR}/linux-vmlinux/tmp/firmare.tmp \
		${WORKDIR}/linux-vmlinux/tmp/firmare.bin
}

do_openwrt_firmware_add_rootfs() {
	bbnote run add rootfs

	cat ${WORKDIR}/linux-vmlinux/tmp/firmare.bin \
		>${WORKDIR}/linux-vmlinux/tmp/firmare.tmp

	rootfs_paths=$(ls ${IMGDEPLOYDIR}/*.squashfs-xz)

	count="0"
	for rootfs_path in ${rootfs_paths}
	do
		if [ -f ${rootfs_path} -a "${count}" == "0" ]; then
			count="1"
			cat ${rootfs_path} >>${WORKDIR}/linux-vmlinux/tmp/firmare.tmp
		fi
	done

	mv ${WORKDIR}/linux-vmlinux/tmp/firmare.tmp \
		${WORKDIR}/linux-vmlinux/tmp/firmare.bin
}

do_openwrt_firmware_image_one_line() {
	bbnote func $1 parameters $2 p3 $3
	do_openwrt_firmware_${1} $2
}

do_openwrt_firmware_image() {
	mkdir -p ${WORKDIR}/linux-vmlinux/
	rm ${WORKDIR}/linux-vmlinux/* -rf
	mkdir ${WORKDIR}/linux-vmlinux/tmp
	rpm -i -r ${WORKDIR}/linux-vmlinux/ \
		${WORKDIR}/oe-rootfs-repo/rpm/${MACHINE}/kernel-vmlinux*.rpm \
		--ignorearch

	vmlinux_path=$(ls ${WORKDIR}/linux-vmlinux/boot/vmlinux*)

	[ "${JUST_FOR_TEST_FAIL}" == "true" ] && {
		do_openwrt_firmware_kernel_bin
		do_openwrt_firmware_add_dtb
		do_openwrt_firmware_lzma
		do_openwrt_firmware_uImage
		do_openwrt_firmware_add_rootfs
	}

	cp "${vmlinux_path}" ${WORKDIR}/linux-vmlinux/tmp/firmare.bin

	rootfs_cmdlines=""
	cmd_func=""

	for sub_cmdline in ${OPENWRT_IMAGE_CMDLINES}
	do
		if [ "${sub_cmdline}" == "," ]; then
			do_openwrt_firmware_image_one_line "${cmd_func}" "${rootfs_cmdlines}"
			cmd_func=""
			cmd_index=0
		else
			if [ "${cmd_func}" == "" ]; then
				cmd_func="${sub_cmdline}"
			else
				rootfs_cmdlines="${rootfs_cmdlines} ${sub_cmdline}"
			fi
		fi
	done

	do_openwrt_firmware_image_one_line "${cmd_func}" "${rootfs_cmdlines}"

	cp ${WORKDIR}/linux-vmlinux/tmp/firmare.bin \
		${WORKDIR}/linux-vmlinux/openwrt-firmare.bin
}

do_openwrt_firmware_image[depends] += "device-tree:do_deploy"

addtask do_rootfs_clean after do_rootfs before do_image
addtask do_openwrt_firmware_image after do_image before do_deploy



