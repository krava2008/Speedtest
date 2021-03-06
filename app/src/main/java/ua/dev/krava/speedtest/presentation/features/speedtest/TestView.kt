package ua.dev.krava.speedtest.presentation.features.speedtest

import com.arellomobile.mvp.MvpView

/**
 * Created by evheniikravchyna on 02.01.2018.
 */
interface TestView: MvpView {
    fun onTestStarted()
    fun showDefaultState()

    fun onCheckLocation()
    fun onLocation(city: String)

    fun onCheckServer()
    fun onServerReady(host: String)
    fun onServerError()

    fun onStartCheckingPing()
    fun onPingSuccess(timeMS: Int)
    fun onPingError()

    fun onStartDownload()
    fun onDownloadUpdate(progress: Float)
    fun onDownloadComplete()

    fun onStartUpload()
    fun onUploadUpdate(progress: Float)
    fun onUploadComplete()
}