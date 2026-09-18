#include <jni.h>
#include <string>
#include <sys/ptrace.h>
#include <unistd.h>
#include <fcntl.h>

/**
 * NepTools Native Security Engine
 *
 * Provides a ptrace-based tracer check that complements the Kotlin RASP engine.
 *
 * Design note: this library deliberately holds NO secrets. Anything compiled into a shipped
 * APK can be recovered by an attacker, so vault key material is derived only from the user's
 * master password plus a per-vault random salt (see VaultCrypto.kt).
 *
 * Symbols are hidden via -fvisibility=hidden and stripped with -Wl,--strip-all; native methods
 * are bound through RegisterNatives in JNI_OnLoad so no Java_* exports exist.
 */

static jboolean native_verify_integrity(JNIEnv * /* env */, jobject /* this */, jobject /* context */) {
    // 1. ptrace check: a process can only be traced by one debugger at a time.
    // If PTRACE_TRACEME fails, an external debugger is already attached.
    if (ptrace(PTRACE_TRACEME, 0, 1, 0) < 0) {
        return JNI_FALSE;
    }

    // 2. Read /proc/self/wchan to detect a tracer waiting on the process.
    int fd = open("/proc/self/wchan", O_RDONLY);
    if (fd >= 0) {
        char buffer[64] = {0};
        ssize_t bytesRead = read(fd, buffer, sizeof(buffer) - 1);
        close(fd);
        if (bytesRead > 0) {
            std::string wchan(buffer);
            if (wchan.find("ptrace") != std::string::npos ||
                wchan.find("sys_ptrace") != std::string::npos) {
                return JNI_FALSE;
            }
        }
    }

    return JNI_TRUE;
}

static const JNINativeMethod NATIVE_METHODS[] = {
    {"verifyEnvironmentIntegrity", "(Landroid/content/Context;)Z", (void *)native_verify_integrity}
};

static const char *CLASS_NAME = "com/neptools/app/core/security/NativeSecurityBridge";

JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void * /* reserved */) {
    JNIEnv *env = nullptr;
    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }

    jclass targetClass = env->FindClass(CLASS_NAME);
    if (targetClass == nullptr) {
        return JNI_ERR;
    }

    jint registerResult = env->RegisterNatives(
        targetClass,
        NATIVE_METHODS,
        sizeof(NATIVE_METHODS) / sizeof(NATIVE_METHODS[0])
    );

    if (registerResult < 0) {
        return JNI_ERR;
    }

    return JNI_VERSION_1_6;
}
