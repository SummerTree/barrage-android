package tv.athena.live.barrage.config;

import android.os.HandlerThread;
import android.util.DisplayMetrics;

import tv.athena.live.barrage.R;
import tv.athena.live.barrage.utils.Config;
import tv.athena.live.barrage.utils.DensityUtil;
import tv.athena.live.barrage.utils.DependencyProperty;

public class BarrageConfig {
    public static final String TAG = "[Barrage]";
    public static final String TAG_RENDER = "[Barrage]render";
    public static final String TAG_QUEUE = "[Barrage]queue";
    public static final String TAG_VIEW = "[Barrage]view";
    public static final String TAG_CACHE = "[Barrage]cache";

    /**
     * 用于存储弹幕配置的Key
     */
    public static final String Channel_Config = "ChannelConfig";


    /**
     * 弹幕三个模式
     **/
    public static final int ModelClose = 0;     //关闭弹幕
    public static final int ModelLuxury = 1;    //华丽模式
    public static final int ModelSimplify = 2;  //精简模式

    /**
     * 是否支持横，竖，闪烁，悬浮模式，对应上面的三个模式
     * 精简模式下只有横，华丽模式全部支持
     */
    public static final int TYPE_NULL = 0x0;
    public static final int TYPE_HORIZONTAL = 0x001;
    public static final int TYPE_VERTICAL = 0x010;
    public static final int TYPE_FLASH = 0x100;
    public static final int TYPE_FLOATING = 0x1000;

    /**
     * 弹幕字大小和颜色
     */
    public static int Chinese = R.string.barrage_chinese;
    public static int English = R.string.barrage_english;
    public static int DefaultColor = 0xfff5f5f5;


    /**
     * 上下显示和闪现显示的弹幕最大行数
     */
    public static int VerticalLineCount = 5;
    public static int FlashCount = 10;

    /**
     * 闪烁弹幕行数
     */
    public static int FLOATING_LINE_COUNT = 10;

    //弹幕屏幕间距
    public static int FLOATING_TOP_MARGIN = DensityUtil.dip2px(BarrageContext.gContext, 8);

    /**
     * 弹幕上下间距设置，这里有区分横竖屏上面margin值
     */
    public static int LANDSCAPE_TOP_MARGIN = DensityUtil.dip2px(BarrageContext.gContext, 24);
    public static int LANDSCAPE_BOTTOM_MARGIN = DensityUtil.dip2px(BarrageContext.gContext, 0);
    public static int PORTRAIT_TOP_MARGIN = DensityUtil.dip2px(BarrageContext.gContext, 8);
    public static int PORTRAIT_BOTTOM_MARGIN = DensityUtil.dip2px(BarrageContext.gContext, 0);

    /**
     * 横屏弹幕字体大小
     **/
    public static int LANDSCAPE_SIZE_BIG = DensityUtil.dip2px(BarrageContext.gContext, 18);
    public static int LANDSCAPE_SIZE_MEDIUM = DensityUtil.dip2px(BarrageContext.gContext, 17);
    public static int LANDSCAPE_SIZE_SMALL = DensityUtil.dip2px(BarrageContext.gContext, 15);
    /**
     * 当尺寸设置为-1时，使用读取到的默认值
     */
    public static int DEFAULT_BARRAGE_SIZE = -1;

    public static int sBaseLandscapeSize = LANDSCAPE_SIZE_BIG;

    /**
     * 竖屏弹幕字体大小
     */
    public static final int PORTRAIT_SIZE_BIG = DensityUtil.dip2px(BarrageContext.gContext, 17);
    public static final int PORTRAIT_SIZE_MEDIUM = DensityUtil.dip2px(BarrageContext.gContext, 16);
    public static final int PORTRAIT_SIZE_SMALL = DensityUtil.dip2px(BarrageContext.gContext, 15);

    public static final int LANDSCAPE_SIZE_BIG_OLD = DensityUtil.sp2px(BarrageContext.gContext, 18);
    public static final int LANDSCAPE_SIZE_MEDIUM_OLD = DensityUtil.sp2px(BarrageContext.gContext, 16);
    public static final int LANDSCAPE_SIZE_SMALL_OLD = DensityUtil.sp2px(BarrageContext.gContext, 14);

    //为了扩展弹幕字体大小现在 Barrage_Size_One：小 Barrage_Size_Two：中 Barrage_Size_Three：大
    public static final int Barrage_Size_Small = 1;
    public static final int Barrage_Size_Middle = 2;
    public static final int Barrage_Size_Big = 3;

    /****新配置弹幕字体大小(5个档位，竖屏在此基础上减1，大屏加1)****/
    public static int BARRAGE_SIZE_1 = 15;
    public static int BARRAGE_SIZE_2 = 16;
    public static int BARRAGE_SIZE_3 = 17;
    public static int BARRAGE_SIZE_4 = 18;
    public static int BARRAGE_SIZE_5 = 19;

    public static final int BARRAGE_SIZE_SECTION_1 = 0;
    public static final int BARRAGE_SIZE_SECTION_2 = 1;
    public static final int BARRAGE_SIZE_SECTION_3 = 2;
    public static final int BARRAGE_SIZE_SECTION_4 = 3;
    public static final int BARRAGE_SIZE_SECTION_5 = 4;

    private static final String Barrage_Size_Section = "barrage_size_section";

    /**
     * 设备是大屏幕的判断标准，根据设计要求xxdpi以上都算。
     */
    private static int sDeviceIsLargeScreenStandard = DisplayMetrics.DENSITY_XXHIGH;
    /********************************************************/

    //默认透明度
    public static float DEFAULT_ALPHA = 0.75f;

    // 默认粗体
    public static boolean sEnableBold = true;

    public static boolean sEnableBitmapPool = true;

    // 默认有阴影
    public static boolean sEnableShadow = true;

    public static boolean sEnbaleStroke = false;

    public static boolean sEndableAssistCache = true;

    //阴影的大小范围 radius越大越模糊，越小越清晰
    public static int ShadowRadius = DensityUtil.sp2px(BarrageContext.gContext, 0.50f);
    public static float ShadowDX = 2.5f;
    public static float ShadowDY = 2f;
    public static int ShadowColor = 0xCF000000;

    //两条弹幕前后间距，上下距离
    public static int LANDSCAPE_SPACE_X = DensityUtil.dip2px(BarrageContext.gContext, 10);
    public static int PORTRAIT_SPACE_X = DensityUtil.dip2px(BarrageContext.gContext, 20);
    public static int SpaceY = DensityUtil.dip2px(BarrageContext.gContext, 5);

    //因为礼物弹幕和文字弹幕一路，但是礼物弹幕要求没有空隙（这里设置为1个像素），而文字弹幕空隙较大，于是采用canvas写字的时候留下空隙
    public static int LANDSCAPE_LINE_SPACE = 1;       //横屏间距
    public static int LANDSCAPE_LINE_SPACE_INNER = DensityUtil.dip2px(BarrageContext.gContext, 2);

    //间距
    public static int PORTRAIT_LINE_SPACE = 1;        //竖屏间距
    public static int COLUMN_SPACE = DensityUtil.dip2px(BarrageContext.gContext, 5);

    public static int LANDSCAPE_LINE_COUNT = 5;
    public static int PORTRAIT_SIMPLIFY_LINE_COUNT = 3;
    public static int PORTRAIT_LUXURY_LINE_COUNT = 10;

    //可测试环境配置
    public final static String KEY_ACTIVATE_ACC_SIZE = "ACTIVATE_ACC_SIZE";
    public static int ACTIVATE_ACC_SIZE = 1;                //整体加速开始
    public final static String KEY_ACC_DENOMINATOR = "ACC_DENOMINATOR";
    public static int ACC_DENOMINATOR = 75;       //整体加速分母
    public final static String KEY_OPEN_RANDOM_SPEED_SIZE = "OPEN_RANDOM_SPEED_SIZE";
    public static int OPEN_RANDOM_SPEED_SIZE = 35;        //随机加速开启阀值
    public final static String KEY_ACCLERATE = "ACCLERATE";
    public static float ACCLERATE = 0.35f;      //弹幕很多时 加速弹幕系数
    public final static String KEY_OPEN_DOUBLE_SPEED = "OPEN_DOUBLE_SPEED";
    public static float OPEN_DOUBLE_SPEED = 5.7f;    //开启双行
    public final static String KEY_CLOSE_DOUBLE_SPEED = "CLOSE_DOUBLE_SPEED";
    public static float CLOSE_DOUBLE_SPEED = 8.0f;  //关闭双行

    public static int DOUBLE_WHEN_SHELL_CACHE_SIZES = 71;      //当通缓存数量达到一定数量时才采用全通道
    public static int SINGLE_WHEN_SHELL_CACHE_SIZES = 13;      //当通缓存数量减到一定数量时才采用单通道
    public static int EMPTY_STRATEGY_LINE_NUMBER = 7;    //空通道判断逻辑的数量

    public static int DEFAULT_DURATION = 8500;// 横屏直播间默认持续时间
    public static int VERTICAL_DEFAULT_DURATION = 6000;// 竖屏直播间默认持续时间

    /**
     * 悬浮弹幕显示时间
     */
    public static int DEFAULT_FLOATING_TIME_FURATION = 6000;

    public static DependencyProperty<Boolean> sVideoBarrageStatusDp = new DependencyProperty<>(false);
    public static int sCurrentVideoBarrageStatus = readVideoBarrageModel();


    /**
     * 字体，透明度，模式设置
     **/
    private static final String Barrage_Alpha = "barrage_alpha";
    private static final String Barrage_Size = "barrage_size";
    private static final String Barrage_Size_Type = "barrage_size_type";
    //这个本来是竖屏字体的key，但是4.13版本开始横竖屏字体统一，之后没有了vertical_barrage_size字段，如果存在这个字段
    // 4.13版本开始之前的小字体需要换成中字体，这里要注意了
    private static final String Barrage_Model = "barrage_model";
    private static final String Video_Barrage_Model = "video_barrage_model";

    private static final String BARRAGE_ENABLE_BOLD = "barrage_enable_bold";

    private static final String BARRAGE_ENABLE_SHADOW = "barrage_enable_shadow";


    /**
     * 防遮挡弹幕引导是否已经提示
     */
    private static final String ANTI_BLOCK_HAS_TIP = "antiBlcokHasTip";
    /**
     * 用来获取防遮挡弹幕的本地开启状态
     */
    private static final String ANTI_BLOCK_ENABLE = "antiBlockBarrageEnable";
    /**
     * 用来获取防遮挡弹幕的现在开启状态
     */
    private static boolean sAntiBlockNowStatus = false;
    /**
     * 用来获取防遮挡弹幕Toast提示的次数
     */
    private static final String ANTI_BLOCK_TOAST_TIP_COUNT = "antiBlockBarrageToastTipCount";
    /**
     * 用来获取防遮挡弹幕是否使用过的
     */
    private static final String ANTI_BLOCK_NEVER_USE = "antiBlockBarrageNeverUse";
    /**
     * 上一次上报防遮挡弹幕
     */
    private static final String LAST_REPORT_ANTI_BLOCK_STATUS_TIME = "lastReportAntiBlockStatusTime";

    public static int sBorderColor = 0xffffa200;

    static {
        sAntiBlockNowStatus = isAntiBlockEnable();

        ACTIVATE_ACC_SIZE = read(KEY_ACTIVATE_ACC_SIZE, ACTIVATE_ACC_SIZE);
        ACC_DENOMINATOR = read(KEY_ACC_DENOMINATOR, ACC_DENOMINATOR);
        OPEN_RANDOM_SPEED_SIZE = read(KEY_OPEN_RANDOM_SPEED_SIZE, OPEN_RANDOM_SPEED_SIZE);
        ACCLERATE = read(KEY_ACCLERATE, ACCLERATE);
        OPEN_DOUBLE_SPEED = read(KEY_OPEN_DOUBLE_SPEED, OPEN_DOUBLE_SPEED);
        CLOSE_DOUBLE_SPEED = read(KEY_CLOSE_DOUBLE_SPEED, CLOSE_DOUBLE_SPEED);


        //这个依赖于前面的设置
        DOUBLE_WHEN_SHELL_CACHE_SIZES = getSizeBySpeed(OPEN_DOUBLE_SPEED);
        SINGLE_WHEN_SHELL_CACHE_SIZES = getSizeBySpeed(CLOSE_DOUBLE_SPEED);

//        BarrageLog.info("wolf", "DOUBLE_WHEN_SHELL_CACHE_SIZES :%d, SINGLE_WHEN_SHELL_CACHE_SIZES : %d, ACTIVATE_ACC_SIZE : %d" +
//                        ", ACC_DENOMINATOR :%d, OPEN_RANDOM_SPEED_SIZE:%d ， ACCLERATE :%f, OPEN_DOUBLE_SPEED:%f, CLOSE_DOUBLE_SPEED:%f",
//                DOUBLE_WHEN_SHELL_CACHE_SIZES, SINGLE_WHEN_SHELL_CACHE_SIZES, ACTIVATE_ACC_SIZE,
//                ACC_DENOMINATOR, OPEN_RANDOM_SPEED_SIZE, ACCLERATE, OPEN_DOUBLE_SPEED, CLOSE_DOUBLE_SPEED);
    }

    public static void configDeviceIsLargeScreenStandard(int standard) {
        sDeviceIsLargeScreenStandard = standard;
    }

    public static int getDeviceIsLargeScreenStandard() {
        return sDeviceIsLargeScreenStandard;
    }

    public static int getSizeBySpeed(float speed) {
        float wholeTime = ((float) DEFAULT_DURATION) / 1000f;
        return (int) (((wholeTime - speed) / wholeTime) * ACC_DENOMINATOR / ACCLERATE + ACTIVATE_ACC_SIZE);
    }

    public static float getBarrageAlpha() {
        return readBarrageAlpha(DEFAULT_ALPHA);
    }

    public static int getBarrageSize() {
        int section = getBarrageSizeSection();
        int size = getBarrageSizeDpBySection(section, true);
        return DensityUtil.dip2px(BarrageContext.gContext, size);
    }

    public static int getVerticalBarrageSize() {
        int section = getBarrageSizeSection();
        int size = getBarrageSizeDpBySection(section, false);
        return DensityUtil.dip2px(BarrageContext.gContext, size);
    }

    public static int getBarrageSizeBySection(int section, boolean isLandscape) {
        int size = getBarrageSizeDpBySection(section, isLandscape);
        return DensityUtil.dip2px(BarrageContext.gContext, size);
    }

    public static int covertBarrageSizeFromDp(int size) {
        return DensityUtil.dip2px(BarrageContext.gContext, size);
    }

    public static int getBarrageSizeDpBySection(int section, boolean isLandscape) {
        int size;
        switch (section) {
            case BARRAGE_SIZE_SECTION_1:
                size = BARRAGE_SIZE_1;
                break;
            case BARRAGE_SIZE_SECTION_2:
                size = BARRAGE_SIZE_2;
                break;
            case BARRAGE_SIZE_SECTION_3:
                size = BARRAGE_SIZE_3;
                break;
            case BARRAGE_SIZE_SECTION_4:
                size = BARRAGE_SIZE_4;
                break;
            case BARRAGE_SIZE_SECTION_5:
                size = BARRAGE_SIZE_5;
                break;
            default:
                size = BARRAGE_SIZE_3;
        }
        if (isLandscape) {
            //如果是大屏手机则+1
            float density = BarrageContext.gContext.getResources().getDisplayMetrics().densityDpi;
            if (density >= sDeviceIsLargeScreenStandard) {
                BarrageLog.info(TAG, "getBarrageSizeDpBySection: device has large screen");
                size += 1;
            }
        } else {
            size -= 1;
        }
        return size;
    }

    public static int getVerticalBarrageSizeByType(int sizeType) {
        switch (sizeType) {
            case Barrage_Size_Small:
                return PORTRAIT_SIZE_SMALL;
            case Barrage_Size_Big:
                return PORTRAIT_SIZE_BIG;
            case Barrage_Size_Middle:
            default:
                return PORTRAIT_SIZE_MEDIUM;
        }
    }

    public static int getBarrageSizeByType(int sizeType) {
        switch (sizeType) {
            case Barrage_Size_Small:
                return LANDSCAPE_SIZE_SMALL;
            case Barrage_Size_Big:
                return LANDSCAPE_SIZE_BIG;
            case Barrage_Size_Middle:
            default:
                return LANDSCAPE_SIZE_MEDIUM;
        }
    }

    public static int getBarrageSizeType() {
        return readBarrageSizeType();
    }

    public static int getBarrageModel() {
        return readBarrageModel();
    }

    public static void saveVideoBarrageModel(int model) {
        sCurrentVideoBarrageStatus = model;
        Config.getInstance(BarrageContext.gContext, Channel_Config).setIntSync(Video_Barrage_Model, model);
        sVideoBarrageStatusDp.set(!sVideoBarrageStatusDp.get());
    }

    public static int getVideoBarrageModel() {
        return sCurrentVideoBarrageStatus;
    }

    public static int readBarrageModel() {
        return Config.getInstance(BarrageContext.gContext, Channel_Config).getInt(Barrage_Model, ModelLuxury);
    }

    public static void saveBarrageModel(int model) {
        Config.getInstance(BarrageContext.gContext, Channel_Config).setInt(Barrage_Model, model);
    }

    public static int readVideoBarrageModel() {
        return Config.getInstance(BarrageContext.gContext, Channel_Config).getInt(Video_Barrage_Model, ModelLuxury);
    }

    public static void saveBarrageAlpha(float alpha) {
        Config.getInstance(BarrageContext.gContext, Channel_Config).setFloat(Barrage_Alpha, alpha);
    }

    public static float readBarrageAlpha(float defaultVal) {
        return Config.getInstance(BarrageContext.gContext, Channel_Config).getFloat(Barrage_Alpha, defaultVal);
    }

    public static void saveBarrageSizeType(int sizeType) {
        Config.getInstance(BarrageContext.gContext, Channel_Config).setInt(Barrage_Size_Type, sizeType);
    }

    public static void saveBarrageSizeSection(int sizeSection) {
        Config.getInstance(BarrageContext.gContext, Channel_Config).setInt(Barrage_Size_Section, sizeSection);
    }

    public static int getBarrageSizeSection() {
        int section = Config.getInstance(BarrageContext.gContext, Channel_Config).getInt(Barrage_Size_Section, -1);
        if (section < 0) {
            int type = Config.getInstance(BarrageContext.gContext, Channel_Config).getInt(Barrage_Size_Type, BarrageConfig.Barrage_Size_Middle);
            switch (type) {
                case Barrage_Size_Small:
                    section = BARRAGE_SIZE_SECTION_1;
                    break;
                case Barrage_Size_Middle:
                    section = BARRAGE_SIZE_SECTION_3;
                    break;
                case Barrage_Size_Big:
                default:
                    section = BARRAGE_SIZE_SECTION_4;
            }
            saveBarrageSizeSection(section);
        }
        return section;
    }

    public static int readBarrageSizeType() {
        //如果是旧版本的字体
        int size = Config.getInstance(BarrageContext.gContext, Channel_Config).getInt(Barrage_Size, 0);
        if (size != 0) {
            BarrageLog.info(TAG, "enter getInstance().mSetting.getInt(Barrage_Old_Version, 0) != 0");
//            //删除旧版本字体本地缓存
//            getInstance().mSetting.edit().remove(Barrage_Size).apply();
            //如果旧版本字体是以前的大号
            if (size == BarrageConfig.LANDSCAPE_SIZE_BIG_OLD) {
                //如果设置当前版本的大号
                saveBarrageSizeType(BarrageConfig.Barrage_Size_Big);
                BarrageLog.info(TAG, "saveBarrageSizeType(BarrageConfig.LANDSCAPE_SIZE_BIG);");
            } else if (size == BarrageConfig.LANDSCAPE_SIZE_MEDIUM_OLD) {
                saveBarrageSizeType(BarrageConfig.Barrage_Size_Middle);
                BarrageLog.info(TAG, "saveBarrageSizeType(BarrageConfig.LANDSCAPE_SIZE_Middle);");
            } else if (size == BarrageConfig.LANDSCAPE_SIZE_SMALL_OLD) {
                //如果旧版本字体是以前的中号
                //如果设置当前版本的中号
                saveBarrageSizeType(BarrageConfig.Barrage_Size_Small);
                BarrageLog.info(TAG, "saveBarrageSize(BarrageConfig.LANDSCAPE_SIZE_SMALL);");
            }
        }
        return Config.getInstance(BarrageContext.gContext, Channel_Config).getInt(Barrage_Size_Type, BarrageConfig.Barrage_Size_Middle);
    }


    /****  弹幕遮罩相关  begin  ***/
    public static boolean isAntiBlockHasTip() {
        return Config.getInstance(BarrageContext.gContext, Channel_Config).getBoolean(ANTI_BLOCK_HAS_TIP, false);
    }

    public static void setAntiBlockHasTip() {
        Config.getInstance(BarrageContext.gContext, Channel_Config).setBoolean(ANTI_BLOCK_HAS_TIP, true);
    }

    public static void setAntiBlockEnable(boolean enable) {
        Config.getInstance(BarrageContext.gContext, Channel_Config).setBoolean(ANTI_BLOCK_ENABLE, enable);
    }

    public static boolean isAntiBlockEnable() {
        return Config.getInstance(BarrageContext.gContext, Channel_Config).getBoolean(ANTI_BLOCK_ENABLE, false);
    }

    public static void setAntiBlockNowStatus(boolean status) {
        sAntiBlockNowStatus = status;
    }

    public static boolean getAntiBlockNowStatus() {
        return sAntiBlockNowStatus;
    }

    public static boolean isAntiBlockNeverUse() {
        return Config.getInstance(BarrageContext.gContext, Channel_Config).getBoolean(ANTI_BLOCK_NEVER_USE, true);
    }

    public static void setAntiBlcokHasUsed() {
        Config.getInstance(BarrageContext.gContext, Channel_Config).setBoolean(ANTI_BLOCK_NEVER_USE, false);
    }

    public static int getAntiBlockToastTipCount() {
        return Config.getInstance(BarrageContext.gContext, Channel_Config).getInt(ANTI_BLOCK_TOAST_TIP_COUNT, 0);
    }

    public static void setAntiBlockToastTipCount(int count) {
        Config.getInstance(BarrageContext.gContext, Channel_Config).setInt(ANTI_BLOCK_TOAST_TIP_COUNT, count);
    }

    public static long getLastReportAntiBlockStatusTime() {
        return Config.getInstance(BarrageContext.gContext, Channel_Config).getLong(LAST_REPORT_ANTI_BLOCK_STATUS_TIME, 0);
    }

    public static void setLastReportAntiBlockStatusTime(long time) {
        Config.getInstance(BarrageContext.gContext, Channel_Config).setLong(LAST_REPORT_ANTI_BLOCK_STATUS_TIME, time);
    }

    /****  弹幕遮罩相关  end  ***/


    public static float read(String tag, float defaultVal) {
        return Config.getInstance(BarrageContext.gContext, Channel_Config).getFloat(tag, defaultVal);
    }

    public static void save(String tag, float size) {
        Config.getInstance(BarrageContext.gContext, Channel_Config).setFloat(tag, size);
    }

    public static int read(String tag, int defaultVal) {
        return Config.getInstance(BarrageContext.gContext, Channel_Config).getInt(tag, defaultVal);
    }

    public static void save(String tag, int size) {
        Config.getInstance(BarrageContext.gContext, Channel_Config).setInt(tag, size);
    }


    /**
     * 设置界面打印刷新频率开关
     */
    private static boolean mBarrageRefreshPrint = false;

    public static boolean isBarrageRefreshPrint() {
        return mBarrageRefreshPrint;
    }

    public static void setBarrageRefreshPrint(boolean onOrOff) {
        mBarrageRefreshPrint = onOrOff;
    }

    private static boolean sIsFixedQueue = true;

    public static void setFixedQueue(boolean isFixed) {
        sIsFixedQueue = isFixed;
    }

    public static boolean isFixedQueue() {
        return sIsFixedQueue;
    }

    private static int sFixedLine = 1;

    public static void setFixedLine(int line) {
        sFixedLine = line;
    }

    public static int getFixedLine() {
        return sFixedLine;
    }

    private static boolean sIsTestEnv = false;

    public static boolean isTestEnv() {
        return sIsTestEnv;
    }

    public static void setTestEnv(boolean isTestEvn) {
        sIsTestEnv = isTestEvn;
    }

    /**
     * 模式转换
     *
     * @param model
     * @return
     */
    public static int modelToType(int model) {
        switch (model) {
            case ModelClose:
                return TYPE_NULL;
            case ModelSimplify:
                //只有横屏有精简模式
                return TYPE_HORIZONTAL | TYPE_FLOATING;
            case ModelLuxury:
            default:
                return TYPE_HORIZONTAL | TYPE_VERTICAL | TYPE_FLASH | TYPE_FLOATING;
        }
    }

    public static HandlerThread newStartHandlerThread(String name, int priority) {
        HandlerThread handlerThread = new HandlerThread(TAG + "-" + name + "-h", priority);
        handlerThread.start();
        return handlerThread;
    }

}
