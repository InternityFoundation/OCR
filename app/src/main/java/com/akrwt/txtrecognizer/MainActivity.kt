package com.akrwt.txtrecognizer

import android.app.Activity
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.text.TextRecognizer
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.StringBuilder
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {

    private val cameraRequestCode = 200
    private val storageRequestCode = 400
    private val imagePickGalleryCode = 1000
    private val imagePickCameraCode = 2000

    private lateinit var cameraPermission: Array<String>
    private lateinit var storagePermission: Array<String>
    private lateinit var imageUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cameraPermission = arrayOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        storagePermission = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.addImage -> {
                showImageImportDialog()
            }

            R.id.settings -> {
                Toast.makeText(applicationContext, "Settings", Toast.LENGTH_LONG).show()
            }
        }


        return super.onOptionsItemSelected(item)
    }

    private fun showImageImportDialog() {

        val items = arrayOf("Camera", "Gallery")
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Select Image")
        dialog.setItems(items, DialogInterface.OnClickListener { d, which ->

            when (which) {
                0 -> {
                    if (!checkCameraPermission())
                        requestCameraPermission()
                    else
                        pickCamera()
                }
                1 -> {
                    if (!checkStoragePermission())
                        requestStoragePermission()
                    else
                        pickGallery()
                }
                else -> {
                }
            }
        })
        dialog.create().show()
    }

    private fun pickGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, imagePickGalleryCode)
    }

    private fun pickCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New pic")
        values.put(MediaStore.Images.Media.DESCRIPTION, "Image To Text")
        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)!!

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(intent, imagePickCameraCode)
    }

    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermission, storageRequestCode)

    }

    private fun checkStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermission, cameraRequestCode)

    }

    private fun checkCameraPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        val result1 = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        return result && result1
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            cameraRequestCode -> {
                if (grantResults.isNotEmpty()) {
                    val cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    val writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    if (cameraAccepted && writeStorageAccepted)
                        pickCamera()
                    else
                        Toast.makeText(applicationContext, "Permission Denied", Toast.LENGTH_LONG)
                            .show()
                }
            }
            storageRequestCode -> {
                if (grantResults.isNotEmpty()) {
                    val writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    if (writeStorageAccepted)
                        pickGallery()
                    else
                        Toast.makeText(applicationContext, "Permission Denied", Toast.LENGTH_LONG)
                            .show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == imagePickGalleryCode) {
                CropImage.activity(data!!.data).setGuidelines(CropImageView.Guidelines.ON)
                    .start(this)
            }
            if (requestCode == imagePickCameraCode) {
                CropImage.activity(imageUri).setGuidelines(CropImageView.Guidelines.ON)
                    .start(this)
            }
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                val resultUri = result.uri
                imageView.setImageURI(resultUri)

                val bitmapDrawable = imageView.drawable as BitmapDrawable
                val bitmap = bitmapDrawable.bitmap
                val recognizer = TextRecognizer.Builder(applicationContext).build()
                if (!recognizer.isOperational) {
                    Toast.makeText(applicationContext, "ERROR", Toast.LENGTH_LONG).show()
                } else {
                    val frame = Frame.Builder().setBitmap(bitmap).build()
                    val items = recognizer.detect(frame)
                    val sb = StringBuilder()

                    for (i in 0 until items.size()) {
                        val myItem = items.valueAt(i)
                        sb.append(myItem.value)
                        sb.append("\n")
                    }

                    resultET.setText(sb.toString())
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                Toast.makeText(applicationContext,result.error.toString(),Toast.LENGTH_LONG).show()
            }
        }
    }
}
