
DEPENDS += " script-tools-native"

openwrt_mkits() {
	bbnote "/usr/bin/mkits.sh $@"
	mkits.sh $@
}
