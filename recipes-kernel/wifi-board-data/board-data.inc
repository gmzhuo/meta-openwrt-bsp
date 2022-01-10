BOARD_DATA_PATH ?= "${D}/lib/firmware/"
BOARD_DATA_FILE ?= "board-xiaomi_ax9000.ipq8074"
SRC_URI = "file://${BOARD_DATA_FILE}"

S = "${WORKDIR}"

do_configure() {
}

FILES:${PN} = "${BOARD_DATA_PATH}/*"

do_install() {
	install -d -m 755 ${D}${BOARD_DATA_PATH}
	install -m 644 ${S}/${BOARD_DATA_FILE} ${D}${BOARD_DATA_PATH}/board-2.bin
}

do_compile() {
}