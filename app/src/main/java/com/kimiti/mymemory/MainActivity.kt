package com.kimiti.mymemory

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.kimiti.mymemory.models.BoardSize
import com.kimiti.mymemory.models.MemoryCard
import com.kimiti.mymemory.models.MemoryGame
import com.kimiti.mymemory.utils.DEFAULT_ICONS
import kotlin.math.log

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var adapter: MemoryBoardAdapter
    private lateinit var memoryGame: MemoryGame
    private lateinit var rvBoard: RecyclerView
    private lateinit var clRoot: ConstraintLayout
    private lateinit var tvNumMoves: TextView
    private lateinit var tvNumPairs: TextView

    private var boardSize: BoardSize = BoardSize.EASY


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        clRoot = findViewById(R.id.clRoot)
        rvBoard = findViewById(R.id.rvBoard)
        tvNumMoves = findViewById(R.id.tvNumMoves)
        tvNumPairs = findViewById(R.id.tvNumPairs)

        setupBoard()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mi_refresh -> {
                // setup the game again
                if (memoryGame.getNumMoves() > 0 && !memoryGame.haveWonGame()) {
                    showAlertDialog(
                        "quit your current game",
                        null,
                        View.OnClickListener { setupBoard() })
                } else {
                    setupBoard()
                }
                return true
            }
            R.id.mi_new_size -> {
                showNewSizeDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showNewSizeDialog() {
        val boardSizeView = LayoutInflater.from(this).inflate(R.layout.dialog_board_size, null)
        val radioGroupSize = boardSizeView.findViewById<RadioGroup>(R.id.radioGroup)
        when (boardSize) {
            BoardSize.EASY -> radioGroupSize.check(R.id.rbEasy)
            BoardSize.MEDIUM -> radioGroupSize.check(R.id.rbMedium)
            BoardSize.HARD -> radioGroupSize.check(R.id.rbHard)
        }
        showAlertDialog("Select new size", boardSizeView) {
            boardSize = when (radioGroupSize.checkedRadioButtonId) {
                R.id.rbEasy -> BoardSize.EASY
                R.id.rbMedium -> BoardSize.MEDIUM
                else -> BoardSize.HARD
            }
            setupBoard()
        }
    }

    private fun showAlertDialog(
        title: String,
        view: View?,
        positiveClickListener: View.OnClickListener
    ) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(view)
            .setNegativeButton("cancel", null)
            .setPositiveButton("OK") { _, _ -> positiveClickListener.onClick(null) }.show()
    }

    private fun setupBoard() {
        when (boardSize) {
            BoardSize.EASY -> {
                tvNumMoves.text = "Easy 4 x 2"
                tvNumPairs.text = "Pairs 0 /4"
            }
            BoardSize.MEDIUM -> {
                tvNumMoves.text = "Medium 6 x 3"
                tvNumPairs.text = "Pairs 0 /9"
            }
            BoardSize.HARD -> {
                tvNumMoves.text = "Hard 6 x 4"
                tvNumPairs.text = "Pairs 0 /12"
            }
        }
        tvNumPairs.setTextColor(ContextCompat.getColor(this, R.color.color_progress_none))
        memoryGame = MemoryGame(boardSize)
        adapter = MemoryBoardAdapter(this, boardSize, memoryGame.cards,
            object : MemoryBoardAdapter.CardClickListener {
                override fun onCardClicked(position: Int) {
                    updateGameWithFlip(position)
                }

            })
        rvBoard.adapter = adapter
        rvBoard.setHasFixedSize(true)
        rvBoard.layoutManager = GridLayoutManager(this, boardSize.getWidth())
    }


    private fun updateGameWithFlip(position: Int) {
        //Error checking
        if (memoryGame.haveWonGame()) {
            Snackbar.make(clRoot, "you have already won", Snackbar.LENGTH_LONG).show()
            return
        }

        if (memoryGame.isCardFaceUp(position)) {
            Snackbar.make(clRoot, "invalid move", Snackbar.LENGTH_SHORT).show()
            return
        }

        if (memoryGame.flipCard(position)) {
            Log.i(TAG, "Found a match num pairs found ${memoryGame.numPairsFound}")
            tvNumPairs.text = "Pairs: ${memoryGame.numPairsFound} / ${boardSize.getNumPairs()}"
            if (memoryGame.haveWonGame()) {
                Snackbar.make(clRoot, "you have won congratulations", Snackbar.LENGTH_LONG).show()
            }
        }

        tvNumMoves.text = "Moves: ${memoryGame.getNumMoves()}"

        adapter.notifyDataSetChanged()
    }
}