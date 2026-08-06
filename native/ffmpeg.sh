#!/bin/bash
FFMPEG_VERSION="9.0"
SCRIPT_DIR=${PWD}
cd $HOME
##git clone https://git.ffmpeg.org/ffmpeg.git -b release/$FFMPEG_VERSION --single-branch
#git clone https://github.com/FFmpeg/FFmpeg -b release/$FFMPEG_VERSION --single-branch ffmpeg
#cd ffmpeg
#rm -r -f ".git"

rm $FFMPEG_VERSION.zip
wget https://github.com/FFmpeg/FFmpeg/archive/refs/heads/release/$FFMPEG_VERSION.zip
if [[ $? -ne 0 ]]; then
    echo "Wget failed!"
    exit 1;
fi
unzip $FFMPEG_VERSION.zip
rm $FFMPEG_VERSION.zip
mv FFmpeg-release-$FFMPEG_VERSION ffmpeg


ENABLED_DECODERS=(mpeg4 h264 hevc mp3 aac ac3 eac3 flac vorbis alac)
HOST_PLATFORM="linux-x86_64"
NDK_PATH="$HOME/Android/Sdk/ndk/30.0.15729638"

cp ${SCRIPT_DIR}/okhttp.c "$HOME/ffmpeg/libavformat/okhttp.c"

cat <<EOT >> "$HOME/ffmpeg/libavformat/Makefile"
OBJS     	 += okhttp.o
EOT

cp ${SCRIPT_DIR}/okhttp.patch "$HOME/ffmpeg/okhttp.patch"
cd "$HOME/ffmpeg/"
patch -p1 < okhttp.patch

cd ${SCRIPT_DIR}/src/main/jni/

echo 'Please input platform version (Example 26 - Android 8.0): '
read ANDROID_PLATFORM

./build_ffmpeg.sh "${NDK_PATH}" "${HOST_PLATFORM}" "${ANDROID_PLATFORM}" "-fvisibility=hidden" "${ENABLED_DECODERS[@]}"

cd ${SCRIPT_DIR}
