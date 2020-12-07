package com.mp.yourcalendar

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.Toast.makeText
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
        return inflater.inflate(R.layout.fragment_log_in, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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
                makeText(requireContext(), "Enter email & password!", Toast.LENGTH_SHORT).show()
                if (logInEmailEditText.text.trim().isEmpty()) logInEmailEditText.error = "Enter email!"
                if (logInPasswordEditText.text.trim().isEmpty()) logInPasswordEditText.error = "Enter password!"
            }
        }
    }

    // User Login with email and psw
    private fun logInUser(email: String, password: String){
        Firebase.auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    // Login successful -> start MainActivity
                    var intent = Intent(requireActivity(), MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                } else {
                    // Login failed show error
                    try {
                        throw task.exception!!
                    } catch (e: FirebaseAuthInvalidCredentialsException) {
                        logInPasswordEditText.requestFocus()
                        logInPasswordEditText.error = "Invalid password!"
                    } catch (e: FirebaseAuthInvalidUserException) {
                        Toast.makeText(requireActivity(), "Account not found!", Toast.LENGTH_LONG).show()
                    }
                }
            }
    }

    // Handle fragment change to registration, forgot psw, etc....
    private fun changeFragment(fragment: Fragment){
        // Setup
        val fragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        // Transaction
        val transaction = fragmentTransaction
        transaction.replace(R.id.authFragmentFrame, fragment)
        transaction.disallowAddToBackStack()
        transaction.commit()
    }

}