#!/bin/bash
NDK=/home/zk/android-ndk-r21b
TOOLCHAIN=$NDK/toolchains/llvm/prebuilt/linux-x86_64
API=21

function build_android
{
./configure \
    --prefix=$PREFIX \
    --enable-neon \
    --enable-hwaccels \
    --enable-gpl \
    --enable-postproc \
    --enable-shared \
    --enable-jni \
    --enable-mediacodec \
    --enable-decoder=h264_mediacodec \
	--disable-static \
	--enable-shared \
    --disable-doc \
    --disable-ffmpeg \
    --disable-ffplay \
    --disable-ffprobe \
    --enable-avdevice \
    --disable-doc \
    --disable-symver \
    --cross-prefix=$CROSS_PREFIX \
    --target-os=android \
    --arch=$ARCH \
    --cpu=$CPU \
    --cc=$CC
    --cxx=$CXX
    --enable-cross-compile \
    --sysroot=$SYSROOT \
	--extra-cflags="-I$ASM -isysroot $ISYSROOT -D__ANDROID_API__=21 -U_FILE_OFFSET_BITS -Os -fPIC -DANDROID -Wno-deprecated -mfloat-abi=softfp -marm -march=$CPU -Wl,--hash-style=sysv " \
	--extra-ldflags="-Wl,--hash-style=sysv  $ADDI_LDFLAGS" \
    $ADDITIONAL_CONFIGURE_FLAG

}


#armv7-a
ARCH=arm
CPU=armv7-a
CC=$TOOLCHAIN/bin/armv7a-linux-androideabi$API-clang
CXX=$TOOLCHAIN/bin/armv7a-linux-androideabi$API-clang++
SYSROOT=$NDK/platforms/android-21/arch-arm
ISYSROOT=$NDK/sysroot
ASM=$ISYSROOT/usr/include/arm-linux-androideabi
CROSS_PREFIX=$TOOLCHAIN/bin/arm-linux-androideabi-
PREFIX=$(pwd)/android/$CPU
ADDI_CFLAGS="-marm"
#OPTIMIZE_CFLAGS="-mfloat-abi=softfp -mfpu=vfp -marm -march=$CPU -Wl,--hash-style=sysv "
#ADDI_LDFLAGS="-Wl,--hash-style=sysv "
echo "Compiling FFmpeg for $CPU"
make clean
build_android
make -j16
make install

# 打包
# $TOOLCHAIN/bin/arm-linux-androideabi-ld \
	# -rpath-link=$SYSROOT/usr/lib \
	# -L$SYSROOT/usr/lib \
	# -L$PREFIX/lib \
	# -soname libffmpeg.so -shared -nostdlib -Bsymbolic --whole-archive --no-undefined -o \
	# $PREFIX/libffmpeg.so \
	# libavcodec/libavcodec.a \
	# libavfilter/libavfilter.a \
	# libavformat/libavformat.a \
	# libavutil/libavutil.a \
	# libswresample/libswresample.a \
	# libswscale/libswscale.a \
	# -lc -lm -lz -ldl -llog --dynamic-linker=/system/bin/linker \
	# $TOOLCHAIN/lib/gcc/arm-linux-androideabi/4.9.x/libgcc.a

# strip 精简文件
# $TOOLCHAIN/bin/arm-linux-androideabi-strip  $PREFIX/libffmpeg.so
	
echo "The Compilation of FFmpeg for $CPU is completed"
