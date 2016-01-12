#  Make file for gnonograms-java
#  Copyright (C) 2012  Jeremy Wootten
#
#	This program is free software; you can redistribute it and/or modify
#    it under the terms of the GNU General Public License as published by
#    the Free Software Foundation; either version 2 of the License, or
#    (at your option) any later version.
#
#    This program is distributed in the hope that it will be useful,
#    but WITHOUT ANY WARRANTY; without even the implied warranty of
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#    GNU General Public License for more details.
#
#    You should have received a copy of the GNU General Public License along
#    with this program; if not, write to the Free Software Foundation, Inc.,
#    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
# *
# *  Author:
# * 	Jeremy Wootten <jeremwootten@gmail.com>
PROGRAM=Gnonograms
VERSION=0.9.7-jre6

#Java 1.6 or equivalent recommended.  Code will compile with Java 1.7 but with some warnings
JAVAC=javac 
JAR=jar
TAR=tar

BUILD_DIR=build
SRC_DIR=gnonograms
RESOURCE_DIR=res
IMAGE_DIR=res/images
LOCALE_DIR=res/i18n

SRC_FILES = \
	utils/Cell.java \
	utils/Config.java \
	utils/GameLoader.java \
	utils/GameSaver.java \
	utils/Model.java \
	utils/Move.java \
	utils/MoveList.java \
	utils/My2DCellArray.java \
	utils/Region.java \
	utils/Solver.java \
	utils/Utils.java \
	app/gui/CellGrid.java \
	app/gui/GameEditor.java \
	app/gui/GnonogramLabel.java \
	app/gui/ImageImporter.java \
	app/gui/LabelBox.java \
	app/Controller.java \
	app/Resource.java \
	app/Viewer.java \
	Main.java \

IMAGE_FILES = \
	*.png \
	*.gif \

LOCALE_FILES = \
	*.properties \
	supported_languages.xml
	
TEXT_FILES = \
	Makefile \
	AUTHORS \
	COPYING \
	INSTALL \
	MAINTAINERS \
	NEWS \
	README \
	THANKS

EXPANDED_SRC_FILES = $(foreach src,$(SRC_FILES),$(SRC_DIR)/$(src))
EXPANDED_IMAGE_FILES= $(foreach img, $(IMAGE_FILES), $(IMAGE_DIR)/$(img))
EXPANDED_LOCALE_FILES= $(foreach loc, $(LOCALE_FILES), $(LOCALE_DIR)/$(loc))

DIST_FILES = $(TEXT_FILES)
DIST_FILES+= $(EXPANDED_SRC_FILES) 
DIST_FILES+= $(EXPANDED_IMAGE_FILES)
DIST_FILES+=$(EXPANDED_LOCALE_FILES)

DIST_TAR_GZ = $(PROGRAM)-$(VERSION).tar.gz

#-----------------------------------TARGETS----------------------------------------------
all: $(PROGRAM)
###############
	

$(PROGRAM): $(EXPANDED_SRC_FILES) Makefile
############################################################
	mkdir -p $(BUILD_DIR)
	cp --parents $(EXPANDED_LOCALE_FILES) $(BUILD_DIR)
	cp --parents $(EXPANDED_IMAGE_FILES) $(BUILD_DIR)
	$(JAVAC) -Xlint -Xlint:-serial -d  $(BUILD_DIR) $(EXPANDED_SRC_FILES)

jar: $(PROGRAM)
###############
	$(JAR) cmf Manifest.txt $(PROGRAM)-$(VERSION).jar  -C build .

dist: $(PROGRAM) $(DIST_FILES)
###############################
	mkdir -p $(PROGRAM)-$(VERSION)
	cp --parents $(DIST_FILES) $(PROGRAM)-$(VERSION)
	rm -f $(DIST_TAR_GZ)
	$(TAR) --gzip -cvf $(DIST_TAR_GZ) $(PROGRAM)-$(VERSION)
	rm -rf $(PROGRAM)-$(VERSION)
