#include "fcntl.h"
#include "sys/stat.h"
#include "sys/inotify.h"
#include "jni.h"
#include "pthread.h"
#include "stdlib.h"
#include "stdio.h"
#include "unistd.h"
#include "string.h"
#include "assert.h"
#include "android/log.h"

#define TAG		"PushDaemon"
#define	DAEMON_CALLBACK_NAME "onDaemonDead"
#define LOGI(...)	__android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGD(...)	__android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define LOGW(...)	__android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__)
#define	LOGE(...)	__android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

/**
 *  get the android version code
 */
int get_version(){
    char value[8] = "";
    __system_property_get("ro.build.version.sdk", value);
    return atoi(value);
}

/**
 *  stitch three string to one
 */
char *str_stitching(const char *str1, const char *str2, const char *str3){
    char *result;
    result = (char*) malloc(strlen(str1) + strlen(str2) + strlen(str3) + 1);
    if (!result){
        return NULL;
    }
    strcpy(result, str1);
    strcat(result, str2);
    strcat(result, str3);
    return result;
}

/**
 * get android context
 */
jobject get_context(JNIEnv* env, jobject jobj){
    jclass thiz_cls = (*env)->GetObjectClass(env, jobj);
    jfieldID context_field = (*env)->GetFieldID(env, thiz_cls, "mContext", "Landroid/content/Context;");
    return (*env)->GetObjectField(env, jobj, context_field);
}


char* get_package_name(JNIEnv* env, jobject jobj){
    jobject context_obj = get_context(env, jobj);
    jclass context_cls = (*env)->GetObjectClass(env, context_obj);
    jmethodID getpackagename_method = (*env)->GetMethodID(jobj, context_cls, "getPackageName", "()Ljava/lang/String;");
    jstring package_name = (jstring)(*env)->CallObjectMethod(env, context_obj, getpackagename_method);
    return (char*)(*env)->GetStringUTFChars(env, package_name, 0);
}


/**
 * call java callback
 */
void java_callback(JNIEnv* env, jobject jobj, char* method_name){
    jclass cls = (*env)->GetObjectClass(env, jobj);
    if(cls != NULL) {
        jmethodID cb_method = (*env)->GetMethodID(env, cls, method_name, "()V");
        if(cb_method != NULL) {
            (*env)->CallVoidMethod(env, jobj, cb_method);
        }
    }
    if ((*env)->ExceptionCheck(env)) {
        (*env)->ExceptionClear(env);
    }
}

void remove_path(const char* file_path) {
   int ret = remove(file_path);
   if(ret == 0) {
      LOGD("file %s deleted successfully", file_path);
   } else {
      LOGE("Error: unable to delete the file %s", file_path);
   }
}


notify_and_waitfor(char *observer_self_path, char *observer_daemon_path){
	int observer_self_descriptor = open(observer_self_path, O_RDONLY);
	if (observer_self_descriptor == -1){
		observer_self_descriptor = open(observer_self_path, O_CREAT | O_RDONLY, S_IRUSR | S_IWUSR);
        LOGE("do create : %s" , observer_self_path);
    }
    int closeResult = 0;
    if(observer_self_descriptor != -1) {
        closeResult = close(observer_self_descriptor);
    }
    LOGE("create file successfully : %s, close result = %d" , observer_self_path, closeResult);

    int observer_daemon_descriptor = open(observer_daemon_path, O_RDONLY);
	while (observer_daemon_descriptor == -1){
		usleep(1000);
		observer_daemon_descriptor = open(observer_daemon_path, O_RDONLY);
	}
	closeResult = close(observer_daemon_descriptor);
	remove_path(observer_daemon_path);
	LOGE("Watched >>>>OBSERVER<<<< has been ready... close file result = %d", closeResult);
}


/**
 *  Lock the file, this is block method.
 */
int lock_file(char* lock_file_path, int* fd){
    LOGE("start try to lock file >> %s <<", lock_file_path);
    *fd = open(lock_file_path, O_RDONLY);
    if (*fd == -1){
        *fd = open(lock_file_path, O_CREAT | O_RDONLY, S_IRUSR | S_IWUSR);
    }
    int lockRet = flock(*fd, LOCK_EX);

    if (lockRet == -1){
        LOGE("lock file failed >> %s <<", lock_file_path);
        return 0;
    } else {
        LOGD("lock file success  >> %s <<", lock_file_path);
        return 1;
    }
}

JavaVM *g_jvm = NULL;
jobject g_obj = NULL;

jstring g_indicatorSelfPath = NULL;
jstring g_indicatorDaemonPath = NULL;
jstring g_observerSelfPath = NULL;
jstring g_observerDaemonPath = NULL;

void *start_file_observer(void *args) {
    pthread_detach(pthread_self());
	if(g_indicatorSelfPath == NULL || g_indicatorDaemonPath == NULL ||
	    g_observerSelfPath == NULL || g_observerDaemonPath == NULL){
		LOGE("parameters cannot be NULL !");
		return 1;
	}
    JNIEnv *env;
    //Attach主线程
    if((*g_jvm)->AttachCurrentThread(g_jvm, &env, NULL) == JNI_OK){
        LOGE("AttachCurrentThread() success !!");
    } else {
        LOGE("AttachCurrentThread() fail !!");
        return 1;
    }

    char* indicator_self_path = (char*)(*env)->GetStringUTFChars(env, g_indicatorSelfPath, 0);
    char* indicator_daemon_path = (char*)(*env)->GetStringUTFChars(env, g_indicatorDaemonPath, 0);
    char* observer_self_path = (char*)(*env)->GetStringUTFChars(env, g_observerSelfPath, 0);
    char* observer_daemon_path = (char*)(*env)->GetStringUTFChars(env, g_observerDaemonPath, 0);

    int lock_status = 0;
    int try_time = 0;
    int indicator_self_path_fd = -1;
    while(try_time < 3 && !(lock_status = lock_file(indicator_self_path, &indicator_self_path_fd))){
        try_time++;
        LOGD("Persistent lock myself failed and try again as %d times", try_time);
        usleep(10000);
    }
    if(!lock_status){
        LOGE("Persistent lock myself failed and exit");
        if((*g_jvm)->DetachCurrentThread(g_jvm) != JNI_OK){
            LOGE("DetachCurrentThread() failed!");
        }
        if(indicator_self_path_fd != -1) {
            close(indicator_self_path_fd);
        }
        return 1;
    }

    notify_and_waitfor(observer_self_path, observer_daemon_path);
    int indicator_daemon_path_fd = -1;
    lock_status = lock_file(indicator_daemon_path, &indicator_daemon_path_fd);

    if(indicator_self_path_fd != -1) {
        close(indicator_self_path_fd);
    }

    if(indicator_daemon_path_fd != -1) {
        close(indicator_daemon_path_fd);
    }

    if(lock_status){
        LOGE("Watch >>>>DAEMON<<<<< Died !!");
        remove_path(observer_self_path);// it`s important ! to prevent from deadlock
        remove_path(indicator_self_path);
        remove_path(indicator_daemon_path);
        java_callback(env, g_obj, DAEMON_CALLBACK_NAME);
    }
    if((*g_jvm)->DetachCurrentThread(g_jvm) != JNI_OK){
        LOGE("DetachCurrentThread() failed");
    }
    pthread_exit(0);
}

JNIEXPORT void native_doDaemon(JNIEnv *env, jobject jobj,
jstring indicatorSelfPath, jstring indicatorDaemonPath, jstring observerSelfPath, jstring observerDaemonPath){
	if(indicatorSelfPath == NULL || indicatorDaemonPath == NULL || observerSelfPath == NULL || observerDaemonPath == NULL){
		LOGE("parameters cannot be NULL !");
		return ;
	}
    //保存全局JVM以便在子线程中使用
    (*env)->GetJavaVM(env,&g_jvm);
    //不能直接赋值(g_obj = obj)
    g_obj = (*env)->NewGlobalRef(env,jobj);

    g_indicatorSelfPath = (*env)->NewGlobalRef(env,indicatorSelfPath);
    g_indicatorDaemonPath = (*env)->NewGlobalRef(env,indicatorDaemonPath);
    g_observerSelfPath = (*env)->NewGlobalRef(env,observerSelfPath);
    g_observerDaemonPath = (*env)->NewGlobalRef(env,observerDaemonPath);

    pthread_t file_observer_thread;
    int ret_file_observer_thread = pthread_create(&file_observer_thread, NULL, (void *) start_file_observer, NULL);

    if (ret_file_observer_thread != 0) {
        LOGE("Add File Observer Fail");
    } else {
        LOGE("Add File Observer Success");
    }
}


JNINativeMethod gMethods[] = {
        {"doDaemon", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", (void *) native_doDaemon},
};
const char* gClassName = "com/example/keepalivedemo/daemon/nativ/NativeDaemonAPI";

/*
* 为某一个类注册本地方法
*/
int registerNativeMethods(JNIEnv* env, const char* className, JNINativeMethod* gMethods, int numMethods) {
    jclass clazz;
    clazz = (*env)->FindClass(env, className);
    if (clazz == NULL) {
        return JNI_FALSE;
    }
    if ((*env)->RegisterNatives(env, clazz, gMethods, numMethods) < 0) {
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved){
    JNIEnv* env = NULL;
    jint result = -1;
    if ((*vm)->GetEnv(vm, (void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        return -1;
    }
    assert(env != NULL);
    if (!registerNativeMethods(env, gClassName, gMethods,sizeof(gMethods) / sizeof(gMethods[0]))){
        return -1;
    }
    result = JNI_VERSION_1_4;
    return result;
}