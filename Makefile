VERSION=0.0.2-SNAPSHOT
REVISION=0
NAME=streams-video
BUILD=.build_tmp
DIST=jwall-devel
ARCH=noarch
ZIP_FILE=${NAME}-${VERSION}-${REVISION}.zip
DEB_FILE=${NAME}-${VERSION}-${REVISION}.deb
RPM_FILE=${NAME}-${VERSION}-${REVISION}.${ARCH}.rpm
RELEASE_DIR=releases
RPMBUILD=$(PWD)/.rpmbuild


clean:
	@mvn clean


pre-package:
	echo "Preparing packages build in ${BUILD}"
	mkdir -p ${BUILD}
	mkdir -p ${BUILD}/opt/streams/ext
	mvn -DskipTests=true package
	cp target/${NAME}-${VERSION}.jar ${BUILD}/opt/streams/ext/
#
#	cp stream-api/target/stream-api-${VERSION}.jar ${BUILD}/opt/streams/lib
#	for mod in ${MODULES} ; do \
#		cd $$mod && mvn -DskipTests=true package ; \
#		cd .. ; \
#		cp $$mod/target/dependency/*.jar ${BUILD}/opt/streams/lib/ ; \
#		cp $$mod/target/$$mod-${VERSION}.jar ${BUILD}/opt/streams/lib/ ; \
#	done

zip:	pre-package
	cd ${BUILD} && zip -r ../${RELEASE_DIR}/${NAME}-${VERSION}-${REVISION}.zip . && cd ..
	md5sum ${RELEASE_DIR}/${ZIP_FILE} > ${RELEASE_DIR}/${ZIP_FILE}.md5

deb:	pre-package
	rm -rf ${RELEASE_DIR}
	mkdir -p ${RELEASE_DIR}
	mkdir -p ${BUILD}/DEBIAN
	cp dist/DEBIAN/* ${BUILD}/DEBIAN/
	cat dist/DEBIAN/control | sed -e 's/Version:.*/Version: ${VERSION}-${REVISION}/' > ${BUILD}/DEBIAN/control
	cd ${BUILD} && find opt -type f -exec md5sum {} \; > DEBIAN/md5sums && cd ..
	dpkg -b ${BUILD} ${RELEASE_DIR}/${DEB_FILE}
	md5sum ${RELEASE_DIR}/${DEB_FILE} > ${RELEASE_DIR}/${DEB_FILE}.md5
	rm -rf ${BUILD}
	debsigs --sign=origin --default-key=C5C3953C ${RELEASE_DIR}/${DEB_FILE}

release-deb:
	reprepro --ask-passphrase -b /var/www/download.jwall.org/htdocs/debian includedeb ${DIST} ${RELEASE_DIR}/${DEB_FILE}


unrelease-deb:
	reprepro --ask-passphrase -b /var/www/download.jwall.org/htdocs/debian remove ${DIST} streams



rpm:
	mkdir -p ${RELEASE_DIR}
	mkdir -p ${RPMBUILD}
	mkdir -p ${RPMBUILD}/tmp
	mkdir -p ${RPMBUILD}/RPMS
	mkdir -p ${RPMBUILD}/RPMS/${ARCH}
	mkdir -p ${RPMBUILD}/BUILD
	mkdir -p ${RPMBUILD}/SRPMS
	rm -rf ${RPMBUILD}/BUILD
	mkdir -p ${RPMBUILD}/BUILD
	mkdir -p ${RPMBUILD}/SPECS
	cp dist/streams.spec ${RPMBUILD}/SPECS
	cp -a dist/opt ${RPMBUILD}/BUILD
	mkdir -p ${RPMBUILD}/BUILD/opt/streams/lib
	mvn -DskipTests=true clean install
	rm -rf stream-runner/target/dependency/*
	cd stream-runner && mvn -DskipTests=true dependency:copy-dependencies && cd ..
	cp stream-runner/target/dependency/*.jar ${RPMBUILD}/BUILD/opt/streams/lib/
	find .rpmbuild/BUILD -type f | sed -e s/^\.rpmbuild\\/BUILD// | grep -v DEBIAN > ${RPMBUILD}/BUILD/rpmfiles.list
	rpmbuild --target noarch --sign --define '_topdir ${RPMBUILD}' --define '_version ${VERSION}' --define '_revision ${REVISION}' -bb ${RPMBUILD}/SPECS/streams.spec --buildroot ${RPMBUILD}/BUILD/
	cp ${RPMBUILD}/RPMS/${ARCH}/${RPM_FILE} ${RELEASE_DIR}
	md5sum ${RELEASE_DIR}/${RPM_FILE} > ${RELEASE_DIR}/${RPM_FILE}.md5

release-rpm:
	mkdir -p /var/www/download.jwall.org/htdocs/yum/${DIST}/noarch
	cp ${RELEASE_DIR}/${RPM_FILE} /var/www/download.jwall.org/htdocs/yum/${DIST}/noarch/
	createrepo /var/www/download.jwall.org/htdocs/yum/${DIST}/
