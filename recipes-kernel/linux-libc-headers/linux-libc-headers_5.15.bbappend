FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI += "\
    file://switch.h \
   "


do_install:append () {
	bbnote "append"
	cp ../switch.h ${D}${exec_prefix}/include/linux/switch.h
	pwd
}