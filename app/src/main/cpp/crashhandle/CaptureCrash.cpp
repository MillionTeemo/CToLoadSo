//
// Created by Admin on 2023/7/6.
//

#include <jni.h>


#include <signal.h>
#include <unistd.h>
#include <fstream>
#include <sstream>

#include <unwind.h>
#include <dlfcn.h>
#include <vector>

// Global variables to store crash information
static std::string gCrashStackTrace;
static std::string gCrashRegisterValues;

struct StackState {
    void** frames;
    int frame_count;
    int cur_frame = 0;

    StackState(void** frames, int frame_count) : frames(frames), frame_count(frame_count) {}
};

static _Unwind_Reason_Code TraceFunction(_Unwind_Context* context, void* arg) {
    // The instruction pointer is pointing at the instruction after the return
    // call on all architectures.
    // Modify the pc to point at the real function.
    uintptr_t ip = _Unwind_GetIP(context);
    if (ip != 0) {
#if defined(__arm__)
        // If the ip is suspiciously low, do nothing to avoid a segfault trying
    // to access this memory.
    if (ip >= 4096) {
      // Check bits [15:11] of the first halfword assuming the instruction
      // is 32 bits long. If the bits are any of these values, then our
      // assumption was correct:
      //  b11101
      //  b11110
      //  b11111
      // Otherwise, this is a 16 bit instruction.
      uint16_t value = (*reinterpret_cast<uint16_t*>(ip - 2)) >> 11;
      if (value == 0x1f || value == 0x1e || value == 0x1d) {
        ip -= 4;
      } else {
        ip -= 2;
      }
    }
#elif defined(__aarch64__)
        // All instructions are 4 bytes long, skip back one instruction.
        ip -= 4;
#elif defined(__i386__) || defined(__x86_64__)
        // It's difficult to decode exactly where the previous instruction is,
    // so subtract 1 to estimate where the instruction lives.
    ip--;
#endif
    }

    StackState* state = static_cast<StackState*>(arg);
    state->frames[state->cur_frame++] = reinterpret_cast<void*>(ip);
    return (state->cur_frame >= state->frame_count) ? _URC_END_OF_STACK : _URC_NO_REASON;
}

int backtrace(void** buffer, int size) {
    if (size <= 0) {
        return 0;
    }

    StackState state(buffer, size);
    _Unwind_Backtrace(TraceFunction, &state);
    return state.cur_frame;
}


// Signal handler function
void CrashSignalHandler(int signal) {
    // Retrieve crash information, such as stack trace and register values
    // You can use tools like `backtrace` and `libunwind` to retrieve the stack trace
    // and platform-specific functions to get register values

    const int MAX_STACK_FRAMES = 64;
    void* stackFrames[MAX_STACK_FRAMES];

    // Retrieve stack trace information
    int stackSize = backtrace(stackFrames, MAX_STACK_FRAMES);
    //char** stackSymbols = backtrace_symbols(stackFrames, stackSize);

    // Convert stack frames to vector for convenience
    std::vector<void*> stackFramesVector(stackFrames, stackFrames + stackSize);
    std::stringstream stackTrace;
    const int addressWidth = sizeof(void*) * 2;

    // Iterate over each stack frame
    for (size_t i = 0; i < 64; ++i) {
        void* address = stackFrames[i];

        // Convert the address to a hexadecimal string
        std::stringstream addressStream;
        addressStream << "0x" << std::setfill('0') << std::setw(addressWidth) << std::hex << reinterpret_cast<unsigned long>(address);

        // Append the address to the stack trace
        stackTrace << "[" << i << "] " << addressStream.str() << "\n";
    }


    std::stringstream crashInfo;
    crashInfo << "Crash Signal: " << signal << "\n";
    crashInfo << "Stack Trace:\n" << stackTrace.str() << "\n";
    crashInfo << "Register Values:\n" << gCrashRegisterValues << "\n";

    // Save crash information to a local file

//com.zltech.ctoloadso
    std::ofstream outFile("/sdcard/Android/data/com.zltech.ctoloadso/ctoloadso/crash/crash_log.txt");
    if (outFile.is_open()) {
        //outFile << crashInfo.str();

    FILE *logcatProcess = popen("logcat -d -v threadtime", "r");

        if (logcatProcess == NULL) {
            NULL;
        }else {
            char buffer[128];
            std::string logTxt;
            // Read the output of the command
            while (fgets(buffer, sizeof(buffer), logcatProcess) != NULL) {
                logTxt += buffer;
                outFile << logTxt;
            }
            pclose(logcatProcess);

        }
        outFile.close();
    }
   // DEBUG_D("crash======== :signal =%d",signal);
    // Terminate the process
    kill(getpid(), SIGKILL);

}



void setCrashCapture(JNIEnv *env, jclass cl  ){
    //DEBUG_D("setCrashCapture  init ");
    struct sigaction sa;
    sa.sa_handler = CrashSignalHandler;
    sigemptyset(&sa.sa_mask);
    sa.sa_flags = 0;
/*    sigaction(SIGSEGV, &sa, nullptr);
    sigaction(SIGABRT, &sa, nullptr);*/
}
//com.zltech.ctoloadso
static const char *CrashHandleClassPathName = "com/zltech/ctoloadso/CrashHandleApi";

static JNINativeMethod CrashHandleMethod[]={
        {"setCrashCapture", "()V" ,(void *)setCrashCapture},
};

inline int registerNativeMethods(JNIEnv *env, const char *className, JNINativeMethod *method,
                                 int methodsCount) {
    jclass clazz;
    clazz = env->FindClass(className);
    if (clazz == nullptr) {
        return JNI_FALSE;
    }

    if (env->RegisterNatives(clazz, method, methodsCount) < 0) {
        return JNI_FALSE;
    }
    return JNI_TRUE;
}

extern "C" int registerNativeCrashHandleFunctions(JavaVM *vm, JNIEnv *env) {
    if (!registerNativeMethods(env, CrashHandleClassPathName, CrashHandleMethod,
                               sizeof(CrashHandleMethod) /
                               sizeof(CrashHandleMethod[0]))) {
        return JNI_FALSE;
    }
    return JNI_TRUE;
}
