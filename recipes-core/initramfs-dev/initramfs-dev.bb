SUMMARY = "Extremely basic live image init script"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"
SRC_URI = "file://init"

PR = "r2"

S = "${WORKDIR}"

do_install() {
        install -m 0755 ${WORKDIR}/init ${D}/init

        # Create device nodes expected by some kernels in initramfs
        # before even executing /init.
        install -d ${D}/dev
		install -d ${D}/dev/pts
        mknod -m 622 ${D}/dev/console c 5 1
		mknod -m 622 ${D}/dev/random c 1 8
		mknod -m 622 ${D}/dev/tty c 5 0
		mknod -m 622 ${D}/dev/tty0 c 4 0
		mknod -m 622 ${D}/dev/tty1 c 4 1
		mknod -m 622 ${D}/dev/urandom c 1 9
		mknod -m 622 ${D}/dev/zero c 1 5
}

inherit allarch

FILES:${PN} += "/dev /init /dev/console"