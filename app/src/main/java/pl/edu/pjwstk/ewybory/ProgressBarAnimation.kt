package pl.edu.pjwstk.ewybory

import android.content.Context
import android.util.AttributeSet
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.ProgressBar




//class ProgressBarAnimation(context: Context?, attrs: AttributeSet?) : Animation(context, attrs) {
//
//    private var progressBar: ProgressBar? = null
//    private var from = 0f
//    private var to = 0f
//
//    constructor(
//        progressBar: ProgressBar?,
//        from: Float,
//        to: Float
//    ) {
//        super()
//        this.progressBar = progressBar
//        this.from = from
//        this.to = to
//    }
//
//    override fun applyTransformation(
//        interpolatedTime: Float,
//        t: Transformation?
//    ) {
//        super.applyTransformation(interpolatedTime, t)
//        val value = from + (to - from) * interpolatedTime
//        progressBar!!.progress = value.toInt()
//    }
//}