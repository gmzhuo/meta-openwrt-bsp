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

SRC_URI = "git://github.com/kmilo17pet/quectel-cm.git;branch=main;protocol=https"

# Modify these as desired
PV = "1.0+git${SRCPV}"
SRCREV = "2c623ffc8a1a8a7054b695956eb343e37b04727b"

S = "${WORKDIR}/git"

# NOTE: the following library dependencies are unknown, ignoring: pthread dl rt
#       (this is based on recipes that have previously been built and packaged)

# NOTE: this is a Makefile-only piece of software, so we cannot generate much of the
# recipe automatically - you will need to examine the Makefile yourself and ensure
# that the appropriate arguments are passed in.

do_configure () {
	# Specify any needed configure commands here
	:
}

do_compile () {
	# You will almost certainly need to add additional arguments here
	oe_runmake debug
}

#TO FIX QA Issue: File /usr/sbin/quectel-CM in package quectel-cm doesn't have GNU_HASH (didn't pass LDFLAGS?) [ldflags]
TARGET_CC_ARCH += " ${LDFLAGS}"

do_install () {
	# NOTE: unable to determine what to put here - there is a Makefile but no
	# target named "install", so you will need to define this yourself
	install -d ${D}/usr/sbin
	install -m 0755 quectel-CM ${D}/usr/sbin/quectel-CM
}

#do_package_qa() {
#}

