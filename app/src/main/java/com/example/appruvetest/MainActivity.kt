package com.example.appruvetest

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.util.*

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private val PERMISSION_CODE = 1000
    private val IMAGE_CAPTURE_CODE = 1001
    var imageUri: Uri? = null
    var bitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        takePicture.setOnClickListener {
            //if the system OS is marshmallow or above, we need runtime permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED
                    || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
                ) {

                    //permission was not enabled
                    val permission = arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                    //pop up to show permission
                    requestPermissions(permission, PERMISSION_CODE)
                } else {
                    //permission already granted
                    openCamera()
                }
            } else {
                //system OS is less than marshmallow
                openCamera()
            }
        }

    }

    private fun openCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        //creating a camera intent
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission pop up was granted
                    openCamera()
                } else {
                    //permission pop up was denied
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, dataa: Intent?) {
        super.onActivityResult(requestCode, resultCode, dataa)
        //called when image was captured from the camera intent
        if (requestCode==IMAGE_CAPTURE_CODE && resultCode == Activity.RESULT_OK) {
            //set image captured to image view
            imageView.setImageURI(imageUri)

            //convert image URI to bitmap
            bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)

            //save the image
            val contextWrapper = ContextWrapper(applicationContext)
            val directory = contextWrapper.getDir("imageDir", Context.MODE_PRIVATE)

            //create imageDir
            val myPath = File(directory, "${UUID.randomUUID()}.jpg")

            try {
                val fileOutputStream = FileOutputStream(myPath)
                //use the compress method on the BitMap object to write the image to the output stream
                bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)

                //flush the stream
                fileOutputStream.flush()
                //close the stream
                fileOutputStream.close()

            } catch (e: Exception) {
                e.printStackTrace()
            }
            Toast.makeText(this, "image Saved", Toast.LENGTH_SHORT).show()
            dirTextView.text = "Image Saved to: ${directory.toString()}"
        }
    }
}
