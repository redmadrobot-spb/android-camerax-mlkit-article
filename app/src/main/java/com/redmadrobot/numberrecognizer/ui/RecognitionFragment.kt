package com.redmadrobot.numberrecognizer.ui

import android.Manifest
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalUseCaseGroup
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.TorchState
import androidx.camera.core.UseCaseGroup
import androidx.camera.lifecycle.ExperimentalUseCaseGroupLifecycle
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import com.redmadrobot.numberrecognizer.R
import com.redmadrobot.numberrecognizer.databinding.MainFragmentBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions
import timber.log.Timber

@RuntimePermissions
class RecognitionFragment : Fragment() {

    private lateinit var viewModel: RecognitionViewModel
    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private lateinit var camera: Camera
    private var flashStateLiveData: LiveData<Int>? = null
    private var imageCapture: ImageCapture? = null
    private lateinit var outputDirectory: File

    private val framesAnalyzer: ImageAnalysis.Analyzer by lazy {
        ImageAnalysis.Analyzer(viewModel::onFrameReceived)
    }

    private var _binding: MainFragmentBinding? = null
    private val binding
        get() = requireNotNull(_binding)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MainFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(RecognitionViewModel::class.java)
    }

    @ExperimentalUseCaseGroup
    @ExperimentalUseCaseGroupLifecycle
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        outputDirectory = getOutputDirectory(requireContext())
        binding.toolbar.title = getString(R.string.app_name)
        startCameraWithPermissionCheck()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    @NeedsPermission(Manifest.permission.CAMERA)
    @ExperimentalUseCaseGroup
    @ExperimentalUseCaseGroupLifecycle
    fun startCamera() {
        ProcessCameraProvider.getInstance(requireContext())
            .apply {
                addListener(
                    { bindCameraUseCases(get()) },
                    ContextCompat.getMainExecutor(requireContext())
                )
            }
    }

    @ExperimentalUseCaseGroup
    @ExperimentalUseCaseGroupLifecycle
    private fun bindCameraUseCases(cameraProvider: ProcessCameraProvider) {
        val screenAspectRatio = aspectRatio(binding.cameraPreview.width, binding.cameraPreview.height)

        val preview = Preview.Builder()
            .setTargetRotation(Surface.ROTATION_0)
            .setTargetAspectRatio(screenAspectRatio)
            .build()
            .also { it.setSurfaceProvider(binding.cameraPreview.surfaceProvider) }

        val imageAnalyzer = ImageAnalysis.Builder()
            .setTargetRotation(Surface.ROTATION_0)
            .setTargetAspectRatio(screenAspectRatio)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also { it.setAnalyzer(cameraExecutor, framesAnalyzer) }

        imageCapture = ImageCapture.Builder()
            .setTargetRotation(Surface.ROTATION_0)
            .setTargetAspectRatio(screenAspectRatio)
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .build()

        val useCaseGroup = UseCaseGroup.Builder().run {
            addUseCase(preview)
            addUseCase(imageAnalyzer)
            addUseCase(imageCapture!!)
            binding.cameraPreview.viewPort?.let { setViewPort(it) }
            build()
        }

        try {
            cameraProvider.unbindAll()
            camera = cameraProvider.bindToLifecycle(
                viewLifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                useCaseGroup
            )
            setupCameraMenuIcons()
        } catch (t: Throwable) {
            Timber.e(t, "Camera use cases binding failed")
        }
    }

    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    private fun setupCameraMenuIcons() {
        if (!camera.cameraInfo.hasFlashUnit()) {
            Timber.tag("RTRT").d("does not have flash")
            return
        }

        with(binding.toolbar) {
            inflateMenu(R.menu.menu_recognition)

            menu.findItem(R.id.menuFlash)?.setOnMenuItemClickListener {
                val isFlashOff = flashStateLiveData?.value == TorchState.OFF
                camera.cameraControl.enableTorch(isFlashOff)
                true
            }

            menu.findItem(R.id.menuCapture)?.setOnMenuItemClickListener {
                imageCapture?.let { imageCapture ->
                    val photoFile = createFile(outputDirectory)
                    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                    imageCapture.takePicture(
                        outputOptions, cameraExecutor, object : ImageCapture.OnImageSavedCallback {
                            override fun onError(exc: ImageCaptureException) {
                                Timber.e(exc, "Photo capture failed: ${exc.message}")
                            }

                            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                val savedUri = Uri.fromFile(photoFile)
                                Timber.d("Photo capture succeeded: $savedUri")

//                                val source = ImageDecoder.createSource(requireContext().contentResolver, savedUri)
//                                val bitmap = ImageDecoder.decodeBitmap(source)
                                val bitmap = BitmapFactory.decodeFile(photoFile.path)
                                viewModel.onFrameCaptured(bitmap, 90)
                            }
                        })
                }
                true
            }
        }

        flashStateLiveData = camera.cameraInfo.torchState
        flashStateLiveData?.observe(viewLifecycleOwner) { torchState ->
            val newFlashIconRes = when (torchState) {
                TorchState.OFF -> R.drawable.ic_flash_off
                else -> R.drawable.ic_flash_on
            }

            binding.toolbar.menu
                .findItem(R.id.menuFlash)
                .setIcon(newFlashIconRes)
        }
    }

    private fun getOutputDirectory(context: Context): File {
        val appContext = context.applicationContext
        val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
            File(it, appContext.resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else appContext.filesDir
    }

    companion object {
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0

        private fun createFile(baseFolder: File): File {
            val fileName = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
                .format(System.currentTimeMillis()) + ".jpg"
            return File(baseFolder, fileName)
        }

        fun newInstance() = RecognitionFragment()
    }
}