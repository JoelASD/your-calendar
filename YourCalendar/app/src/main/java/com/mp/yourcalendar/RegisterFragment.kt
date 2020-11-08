package com.mp.yourcalendar

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.MalformedJsonException
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class RegisterFragment : Fragment() {

    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var repeatPasswordInput: EditText

    // Firebase
    //private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val registerView = inflater.inflate(R.layout.fragment_register, container, false)

        //auth = FirebaseAuth.getInstance()

        // Get/listen to toLogInButton, set action
        val toLogInButton = registerView.findViewById<View>(R.id.toLogInButton) as Button
        toLogInButton.setOnClickListener {
            changeFragment(LogInFragment()) // Go to login
        }

        // Get registerButton, emailInput, passwordInput and repeatPasswordInput
        val registerButton = registerView.findViewById<View>(R.id.registerButton) as Button
        emailInput = registerView.findViewById<View>(R.id.registrationEmailEditText) as EditText
        passwordInput = registerView.findViewById<View>(R.id.registrationPasswordEditText) as EditText
        repeatPasswordInput = registerView.findViewById<View>(R.id.registrationRepeatPasswordEditText) as EditText
        //  Listen to registerButton
        registerButton.setOnClickListener {
            //check if all inputs are given
            if(emailInput.text.trim().toString().isNotEmpty() && passwordInput.text.trim().toString().isNotEmpty() && repeatPasswordInput.text.trim().toString().isNotEmpty()){
                // If input are give, check that given passwords match
                if(passwordInput.text.trim().toString() == repeatPasswordInput.text.trim().toString()){
                    // If passwords match, try to create account
                    createAccount(emailInput.text.trim().toString(), passwordInput.text.trim().toString())
                } else {
                    // Passwords do not match
                    //Toast.makeText(activity!!, "Passwords do not match!", Toast.LENGTH_LONG).show()
                    passwordInput.text.clear()
                    passwordInput.setError("Password did not match")
                    repeatPasswordInput.text.clear()
                    repeatPasswordInput.setError("Password did not match")

                }
            } else {
                //TODO: proper error
                Toast.makeText(activity!!, "All fields must be filled!", Toast.LENGTH_LONG).show()
            }
        }

        return registerView
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

    fun createAccount(email: String, password: String){
        Firebase.auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    // Send verification email
                    sendVerificationEmail()
                    //create users node to DB
                    createUserNodeToDB()
                    // Start MainActivity
                    var intent = Intent(activity!!, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    //TODO: more error handling
                    try {
                        throw task.exception!!
                    } catch (e: FirebaseAuthUserCollisionException) {
                        //Toast.makeText(activity!!, "Email is already registered", Toast.LENGTH_LONG).show()
                        emailInput.requestFocus()
                        emailInput.setError("Email already in use")
                    } catch (e: FirebaseAuthWeakPasswordException) {
                        // TODO: set psw regs
                        //Toast.makeText(activity!!, "Password too weak!", Toast.LENGTH_LONG).show()
                        passwordInput.text.clear()
                        passwordInput.setError("Password too weak")
                        repeatPasswordInput.text.clear()
                        repeatPasswordInput.setError("Password too weak")
                        passwordInput.requestFocus()
                    } finally {
                        Toast.makeText(activity!!, "Registration failed", Toast.LENGTH_LONG).show()
                    }
                }
            }
    }

    fun sendVerificationEmail() {
        var user = Firebase.auth.currentUser
        user!!.sendEmailVerification()
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    // Email sent properly
                    Toast.makeText(activity!!, "Verification email sent!", Toast.LENGTH_LONG).show()
                } else {
                    //TODO: better error handling
                    Toast.makeText(activity!!, "Could not send verification email", Toast.LENGTH_LONG).show()
                }
            }
    }

    fun createUserNodeToDB(){
        val userUID = Firebase.auth.uid
        database = Firebase.database.reference
        //val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        //val ref: DatabaseReference = database.reference
        if (userUID != null){
            database.child("users").child(userUID).setValue(0)
        }
    }

}