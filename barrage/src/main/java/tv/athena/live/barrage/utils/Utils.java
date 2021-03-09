package tv.athena.live.barrage.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.text.TextUtils;


import tv.athena.live.barrage.logger.MTPApi;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import androidx.annotation.NonNull;

@SuppressLint("DefaultLocale")
public class Utils {
    public static final String TAG = "Utils";


    private static final String CHECK_OP_NO_THROW = "checkOpNoThrow";
    private static final String OP_POST_NOTIFICATION = "OP_POST_NOTIFICATION";

    public static boolean isNotificationEnabled(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            AppOpsManager mAppOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            ApplicationInfo appInfo = context.getApplicationInfo();
            String pkg = context.getApplicationContext().getPackageName();
            int uid = appInfo.uid;
            Class appOpsClass = null; /* Context.APP_OPS_MANAGER */
            try {
                appOpsClass = Class.forName(AppOpsManager.class.getName());
                Method checkOpNoThrowMethod = appOpsClass.getMethod(CHECK_OP_NO_THROW, Integer.TYPE, Integer.TYPE, String.class);
                Field opPostNotificationValue = appOpsClass.getDeclaredField(OP_POST_NOTIFICATION);
                int value = (int) opPostNotificationValue.get(Integer.class);
                return ((int) checkOpNoThrowMethod.invoke(mAppOps, value, uid, pkg) == AppOpsManager.MODE_ALLOWED);
            } catch (Throwable e) {
                MTPApi.LOGGER.error(TAG, e);
            }
        } else {
            // api 19以下没有判断方法，默认返回true
        }
        return true;
    }


    @TargetApi(3)
    public static boolean isForeground(Context context) {
        try {
            ActivityManager activityManager = (ActivityManager) context
                    .getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
                    .getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
                if (appProcess.processName.equals(context.getPackageName())) {
                    if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                        return true;
                    } else {
                        return false;
                    }
                }
            }
            return false;
        } catch (Throwable e) {
            MTPApi.LOGGER.error("Utils", e);
            return false;
        }
    }

    @TargetApi(3)
    public static String getProcessName(@NonNull Context mContext) {
        final String DEFAULT_PROCESS_NAME = "null_name";
        try {
            int pid = android.os.Process.myPid();
            ActivityManager mActivityManager = (ActivityManager) mContext
                    .getSystemService(Context.ACTIVITY_SERVICE);
            if (mActivityManager == null) {
                MTPApi.LOGGER.error(TAG, "ActivityManager got null!");
                return DEFAULT_PROCESS_NAME;
            }
            List<ActivityManager.RunningAppProcessInfo> infoList =
                    mActivityManager.getRunningAppProcesses();
            if (infoList == null) {
                MTPApi.LOGGER.error(TAG, "getRunningAppProcesses got null!");
                return DEFAULT_PROCESS_NAME;
            }
            for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager
                    .getRunningAppProcesses()) {
                if (appProcess.pid == pid) {
                    if (TextUtils.isEmpty(appProcess.processName)) {
                        return DEFAULT_PROCESS_NAME;
                    } else {
                        return appProcess.processName;
                    }
                }
            }
            return DEFAULT_PROCESS_NAME;
        } catch (Throwable e) {
            MTPApi.LOGGER.error(TAG, e);
        }
        return DEFAULT_PROCESS_NAME;
    }

    public static boolean isOverWriteInstall(Context context) {
        if (null == context) {
            throw new NullPointerException("context may not be null");
        }
        File file = new File("/data/data/"
                + context.getPackageName()
                + "/shared_prefs/"
                + context.getPackageName()
                + ".configuration.configuration.xml"
        );
        return file.exists() && file.length() > 0;
    }

    /**
     * Check whether the service is running
     *
     * @param mContext
     * @param className service name
     * @return true running false not running
     */
    public static boolean isServiceRunning(Context mContext, String className) {
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager.getRunningServices(100);
        if (null == serviceList)
            return false;
        for (ActivityManager.RunningServiceInfo service : serviceList) {
            if (service.service.getClassName().equals(className)) {
                return true;
            }
        }
        return false;
    }

    public static String sha1(String str) {
        StringBuffer sb = new StringBuffer();
        try {
            java.security.MessageDigest sha1 = java.security.MessageDigest
                    .getInstance("SHA1");
            byte[] digest = sha1.digest(str.getBytes());
            sb.append(bytesToHexString(digest));
        } catch (NoSuchAlgorithmException e) {
            MTPApi.LOGGER.error(Utils.class, e);
        }
        return sb.toString();
    }

    public static String md5(String str) {
        StringBuffer sb = new StringBuffer();
        try {
            java.security.MessageDigest md5 = java.security.MessageDigest
                    .getInstance("MD5");
            byte[] digest = md5.digest(str.getBytes());
            sb.append(bytesToHexString(digest));
        } catch (NoSuchAlgorithmException e) {
            MTPApi.LOGGER.error(Utils.class, e);
        }
        return sb.toString();
    }

    /**
     * 通过包信息查询是否在调试模式（IDE调试的时候生成的包会返回true）
     *
     * @param context 上下文
     * @return 是否在Debug模式
     */
    public static boolean isDebugMode(Context context) {
        boolean debuggable = false;
        ApplicationInfo appInfo = null;
        PackageManager packageManager = context.getPackageManager();
        try {
            appInfo = packageManager.getApplicationInfo(context.getPackageName(),
                    PackageManager.GET_META_DATA);
        } catch (NameNotFoundException e) {
            MTPApi.LOGGER.error("Utils", e);
            e.printStackTrace();
        }
        if (appInfo != null) {
            debuggable = (appInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) > 0;
        }
        MTPApi.LOGGER.verbose("Utils", "isDebugMode debuggable: " + debuggable);
        return debuggable;
    }

    public static Object getFieldValue(Object o, String fieldName) {
        try {
            Field f;
            Class<?> clazz = o.getClass();
            while (clazz != null) {
                f = clazz.getDeclaredField(fieldName);
                if (f != null) {
                    f.setAccessible(true);
                    return f.get(o);
                }
                clazz = clazz.getSuperclass();
            }
        } catch (Exception e) {
            MTPApi.LOGGER.error(o, "setFieldValue fail : %s %s", fieldName, e);
        }
        return null;
    }

    public static void setFieldValue(Object o, String fieldName, Object value) {
        try {
            Field f;
            Class<?> clazz = o.getClass();
            while (clazz != null) {
                f = clazz.getDeclaredField(fieldName);
                if (f != null) {
                    f.setAccessible(true);
                    f.set(o, value);
                }
                clazz = clazz.getSuperclass();
            }
        } catch (Exception e) {
            MTPApi.LOGGER.error(o, "setFieldValue fail : %s %s", fieldName, e);
        }
    }

    public static String bytesToHexString(byte[] bytes) {
        if (bytes == null) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        for (byte b : bytes) {
            int val = b & 0xff;
            if (val < 0x10) {
                sb.append("0");
            }
            sb.append(Integer.toHexString(val));
        }
        return sb.toString();
    }

    public static byte[] hexStringToBytes(String hex) {
        final byte[] encodingTable = {(byte) '0', (byte) '1', (byte) '2',
                (byte) '3', (byte) '4', (byte) '5', (byte) '6', (byte) '7',
                (byte) '8', (byte) '9', (byte) 'a', (byte) 'b', (byte) 'c',
                (byte) 'd', (byte) 'e', (byte) 'f'};
        final byte[] decodingTable = new byte[128];
        for (int i = 0; i < encodingTable.length; i++) {
            decodingTable[encodingTable[i]] = (byte) i;
        }
        decodingTable['A'] = decodingTable['a'];
        decodingTable['B'] = decodingTable['b'];
        decodingTable['C'] = decodingTable['c'];
        decodingTable['D'] = decodingTable['d'];
        decodingTable['E'] = decodingTable['e'];
        decodingTable['F'] = decodingTable['f'];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte b1, b2;
        int end = hex.length();
        while (end > 0) {
            if (!isSpace(hex.charAt(end - 1))) {
                break;
            }
            end--;
        }
        int i = 0;
        while (i < end) {
            while (i < end && isSpace(hex.charAt(i))) {
                i++;
            }
            b1 = decodingTable[hex.charAt(i++)];
            while (i < end && isSpace(hex.charAt(i))) {
                i++;
            }
            b2 = decodingTable[hex.charAt(i++)];
            out.write((b1 << 4) | b2);
        }
        return out.toByteArray();
    }

    public static boolean isSpace(char c) {
        return (c == '\n' || c == '\r' || c == '\t' || c == ' ');
    }

    public static Method getMethod(final Object obj, final String name,
                                   Class<?>... parameterTypes) {
        Utils.dwAssert(obj != null);
        Class<?> cls = obj.getClass();
        Method method = null;
        try {
            method = cls.getMethod(name, parameterTypes);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        if (method == null) {
            MTPApi.LOGGER.error(null, "getDeclaredMethod return null.%s, %s", obj, name);
        }
        return method;
    }

    /**
     * able to set breakpoint when assert exception about to trigger
     *
     * @param condition assert condition
     */
    public static void dwAssert(boolean condition) {
        if (!condition) {
            assert false;
        }
//        if (BuildConfig.DEBUG && !condition) {
//            throw new AssertionError();
//        }
    }

    // used to check current thread is what we want
    public static boolean checkThreadSafe(long threadId, String msg, boolean in) {
        boolean isInThread = (Thread.currentThread().getId() == threadId);
        if (isInThread != in) {
            MTPApi.LOGGER.error("ThreadSafeCheck", msg);
        }
        return isInThread;
    }

    // used to assert the thread is what we want
    public static void assertThreadSafe(long threadId, String msg, boolean in) {
        Utils.dwAssert(checkThreadSafe(threadId, msg, in));
    }

    // used to assert the thread is the main thread
    public static void assertInMainThread(String msg) {
        assertThreadSafe(Looper.getMainLooper().getThread().getId(), msg, true);
    }

    // used to assert the thread is not the main thread
    public static void assertNotInMainThread(String msg) {
        assertThreadSafe(Looper.getMainLooper().getThread().getId(), msg, false);
    }

    /**
     * read asset file as string
     *
     * @param context
     * @param fileName asset file name
     * @return string content of asset file
     */
    public static String readAssets(Context context, String fileName) {
        String doc = "";
        try {
            InputStream is = context.getAssets().open(fileName);
            doc = readString(is);
            is.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return doc;
    }

    /**
     * read raw res as string
     *
     * @param context
     * @param resId   resource id
     * @return string content of raw res
     */
    public static String readRawRes(Context context, int resId) {
        String doc = "";
        try {
            InputStream is = context.getResources().openRawResource(resId);
            doc = readString(is);
            is.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return doc;
    }

    /**
     * read string from input stream
     *
     * @param inputStream input stream
     * @return string content of input stream
     * @throws IOException
     */
    public static String readString(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte buf[] = new byte[1024];
        int len;
        while ((len = inputStream.read(buf)) != -1) {
            outputStream.write(buf, 0, len);
        }
        outputStream.close();
        inputStream.close();
        return outputStream.toString();
    }

    public static long uint2long(int i) {
        long l = 0xffffffffL & i;
        return l;
    }

    public static String getSimOperator(Context context) {
        TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getSimOperator();
    }

    public static class ChinaOperator {
        public static final String CMCC = "CMCC";
        public static final String CTL = "CTL";
        public static final String UNICOM = "UNICOM";
        public static final String UNKNOWN = "Unknown";
    }

    public static String getOperator(Context context) {
        String sim = Utils.getSimOperator(context);
        if (sim.startsWith("46003") || sim.startsWith("46005")) {
            return ChinaOperator.CTL;
        } else if (sim.startsWith("46001") || sim.startsWith("46006")) {
            return ChinaOperator.UNICOM;
        } else if (sim.startsWith("46000") || sim.startsWith("46002")
                || sim.startsWith("46007") || sim.startsWith("46020")) {
            return ChinaOperator.CMCC;
        } else {
            return ChinaOperator.UNKNOWN;
        }
    }

    public static class NetworkType {
        public static final String Unknown = "";
        public static final String Wifi = ",w";
        public static final String Mobile3G = ",3";
        public static final String Mobile2G = ",2";
    }


    public static String fileMd5(String filePath) {
        File file = new File(filePath);
        return fileMd5(file);
    }

    public static String fileMd5(File file) {
        if (file == null) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            byte[] buffer = new byte[8192];
            int readCount = 0;
            java.security.MessageDigest md5 = java.security.MessageDigest
                    .getInstance("MD5");
            while ((readCount = in.read(buffer)) != -1) {
                md5.update(buffer, 0, readCount);
            }
            sb.append(bytesToHexString(md5.digest()));
        } catch (FileNotFoundException e) {
            MTPApi.LOGGER.error(Utils.class, e);
        } catch (NoSuchAlgorithmException e) {
            MTPApi.LOGGER.error(Utils.class, e);
        } catch (IOException e) {
            MTPApi.LOGGER.error(Utils.class, e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
        return sb.toString();
    }

    private final static int SHA1_LENGTH = 40; // SHA1 digest consists of 40 hex
    // digits, total 160 bits

    public static boolean isPasswordPlainText(String password) {
        // if password is plain text, it's length will be shorter than
        // SHA1_LENGTH
        if (!TextUtils.isEmpty(password)
                && password.length() < SHA1_LENGTH) {
            return true;
        } else {
            return false;
        }
    }

    public static String getHashIfPassIsPlainText(String password) {
        if (isPasswordPlainText(password)) {
            return Utils.sha1(password);
        } else {
            return password;
        }
    }

    public static byte[] getIPArray(int ip) {
        byte[] ipAddr = new byte[4];
        ipAddr[0] = (byte) ip;
        ipAddr[1] = (byte) (ip >>> 8);
        ipAddr[2] = (byte) (ip >>> 16);
        ipAddr[3] = (byte) (ip >>> 24);
        return ipAddr;
    }

    public static String getIpString(byte[] ip) {
        StringBuilder sb = new StringBuilder();
        sb.append(ip[0] & 0xff);
        sb.append(".");
        sb.append(ip[1] & 0xff);
        sb.append(".");
        sb.append(ip[2] & 0xff);
        sb.append(".");
        sb.append(ip[3] & 0xff);
        return sb.toString();
    }

    public static String getIpString(int ip) {
        StringBuilder sb = new StringBuilder();
        sb.append(ip & 0xff);
        sb.append(".");
        sb.append(ip >>> 8 & 0xff);
        sb.append(".");
        sb.append(ip >>> 16 & 0xff);
        sb.append(".");
        sb.append(ip >>> 24 & 0xff);
        return sb.toString();
    }

    public static int getPort(List<Integer> ports) {
        java.util.Random random = new java.util.Random(
                System.currentTimeMillis());
        return ports.get(random.nextInt(ports.size()));
    }

    public static int getLittleEndianInt(byte[] buffer, int start) {
        int i = buffer[start + 0] & 0xff;
        i |= (buffer[start + 1] << 8) & 0xff00;
        i |= (buffer[start + 2] << 16) & 0xff0000;
        i |= (buffer[start + 3] << 24) & 0xff000000;
        return i;
    }

    public static byte[] toBytes(ByteBuffer buffer) {
        if (buffer == null) {
            return new byte[0];
        }
        int savedPos = buffer.position();
        int savedLimit = buffer.limit();
        try {
            byte[] array = new byte[buffer.limit() - buffer.position()];
            if (buffer.hasArray()) {
                int offset = buffer.arrayOffset() + savedPos;
                byte[] bufferArray = buffer.array();
                System.arraycopy(bufferArray, offset, array, 0, array.length);
                return array;
            } else {
                buffer.get(array);
                return array;
            }
        } finally {
            buffer.position(savedPos);
            buffer.limit(savedLimit);
        }
    }

    public static char[] getChars(byte[] bytes) {
        Charset cs = Charset.forName("UTF-8");
        ByteBuffer bb = ByteBuffer.allocate(bytes.length);
        bb.put(bytes);
        bb.flip();
        CharBuffer cb = cs.decode(bb);
        return cb.array();
    }

    public static byte[] getBytes(char[] chars) {
        Charset cs = Charset.forName("UTF-8");
        CharBuffer cb = CharBuffer.allocate(chars.length);
        cb.put(chars);
        cb.flip();
        ByteBuffer bb = cs.encode(cb);
        return bb.array();
    }

    public static String getFileExt(String fileName) {
        final int pos = fileName.lastIndexOf(".");
        return pos == -1 ? "" : fileName.toLowerCase().substring(pos);
    }

    //conversion 1000 to 1K
    public static String getConversionOfUnits(int num) {
        String result;
        if (num <= 0) {
            result = String.valueOf(0);
        } else if (num < 1000) {
            result = String.valueOf(num);
        } else {
            int decimals = (num % 1000) / 100;
            int integer = num / 1000;
            result = String.valueOf(integer);
            result += decimals == 0 ? "K" : "." + String.valueOf(decimals) + "K";
        }
        return result;
    }

}
