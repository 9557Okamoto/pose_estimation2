package org.tensorflow.lite.examples.poseestimation.training

import android.content.Context
import android.os.Bundle
import org.tensorflow.lite.examples.poseestimation.data.Person
import android.speech.tts.TextToSpeech
import java.util.*

abstract class Training(val name: String, val context: Context): TextToSpeech.OnInitListener {

    var personList : List<Person> = mutableListOf()
    private var tts: TextToSpeech? = null
    var message1 : String = ""
    var message2 : String = "その調子です"
    var message3 : String = "膝をもっと曲げてください"
    var message4: String = "膝を伸ばし切ってください"

    init {
        this.tts = TextToSpeech(context, this)
//        val params = Bundle()
//        //音のボリューム
//        params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f )
//        //音の定位
//        params.putFloat(TextToSpeech.Engine.KEY_PARAM_PAN, 1.0f)
    }

    // 骨格情報を入力
    abstract fun addPerson(person: Person)

    // 注意
    protected abstract fun attention(person: Person): String

    // 結果（途中結果）を渡す
    abstract fun getResult(): String

    // 消費カロリーを渡す
    abstract fun  getKcal(): Float

    // 入力された文字列を読み上げる
    // TODO
    protected fun speak(text: String){
        this.tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, "utteranceId")
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // ロケールの指定
            val locale = Locale.JAPAN
            if (this.tts!!.isLanguageAvailable(locale) >= TextToSpeech.LANG_AVAILABLE) {
                this.tts!!.language = Locale.JAPAN
            }

            //声の速度
            this.tts!!.setSpeechRate(1.2f)
            //声の高さ
            this.tts!!.setPitch(0.8f)
            // 音声合成の実行
            this.tts!!.speak("こんにちは", TextToSpeech.QUEUE_FLUSH, null, "utteranceId")
        }
    }
}