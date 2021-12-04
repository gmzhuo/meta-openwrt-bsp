inherit kernel-fitimage

#
# Emit the fitImage ITS rootfs section
#
# $1 ... .its filename
# $2 ... Image counter
# $3 ... Path to rootfs image
fitimage_emit_section_rootfs() {

	ramdisk_csum="${FIT_HASH_ALG}"
	ramdisk_sign_algo="${FIT_SIGN_ALG}"
	ramdisk_sign_keyname="${UBOOT_SIGN_IMG_KEYNAME}"

	cat << EOF >> $1
                rootfs-$2 {
                        description = "${INITRAMFS_IMAGE}";
                        data = /incbin/("$3");
                        type = "filesystem";
                        arch = "${UBOOT_ARCH}";
                        os = "linux";
                        compression = "none";
                        hash-1 {
                                algo = "$ramdisk_csum";
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
	if [ -n "$dtb_image" ]; then
		conf_node=$conf_node$dtb_image
	else
		conf_node=$conf_node$kernel_id
	fi

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

	if [ -n "$ramdisk_id" ]; then
		conf_desc="$conf_desc${sep}rootfs"
		sep=", "
		ramdisk_line="loadables = \"rootfs-$ramdisk_id\";"
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
		if [ -n "$dtb_image" ]; then
			default_line="default = \"conf-$dtb_image\";"
		else
			default_line="default = \"conf-$kernel_id\";"
		fi
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
                        hash-1 {
                                algo = "$conf_csum";
                        };
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
# Assemble fitImage sysupgrade
#
# $1 ... .its filename
# $2 ... fitImage name
# $3 ... include ramdisk
fitimage_assemble_sysupgrade_bbb() {
	kernelcount=1
	dtbcount=""
	DTBS=""
	ramdiskcount=$3
	setupcount=""
	bootscr_id=""
	rm -f $1 arch/${ARCH}/boot/$2

	if [ -n "${UBOOT_SIGN_IMG_KEYNAME}" -a "${UBOOT_SIGN_KEYNAME}" = "${UBOOT_SIGN_IMG_KEYNAME}" ]; then
		bbfatal "Keys used to sign images and configuration nodes must be different."
	fi

	fitimage_emit_fit_header $1

	#
	# Step 1: Prepare a kernel image section.
	#
	fitimage_emit_section_maint $1 imagestart

	uboot_prep_kimage
	fitimage_emit_section_kernel $1 $kernelcount linux.bin "$linux_comp"

	#
	# Step 2: Prepare a DTB image section
	#

	if [ -n "${KERNEL_DEVICETREE}" ]; then
		dtbcount=1
		for DTB in ${KERNEL_DEVICETREE}; do
			if echo $DTB | grep -q '/dts/'; then
				bbwarn "$DTB contains the full path to the the dts file, but only the dtb name should be used."
				DTB=`basename $DTB | sed 's,\.dts$,.dtb,g'`
			fi

			# Skip ${DTB} if it's also provided in ${EXTERNAL_KERNEL_DEVICETREE}
			if [ -n "${EXTERNAL_KERNEL_DEVICETREE}" ] && [ -s ${EXTERNAL_KERNEL_DEVICETREE}/${DTB} ]; then
				continue
			fi

			DTB_PATH="arch/${ARCH}/boot/dts/$DTB"
			if [ ! -e "$DTB_PATH" ]; then
				DTB_PATH="arch/${ARCH}/boot/$DTB"
			fi

			DTB=$(echo "$DTB" | tr '/' '_')
			DTBS="$DTBS $DTB"
			fitimage_emit_section_dtb $1 $DTB $DTB_PATH
		done
	fi

	if [ -n "${EXTERNAL_KERNEL_DEVICETREE}" ]; then
		dtbcount=1
		for DTB in $(find "${EXTERNAL_KERNEL_DEVICETREE}" \( -name '*.dtb' -o -name '*.dtbo' \) -printf '%P\n' | sort); do
			DTB=$(echo "$DTB" | tr '/' '_')
			DTBS="$DTBS $DTB"
			fitimage_emit_section_dtb $1 $DTB "${EXTERNAL_KERNEL_DEVICETREE}/$DTB"
		done
	fi

	#
	# Step 3: Prepare a u-boot script section
	#

	if [ -n "${UBOOT_ENV}" ] && [ -d "${STAGING_DIR_HOST}/boot" ]; then
		if [ -e "${STAGING_DIR_HOST}/boot/${UBOOT_ENV_BINARY}" ]; then
			cp ${STAGING_DIR_HOST}/boot/${UBOOT_ENV_BINARY} ${B}
			bootscr_id="${UBOOT_ENV_BINARY}"
			fitimage_emit_section_boot_script $1 "$bootscr_id" ${UBOOT_ENV_BINARY}
		else
			bbwarn "${STAGING_DIR_HOST}/boot/${UBOOT_ENV_BINARY} not found."
		fi
	fi

	#
	# Step 4: Prepare a setup section. (For x86)
	#
	if [ -e arch/${ARCH}/boot/setup.bin ]; then
		setupcount=1
		fitimage_emit_section_setup $1 $setupcount arch/${ARCH}/boot/setup.bin
	fi

	#
	# Step 5: Prepare a rootfs section.
	#
	if [ "x${ramdiskcount}" = "x1" ] ; then
		# Find and use the first initramfs image archive type we find
		for img in squashfs-xz; do
			initramfs_path="${DEPLOY_DIR_IMAGE}/${INITRAMFS_IMAGE_NAME}.$img"
			echo -n "Searching for $initramfs_path..."
			if [ -e "$initramfs_path" ]; then
				echo "found"
				fitimage_emit_section_rootfs $1 "$ramdiskcount" "$initramfs_path"
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
				fitimage_emit_section_rootfs_config $1 "" "$DTB" "" "$bootscr_id" "" "`expr $i = $dtbcount`"
			else
				fitimage_emit_section_rootfs_config $1 $kernelcount "$DTB" "$ramdiskcount" "$bootscr_id" "$setupcount" "`expr $i = $dtbcount`"
			fi
			i=`expr $i + 1`
		done
	else
		defaultconfigcount=1
		fitimage_emit_section_rootfs_config $1 $kernelcount "" "$ramdiskcount" "$bootscr_id"  "$setupcount" $defaultconfigcount
	fi

	fitimage_emit_section_maint $1 sectend

	fitimage_emit_section_maint $1 fitend

	#
	# Step 7: Assemble the image
	#
	${UBOOT_MKIMAGE} \
		${@'-D "${UBOOT_MKIMAGE_DTCOPTS}"' if len('${UBOOT_MKIMAGE_DTCOPTS}') else ''} \
		-f $1 \
		arch/${ARCH}/boot/$2

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
			-r arch/${ARCH}/boot/$2 \
			${UBOOT_MKIMAGE_SIGN_ARGS}
	fi
}

fitimage_assemble_sysupgrade() {
	kernelcount=1
	dtbcount=""
	DTBS=""
	its_file="$1"
	ramdiskcount=$3
	setupcount=""
	bootscr_id=""
	rm -f $1 arch/${ARCH}/boot/$2

	if [ -n "${UBOOT_SIGN_IMG_KEYNAME}" -a "${UBOOT_SIGN_KEYNAME}" = "${UBOOT_SIGN_IMG_KEYNAME}" ]; then
		bbfatal "Keys used to sign images and configuration nodes must be different."
	fi

	uboot_prep_kimage

#head
	cat << EOF >> $its_file
/dts-v1/;

/ {
	description = "ARM64 OpenWrt FIT (Flattened Image Tree)";
	#address-cells = <1>;
EOF

#images
	cat << EOF >> $its_file
	images {
EOF

#image items

	cat << EOF >> $its_file
		kernel-1 {
			description = "ARM64 OpenWrt Linux-5.10.78";
			data = /incbin/("linux.bin");
			type = "kernel";
			arch = "arm64";
			os = "linux";
			compression = "gzip";
			load = <0x44000000>;
			entry = <0x44000000>;
			hash@1 {
				algo = "crc32";
			};
			hash@2 {
				algo = "sha1";
			};
		};


		fdt-1 {
			description = "ARM64 OpenWrt bananapi_bpi-r64 device tree blob";
			
			data = /incbin/("arch/arm64/boot/dts/mediatek/mt7622-bananapi-bpi-r64.dtb");
			type = "flat_dt";
			load = <0x43ff82b6>;
			arch = "arm64";
			compression = "none";
			hash@1 {
				algo = "crc32";
			};
			hash@2 {
				algo = "sha1";
			};
		};



		fdt-mt7622-bananapi-bpi-r64-pcie1 {
			description = "ARM64 OpenWrt bananapi_bpi-r64 device tree overlay mt7622-bananapi-bpi-r64-pcie1";
			
			data = /incbin/("arch/arm64/boot/dts/mediatek/mt7622-bananapi-bpi-r64-pcie1.dtbo");
			type = "flat_dt";
			arch = "arm64";
			load = <0x43ff819b>;
			compression = "none";
			hash@1 {
				algo = "crc32";
			};
			hash@2 {
				algo = "sha1";
			};
		};


		fdt-mt7622-bananapi-bpi-r64-sata {
			description = "ARM64 OpenWrt bananapi_bpi-r64 device tree overlay mt7622-bananapi-bpi-r64-sata";
			
			data = /incbin/("arch/arm64/boot/dts/mediatek/mt7622-bananapi-bpi-r64-sata.dtbo");
			type = "flat_dt";
			arch = "arm64";
			load = <0x43ff7f8b>;
			compression = "none";
			hash@1 {
				algo = "crc32";
			};
			hash@2 {
				algo = "sha1";
			};
		};


		rootfs-1 {
			description = "ARM64 OpenWrt bananapi_bpi-r64 rootfs";
			
			data = /incbin/("/opt/work/yocto/repo/build/tmp/deploy/images/bananapi_bpi-r64/openwrt-initramfs-bananapi_bpi-r64.squashfs-xz");
			type = "filesystem";
			arch = "arm64";
			compression = "none";
			hash@1 {
				algo = "crc32";
			};
			hash@2 {
				algo = "sha1";
			};
		};

EOF


#images end
	cat << EOF >> $its_file
	};
EOF

#configs
	cat << EOF >> $its_file
	configurations {
		default = "config-1";

EOF

#config items
	cat << EOF >> $its_file
		config-1 {
			description = "OpenWrt bananapi_bpi-r64";
			kernel = "kernel-1";
			fdt = "fdt-1";
			loadables = "rootfs-1";
			
			
		};
		

		config-mt7622-bananapi-bpi-r64-pcie1 {
			description = "OpenWrt bananapi_bpi-r64 with mt7622-bananapi-bpi-r64-pcie1";
			kernel = "kernel-1";
			fdt = "fdt-1", "fdt-mt7622-bananapi-bpi-r64-pcie1";
			loadables = "rootfs-1";
			
			
		};
	

		config-mt7622-bananapi-bpi-r64-sata {
			description = "OpenWrt bananapi_bpi-r64 with mt7622-bananapi-bpi-r64-sata";
			kernel = "kernel-1";
			fdt = "fdt-1", "fdt-mt7622-bananapi-bpi-r64-sata";
			loadables = "rootfs-1";
			
			
		};
EOF

#configs end
	cat << EOF >> $its_file
	};
EOF

#end
	cat << EOF >> $its_file
};
EOF

	#
	# Step 7: Assemble the image
	#
	${UBOOT_MKIMAGE} \
		-E -B 0x1000 -p 0x1000 ${@'-D "${UBOOT_MKIMAGE_DTCOPTS}"' if len('${UBOOT_MKIMAGE_DTCOPTS}') else ''} \
		-f $1 \
		arch/${ARCH}/boot/$2

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
			-r arch/${ARCH}/boot/$2 \
			${UBOOT_MKIMAGE_SIGN_ARGS}
	fi
}

do_assemble_fitimage_sysupgrade() {
	if echo ${KERNEL_IMAGETYPES} | grep -wq "fitImage" && \
		test -n "${INITRAMFS_IMAGE}" ; then
		cd ${B}
		fitimage_assemble_sysupgrade fit-image-sysupgrade.its fitImage-${INITRAMFS_IMAGE}-sysupgrade 1
	fi
}

kernel_do_deploy:append() {
	# Update deploy directory
	[ -f ${B}/arch/${ARCH}/boot/fitImage-${INITRAMFS_IMAGE}-sysupgrade ] && install -m 0644 ${B}/arch/${ARCH}/boot/fitImage-${INITRAMFS_IMAGE}-sysupgrade "$deployDir/fitImage-${INITRAMFS_IMAGE_NAME}-${KERNEL_FIT_NAME}-sysupgrade.bin"
}

addtask assemble_fitimage_sysupgrade before do_deploy after do_bundle_initramfs