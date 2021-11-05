# Recipe created by recipetool
# This is the basis of a recipe and may need further editing in order to be fully functional.
# (Feel free to remove these comments when editing.)

# Unable to find any files that looked like license statements. Check the accompanying
# documentation and source headers and set LICENSE and LIC_FILES_CHKSUM accordingly.
#
# NOTE: LICENSE is being set to "CLOSED" to allow you to at least start building - if
# this is not accurate with respect to the licensing of the software being built (it
# will not be in most cases) you must specify the correct value before using this
# recipe for anything other than initial testing/development!
LICENSE = "CLOSED"
LIC_FILES_CHKSUM = ""
inherit pkgconfig binconfig
DEPENDS += "libnl-tiny uci"


SRC_URI = "file://swconfig-${PV}/Makefile \
			file://swconfig-${PV}/cli.c \
			file://swconfig-${PV}/swlib.c \
			file://swconfig-${PV}/swlib.h \
			file://swconfig-${PV}/uci.c \
		"
# NOTE: no Makefile found, unable to determine what needs to be done

do_configure () {
	# Specify any needed configure commands here
	:
}

do_compile () {
	# Specify compilation commands here
	export CFLAGS="${CFLAGS} -I${STAGING_INCDIR}/libnl-tiny "
	oe_runmake -C ${S}/
}

do_install () {
	# Specify install commands here
	install -d ${D}/usr/sbin
	echo ${D}
	install -m 0755 swconfig ${D}/usr/sbin/swconfig
}


FILES:${PN} += "usr/sbin/swconfig"

