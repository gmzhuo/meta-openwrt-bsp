
DEPENDS += " script-tools-native"

openwrt_mkits() {
	bbnote "/usr/bin/mkits.sh $@"
	/usr/bin/mkits.sh $@
}
