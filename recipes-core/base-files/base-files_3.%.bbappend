# Copyright (C) 2021 Guiming Zhuo <18414710@qq.com>

# Released under the MIT license (see COPYING.MIT for the terms)

do_install:append () {
	rm ${D}/etc/iproute2/ -rfd
	install -m 0644 ${WORKDIR}/shells ${D}${sysconfdir}/shells
}
