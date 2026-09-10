#include <jni.h>
#include <string>
#include <vector>
#include <sys/ptrace.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/stat.h>

/**
 * NepTools Native Security Engine
 * Implements:
 * 1. Obfuscated byte extraction for Vault Secret Pepper / Salt
 * 2. Native ptrace anti-debugging
 * 3. Dynamic JNI Registration (Hiding exported symbols from Ghidra/IDA Pro)
 */

#define NEUTRAL_KEY 0x5A
#define ROTATING_KEY 0x3C

// Obfuscated encrypted seed array: "NepTools_SuperSecure_Vault_Key_Seed_2026"
static const unsigned char OBFUSCATED_SEED[] = {
    0x14, 0x3F, 0x2A, 0x0E, 0x35, 0x35, 0x36, 0x05, 0x09, 0x2F,
    0x2A, 0x3F, 0x28, 0x09, 0x3F, 0x39, 0x2F, 0x05, 0x0C, 0x3B,
    0x2F, 0x36, 0x2E, 0x05, 0x11, 0x3F, 0x23, 0x05, 0x09, 0x3F,
    0x3F, 0x3E, 0x05, 0x68, 0x6A, 0x68, 0x6C
};
static const size_t SEED_LEN = sizeof(OBFUSCATED_SEED) / sizeof(OBFUSCATED_SEED[0]);

/**
 * Returns the de-obfuscated vault seed byte array dynamically at runtime.
 */
static jbyteArray native_get_vault_seed(JNIEnv *env, jobject /* this */) {
    std::vector<jbyte> decryptedBytes(SEED_LEN);
    for (size_t i = 0; i < SEED_LEN; ++i) {
        decryptedBytes[i] = static_cast<jbyte>(OBFUSCATED_SEED[i] ^ NEUTRAL_KEY ^ (ROTATING_KEY + (i % 7)));
    }

    jbyteArray resultArray = env->NewByteArray(static_cast<jsize>(SEED_LEN));
    env->SetByteArrayRegion(resultArray, 0, static_cast<jsize>(SEED_LEN), decryptedBytes.data());
    return resultArray;
}

/**
 * Performs native-level environment and ptrace anti-debugging checks.
 */
static jboolean native_verify_integrity(JNIEnv * /* env */, jobject /* this */, jobject /* context */) {
    // 1. Native ptrace check: A process can only be traced by one debugger at a time.
    // If ptrace(PTRACE_TRACEME) fails, an external debugger is already attached!
    if (ptrace(PTRACE_TRACEME, 0, 1, 0) < 0) {
        return JNI_FALSE; // Debugger detected!
    }

    // 2. Read /proc/self/wchan to detect debugger tracing state
    int fd = open("/proc/self/wchan", O_RDONLY);
    if (fd >= 0) {
        char buffer[64] = {0};
        ssize_t bytesRead = read(fd, buffer, sizeof(buffer) - 1);
        close(fd);
        if (bytesRead > 0) {
            std::string wchan(buffer);
            if (wchan.find("ptrace") != std::string::npos || wchan.find("sys_ptrace") != std::string::npos) {
                return JNI_FALSE; // Native tracer active
            }
        }
    }

    return JNI_TRUE; // Secure
}

// ----------------------------------------------------------------------------
// DYNAMIC JNI REGISTRATION (Inside JNI_OnLoad)
// Prevents exposing standard "Java_com_neptools_app_..." symbols to decompilers
// ----------------------------------------------------------------------------

static const JNINativeMethod NATIVE_METHODS[] = {
    {"getVaultSeed", "()[B", (void *)native_get_vault_seed},
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
