# Description string
FIT_DESC ?= "Kernel fitImage for ${DISTRO_NAME}/${PV}/${MACHINE}"

#
# Emit the fitImage ITS header
#
# $1 ... .its filename
fitimage_emit_fit_header() {
	cat << EOF >> $1
/dts-v1/;

/ {
        description = "${FIT_DESC}";
        #address-cells = <1>;
EOF
}

#
# Emit the fitImage section bits
#
# $1 ... .its filename
# $2 ... Section bit type: imagestart - image section start
#                          confstart  - configuration section start
#                          sectend    - section end
#                          fitend     - fitimage end
#
fitimage_emit_section_maint() {
	case $2 in
	imagestart)
		cat << EOF >> $1

        images {
EOF
	;;
	confstart)
		cat << EOF >> $1

        configurations {
EOF
	;;
	sectend)
		cat << EOF >> $1
	};
EOF
	;;
	fitend)
		cat << EOF >> $1
};
EOF
	;;
	esac
}


#
# Emit the fitImage ITS rootfs section
#
# $1 ... .its filename
# $2 ... Image counter
# $3 ... Path to rootfs image
# $4 ... Type for Image
fitimage_emit_section_rootfs() {

	ramdisk_csum="${FIT_HASH_ALG}"
	ramdisk_sign_algo="${FIT_SIGN_ALG}"
	ramdisk_sign_keyname="${UBOOT_SIGN_IMG_KEYNAME}"

	type_for_image=$4
	if [ ! -n "${type_for_image}" ]; then
		type_for_image="ramdisk"
	fi

	cat << EOF >> $1
                rootfs-$2 {
                        description = "${INITRAMFS_IMAGE}";
                        data = /incbin/("$3");
                        type = "${type_for_image}";
                        arch = "${UBOOT_ARCH}";
                        os = "linux";
                        compression = "none";
                        hash@1 {
                            algo = "crc32";
                        };
                        hash@2 {
                            algo = "sha1";
                        };
                };
EOF

	if [ "${UBOOT_SIGN_ENABLE}" = "1" -a "${FIT_SIGN_INDIVIDUAL}" = "1" -a -n "$ramdisk_sign_keyname" ] ; then
		sed -i '$ d' $1
		cat << EOF >> $1
                        signature-1 {
                                algo = "$ramdisk_csum,$ramdisk_sign_algo";
                                key-name-hint = "$ramdisk_sign_keyname";
                        };
                };
EOF
	fi
}

#
# Emit the fitImage ITS configuration section
#
# $1 ... .its filename
# $2 ... Linux kernel ID
# $3 ... DTB image name
# $4 ... ramdisk ID
# $5 ... u-boot script ID
# $6 ... config ID
# $7 ... default flag
# $8 ... load type
fitimage_emit_section_rootfs_config() {

	conf_csum="${FIT_HASH_ALG}"
	conf_sign_algo="${FIT_SIGN_ALG}"
	if [ "${UBOOT_SIGN_ENABLE}" = "1" ] ; then
		conf_sign_keyname="${UBOOT_SIGN_KEYNAME}"
	fi

	its_file="$1"
	kernel_id="$2"
	dtb_image="$3"
	ramdisk_id="$4"
	bootscr_id="$5"
	config_id="$6"
	default_flag="$7"
	load_type="$8"
	load_id="$9"

	# Test if we have any DTBs at all
	sep=""
	conf_desc=""
	conf_node="conf-"
	kernel_line=""
	fdt_line=""
	ramdisk_line=""
	bootscr_line=""
	setup_line=""
	default_line=""

	# conf node name is selected based on dtb ID if it is present,
	# otherwise its selected based on kernel ID
	#if [ -n "$dtb_image" ]; then
	#	conf_node=$conf_node$dtb_image
	#else
	#	conf_node=$conf_node$kernel_id
	#fi
	conf_node="config-${load_id}"

	if [ -n "$kernel_id" ]; then
		conf_desc="Linux kernel"
		sep=", "
		kernel_line="kernel = \"kernel-$kernel_id\";"
	fi

	if [ -n "$dtb_image" ]; then
		conf_desc="$conf_desc${sep}FDT blob"
		sep=", "
		fdt_line="fdt = \"fdt-$dtb_image\";"
	fi

	if [ -n "$ramdisk_id" -a ! "$ramdisk_id" = "0" ]; then
		conf_desc="$conf_desc${sep}rootfs"
		sep=", "
		if [ -n "${load_type}" ]; then
			ramdisk_line="${load_type} = \"rootfs-$ramdisk_id\";"
		else
			ramdisk_line="ramdisk = \"rootfs-$ramdisk_id\";"
		fi
	fi

	if [ -n "$bootscr_id" ]; then
		conf_desc="$conf_desc${sep}u-boot script"
		sep=", "
		bootscr_line="bootscr = \"bootscr-$bootscr_id\";"
	fi

	if [ -n "$config_id" ]; then
		conf_desc="$conf_desc${sep}setup"
		setup_line="setup = \"setup-$config_id\";"
	fi

	if [ "$default_flag" = "1" ]; then
		# default node is selected based on dtb ID if it is present,
		# otherwise its selected based on kernel ID
		#if [ -n "$dtb_image" ]; then
		#	default_line="default = \"conf-$dtb_image\";"
		#else
		#	default_line="default = \"conf-$kernel_id\";"
		#fi
		default_line="default = \"${conf_node}\";"
	fi

	cat << EOF >> $its_file
                $default_line
                $conf_node {
                        description = "$default_flag $conf_desc";
                        $kernel_line
                        $fdt_line
                        $ramdisk_line
                        $bootscr_line
                        $setup_line
EOF

	if [ -n "$conf_sign_keyname" ] ; then

		sign_line="sign-images = "
		sep=""

		if [ -n "$kernel_id" ]; then
			sign_line="$sign_line${sep}\"kernel\""
			sep=", "
		fi

		if [ -n "$dtb_image" ]; then
			sign_line="$sign_line${sep}\"fdt\""
			sep=", "
		fi

		if [ -n "$ramdisk_id" ]; then
			sign_line="$sign_line${sep}\"ramdisk\""
			sep=", "
		fi

		if [ -n "$bootscr_id" ]; then
			sign_line="$sign_line${sep}\"bootscr\""
			sep=", "
		fi

		if [ -n "$config_id" ]; then
			sign_line="$sign_line${sep}\"setup\""
		fi

		sign_line="$sign_line;"

		cat << EOF >> $its_file
                        signature-1 {
                                algo = "$conf_csum,$conf_sign_algo";
                                key-name-hint = "$conf_sign_keyname";
                                $sign_line
                        };
EOF
	fi

	cat << EOF >> $its_file
                };
EOF
}

#
# Emit the fitImage ITS kernel section
#
# $1 ... .its filename
# $2 ... Image counter
# $3 ... Path to kernel image
# $4 ... Compression type
fitimage_emit_section_kernel_extend() {

	kernel_csum="${FIT_HASH_ALG}"
	kernel_sign_algo="${FIT_SIGN_ALG}"
	kernel_sign_keyname="${UBOOT_SIGN_IMG_KEYNAME}"

	ENTRYPOINT="${UBOOT_ENTRYPOINT}"
	if [ -n "${UBOOT_ENTRYSYMBOL}" ]; then
		ENTRYPOINT=`${HOST_PREFIX}nm vmlinux | \
			awk '$3=="${UBOOT_ENTRYSYMBOL}" {print "0x"$1;exit}'`
	fi

	cat << EOF >> $1
                kernel-$2 {
                        description = "Linux kernel";
                        data = /incbin/("$3");
                        type = "kernel";
                        arch = "${UBOOT_ARCH}";
                        os = "linux";
                        compression = "$4";
                        load = <${UBOOT_LOADADDRESS}>;
                        entry = <$ENTRYPOINT>;
						hash@1 {
                            algo = "crc32";
                        };
						hash@2 {
                            algo = "sha1";
                        };
                };
EOF

	if [ "${UBOOT_SIGN_ENABLE}" = "1" -a "${FIT_SIGN_INDIVIDUAL}" = "1" -a -n "$kernel_sign_keyname" ] ; then
		sed -i '$ d' $1
		cat << EOF >> $1
                        signature-1 {
                                algo = "$kernel_csum,$kernel_sign_algo";
                                key-name-hint = "$kernel_sign_keyname";
                        };
                };
EOF
	fi
}

#
# Emit the fitImage ITS DTB section
#
# $1 ... .its filename
# $2 ... Image counter
# $3 ... Path to DTB image
fitimage_emit_section_dtb_extend() {

	dtb_csum="${FIT_HASH_ALG}"
	dtb_sign_algo="${FIT_SIGN_ALG}"
	dtb_sign_keyname="${UBOOT_SIGN_IMG_KEYNAME}"

	dtb_loadline=""
	dtb_ext=${DTB##*.}
	if [ "${dtb_ext}" = "dtbo" ]; then
		if [ -n "${UBOOT_DTBO_LOADADDRESS}" ]; then
			dtb_loadline="load = <${UBOOT_DTBO_LOADADDRESS}>;"
		fi
	elif [ -n "${UBOOT_DTB_LOADADDRESS}" ]; then
		dtb_loadline="load = <${UBOOT_DTB_LOADADDRESS}>;"
	fi
	cat << EOF >> $1
                fdt-$2 {
                        description = "Flattened Device Tree blob";
                        data = /incbin/("$3");
                        type = "flat_dt";
                        arch = "${UBOOT_ARCH}";
                        compression = "none";
                        $dtb_loadline
                        hash@1 {
                            algo = "crc32";
                        };
                        hash@2 {
                            algo = "sha1";
                        };
                };
EOF

	if [ "${UBOOT_SIGN_ENABLE}" = "1" -a "${FIT_SIGN_INDIVIDUAL}" = "1" -a -n "$dtb_sign_keyname" ] ; then
		sed -i '$ d' $1
		cat << EOF >> $1
                        signature-1 {
                                algo = "$dtb_csum,$dtb_sign_algo";
                                key-name-hint = "$dtb_sign_keyname";
                        };
                };
EOF
	fi
}


FIT_IMAGE_ROOTFS_TYPE ?= "cpio.gz"
#
# Assemble fitImage sysupgrade
#
# $1 ... .its filename
# $2 ... fitImage name
# $3 ... include ramdisk
# $4 ... final image
fitimage_assemble_sysupgrade() {
	kernelcount=1
	dtbcount=""
	DTBS=""
	ramdiskcount=$3
	finalimage=$4
	setupcount=""
	bootscr_id=""

	fitimage_emit_fit_header $1

	#
	# Step 1: Prepare a kernel image section.
	#
	fitimage_emit_section_maint $1 imagestart

	#we need kernel image here
	fitimage_emit_section_kernel_extend $1 $kernelcount \
		${WORKDIR}/linux-vmlinux/tmp/firmare.bin "$linux_comp"

	#
	# Step 2: Prepare a DTB image section
	#

	if [ -n "${KERNEL_DEVICETREE}" ]; then
		dtbcount=1
		for DTB in ${KERNEL_DEVICETREE}; do

			[ -f ${DEPLOY_DIR_IMAGE}/devicetree/${DTB} ] && {
				DTB_PATH="${DEPLOY_DIR_IMAGE}/devicetree/${DTB}"
				DTB=$(echo "$DTB" | tr '/' '_')
				DTBS="$DTBS $DTB"
				fitimage_emit_section_dtb_extend $1 $DTB $DTB_PATH
			}
		done
	fi

	if [ -n "${EXTERNAL_KERNEL_DEVICETREE}" ]; then
		dtbcount=1
		for DTB in $(find "${EXTERNAL_KERNEL_DEVICETREE}" \( -name '*.dtb' -o -name '*.dtbo' \) -printf '%P\n' | sort); do
			DTB=$(echo "$DTB" | tr '/' '_')
			DTBS="$DTBS $DTB"
			fitimage_emit_section_dtb_extend $1 $DTB "${EXTERNAL_KERNEL_DEVICETREE}/$DTB"
		done
	fi

	#
	# Step 3: Prepare a u-boot script section
	#

	#
	# Step 4: Prepare a setup section. (For x86)
	#


	#
	# Step 5: Prepare a rootfs section.
	#
	if [ "x${ramdiskcount}" = "x1" ] ; then
		# Find and use the first initramfs image archive type we find
		for img in "${FIT_IMAGE_ROOTFS_TYPE}"; do
			initramfs_path="${IMGDEPLOYDIR}/${INITRAMFS_IMAGE_NAME}.$img"
			echo -n "Searching for $initramfs_path..."
			if [ -e "$initramfs_path" ]; then
				echo "found"
				fitimage_emit_section_rootfs $1 "$ramdiskcount" "$initramfs_path" "${ROOTFS_LOAD_TYPE}"
				break
			else
				echo "not found"
			fi
		done
	fi

	fitimage_emit_section_maint $1 sectend

	# Force the first Kernel and DTB in the default config
	kernelcount=1
	if [ -n "$dtbcount" ]; then
		dtbcount=1
	fi

	#
	# Step 6: Prepare a configurations section
	#
	fitimage_emit_section_maint $1 confstart

	# kernel-fitimage.bbclass currently only supports a single kernel (no less or
	# more) to be added to the FIT image along with 0 or more device trees and
	# 0 or 1 ramdisk.
        # It is also possible to include an initramfs bundle (kernel and rootfs in one binary)
        # When the initramfs bundle is used ramdisk is disabled.
	# If a device tree is to be part of the FIT image, then select
	# the default configuration to be used is based on the dtbcount. If there is
	# no dtb present than select the default configuation to be based on
	# the kernelcount.
	if [ -n "$DTBS" ]; then
		i=1
		for DTB in ${DTBS}; do
			dtb_ext=${DTB##*.}
			if [ "$dtb_ext" = "dtbo" ]; then
				fitimage_emit_section_rootfs_config $1 "" "$DTB" "" "$bootscr_id" "" "`expr $i = $dtbcount`" "${ROOTFS_LOAD_LABLE}" "${i}"
			else
				fitimage_emit_section_rootfs_config $1 $kernelcount "$DTB" "$ramdiskcount" "$bootscr_id" "$setupcount" "`expr $i = $dtbcount`" "${ROOTFS_LOAD_LABLE}" "${i}"
			fi
			i=`expr $i + 1`
		done
	else
		defaultconfigcount=1
		fitimage_emit_section_rootfs_config $1 $kernelcount "" "$ramdiskcount" "$bootscr_id"  "$setupcount" "$defaultconfigcount" "${ROOTFS_LOAD_LABLE}" "1"
	fi

	fitimage_emit_section_maint $1 sectend

	fitimage_emit_section_maint $1 fitend

	#
	# Step 7: Assemble the image
	#
	${UBOOT_MKIMAGE} \
		${@'-D "${UBOOT_MKIMAGE_DTCOPTS}"' if len('${UBOOT_MKIMAGE_DTCOPTS}') else ''} \
		${UBOOT_MKIMAGE_EXTERNAL} \
		-f $1 \
		${finalimage}

	#
	# Step 8: Sign the image and add public key to U-Boot dtb
	#
	if [ "x${UBOOT_SIGN_ENABLE}" = "x1" ] ; then
		add_key_to_u_boot=""
		if [ -n "${UBOOT_DTB_BINARY}" ]; then
			# The u-boot.dtb is a symlink to UBOOT_DTB_IMAGE, so we need copy
			# both of them, and don't dereference the symlink.
			cp -P ${STAGING_DATADIR}/u-boot*.dtb ${B}
			add_key_to_u_boot="-K ${B}/${UBOOT_DTB_BINARY}"
		fi
		${UBOOT_MKIMAGE_SIGN} \
			${@'-D "${UBOOT_MKIMAGE_DTCOPTS}"' if len('${UBOOT_MKIMAGE_DTCOPTS}') else ''} \
			-F -k "${UBOOT_SIGN_KEYDIR}" \
			$add_key_to_u_boot \
			-r ${finalimage} \
			${UBOOT_MKIMAGE_SIGN_ARGS}
	fi
}

