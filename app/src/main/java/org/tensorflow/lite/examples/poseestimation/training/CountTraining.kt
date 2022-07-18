package org.tensorflow.lite.examples.poseestimation.training

import android.content.Context
import org.tensorflow.lite.examples.poseestimation.data.Person

abstract class CountTraining(name: String, context: Context) : Training(name, context) {

    private var count: Int = 0
    private var counting: Boolean = false

    override fun addPerson(person: Person) {
        if(personList.isNotEmpty()){
            var attention: String = attention(person)

//            if(attention == message1){
//                return
//            }
//            if(attention == message3){
//                speak(attention)
//                return
//            }
//            if(attention == message4){
//                speak(attention)
//                return
//            }
//            if(attention == message5) {
//                count++
//                return
//            }
//        }

            if(attention != message1){
                speak(attention)
                return
            }

            if(isCount(person) && !counting){
                count++
                counting = true
            }else if(isCountRelease(person)){
                counting = false
            }
        }
        personList = personList.plus(person)
    }

    override fun getResult(): String {
        return "${count}回"
    }

    override fun getKcal(): Float {
        return 0.4f * count
    }
    protected abstract fun isCount(person: Person): Boolean

    protected abstract fun isCountRelease(person: Person): Boolean


}