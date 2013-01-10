SUMMARY = "d-bus over android accessory protocol"
DESCRIPTION = "This program allow interfacing from Android to the local d-bus using the android accessory protocol"
HOMEPAGE = "ict.nl"
SECTION = "base"
LICENSE = "Idk"

DEPENDS = "dbus-1, libandroidaccessory-1.0"

# Update checksum if license file changes.
LIC_FILES_CHKSUM = "file://COPYING;md5=4347375aa52ecf07c16943db6168fc19"
PR = "r7"

# Do a "make dist" in the AAP-Bridge/ subdirectory to create the source package.
# Update checksum if package content changes.
SRC_URI = "file://AAP-Bridge/${P}.tar.gz;md5=c11fe47ccc3ce745f5e98a9be5d99f63"

inherit autotools

