package com.mp.yourcalendar

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_log_in.*

class LogInFragment : Fragment() {
    

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val logInView = inflater.inflate(R.layout.fragment_log_in, container, false)

        // Get/listen to toRegistrationButton, set action
        val toRegisterButton = logInView.findViewById<View>(R.id.toRegisterButton) as Button
        toRegisterButton.setOnClickListener {
            changeFragment(RegisterFragment()) // Go to registration
        }

        // Get/listen to forgotPasswordButton, set action
        val forgotPasswordButton = logInView.findViewById<View>(R.id.forgotPasswordButton) as Button
        forgotPasswordButton.setOnClickListener {
            changeFragment(ResetPasswordFragment()) // Go to resetPassword
        }

        //get logInButton, emailInput and passwordInput
        val logInButton = logInView.findViewById<View>(R.id.logInButton) as Button
        val emailInput = logInView.findViewById<View>(R.id.logInEmailEditText) as EditText
        val passwordInput = logInView.findViewById<View>(R.id.logInPasswordEditText) as EditText
        //listen to logInButton
        logInButton.setOnClickListener {
            // Check if inputs are ok
            if(emailInput.text.trim().toString().isNotEmpty() && passwordInput.text.trim().toString().isNotEmpty()){
                // Try to log user in
                logInUser(emailInput.text.trim().toString(), passwordInput.text.trim().toString())
            } else {
                //TODO: proper error
                Log.d("LOGIN", "No email or password given")
            }
        }

        return logInView
    }

    /*override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Go to registration
        toRegisterButton.setOnClickListener {
            changeFragment(RegisterFragment())
        }

        // Go to reset password
        forgotPasswordButton.setOnClickListener {
            changeFragment(ResetPasswordFragment())
        }

        // Login Button
        logInButton.setOnClickListener {
            // Check some inputs are given
            if (logInEmailEditText.text.trim().isNotEmpty() && logInPasswordEditText.text.trim().isNotEmpty()) {
                logInUser(logInEmailEditText.text.trim().toString(), logInPasswordEditText.text.trim().toString())
            } else {

            }
        }
    }*/

    // User Login with email and psw
    fun logInUser(email: String, password: String){
        Firebase.auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    // Login successful -> start MainActivity
                    var intent = Intent(requireActivity(), MainActivity::class.java)
                    startActivity(intent)
                } else {
                    // Login failed show error
                    // TODO: more handling errors?
                    try {
                        throw task.exception!!
                    } catch (e: FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(requireActivity(), "Invalid password!", Toast.LENGTH_LONG).show()
                    } catch (e: FirebaseAuthInvalidUserException) {
                        Toast.makeText(requireActivity(), "Account not found!", Toast.LENGTH_LONG).show()
                    }
                }
            }
    }

    // Handle fragment change to registration, forgot psw, etc....
    fun changeFragment(fragment: Fragment){
        // Setup
        val fragmentManager = activity!!.supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        // Transaction
        val transaction = fragmentTransaction
        transaction.replace(R.id.authFragmentFrame, fragment) // set new fragment to authFragmentFrame
        transaction.disallowAddToBackStack() //TODO: see if we should allow backstack?
        transaction.commit()
    }

}