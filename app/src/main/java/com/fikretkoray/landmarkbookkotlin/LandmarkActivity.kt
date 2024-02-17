package com.fikretkoray.landmarkbookkotlin

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.fikretkoray.landmarkbookkotlin.databinding.ActivityLandmarkBinding
import com.google.android.material.snackbar.Snackbar
import java.io.ByteArrayOutputStream

class LandmarkActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLandmarkBinding
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    var selectedBitmap: Bitmap? = null
    private lateinit var database : SQLiteDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLandmarkBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        database = this.openOrCreateDatabase("LandmarkBook", MODE_PRIVATE,null)

        registerLauncher()

        val intent = intent
        val info = intent.getStringExtra("info")
        if (info.equals("new")) {
            binding.landmarkNameText.setText("")
            binding.countryNameText.setText("")
            binding.saveButton.visibility = View.VISIBLE
            binding.selectImage.setImageResource(R.drawable.selectimage)

        }else {
            binding.saveButton.visibility = View.INVISIBLE
            val selectedId = intent.getIntExtra("id",1)

            val cursor = database.rawQuery("SELECT * FROM landmarkBook WHERE id = ?", arrayOf(selectedId.toString()))

            val landmarkNameIx = cursor.getColumnIndex("landmarkname")
            val countryNameIx = cursor.getColumnIndex("countryname")
            val imageIx = cursor.getColumnIndex("image")

            while (cursor.moveToNext()){
                binding.landmarkNameText.setText(cursor.getString(landmarkNameIx))
                binding.countryNameText.setText(cursor.getString(countryNameIx))

                val byteArray = cursor.getBlob(imageIx)
                val bitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)
                binding.selectImage.setImageBitmap(bitmap)
            }

            cursor.close()

        }
    }

    fun saveButtonClicked(view: View) {
        val landmarkName = binding.landmarkNameText.text.toString()
        val countryName = binding.countryNameText.text.toString()

        if (selectedBitmap != null){
            val  smallBitmap = makeSmallerBitmap(selectedBitmap!!,300)

            // görseli veriye çevirme
            val outputStream = ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            val byteArray = outputStream.toByteArray()

            try {
                // val database = this.openOrCreateDatabase("LandmarkBook", MODE_PRIVATE,null)
                database.execSQL("CREATE TABLE IF NOT EXISTS landmarkBook (id INTEGER PRIMARY KEY, landmarkname VARCHAR, countryname VARCHAR, image BLOB)")

                val sqlString = "INSERT INTO landmarkBook (landmarkname, countryname, image) VALUES(?,?,?)"
                val statement = database.compileStatement(sqlString)
                statement.bindString(1,landmarkName)
                statement.bindString(2,countryName)
                statement.bindBlob(3,byteArray)
                statement.execute()

            }catch (e:Exception){
                e.printStackTrace()
            }

            val intent = Intent(this@LandmarkActivity,MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)

        }
    }

    private fun makeSmallerBitmap(image: Bitmap, maximumSize: Int) : Bitmap{
        var width = image.width
        var height = image.height

        val bitmapRatio : Double = width.toDouble() / height.toDouble()

        if (bitmapRatio > 1){
            //landscape
            width = maximumSize
            val scaledHeight = width / bitmapRatio
            height = scaledHeight.toInt()
        }else{
            //portrait
            height = maximumSize
            val scaledWidth = height * bitmapRatio
            width = scaledWidth.toInt()
        }

        return Bitmap.createScaledBitmap(image,width,height,true)
    }
    fun selectImage(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            //Android 33 ve sonrası -> READ_MEDIA_IMAGES
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_IMAGES)) {
                    //rationale
                    Snackbar.make(view, "Permission needed for gallery", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Give Permission", View.OnClickListener {
                            //request permission
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                        }).show()
                } else {
                    //request permission
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }


            } else {
                val intentToGallery =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }

        } else {
            //Android 32 ve öncesi -> READ-EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                ) {
                    //rationale
                    Snackbar.make(view, "Permission needed for gallery", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Give Permission", View.OnClickListener {
                            //request permission
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }).show()
                } else {
                    //request permission
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }

            } else {
                val intentToGallery =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
        }
    }
    private fun registerLauncher() {
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val intentFromResult = result.data
                    if (intentFromResult != null) {
                        val imageData = intentFromResult.data
                        //binding.imageView.setImageURI(imageData)
                        if (imageData != null) {
                            try {
                                if (Build.VERSION.SDK_INT >= 28) {
                                    val source = ImageDecoder.createSource(this@LandmarkActivity.contentResolver, imageData)
                                    selectedBitmap = ImageDecoder.decodeBitmap(source)
                                    binding.selectImage.setImageBitmap(selectedBitmap)
                                } else {
                                    selectedBitmap = MediaStore.Images.Media.getBitmap(this@LandmarkActivity.contentResolver, imageData)
                                    binding.selectImage.setImageBitmap(selectedBitmap)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){ result ->
            if (result){
                //permission granted
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }else{
                //permission denied
                Toast.makeText(this@LandmarkActivity,"Permission needed!", Toast.LENGTH_LONG).show()
            }
        }
    }
}