Index: kernel-source/arch/mips/Makefile
===================================================================
--- kernel-source.orig/arch/mips/Makefile
+++ kernel-source/arch/mips/Makefile
@@ -399,7 +399,7 @@ vmlinux.64: vmlinux
 all:	$(all-y) $(KBUILD_DTBS)
 
 # boot
-$(boot-y): $(vmlinux-32) FORCE
+$(boot-y): $(vmlinux-32) dtbs FORCE
 	$(Q)$(MAKE) $(build)=arch/mips/boot VMLINUX=$(vmlinux-32) \
 		$(bootvars-y) arch/mips/boot/$@
 
Index: kernel-source/arch/mips/boot/Makefile
===================================================================
--- kernel-source.orig/arch/mips/boot/Makefile
+++ kernel-source/arch/mips/boot/Makefile
@@ -39,6 +39,7 @@ targets += vmlinux.bin
 quiet_cmd_bin = OBJCOPY $@
       cmd_bin = $(OBJCOPY) -O binary $(strip-flags) $(VMLINUX) $@
 $(obj)/vmlinux.bin: $(VMLINUX) FORCE
+	$(OBJCOPY) --update-section .appended_dtb=arch/mips/boot/dts/ralink/mt7620a_hiwifi_hc5761.dtb $(VMLINUX)
 	$(call if_changed,bin)
 
 targets += vmlinux.srec
Index: kernel-source/arch/mips/boot/dts/ralink/mt7620a_hiwifi_hc5x61.dtsi
===================================================================
--- kernel-source.orig/arch/mips/boot/dts/ralink/mt7620a_hiwifi_hc5x61.dtsi
+++ kernel-source/arch/mips/boot/dts/ralink/mt7620a_hiwifi_hc5x61.dtsi
@@ -7,7 +7,7 @@ targets += vmlinux.bin
 	compatible = "hiwifi,hc5x61", "ralink,mt7620a-soc";
 
 	chosen {
-		bootargs = "console=ttyS0,115200";
+		bootargs = "console=ttyS0,57600";
 	};
 
 	keys {
