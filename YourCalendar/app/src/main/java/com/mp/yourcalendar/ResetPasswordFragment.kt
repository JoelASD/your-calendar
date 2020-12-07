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
import kotlinx.android.synthetic.main.fragment_reset_password.*

class ResetPasswordFragment : Fragment() {

    private lateinit var emailInput: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val forgotPasswordView = inflater.inflate(R.layout.fragment_reset_password, container, false)

        // Get/listen to toLogInButton, set action
        /*val toLogInButton = forgotPasswordView.findViewById<View>(R.id.exitResetPasswordButton) as Button
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
        }*/

        return forgotPasswordView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        exitResetPasswordButton.setOnClickListener {
            changeFragment(LogInFragment())
        }

        resetPasswordButton.setOnClickListener {
            if (resetEmailEditText.text.trim().isNotEmpty()) {
                resetPassword(resetEmailEditText.text.trim().toString())
            } else {
                resetEmailEditText.requestFocus()
                resetEmailEditText.error = "Enter your account email!"
            }
        }
    }

    // reset password
    private fun resetPassword(email: String){
        Firebase.auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    // Email sent
                    Toast.makeText(requireActivity(), "Email sent!", Toast.LENGTH_LONG).show()
                    changeFragment(LogInFragment())
                } else {
                    //TODO: proper error handling
                    resetEmailEditText.setError("This email has not been registered.")
                    resetEmailEditText.requestFocus()
                }
            }
    }

    // Handle fragment change to login, etc....
    private fun changeFragment(fragment: Fragment){
        // Setup
        val fragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        // Transaction
        val transaction = fragmentTransaction
        transaction.replace(R.id.authFragmentFrame, fragment) // set new fragment to authFragmentFrame
        transaction.disallowAddToBackStack() //TODO: see if we should allow backstack?
        transaction.commit()
    }

}