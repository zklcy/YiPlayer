
#include <jni.h>
#include <string.h>

#include "android/log.h"
#include "ffmpeg.h"
#include "libavcodec/jni.h"


#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, "ffmpeg-invoke", __VA_ARGS__)


JNIEXPORT jint JNICALL Java_com_coder_ffmpeg_FFmpegCmd_run(JNIEnv *env, jclass type, jint cmdLen,
                                             jobjectArray cmd) {
    //set java vm
    JavaVM *jvm = NULL;
    (*env)->GetJavaVM(env,&jvm);
    av_jni_set_java_vm(jvm, NULL);

    char *argCmd[cmdLen] ;
    jstring buf[cmdLen];

    for (int i = 0; i < cmdLen; ++i) {
        //buf[i] = static_cast<jstring>(env->GetObjectArrayElement(cmd, i));
		buf[i] = (jstring)(*env)->GetObjectArrayElement(env,cmd, i);
        //char *string = const_cast<char *>(env->GetStringUTFChars(buf[i], JNI_FALSE));
		char *string = (*env)->GetStringUTFChars(env,buf[i], JNI_FALSE);
        argCmd[i] = string;
        LOGD("argCmd=%s",argCmd[i]);
    }

    int retCode = run(cmdLen, argCmd);
    LOGD("ffmpeg-invoke: retCode=%d",retCode);

    return retCode;

}



/*
 * Class:     com_learn_helloffmpeg_MainActivity
 * Method:    stringFromJNI
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_coder_ffmpeg_FFmpegCmd_stringFromJNI(JNIEnv *env, jobject thiz){
#if defined(__arm__)
  #if defined(__ARM_ARCH_7A__)
    #if defined(__ARM_NEON__)
      #define ABI "armeabi-v7a/NEON"
    #else
      #define ABI "armeabi-v7a"
    #endif
  #else
   #define ABI "armeabi"
  #endif
#elif defined(__i386__)
   #define ABI "x86"
#elif defined(__mips__)
   #define ABI "mips"
#else
   #define ABI "unknown"
#endif
 
    return (*env)->NewStringUTF(env, "Hello from JNI !  Compiled with ABI " ABI ".");
}

