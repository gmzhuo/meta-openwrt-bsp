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

IMAGE_INSTALL ?= "${CORE_IMAGE_BASE_INSTALL}"

IMAGE_FSTYPES += "ext4"

