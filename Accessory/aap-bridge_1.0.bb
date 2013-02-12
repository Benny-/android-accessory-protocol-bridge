SUMMARY = "d-bus over android accessory protocol"
DESCRIPTION = "This program allow interfacing from Android to the local d-bus using the android accessory protocol"
HOMEPAGE = "ict.nl"
SECTION = "base"
LICENSE = "Idk"

DEPENDS = "dbus-1, libandroidaccessory-1.0"

# Update checksum if license file changes.
LIC_FILES_CHKSUM = "file://COPYING;md5=4347375aa52ecf07c16943db6168fc19"
PR = "r8"

# Do a "make dist" in the AAP-Bridge/ subdirectory to create the source package.
# Update checksum if package content changes.
SRC_URI = "file://AAP-Bridge/${P}.tar.gz;md5=883e33552f13213fb518ed870d0e4c31"

inherit autotools

