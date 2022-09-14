package com.example.rickmorty.utils

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.example.rickmorty.R
import com.google.android.material.snackbar.Snackbar

object Resources {
    private lateinit var dialog: Dialog

    fun initLoadingDialog(context: Context){
        dialog = Dialog(context)
    }

    fun loadingDialog(){
        dialog.setContentView(R.layout.loading_dialog)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(false)
        dialog.show()
    }

    fun hideLoadingDialog(){
        dialog.dismiss()
    }

    fun Activity.showSnackBar(title: String, color: Int? = null){
        val snackBar = Snackbar.make(findViewById(android.R.id.content), title, Snackbar.LENGTH_SHORT)
        snackBar.show()
    }

    fun Fragment.showSnackBar(title: String, color: Int? = null){
        val snackBar = Snackbar.make(requireActivity().findViewById(android.R.id.content), title, Snackbar.LENGTH_SHORT)
        snackBar.show()
    }

    fun toolBar(toolbar: Toolbar, title: String, subTitle: String? = null, activity: AppCompatActivity){
        activity.setSupportActionBar(toolbar)
        val actionBar = activity.supportActionBar
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_new_24)
            actionBar.title = title
        }

        toolbar.setNavigationOnClickListener {
            activity.onBackPressed()
        }
    }

    fun normalToolBar(toolbar: Toolbar, title: String, subTitle: String? = null, activity: AppCompatActivity){
        activity.setSupportActionBar(toolbar)
        val actionBar = activity.supportActionBar
        if(actionBar != null){
            actionBar.title = title
        }
    }

    fun Activity.hideKeyBoard(){
        val view = this.currentFocus
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    fun hideKeyBoard(activity: AppCompatActivity){
        val view = activity.currentFocus
        val inputMethodManager = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view?.windowToken, 0)
    }





}