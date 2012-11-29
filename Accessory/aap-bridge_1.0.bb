SUMMARY = "d-bus over android accessory protocol"
DESCRIPTION = "This program allow interfacing from Android to the local d-bus using the android accessory protocol"
HOMEPAGE = "ict.nl"
LICENSE = "Idk"

DEPENDS = "dbus-1"

# Update checksum if license file changes.
LIC_FILES_CHKSUM = "file://COPYING;md5=4347375aa52ecf07c16943db6168fc19"
PR = "r6"

# Do a "make dist" in the AAP-Bridge/ subdirectory to create the source package.
# Update checksum if package content changes.
SRC_URI = "file://AAP-Bridge/${P}.tar.gz;md5=96ab44542ff919b0104e613ef795e2b2"

inherit autotools

