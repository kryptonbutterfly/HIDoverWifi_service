#!/bin/bash

export SUDO_ASKPASS="/opt/custom_commands/zenityPw.sh"

if [ "$(id -u)" != "0" ]; then
	exec sudo "$0" "$@"
fi

cd ./deb-content
echo ${pwd}

VERSION_FILE="version.txt"

APP_NAME="HIDoverWifi"
ARTIFACT_NAME="hid-over-wifi"
PACKAGE_NAME=$APP_NAME
PACKAGE_VERSION=$(<$VERSION_FILE) 
SOURCE_DIR=$PWD/..
DEPLOY_DIR="/home/$SUDO_USER/ownCloud/Applications"
BUILD_DIR="/tmp/debian"

mkdir -p $BUILD_DIR/DEBIAN
mkdir -p $BUILD_DIR/usr/share/$PACKAGE_NAME
mkdir -p $BUILD_DIR/usr/share/doc/$PACKAGE_NAME
mkdir -p $BUILD_DIR/usr/share/common-licenses/$PACKAGE_NAME
mkdir -p $BUILD_DIR/usr/local/bin/
mkdir -p $BUILD_DIR/etc/systemd/system/
mkdir -p $BUILD_DIR/etc/$PACKAGE_NAME

echo "Package: $PACKAGE_NAME" > $BUILD_DIR/DEBIAN/control
echo "Version: $PACKAGE_VERSION" >> $BUILD_DIR/DEBIAN/control
cat control >> $BUILD_DIR/DEBIAN/control

function createCLIScript() {
SCRIPT_NAME=$(echo "$APP_NAME" | tr '[:upper:]' '[:lower:]')
CLI=$BUILD_DIR/usr/local/bin/$SCRIPT_NAME
echo "#!/bin/bash
TARGET=/etc/$PACKAGE_NAME
mkdir -p \$TARGET
cd \$TARGET
sudo java -jar /usr/share/$APP_NAME/$APP_NAME.jar
" > $CLI
chmod 0755 $CLI
}

function createPostInst() {
POST_INST=$BUILD_DIR/DEBIAN/postinst
echo "#!/bin/sh

SERVICE_FOLDER=/etc/systemd/system/
SERVICE=\$SERVICE_FOLDER/$PACKAGE_NAME.service

echo \"[Unit]
Description=$APP_NAME server behaving like a mouse and keyboard.
Wants=graphical.target
After=graphical.target network.target

[Service]
ExecStart=/usr/share/$PACKAGE_NAME/$APP_NAME.sh
Environment=DISPLAY=\$(who | awk '{print \$2}')
Restart=always
User=\$SUDO_USER

[Install]
WantedBy=graphical.target
\" > \$SERVICE

systemctl daemon-reload
systemctl enable HIDoverWifi.service
systemctl start HIDoverWifi.service
" > $POST_INST
chmod 0755 $POST_INST
}

function createRunScript() {
RUN_SH=$BUILD_DIR/usr/share/$PACKAGE_NAME/$APP_NAME.sh
echo "#!/bin/bash
TARGET=/etc/$PACKAGE_NAME
mkdir -p \$TARGET
cd \$TARGET
java -jar /usr/share/$PACKAGE_NAME/$APP_NAME.jar -s
" > $RUN_SH
chmod 0755 $RUN_SH
}

createCLIScript

createPostInst

createRunScript

cp $SOURCE_DIR/target/$ARTIFACT_NAME-$PACKAGE_VERSION.jar $BUILD_DIR/usr/share/$PACKAGE_NAME/$APP_NAME.jar

echo "$PACKAGE_NAME ($PACKAGE_VERSION) trusty; urgency=low" > changelog
echo "  * Rebuild" >> changelog
echo " -- kryptonbutterfly <tinycodecrank@gmail.com> `date -R`" >> changelog
gzip -9c changelog > $BUILD_DIR/usr/share/doc/$PACKAGE_NAME/changelog.gz

PACKAGE_SIZE=`du -bs $BUILD_DIR | cut -f 1`
PACKAGE_SIZE=$((PACKAGE_SIZE/1024))
echo "Installed-Size: $PACKAGE_SIZE" >> $BUILD_DIR/DEBIAN/control

chown -R root $BUILD_DIR/
chgrp -R root $BUILD_DIR/

cd /tmp
dpkg --build debian
mv /tmp/debian.deb $SOURCE_DIR/build/$PACKAGE_NAME-$PACKAGE_VERSION.deb
rm -r $BUILD_DIR

