SUMMARY = "board data for ax9000 ipq8074"
HOMEPAGE = "https://www.kernel.org/"
DESCRIPTION = "board data for ax9000 ipq8074"
SECTION = "kernel"

LICENSE = "CLOSED"

LIC_FILES_CHKSUM = ""

SRC_URI = "file://board-xiaomi_ax9000.ipq8074"
BOARD_DATA_PATH = "/lib/firmware/ath11k/IPQ8074/hw2.0"
BOARD_DATA_FILE = "board-xiaomi_ax9000.ipq8074"

include board-data.inc

PV = "1.0"
