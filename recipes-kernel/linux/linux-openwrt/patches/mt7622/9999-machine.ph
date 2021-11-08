Index: kernel-source/drivers/net/phy/rtk/Makefile
===================================================================
--- kernel-source.orig/drivers/net/phy/rtk/Makefile
+++ kernel-source/drivers/net/phy/rtk/Makefile
@@ -63,4 +63,7 @@ ccflags-y += -Werror -D_LITTLE_ENDIAN -D
 
 ccflags-y += -Idrivers/net/phy/rtk/rtl8367c/include
 ccflags-y += -Iinclude/linux/
+ccflags-y += -I${srctree}/drivers/net/phy/rtk/rtl8367c/include
+ccflags-y += -I${srctree}/include/linux/
+
 
