package com.huawei.huaweiscankit


import android.Manifest
import android.app.Activity
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.huawei.hms.hmsscankit.ScanUtil
import com.huawei.hms.hmsscankit.WriterException
import com.huawei.hms.ml.scan.HmsBuildBitmapOption
import com.huawei.hms.ml.scan.HmsScan
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


class MainActivity : AppCompatActivity() {

    companion object {
        const val BITMAP = 0x22
        const val REQUEST_CODE_PHOTO = 0x33
        const val BitmapSave = 2
        const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun newViewBtnClick(view: View?) {
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            BITMAP)
    }

    fun saveImageBitmapBtnClick(view: View) {
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            BitmapSave)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            return
        }
        if (requestCode == MainActivity.BITMAP) {
            // Call the system album.
            val pickIntent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
            this@MainActivity.startActivityForResult(pickIntent, REQUEST_CODE_PHOTO)
        }

        if (requestCode == MainActivity.BitmapSave) {
            // Insert the QR to the system album.
            Log.d(TAG, "External storage")
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0])
                val content = "TEST QR"
                val type = HmsScan.QRCODE_SCAN_TYPE
                val width = 400
                val height = 400
                val options = HmsBuildBitmapOption.Creator().setBitmapBackgroundColor(Color.RED).setBitmapColor(
                    Color.BLUE).setBitmapMargin(3).create()
                try {
                    // If the HmsBuildBitmapOption object is not constructed, set options to null.
                    val qrBitmap = ScanUtil.buildBitmap(content, type, width, height, options)
                    saveToGallery(applicationContext,qrBitmap,"HuaweiScanKitAlbum")
                    Toast.makeText(applicationContext,"QR Code is created",Toast.LENGTH_LONG).show()
                } catch (e: WriterException) {
                    Log.w("buildBitmap", e)
                }

            } else {
                Log.d(TAG, "There is a problem")
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //receive result after your activity finished scanning
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK || data == null) {
            return
        }
        if (requestCode == REQUEST_CODE_PHOTO) {
            // Obtain the image path.
            val path = getImagePath(this@MainActivity, data)
            if (TextUtils.isEmpty(path)) {
                return
            }
            // Obtain the bitmap from the image path.
            val bitmap = ScanUtil.compressBitmap(this@MainActivity, path)
            // Call the decodeWithBitmap method to pass the bitmap.
            val result = ScanUtil.decodeWithBitmap(this@MainActivity, bitmap, HmsScanAnalyzerOptions.Creator().setHmsScanTypes(HmsScan.QRCODE_SCAN_TYPE, HmsScan.DATAMATRIX_SCAN_TYPE).setPhotoMode(true).create())
            // Obtain the scanning result.
            if (result != null && result.isNotEmpty()) {
                if (!TextUtils.isEmpty(result[0].getOriginalValue())) {
                    Toast.makeText(this, result[0].getOriginalValue(), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun saveToGallery(context: Context, bitmap: Bitmap, albumName: String) {
        val filename = "${System.currentTimeMillis()}.png"
        val write: (OutputStream) -> Boolean = {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DCIM}/$albumName")
            }

            context.contentResolver.let {
                it.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)?.let { uri ->
                    it.openOutputStream(uri)?.let(write)
                }
            }
        } else {
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + File.separator + albumName
            val file = File(imagesDir)
            if (!file.exists()) {
                file.mkdir()
            }
            val image = File(imagesDir, filename)
            write(FileOutputStream(image))
        }
    }

    private fun getImagePath(context: Context, data: Intent): String? {
        var imagePath: String? = null
        val uri = data.data
        val currentAPIVersion = Build.VERSION.SDK_INT
        if (currentAPIVersion > Build.VERSION_CODES.KITKAT) {
            imagePath = getImagePath(context, uri)
            }
        return imagePath
    }

    private fun getImagePath(context: Context, uri: Uri?): String? {
        var path: String? = null
        val cursor = context.contentResolver.query(uri!!, null, null, null, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
            }
            cursor.close()
        }
        return path
    }
}