inherit devicetree
COMPATIBLE_MACHINE += "|ipq807x|hc5761|bananapi_bpi-r64|ax3600|ax9000|ap143"
SRC_URI = " \
	file://dts/ \
	${OPENWRT_SRC_URI};type=openwrt;name=openwrt;destsuffix=openwrt \
	"
SRCREV = "${AUTOREV}"

do_dts_copy() {
	if [ -d ${WORKDIR}/openwrt/target/linux/${KERNEL_FEATURE_BSP_PATH}/dts ]; then
		cp ${WORKDIR}/openwrt/target/linux/${KERNEL_FEATURE_BSP_PATH}/dts/* ${WORKDIR}/dts/ -rf
	fi

	if [ -d ${STAGING_KERNEL_DIR}/arch/${ARCH}/boot/dts/${OPENWRT_KERNEL_DTS_PATH} ]; then
		cp ${STAGING_KERNEL_DIR}/arch/${ARCH}/boot/dts/${OPENWRT_KERNEL_DTS_PATH}/* ${WORKDIR}/dts/ -rf
	fi
}

CONFIG_DTFILE ??= "${MACHINE_DEFAULT_DTB}"
DT_FILES_PATH = "${WORKDIR}/dts/"

python devicetree_do_compile() {
    includes = expand_includes("DT_INCLUDE", d)
    listpath = d.getVar("DT_FILES_PATH")
    for dts in os.listdir(listpath):
        bb.note("handle one dts %s" % (dts))
        dtspath = os.path.join(listpath, dts)
        try:
            if not(os.path.isfile(dtspath)) or not(dts.endswith(".dts") or devicetree_source_is_overlay(dtspath)):
                continue # skip non-.dts files and non-overlay files
        except:
            continue # skip if can't determine if overlay
        try:
            devicetree_compile(dtspath, includes, d)
        except:
            continue
}

addtask do_dts_copy after do_unpack before do_patch
