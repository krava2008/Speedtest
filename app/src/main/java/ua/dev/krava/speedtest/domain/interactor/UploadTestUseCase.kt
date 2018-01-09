package ua.dev.krava.speedtest.domain.interactor

import android.util.Log
import fr.bmartel.speedtest.SpeedTestReport
import fr.bmartel.speedtest.SpeedTestSocket
import fr.bmartel.speedtest.inter.IRepeatListener
import fr.bmartel.speedtest.inter.ISpeedTestListener
import fr.bmartel.speedtest.model.SpeedTestError
import fr.bmartel.speedtest.model.UploadStorageType
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import ua.dev.krava.speedtest.domain.utils.MathUtils


/**
 * Created by evheniikravchyna on 05.01.2018.
 */
class UploadTestUseCase(private val url: String) {

    fun execute(): Flowable<Float> {
        val socket = SpeedTestSocket()
        socket.uploadStorageType = UploadStorageType.FILE_STORAGE
        return Flowable.create({
            socket.addSpeedTestListener(object : ISpeedTestListener {
                override fun onError(speedTestError: SpeedTestError?, errorMessage: String?) {
                    Log.e("Main", "upload error: $errorMessage")
                    socket.closeSocket()
                    socket.clearListeners()

                    if (!it.isCancelled) it.onError(Throwable(errorMessage))
                }
                override fun onCompletion(report: SpeedTestReport?) { }
                override fun onProgress(percent: Float, report: SpeedTestReport) { }
            })
            socket.startUploadRepeat (url, 13000, 240, 900000, object: IRepeatListener {
                override fun onCompletion(report: SpeedTestReport?) {
                    socket.closeSocket()
                    socket.clearListeners()

                    if (!it.isCancelled) it.onComplete()
                }

                override fun onReport(report: SpeedTestReport) {
                    if (!it.isCancelled) {
                        val speed = report.transferRateBit.toDouble() / 1048576
                        it.onNext(MathUtils.round(speed.toFloat(), 2))
                    } else {
                        socket.closeSocket()
                        socket.clearListeners()
                    }
                }
            })
        }, BackpressureStrategy.MISSING)
    }
}