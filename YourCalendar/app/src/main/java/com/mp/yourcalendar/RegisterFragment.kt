package com.mp.yourcalendar

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_register.*


class RegisterFragment : Fragment() {

    // Firebase
    private lateinit var database: DatabaseReference


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        toLogInButton.setOnClickListener {
            changeFragment(LogInFragment())
        }

        registerButton.setOnClickListener {
            val email = registrationEmailEditText.text.trim().toString()
            val pwd = registrationPasswordEditText.text.trim().toString()
            val pwdrepeat = registrationRepeatPasswordEditText.text.trim().toString()
            //check if all inputs are given
            if (email.isNotEmpty() && pwd.isNotEmpty() && pwdrepeat.isNotEmpty()) {
                if (pwd == pwdrepeat) {
                    createAccount(email, pwd)
                } else {
                    Toast.makeText(requireContext(), "Passwords do not match!", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(requireContext(), "All fields must be filled!", Toast.LENGTH_SHORT).show()
                if (registrationEmailEditText.text.trim().isEmpty()) registrationEmailEditText.error = "Enter email"
                if (registrationPasswordEditText.text.trim().isEmpty()) registrationPasswordEditText.error = "Enter password"
                if (registrationRepeatPasswordEditText.text.trim().isEmpty()) registrationRepeatPasswordEditText.error = "Repeat password"
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
        transaction.replace(R.id.authFragmentFrame, fragment)
        transaction.disallowAddToBackStack()
        transaction.commit()
    }

    private fun createAccount(email: String, password: String){
        Firebase.auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    // Send verification email
                    sendVerificationEmail()
                    //create users node to DB
                    createUserNodeToDB()
                    // Start MainActivity
                    var intent = Intent(requireActivity(), MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                } else {
                    try {
                        throw task.exception!!
                    } catch (e: FirebaseAuthUserCollisionException) {
                        Toast.makeText(requireActivity(), "Email is already registered", Toast.LENGTH_LONG).show()
                        registrationEmailEditText.requestFocus()
                        registrationEmailEditText.setError("Email already in use")
                    } catch (e: FirebaseAuthWeakPasswordException) {
                        Toast.makeText(requireActivity(), "Password too weak!", Toast.LENGTH_LONG).show()
                        registrationPasswordEditText.text.clear()
                        registrationPasswordEditText.setError("Password too weak")
                        registrationRepeatPasswordEditText.text.clear()
                        registrationPasswordEditText.requestFocus()
                    } finally {
                        Toast.makeText(requireActivity(), "Registration failed", Toast.LENGTH_LONG).show()
                    }
                }
            }
    }

    private fun sendVerificationEmail() {
        var user = Firebase.auth.currentUser
        user!!.sendEmailVerification()
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    Toast.makeText(requireActivity(), "Verification email sent!", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun createUserNodeToDB(){
        val userUID = Firebase.auth.uid
        database = Firebase.database.reference

        if (userUID != null){
            database.child("users").child(userUID).setValue(0)
        }
    }

}