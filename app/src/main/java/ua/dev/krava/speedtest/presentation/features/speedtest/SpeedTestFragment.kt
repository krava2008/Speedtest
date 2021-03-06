package ua.dev.krava.speedtest.presentation.features.speedtest

import android.animation.Animator
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.arellomobile.mvp.MvpAppCompatFragment
import com.arellomobile.mvp.presenter.InjectPresenter
import kotlinx.android.synthetic.main.fragment_test.*
import org.krava.speedometer.SpeedometerView
import ua.dev.krava.speedtest.R
import ua.dev.krava.speedtest.presentation.features.privacy.PrivacyPolicyActivity

/**
 * Created by evheniikravchyna on 01.01.2018.
 */
class SpeedTestFragment: MvpAppCompatFragment(), TestView {
    @InjectPresenter
    lateinit var presenter: SpeedTestPresenter
    private lateinit var btnStartTest: View
    private lateinit var btnRepeatTest: View
    private lateinit var txtPrivacyPolicy: TextView
    private lateinit var progressView: SpeedometerView
    private var flickerAnimation: FlickerAnimation? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            presenter.autoStartTest = it.getBoolean("auto_start", false)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_test, container, false)
        progressView = view.findViewById(R.id.progressView)
        btnStartTest = view.findViewById(R.id.btnStartTest)
        btnRepeatTest = view.findViewById(R.id.btnRepeatTest)
        btnStartTest.setOnClickListener {
            presenter.startTest()
        }
        txtPrivacyPolicy = view.findViewById(R.id.privacy_policy)
        txtPrivacyPolicy.setOnClickListener {
            startActivity(Intent(context, PrivacyPolicyActivity::class.java))
        }
        btnRepeatTest.setOnClickListener {
            btnRepeatTest.visibility = View.GONE
            speedContainer.visibility = View.GONE
            uploadContainer.visibility = View.GONE
            uploadValue.text = "0.0"
            downloadValue.text = "0.0"
            pingValue.text = "--"
            presenter.startTest()
        }
        return view
    }

    override fun showDefaultState() {
        btnStartTest.visibility = View.VISIBLE
        txtPrivacyPolicy.visibility = View.VISIBLE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        presenter.onViewCreated()
    }

    override fun onTestStarted() {
        btnStartTest.visibility = View.GONE
        txtPrivacyPolicy.visibility = View.GONE
        testResultContainer.visibility = View.VISIBLE
    }

    override fun onPingSuccess(timeMS: Int) {
        pingProgressIndicator.hide()
        pingValue.text = "$timeMS ms"
        pingValue.visibility = View.VISIBLE
        speedContainer.visibility =  View.VISIBLE
    }

    override fun onPingError() {
        pingProgressIndicator.hide()
        pingProgressIndicator.visibility = View.GONE
        pingValue.text = "Error"
        btnRepeatTest.visibility = View.VISIBLE
        txtPrivacyPolicy.visibility = View.VISIBLE
    }

    override fun onStartCheckingPing() {
        pingValue.visibility = View.GONE
        pingProgressIndicator.show()
    }

    override fun onStartDownload() {
        flickerAnimation?.cancel()
        flickerAnimation = FlickerAnimation(downloadSpeedTitle)
        downloadContainer.visibility = View.VISIBLE
        flickerAnimation?.start()
    }

    override fun onDownloadComplete() {
        flickerAnimation?.cancel()
        downloadValue.animate().scaleX(1.5f).scaleY(1.5f).setListener(object: Animator.AnimatorListener {
            override fun onAnimationEnd(p0: Animator?) {
                downloadValue?.apply { animate().scaleX(1.0f).scaleY(1.0f).setListener(null).start() }
            }
            override fun onAnimationRepeat(p0: Animator?) { }
            override fun onAnimationStart(p0: Animator?) { }
            override fun onAnimationCancel(p0: Animator?) { }
        }).start()
        progressView.animateToZero()
    }

    override fun onStartUpload() {
        flickerAnimation = FlickerAnimation(uploadSpeedTitle)
        uploadContainer.visibility = View.VISIBLE
        flickerAnimation?.start()
    }

    override fun onUploadUpdate(progress: Float) {
        progressView.setProgress(progress)
        uploadValue.text = "$progress"
    }

    override fun onUploadComplete() {
        flickerAnimation?.cancel()
        uploadValue.animate().scaleX(1.5f).scaleY(1.5f).setListener(object: Animator.AnimatorListener {
            override fun onAnimationEnd(p0: Animator?) {
                uploadValue?.apply { animate().scaleX(1.0f).scaleY(1.0f).setListener(null).start() }
            }
            override fun onAnimationRepeat(p0: Animator?) { }
            override fun onAnimationStart(p0: Animator?) { }
            override fun onAnimationCancel(p0: Animator?) { }
        }).start()
        progressView.animateToZero()
        btnRepeatTest.visibility = View.VISIBLE
        txtPrivacyPolicy.visibility = View.VISIBLE
    }


    override fun onDownloadUpdate(progress: Float) {
        progressView.setProgress(progress)
        downloadValue.text = "$progress"
    }

    override fun onCheckLocation() {
        locationValue.visibility = View.GONE
        hostValue.visibility = View.GONE
        hostProgressIndicator.show()
        locationProgressIndicator.show()
    }

    override fun onLocation(city: String) {
        locationProgressIndicator.hide()
        locationValue.text = city
        locationValue.visibility = View.VISIBLE
    }

    override fun onCheckServer() {

    }

    override fun onServerError() {
        onServerReady("Error")
        onLocation("Error")
        btnRepeatTest.visibility = View.VISIBLE
        txtPrivacyPolicy.visibility = View.VISIBLE
    }

    override fun onServerReady(host: String) {
        hostProgressIndicator.hide()
        hostValue.text = host
        hostValue.visibility = View.VISIBLE
    }

    companion object {
        val TAG = "speed_test"

        fun instance(isAutoStart: Boolean = false): SpeedTestFragment {
            val instance = SpeedTestFragment()
            instance.arguments = Bundle()
            instance.arguments?.putBoolean("auto_start", isAutoStart)

            return instance
        }
    }
}
