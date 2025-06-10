package com.example.weather
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import kotlin.math.cos
import kotlin.math.sin

class SunriseSunset @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val DEFAULT_SIZE = 300f
    private val sweepAngle = 140
    private val starAngle = 200 - (sweepAngle - 140) / 2
    private val halfRemainAngle = (180 - sweepAngle) / 2 / 180 * Math.PI
    private var mWidth: Int = 0
    private var mHeight: Int = 0
    private lateinit var mCenterPoint: Point
    private var mDefaultSize: Int

    private lateinit var mArcPaint: Paint
    private lateinit var mRectF: RectF
    private var mRadius: Float = 0f
    private var lineYLocate: Float = 0f
    private lateinit var mAnimator: ObjectAnimator
    private lateinit var mSunPaint: Paint
    private lateinit var mTimePaint: Paint

    private var lineColor: Int = 0
    private var sunColor: Int = 0
    private var timeTextColor: Int = 0
    private var timeTextSize: Float = 0f
    private var mSunriseTime = "6:00"
    private var mSunsetTime = "19:00"

    private lateinit var mSunriseBitmap: Bitmap
    private lateinit var mSunsetBitmap: Bitmap
    private var percent = 0f
    private var mTotalTime: Int = 0
    private var isSetTime = false
    private var mNowTime: Int = 0

    init {
        mDefaultSize = dipToPx(context, DEFAULT_SIZE).toInt()
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.SunriseSunset)
            lineColor = typedArray.getColor(R.styleable.SunriseSunset_lineColor, Color.WHITE)
            sunColor = typedArray.getColor(R.styleable.SunriseSunset_sunColor, Color.YELLOW)
            timeTextColor = typedArray.getColor(R.styleable.SunriseSunset_timeTextColor, Color.GRAY)
            timeTextSize = typedArray.getDimension(R.styleable.SunriseSunset_timeTextSize, 40f)
            typedArray.recycle()
        }
        mCenterPoint = Point()
        mArcPaint = Paint().apply {
            isAntiAlias = true
            color = lineColor
            style = Paint.Style.STROKE
            strokeWidth = 3f
            pathEffect = DashPathEffect(floatArrayOf(20f, 15f), 20f)
            strokeCap = Paint.Cap.ROUND
        }
        mRectF = RectF()

        mSunPaint = Paint().apply {
            isAntiAlias = true
            color = sunColor
            style = Paint.Style.STROKE
            strokeWidth = 3f
            pathEffect = DashPathEffect(floatArrayOf(20f, 15f), 20f)
        }
        mSunriseBitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_sun)
        mSunsetBitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_sun)
        mSunPaint.colorFilter = PorterDuffColorFilter(sunColor, PorterDuff.Mode.SRC_ATOP)

        mTimePaint = Paint().apply {
            color = timeTextColor
            textSize = timeTextSize
            textAlign = Paint.Align.CENTER
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(
            internalMeasure(widthMeasureSpec, mDefaultSize),
            internalMeasure(heightMeasureSpec, (mDefaultSize * 0.55).toInt())
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = w - paddingLeft - paddingRight
        mHeight = h - paddingBottom - paddingTop
        mRadius = ((if (mWidth / 2 < mHeight) mWidth / 2 else mHeight) - dipToPx(context, 15f)).toFloat()
        mCenterPoint.x = mWidth / 2
        mCenterPoint.y = mHeight
        mRectF.left = mCenterPoint.x - mRadius
        mRectF.top = mCenterPoint.y - mRadius
        mRectF.right = mCenterPoint.x + mRadius
        mRectF.bottom = mCenterPoint.y + mRadius
        lineYLocate = (mCenterPoint.y - mRadius * sin(Math.PI / 180 * (180 - sweepAngle) / 2)).toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save()
        drawArc(canvas)
        drawText(canvas)
        canvas.restore()
    }

    private fun drawText(canvas: Canvas) {
        canvas.drawText(
            mSunriseTime,
            mCenterPoint.x - mRadius + timeTextSize,
            lineYLocate + timeTextSize + 15f,
            mTimePaint
        )
        canvas.drawText(
            mSunsetTime,
            mCenterPoint.x + mRadius - timeTextSize,
            lineYLocate + timeTextSize + 15f,
            mTimePaint
        )
        mTimePaint.textSize = 40f
        canvas.drawText(
            "日出日落",
            mCenterPoint.x.toFloat(),
            lineYLocate - timeTextSize,
            mTimePaint
        )
    }

    private fun drawArc(canvas: Canvas) {
        val nowAngle: Float
        if (percent == 0f) {
            nowAngle = 0f
            canvas.drawBitmap(
                mSunsetBitmap,
                (mCenterPoint.x - mRadius * cos(halfRemainAngle)).toFloat(),
                lineYLocate - mSunsetBitmap.height,
                mSunPaint
            )
        } else if (percent == 1f) {
            nowAngle = sweepAngle.toFloat()
            canvas.drawBitmap(
                mSunsetBitmap,
                (mCenterPoint.x + mRadius * cos(halfRemainAngle)).toFloat() - mSunsetBitmap.width,
                lineYLocate - mSunsetBitmap.height,
                mSunPaint
            )
        } else {
            nowAngle = sweepAngle * percent

            canvas.drawBitmap(
                mSunriseBitmap,
                (mCenterPoint.x - mRadius * cos((nowAngle + 18) * (Math.PI / 180) + halfRemainAngle)).toFloat() - mSunriseBitmap.width / 2,
                (mCenterPoint.y - mRadius * sin((nowAngle + 18) * (Math.PI / 180) + halfRemainAngle)).toFloat() - mSunriseBitmap.height / 2,
                mSunPaint
            )
        }
        canvas.drawArc(
            mRectF,
            starAngle + nowAngle,
            sweepAngle - nowAngle,
            false,
            mArcPaint
        )
        canvas.drawLine(0f, lineYLocate, mWidth.toFloat(), lineYLocate, mArcPaint)
        canvas.drawArc(
            mRectF,
            starAngle.toFloat(),
            nowAngle - 3.5f,
            false,
            mSunPaint
        )
    }

    fun startAnimation() {
        if (isSetTime) {
            val nowPercent = mNowTime.toFloat() / mTotalTime.toFloat()
            if (nowPercent < 0.02) {
                setPercent(0f)
            } else if (nowPercent > 0.98) {
                setPercent(1f)
            } else {
                mAnimator = ObjectAnimator.ofFloat(this, "percent", 0f, nowPercent).apply {
                    duration = (2000 + 3000 * nowPercent).toLong()
                    interpolator = LinearInterpolator()
                }
                mAnimator.start()
            }
        }
    }

    fun stopAnimator() {
        clearAnimation()
    }

    fun setTime(mSunriseTime: String, mSunsetTime: String, nowTime: String) {
        this.mSunriseTime = mSunriseTime
        this.mSunsetTime = mSunsetTime
        mTotalTime = transToMinuteTime(mSunsetTime) - transToMinuteTime(mSunriseTime)
        this.mNowTime = transToMinuteTime(nowTime) - transToMinuteTime(mSunriseTime)
        isSetTime = true
    }

    fun setPercent(percent: Float) {
        this.percent = percent
        invalidate()
    }

    private fun transToMinuteTime(time: String): Int {
        val split = time.split(":")
        return split[0].toInt() * 60 + split[1].toInt()
    }

    private fun internalMeasure(measureSpec: Int, defaultSize: Int): Int {
        val mode = MeasureSpec.getMode(measureSpec)
        val size = MeasureSpec.getSize(measureSpec)
        return when (mode) {
            MeasureSpec.EXACTLY -> size
            MeasureSpec.AT_MOST -> Math.min(size, defaultSize)
            else -> defaultSize
        }
    }

    private fun dipToPx(context: Context, dpValue: Float): Float {
        val scale = context.resources.displayMetrics.density
        return dpValue * scale + 0.5f
    }
}