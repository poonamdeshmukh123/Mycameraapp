package com.example.mycameraapp

import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.OrientationEventListener
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.core.ImageCapture.OutputFileOptions
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mycameraapp.databinding.ActivityMainBinding
import com.example.mycameraapp.util.CameraPermission
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    private val binding:ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var camera: Camera
    private lateinit var cameraSelector: CameraSelector
    lateinit var cameraPermission: CameraPermission
    private lateinit var imageCapture: ImageCapture
    private val multiplePermissionId = 14
    private var aspectRatio = AspectRatio.RATIO_16_9
    private var orientationEventListener: OrientationEventListener? = null
    private val multiplePermissionNameList = if (Build.VERSION.SDK_INT >= 33) {
        arrayListOf(
            android.Manifest.permission.CAMERA,

        )
    } else {
        arrayListOf(
            android.Manifest.permission.CAMERA,

            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Hide the status bar and navigation bar
        // Hide the status bar and navigation bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(binding.root)

        cameraPermission= CameraPermission()
        if (checkMultiplePermission()) {

            startCamera()
        }
        binding.btncapimg.setOnClickListener {
            takePhoto()

        }
        binding.btnflashimg.setOnClickListener {
setFlashValue(camera)
        }
        binding.btnrotateimg.setOnClickListener {
            lensFacing=if(lensFacing==CameraSelector.LENS_FACING_BACK)
            {
                CameraSelector.LENS_FACING_FRONT
            }
            else
            {
                CameraSelector.LENS_FACING_BACK
            }
            bindCameraUseCases()
        }
        //for back camera remove the mirror effect when capture the image
        binding.txtratio.setOnClickListener {
            if(aspectRatio==AspectRatio.RATIO_16_9)
            {
aspectRatio=AspectRatio.RATIO_4_3
                setAspectratio("H,4:3")
                binding.txtratio.text="4:3"

            }
            else
            {
aspectRatio=AspectRatio.RATIO_16_9
                setAspectratio("H,0:0")
                binding.txtratio.text="16:9"
            }
bindCameraUseCases()
        }
    }


    private fun setFlashValue(camera: Camera)
    {

        if(camera.cameraInfo.hasFlashUnit())
        {

            if(camera.cameraInfo.torchState.value==0)
            {

                camera.cameraControl.enableTorch(true)
                binding.btnflashimg.setImageResource(R.drawable.flash_off)
            } else {
                camera.cameraControl.enableTorch(false)
                binding.btnflashimg.setImageResource(R.drawable.flash_on)
            }
        }
        else
        {
            Toast.makeText(
                this,
                "Flash is Not Available",
                Toast.LENGTH_LONG
            ).show()
            binding.btnflashimg.isEnabled = false
        }
    }

    fun startCamera()
{
    val cameraproviderFuture=ProcessCameraProvider.getInstance(this)
    cameraproviderFuture.addListener({
        cameraProvider=cameraproviderFuture.get()
        bindCameraUseCases()

    },ContextCompat.getMainExecutor(this))
}

    private fun takePhoto()
    {
        val imgfolder= File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"Images")
        if(!imgfolder.exists())
        {
            imgfolder.mkdir()
        }
        val filename=SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            .format(System.currentTimeMillis())+".jpg"
        val contentvalues=ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME,filename)
            put(MediaStore.Images.Media.MIME_TYPE,"image/jpeg")
            if(Build.VERSION.SDK_INT>Build.VERSION_CODES.P)
            {
                put(MediaStore.Images.Media.RELATIVE_PATH,"Pictures/Images")

            }
        }
        //avoid mirror image by front camera
        val metadata=ImageCapture.Metadata().apply {
            isReversedHorizontal=(lensFacing==CameraSelector.LENS_FACING_FRONT)
        }
        val outputoption=if(Build.VERSION.SDK_INT>Build.VERSION_CODES.P)
        {
OutputFileOptions.Builder(contentResolver,MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentvalues).setMetadata(metadata).build()


        }
        else
        {
val imagefile=File(imgfolder,filename)
            OutputFileOptions.Builder(imagefile)
                .setMetadata(metadata).build()
        }
        imageCapture.takePicture(outputoption,ContextCompat.getMainExecutor(this),object:ImageCapture.OnImageSavedCallback
        {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
           val message="Photo captured successfully:${outputFileResults.savedUri}"

                
                Toast.makeText(
                    this@MainActivity,
                    message,
                    Toast.LENGTH_LONG
                ).show()
Intent(this@MainActivity,ShowActivity::class.java).also {
    it.putExtra("uri",outputFileResults.savedUri.toString())
    startActivity(it)
}



            }

            override fun onError(exception: ImageCaptureException) {
                Toast.makeText(
                    this@MainActivity,
                    exception.message.toString(),
                    Toast.LENGTH_LONG
                ).show()
            }

        })
    }
        private fun bindCameraUseCases() {
        val rotation=binding.previewView.display.rotation
        val resolutionselector=ResolutionSelector.Builder()
            .setAspectRatioStrategy(
                AspectRatioStrategy(aspectRatio,AspectRatioStrategy.FALLBACK_RULE_AUTO)
            ).build()

        val preview= Preview.Builder()
            .setResolutionSelector(resolutionselector)
            .setTargetRotation(rotation)
            .build()
            .also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)

            }
        imageCapture=ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .setResolutionSelector(resolutionselector)
            .setTargetRotation(rotation)
            .build()
        cameraSelector=CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()
        try {
            cameraProvider.unbindAll()
            camera=cameraProvider.bindToLifecycle(this,cameraSelector,preview,imageCapture)
        }
        catch (e:Exception)
        {

        }

    }

    private fun doOperation() {
        Toast.makeText(
            this,
            "All Permission Granted Successfully!",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun checkMultiplePermission(): Boolean {
        val listPermissionNeeded = arrayListOf<String>()
        for (permission in multiplePermissionNameList) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                listPermissionNeeded.add(permission)
            }
        }
        if (listPermissionNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                listPermissionNeeded.toTypedArray(),
                multiplePermissionId
            )
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == multiplePermissionId) {
            if (grantResults.isNotEmpty()) {
                var isGrant = true
                for (element in grantResults) {
                    if (element == PackageManager.PERMISSION_DENIED) {
                        isGrant = false
                    }
                }
                if (isGrant) {
                    // here all permission granted successfully

                    startCamera()
                } else {
                    var someDenied = false
                    for (permission in permissions) {
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                permission
                            )
                        ) {
                            if (ActivityCompat.checkSelfPermission(
                                    this,
                                    permission
                                ) == PackageManager.PERMISSION_DENIED
                            ) {
                                someDenied = true
                            }
                        }
                    }
                    if (someDenied) {
                        // here app Setting open because all permission is not granted
                        // and permanent denied
                        cameraPermission.appSettingOpen(this)
                    } else {
                        // here warning permission show
                        cameraPermission.warningPermissionDialog(this) { _: DialogInterface, which: Int ->
                            when (which) {
                                DialogInterface.BUTTON_POSITIVE ->
                                    checkMultiplePermission()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setAspectratio(ratio: String) {
binding.previewView.layoutParams=binding.previewView.layoutParams.apply {
    if(this is ConstraintLayout.LayoutParams)
    {
        dimensionRatio=ratio
    }
}
    }

}