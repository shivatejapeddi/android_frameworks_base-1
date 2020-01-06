package android.hardware.miuiface;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.miuiface.IMiuiFaceManager.AuthenticationCallback;
import android.hardware.miuiface.IMiuiFaceManager.EnrollmentCallback;
import android.hardware.miuiface.IMiuiFaceManager.LockoutResetCallback;
import android.hardware.miuiface.IMiuiFaceManager.RemovalCallback;
import android.net.Uri;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Binder;
import android.os.Build;
import android.os.CancellationSignal;
import android.os.CancellationSignal.OnCancelListener;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.util.Log;
import android.util.Slog;
import android.view.Surface;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MiuiFaceManagerImpl implements IMiuiFaceManager {
    private static final int CODE_ADD_LOCKOUT_RESET_CALLBACK = 1;
    private static final int CODE_AUTHENTICATE = 3;
    private static final int CODE_CANCEL_AUTHENTICATE = 4;
    private static final int CODE_CANCEL_ENROLL = 6;
    private static final int CODE_ENROLL = 5;
    private static final int CODE_EXT_CMD = 101;
    private static final int CODE_GET_AUTHENTICATOR_ID = 14;
    private static final int CODE_GET_ENROLLED_FACE_LIST = 9;
    private static final int CODE_GET_VENDOR_INFO = 17;
    private static final int CODE_HAS_ENROLLED_FACES = 12;
    private static final int CODE_POST_ENROLL = 11;
    private static final int CODE_PRE_ENROLL = 10;
    private static final int CODE_PRE_INIT_AUTHEN = 2;
    private static final int CODE_REMOVE = 7;
    private static final int CODE_RENAME = 8;
    private static final int CODE_RESET_TIMEOUT = 15;
    private static boolean DEBUG = true;
    public static final int ERROR_BINDER_CALL = 2100;
    public static final int ERROR_CANCELED = 2000;
    public static final int ERROR_SERVICE_IS_BUSY = 2001;
    public static final int ERROR_SERVICE_IS_IDLE = 2002;
    public static final int ERROR_TIME_OUT = 2003;
    private static final int FACEUNLOCK_CURRENT_USE_INVALID_MODEL = 2;
    private static final int FACEUNLOCK_CURRENT_USE_RGB_MODEL = 1;
    private static final int FACEUNLOCK_CURRENT_USE_STRUCTURE_MODEL = 0;
    private static final String FACEUNLOCK_SUPPORT_SUPERPOWER = "faceunlock_support_superpower";
    private static final String FACE_UNLOCK_3D_HAS_FEATURE = "face_unlock_has_feature_sl";
    private static final String FACE_UNLOCK_HAS_FEATURE = "face_unlock_has_feature";
    private static final String FACE_UNLOCK_HAS_FEATURE_URI = "content://settings/secure/face_unlock_has_feature";
    private static final String FACE_UNLOCK_MODEL = "face_unlock_model";
    private static final String FACE_UNLOCK_VALID_FEATURE = "face_unlock_valid_feature";
    private static final String FACE_UNLOCK_VALID_FEATURE_URI = "content://settings/secure/face_unlock_valid_feature";
    private static volatile IMiuiFaceManager INSTANCE = null;
    public static final int MG_ATTR_BLUR = 20;
    public static final int MG_ATTR_EYE_CLOSE = 22;
    public static final int MG_ATTR_EYE_OCCLUSION = 21;
    public static final int MG_ATTR_MOUTH_OCCLUSION = 23;
    public static final int MG_OPEN_CAMERA_FAIL = 1000;
    public static final int MG_OPEN_CAMERA_SUCCESS = 1001;
    public static final int MG_UNLOCK_BAD_LIGHT = 26;
    public static final int MG_UNLOCK_COMPARE_FAILURE = 12;
    public static final int MG_UNLOCK_DARKLIGHT = 30;
    public static final int MG_UNLOCK_FACE_BAD_QUALITY = 4;
    public static final int MG_UNLOCK_FACE_BLUR = 28;
    public static final int MG_UNLOCK_FACE_DOWN = 18;
    public static final int MG_UNLOCK_FACE_MULTI = 27;
    public static final int MG_UNLOCK_FACE_NOT_COMPLETE = 29;
    public static final int MG_UNLOCK_FACE_NOT_FOUND = 5;
    public static final int MG_UNLOCK_FACE_NOT_ROI = 33;
    public static final int MG_UNLOCK_FACE_OFFSET_BOTTOM = 11;
    public static final int MG_UNLOCK_FACE_OFFSET_LEFT = 8;
    public static final int MG_UNLOCK_FACE_OFFSET_RIGHT = 10;
    public static final int MG_UNLOCK_FACE_OFFSET_TOP = 9;
    public static final int MG_UNLOCK_FACE_RISE = 16;
    public static final int MG_UNLOCK_FACE_ROTATED_LEFT = 15;
    public static final int MG_UNLOCK_FACE_ROTATED_RIGHT = 17;
    public static final int MG_UNLOCK_FACE_SCALE_TOO_LARGE = 7;
    public static final int MG_UNLOCK_FACE_SCALE_TOO_SMALL = 6;
    public static final int MG_UNLOCK_FAILURE = 3;
    public static final int MG_UNLOCK_FEATURE_MISS = 24;
    public static final int MG_UNLOCK_FEATURE_VERSION_ERROR = 25;
    public static final int MG_UNLOCK_HALF_SHADOW = 32;
    public static final int MG_UNLOCK_HIGHLIGHT = 31;
    public static final int MG_UNLOCK_INVALID_ARGUMENT = 1;
    public static final int MG_UNLOCK_INVALID_HANDLE = 2;
    public static final int MG_UNLOCK_KEEP = 19;
    public static final int MG_UNLOCK_LIVENESS_FAILURE = 14;
    public static final int MG_UNLOCK_LIVENESS_WARNING = 13;
    public static final int MG_UNLOCK_OK = 0;
    private static final String POWERMODE_SUPERSAVE_OPEN = "power_supersave_mode_open";
    private static final String POWERMODE_SUPERSAVE_OPEN_URI = "content://settings/secure/power_supersave_mode_open";
    private static String RECEIVER_DESCRIPTOR = "receiver.FaceService";
    private static final int RECEIVER_ON_AUTHENTICATION_FAILED = 204;
    private static final int RECEIVER_ON_AUTHENTICATION_SUCCEEDED = 203;
    private static final int RECEIVER_ON_ENROLL_RESULT = 201;
    private static final int RECEIVER_ON_ERROR = 205;
    private static final int RECEIVER_ON_EXT_CMD = 301;
    private static final int RECEIVER_ON_LOCKOUT_RESET = 261;
    private static final int RECEIVER_ON_ON_ACQUIRED = 202;
    private static final int RECEIVER_ON_PRE_INIT = 207;
    private static final int RECEIVER_ON_REMOVED = 206;
    private static String SERVICE_DESCRIPTOR = null;
    private static String SERVICE_NAME = null;
    private static String TAG = "FaceManagerImpl_client";
    private static final int VERSION_1 = 1;
    private AuthenticationCallback mAuthenticationCallback;
    private DeathRecipient mBinderDied = new DeathRecipient() {
        public void binderDied() {
            synchronized (MiuiFaceManagerImpl.this.mBinderLock) {
                Log.e(MiuiFaceManagerImpl.TAG, "mMiuiFaceService Service Died.");
                MiuiFaceManagerImpl.this.mMiuiFaceService = null;
            }
        }
    };
    private Object mBinderLock = new Object();
    private Context mContext;
    private EnrollmentCallback mEnrollmentCallback;
    private int mFaceUnlockModel;
    private Handler mHandler;
    private boolean mHasFaceData;
    private boolean mHasInit;
    private boolean mIsSuperPower;
    private boolean mIsValid;
    private LockoutResetCallback mLockoutResetCallback;
    private IBinder mMiuiFaceService;
    private RemovalCallback mRemovalCallback;
    private Miuiface mRemovalMiuiface;
    private IBinder mServiceReceiver = new Binder() {
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int i = code;
            Parcel parcel = data;
            String access$200 = MiuiFaceManagerImpl.TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("mServiceReceiver callback: ");
            stringBuilder.append(i);
            Log.d(access$200, stringBuilder.toString());
            if (i == 261) {
                parcel.enforceInterface(MiuiFaceManagerImpl.RECEIVER_DESCRIPTOR);
                MiuiFaceManagerImpl.this.mHandler.obtainMessage(261, Integer.valueOf(data.readInt())).sendToTarget();
                reply.writeNoException();
                return true;
            } else if (i != 301) {
                long readLong;
                switch (i) {
                    case 201:
                        parcel.enforceInterface(MiuiFaceManagerImpl.RECEIVER_DESCRIPTOR);
                        long devId = data.readLong();
                        i = data.readInt();
                        MiuiFaceManagerImpl.this.mHandler.obtainMessage(201, data.readInt(), 0, new Miuiface(null, data.readInt(), i, devId)).sendToTarget();
                        reply.writeNoException();
                        return true;
                    case 202:
                        parcel.enforceInterface(MiuiFaceManagerImpl.RECEIVER_DESCRIPTOR);
                        MiuiFaceManagerImpl.this.mHandler.obtainMessage(202, data.readInt(), data.readInt(), Long.valueOf(data.readLong())).sendToTarget();
                        reply.writeNoException();
                        return true;
                    case 203:
                        parcel.enforceInterface(MiuiFaceManagerImpl.RECEIVER_DESCRIPTOR);
                        readLong = data.readLong();
                        Miuiface face = null;
                        if (data.readInt() != 0) {
                            face = (Miuiface) Miuiface.CREATOR.createFromParcel(parcel);
                        }
                        MiuiFaceManagerImpl.this.mHandler.obtainMessage(203, data.readInt(), 0, face).sendToTarget();
                        reply.writeNoException();
                        return true;
                    case 204:
                        parcel.enforceInterface(MiuiFaceManagerImpl.RECEIVER_DESCRIPTOR);
                        readLong = data.readLong();
                        MiuiFaceManagerImpl.this.mHandler.obtainMessage(204).sendToTarget();
                        reply.writeNoException();
                        return true;
                    case 205:
                        parcel.enforceInterface(MiuiFaceManagerImpl.RECEIVER_DESCRIPTOR);
                        MiuiFaceManagerImpl.this.mHandler.obtainMessage(205, data.readInt(), data.readInt(), Long.valueOf(data.readLong())).sendToTarget();
                        reply.writeNoException();
                        return true;
                    case 206:
                        parcel.enforceInterface(MiuiFaceManagerImpl.RECEIVER_DESCRIPTOR);
                        long devId2 = data.readLong();
                        int faceId = data.readInt();
                        int groupId = data.readInt();
                        int remaining = data.readInt();
                        MiuiFaceManagerImpl.this.mHandler.obtainMessage(206, remaining, 0, new Miuiface((CharSequence)null, groupId, faceId, devId2)).sendToTarget();
                        reply.writeNoException();
                        return true;
                    case 207:
                        parcel.enforceInterface(MiuiFaceManagerImpl.RECEIVER_DESCRIPTOR);
                        MiuiFaceManagerImpl.this.mHasInit = data.readInt() == 1;
                        reply.writeNoException();
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                parcel.enforceInterface(MiuiFaceManagerImpl.RECEIVER_DESCRIPTOR);
                MiuiFaceManagerImpl.this.mHandler.obtainMessage(301, Integer.valueOf(data.readInt())).sendToTarget();
                reply.writeNoException();
                return true;
            }
        }
    };
    private IBinder mToken = new Binder();

    private class ClientHandler extends Handler {
        private ClientHandler(Context context) {
            super(context.getMainLooper());
        }

        private ClientHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (MiuiFaceManagerImpl.DEBUG) {
                String access$200 = MiuiFaceManagerImpl.TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(" handleMessage  callback what:");
                stringBuilder.append(msg.what);
                Log.d(access$200, stringBuilder.toString());
            }
            int i = msg.what;
            if (i == 261) {
                MiuiFaceManagerImpl.this.sendLockoutReset();
            } else if (i != 301) {
                switch (i) {
                    case 201:
                        MiuiFaceManagerImpl.this.sendEnrollResult((Miuiface) msg.obj, msg.arg1);
                        return;
                    case 202:
                        MiuiFaceManagerImpl.this.sendAcquiredResult(((Long) msg.obj).longValue(), msg.arg1, msg.arg2);
                        return;
                    case 203:
                        MiuiFaceManagerImpl.this.sendAuthenticatedSucceeded((Miuiface) msg.obj, msg.arg1);
                        return;
                    case 204:
                        MiuiFaceManagerImpl.this.sendAuthenticatedFailed();
                        return;
                    case 205:
                        MiuiFaceManagerImpl.this.sendErrorResult(((Long) msg.obj).longValue(), msg.arg1, msg.arg2);
                        return;
                    case 206:
                        MiuiFaceManagerImpl.this.sendRemovedResult((Miuiface) msg.obj, msg.arg1);
                        return;
                    default:
                        return;
                }
            }
        }
    }

    private class FaceObserver extends ContentObserver {
        public FaceObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean r10, android.net.Uri r11) {
      // Byte code:
      //   0: aload_0
      //   1: iload_1
      //   2: aload_2
      //   3: invokespecial onChange : (ZLandroid/net/Uri;)V
      //   6: aload_2
      //   7: invokevirtual getLastPathSegment : ()Ljava/lang/String;
      //   10: astore_2
      //   11: aload_2
      //   12: invokevirtual hashCode : ()I
      //   15: istore_3
      //   16: iconst_1
      //   17: istore #4
      //   19: iconst_1
      //   20: istore #5
      //   22: iconst_1
      //   23: istore_1
      //   24: iload_3
      //   25: ldc -1709079816
      //   27: if_icmpeq -> 73
      //   30: iload_3
      //   31: ldc 280401189
      //   33: if_icmpeq -> 59
      //   36: iload_3
      //   37: ldc 1905422490
      //   39: if_icmpeq -> 45
      //   42: goto -> 87
      //   45: aload_2
      //   46: ldc 'face_unlock_valid_feature'
      //   48: invokevirtual equals : (Ljava/lang/Object;)Z
      //   51: ifeq -> 42
      //   54: iconst_1
      //   55: istore_3
      //   56: goto -> 89
      //   59: aload_2
      //   60: ldc 'power_supersave_mode_open'
      //   62: invokevirtual equals : (Ljava/lang/Object;)Z
      //   65: ifeq -> 42
      //   68: iconst_2
      //   69: istore_3
      //   70: goto -> 89
      //   73: aload_2
      //   74: ldc 'face_unlock_has_feature'
      //   76: invokevirtual equals : (Ljava/lang/Object;)Z
      //   79: ifeq -> 42
      //   82: iconst_0
      //   83: istore_3
      //   84: goto -> 89
      //   87: iconst_m1
      //   88: istore_3
      //   89: iload_3
      //   90: ifeq -> 181
      //   93: iload_3
      //   94: iconst_1
      //   95: if_icmpeq -> 142
      //   98: iload_3
      //   99: iconst_2
      //   100: if_icmpeq -> 106
      //   103: goto -> 217
      //   106: aload_0
      //   107: getfield this$0 : Landroid/hardware/miuiface/MiuiFaceManagerImpl;
      //   110: astore_2
      //   111: aload_2
      //   112: invokestatic access$900 : (Landroid/hardware/miuiface/MiuiFaceManagerImpl;)Landroid/content/Context;
      //   115: invokevirtual getContentResolver : ()Landroid/content/ContentResolver;
      //   118: ldc 'power_supersave_mode_open'
      //   120: iconst_0
      //   121: iconst_0
      //   122: invokestatic getIntForUser : (Landroid/content/ContentResolver;Ljava/lang/String;II)I
      //   125: ifeq -> 131
      //   128: goto -> 133
      //   131: iconst_0
      //   132: istore_1
      //   133: aload_2
      //   134: iload_1
      //   135: invokestatic access$1102 : (Landroid/hardware/miuiface/MiuiFaceManagerImpl;Z)Z
      //   138: pop
      //   139: goto -> 217
      //   142: aload_0
      //   143: getfield this$0 : Landroid/hardware/miuiface/MiuiFaceManagerImpl;
      //   146: astore_2
      //   147: aload_2
      //   148: invokestatic access$900 : (Landroid/hardware/miuiface/MiuiFaceManagerImpl;)Landroid/content/Context;
      //   151: invokevirtual getContentResolver : ()Landroid/content/ContentResolver;
      //   154: ldc 'face_unlock_valid_feature'
      //   156: iconst_1
      //   157: iconst_0
      //   158: invokestatic getIntForUser : (Landroid/content/ContentResolver;Ljava/lang/String;II)I
      //   161: ifeq -> 170
      //   164: iload #4
      //   166: istore_1
      //   167: goto -> 172
      //   170: iconst_0
      //   171: istore_1
      //   172: aload_2
      //   173: iload_1
      //   174: invokestatic access$1002 : (Landroid/hardware/miuiface/MiuiFaceManagerImpl;Z)Z
      //   177: pop
      //   178: goto -> 217
      //   181: aload_0
      //   182: getfield this$0 : Landroid/hardware/miuiface/MiuiFaceManagerImpl;
      //   185: astore_2
      //   186: aload_2
      //   187: invokestatic access$900 : (Landroid/hardware/miuiface/MiuiFaceManagerImpl;)Landroid/content/Context;
      //   190: invokevirtual getContentResolver : ()Landroid/content/ContentResolver;
      //   193: ldc 'face_unlock_has_feature'
      //   195: iconst_0
      //   196: iconst_0
      //   197: invokestatic getIntForUser : (Landroid/content/ContentResolver;Ljava/lang/String;II)I
      //   200: ifeq -> 209
      //   203: iload #5
      //   205: istore_1
      //   206: goto -> 211
      //   209: iconst_0
      //   210: istore_1
      //   211: aload_2
      //   212: iload_1
      //   213: invokestatic access$802 : (Landroid/hardware/miuiface/MiuiFaceManagerImpl;Z)Z
      //   216: pop
      //   217: return
        }
    }

    private class OnAuthenticationCancelListener implements OnCancelListener {
        private OnAuthenticationCancelListener() {
        }

        public void onCancel() {
            MiuiFaceManagerImpl.this.cancelAuthentication();
        }
    }

    private class OnEnrollCancelListener implements OnCancelListener {
        private OnEnrollCancelListener() {
        }

        public void onCancel() {
            MiuiFaceManagerImpl.this.cancelEnrollment();
        }
    }

    static {
        String str = "miui.face.FaceService";
        SERVICE_NAME = str;
        SERVICE_DESCRIPTOR = str;
    }

    private MiuiFaceManagerImpl(Context con) {
        this.mContext = con.getApplicationContext();
        this.mHandler = new ClientHandler(this.mContext);
        boolean equals = "ursa".equals(Build.DEVICE);
        String str = FACE_UNLOCK_VALID_FEATURE;
        String str2 = FACE_UNLOCK_HAS_FEATURE;
            ContentResolver contentResolver = this.mContext.getContentResolver();
            String str3 = FACE_UNLOCK_MODEL;
            this.mFaceUnlockModel = Secure.getIntForUser(contentResolver, str3, 1, -2);
            if (this.mFaceUnlockModel != 2) {
                Secure.putIntForUser(this.mContext.getContentResolver(), str3, 2, -2);
                if (this.mFaceUnlockModel == 0) {
                    Secure.putIntForUser(this.mContext.getContentResolver(), FACE_UNLOCK_3D_HAS_FEATURE, Secure.getIntForUser(this.mContext.getContentResolver(), str2, 0, -2), -2);
                    Secure.putIntForUser(this.mContext.getContentResolver(), str2, 0, -2);
                    Secure.putIntForUser(this.mContext.getContentResolver(), str, 1, -2);
                }
            }
        Secure.putIntForUser(this.mContext.getContentResolver(), FACEUNLOCK_SUPPORT_SUPERPOWER, 1, -2);
        FaceObserver faceObserver = new FaceObserver(this.mHandler);
        this.mContext.getContentResolver().registerContentObserver(Secure.getUriFor(str2), false, faceObserver, 0);
        faceObserver.onChange(false, Uri.parse(FACE_UNLOCK_HAS_FEATURE_URI));
        this.mContext.getContentResolver().registerContentObserver(Secure.getUriFor(str), false, faceObserver, 0);
        faceObserver.onChange(false, Uri.parse(FACE_UNLOCK_VALID_FEATURE_URI));
        this.mContext.getContentResolver().registerContentObserver(System.getUriFor("power_supersave_mode_open"), false, faceObserver, 0);
        faceObserver.onChange(false, Uri.parse(POWERMODE_SUPERSAVE_OPEN_URI));
    }

    public static IMiuiFaceManager getInstance(Context con) {
        if (INSTANCE == null) {
            synchronized (MiuiFaceManagerImpl.class) {
                if (INSTANCE == null) {
                    INSTANCE = new MiuiFaceManagerImpl(con);
                }
            }
        }
        return INSTANCE;
    }

    private void initService() throws RemoteException {
        synchronized (this.mBinderLock) {
            if (this.mMiuiFaceService == null) {
                this.mMiuiFaceService = ServiceManager.getService(SERVICE_NAME);
                if (this.mMiuiFaceService != null) {
                    this.mMiuiFaceService.linkToDeath(this.mBinderDied, 0);
                }
            }
        }
    }

    public boolean isFaceFeatureSupport() {
            return true;
    }

    public boolean isFaceUnlockInited() {
        return this.mHasInit;
    }

    private void cancelAuthentication() {
        if (DEBUG) {
            Slog.d(TAG, "cancelAuthentication ");
        }
        try {
            initService();
            if (this.mMiuiFaceService != null) {
                binderCallCancelAuthention(this.mMiuiFaceService, this.mToken, this.mContext.getPackageName());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void cancelEnrollment() {
        if (DEBUG) {
            Slog.d(TAG, "cancelEnrollment ");
        }
        try {
            initService();
            if (this.mMiuiFaceService != null) {
                binderCallCancelEnrollment(this.mMiuiFaceService, this.mToken);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public int getManagerVersion() {
        return 1;
    }

    public String getVendorInfo() {
        String res = "";
        try {
            initService();
            if (this.mMiuiFaceService != null) {
                res = binderCallGetVendorInfo(this.mMiuiFaceService, this.mContext.getPackageName());
            }
        } catch (RemoteException e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("transact fail. ");
            stringBuilder.append(e);
            Log.e(str, stringBuilder.toString());
        }
        if (DEBUG) {
            String str2 = TAG;
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("getVendorInfo, res:");
            stringBuilder2.append(res);
            Slog.d(str2, stringBuilder2.toString());
        }
        return res;
    }

    public void authenticate(CancellationSignal cancel, int flags, AuthenticationCallback callback, Handler handler, int timeout) {
        CancellationSignal cancellationSignal = cancel;
        AuthenticationCallback authenticationCallback = callback;
        if (DEBUG) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("authenticate mServiceReceiver:");
            stringBuilder.append(this.mServiceReceiver);
            Slog.d(str, stringBuilder.toString());
        }
        if (authenticationCallback != null) {
            if (cancellationSignal != null) {
                if (cancel.isCanceled()) {
                    Slog.d(TAG, "authentication already canceled");
                    return;
                }
                cancellationSignal.setOnCancelListener(new OnAuthenticationCancelListener());
            }
            useHandler(handler);
            this.mAuthenticationCallback = authenticationCallback;
            this.mEnrollmentCallback = null;
            try {
                initService();
                if (this.mMiuiFaceService != null) {
                    binderCallAuthenticate(this.mMiuiFaceService, this.mToken, -1, -1, this.mServiceReceiver, flags, this.mContext.getPackageName(), timeout);
                } else {
                    Slog.d(TAG, "mMiuiFaceService is null");
                    authenticationCallback.onAuthenticationError(2100, getMessageInfo(2100));
                }
            } catch (Exception e) {
                Log.e(TAG, "Remote exception while authenticating: ", e);
                authenticationCallback.onAuthenticationError(2100, getMessageInfo(2100));
            }
            return;
        }
        Handler handler2 = handler;
        throw new IllegalArgumentException("Must supply an authentication callback");
    }

    public void enroll(byte[] cryptoToken, CancellationSignal cancel, int flags, EnrollmentCallback enrollCallback, Surface surface, Rect detectArea, int timeout) {
        enroll(cryptoToken, cancel, flags, enrollCallback, surface, null, new RectF(detectArea), timeout);
    }

    public void enroll(byte[] cryptoToken, CancellationSignal cancel, int flags, EnrollmentCallback enrollCallback, Surface surface, RectF detectArea, RectF enrollArea, int timeout) {
        int i;
        RemoteException e;
        CancellationSignal cancellationSignal = cancel;
        EnrollmentCallback enrollmentCallback = enrollCallback;
        if (enrollmentCallback != null) {
            if (cancellationSignal != null) {
                if (cancel.isCanceled()) {
                    Slog.d(TAG, "enrollment already canceled");
                    return;
                }
                cancellationSignal.setOnCancelListener(new OnEnrollCancelListener());
            }
            try {
                initService();
                if (this.mMiuiFaceService != null) {
                    this.mEnrollmentCallback = enrollmentCallback;
                    i = 2100;
                    try {
                        binderCallEnroll(this.mMiuiFaceService, this.mToken, cryptoToken, 0, this.mServiceReceiver, flags, this.mContext.getPackageName(), surface, enrollArea, timeout);
                    } catch (RemoteException e2) {
                        e = e2;
                        Log.e(TAG, "exception in enroll: ", e);
                        enrollmentCallback.onEnrollmentError(i, getMessageInfo(i));
                        return;
                    }
                }
                i = 2100;
                Slog.d(TAG, "mMiuiFaceService is null");
                enrollmentCallback.onEnrollmentError(i, getMessageInfo(i));
            } catch (RemoteException e3) {
                e = e3;
                i = 2100;
                Log.e(TAG, "exception in enroll: ", e);
                enrollmentCallback.onEnrollmentError(i, getMessageInfo(i));
                return;
            }
            return;
        }
        throw new IllegalArgumentException("Must supply an enrollment callback");
    }

    public int extCmd(int cmd, int param) {
        int res = -1;
        try {
            initService();
            if (this.mMiuiFaceService != null) {
                res = binderCallExtCmd(this.mMiuiFaceService, this.mToken, this.mServiceReceiver, cmd, param, this.mContext.getPackageName());
            }
        } catch (RemoteException e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("transact fail. ");
            stringBuilder.append(e);
            Log.e(str, stringBuilder.toString());
        }
        if (DEBUG) {
            String str2 = TAG;
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("extCmd  cmd:");
            stringBuilder2.append(cmd);
            stringBuilder2.append(" param:");
            stringBuilder2.append(param);
            stringBuilder2.append(" res:");
            stringBuilder2.append(res);
            Slog.d(str2, stringBuilder2.toString());
        }
        return res;
    }

    public void remove(Miuiface face, RemovalCallback callback) {
        if (DEBUG) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("remove  faceId:");
            stringBuilder.append(face.getMiuifaceId());
            stringBuilder.append("  callback:");
            stringBuilder.append(callback);
            Slog.d(str, stringBuilder.toString());
        }
        try {
            initService();
            if (this.mMiuiFaceService != null) {
                this.mRemovalMiuiface = face;
                this.mRemovalCallback = callback;
                this.mEnrollmentCallback = null;
                this.mAuthenticationCallback = null;
                binderCallRemove(this.mMiuiFaceService, this.mToken, face.getMiuifaceId(), face.getGroupId(), 0, this.mServiceReceiver);
                return;
            }
            Slog.d(TAG, "mMiuiFaceService is null");
            callback.onRemovalError(face, 2100, getMessageInfo(2100));
        } catch (RemoteException e) {
            String str2 = TAG;
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("transact fail. ");
            stringBuilder2.append(e);
            Log.e(str2, stringBuilder2.toString());
        }
    }

    public void rename(int faceId, String name) {
        if (DEBUG) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("rename  faceId:");
            stringBuilder.append(faceId);
            stringBuilder.append(" name:");
            stringBuilder.append(name);
            Slog.d(str, stringBuilder.toString());
        }
        try {
            initService();
            if (this.mMiuiFaceService != null) {
                binderCallRename(this.mMiuiFaceService, faceId, 0, name);
            }
        } catch (RemoteException e) {
            String str2 = TAG;
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("transact fail. ");
            stringBuilder2.append(e);
            Log.e(str2, stringBuilder2.toString());
        }
    }

    public void addLockoutResetCallback(LockoutResetCallback callback) {
        if (DEBUG) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("addLockoutResetCallback  callback:");
            stringBuilder.append(callback);
            Slog.d(str, stringBuilder.toString());
        }
        try {
            initService();
            if (this.mMiuiFaceService != null) {
                this.mLockoutResetCallback = callback;
                binderCallAddLoackoutResetCallback(this.mMiuiFaceService, this.mServiceReceiver);
            }
        } catch (RemoteException e) {
            String str2 = TAG;
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("transact fail. ");
            stringBuilder2.append(e);
            Log.e(str2, stringBuilder2.toString());
        }
    }

    public void resetTimeout(byte[] token) {
        if (DEBUG) {
            Slog.d(TAG, "resetTimeout");
        }
        try {
            initService();
            if (this.mMiuiFaceService != null) {
                binderCallRestTimeout(this.mMiuiFaceService, token);
            }
        } catch (RemoteException e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("transact fail. ");
            stringBuilder.append(e);
            Log.e(str, stringBuilder.toString());
        }
    }

    public List<Miuiface> getEnrolledFaces() {
        StringBuilder stringBuilder;
        List<Miuiface> res = new ArrayList();
        try {
            initService();
            if (this.mMiuiFaceService != null) {
                res = binderCallGetEnrolledFaces(this.mMiuiFaceService, 0, this.mContext.getPackageName());
            }
        } catch (RemoteException e) {
            String str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("transact fail. ");
            stringBuilder.append(e);
            Log.e(str, stringBuilder.toString());
        }
        if (DEBUG) {
            String str2;
            String str3 = TAG;
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("getEnrolledFaces   res:");
            if (res == null || res.size() == 0) {
                str2 = " is null";
            } else {
                stringBuilder = new StringBuilder();
                stringBuilder.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                stringBuilder.append(res.size());
                str2 = stringBuilder.toString();
            }
            stringBuilder2.append(str2);
            Slog.d(str3, stringBuilder2.toString());
        }
        return res;
    }

    public int hasEnrolledFaces() {
        try {
            if (this.mHasFaceData && this.mIsValid) {
                return 1;
            }
            if (this.mHasFaceData) {
                return -1;
            }
            return 0;
        } catch (Exception e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("transact fail. ");
            stringBuilder.append(e);
            Log.e(str, stringBuilder.toString());
            return -2;
        }
    }

    public void preInitAuthen() {
        try {
            initService();
            if (this.mMiuiFaceService != null) {
                this.mHasInit = false;
                binderCallPpreInitAuthen(this.mMiuiFaceService, this.mToken, this.mContext.getPackageName(), this.mServiceReceiver);
            }
        } catch (RemoteException e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("transact fail. ");
            stringBuilder.append(e);
            Log.e(str, stringBuilder.toString());
        }
    }

    public boolean isReleased() {
        return false;
    }

    public void release() {
    }

    private void binderCallPpreInitAuthen(IBinder service, IBinder token, String packName, IBinder receiver) throws RemoteException {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeInterfaceToken(SERVICE_DESCRIPTOR);
        request.writeStrongBinder(token == null ? null : token);
        request.writeString(packName);
        request.writeStrongBinder(receiver);
        service.transact(2, request, reply, 0);
        reply.readException();
        request.recycle();
        reply.recycle();
    }

    private int binderCallAuthenticate(IBinder service, IBinder token, long sessionId, int userId, IBinder receiver, int flags, String packName, int timeout) throws RemoteException {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeInterfaceToken(SERVICE_DESCRIPTOR);
        IBinder iBinder = null;
        request.writeStrongBinder(token == null ? null : token);
        request.writeLong(sessionId);
        request.writeInt(userId);
        if (receiver != null) {
            iBinder = receiver;
        }
        request.writeStrongBinder(iBinder);
        request.writeInt(flags);
        request.writeString(packName);
        request.writeInt(timeout);
        service.transact(3, request, reply, 0);
        reply.readException();
        int res = reply.readInt();
        request.recycle();
        reply.recycle();
        return res;
    }

    private void binderCallCancelAuthention(IBinder service, IBinder token, String packName) throws RemoteException {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeInterfaceToken(SERVICE_DESCRIPTOR);
        request.writeStrongBinder(token == null ? null : token);
        request.writeString(packName);
        service.transact(4, request, reply, 0);
        reply.readException();
        request.recycle();
        reply.recycle();
    }

    private void binderCallEnroll(IBinder service, IBinder token, byte[] cryptoToken, int groupId, IBinder receiver, int flags, String packName, Surface surface, RectF detectArea, int timeout) throws RemoteException {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeInterfaceToken(SERVICE_DESCRIPTOR);
        IBinder iBinder = null;
        request.writeStrongBinder(token == null ? null : token);
        request.writeByteArray(cryptoToken);
        request.writeInt(groupId);
        if (receiver != null) {
            iBinder = receiver;
        }
        request.writeStrongBinder(iBinder);
        request.writeInt(flags);
        request.writeString(packName);
        if (surface != null) {
            request.writeInt(1);
            surface.writeToParcel(request, 0);
        } else {
            request.writeInt(0);
        }
        if (detectArea != null) {
            request.writeInt(1);
            detectArea.writeToParcel(request, 0);
        } else {
            request.writeInt(0);
        }
        request.writeInt(timeout);
        service.transact(5, request, reply, 0);
        reply.readException();
        request.recycle();
        reply.recycle();
    }

    private void binderCallCancelEnrollment(IBinder service, IBinder token) throws RemoteException {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeInterfaceToken(SERVICE_DESCRIPTOR);
        request.writeStrongBinder(token == null ? null : token);
        service.transact(6, request, reply, 0);
        reply.readException();
        request.recycle();
        reply.recycle();
    }

    private void binderCallRemove(IBinder service, IBinder token, int faceId, int groupId, int userId, IBinder receiver) throws RemoteException {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeInterfaceToken(SERVICE_DESCRIPTOR);
        IBinder iBinder = null;
        request.writeStrongBinder(token == null ? null : token);
        request.writeInt(faceId);
        request.writeInt(groupId);
        request.writeInt(userId);
        if (receiver != null) {
            iBinder = receiver;
        }
        request.writeStrongBinder(iBinder);
        service.transact(7, request, reply, 0);
        reply.readException();
        request.recycle();
        reply.recycle();
    }

    private void binderCallRename(IBinder service, int faceId, int groupId, String name) throws RemoteException {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeInterfaceToken(SERVICE_DESCRIPTOR);
        request.writeInt(faceId);
        request.writeInt(groupId);
        request.writeString(name);
        service.transact(8, request, reply, 0);
        reply.readException();
        request.recycle();
        reply.recycle();
    }

    private List<Miuiface> binderCallGetEnrolledFaces(IBinder service, int groupId, String packName) throws RemoteException {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeInterfaceToken(SERVICE_DESCRIPTOR);
        request.writeInt(groupId);
        request.writeString(packName);
        service.transact(9, request, reply, 0);
        reply.readException();
        List<Miuiface> res = reply.createTypedArrayList(Miuiface.CREATOR);
        request.recycle();
        reply.recycle();
        return res;
    }

    private void binderCallPreEnroll(IBinder service, IBinder token) throws RemoteException {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeInterfaceToken(SERVICE_DESCRIPTOR);
        request.writeStrongBinder(token == null ? null : token);
        service.transact(10, request, reply, 0);
        reply.readException();
        request.recycle();
        reply.recycle();
    }

    private void binderCallPostEnroll(IBinder service, IBinder token) throws RemoteException {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeInterfaceToken(SERVICE_DESCRIPTOR);
        request.writeStrongBinder(token == null ? null : token);
        service.transact(11, request, reply, 0);
        reply.readException();
        request.recycle();
        reply.recycle();
    }

    private int binderCallHasEnrolledFaces(IBinder service, int groupId, String packName) throws RemoteException {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeInterfaceToken(SERVICE_DESCRIPTOR);
        request.writeInt(groupId);
        request.writeString(packName);
        service.transact(12, request, reply, 0);
        reply.readException();
        int res = reply.readInt();
        request.recycle();
        reply.recycle();
        return res;
    }

    private long binderCallAuthenticatorId(IBinder service, String packName) throws RemoteException {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeInterfaceToken(SERVICE_DESCRIPTOR);
        request.writeString(packName);
        service.transact(14, request, reply, 0);
        reply.readException();
        long res = reply.readLong();
        request.recycle();
        reply.recycle();
        return res;
    }

    private void binderCallRestTimeout(IBinder service, byte[] cryptoToken) throws RemoteException {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeInterfaceToken(SERVICE_DESCRIPTOR);
        request.writeByteArray(cryptoToken);
        service.transact(15, request, reply, 0);
        reply.readException();
        request.recycle();
        reply.recycle();
    }

    private void binderCallAddLoackoutResetCallback(IBinder service, IBinder callback) throws RemoteException {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeInterfaceToken(SERVICE_DESCRIPTOR);
        request.writeStrongBinder(callback == null ? null : callback);
        service.transact(16, request, reply, 0);
        reply.readException();
        request.recycle();
        reply.recycle();
    }

    private String binderCallGetVendorInfo(IBinder service, String packName) throws RemoteException {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeInterfaceToken(SERVICE_DESCRIPTOR);
        request.writeString(packName);
        service.transact(17, request, reply, 0);
        reply.readException();
        String res = reply.readString();
        request.recycle();
        reply.recycle();
        return res;
    }

    private int binderCallExtCmd(IBinder service, IBinder token, IBinder receiver, int cmd, int param, String packName) throws RemoteException {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeInterfaceToken(SERVICE_DESCRIPTOR);
        IBinder iBinder = null;
        request.writeStrongBinder(token == null ? null : token);
        if (receiver != null) {
            iBinder = receiver;
        }
        request.writeStrongBinder(iBinder);
        request.writeInt(cmd);
        request.writeInt(param);
        request.writeString(packName);
        service.transact(101, request, reply, 0);
        reply.readException();
        int res = reply.readInt();
        request.recycle();
        reply.recycle();
        return res;
    }

    private void useHandler(Handler handler) {
        if (handler != null) {
            this.mHandler = new ClientHandler(handler.getLooper());
        } else if (this.mHandler.getLooper() != this.mContext.getMainLooper()) {
            this.mHandler = new ClientHandler(this.mContext.getMainLooper());
        }
    }

    private void sendRemovedResult(Miuiface face, int remaining) {
        if (this.mRemovalCallback != null) {
            if (face == null || this.mRemovalMiuiface == null) {
                Slog.d(TAG, "Received MSG_REMOVED, but face or mRemovalMiuiface is null, ");
                return;
            }
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("sendRemovedResult faceId:");
            stringBuilder.append(face.getMiuifaceId());
            stringBuilder.append("  remaining:");
            stringBuilder.append(remaining);
            Slog.d(str, stringBuilder.toString());
            int faceId = face.getMiuifaceId();
            int reqFaceId = this.mRemovalMiuiface.getMiuifaceId();
            if (reqFaceId == 0 || faceId == 0 || faceId == reqFaceId) {
                this.mRemovalCallback.onRemovalSucceeded(face, remaining);
                return;
            }
            String str2 = TAG;
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("Face id didn't match: ");
            stringBuilder2.append(faceId);
            stringBuilder2.append(" != ");
            stringBuilder2.append(reqFaceId);
            Slog.d(str2, stringBuilder2.toString());
        }
    }

    private void sendErrorResult(long deviceId, int errMsgId, int vendorCode) {
        String errorMsg = getMessageInfo(errMsgId);
        EnrollmentCallback enrollmentCallback = this.mEnrollmentCallback;
        if (enrollmentCallback != null) {
            enrollmentCallback.onEnrollmentError(errMsgId, errorMsg);
            return;
        }
        AuthenticationCallback authenticationCallback = this.mAuthenticationCallback;
        if (authenticationCallback != null) {
            authenticationCallback.onAuthenticationError(errMsgId, errorMsg);
            return;
        }
        RemovalCallback removalCallback = this.mRemovalCallback;
        if (removalCallback != null) {
            removalCallback.onRemovalError(this.mRemovalMiuiface, errMsgId, errorMsg);
        }
    }

    private void sendEnrollResult(Miuiface face, int remaining) {
        EnrollmentCallback enrollmentCallback = this.mEnrollmentCallback;
        if (enrollmentCallback != null) {
            enrollmentCallback.onEnrollmentProgress(remaining, face.getMiuifaceId());
        }
    }

    private void sendAuthenticatedSucceeded(Miuiface face, int userId) {
        AuthenticationCallback authenticationCallback = this.mAuthenticationCallback;
        if (authenticationCallback != null) {
            authenticationCallback.onAuthenticationSucceeded(face);
        }
    }

    private void sendAuthenticatedFailed() {
        AuthenticationCallback authenticationCallback = this.mAuthenticationCallback;
        if (authenticationCallback != null) {
            authenticationCallback.onAuthenticationFailed();
        }
    }

    private void sendLockoutReset() {
        LockoutResetCallback lockoutResetCallback = this.mLockoutResetCallback;
        if (lockoutResetCallback != null) {
            lockoutResetCallback.onLockoutReset();
        }
    }

    private void sendAcquiredResult(long deviceId, int clientInfo, int vendorCode) {
        String msg = getMessageInfo(clientInfo);
        EnrollmentCallback enrollmentCallback = this.mEnrollmentCallback;
        if (enrollmentCallback != null) {
            enrollmentCallback.onEnrollmentHelp(clientInfo, msg);
            return;
        }
        AuthenticationCallback authenticationCallback = this.mAuthenticationCallback;
        if (authenticationCallback != null) {
            authenticationCallback.onAuthenticationHelp(clientInfo, msg);
        }
    }

    private String getMessageInfo(int msgId) {
        String msg = "define by client";
        if (msgId == 1000) {
            return "Ã¦â€°â€œÃ¥Â¼â‚¬Ã§â€ºÂ¸Ã¦Å“ÂºÃ¥Â¤Â±Ã¨Â´Â¥";
        }
        if (msgId == 1001) {
            return "Ã¦â€°â€œÃ¥Â¼â‚¬Ã§â€ºÂ¸Ã¦Å“ÂºÃ¦Ë†ÂÃ¥Å Å¸";
        }
        if (msgId == 2000) {
            return "Ã¥Ââ€“Ã¦Â¶Ë†Ã¦Ë†ÂÃ¥Å Å¸";
        }
        if (msgId == 2100) {
            return "binderÃ¨Â°Æ’Ã§â€Â¨Ã¥Â¼â€šÃ¥Â¸Â¸";
        }
        switch (msgId) {
            case 1:
                return "Ã¥Ââ€šÃ¦â€¢Â°Ã¤Â¸ÂÃ¥ÂË†Ã¦Â³â€¢";
            case 2:
                return "HandlerÃ¤Â¸ÂÃ¦Â­Â£Ã§Â¡Â®";
            case 3:
                return "Ã¨Â§Â£Ã©â€ÂÃ¥Â¤Â±Ã¨Â´Â¥Ã¯Â¼Ë†Ã¥â€ â€¦Ã©Æ’Â¨Ã©â€â„¢Ã¨Â¯Â¯Ã¯Â¼â€°";
            case 4:
                return "Ã¤Â¼Â Ã¥â€¦Â¥Ã¦â€¢Â°Ã¦ÂÂ®Ã¨Â´Â¨Ã©â€¡ÂÃ¤Â¸ÂÃ¥Â¥Â½";
            case 5:
                return "Ã¦Å“ÂªÃ¦Â£â‚¬Ã¦Âµâ€¹Ã¥Ë†Â°Ã¤ÂºÂºÃ¨â€žÂ¸";
            case 6:
                return "Ã¨â€žÂ¸Ã¥Â¤ÂªÃ¥Â°Â";
            case 7:
                return "Ã¨â€žÂ¸Ã¥Â¤ÂªÃ¥Â¤Â§";
            case 8:
                return "Ã¨â€žÂ¸Ã¥ÂÂÃ¥Â·Â¦";
            case 9:
                return "Ã¨â€žÂ¸Ã¥ÂÂÃ¤Â¸Å ";
            case 10:
                return "Ã¨â€žÂ¸Ã¥ÂÂÃ¥ÂÂ³";
            case 11:
                return "Ã¨â€žÂ¸Ã¥ÂÂÃ¤Â¸â€¹";
            case 12:
                return "Ã¥Â¯Â¹Ã¦Â¯â€Ã¥Â¤Â±Ã¨Â´Â¥";
            case 13:
                return "Ã¦Â´Â»Ã¤Â½â€œÃ¦â€Â»Ã¥â€¡Â»Ã¨Â­Â¦Ã¥â€˜Å ";
            case 14:
                return "Ã¦Â´Â»Ã¤Â½â€œÃ¦Â£â‚¬Ã¦Âµâ€¹Ã¥Â¤Â±Ã¨Â´Â¥";
            case 15:
                return "Ã¥Ââ€˜Ã¥Â·Â¦Ã¨Â½Â¬Ã¥Â¤Â´";
            case 16:
                return "Ã¦Å Â¬Ã¥Â¤Â´";
            case 17:
                return "Ã¥Ââ€˜Ã¥ÂÂ³Ã¨Â½Â¬Ã¥Â¤Â´";
            case 18:
                return "Ã¤Â½Å½Ã¥Â¤Â´";
            case 19:
                return "Ã§Â»Â§Ã§Â»Â­Ã¨Â°Æ’Ã§â€Â¨Ã¤Â¼Â Ã¥â€¦Â¥Ã¦â€¢Â°Ã¦ÂÂ®";
            case 20:
                return "Ã¥â€ºÂ¾Ã§â€°â€¡Ã¦Â¨Â¡Ã§Â³Å ";
            case 21:
                return "Ã§Å“Â¼Ã©Æ’Â¨Ã©ÂÂ®Ã¦Å’Â¡";
            case 22:
                return "Ã©â€”Â­Ã§Å“Â¼";
            case 23:
                return "Ã¥ËœÂ´Ã©Æ’Â¨Ã©ÂÂ®Ã¦Å’Â¡";
            case 24:
                return "Ã¥Â¤â€žÃ§Ââ€ FeatureÃ¨Â¯Â»Ã¥Ââ€“Ã¥Â¼â€šÃ¥Â¸Â¸";
            case 25:
                return "FeatureÃ§â€°Ë†Ã¦Å“Â¬Ã©â€â„¢Ã¨Â¯Â¯";
            case 26:
                return "Ã¥â€¦â€°Ã§ÂºÂ¿Ã¤Â¸ÂÃ¥Â¥Â½";
            case 27:
                return "Ã¥Â¤Å¡Ã¥Â¼Â Ã¤ÂºÂºÃ¨â€žÂ¸";
            case 28:
                return "Ã¨â€žÂ¸Ã©Æ’Â¨Ã¦Â¨Â¡Ã§Â³Å ";
            case 29:
                return "Ã¨â€žÂ¸Ã©Æ’Â¨Ã¤Â¸ÂÃ¥Â®Å’Ã¦â€¢Â´";
            case 30:
                return "Ã¥â€¦â€°Ã§ÂºÂ¿Ã¥Â¤ÂªÃ¦Å¡â€”";
            case 31:
                return "Ã¥â€¦â€°Ã§ÂºÂ¿Ã¥Â¤ÂªÃ¤ÂºÂ®";
            case 32:
                return "Ã©ËœÂ´Ã©ËœÂ³Ã¨â€žÂ¸";
            default:
                if (!DEBUG) {
                    return msg;
                }
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("default msgId: ");
                stringBuilder.append(msgId);
                Log.d(str, stringBuilder.toString());
                return msg;
        }
    }
}



