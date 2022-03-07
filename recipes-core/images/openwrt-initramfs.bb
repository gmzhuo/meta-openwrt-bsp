# This file Copyright (C) 2015 Khem Raj <raj.khem@gmail.com> and
# Copyright (C) 2018 Daniel Dickinson <cshored@thecshore.com>
#
# It is released under the MIT license.  See COPYING.MIT
# for the terms.

SUMMARY = "OpenWrt Minimal Complete Image"

LICENSE = "MIT"
export IMAGE_BASENAME = "${MLPREFIX}openwrt-initramfs"

inherit core-image openwrt openwrt-kmods openwrt-services fitimage-rootfs mkits uboot-config
DEPENDS += " lzma-native u-boot-tools-native mtd-utils-native"

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

OPENWRT_IMAGE_CMDLINES ??= "kernel_bin , add_dtb , lzma , uImage lzma aa , add_rootfs"

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

do_openwrt_firmware_gzip() {
	bbnote gzip ${1} ${2}

	gzip \
		${WORKDIR}/linux-vmlinux/tmp/firmare.bin

	mv ${WORKDIR}/linux-vmlinux/tmp/firmare.bin.gz \
		${WORKDIR}/linux-vmlinux/tmp/firmare.bin
}

RAMDISK_COUNT ??= "1"

OPENWRT_DEVICE_NAME ??= "xiaomi_ax9000"
OPENWRT_DEVICE_DTS_CONFIG ??= "config@hk14"
DEVICE_DTS_OVERLAY ??= ""

do_openwrt_firmware_fit() {
	bbnote fit ${1} ${2}

	ENTRYPOINT=${UBOOT_ENTRYPOINT}
	if [ -n "${UBOOT_ENTRYSYMBOL}" ]; then
		ENTRYPOINT=`${HOST_PREFIX}nm ${vmlinux_path} | \
			awk '$3=="${UBOOT_ENTRYSYMBOL}" {print "0x"$1;exit}'`
	fi

	#for bpi64
	#/opt/work/yocto/openwrt/openwrt/scripts/mkits.sh \
	#	-D bananapi_bpi-r64 -o /opt/work/yocto/openwrt/openwrt/build_dir/target-aarch64_cortex-a53_musl/linux-mediatek_mt7622/tmp/openwrt-mediatek-mt7622-bananapi_bpi-r64-squashfs-sysupgrade.itb.its \
	#	-k /opt/work/yocto/openwrt/openwrt/build_dir/target-aarch64_cortex-a53_musl/linux-mediatek_mt7622/tmp/openwrt-mediatek-mt7622-bananapi_bpi-r64-squashfs-sysupgrade.itb \
	#	-C gzip  -d /opt/work/yocto/openwrt/openwrt/build_dir/target-aarch64_cortex-a53_musl/linux-mediatek_mt7622/image-$(basename /opt/work/yocto/openwrt/openwrt/build_dir/target-aarch64_cortex-a53_musl/linux-mediatek_mt7622/linux-5.10.83/arch/arm64/boot/dts/mediatek/mt7622-bananapi-bpi-r64.dtb) \
	#	-r /opt/work/yocto/openwrt/openwrt/build_dir/target-aarch64_cortex-a53_musl/linux-mediatek_mt7622/root.squashfs  \
	#   -a 0x44000000 -e 0x44000000    \
	#	-O mt7622-bananapi-bpi-r64-pcie1:/opt/work/yocto/openwrt/openwrt/build_dir/target-aarch64_cortex-a53_musl/linux-mediatek_mt7622/image-mt7622-bananapi-bpi-r64-pcie1.dtb  \
	#	-O mt7622-bananapi-bpi-r64-sata:/opt/work/yocto/openwrt/openwrt/build_dir/target-aarch64_cortex-a53_musl/linux-mediatek_mt7622/image-mt7622-bananapi-bpi-r64-sata.dtb \
	#	-c "config-1" -A arm64 -v 5.10.83

	WITH_ROOTFS_ARGS=""
	WITH_DTS_OVERLAY_ARGS=""
	CHECK_WITH_ROOTFS=$(echo $@|grep "with-rootfs")
	bbnote ${CHECK_WITH_ROOTFS}
	if [ -n "${CHECK_WITH_ROOTFS}" ]; then
		rootfs_paths=$(ls ${IMGDEPLOYDIR}/*.squashfs*)
		bbnote get  WITH_ROOTFS_ARGS ${rootfs_paths}

		count="0"

		for rootfs_path in ${rootfs_paths};
		do
			if [ -f ${rootfs_path} -a "${count}" == "0" ]; then
				count="1"

				WITH_ROOTFS_ARGS=" -r ${rootfs_path} "
				bbnote set WITH_ROOTFS_ARGS ${WITH_ROOTFS_ARGS}
			fi
		done
	fi

	for dts in ${DEVICE_DTS_OVERLAY};
	do
		if [ -f ${DEPLOY_DIR_IMAGE}/devicetree/${dts}.dtbo ]; then
			WITH_DTS_OVERLAY_ARGS="${WITH_DTS_OVERLAY_ARGS} -O ${dts}:${DEPLOY_DIR_IMAGE}/devicetree/${dts}.dtbo"
		fi
	done

	openwrt_mkits \
		-D ${OPENWRT_DEVICE_NAME} \
		-o ${WORKDIR}/linux-vmlinux/tmp/openwrt-${OPENWRT_DEVICE_NAME}-fit-uImage.itb.its \
		-k ${WORKDIR}/linux-vmlinux/tmp/firmare.bin \
		-C ${1} -d ${DEPLOY_DIR_IMAGE}/devicetree/${MACHINE_DEFAULT_DTB} \
		${WITH_ROOTFS_ARGS} \
		-a ${UBOOT_LOADADDRESS} -e ${ENTRYPOINT} \
		${WITH_DTS_OVERLAY_ARGS} \
		-c "${OPENWRT_DEVICE_DTS_CONFIG}"  -A ${UBOOT_ARCH} -v "${@oe.utils.read_file('${STAGING_KERNEL_BUILDDIR}/kernel-abiversion')}"

	${UBOOT_MKIMAGE} \
		-f ${WORKDIR}/linux-vmlinux/tmp/openwrt-${OPENWRT_DEVICE_NAME}-fit-uImage.itb.its \
		${WORKDIR}/linux-vmlinux/tmp/firmare.bin.tmp

	mv ${WORKDIR}/linux-vmlinux/tmp/firmare.bin.tmp \
		${WORKDIR}/linux-vmlinux/tmp/firmare.bin
}

OPENWRT_DEVICE_BLOCKSIZE ??= "128"
OPENWRT_DEVICE_PAGESIZE ??= "2048"
do_openwrt_firmware_ubinize() {
	rootfs_paths=$(ls ${IMGDEPLOYDIR}/*.squashfs-xz)

	count="0"
	for rootfs_path in ${rootfs_paths}
	do
		if [ -f ${rootfs_path} -a "${count}" == "0" ]; then
			count="1"


			/usr/bin/sysupgrade-tar.sh \
				--board ${OPENWRT_DEVICE_NAME} \
				--kernel ${WORKDIR}/linux-vmlinux/tmp/firmare.bin \
				--rootfs ${rootfs_path} \
				${WORKDIR}/linux-vmlinux/tmp/openwrt-${OPENWRT_DEVICE_NAME}-nand-sysupgrade.bin

			/usr/bin/ubinize-image.sh  \
				--kernel ${WORKDIR}/linux-vmlinux/tmp/firmare.bin  \
				--rootfs ${rootfs_path} \
				${WORKDIR}/linux-vmlinux/tmp/openwrt-${OPENWRT_DEVICE_NAME}-nand-factory.ubi.tmp \
				-p ${OPENWRT_DEVICE_BLOCKSIZE}KiB -m ${OPENWRT_DEVICE_PAGESIZE}
		fi
	done

	mv ${WORKDIR}/linux-vmlinux/tmp/openwrt-${OPENWRT_DEVICE_NAME}-nand-sysupgrade.bin \
		${WORKDIR}/linux-vmlinux/tmp/firmare.bin
}

do_openwrt_firmware_add_metadata() {
	echo "do nothing"
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
		${WORKDIR}/oe-rootfs-repo/rpm/${MACHINE_ARCH}/kernel-vmlinux*.rpm \
		--ignorearch

	vmlinux_path=$(ls ${WORKDIR}/linux-vmlinux/boot/vmlinux*)

	[ "${JUST_FOR_TEST_FAIL}" == "true" ] && {
		do_openwrt_firmware_kernel_bin
		do_openwrt_firmware_add_dtb
		do_openwrt_firmware_lzma
		do_openwrt_firmware_uImage
		do_openwrt_firmware_add_rootfs
		do_openwrt_firmware_gzip
		do_openwrt_firmware_fit
		do_openwrt_firmware_add_metadata
		do_openwrt_firmware_ubinize
	}

	cp "${vmlinux_path}" ${WORKDIR}/linux-vmlinux/tmp/firmare.bin

	rootfs_cmdlines=""
	cmd_func=""

	bbnote "image chain ${OPENWRT_IMAGE_CMDLINES}"
	for sub_cmdline in ${OPENWRT_IMAGE_CMDLINES};
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
		fi;
	done

	do_openwrt_firmware_image_one_line "${cmd_func}" "${rootfs_cmdlines}"

	cp ${WORKDIR}/linux-vmlinux/tmp/firmare.bin \
		${WORKDIR}/linux-vmlinux/openwrt-firmare.bin
}

do_openwrt_firmware_image[depends] += "device-tree:do_deploy"

addtask do_rootfs_clean after do_rootfs before do_image
addtask do_openwrt_firmware_image after do_image_complete before do_populate_lic_deploy



