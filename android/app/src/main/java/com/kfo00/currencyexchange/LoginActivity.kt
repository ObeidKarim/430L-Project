package com.kfo00.currencyexchange

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.kfo00.currencyexchange.api.Authentication
import com.kfo00.currencyexchange.api.ExchangeService
import com.kfo00.currencyexchange.api.model.Token
import com.kfo00.currencyexchange.api.model.User
import retrofit2.Callback
import retrofit2.Call
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    private var usernameEditText: TextInputLayout? = null
    private var passwordEditText: TextInputLayout? = null
    private var submitButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        usernameEditText = findViewById(R.id.txtInptUsername)
        passwordEditText = findViewById(R.id.txtInptPassword)
        submitButton = findViewById(R.id.btnSubmit)
        submitButton?.setOnClickListener {view -> loginUser()}
    }

    private fun loginUser(){
        val user = User()
        user.username = usernameEditText?.editText?.text.toString()
        user.password = passwordEditText?.editText?.text.toString()

        ExchangeService.exchangeApi().authenticate(user).enqueue(object: Callback<Token> {
            override fun onFailure(call: Call<Token>, t: Throwable){
                Snackbar.make(
                    submitButton as View,
                    "Incorrect Credentials.",
                    Snackbar.LENGTH_LONG
                )
                    .show()
            }

            override fun onResponse(call: Call<Token>, response: Response<Token>) {
                Snackbar.make(
                    submitButton as View,
                    "Logged In.",
                    Snackbar.LENGTH_LONG
                )
                    .show()
                response.body()?.token?.let {
                    Authentication.saveToken(it) }
                onCompleted()
            }
        })
    }

    private fun onCompleted(){
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }

}