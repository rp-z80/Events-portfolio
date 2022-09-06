package com.example.meeting

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_explore.*

class ExploreFragment : Fragment(), SearchView.OnQueryTextListener,
    android.widget.SearchView.OnQueryTextListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_explore, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        searchViewExploreFragment?.isSubmitButtonEnabled = true
        searchViewExploreFragment?.setOnQueryTextListener(this)

        button_tag_sport.setOnClickListener() {
            val intent = Intent(requireContext(), SearchActivity::class.java)
            intent.putExtra("QUERY", "Sport")
            intent.putExtra("FLAG", true)
            startActivity(intent)
        }

        button_tag_comics.setOnClickListener() {
            val intent = Intent(requireContext(), SearchActivity::class.java)
            intent.putExtra("QUERY", "Comics")
            intent.putExtra("FLAG", true)
            startActivity(intent)
        }

        button_tag_food.setOnClickListener() {
            val intent = Intent(requireContext(), SearchActivity::class.java)
            intent.putExtra("QUERY", "Food")
            intent.putExtra("FLAG", true)
            startActivity(intent)
        }

        button_tag_games.setOnClickListener() {
            val intent = Intent(requireContext(), SearchActivity::class.java)
            intent.putExtra("QUERY", "Games")
            intent.putExtra("FLAG", true)
            startActivity(intent)
        }

        button_tag_business.setOnClickListener() {
            val intent = Intent(requireContext(), SearchActivity::class.java)
            intent.putExtra("QUERY", "Business")
            intent.putExtra("FLAG", true)
            startActivity(intent)
        }

        button_tag_party.setOnClickListener() {
            val intent = Intent(requireContext(), SearchActivity::class.java)
            intent.putExtra("QUERY", "Party")
            intent.putExtra("FLAG", true)
            startActivity(intent)
        }


    }


   override fun onQueryTextSubmit(query: String?): Boolean {
        if (query != null) {
            
            Log.d("Explore Fragment", "Query is: $query")
            val intent = Intent(requireContext(), SearchActivity::class.java)
            intent.putExtra("QUERY", query)
            intent.putExtra("FLAG", false) //false = search through event name
            this.startActivity(intent)
        }
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        return true
    }
    }