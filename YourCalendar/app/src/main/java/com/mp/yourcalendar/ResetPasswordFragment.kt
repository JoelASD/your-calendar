package com.mp.yourcalendar

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ResetPasswordFragment : Fragment() {

    private lateinit var emailInput: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val forgotPasswordView = inflater.inflate(R.layout.fragment_reset_password, container, false)

        // Get/listen to toLogInButton, set action
        val toLogInButton = forgotPasswordView.findViewById<View>(R.id.exitResetPasswordButton) as Button
        toLogInButton.setOnClickListener {
            changeFragment(LogInFragment()) // Go to login
        }

        // Get emailEditText, resetButton
        val sendResetEmailButton = forgotPasswordView.findViewById<View>(R.id.resetPasswordButton) as Button
        emailInput = forgotPasswordView.findViewById<View>(R.id.resetEmailEditText) as EditText
        // Listen to button
        sendResetEmailButton.setOnClickListener {
            // Check that email is given
            if(emailInput.text.trim().toString().isNotEmpty()){
                // Try to send email
                resetPassword(emailInput.text.trim().toString())
            } else {
                // Email not given
                emailInput.setError("Must give email!")
                emailInput.requestFocus()
            }
        }

        return forgotPasswordView
    }

    // reset password
    fun resetPassword(email: String){
        Firebase.auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    // Email sent
                    Toast.makeText(activity!!, "Email sent!", Toast.LENGTH_LONG).show()
                } else {
                    //TODO: proper error handling
                    //Toast.makeText(activity!!, "No account found with that email", Toast.LENGTH_LONG).show()
                    emailInput.setError("This email has not been registered.")
                    emailInput.requestFocus()

                }
            }
    }

    // Handle fragment change to login, etc....
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