SUMMARY = "Board data for ath11k"
HOMEPAGE = "https://www.kernel.org/"
DESCRIPTION = "caldata hotplug script"
SECTION = "kernel"

LICENSE = "CLOSED"

LIC_FILES_CHKSUM = ""

SRC_URI = "file://scripts"

S = "${WORKDIR}/"

PV = "1.0"

FILES:${PN} = "/etc/hotplug.d/firmware/* /lib/functions/*"

HOTPLUG_SCRIPTS ??= "'ath11k-caldata 11-ath11k-caldata' "
HOTPLUG_SCRIPTS_DEPENDS ??= ""


do_configure() {
}

do_install_one_file() {
	
}

do_install() {
	install -d -m 755 ${D}/etc/hotplug.d/firmware/
	install -d -m 755 ${D}/lib/functions/

	for HOTPLUG_SCRIPT in ${HOTPLUG_SCRIPTS};
	do
		install -m 755 ${S}/scripts/${HOTPLUG_SCRIPT% *} ${D}/etc/hotplug.d/firmware/${HOTPLUG_SCRIPT##* }
	done

	for HOTPLUG_SCRIPT in ${HOTPLUG_SCRIPTS_DEPENDS};
	do
		install -m 755 ${S}/scripts/${HOTPLUG_SCRIPT} ${D}/lib/functions/
	done
}

do_compile() {
}

# Firmware files are generally not ran on the CPU, so they can be
# allarch despite being architecture specific
INSANE_SKIP = "arch"

PACKAGE_ARCH = "${MACHINE_ARCH}"