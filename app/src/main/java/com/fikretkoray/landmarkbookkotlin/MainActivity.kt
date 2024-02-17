package com.fikretkoray.landmarkbookkotlin

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.fikretkoray.landmarkbookkotlin.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var landmarkList: ArrayList<Landmark>
    private lateinit var landmarkAdapter: LandmarkAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        landmarkList = ArrayList<Landmark>()
        landmarkAdapter = LandmarkAdapter(landmarkList)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = landmarkAdapter

        try {
            val database = this.openOrCreateDatabase("LandmarkBook", MODE_PRIVATE,null)

            val cursor = database.rawQuery("SELECT * FROM landmarkBook",null)
            val landmarkNameIx = cursor.getColumnIndex("landmarkname")
            val idIx = cursor.getColumnIndex("id")

            while (cursor.moveToNext()){
                val name = cursor.getString(landmarkNameIx)
                val id = cursor.getInt(idIx)
                val landmark = Landmark(name, id)
                landmarkList.add(landmark)
            }

            landmarkAdapter.notifyDataSetChanged() //kendini yenile, yeni veri geldi

            cursor.close()

        }catch (e:Exception){
            e.printStackTrace()
        }


        //RecyclerView
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = LandmarkAdapter(landmarkList)
        binding.recyclerView.adapter = adapter

        /* Listview varken kullanıyorduk
        //Adapter : Layout & Data

        //Mapping

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, landmarkList.map { landmark -> landmark.name })

        binding.listView.adapter = adapter

        binding.listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                val intent = Intent(MainActivity@ this, DetailsActivity::class.java)
                intent.putExtra("landmark",landmarkList.get(position))
                MySingleton.selectedLandmark = landmarkList[position]
                startActivity(intent)
            }

         */
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.landmark_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if(item.itemId == R.id.landmark_item){
            val intent = Intent(this@MainActivity,LandmarkActivity::class.java)
            intent.putExtra("info","new")
            startActivity(intent)
        }

        return super.onOptionsItemSelected(item)
    }

}