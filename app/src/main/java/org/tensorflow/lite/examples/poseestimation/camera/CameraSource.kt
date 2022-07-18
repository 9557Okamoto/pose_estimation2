/* Copyright 2021 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================
*/

package org.tensorflow.lite.examples.poseestimation.camera

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import android.view.SurfaceView
import kotlinx.coroutines.suspendCancellableCoroutine
import org.tensorflow.lite.examples.poseestimation.VisualizationUtils
import org.tensorflow.lite.examples.poseestimation.YuvToRgbConverter
import org.tensorflow.lite.examples.poseestimation.data.BodyPart
import org.tensorflow.lite.examples.poseestimation.data.Person
import org.tensorflow.lite.examples.poseestimation.ml.PoseDetector
import org.tensorflow.lite.examples.poseestimation.training.Plank
import org.tensorflow.lite.examples.poseestimation.training.Squat
import org.tensorflow.lite.examples.poseestimation.training.Training
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class CameraSource(
    private val surfaceView: SurfaceView,
    private val listener: CameraSourceListener? = null
) {

    companion object {
        private const val PREVIEW_WIDTH = 640
        private const val PREVIEW_HEIGHT = 480

        /** Threshold for confidence score. */
        private const val MIN_CONFIDENCE = .2f
        private const val TAG = "Camera Source"
    }

    private val lock = Any()
    private var detector: PoseDetector? = null
    private var isTrackerEnabled = false
    private var yuvConverter: YuvToRgbConverter = YuvToRgbConverter(surfaceView.context)
    private lateinit var imageBitmap: Bitmap

    var training: Training? = null

    /** Frame count that have been processed so far in an one second interval to calculate FPS. */
    private var fpsTimer: Timer? = null
    private var frameProcessedInOneSecondInterval = 0
    private var framesPerSecond = 0

    /** Detects, characterizes, and connects to a CameraDevice (used for all camera operations) */
    private val cameraManager: CameraManager by lazy {
        val context = surfaceView.context
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    /** Readers used as buffers for camera still shots */
    private var imageReader: ImageReader? = null

    /** The [CameraDevice] that will be opened in this fragment */
    private var camera: CameraDevice? = null

    /** Internal reference to the ongoing [CameraCaptureSession] configured with our parameters */
    private var session: CameraCaptureSession? = null

    /** [HandlerThread] where all buffer reading operations run */
    private var imageReaderThread: HandlerThread? = null

    /** [Handler] corresponding to [imageReaderThread] */
    private var imageReaderHandler: Handler? = null
    private var cameraId: String = ""

//    private var i = 0
//    private var elb_x = 0.0f
//    private var sho_x = 0.0f
//    private var hip_x = 0.0f
//    private var elb_y = 0.0f
//    private var sho_y = 0.0f
//    private var hip_y = 0.0f
//    private var nume = 0.0f
//    private var deno = 0.0f
//    private var deno1 = 0.0f
//    private var deno2 = 0.0f
//    private var cos = 0.0f
//    private var deg = 0.0f
//    private var rad = 0.0f
//    private var elb: List<Float>? = null
//    private var sho: FloatArray? = null
//    private var hip: FloatArray? = null

    // カメラの初期化
    suspend fun initCamera() {
        camera = openCamera(cameraManager, cameraId)
        // YUV(輝度信号（Y）と、輝度信号と青色成分の差（U）、輝度信号と赤色成分の差（V）)でカメラの画像を取得
        imageReader =
            ImageReader.newInstance(PREVIEW_WIDTH, PREVIEW_HEIGHT, ImageFormat.YUV_420_888, 3)
        // カメラの画像を取得する度に呼ばれる
        imageReader?.setOnImageAvailableListener({ reader ->
            // 最新の画像を取得する
            val image = reader.acquireLatestImage()
            if (image != null) {
                if (!::imageBitmap.isInitialized) {
                    // ARGB(透明度, 赤, 緑, 青成分)の画像を生成
                    imageBitmap =
                        Bitmap.createBitmap(
                            PREVIEW_WIDTH,
                            PREVIEW_HEIGHT,
                            Bitmap.Config.ARGB_8888
                        )
                }

                // カメラの画像をYUV->ARGBに変換しimageBitmapに代入
                yuvConverter.yuvToRgb(image, imageBitmap)


                // 画像を回転する
                val rotateMatrix = Matrix()
                rotateMatrix.postRotate(90.0f)
                val rotatedBitmap = Bitmap.createBitmap(
                    imageBitmap, 0, 0, PREVIEW_WIDTH, PREVIEW_HEIGHT,
                    rotateMatrix, false
                )

                // ポーズ推定をする
                processImage(rotatedBitmap)
                image.close()
            }
        }, imageReaderHandler)

        imageReader?.surface?.let { surface ->
            session = createSession(listOf(surface))
            val cameraRequest = camera?.createCaptureRequest(
                CameraDevice.TEMPLATE_PREVIEW
            )?.apply {
                addTarget(surface)
            }
            cameraRequest?.build()?.let {
                session?.setRepeatingRequest(it, null, null)
            }
        }
    }

    private suspend fun createSession(targets: List<Surface>): CameraCaptureSession =
        suspendCancellableCoroutine { cont ->
            camera?.createCaptureSession(targets, object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(captureSession: CameraCaptureSession) =
                    cont.resume(captureSession)

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    cont.resumeWithException(Exception("Session error"))
                }
            }, null)
        }

    @SuppressLint("MissingPermission")
    private suspend fun openCamera(manager: CameraManager, cameraId: String): CameraDevice =
        suspendCancellableCoroutine { cont ->
            manager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) = cont.resume(camera)

                override fun onDisconnected(camera: CameraDevice) {
                    camera.close()
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    if (cont.isActive) cont.resumeWithException(Exception("Camera error"))
                }
            }, imageReaderHandler)
        }

    fun prepareCamera() {
        for (cameraId in cameraManager.cameraIdList) {
            val characteristics = cameraManager.getCameraCharacteristics(cameraId)

            // We don't use a front facing camera in this sample.
            val cameraDirection = characteristics.get(CameraCharacteristics.LENS_FACING)
            if (cameraDirection != null &&
                cameraDirection == CameraCharacteristics.LENS_FACING_FRONT
            ) {
                continue
            }
            this.cameraId = cameraId
        }
    }

    fun setDetector(detector: PoseDetector) {
        synchronized(lock) {
            if (this.detector != null) {
                this.detector?.close()
                this.detector = null
            }
            this.detector = detector
        }
    }

    /**
     * Set Tracker for Movenet MuiltiPose model.
     */

    fun resume() {
        imageReaderThread = HandlerThread("imageReaderThread").apply { start() }
        imageReaderHandler = Handler(imageReaderThread!!.looper)
        fpsTimer = Timer()
        fpsTimer?.scheduleAtFixedRate(
            object : TimerTask() {
                override fun run() {
                    framesPerSecond = frameProcessedInOneSecondInterval
                    frameProcessedInOneSecondInterval = 0
                }
            },
            0,
            1000
        )
    }

    fun close() {
        session?.close()
        session = null
        camera?.close()
        camera = null
        imageReader?.close()
        imageReader = null
        stopImageReaderThread()
        detector?.close()
        detector = null
        fpsTimer?.cancel()
        fpsTimer = null
        frameProcessedInOneSecondInterval = 0
        framesPerSecond = 0
    }

    // process image
    private fun processImage(bitmap: Bitmap) {
        val persons = mutableListOf<Person>()
        var classificationResult: List<Pair<String, Float>>? = null

        // 並列処理で付加軽減。lockは{}の処理が終わるまで次に行かないようにする？
        synchronized(lock) {

            // MoveNet.ktのestimatePoses(骨格推定)が呼ばれる。
            detector?.estimatePoses(bitmap)?.let {
                persons.addAll(it)
            }
        }
        frameProcessedInOneSecondInterval++
        if (frameProcessedInOneSecondInterval == 1) {
            // send fps to view
            listener?.onFPSListener(framesPerSecond)
        }

        // if the model returns only one item, show that item's score.
        if (persons.isNotEmpty()) {
            listener?.onDetectedInfo(persons[0].score, classificationResult)
        }

//        listener?.xValue(persons[0].keyPoints[0].coordinate.x)
//        listener?.yValue(persons[0].keyPoints[0].coordinate.y)
//        listener?.nameValue(persons[0].keyPoints[0].bodyPart)

        if(training != null){
            training!!.addPerson(persons[0])
            listener?.CountListener(training!!.getResult())
            listener?.CalorieListener(training!!.getKcal().toString())
        }


        visualize(persons, bitmap)
    }

    // personsの情報から骨格を描画する
    private fun visualize(persons: List<Person>, bitmap: Bitmap) {
        //var training: Training = Prank();
        //if(training.isContinue()){

        //}

        // bodyPartに骨格の部分の名前、coordinateにx,y座標、scoreに信頼度の17個セットがkeyPointsに格納されている
        //println(persons[0].keyPoints.map { "lll: ${it.bodyPart}, ${it.coordinate}, ${it.score}"})

        //elb_x = persons[0].keyPoints[7].coordinate.x
        //elb_y = persons[0].keyPoints[7].coordinate.y
        //sho_x = persons[0].keyPoints[5].coordinate.x
        //sho_y = persons[0].keyPoints[5].coordinate.y
        //hip_x = persons[0].keyPoints[11].coordinate.x
        //hip_y = persons[0].keyPoints[11].coordinate.y

        //elb.add(elb_x,elb_y)
        //println(elb)


        //nume = (hip_x - sho_x)*(elb_x - sho_x) + (hip_y - sho_y)*(elb_y - sho_y)
        //deno1 = kotlin.math.sqrt((elb_x - sho_x)*(elb_x - sho_x) + (elb_y - sho_y)*(elb_y - sho_y))
        //deno2 = kotlin.math.sqrt((hip_x - sho_x)*(hip_x - sho_x) + (hip_y - sho_y)*(hip_y - sho_y))
        //deno = deno1 * deno2
        //cos = nume/deno
        //rad = acos(cos)
        //deg = Math.toDegrees(rad.toDouble()).toFloat()

        //listener?.taisei(deg)


        val outputBitmap = VisualizationUtils.drawBodyKeypoints(
            bitmap,
            persons.filter { it.score > MIN_CONFIDENCE }, isTrackerEnabled
        )

        val holder = surfaceView.holder
        val surfaceCanvas = holder.lockCanvas()
        surfaceCanvas?.let { canvas ->
            val screenWidth: Int
            val screenHeight: Int
            val left: Int
            val top: Int

            if (canvas.height > canvas.width) {
                val ratio = outputBitmap.height.toFloat() / outputBitmap.width
                screenWidth = canvas.width
                left = 0
                screenHeight = (canvas.width * ratio).toInt()
                top = (canvas.height - screenHeight) / 2
            } else {
                val ratio = outputBitmap.width.toFloat() / outputBitmap.height
                screenHeight = canvas.height
                top = 0
                screenWidth = (canvas.height * ratio).toInt()
                left = (canvas.width - screenWidth) / 2
            }
            val right: Int = left + screenWidth
            val bottom: Int = top + screenHeight

            canvas.drawBitmap(
                outputBitmap, Rect(0, 0, outputBitmap.width, outputBitmap.height),
                Rect(left, top, right, bottom), null
            )
            surfaceView.holder.unlockCanvasAndPost(canvas)
        }
    }

    private fun stopImageReaderThread() {
        imageReaderThread?.quitSafely()
        try {
            imageReaderThread?.join()
            imageReaderThread = null
            imageReaderHandler = null
        } catch (e: InterruptedException) {
            Log.d(TAG, e.message.toString())
        }
    }

    interface CameraSourceListener {
        fun onFPSListener(fps: Int)

        fun onDetectedInfo(personScore: Float?, poseLabels: List<Pair<String, Float>>?)
//
//        fun xValue(data1: Float)
//
//        fun yValue(data2: Float)
//
//        fun nameValue(data3: BodyPart)
//
//        fun taisei(data4: Float)

        fun CountListener(count: String)

        fun CalorieListener(calorie: String)
    }
}
