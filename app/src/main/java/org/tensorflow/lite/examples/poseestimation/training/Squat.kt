package org.tensorflow.lite.examples.poseestimation.training

import android.content.Context
import org.tensorflow.lite.examples.poseestimation.data.Person
import kotlin.math.*

class Squat(context: Context): CountTraining("Squat", context) {

    private var point1: Int = 0
    private var point2: Int = 0
    private var count: Int = 0

    // 足を曲げたとき
    override fun isCount(person: Person): Boolean {

        var sho_x = 0.0f
        var sho_y = 0.0f
        var hip_x = 0.0f
        var hip_y = 0.0f
        var knee_x = 0.0f
        var knee_y = 0.0f
        var ankle_x = 0.0f
        var ankle_y = 0.0f
        var nume = 0.0f
        var deno = 0.0f
        var deno1_1 = 0.0f
        var deno1_2 = 0.0f
        var cos = 0.0f
        var deg = 0.0f
        var rad = 0.0f
        var nume2 = 0.0f
        var deno2 = 0.0f
        var deno2_1 = 0.0f
        var deno2_2 = 0.0f
        var cos2 = 0.0f
        var deg2 = 0.0f
        var rad2 = 0.0f

        sho_x = person.keyPoints[6].coordinate.x
        sho_y = person.keyPoints[6].coordinate.y
        knee_x = person.keyPoints[14].coordinate.x
        knee_y = person.keyPoints[14].coordinate.y
        ankle_x = person.keyPoints[16].coordinate.x
        ankle_y = person.keyPoints[16].coordinate.y
        hip_x = person.keyPoints[12].coordinate.x
        hip_y = person.keyPoints[12].coordinate.y

        nume = (hip_x - knee_x)*(ankle_x - knee_x) + (hip_y - knee_y)*(ankle_y - knee_y)
        deno1_1 = sqrt((ankle_x - knee_x)*(ankle_x - knee_x) + (ankle_y - knee_y)*(ankle_y - knee_y))
        deno1_2 = sqrt((hip_x - knee_x)*(hip_x - knee_x) + (hip_y - knee_y)*(hip_y - knee_y))
        deno = deno1_1 * deno1_2
        cos = nume/deno
        rad = acos(cos)
        deg = Math.toDegrees(rad.toDouble()).toFloat()

        nume2 = (sho_x - hip_x)*(knee_x - sho_x) + (sho_y - hip_y)*(knee_y - hip_y)
        deno2_1 = sqrt((sho_x - hip_x)*(sho_x - hip_x) + (sho_y - hip_y)*(sho_y - hip_y))
        deno2_2 = sqrt((knee_x - hip_x)*(knee_x - hip_x) + (knee_y - hip_y)*(knee_y - hip_y))
        deno2 = deno2_1 * deno2_2
        cos2 = nume2/deno2
        rad2 = acos(cos2)
        deg2 = Math.toDegrees(rad2.toDouble()).toFloat()

        println(deg)
        println(deg2)
        return deg<=70 && deg2>70
    }

    // 足が伸びてるか
    override fun isCountRelease(person: Person): Boolean {

        var sho_x = 0.0f
        var sho_y = 0.0f
        var hip_x = 0.0f
        var hip_y = 0.0f
        var knee_x = 0.0f
        var knee_y = 0.0f
        var ankle_x = 0.0f
        var ankle_y = 0.0f
        var nume = 0.0f
        var deno = 0.0f
        var deno1_1 = 0.0f
        var deno1_2 = 0.0f
        var cos = 0.0f
        var deg = 0.0f
        var rad = 0.0f
        var nume2 = 0.0f
        var deno2 = 0.0f
        var deno2_1 = 0.0f
        var deno2_2 = 0.0f
        var cos2 = 0.0f
        var deg2 = 0.0f
        var rad2 = 0.0f


        sho_x = person.keyPoints[6].coordinate.x
        sho_y = person.keyPoints[6].coordinate.y
        knee_x = person.keyPoints[14].coordinate.x
        knee_y = person.keyPoints[14].coordinate.y
        ankle_x = person.keyPoints[16].coordinate.x
        ankle_y = person.keyPoints[16].coordinate.y
        hip_x = person.keyPoints[12].coordinate.x
        hip_y = person.keyPoints[12].coordinate.y

        nume = (hip_x - knee_x)*(ankle_x - knee_x) + (hip_y - knee_y)*(ankle_y - knee_y)
        deno1_1 = sqrt((ankle_x - knee_x)*(ankle_x - knee_x) + (ankle_y - knee_y)*(ankle_y - knee_y))
        deno1_2 = sqrt((hip_x - knee_x)*(hip_x - knee_x) + (hip_y - knee_y)*(hip_y - knee_y))
        deno = deno1_1 * deno1_2
        cos = nume/deno
        rad = acos(cos)
        deg = Math.toDegrees(rad.toDouble()).toFloat()

        nume2 = (sho_x - hip_x)*(knee_x - sho_x) + (sho_y - hip_y)*(knee_y - hip_y)
        deno2_1 = sqrt((sho_x - hip_x)*(sho_x - hip_x) + (sho_y - hip_y)*(sho_y - hip_y))
        deno2_2 = sqrt((knee_x - hip_x)*(knee_x - hip_x) + (knee_y - hip_y)*(knee_y - hip_y))
        deno2 = deno2_1 * deno2_2
        cos2 = nume2/deno2
        rad2 = acos(cos2)
        deg2 = Math.toDegrees(rad2.toDouble()).toFloat()

        return deg >= 170 && deg2>=170
    }

    // 膝が前に出てないか等
    override fun attention(person: Person): String{

        var message: String = message1

        var hip_x = 0.0f
        var hip_y = 0.0f
        var knee_x = 0.0f
        var knee_y = 0.0f
        var ankle_x = 0.0f
        var ankle_y = 0.0f
        var nume = 0.0f
        var deno1 = 0.0f
        var deno1_1 = 0.0f
        var deno1_2 = 0.0f
        var cos = 0.0f
        var deg = 0.0f
        var rad = 0.0f

        var hip_x2 = 0.0f
        var hip_y2 = 0.0f
        var knee_x2 = 0.0f
        var knee_y2 = 0.0f
        var ankle_x2 = 0.0f
        var ankle_y2 = 0.0f
        var nume2 = 0.0f
        var deno2 = 0.0f
        var deno2_1 = 0.0f
        var deno2_2 = 0.0f
        var cos2 = 0.0f
        var deg2 = 0.0f
        var rad2 = 0.0f

        knee_x = person.keyPoints[14].coordinate.x
        knee_y = person.keyPoints[14].coordinate.y
        ankle_x = person.keyPoints[16].coordinate.x
        ankle_y = person.keyPoints[16].coordinate.y
        hip_x = person.keyPoints[12].coordinate.x
        hip_y = person.keyPoints[12].coordinate.y

        knee_x2 = personList.last().keyPoints[14].coordinate.x
        knee_y2 = personList.last().keyPoints[14].coordinate.y
        ankle_x2 = personList.last().keyPoints[16].coordinate.x
        ankle_y2 = personList.last().keyPoints[16].coordinate.y
        hip_x2 = personList.last().keyPoints[12].coordinate.x
        hip_y2 = personList.last().keyPoints[12].coordinate.y

        nume = (hip_x - knee_x)*(ankle_x - knee_x) + (hip_y - knee_y)*(ankle_y - knee_y)
        deno1_1 = sqrt((ankle_x - knee_x)*(ankle_x - knee_x) + (ankle_y - knee_y)*(ankle_y - knee_y))
        deno1_2 = sqrt((hip_x - knee_x)*(hip_x - knee_x) + (hip_y - knee_y)*(hip_y - knee_y))
        deno1 = deno1_1 * deno1_2
        cos = nume/deno1
        rad = acos(cos)
        deg = Math.toDegrees(rad.toDouble()).toFloat()

        nume2 = (hip_x2 - knee_x2)*(ankle_x2 - knee_x2) + (hip_y2 - knee_y2)*(ankle_y2 - knee_y2)
        deno2_1 = sqrt((ankle_x2 - knee_x2)*(ankle_x2 - knee_x2) + (ankle_y2 - knee_y2)*(ankle_y2 - knee_y2))
        deno2_2 = sqrt((hip_x2 - knee_x2)*(hip_x2 - knee_x2) + (hip_y2 - knee_y2)*(hip_y2 - knee_y2))
        deno2 = deno2_1 * deno2_2
        cos2 = nume2/deno2
        rad2 = acos(cos2)
        deg2 = Math.toDegrees(rad2.toDouble()).toFloat()

        if(deg <= 70 && deg2 > 70){
            point1 = 1
        }
        if(deg >= 160 && deg2 < 160){
            point2 = 1
            count++
        }
        if(deg >= 160 && deg2 < 160 && point1 == 0 && deg2 != 0.0f){
            message =  "膝をもっと曲げてください"
        }
        if(deg <= 70 && deg2 > 70 && point2 == 0 && deg2 != 0.0f && count != 0){
            message =  "膝を伸ばし切ってください"
        }
        if(deg >= 160 && deg2 < 160 && point1 != 0 && deg2 !=0.0f){
            point1 = 0
        }
        if(deg <= 70 && deg2 > 70 && point2 != 0 && deg2 != 0.0f){
            point2 = 0
        }
        return when (message) {
            message1 -> {
                message1
            }
            message3 -> {
                message3
            }
            else -> {
                message4
            }
        }

//        println(deg)
//
//        if(deg <= 80 && deg2 > 80){
//            point1 = 1
//        }
//        if(deg >= 160 && deg2 < 160){
//            point2 = 1
//            count++
//        }
//        if(deg >= 160 && deg2 < 160 && point1 == 0 && deg2 != 0.0f){
//            message = message3
//        }
//        if(deg <= 80 && deg2 > 80 && point2 == 0 && deg2 != 0.0f && count != 0){
//            message =  message4
//        }
//        if(deg >= 160 && deg2 < 160 && point1 != 0 && deg2 !=0.0f){
//            message = message5
//            point1 = 0
//        }
//        if(deg <= 80 && deg2 > 80 && point2 != 0 && deg2 != 0.0f){
//            point2 = 0
//        }
//
//        return if(message == message3){
//            message3
//        }else if(message == message4){
//            message4
//        }else if(message == message5){
//            message5
//        }else{
//            message1
//        }
    }

    // 回数 * 1回あたりの消費カロリー
//    override fun getKcal(): Float {
//        var count = getResult().toFloat()
//        return 0.4f * count
//    }

}