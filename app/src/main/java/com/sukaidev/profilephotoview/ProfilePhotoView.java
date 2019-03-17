package com.sukaidev.profilephotoview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;


/**
 * Created by sukaidev on 2019/03/12.
 */
public class ProfilePhotoView extends View {

    public static final int CIRCLE = 0;
    public static final int RECTANGLE = 1;

    private Paint mPaint;
    // 图片的resId
    private int mResId = -1;
    @SuppressWarnings("FieldCanBeLocal")
    private Bitmap mBitmap;
    // 头像类型，默认为圆形
    private int mEnumFormat = CIRCLE;
    // 矩形，用于绘制圆角矩阵
    private RectF mRectF;
    // 圆角矩形默认圆角半径
    private int mRadius = 5;
    // 着色器
    private BitmapShader mBitmapShader;
    // 矩阵，用于缩放着色器
    private Matrix matrix;

    // 保存控件的宽高
    private int mWidth;
    private int mHeight;

    // 保存图片的宽高
    private int mResWidth;
    private int mResHeight;

    public ProfilePhotoView(Context context) {
        this(context, null);
    }

    public ProfilePhotoView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProfilePhotoView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        try {
            initView(context, attrs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initView(Context context, AttributeSet attrs) throws Exception {

        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ProfilePhotoView);
            mResId = typedArray.getResourceId(R.styleable.ProfilePhotoView_src, -1);
            if (mResId == -1) {
                throw new Exception("ProfilePhotoView Need A Source Picture.");
            }
            mEnumFormat = typedArray.getInt(R.styleable.ProfilePhotoView_format, 0);
            if (mEnumFormat == RECTANGLE) {
                mRadius = typedArray.getInt(R.styleable.ProfilePhotoView_radius, 5);
            }

            typedArray.recycle();
        }

        mPaint = new Paint();
        matrix = new Matrix();
    }

    /**
     * 加载Bitmap
     */
    private void reDecode() {
        if (mResId != -1 && mWidth != 0 && mHeight != 0) {
            // 高效加载Bitmap
            mBitmap = decodeSampleBitmapFromResource(getResources(), mResId, mWidth, mHeight);
            mResWidth = mBitmap.getWidth();
            mResHeight = mBitmap.getHeight();
            // 初始化着色器
            mBitmapShader = new BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

            if (mEnumFormat == RECTANGLE) {
                mRectF = new RectF(0, 0, mWidth, mHeight);
            }
        } else if (mResId == -1) {
            throw new RuntimeException("Must set the image resource.");
        }
    }

    /**
     * Bitmap加载优化
     *
     * @param res       资源文件对象
     * @param resId     文件resId
     * @param reqWidth  要求的图片宽度，即控件的宽度
     * @param reqHeight 要求的图片高度，即控件的高度
     * @return 优化后加载的Bitmap
     */
    private Bitmap decodeSampleBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        // 加载图片
        BitmapFactory.decodeResource(res, resId, options);
        // 计算缩放比
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        // 重新加载图片并返回
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    /**
     * 计算采样率
     *
     * @param options   BitmapFactory.Options
     * @param reqWidth  要求的图片宽度
     * @param reqHeight 要求的图片高度
     * @return 采样率
     */
    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // 拿到图片的宽高
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        if (width > reqWidth || height > reqHeight) {
            int halfHeight = height / 2;
            int halfWidth = width / 2;
            // 计算缩放比，是2的n次幂
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;

        reDecode();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // 处理wrap_content
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(100, 100);
        } else if (widthSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(100, heightSize);
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSize, 100);
        }
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 将BitmapShader缩放到控件大小
        final float scaleWidth = (float) mWidth / mResWidth;
        final float scaleHeight = (float) mHeight / mResHeight;
        // 利用矩阵进行缩放
        matrix.setScale(scaleWidth, scaleHeight);
        mBitmapShader.setLocalMatrix(matrix);
        mPaint.setShader(mBitmapShader);

        final float halfWidth = (float) mWidth / 2;
        final float halfHeight = (float) mHeight / 2;

        // 内切圆半径
        final float radius = Math.min(halfWidth, halfWidth);

        if (mEnumFormat == 0) {
            canvas.drawCircle(halfWidth, halfHeight, radius, mPaint);
        } else {
            canvas.drawRoundRect(mRectF, mRadius, mRadius, mPaint);
        }
    }

    /**
     * 设置控件类型
     *
     * @param format 类型
     */
    public void setFormat(int format) {
        if (format != CIRCLE && format != RECTANGLE) {
            throw new IllegalArgumentException("Format is illegal.");
        }
        mEnumFormat = format;
    }

    /**
     * 设置圆角半径
     *
     * @param radius 圆角半径
     */
    public void setRadius(int radius) {
        if (mEnumFormat == 1 && radius > 0) {
            this.mRadius = radius;
        } else {
            throw new IllegalArgumentException("Radius is illegal.");
        }
    }

    /**
     * 设置图片
     *
     * @param resId 资源Id
     */
    public void setImageResource(@DrawableRes int resId) {
        if (mResId == -1 || mResId != resId) {
            this.mResId = resId;
//            reDecode();
        }
    }

    /**
     * 设置控件大小
     *
     * @param widthDp  控件宽DP值
     * @param heightDp 控件高DP值
     */
    public void setViewSize(int widthDp, int heightDp) {
        if (widthDp > 0 && heightDp > 0) {
            setLayoutParams(new LinearLayout.LayoutParams(dpToPx(widthDp), dpToPx(heightDp)));
        } else {
            throw new IllegalArgumentException("Size is illegal.");
        }
    }

    /**
     * dp转px工具方法
     *
     * @param dpValue dp值
     * @return 对应的px值
     */
    private int dpToPx(float dpValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
