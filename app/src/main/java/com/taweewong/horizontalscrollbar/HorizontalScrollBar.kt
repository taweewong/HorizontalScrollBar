package com.taweewong.horizontalscrollbar

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.customview.view.AbsSavedState
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.taweewong.horizontalscrollbar.databinding.ViewHorizontalScrollBarBinding
import kotlin.math.max

class HorizontalScrollBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        inflateView(context)
        attrs?.let { _ -> setupStyleables(attrs) }
        setupView()
    }

    // View
    private lateinit var scrollBarContainer: FrameLayout
    private lateinit var scrollBarTrack: MaterialCardView
    private lateinit var scrollBarThumb: MaterialCardView

    // Style
    private var size: Float = 0f
    private var cornerRadius: Float = 0f

    private var trackWidth: Float = 0f
    private var trackColor: Int = 0

    private var thumbMinWidthPercent: Float = 0f
    private var thumbWidth: Float = 0f
    private var thumbColor: Int = 0

    // Variable
    private var transThumbX: Float = 0f
    private var maxRange: Int = 0

    private fun inflateView(context: Context) {
        val binding = ViewHorizontalScrollBarBinding.inflate(LayoutInflater.from(context), this, true)
        findView(binding)
    }

    private fun findView(binding: ViewHorizontalScrollBarBinding) {
        scrollBarContainer = binding.horizontalScrollBarContainer
        scrollBarTrack = binding.horizontalScrollBarTrack
        scrollBarThumb = binding.horizontalScrollBarThumb
    }

    private fun setupStyleables(attrs: AttributeSet) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.HorizontalScrollBar)
        size = typedArray.getDimension(R.styleable.HorizontalScrollBar_horizontal_scroll_bar_size, 0f)
        cornerRadius = typedArray.getDimension(R.styleable.HorizontalScrollBar_horizontal_scroll_bar_corner_radius, 0f)
        trackWidth = typedArray.getDimension(R.styleable.HorizontalScrollBar_horizontal_scroll_bar_track_width, 0f)
        trackColor = typedArray.getColor(R.styleable.HorizontalScrollBar_horizontal_scroll_bar_track_color, 0)
        thumbMinWidthPercent = typedArray.getFloat(R.styleable.HorizontalScrollBar_horizontal_scroll_bar_thumb_min_width_percent, 0f)
        thumbWidth = typedArray.getDimension(R.styleable.HorizontalScrollBar_horizontal_scroll_bar_thumb_width, 0f)
        thumbColor = typedArray.getColor(R.styleable.HorizontalScrollBar_horizontal_scroll_bar_thumb_color, 0)
        typedArray.recycle()
    }

    private fun setupView() {
        if (size != 0f) {
            scrollBarTrack.layoutParams.height = size.toInt()
            scrollBarThumb.layoutParams.height = size.toInt()
        }

        if (cornerRadius != 0f) {
            scrollBarTrack.radius = cornerRadius
            scrollBarThumb.radius = cornerRadius
        }

        if (trackWidth != 0f) {
            scrollBarTrack.layoutParams.width = trackWidth.toInt()
        }

        if (trackColor != 0) {
            scrollBarTrack.setCardBackgroundColor(trackColor)
        }

        if (thumbWidth != 0f) {
            scrollBarThumb.layoutParams.width = thumbWidth.toInt()
        }

        if (thumbColor != 0) {
            scrollBarThumb.setCardBackgroundColor(thumbColor)
        }

        scrollBarThumb.translationX = transThumbX
    }

    /***
     * If using `thumbMinWidthPercent` should attach this after recyclerview is already set item
     */
    fun attachToRecyclerView(recyclerView: RecyclerView) {
        // Make sure that RecyclerView item is drawn and check if thumbMinWidthPercent is required
        recyclerView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (thumbMinWidthPercent != 0f) {
                    val width = computeWidthByMinWidthPercent(recyclerView)
                    val layoutParams = scrollBarThumb.layoutParams
                    layoutParams.width = width
                    scrollBarThumb.layoutParams = layoutParams
                }
                recyclerView.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                // The total width of the whole, pay attention to the whole, including the outside of the display area.
                val paddingStart = recyclerView.paddingStart
                val paddingEnd = recyclerView.paddingEnd
                val range = recyclerView.computeHorizontalScrollRange()
                maxRange = max(maxRange, range)
                val rangeWithPadding = maxRange + paddingStart + paddingEnd
                // The current offset of thumb
                val offset = recyclerView.computeHorizontalScrollOffset()
                // Screen width of device
                val screenWidth = context.resources.displayMetrics.widthPixels
                // Calculate the width of the scroll bar
                val transMaxRange = (scrollBarTrack.width - scrollBarThumb.width).toFloat()
                // Calculate new offset of scroll bar
                val transX = offset * transMaxRange / (rangeWithPadding - screenWidth)
                transThumbX = if (transX > transMaxRange) transMaxRange else transX

                // Log values for maintenance
                Log.i("HorizontalScrollBar", "maxRange: $maxRange")
                Log.i("HorizontalScrollBar", "rangeWithPadding: $rangeWithPadding")
                Log.i("HorizontalScrollBar", "offset: $offset")
                Log.i("HorizontalScrollBar", "screenWidth: $screenWidth")
                Log.i("HorizontalScrollBar", "transMaxRange: $transMaxRange")
                Log.i("HorizontalScrollBar", "transX: $transX")
                Log.i("HorizontalScrollBar", "transThumbX: $transThumbX")
                Log.d("HorizontalScrollBar", "--------------------------------------")

                // Set translationX to scroll bar thumb
                scrollBarThumb.translationX = transThumbX
                // Check can scroll horizontal
                if (rangeWithPadding < screenWidth) {
                    scrollBarContainer.visibility = View.GONE
                } else {
                    scrollBarContainer.visibility = View.VISIBLE
                }

                super.onScrolled(recyclerView, dx, dy)
            }
        })
    }

    private fun computeWidthByMinWidthPercent(recyclerView: RecyclerView): Int {
        return try {
            if (recyclerView.computeHorizontalScrollRange() > 0) {
                val minPercent = if (thumbMinWidthPercent > 1f) 1f else thumbMinWidthPercent
                val actualPercent = recyclerView.width.toFloat() / recyclerView.computeHorizontalScrollRange().toFloat()
                val widthPercent = max(actualPercent, minPercent)
                (widthPercent * trackWidth).toInt()
            } else {
                0
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            0
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState: Parcelable? = super.onSaveInstanceState()
        superState?.let {
            val state = SavedState(superState)
            state.size = this.size
            state.cornerRadius = this.cornerRadius
            state.thumbMinWidthPercent = this.thumbMinWidthPercent
            state.trackWidth = this.trackWidth
            state.trackColor = this.trackColor
            state.thumbWidth = this.thumbWidth
            state.thumbColor = this.thumbColor
            state.transThumbX = this.transThumbX
            state.maxRange = this.maxRange
            return state
        } ?: run {
            return superState
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        when (state) {
            is SavedState -> {
                super.onRestoreInstanceState(state.superState)
                this.size = state.size
                this.cornerRadius = state.cornerRadius
                this.thumbMinWidthPercent = state.thumbMinWidthPercent
                this.trackWidth = state.trackWidth
                this.trackColor = state.trackColor
                this.thumbWidth = state.thumbWidth
                this.thumbColor = state.thumbColor
                this.transThumbX = state.transThumbX
                this.maxRange = state.maxRange
                setupView()
            }
            else -> {
                super.onRestoreInstanceState(state)
            }
        }
    }

    internal class SavedState : AbsSavedState {

        var size: Float = 0f
        var cornerRadius: Float = 0f
        var thumbMinWidthPercent: Float = 0f
        var trackWidth: Float = 0f
        var trackColor: Int = 0
        var thumbWidth: Float = 0f
        var thumbColor: Int = 0
        var endX: Float = 0f
        var transThumbX: Float = 0f
        var maxRange: Int = 0

        constructor(superState: Parcelable) : super(superState)

        constructor(source: Parcel, loader: ClassLoader?) : super(source, loader) {
            size = source.readFloat()
            cornerRadius = source.readFloat()
            thumbMinWidthPercent = source.readFloat()
            trackWidth = source.readFloat()
            trackColor = source.readInt()
            thumbWidth = source.readFloat()
            thumbColor = source.readInt()
            endX = source.readFloat()
            transThumbX = source.readFloat()
            maxRange = source.readInt()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeFloat(size)
            out.writeFloat(cornerRadius)
            out.writeFloat(thumbMinWidthPercent)
            out.writeFloat(trackWidth)
            out.writeInt(trackColor)
            out.writeFloat(thumbWidth)
            out.writeInt(thumbColor)
            out.writeFloat(endX)
            out.writeFloat(transThumbX)
            out.writeInt(maxRange)
        }

        companion object {

            @JvmField
            val CREATOR: Parcelable.ClassLoaderCreator<SavedState> = object :
                Parcelable.ClassLoaderCreator<SavedState> {
                override fun createFromParcel(source: Parcel, loader: ClassLoader): SavedState {
                    return SavedState(source, loader)
                }

                override fun createFromParcel(source: Parcel): SavedState {
                    return SavedState(source, null)
                }

                override fun newArray(size: Int): Array<SavedState> {
                    return newArray(size)
                }
            }
        }
    }
}