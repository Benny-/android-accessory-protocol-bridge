SUMMARY = "Android accessory protocol"
DESCRIPTION = "This library allows this computer to be seen as a android accessory using the Android accessory protocol"
HOMEPAGE = "ict.nl"
SECTION = "libs"
LICENSE = "Idk"

# Update checksum if license file changes.
LIC_FILES_CHKSUM = "file://COPYING;md5=4347375aa52ecf07c16943db6168fc19"
PR = "r21"

# Do a "make dist" in the AndroidAccessory/ subdirectory to create the source package.
# This package will be used by bitbake.
# Update checksum if package content changes.
SRC_URI = "file://AndroidAccessory/${P}.tar.gz;md5=6179320a5583ff59bfc8a5589837a4d7"

inherit autotools pkgconfig

