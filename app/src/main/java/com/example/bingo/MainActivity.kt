package com.example.bingo

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import java.util.Timer
import java.util.TimerTask

class MainActivity : AppCompatActivity() {

    private lateinit var bingoNumberTextView: TextView
    private lateinit var startButton: Button
    private lateinit var newCardButton: Button
    private lateinit var manualDrawButton: Button
    private lateinit var database: FirebaseDatabase
    private lateinit var bingoNumberRef: DatabaseReference
    private val numbersList = mutableListOf<Int>()
    private val numberTextViews = mutableListOf<TextView>()
    private val selectedNumbers = mutableSetOf<Int>()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bingoNumberTextView = findViewById(R.id.bingoNumber)
        startButton = findViewById(R.id.startButton)
        newCardButton = findViewById(R.id.newCardButton)
        manualDrawButton = findViewById(R.id.manualDrawButton)

        database = FirebaseDatabase.getInstance()
        bingoNumberRef = database.reference.child("bingoNumber")

        numberTextViews.add(findViewById(R.id.number1))
        // Adicione os TextViews de number2 a number24 na lista

        for (i in 1..24) {
            numbersList.add(i)
        }

        startButton.setOnClickListener {
            startBingo()
        }

        newCardButton.setOnClickListener {
            generateNewCard()
        }

        manualDrawButton.setOnClickListener {
            manualDraw()
        }

        generateNewCard()

        bingoNumberRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val randomNumber = dataSnapshot.getValue(Int::class.java)
                randomNumber?.let {
                    bingoNumberTextView.text = it.toString()

                    if (it in selectedNumbers) {
                        selectedNumbers.remove(it)
                        updateNumberBackground(it)
                    }

                    if (selectedNumbers.isEmpty()) {
                        startButton.isEnabled = true
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Tratar erros na leitura do valor do Firebase
            }
        })
    }

    private fun startBingo() {
        startButton.isEnabled = false
        bingoNumberTextView.visibility = View.VISIBLE

        val timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    val randomNumber = generateRandomNumber()
                    bingoNumberTextView.text = randomNumber.toString()

                    if (randomNumber in selectedNumbers) {
                        selectedNumbers.remove(randomNumber)
                        updateNumberBackground(randomNumber)
                    }

                    if (selectedNumbers.isEmpty()) {
                        timer.cancel()
                        startButton.isEnabled = true
                    }

                    // Atualiza o número sorteado no Firebase
                    updateBingoNumber(randomNumber)
                }
            }
        }, 0, 1000)
    }

    private fun generateRandomNumber(): Int {
        val index = (0 until numbersList.size).random()
        return numbersList.removeAt(index)
    }

    private fun generateNewCard() {
        selectedNumbers.clear()
        numbersList.clear()
        numbersList.addAll(1..24)

        numberTextViews.forEach { textView ->
            val randomNumber = generateRandomNumber()
            textView.text = randomNumber.toString()

            if (randomNumber in selectedNumbers) {
                selectedNumbers.remove(randomNumber)
            }
            selectedNumbers.add(randomNumber)
        }

        numberTextViews.forEach { textView ->
            val number = textView.text.toString().toInt()
            updateNumberBackground(number)
        }

        startButton.isEnabled = true
    }



    private fun manualDraw() {
        val randomNumber = generateRandomNumber()
        bingoNumberTextView.text = randomNumber.toString()

        if (randomNumber in selectedNumbers) {
            selectedNumbers.remove(randomNumber)
            updateNumberBackground(randomNumber)
        }

        if (selectedNumbers.isEmpty()) {
            startButton.isEnabled = true
        }

        // Atualiza o número sorteado no Firebase
        updateBingoNumber(randomNumber)
    }

    private fun updateBingoNumber(number: Int) {
        bingoNumberRef.setValue(number)
    }

    private fun updateNumberBackground(number: Int) {
        val textView = numberTextViews.find { it.text.toString().toInt() == number }
        textView?.setBackgroundResource(R.drawable.rounded_button_gray)
        textView?.setTextColor(Color.WHITE)
    }
}


