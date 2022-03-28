//
// Created by fengyue on 2022/3/28.
//

#include <dobby.h>
#include <lsplant.hpp>
#include <sys/mman.h>
#include "elf_util.h"
#include "logging.h"

#define _uintval(p)               reinterpret_cast<uintptr_t>(p)
#define _ptr(p)                   reinterpret_cast<void *>(p)
#define _align_up(x, n)           (((x) + ((n) - 1)) & ~((n) - 1))
#define _align_down(x, n)         ((x) & -(n))
#define _page_size                4096
#define _page_align(n)            _align_up(static_cast<uintptr_t>(n), _page_size)
#define _ptr_align(x)             _ptr(_align_down(reinterpret_cast<uintptr_t>(x), _page_size))
#define _make_rwx(p, n)           ::mprotect(_ptr_align(p), \
                                              _page_align(_uintval(p) + n) != _page_align(_uintval(p)) ? _page_align(n) + _page_size : _page_align(n), \
                                              PROT_READ | PROT_WRITE | PROT_EXEC)

bool init_result;

void *InlineHooker(void *target, void *hooker) {
    _make_rwx(target, _page_size);
    void *origin_call;
    if (DobbyHook(target, hooker, &origin_call) == RS_SUCCESS) {
        return origin_call;
    } else {
        return nullptr;
    }
}

bool InlineUnhooker(void *func) {
    return DobbyDestroy(func) == RT_SUCCESS;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_me_fycz_maple_MapleUtils_initHook(JNIEnv *, jclass) {
    return init_result;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_me_fycz_maple_MapleBridge_doHook(JNIEnv *env, jobject thiz, jobject original,
                                    jobject callback) {
    return lsplant::Hook(env, original, thiz, callback);
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_me_fycz_maple_MapleBridge_doUnhook(JNIEnv *env, jobject thiz, jobject target) {
    return lsplant::UnHook(env, target);
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_me_fycz_maple_MapleUtils_isHooked(JNIEnv *env, jclass , jobject method) {
    return lsplant::IsHooked(env, method);
}

JNIEXPORT jint JNICALL
JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }
    SandHook::ElfImg art("libart.so");
    lsplant::InitInfo initInfo{
            .inline_hooker = InlineHooker,
            .inline_unhooker = InlineUnhooker,
            .art_symbol_resolver = [&art](std::string_view symbol) -> void * {
                auto *out = reinterpret_cast<void *>(art.getSymbAddress(symbol));
                return out;
            }
    };
    init_result = lsplant::Init(env, initInfo);
    return JNI_VERSION_1_6;
}
