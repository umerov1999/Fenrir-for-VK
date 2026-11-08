#include <jni.h>
#include <iostream>
#include <mpegfile.h>
#include <mp4file.h>
#include <tstring.h>
#include <id3v2tag.h>
#include <mp4tag.h>
#include <id3v2frame.h>
#include <id3v2header.h>
#include <attachedpictureframe.h>
#include <mp4coverart.h>
#include "fenrir_native.h"

using namespace std;
using namespace TagLib;

static string fixCov(const char *str) {
    string v = str;
    for (auto &i: v) {
        if (i == L'\"') {
            i = L'\'';
        }
    }
    return v;
}


extern "C" JNIEXPORT jboolean JNICALL
Java_dev_ragnarok_fenrir_module_FileUtils_audioTagModifyNative(JNIEnv *env, jobject,
                                                               jstring audioFile,
                                                               jstring coverFile,
                                                               jstring coverMimeType,
                                                               jstring title, jstring artist,
                                                               jstring albumTitle, jstring ifGenre,
                                                               jstring comment) {
    char const *audioFileString = SafeGetStringUTFChars(env, audioFile, nullptr);
    if (!audioFileString) {
        return false;
    }

    MPEG::File f(audioFileString);
    if (!f.isOpen() || !f.isValid()) {
        env->ReleaseStringUTFChars(audioFile, audioFileString);
        return false;
    }
    env->ReleaseStringUTFChars(audioFile, audioFileString);
    auto v = f.ID3v2Tag(true);
    v->removeFrames("APIC");

    char const *titleString = SafeGetStringUTFChars(env, title, nullptr);
    if (titleString) {
        if (strlen(titleString) > 0) {
            v->setTitle(String(fixCov(titleString), String::Type::UTF8));
        }
        env->ReleaseStringUTFChars(title, titleString);
    }

    char const *artistString = SafeGetStringUTFChars(env, artist, nullptr);
    if (artistString) {
        if (strlen(artistString) > 0) {
            v->setArtist(String(fixCov(artistString), String::Type::UTF8));
        }
        env->ReleaseStringUTFChars(artist, artistString);
    }

    char const *albumTitleString = SafeGetStringUTFChars(env, albumTitle, nullptr);
    if (albumTitleString) {
        if (strlen(albumTitleString) > 0) {
            v->setAlbum(String(fixCov(albumTitleString), String::Type::UTF8));
        }
        env->ReleaseStringUTFChars(albumTitle, albumTitleString);
    }

    char const *commentString = SafeGetStringUTFChars(env, comment, nullptr);
    if (commentString) {
        if (strlen(commentString) > 0) {
            v->setComment(String(fixCov(commentString), String::Type::UTF8));
        }
        env->ReleaseStringUTFChars(comment, commentString);
    }

    if (v->genre().isEmpty()) {
        char const *ifGenreString = SafeGetStringUTFChars(env, ifGenre, nullptr);
        if (ifGenreString) {
            if (strlen(ifGenreString) > 0) {
                v->setGenre(String(ifGenreString, String::Type::Latin1));
            }
            env->ReleaseStringUTFChars(ifGenre, ifGenreString);
        }
    }
    auto pic = new ID3v2::AttachedPictureFrame();
    ByteVector buf;
    char const *coverFileString = SafeGetStringUTFChars(env, coverFile, nullptr);
    if (coverFileString) {
        FILE *cv = fopen(coverFileString, "rb");
        if (cv != nullptr) {
            char const *coverMimeTypeString = SafeGetStringUTFChars(env, coverMimeType,
                                                                    nullptr);
            if (!coverMimeTypeString || strlen(coverMimeTypeString) <= 0) {
                pic->setMimeType("image/jpg");
            } else {
                pic->setMimeType(coverMimeTypeString);
                env->ReleaseStringUTFChars(coverMimeType, coverMimeTypeString);
            }
            fseek(cv, 0, SEEK_END);
            int size = (int) ftell(cv);
            buf.resize(size, '\0');
            fseek(cv, 0, SEEK_SET);
            fread(buf.data(), 1, size, cv);
            fclose(cv);

            pic->setTextEncoding(String::Type::Latin1);
            pic->setType(ID3v2::AttachedPictureFrame::FrontCover);
            pic->setDescription("VK Cover");
            pic->setPicture(buf);
            v->addFrame(pic);
        }
        env->ReleaseStringUTFChars(coverFile, coverFileString);
    }
    buf.clear();
    return f.save(MPEG::File::TagTypes::ID3v2, TagLib::File::StripOthers, ID3v2::v4,
                  TagLib::File::Duplicate);
}

extern "C" JNIEXPORT jboolean JNICALL
Java_dev_ragnarok_fenrir_module_FileUtils_videoTagModifyNative(JNIEnv *env, jobject,
                                                               jstring file,
                                                               jstring coverFile,
                                                               jstring title, jstring artist,
                                                               jstring albumTitle, jstring ifGenre,
                                                               jstring comment) {
    char const *fileString = SafeGetStringUTFChars(env, file, nullptr);
    if (!fileString) {
        return false;
    }

    MP4::File f(fileString);
    if (!f.isOpen() || !f.isValid()) {
        env->ReleaseStringUTFChars(file, fileString);
        return false;
    }
    env->ReleaseStringUTFChars(file, fileString);
    auto v = f.tag();
    v->removeItem("covr");

    char const *titleString = SafeGetStringUTFChars(env, title, nullptr);
    if (titleString) {
        if (strlen(titleString) > 0) {
            v->setTitle(String(fixCov(titleString), String::Type::UTF8));
        }
        env->ReleaseStringUTFChars(title, titleString);
    }

    char const *artistString = SafeGetStringUTFChars(env, artist, nullptr);
    if (artistString) {
        if (strlen(artistString) > 0) {
            v->setArtist(String(fixCov(artistString), String::Type::UTF8));
        }
        env->ReleaseStringUTFChars(artist, artistString);
    }

    char const *albumTitleString = SafeGetStringUTFChars(env, albumTitle, nullptr);
    if (albumTitleString) {
        if (strlen(albumTitleString) > 0) {
            v->setAlbum(String(fixCov(albumTitleString), String::Type::UTF8));
        }
        env->ReleaseStringUTFChars(albumTitle, albumTitleString);
    }

    char const *commentString = SafeGetStringUTFChars(env, comment, nullptr);
    if (commentString) {
        if (strlen(commentString) > 0) {
            v->setComment(String(fixCov(commentString), String::Type::UTF8));
        }
        env->ReleaseStringUTFChars(comment, commentString);
    }

    if (v->genre().isEmpty()) {
        char const *ifGenreString = SafeGetStringUTFChars(env, ifGenre, nullptr);
        if (ifGenreString) {
            if (strlen(ifGenreString) > 0) {
                v->setGenre(String(ifGenreString, String::Type::Latin1));
            }
            env->ReleaseStringUTFChars(ifGenre, ifGenreString);
        }
    }
    ByteVector buf;
    char const *coverFileString = SafeGetStringUTFChars(env, coverFile, nullptr);
    if (coverFileString) {
        FILE *cv = fopen(coverFileString, "rb");
        if (cv != nullptr) {
            fseek(cv, 0, SEEK_END);
            int size = (int) ftell(cv);
            buf.resize(size, '\0');
            fseek(cv, 0, SEEK_SET);
            fread(buf.data(), 1, size, cv);
            fclose(cv);

            TagLib::MP4::CoverArt cover(
                    TagLib::MP4::CoverArt::JPEG,
                    buf
            );
            TagLib::MP4::CoverArtList covers;
            covers.append(cover);
            v->setItem("covr", TagLib::MP4::Item(covers));
        }
        env->ReleaseStringUTFChars(coverFile, coverFileString);
    }
    buf.clear();
    return f.save();
}

extern "C" JNIEXPORT jboolean JNICALL
Java_dev_ragnarok_fenrir_module_FileUtils_audioTagStripNative(JNIEnv *env, jobject,
                                                              jstring audioFile) {
    char const *audioFileString = SafeGetStringUTFChars(env, audioFile, nullptr);
    if (!audioFileString) {
        return false;
    }
    MPEG::File f(audioFileString);
    if (!f.isOpen() || !f.isValid()) {
        env->ReleaseStringUTFChars(audioFile, audioFileString);
        return false;
    }
    env->ReleaseStringUTFChars(audioFile, audioFileString);
    return f.strip(MPEG::File::AllTags);
}

extern "C" JNIEXPORT jboolean JNICALL
Java_dev_ragnarok_fenrir_module_FileUtils_videoTagStripNative(JNIEnv *env, jobject,
                                                              jstring file) {
    char const *fileString = SafeGetStringUTFChars(env, file, nullptr);
    if (!fileString) {
        return false;
    }
    MP4::File f(fileString);
    if (!f.isOpen() || !f.isValid()) {
        env->ReleaseStringUTFChars(file, fileString);
        return false;
    }
    env->ReleaseStringUTFChars(file, fileString);
    return f.strip(MP4::File::AllTags);
}