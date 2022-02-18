inherit devicetree
COMPATIBLE_MACHINE += "|ipq807x|hc5761|bananapi_bpi-r64|ax3600|ax9000|ap143"
SRC_URI = " \
	file://dts/ \
	${OPENWRT_SRC_URI};type=openwrt;name=openwrt;destsuffix=openwrt \
	"
SRCREV = "${AUTOREV}"

do_dts_copy() {
	cp ${WORKDIR}/openwrt/target/linux/${KERNEL_FEATURE_BSP_PATH}/dts/* ${WORKDIR}/dts/ -rf
}

CONFIG_DTFILE ??= "${MACHINE_DEFAULT_DTB}"
DT_FILES_PATH = "${WORKDIR}/dts/"

addtask do_dts_copy after do_unpack before do_patch
