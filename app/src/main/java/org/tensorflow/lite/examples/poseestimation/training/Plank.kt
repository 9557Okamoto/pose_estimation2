package org.tensorflow.lite.examples.poseestimation.training

import android.content.Context
import org.tensorflow.lite.examples.poseestimation.data.Person
import kotlin.math.acos

class Plank(context: Context): KeepTraining("Plank", context) {

    private var count1: Int = 0
    // カウントする条件
    override fun isKeep(person: Person): Boolean {
        var knee_x = 0.0f
        var sho_x = 0.0f
        var hip_x = 0.0f
        var knee_y = 0.0f
        var sho_y = 0.0f
        var hip_y = 0.0f
        var nume = 0.0f
        var deno = 0.0f
        var deno1 = 0.0f
        var deno2 = 0.0f
        var cos = 0.0f
        var deg = 0.0f
        var rad = 0.0f

        knee_x = person.keyPoints[13].coordinate.x
        knee_y = person.keyPoints[13].coordinate.y
        sho_x = person.keyPoints[5].coordinate.x
        sho_y = person.keyPoints[5].coordinate.y
        hip_x = person.keyPoints[11].coordinate.x
        hip_y = person.keyPoints[11].coordinate.y

        nume = (sho_x - hip_x)*(knee_x - hip_x) + (sho_y - hip_y)*(knee_y - hip_y)
        deno1 = kotlin.math.sqrt((knee_x - hip_x)*(knee_x - hip_x) + (knee_y - hip_y)*(knee_y - hip_y))
        deno2 = kotlin.math.sqrt((sho_x - hip_x)*(sho_x - hip_x) + (sho_y - hip_y)*(sho_y - hip_y))
        deno = deno1 * deno2
        cos = nume/deno
        rad = acos(cos)
        deg = Math.toDegrees(rad.toDouble()).toFloat()

        return deg>=165
    }

    // 腰が曲がっていないか？
    override fun attention(person: Person): String {
        var knee_x = 0.0f
        var sho_x = 0.0f
        var hip_x = 0.0f
        var knee_y = 0.0f
        var sho_y = 0.0f
        var hip_y = 0.0f
        var nume = 0.0f
        var deno = 0.0f
        var deno1 = 0.0f
        var deno2 = 0.0f
        var cos = 0.0f
        var deg = 0.0f
        var rad = 0.0f

        knee_x = person.keyPoints[13].coordinate.x
        knee_y = person.keyPoints[13].coordinate.y
        sho_x = person.keyPoints[5].coordinate.x
        sho_y = person.keyPoints[5].coordinate.y
        hip_x = person.keyPoints[11].coordinate.x
        hip_y = person.keyPoints[11].coordinate.y

        nume = (sho_x - hip_x)*(knee_x - hip_x) + (sho_y - hip_y)*(knee_y - hip_y)
        deno1 = kotlin.math.sqrt((knee_x - hip_x)*(knee_x - hip_x) + (knee_y - hip_y)*(knee_y - hip_y))
        deno2 = kotlin.math.sqrt((sho_x - hip_x)*(sho_x - hip_x) + (sho_y - hip_y)*(sho_y - hip_y))
        deno = deno1 * deno2
        cos = nume/deno
        rad = acos(cos)
        deg = Math.toDegrees(rad.toDouble()).toFloat()

        return if(deg < 165){
            "腰が曲がっています"
        }else{
            "その調子です"
        }
    }

    // 消費カロリーの計算
//    override fun getKcal(): Float {
//        var count = getResult().toFloat()
//        return count/10.0f
//    }

}