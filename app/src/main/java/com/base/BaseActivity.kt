package com.base

import android.app.AlertDialog
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {

    fun showDialog(message:String,
                   posActionName:String?=null,
                   posAction:DialogInterface.OnClickListener?=null,
                   negAction:DialogInterface.OnClickListener?=null,
                   negActionName:String?=null){
        val dialog = AlertDialog.Builder(this,)
        dialog.setMessage(message)
        dialog.setPositiveButton(posActionName,posAction)
        dialog.setNegativeButton(negActionName,negAction)
        dialog.show()
    }

}