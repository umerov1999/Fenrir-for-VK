#include <cstdio>
#include <random>
#include <cmath>
#include <jni.h>
#include <thorvg.h>

bool fenrirNativeThorVGInited = false;

extern jint FFMPEG_JNI_OnLoad(JNIEnv *env);

extern "C" int av_jni_set_java_vm(void *vm, void *log_ctx);

extern "C" JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *) {
    JNIEnv *env = nullptr;
    std::random_device dev;
    srand(dev());

    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        return -1;
    }

    //auto threads = std::thread::hardware_concurrency();
    auto threads = 3;
    if (tvg::Initializer::init(threads) == tvg::Result::Success) {
        fenrirNativeThorVGInited = true;
    }

    FFMPEG_JNI_OnLoad(env);
    av_jni_set_java_vm((void *) vm, nullptr);
    return JNI_VERSION_1_6;
}

extern "C" JNIEXPORT void JNICALL JNI_OnUnload(JavaVM *, void *) {
    tvg::Initializer::term();
    fenrirNativeThorVGInited = false;
}

extern "C" char *__cxa_demangle(const char *, char *, size_t *, int *status) {
    if (status) {
        *status = -3; // invalid argument -- the handler then prints the mangled name and what()
    }
    return nullptr;
}