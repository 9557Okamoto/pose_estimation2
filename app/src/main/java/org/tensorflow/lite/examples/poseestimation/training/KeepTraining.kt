package org.tensorflow.lite.examples.poseestimation.training

import android.content.Context
import org.tensorflow.lite.examples.poseestimation.camera.CameraSource
import org.tensorflow.lite.examples.poseestimation.data.Person
import java.time.LocalDateTime
import java.time.ZoneOffset

abstract class KeepTraining(name: String, context: Context) : Training(name, context) {

    private var start: LocalDateTime? = null
    private var finish: LocalDateTime? = null
    private var now: LocalDateTime? = null
    private var keeping: Boolean = false
    private var count: Int = 0
    private var time: Int = 0
//    private val listener: KeepTraining.KeepTrainingListener? = null

    override fun addPerson(person: Person) {
        if(personList.isNotEmpty()) {

            val attention: String = attention(person)

            if (attention != message2) {
//                listener?.Attention(attention)
                speak(attention)
                count=0
                return
            }

//            if (isKeep()) {
//                if (!keeping && count==0) {
//                    start = LocalDateTime.now()
//                    count++
//                } else if(keeping){
//                    finish = LocalDateTime.now()
//                    count = 0
//                    keeping = false
//                }
//                // 1秒毎にカウントするなら別のスレッドを建てる必要があると思う
//                //            speak(getResult())
//            } else {
//                keeping = true
//            }

            if(isKeep(person) && start==null){
                start = LocalDateTime.now()
            }
            if(isKeep(person) && start!=null){
                now = LocalDateTime.now()
                time = ((now?.toEpochSecond(ZoneOffset.ofHours(+9)) ?: 0) - (start?.toEpochSecond(ZoneOffset.ofHours(+9)) ?: 0)).toInt()

                if(time>=1){
                    count++
                    time = 0
                    start = null
                }
            }
            if(!isKeep(person)){
                count = 0
            }
        }

//            if(isKeep() && count == 0){
//                start = LocalDateTime.now()
//                count++
//            }else if(){
//                finish = LocalDateTime.now()
//                keeping = true
//            }
//        }

        personList = personList.plus(person)

    }

    override fun getResult(): String {
        return "${count}秒"
    }

    override fun getKcal(): Float {
        return count.toFloat()/10.0f
    }

    protected abstract fun isKeep(person: Person): Boolean

//    private fun getSecond(): Int{
//        return ((finish?.toEpochSecond(ZoneOffset.ofHours(+9)) ?: 0) -
//                (start?.toEpochSecond(ZoneOffset.ofHours(+9)) ?: 0)).toInt()
//    }

//    interface KeepTrainingListener {
//        fun Attention(data: String)
//    }
}