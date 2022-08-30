#include <jni.h>
#include <android/bitmap.h>
#include <rlottie.h>
#include <lz4.h>
#include <zlib.h>
#include <unistd.h>
#include <fstream>
#include <condition_variable>
#include <atomic>
#include <thread>
#include <map>
#include <sys/stat.h>
#include <utime.h>
#include "log.h"
#include "tools.h"
#include "gif.h"

extern "C" {
using namespace rlottie;

class LottieInfo {
public:
    ~LottieInfo() {
        if (decompressBuffer != nullptr) {
            delete[]decompressBuffer;
            decompressBuffer = nullptr;
        }
    }

    std::unique_ptr<Animation> animation;
    size_t frameCount = 0;
    int32_t fps = 30;
    bool precache = false;
    bool createCache = false;
    bool limitFps = false;
    std::string path;
    std::string cacheFile;
    uint8_t *decompressBuffer = nullptr;
    uint32_t decompressBufferSize = 0;
    std::atomic<uint32_t> maxFrameSize = 0;
    uint32_t imageSize = 0;
    uint32_t fileOffset = 0;
    uint32_t fileFrame = 0;
    bool nextFrameIsCacheFrame = false;

    FILE *precacheFile = nullptr;
    char *compressBuffer = nullptr;
    const char *buffer = nullptr;
    bool firstFrame = false;
    int bufferSize = 0;
    int compressBound = 0;
    int firstFrameSize = 0;
    std::atomic<uint32_t> framesAvailableInCache = 0;
};

static std::string doDecompressLottie(size_t length, char *bytes, bool &orig) {
    orig = false;
    std::string data;
    if (length >= 3 && memcmp(bytes, "\x1F\x8B\x08", 3) == 0) {
        z_stream zs;
        memset(&zs, 0, sizeof(zs));

        if (inflateInit2(&zs, 15 + 16) != Z_OK) {
            return "";
        }

        zs.next_in = (Bytef *) bytes;
        zs.avail_in = length;

        int ret;
        std::vector<char> outBuffer(32768);

        do {
            zs.next_out = reinterpret_cast<Bytef *>(outBuffer.data());
            zs.avail_out = outBuffer.size();
            ret = inflate(&zs, 0);
            if (data.size() < zs.total_out) {
                data.append(outBuffer.data(), zs.total_out - data.size());
            }

        } while (ret == Z_OK);
        inflateEnd(&zs);
        if (ret != Z_STREAM_END) {
            return "";
        }
    } else if (length >= 4 && memcmp(bytes, "\x02\x4C\x5A\x34", 4) == 0) {
#pragma pack(push, 1)
        struct HDR {
            char hdr[4];
            int size;
        };
#pragma pack(pop)
        HDR hdr = {};
        memcpy(&hdr, bytes, sizeof(HDR));
        data.resize(hdr.size);
        LZ4_decompress_safe(((const char *) bytes + sizeof(HDR)), (char *) data.data(),
                            (int) length - (int) sizeof(HDR), hdr.size);
    } else {
        orig = true;
    }
    return data;
}

JNIEXPORT jlong
Java_dev_ragnarok_fenrir_module_rlottie_RLottieDrawable_create(JNIEnv *env, jobject, jstring src,
                                                               jint w,
                                                               jint h, jintArray data,
                                                               jboolean precache,
                                                               jintArray colorReplacement,
                                                               jboolean useMoveColor,
                                                               jboolean limitFps) {
    auto *info = new LottieInfo();
    internal::ColorReplace *colors = nullptr;
    std::string color;
    uint64_t crc = 0;
    if (colorReplacement != nullptr) {
        jint *arr = env->GetIntArrayElements(colorReplacement, nullptr);
        if (arr != nullptr) {
            jsize len = env->GetArrayLength(colorReplacement);
            if (len % 2 == 0) {
                colors = new internal::ColorReplace(useMoveColor);
                for (int32_t a = 0; a < len / 2; a++) {
                    colors->colorMap[arr[a * 2]] = arr[a * 2 + 1];
                    crc64(crc, &arr[a * 2], sizeof(jint));
                    crc64(crc, &arr[a * 2 + 1], sizeof(jint));
                }
                color = "clr_" + dumpCrc64(crc);
            }
            env->ReleaseIntArrayElements(colorReplacement, arr, 0);
        }
    }

    char const *srcString = SafeGetStringUTFChars(env, src, nullptr);
    info->path = srcString;
    if (srcString != nullptr) {
        env->ReleaseStringUTFChars(src, srcString);
    }
    std::ifstream f;
    f.open(info->path);
    if (!f.is_open()) {
        delete info;
        return 0;
    }
    f.seekg(0, std::ios::end);
    auto length = f.tellg();
    f.seekg(0, std::ios::beg);
    if (length <= 0) {
        f.close();
        delete info;
        return 0;
    }
    auto *arr = new char[(size_t) length + 1];
    f.read(arr, length);
    f.close();
    arr[length] = '\0';
    bool orig;
    std::string jsonString = doDecompressLottie(length, arr, orig);
    if (orig) {
        info->animation = rlottie::Animation::loadFromData(arr, colors);
    }
    delete[] arr;
    if (!orig) {
        if (jsonString.length() <= 0) {
            delete info;
            return 0;
        }
        info->animation = rlottie::Animation::loadFromData(jsonString.c_str(), colors);
    }
    if (info->animation == nullptr) {
        delete info;
        return 0;
    }
    info->frameCount = info->animation->totalFrame();
    info->fps = (int) info->animation->frameRate();
    info->limitFps = limitFps;
    if (info->fps > 60 || info->frameCount > 600) {
        //delete info;
        //return 0;
    }
    info->precache = precache;
    if (info->precache) {
        info->cacheFile = info->path;
        std::string::size_type index = info->cacheFile.find_last_of('/');
        if (index != std::string::npos) {
            std::string dir = info->cacheFile.substr(0, index) + "/rendered";
            mkdir(dir.c_str(), 0777);
            info->cacheFile.insert(index, "/rendered");
        }
        info->cacheFile += "_" + std::to_string(w) + "x" + std::to_string(h);
        if (!color.empty()) {
            info->cacheFile += "_" + color;
        }
        if (limitFps) {
            info->cacheFile += ".s.cache";
        } else {
            info->cacheFile += ".cache";
        }
        FILE *precacheFile = fopen(info->cacheFile.c_str(), "r+");
        if (precacheFile == nullptr) {
            info->createCache = true;
        } else {
            uint8_t temp;
            size_t read = fread(&temp, sizeof(uint8_t), 1, precacheFile);
            info->createCache = read != 1 || temp == 0;
            if (!info->createCache) {
                uint32_t maxFrameSize;
                fread(&maxFrameSize, sizeof(uint32_t), 1, precacheFile);
                info->maxFrameSize = maxFrameSize;
                fread(&(info->imageSize), sizeof(uint32_t), 1, precacheFile);
                info->fileOffset = 9;
                info->fileFrame = 0;
                utimensat(0, info->cacheFile.c_str(), nullptr, 0);
            }
            fclose(precacheFile);
        }
    }

    jint *dataArr = env->GetIntArrayElements(data, nullptr);
    if (dataArr != nullptr) {
        dataArr[0] = (jint) info->frameCount;
        dataArr[1] = (jint) info->animation->frameRate();
        dataArr[2] = info->createCache ? 1 : 0;
        env->ReleaseIntArrayElements(data, dataArr, 0);
    }
    return (jlong) (intptr_t) info;
}

JNIEXPORT jstring
Java_dev_ragnarok_fenrir_module_rlottie_RLottieDrawable_getCacheFile(JNIEnv *env, jobject,
                                                                     jlong ptr) {
    if (!ptr) {
        return nullptr;
    }
    auto info = (LottieInfo *) (intptr_t) ptr;
    if (info->precache) {
        return env->NewStringUTF(info->cacheFile.c_str());
    }
    return nullptr;
}

JNIEXPORT jlong
Java_dev_ragnarok_fenrir_module_rlottie_RLottieDrawable_createWithJsonCache(JNIEnv *env, jobject,
                                                                            jlong json,
                                                                            jstring name,
                                                                            jstring path, jint w,
                                                                            jint h, jintArray data,
                                                                            jintArray colorReplacement,
                                                                            jboolean useMoveColor) {
    std::string color;
    uint64_t crc = 0;
    internal::ColorReplace *colors = nullptr;
    if (colorReplacement != nullptr) {
        jint *arr = env->GetIntArrayElements(colorReplacement, nullptr);
        if (arr != nullptr) {
            jsize len = env->GetArrayLength(colorReplacement);
            if (len % 2 == 0) {
                colors = new internal::ColorReplace(useMoveColor);
                for (int32_t a = 0; a < len / 2; a++) {
                    colors->colorMap[arr[a * 2]] = arr[a * 2 + 1];
                    crc64(crc, &arr[a * 2], sizeof(jint));
                    crc64(crc, &arr[a * 2 + 1], sizeof(jint));
                }
                color = "clr_" + dumpCrc64(crc);
            }
            env->ReleaseIntArrayElements(colorReplacement, arr, 0);
        }
    }

    auto *info = new LottieInfo();

    char const *nameString = SafeGetStringUTFChars(env, name, nullptr);
    auto u = ((std::vector<char> *) (intptr_t) json);

    bool orig;
    std::string jsonString = doDecompressLottie(u->size(), u->data(), orig);
    if (orig) {
        info->animation = rlottie::Animation::loadFromData(u->data(), colors);
    } else {
        info->animation = rlottie::Animation::loadFromData(jsonString.c_str(), colors);
    }
    if (nameString != nullptr) {
        env->ReleaseStringUTFChars(name, nameString);
    }
    char const *pathString = SafeGetStringUTFChars(env, path, nullptr);
    if (pathString != nullptr) {
        env->ReleaseStringUTFChars(path, pathString);
    }
    if (info->animation == nullptr) {
        delete info;
        return 0;
    }
    info->frameCount = info->animation->totalFrame();
    info->fps = (int) info->animation->frameRate();
    info->precache = true;

    info->cacheFile = pathString;
    info->cacheFile += "/rendered";
    mkdir(info->cacheFile.c_str(), 0777);
    info->cacheFile += std::string("/") + nameString;
    info->cacheFile += "_" + std::to_string(w) + "x" + std::to_string(h);
    if (!color.empty()) {
        info->cacheFile += "_" + color;
    }
    info->cacheFile += ".cache";
    FILE *precacheFile = fopen(info->cacheFile.c_str(), "r+");
    if (precacheFile == nullptr) {
        info->createCache = true;
    } else {
        uint8_t temp;
        size_t read = fread(&temp, sizeof(uint8_t), 1, precacheFile);
        info->createCache = read != 1 || temp == 0;
        if (!info->createCache) {
            uint32_t maxFrameSize;
            fread(&maxFrameSize, sizeof(uint32_t), 1, precacheFile);
            info->maxFrameSize = maxFrameSize;
            fread(&(info->imageSize), sizeof(uint32_t), 1, precacheFile);
            info->fileOffset = 9;
            utimensat(0, info->cacheFile.c_str(), nullptr, 0);
        }
        fclose(precacheFile);
    }

    jint *dataArr = env->GetIntArrayElements(data, nullptr);
    if (dataArr != nullptr) {
        dataArr[0] = (int) info->frameCount;
        dataArr[1] = (int) info->animation->frameRate();
        dataArr[2] = 1;
        env->ReleaseIntArrayElements(data, dataArr, 0);
    }
    return (jlong) (intptr_t) info;
}

JNIEXPORT jlong
Java_dev_ragnarok_fenrir_module_rlottie_RLottieDrawable_createWithJson(JNIEnv *env, jobject,
                                                                       jlong json,
                                                                       jintArray data,
                                                                       jintArray colorReplacement,
                                                                       jboolean useMoveColor) {
    internal::ColorReplace *colors = nullptr;
    if (colorReplacement != nullptr) {
        jint *arr = env->GetIntArrayElements(colorReplacement, nullptr);
        if (arr != nullptr) {
            jsize len = env->GetArrayLength(colorReplacement);
            if (len % 2 == 0) {
                colors = new internal::ColorReplace(useMoveColor);
                for (int32_t a = 0; a < len / 2; a++) {
                    colors->colorMap[arr[a * 2]] = arr[a * 2 + 1];
                }
            }
            env->ReleaseIntArrayElements(colorReplacement, arr, 0);
        }
    }

    auto *info = new LottieInfo();
    auto u = ((std::vector<char> *) (intptr_t) json);
    bool orig;
    std::string jsonString = doDecompressLottie(u->size(), u->data(), orig);
    if (orig) {
        info->animation = rlottie::Animation::loadFromData(u->data(), colors);
    } else {
        info->animation = rlottie::Animation::loadFromData(jsonString.c_str(), colors);
    }
    if (info->animation == nullptr) {
        delete info;
        return 0;
    }
    info->frameCount = info->animation->totalFrame();
    info->fps = (int) info->animation->frameRate();

    jint *dataArr = env->GetIntArrayElements(data, nullptr);
    if (dataArr != nullptr) {
        dataArr[0] = (int) info->frameCount;
        dataArr[1] = (int) info->animation->frameRate();
        dataArr[2] = 0;
        env->ReleaseIntArrayElements(data, dataArr, 0);
    }
    return (jlong) (intptr_t) info;
}

JNIEXPORT void
Java_dev_ragnarok_fenrir_module_rlottie_RLottieDrawable_destroy(JNIEnv *, jobject, jlong ptr) {
    if (!ptr) {
        return;
    }
    auto *info = (LottieInfo *) (intptr_t) ptr;
    delete info;
}

JNIEXPORT void
Java_dev_ragnarok_fenrir_module_rlottie_RLottieDrawable_setLayerColor(JNIEnv *env, jobject,
                                                                      jlong ptr,
                                                                      jstring layer, jint color) {
    if (!ptr || layer == nullptr) {
        return;
    }
    auto *info = (LottieInfo *) (intptr_t) ptr;
    char const *layerString = SafeGetStringUTFChars(env, layer, nullptr);
    Color v((float) ((color) & 0xff) / 255.0f,
            (float) ((color >> 8) & 0xff) /
            255.0f,
            (float) ((color >> 16) & 0xff) /
            255.0f);

    info->animation->setValue<Property::FillColor>(layerString, v);
    info->animation->setValue<Property::StrokeColor>(layerString, v);
    if (layerString != nullptr) {
        env->ReleaseStringUTFChars(layer, layerString);
    }
}

JNIEXPORT void
Java_dev_ragnarok_fenrir_module_rlottie_RLottieDrawable_replaceColors(JNIEnv *env, jobject,
                                                                      jlong ptr,
                                                                      jintArray colorReplacement) {
    if (!ptr || colorReplacement == nullptr) {
        return;
    }
    auto *info = (LottieInfo *) (intptr_t) ptr;
    if (!info->animation->colorMap || info->animation->colorMap->useMoveColor) {
        return;
    }
    jint *arr = env->GetIntArrayElements(colorReplacement, nullptr);
    if (arr != nullptr) {
        jsize len = env->GetArrayLength(colorReplacement);
        if (len % 2 == 0) {
            for (int32_t a = 0; a < len / 2; a++) {
                info->animation->colorMap->colorMap[arr[a * 2]] = arr[a * 2 + 1];
            }
            info->animation->resetCurrentFrame();
        }
        env->ReleaseIntArrayElements(colorReplacement, arr, 0);
    }
}

bool cacheWriteThreadCreated{false};
LottieInfo *cacheWriteThreadTask{nullptr};
bool cacheWriteThreadDone{false};
std::thread worker;
std::mutex cacheMutex;
std::condition_variable cacheCv;

std::mutex cacheDoneMutex;
std::condition_variable cacheDoneCv;
std::atomic<bool> frameReady{false};

void CacheWriteThreadProc() {
    while (!cacheWriteThreadDone) {
        std::unique_lock<std::mutex> lk(cacheMutex);
        cacheCv.wait(lk, [] { return frameReady.load(); });
        std::lock_guard<std::mutex> lg(cacheDoneMutex);
        LottieInfo *task;
        if (cacheWriteThreadTask != nullptr) {
            task = cacheWriteThreadTask;
            cacheWriteThreadTask = nullptr;
        } else {
            task = nullptr;
        }
        lk.unlock();

        if (task != nullptr) {
            auto size = LZ4_compress_default(task->buffer, task->compressBuffer,
                                             task->bufferSize, task->compressBound);
            if (task->firstFrame) {
                task->firstFrameSize = size;
                task->fileOffset = 9 + sizeof(uint32_t) + task->firstFrameSize;
                task->fileFrame = 1;
            }
            task->maxFrameSize = max(task->maxFrameSize.load(), size);
            fwrite(&size, sizeof(uint32_t), 1, task->precacheFile);
            fwrite(task->compressBuffer, sizeof(uint8_t), size, task->precacheFile);

            fflush(task->precacheFile);
            fsync(fileno(task->precacheFile));
            task->framesAvailableInCache++;
        }
        frameReady = false;
        cacheDoneCv.notify_one();
    }
}

JNIEXPORT void
Java_dev_ragnarok_fenrir_module_rlottie_RLottieDrawable_createCache(JNIEnv *, jobject, jlong ptr,
                                                                    jint w,
                                                                    jint h) {
    if (!ptr) {
        return;
    }
    auto *info = (LottieInfo *) (intptr_t) ptr;

    FILE *cacheFile = fopen(info->cacheFile.c_str(), "r+");
    if (cacheFile != nullptr) {
        uint8_t temp;
        size_t read = fread(&temp, sizeof(uint8_t), 1, cacheFile);
        fclose(cacheFile);
        if (read == 1 && temp != 0) {
            return;
        }
    }

    if (!cacheWriteThreadCreated) {
        cacheWriteThreadCreated = true;
        worker = std::thread(CacheWriteThreadProc);
    }

    if (info->nextFrameIsCacheFrame && info->createCache && info->frameCount != 0) {
        info->precacheFile = fopen(info->cacheFile.c_str(), "w+");
        if (info->precacheFile != nullptr) {
            fseek(info->precacheFile, info->fileOffset = 9, SEEK_SET);
            info->fileFrame = 0;
            info->maxFrameSize = 0;
            info->bufferSize = w * h * 4;
            info->imageSize = (uint32_t) w * h * 4;
            info->compressBound = LZ4_compressBound(info->bufferSize);
            info->compressBuffer = new char[info->compressBound];
            auto *firstBuffer = new uint8_t[info->bufferSize];
            auto *secondBuffer = new uint8_t[info->bufferSize];
            //long time = ConnectionsManager::getInstance(0).getCurrentTimeMonotonicMillis();

            Surface surface1((uint32_t *) firstBuffer, (size_t) w, (size_t) h, (size_t) w * 4);
            Surface surface2((uint32_t *) secondBuffer, (size_t) w, (size_t) h, (size_t) w * 4);
            int framesPerUpdate = !info->limitFps || info->fps < 60 ? 1 : 2;
            int num = 0;
            for (size_t a = 0; a < info->frameCount; a += framesPerUpdate) {
                Surface &surfaceToRender = num % 2 == 0 ? surface1 : surface2;
                num++;
                info->animation->renderSync(a, surfaceToRender, true);
                if (a != 0) {
                    std::unique_lock<std::mutex> lk(cacheDoneMutex);
                    cacheDoneCv.wait(lk, [] { return !frameReady.load(); });
                }

                std::lock_guard<std::mutex> lg(cacheMutex);
                cacheWriteThreadTask = info;
                info->firstFrame = a == 0;
                info->buffer = (const char *) surfaceToRender.buffer();
                frameReady = true;
                cacheCv.notify_one();
            }
            std::unique_lock<std::mutex> lk(cacheDoneMutex);
            cacheDoneCv.wait(lk, [] { return !frameReady.load(); });

            //DEBUG_D("sticker time = %d", (int) (ConnectionsManager::getInstance(0).getCurrentTimeMonotonicMillis() - time));
            delete[] info->compressBuffer;
            delete[] firstBuffer;
            delete[] secondBuffer;
            fseek(info->precacheFile, 0, SEEK_SET);
            uint8_t byte = 1;
            fwrite(&byte, sizeof(uint8_t), 1, info->precacheFile);
            uint32_t maxFrameSize = info->maxFrameSize;
            fwrite(&maxFrameSize, sizeof(uint32_t), 1, info->precacheFile);
            fwrite(&info->imageSize, sizeof(uint32_t), 1, info->precacheFile);
            fflush(info->precacheFile);
            fsync(fileno(info->precacheFile));
            info->createCache = false;
            fclose(info->precacheFile);
        }
    }
}

JNIEXPORT jint
Java_dev_ragnarok_fenrir_module_rlottie_RLottieDrawable_getFrame(JNIEnv *env, jobject, jlong ptr,
                                                                 jint frame,
                                                                 jobject bitmap, jint w, jint h,
                                                                 jint stride,
                                                                 jboolean clear) {
    if (!ptr || bitmap == nullptr) {
        return 0;
    }
    auto *info = (LottieInfo *) (intptr_t) ptr;

    uint32_t framesPerUpdate = !info->limitFps || info->fps < 60 ? 1 : 2;
    uint32_t framesAvailableInCache = info->framesAvailableInCache;

    if (info->createCache && info->precache && frame > 0) {
        if (frame / framesPerUpdate >= framesAvailableInCache) {
            return -1;
        }
    }

    void *pixels;
    if (AndroidBitmap_lockPixels(env, bitmap, &pixels) >= 0) {
        bool loadedFromCache = false;
        uint32_t maxFrameSize = info->maxFrameSize;
        if (info->precache && (!info->createCache || frame > 0) && w * 4 == stride &&
            maxFrameSize <= w * h * 4 && info->imageSize == w * h * 4) {
            FILE *precacheFile = fopen(info->cacheFile.c_str(), "r");
            if (precacheFile != nullptr) {
                if (info->decompressBuffer != nullptr &&
                    info->decompressBufferSize < maxFrameSize) {
                    delete[] info->decompressBuffer;
                    info->decompressBuffer = nullptr;
                }
                if (info->decompressBuffer == nullptr) {
                    info->decompressBufferSize = maxFrameSize;
                    if (info->createCache) {
                        info->decompressBufferSize += 10000;
                    }
                    info->decompressBuffer = new uint8_t[info->decompressBufferSize];
                }
                uint32_t currentFrame = frame / framesPerUpdate;
                if (info->fileFrame != frame) {
                    info->fileOffset = 9;
                    info->fileFrame = 0;
                    while (info->fileFrame != currentFrame) {
                        fseek(precacheFile, info->fileOffset, SEEK_SET);
                        uint32_t frameSize;
                        fread(&frameSize, sizeof(uint32_t), 1, precacheFile);
                        info->fileOffset += 4 + frameSize;
                        info->fileFrame++;
                    }
                }
                fseek(precacheFile, info->fileOffset, SEEK_SET);
                uint32_t frameSize;
                fread(&frameSize, sizeof(uint32_t), 1, precacheFile);
                if (frameSize > 0 && frameSize <= info->decompressBufferSize) {
                    fread(info->decompressBuffer, sizeof(uint8_t), frameSize, precacheFile);
                    info->fileOffset += 4 + frameSize;
                    info->fileFrame = currentFrame + 1;
                    LZ4_decompress_safe((const char *) info->decompressBuffer, (char *) pixels,
                                        (int) frameSize, w * h * 4);
                    loadedFromCache = true;
                }
                fclose(precacheFile);
                if (frame + framesPerUpdate >= info->frameCount) {
                    info->fileOffset = 9;
                    info->fileFrame = 0;
                }
            }
        }

        if (!loadedFromCache) {
            if (!info->nextFrameIsCacheFrame || !info->precache) {
                Surface surface((uint32_t *) pixels, (size_t) w, (size_t) h, (size_t) stride);
                info->animation->renderSync((size_t) frame, surface, clear);
                info->nextFrameIsCacheFrame = true;
            }
        }

        AndroidBitmap_unlockPixels(env, bitmap);
    }
    return frame;
}

class Lottie2Gif {
public:
    static bool render(LottieInfo *player, jobject bitmap, int w, int h, int stride, int bgColor,
                       bool transparent, const std::string &gifName, int bitDepth,
                       bool dither, JNIEnv *env, jobject listener) {
        void *pixels;
        if (AndroidBitmap_lockPixels(env, bitmap, &pixels) >= 0) {
            size_t frameCount = player->animation->totalFrame();
            //auto pixels = std::unique_ptr<uint32_t[]>(new uint32_t[w * h]);

            uint32_t delay = 2;
            GifBuilder builder(gifName, w, h, bgColor, delay, bitDepth, dither);
            size_t start = 0, end = frameCount;

            if (listener != nullptr) {
                jweak store_Wlistener = env->NewWeakGlobalRef(listener);
                jclass clazz = env->GetObjectClass(store_Wlistener);

                jmethodID mth_update = env->GetMethodID(clazz, "onProgress", "(II)V");
                jmethodID mth_start = env->GetMethodID(clazz, "onStarted", "()V");
                jmethodID mth_end = env->GetMethodID(clazz, "onFinished", "()V");

                env->CallVoidMethod(store_Wlistener, mth_start);

                for (size_t i = start; i < end; i++) {
                    rlottie::Surface surface((uint32_t *) pixels, (size_t) w, (size_t) h,
                                             (size_t) stride);
                    player->animation->renderSync(i, surface, true);
                    builder.addFrame(surface, transparent, delay, bitDepth, dither);

                    env->CallVoidMethod(store_Wlistener, mth_update, (jint) (i + 1),
                                        (jint) frameCount);
                }

                env->CallVoidMethod(store_Wlistener, mth_end);
            } else {
                for (size_t i = start; i < end; i++) {
                    rlottie::Surface surface((uint32_t *) pixels, (size_t) w, (size_t) h,
                                             (size_t) stride);
                    player->animation->renderSync(i, surface, true);
                    builder.addFrame(surface, transparent, delay, bitDepth, dither);
                }
            }

            AndroidBitmap_unlockPixels(env, bitmap);
            return true;
        }
        return false;
    }

};

JNIEXPORT
jboolean
Java_dev_ragnarok_fenrir_module_rlottie_RLottie2Gif_lottie2gif(JNIEnv *env, jobject, jlong ptr,
                                                               jobject bitmap, jint w, jint h,
                                                               jint stride, jint bgColor,
                                                               jboolean transparent,
                                                               jstring gifName,
                                                               jint bitDepth,
                                                               jboolean dither,
                                                               jobject listener) {
    if (!ptr) {
        return false;
    }
    char const *name = SafeGetStringUTFChars(env, gifName, nullptr);
    auto *info = (LottieInfo *) (intptr_t) ptr;
    return Lottie2Gif::render(info, bitmap, w, h, stride, bgColor, (bool) transparent, name,
                              bitDepth, dither, env, listener);
}

}
