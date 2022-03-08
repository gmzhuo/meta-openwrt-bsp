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

BBCLASSEXTEND = "native nativesdk"

SRC_URI = "https://sources.openwrt.org/lzma-${PV}.tar.bz2"
SRC_URI[md5sum] = "434e51a018b4c8ef377bf81520a53af0"
SRC_URI[sha1sum] = "3e1c816933ceb1e55370416dd7532271a2a9f02e"
SRC_URI[sha256sum] = "dcbdb5f4843eff638e4a5e8be0e2486a3c5483df73c70823618db8e66f609ec2"
SRC_URI[sha384sum] = "d2f88b1d3569c3e8ee50c41e3ec198e801064d1ffdc71b920a40c7f407d9bea5e902369ce5d21190268409918f77fa4c"
SRC_URI[sha512sum] = "54ffd39d953cf12a91b71009de70218b1850bd0ebe3a201ca9c260cce5d68504c2c8c72d98eff208eeddec53faf0efbd1db72d7d70da90e386f4bc5f28de2757"

# NOTE: no Makefile found, unable to determine what needs to be done

do_configure () {
	# Specify any needed configure commands here
	:
}

do_compile () {
	# Specify compilation commands here
	oe_runmake -C ${S}/CPP/7zip/Compress/LZMA_Alone -f makefile.gcc
}

do_install () {
	# Specify install commands here
	install -d ${D}${bindir}
	echo ${D}
	install -m 0755 CPP/7zip/Compress/LZMA_Alone/lzma ${D}${bindir}/lzma-alone
}


FILES:${PN} += "usr/bin/lzma-alone"

