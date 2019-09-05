package org.adhash.sdk.adhashask.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.ImageView
import org.adhash.sdk.adhashask.network.ClientAPI
import org.adhash.sdk.adhashask.pojo.AdSizes
import org.adhash.sdk.adhashask.pojo.InfoBody
import org.adhash.sdk.adhashask.pojo.ResponseFirstStep
import org.adhash.sdk.adhashask.utils.MakeInfoBodyUtil
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdHashView : ImageView {

    private val util = MakeInfoBodyUtil()
    private val connection = ClientAPI()
    private var info: InfoBody? = null
    private var adSizeStr: String? = null
    private val log = AdHashView::class.java.name

    constructor(context: Context?) : super(context) {
        context?.let {
            info = util.gatherAllInfo(it)
        }
    }
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        context?.let {
            info = util.gatherAllInfo(it)
        }
    }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        context?.let {
            info = util.gatherAllInfo(it)
        }
    }

    override fun onAttachedToWindow() {

        super.onAttachedToWindow()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        getViewSize(widthMeasureSpec, heightMeasureSpec)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    private fun getViewSize(width: Int, height: Int){
        val widthPixels = MeasureSpec.getSize(width)
        val heightPixels = MeasureSpec.getSize(height)
        adSizeStr = "${widthPixels}x$heightPixels"
        Log.e(log, "Value of adSizeStr $adSizeStr")
    }

    fun prepareBanner(publisherId: String){
        // todo get internetID, gpsCoordinates, set PublisherId, blocked Ads, recently Ads
//        adSizeStr?.let {size ->
//            info?.creatives?.add(AdSizes(size))
//        }
        info?.creatives?.add(AdSizes("300x250"))
        info?.publisherId = publisherId
    }

    fun init(firstUrlStart: String, firstUrlEnd: String){
        val gson = Gson()
        val jsonStr = gson.toJson(info)
        Log.e(log, "Value of jsonStr $jsonStr")
        info?.let { body ->
            val firstCall = connection.create(firstUrlStart).sendInfoBody(firstUrlEnd, body)
            firstCall.enqueue(object : Callback<ResponseFirstStep> {
                override fun onResponse(call: Call<ResponseFirstStep>, response: Response<ResponseFirstStep>) {
                    val jsonResponse = gson.toJson(response.body())
                    Log.e(log, "Value of response ${response.code()}")
                    Log.e(log, "Value of jsonResponse $jsonResponse")
                    val data = response.body()
                    data?.let {
                        val jsonResponse2 = gson.toJson(it)
                        Log.e(log, "Value of jsonResponse $jsonResponse2")
                    } ?: handleError(response.errorBody()?.string())
                }
                override fun onFailure(call: Call<ResponseFirstStep>, t: Throwable) {
                    Log.e(log, "Response FAIL")
                    handleError(t.message)
                }
            })
        }
    }

    private fun handleError(errorMessage: String?) {
        errorMessage?.let {
            Log.e(log, "Value of ERROR response $it")
        }
    }

}