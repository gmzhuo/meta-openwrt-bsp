SUMMARY = "Example of how to build an external Linux kernel module"
DESCRIPTION = "${SUMMARY}"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=6bc538ed5bd9a7fc9398086aedcd7e46"

KERNEL_SPLIT_MODULES = "1"
inherit module
DEPENDS += "flex-native"
MAC80211_PACKAGE_CONFIGS ?= ""

SRC_URI = "https://cdn.kernel.org/pub/linux/kernel/projects/backports/stable/v5.15.8/backports-5.15.8-1.tar.xz \
		  file://test.cfg \
          "

include include/build.inc include/subsys.inc include/ath.inc include/ath5k.inc  include/ath9k.inc \
	include/ath10k.inc include/ath11k.inc include/rt2x00.inc include/brcm.inc include/mwl.inc

SRC_URI[sha256sum] = "9f71b659c034f19d156532ec780fcb606cee3c4ccc42e2f8ef18dd3e9f1b6820"

S = "${WORKDIR}/backports-5.15.8-1"

# The inherit of module.bbclass will automatically name module packages with
# "kernel-module-" prefix as required by the oe-core build environment.

module_do_compile() {
	unset CFLAGS CPPFLAGS CXXFLAGS LDFLAGS

	oe_runmake CROSS_COMPILE="${CROSS_COMPILE}" ARCH="${ARCH}"  \
		KLIB_BUILD="${STAGING_KERNEL_BUILDDIR}" \
		MODPROBE=true KLIB="${D}" modules
}

module_do_install() {
	unset CFLAGS CPPFLAGS CXXFLAGS LDFLAGS

	oe_runmake CROSS_COMPILE="${CROSS_COMPILE}" ARCH="${ARCH}"  \
		KLIB_BUILD="${STAGING_KERNEL_BUILDDIR}" \
		MODPROBE=true KLIB="${D}" modules_install

	if [ ! -e "${B}/${MODULES_MODULE_SYMVERS_LOCATION}/Module.symvers" ] ; then
		bbwarn "Module.symvers not found in ${B}/${MODULES_MODULE_SYMVERS_LOCATION}"
		bbwarn "Please consider setting MODULES_MODULE_SYMVERS_LOCATION to a"
		bbwarn "directory below B to get correct inter-module dependencies"
	else
		install -Dm0644 "${B}/${MODULES_MODULE_SYMVERS_LOCATION}"/Module.symvers ${D}${includedir}/${BPN}/Module.symvers
		# Module.symvers contains absolute path to the build directory.
		# While it doesn't actually seem to matter which path is specified,
		# clear them out to avoid confusion
		sed -e 's:${B}/::g' -i ${D}${includedir}/${BPN}/Module.symvers
	fi
}

python split_kernel_module_packages () {
    import re

    modinfoexp = re.compile("([^=]+)=(.*)")

    def extract_modinfo(file):
        import tempfile, subprocess
        tempfile.tempdir = d.getVar("WORKDIR")
        compressed = re.match( r'.*\.(gz|xz|zst)$', file)
        tf = tempfile.mkstemp()
        tmpfile = tf[1]
        if compressed:
            tmpkofile = tmpfile + ".ko"
            if compressed.group(1) == 'gz':
                cmd = "gunzip -dc %s > %s" % (file, tmpkofile)
                subprocess.check_call(cmd, shell=True)
            elif compressed.group(1) == 'xz':
                cmd = "xz -dc %s > %s" % (file, tmpkofile)
                subprocess.check_call(cmd, shell=True)
            elif compressed.group(1) == 'zst':
                cmd = "zstd -dc %s > %s" % (file, tmpkofile)
                subprocess.check_call(cmd, shell=True)
            else:
                msg = "Cannot decompress '%s'" % file
                raise msg
            cmd = "%sobjcopy -j .modinfo -O binary %s %s" % (d.getVar("HOST_PREFIX") or "", tmpkofile, tmpfile)
        else:
            cmd = "%sobjcopy -j .modinfo -O binary %s %s" % (d.getVar("HOST_PREFIX") or "", file, tmpfile)
        subprocess.check_call(cmd, shell=True)
        # errors='replace': Some old kernel versions contain invalid utf-8 characters in mod descriptions (like 0xf6, 'รถ')
        f = open(tmpfile, errors='replace')
        l = f.read().split("\000")
        f.close()
        os.close(tf[0])
        os.unlink(tmpfile)
        if compressed:
            os.unlink(tmpkofile)
        vals = {}
        for i in l:
            m = modinfoexp.match(i)
            if not m:
                continue
            vals[m.group(1)] = m.group(2)
        return vals

    def frob_metadata(file, pkg, pattern, format, basename):
        vals = extract_modinfo(file)

        dvar = d.getVar('PKGD')

        bb.warn("debug for split")
        # If autoloading is requested, output /etc/modules-load.d/<name>.conf and append
        # appropriate modprobe commands to the postinst
        autoloadlist = (d.getVar("KERNEL_MODULE_AUTOLOAD") or "").split()
        bb.warn("KERNEL_MODULE_AUTOLOAD %s basename %s" % (d.getVar("KERNEL_MODULE_AUTOLOAD"), basename))
        autoload = d.getVar('module_autoload_%s' % basename)
        if autoload and autoload == basename:
            bb.warn("module_autoload_%s was replaced by KERNEL_MODULE_AUTOLOAD for cases where basename == module name, please drop it" % basename)
        if autoload and basename not in autoloadlist:
            bb.warn("module_autoload_%s is defined but '%s' isn't included in KERNEL_MODULE_AUTOLOAD, please add it there" % (basename, basename))
        if basename in autoloadlist:
            bb.warn("try base name %s" % (basename))
            name = '%s/etc/modules-load.d/%s.conf' % (dvar, basename)
            f = open(name, 'w')
            if autoload:
                for m in autoload.split():
                    f.write('%s\n' % m)
            else:
                f.write('%s\n' % basename)
            f.close()
            postinst = d.getVar('pkg_postinst:%s' % pkg)
            if not postinst:
                bb.fatal("pkg_postinst:%s not defined" % pkg)
            postinst += d.getVar('autoload_postinst_fragment') % (autoload or basename)
            d.setVar('pkg_postinst:%s' % pkg, postinst)

        # Write out any modconf fragment
        modconflist = (d.getVar("KERNEL_MODULE_PROBECONF") or "").split()
        modconf = d.getVar('module_conf_%s' % basename)
        if modconf and basename in modconflist:
            name = '%s/etc/modprobe.d/%s.conf' % (dvar, basename)
            f = open(name, 'w')
            f.write("%s\n" % modconf)
            f.close()
        elif modconf:
            bb.error("Please ensure module %s is listed in KERNEL_MODULE_PROBECONF since module_conf_%s is set" % (basename, basename))

        files = d.getVar('FILES:%s' % pkg)
        files = "%s /etc/modules-load.d/%s.conf /etc/modprobe.d/%s.conf" % (files, basename, basename)
        d.setVar('FILES:%s' % pkg, files)

        conffiles = d.getVar('CONFFILES:%s' % pkg)
        conffiles = "%s /etc/modules-load.d/%s.conf /etc/modprobe.d/%s.conf" % (conffiles, basename, basename)
        d.setVar('CONFFILES:%s' % pkg, conffiles)

        if "description" in vals:
            old_desc = d.getVar('DESCRIPTION:' + pkg) or ""
            d.setVar('DESCRIPTION:' + pkg, old_desc + "; " + vals["description"])

        rdepends = bb.utils.explode_dep_versions2(d.getVar('RDEPENDS:' + pkg) or "")
        modinfo_deps = []
        if "depends" in vals and vals["depends"] != "":
            for dep in vals["depends"].split(","):
                on = legitimize_package_name(dep)
                dependency_pkg = format % on
                modinfo_deps.append(dependency_pkg)
        for dep in modinfo_deps:
            if not dep in rdepends:
                rdepends[dep] = []
        d.setVar('RDEPENDS:' + pkg, bb.utils.join_deps(rdepends, commasep=False))

        # Avoid automatic -dev recommendations for modules ending with -dev.
        d.setVarFlag('RRECOMMENDS:' + pkg, 'nodeprrecs', 1)

        # Provide virtual package without postfix
        providevirt = d.getVar('KERNEL_MODULE_PROVIDE_VIRTUAL')
        if providevirt == "1":
           postfix = format.split('%s')[1]
           d.setVar('RPROVIDES:' + pkg, pkg.replace(postfix, ''))

    kernel_package_name = d.getVar("KERNEL_PACKAGE_NAME") or "kernel"
    kernel_version = d.getVar("KERNEL_VERSION")

    metapkg = d.getVar('KERNEL_MODULES_META_PACKAGE')
    splitmods = d.getVar('KERNEL_SPLIT_MODULES')
    postinst = d.getVar('pkg_postinst:modules')
    postrm = d.getVar('pkg_postrm:modules')

    if splitmods != '1':
        etcdir = d.getVar('sysconfdir')
        d.appendVar('FILES:' + metapkg, '%s/modules-load.d/ %s/modprobe.d/ %s/modules/' % (etcdir, etcdir, d.getVar("nonarch_base_libdir")))
        d.appendVar('pkg_postinst:%s' % metapkg, postinst)
        d.prependVar('pkg_postrm:%s' % metapkg, postrm);
        return

    module_regex = r'^(.*)\.k?o(?:\.(gz|xz|zst))?$'

    module_pattern_prefix = d.getVar('KERNEL_MODULE_PACKAGE_PREFIX')
    module_pattern_suffix = d.getVar('KERNEL_MODULE_PACKAGE_SUFFIX')
    module_pattern = module_pattern_prefix + kernel_package_name + '-module-%s' + module_pattern_suffix

    modules = do_split_packages(d, root='${nonarch_base_libdir}/modules', file_regex=module_regex, output_pattern=module_pattern, description='%s kernel module', postinst=postinst, postrm=postrm, recursive=True, hook=frob_metadata, extra_depends='%s-%s' % (kernel_package_name, kernel_version))
    if modules:
        d.appendVar('RDEPENDS:' + metapkg, ' '+' '.join(modules))

    # If modules-load.d and modprobe.d are empty at this point, remove them to
    # avoid warnings. removedirs only raises an OSError if an empty
    # directory cannot be removed.
    dvar = d.getVar('PKGD')
    for dir in ["%s/etc/modprobe.d" % (dvar), "%s/etc/modules-load.d" % (dvar), "%s/etc" % (dvar)]:
        if len(os.listdir(dir)) == 0:
            os.rmdir(dir)
}

inherit cml1

do_configure() {
	rm -rf \
		include/linux/ssb \
		include/linux/bcma \
		include/net/bluetooth

	rm -f \
		include/linux/cordic.h \
		include/linux/crc8.h \
		include/linux/eeprom_93cx6.h \
		include/linux/wl12xx.h \
		include/linux/spi/libertas_spi.h \
		include/net/ieee80211.h \
		backport-include/linux/bcm47xx_nvram.h

	set -x
	>.config

	for config in ${MAC80211_PACKAGE_CONFIGS}; do
		echo $config >> .config
	done
	cp .config CONFIG.SEC

	#merge_config.sh -m .config ${@" ".join(find_cfgs(d))}

	oe_runmake CROSS_COMPILE="${CROSS_COMPILE}" ARCH="${ARCH}"  \
		KLIB_BUILD="${STAGING_KERNEL_BUILDDIR}" \
		MODPROBE=true KLIB="${D}" CC=gcc LEX=flex allnoconfig 
}

RPROVIDES:${PN} += "kernel-module-mac80211"
